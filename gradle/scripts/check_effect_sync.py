#!/usr/bin/env python3
"""Check that the shipped Java defaults and the shipped pack config still say the same thing.

Three pairs are kept in step by hand, because each is a Java table that a config `reset()` writes
and a pack file that the game actually reads:

    GodNodeEffectDefaults.java   <->  config/the_vault/gods/god_node_effects_<god>.json
    GreedMedallionsConfig.java   <->  config/the_vault/gods/greed_medallions.json
    GreedRanksConfig.java        <->  config/the_vault/gods/greed_ranks.json

When a pair drifts, the pack plays on one set of numbers until anything makes the config
regenerate - a syntax error, a fresh install, a deleted file - and then silently plays on the
other. The god effect pair drifted exactly that way once and shipped an attribute id that no
longer existed, which is a boot crash rather than a balance change.

`checkGodTreeSync` gates the tree topology the same way. Wired into `gradlew check` as
`checkGodEffectSync`. Run by hand with:

    python check_effect_sync.py --src <src/main/java> --config <pack config/the_vault dir>
"""

import argparse
import json
import re
import struct
import sys
from pathlib import Path

GODS = ("idona", "wendarr", "velara", "tenos")

HANDLER_CONSTANTS = {
    "GEAR_ATTRIBUTE_SCALED": "gear_attribute_scaled",
    "PIETY": "piety",
}

PUT = re.compile(r'put\(\s*map\s*,\s*"([\w]+)"\s*,\s*([A-Z_]+|"[\w.:]+")\s*,'
                 r'\s*new float\[\]\{([^}]*)\}\s*(.*?)\);', re.S)
GOD_METHOD = re.compile(r'^    private static void (\w+)\(GodNodeEffectsConfig\.EffectMap map\) \{$')
NUMBER = re.compile(r'^-?\d+(\.\d+)?([Ee][-+]?\d+)?([FfDdLl])?$')


def as_float32(value):
    """Java rounds an F-suffixed literal to float32 before Gson ever prints it."""
    return struct.unpack("f", struct.pack("f", value))[0]


def parse_literal(token):
    """A Java numeric or string literal as the value Gson would write for it."""
    token = token.strip()
    if token.startswith('"'):
        return token[1:-1]
    if not NUMBER.match(token):
        raise ValueError("unparseable literal %r" % token)
    suffix = token[-1]
    if suffix in "FfDdLl":
        token = token[:-1]
        if suffix in "Ff":
            return as_float32(float(token))
        if suffix in "Dd":
            return float(token)
        return int(token)
    return float(token) if "." in token or "e" in token.lower() else int(token)


def split_fields(tail):
    """The `"name", value` pairs trailing a put(...) call, in source order."""
    tokens, depth, current = [], 0, ""
    for char in tail:
        if char == "," and depth == 0:
            tokens.append(current)
            current = ""
            continue
        if char in "([{":
            depth += 1
        elif char in ")]}":
            depth -= 1
        current += char
    tokens.append(current)
    tokens = [token.strip() for token in tokens if token.strip()]
    if len(tokens) % 2:
        raise ValueError("odd number of field tokens: %r" % tokens)
    return [(parse_literal(tokens[i]), parse_literal(tokens[i + 1])) for i in range(0, len(tokens), 2)]


def parse_effect_defaults(path):
    """Replays GodNodeEffectDefaults' put(...) calls into the JSON its `put` helper builds."""
    text = path.read_text(encoding="utf-8")
    per_god, current = {}, None
    body = []
    for line in text.splitlines():
        method = GOD_METHOD.match(line)
        if method:
            current = method.group(1) if method.group(1) in GODS else None
            body = []
            continue
        if current is None:
            continue
        if line == "    }":
            per_god[current] = parse_puts("\n".join(body))
            current = None
            continue
        body.append(line)
    return per_god


def parse_puts(body):
    effects = {}
    for match in PUT.finditer(body):
        effect_id, handler, values, tail = match.groups()
        handler = HANDLER_CONSTANTS.get(handler, handler.strip('"'))
        entry = {"handler": handler,
                 "values": [parse_literal(v) for v in values.split(",") if v.strip()]}
        for name, value in split_fields(tail):
            entry[name] = value
        effects[effect_id] = entry
    return effects


def normalise(value):
    """Compares a config number against a Java literal at the precision Java stored it in."""
    if isinstance(value, bool) or isinstance(value, str):
        return value
    if isinstance(value, list):
        return [normalise(item) for item in value]
    if isinstance(value, dict):
        return {key: normalise(item) for key, item in value.items()}
    if isinstance(value, float):
        return round(as_float32(value), 6)
    return value


