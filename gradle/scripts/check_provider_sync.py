#!/usr/bin/env python3
"""Check that ModGodTreesProvider and the shipped god tree config still say the same thing.

`runData` is broken in this checkout (pre-existing, `ModItemModelProvider.researchToken` dies on
a missing base-mod texture), so `config/the_vault/gods/god_tree_<god>.json` and its
`_gui_styles.json` companion are hand-synced. This parses the provider's builder calls, replays
them the way `GodTreeBuilder` would, and diffs the result against the config that ships.

Wired into `gradlew check` as `checkGodTreeSync`. Run by hand with:

    python check_provider_sync.py --provider <ModGodTreesProvider.java> --config <gods config dir>
"""

import argparse
import json
import sys
from pathlib import Path
import re

DEFAULT_PROVIDER = (Path(__file__).resolve().parents[2]
                    / "src/main/java/xyz/iwolfking/woldsvaults/datagen/ModGodTreesProvider.java")

METHOD = re.compile(r'^    private static (?:GodTreeBuilder (\w+)\(\)'
                    r'|void (\w+)\(GodTreeBuilder tree(?:, String id, int x, int y)?\)) \{$')
ROOT = re.compile(r'^\s*tree\.root\("([\w]+)", "([^"]*)", (-?\d+), (-?\d+)\);$')
TYPED = re.compile(r'^\s*tree\.(minor|major|stat|disabledMinor|disabledMajor|disabledStat)'
                   r'\("([\w]+)", "([^"]*)", "([\w]+)", icon\("([\w]+)"\), (-?\d+), (-?\d+)\);$')
HELPER_BODY = re.compile(r'^\s*tree\.(stat|disabledStat)\(id, "([^"]*)", "([\w]+)", icon\("([\w]+)"\), x, y\);$')
CALL = re.compile(r'^\s*(\w+)\(tree, "([\w]+)", (-?\d+), (-?\d+)\);$')
GROUP_CALL = re.compile(r'^\s*(\w+)\(tree\);$')
EDGE = re.compile(r'^\s*tree\.edge\("([\w]+)", "([\w]+)"\);$')
LABEL = re.compile(r'^\s*tree\.label\("([^"]*)", (-?\d+), (-?\d+)\);$')
ICON_PATH = "the_vault:textures/gui/greed/nodes/{}.png"
GODS = ("idona", "wendarr", "velara", "tenos")
DEFAULT_COST = 1
FRAME_STAT = "CIRCLE"
FRAME_NOTABLE = "SQUARE"


def parse_methods(text):
    """method name -> its body lines, in order."""
    methods, current = {}, None
    for line in text.splitlines():
        match = METHOD.match(line)
        if match:
            current = match.group(1) or match.group(2)
            methods[current] = []
            continue
        if current is not None:
            if line == "    }":
                current = None
            else:
                methods[current].append(line)
    return methods


def build(god, methods):
    """Replay the builder calls the way GodTreeBuilder would, into the shipped config shapes."""
    helpers = {}
    for name, body in methods.items():
        for line in body:
            match = HELPER_BODY.match(line)
            if match:
                kind, display, effect, icon = match.groups()
                helpers[name] = (display, effect, icon, kind == "stat")

    nodes, edges, labels, styles = [], [], [], {}

    def add(nid, display, kind, effect, icon, x, y, enabled):
        node = {"id": nid, "name": display, "type": kind}
        if effect is not None:
            node["effect"] = effect
        node["cost"] = DEFAULT_COST
        node["enabled"] = enabled
        nodes.append(node)
        style = {"x": x, "y": y, "frameType": FRAME_STAT if kind == "stat" else FRAME_NOTABLE}
        if icon is not None:
            style["icon"] = icon
        styles[nid] = style

    def replay(body):
        for line in body:
            match = GROUP_CALL.match(line)
            if match and match.group(1) in methods:
                replay(methods[match.group(1)])
                continue
            match = ROOT.match(line)
            if match:
                nid, display, x, y = match.groups()
                add(nid, display, "root", None, None, int(x), int(y), True)
                continue
            match = TYPED.match(line)
            if match:
                call, nid, display, effect, icon, x, y = match.groups()
                kind = call.replace("disabled", "").lower()
                add(nid, display, kind, effect, ICON_PATH.format(icon), int(x), int(y),
                    not call.startswith("disabled"))
                continue
            match = CALL.match(line)
            if match and match.group(1) in helpers:
                display, effect, icon, enabled = helpers[match.group(1)]
                add(match.group(2), display, "stat", effect, ICON_PATH.format(icon),
                    int(match.group(3)), int(match.group(4)), enabled)
                continue
            match = EDGE.match(line)
            if match:
                edges.append([match.group(1), match.group(2)])
                continue
            match = LABEL.match(line)
            if match:
                labels.append({"text": match.group(1), "x": int(match.group(2)), "y": int(match.group(3))})

    replay(methods[god])
    return {"nodes": nodes, "edges": edges, "labels": labels}, {"styles": styles}