def diff(label, shipped, configured, failures):
    for key in sorted(set(shipped) | set(configured)):
        if key not in configured:
            failures.append("%s: '%s' is in the Java defaults but not the config" % (label, key))
        elif key not in shipped:
            failures.append("%s: '%s' is in the config but not the Java defaults" % (label, key))
        elif normalise(shipped[key]) != normalise(configured[key]):
            failures.append("%s: '%s' differs - Java has %r, config has %r"
                            % (label, key, shipped[key], configured[key]))


def check_effects(src, config_dir, failures):
    shipped = parse_effect_defaults(src / "xyz/iwolfking/woldsvaults/config/gods/GodNodeEffectDefaults.java")
    for god in GODS:
        path = config_dir / "gods" / ("god_node_effects_%s.json" % god)
        configured = json.loads(path.read_text(encoding="utf-8")).get("effects", {})
        before = len(failures)
        diff("god_node_effects_%s.json" % god, shipped.get(god, {}), configured, failures)
        if len(failures) == before:
            print("%s: in sync (%d effects)" % (god, len(configured)))


def check_medallions(src, config_dir, failures):
    path = src / "xyz/iwolfking/woldsvaults/config/GreedMedallionsConfig.java"
    text = path.read_text(encoding="utf-8")
    fields = re.findall(r'@Expose public (?:int|double) (\w+);', text)
    tier_fields = fields[:8]
    rows = re.findall(r'this\.tiers\.put\("(\d+)", Tier\.of\(([^)]*)\)\);', text)
    shipped_tiers = {}
    for rank, args in rows:
        values = [parse_literal(a) for a in args.split(",")]
        shipped_tiers[rank] = dict(zip(tier_fields, values))
    shipped_scalars = {name: parse_literal(value) for name, value
                       in re.findall(r'this\.(?:gates|spawn)\.(\w+) = ([^;]+);', text)}

    configured = json.loads((config_dir / "gods" / "greed_medallions.json").read_text(encoding="utf-8"))
    configured_scalars = dict(configured.get("gates", {}))
    configured_scalars.update(configured.get("spawn", {}))
    before = len(failures)
    diff("greed_medallions.json tiers", shipped_tiers, configured.get("tiers", {}), failures)
    diff("greed_medallions.json", shipped_scalars, configured_scalars, failures)
    if len(failures) == before:
        print("medallions: in sync (%d tiers, %d scalars)" % (len(shipped_tiers), len(shipped_scalars)))


def check_ranks(src, config_dir, failures):
    text = (src / "xyz/iwolfking/woldsvaults/config/GreedRanksConfig.java").read_text(encoding="utf-8")
    shipped = {
        "bandSize": parse_literal(re.search(r'this\.bandSize = ([^;]+);', text).group(1)),
        "bandNames": [name for name in re.findall(r'"([\w]+)"',
                      re.search(r'this\.bandNames = new ArrayList<>\(List\.of\(([^)]*)\)\);', text).group(1))],
        "thresholds": [parse_literal(v) for v in
                       re.search(r'this\.thresholds = new ArrayList<>\(List\.of\(([^)]*)\)\);', text).group(1).split(",")],
        "godLevelGates": {rank: parse_literal(level) for rank, level
                          in re.findall(r'this\.godLevelGates\.put\("(\d+)", ([^)]+)\);', text)},
    }
    configured = json.loads((config_dir / "gods" / "greed_ranks.json").read_text(encoding="utf-8"))
    before = len(failures)
    diff("greed_ranks.json", shipped, {key: configured.get(key) for key in shipped}, failures)
    if len(failures) == before:
        print("ranks: in sync (%d ranks, %d bands)" % (len(shipped["thresholds"]), len(shipped["bandNames"])))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--src", required=True, type=Path, help="src/main/java of the addon")
    parser.add_argument("--config", required=True, type=Path, help="the pack's config/the_vault directory")
    args = parser.parse_args()

    failures = []
    check_effects(args.src, args.config, failures)
    check_medallions(args.src, args.config, failures)
    check_ranks(args.src, args.config, failures)
    for failure in failures:
        print("DRIFT %s" % failure)
    if failures:
        print("\n%d difference(s). The Java defaults are what a config regeneration writes, so a pack "
              "playing on the config is one deleted file away from playing on these instead."
              % len(failures))
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