def diff_sequences(field, built, shipped, problems):
    if len(built) != len(shipped):
        problems.append(f"{field}: provider has {len(built)}, config has {len(shipped)}")
    for index, (left, right) in enumerate(zip(built, shipped)):
        if left != right:
            problems.append(f"{field}[{index}]: provider {left} != config {right}")
            if len([p for p in problems if p.startswith(field)]) > 3:
                return


def diff_styles(built, shipped, problems):
    missing = [nid for nid in built if nid not in shipped]
    extra = [nid for nid in shipped if nid not in built]
    if missing:
        problems.append(f"styles: config is missing {len(missing)} node(s), first {missing[:3]}")
    if extra:
        problems.append(f"styles: config has {len(extra)} node(s) the provider does not declare, "
                        f"first {extra[:3]}")
    mismatched = 0
    for nid, style in built.items():
        if nid in shipped and shipped[nid] != style:
            problems.append(f"styles[{nid}]: provider {style} != config {shipped[nid]}")
            mismatched += 1
            if mismatched > 3:
                return


def check(god, methods, config_dir):
    tree, styles = build(god, methods)
    shipped_tree = json.loads((config_dir / f"god_tree_{god}.json").read_text(encoding="utf-8"))
    shipped_styles = json.loads((config_dir / f"god_tree_{god}_gui_styles.json").read_text(encoding="utf-8"))
    problems = []
    for field in ("nodes", "edges", "labels"):
        diff_sequences(field, tree[field], shipped_tree.get(field, []), problems)
    diff_styles(styles["styles"], shipped_styles.get("styles", {}), problems)
    counts = (f'{len(tree["nodes"])} nodes / {len(tree["edges"])} edges / '
              f'{len(tree["labels"])} labels / {len(styles["styles"])} styles')
    if problems:
        print(f"{god}: MISMATCH ({counts})")
        for problem in problems[:10]:
            print("   ", problem)
        return False
    print(f"{god}: in sync ({counts})")
    return True


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--provider", type=Path, default=DEFAULT_PROVIDER,
                        help="path to ModGodTreesProvider.java")
    parser.add_argument("--config", type=Path, required=False,
                        help="path to the pack's config/the_vault/gods directory")
    args = parser.parse_args()
    if args.config is None:
        print("check_provider_sync: --config <pack>/config/the_vault/gods is required", file=sys.stderr)
        return 2
    if not args.provider.is_file():
        print(f"check_provider_sync: no provider at {args.provider}", file=sys.stderr)
        return 2
    if not args.config.is_dir():
        print(f"check_provider_sync: no god config directory at {args.config}", file=sys.stderr)
        return 2
    methods = parse_methods(args.provider.read_text(encoding="utf-8"))
    missing = [god for god in GODS if god not in methods]
    if missing:
        print(f"check_provider_sync: provider declares no tree for {missing}", file=sys.stderr)
        return 2
    return 0 if all([check(god, methods, args.config) for god in GODS]) else 1


if __name__ == "__main__":
    sys.exit(main())
