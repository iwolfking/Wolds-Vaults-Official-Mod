#version 150

in vec2 texCoord0;

out vec4 fragColor;

// The greed Main tab's animated touches. Each is one quad whose UVs are GUI pixel positions local to what it decorates,
// so every effect lands on whole pixels. Area.z picks the mode:
// 0 = the Take Trial button: a gold-rimmed plate that warms to a red heat towards the bottom, with embers drifting up
//     out of it and a few pixels past its top edge (the quad starts above the button to leave them room). Params.x is
//     1 while hovered; Text is the label's box, which the embers keep clear of.
// 1 = the reputation bar's molten fill: shaded gold with a bright top lip, a slow ripple and a shine that passes now
//     and then, and a white-hot leading edge that lights the 3 px of empty track ahead of it. Params.x is the filled
//     width, Params.y the whole track's.

uniform vec4 ColorModulator;
uniform vec4 Area;   // width, height, mode, seconds
uniform vec4 Params; // per mode, see above
uniform vec4 Text;   // label box x0, y0, x1, y1 in local pixels (trial mode)

const vec3 INK = vec3(0.165, 0.110, 0.016);
const vec3 GOLD = vec3(1.0, 0.769, 0.302);
const vec3 GOLD_DEEP = vec3(0.420, 0.306, 0.071);
const vec3 HEAT = vec3(0.416, 0.133, 0.039);
const vec3 EMBER = vec3(1.0, 0.478, 0.180);
const vec3 CREAM = vec3(1.0, 0.957, 0.816);
const vec3 LIP = vec3(1.0, 0.851, 0.471);
const vec3 MOLTEN_HI = vec3(0.827, 0.616, 0.200);
const vec3 MOLTEN_LO = vec3(0.431, 0.306, 0.078);
const int EMBERS = 34;

float hash(float n) {
    return fract(sin(n * 127.1 + 311.7) * 43758.5453);
}

// the brightest ember on this pixel, if any: each one loops from the bottom of the plate to above its top, fading in
// and out, cooling from cream through gold to orange as it rises
vec4 ember(vec2 p, float w, float h, float t) {
    vec4 best = vec4(0.0);
    for (int i = 0; i < EMBERS; i++) {
        float fi = float(i);
        float period = 1.4 + hash(fi * 3.1) * 1.6;
        float life = fract(t / period + hash(fi * 7.7));
        float x = 3.0 + hash(fi) * (w - 6.0) + sin(t * 2.0 + fi) * 1.5 * life;
        float y = h - 3.0 - life * (h + 4.0);
        if (floor(x) != p.x || floor(y) != p.y) {
            continue;
        }
        if (x >= Text.x && x <= Text.z && y >= Text.y && y <= Text.w) {
            continue;
        }
        vec3 c = life < 0.5 ? mix(CREAM, GOLD, life * 2.0) : mix(GOLD, EMBER, (life - 0.5) * 2.0);
        float a = sin(life * 3.14159) * 0.95;
        if (a > best.a) {
            best = vec4(c, a);
        }
    }
    return best;
}

void trial(vec2 p, float w, float h, float t) {
    vec4 e = ember(p, w, h, t);
    if (p.x < 0.0 || p.y < 0.0 || p.x >= w || p.y >= h) {
        if (e.a <= 0.003) {
            discard;
        }
        fragColor = e * ColorModulator;
        return;
    }
    float edge = min(min(p.x, p.y), min(w - 1.0 - p.x, h - 1.0 - p.y));
    float hover = Params.x;
    vec3 col;
    if (edge < 0.5) {
        col = INK;
    } else if (edge < 1.5) {
        col = GOLD * (0.85 + 0.15 * sin(t * 3.0));
    } else {
        float v = p.y / h;
        col = mix(GOLD_DEEP * mix(1.05, 1.35, hover), HEAT, v * v * mix(0.7, 0.85, hover));
    }
    col = mix(col, e.rgb, e.a);
    fragColor = vec4(min(col, vec3(1.0)), 1.0) * ColorModulator;
}

void molten(vec2 p, float h, float t) {
    float filled = Params.x;
    float track = Params.y;
    if (p.x < filled) {
        float v = h > 1.0 ? p.y / (h - 1.0) : 0.0;
        vec3 col = v < 0.1 ? LIP : mix(MOLTEN_HI, MOLTEN_LO, v);
        col *= 1.0 + 0.08 * sin((p.x - t * 22.0) / 5.0 + v * 2.5);
        float sweep = fract(t / 2.6) * (filled + 60.0) - 30.0;
        float d = p.x - sweep;
        col += vec3(0.4 * exp(-d * d / 26.0));
        if (filled < track && p.x == filled - 1.0) {
            col = CREAM;
        }
        fragColor = vec4(min(col, vec3(1.0)), 1.0) * ColorModulator;
        return;
    }
    if (filled < track && p.x < filled + 3.0) {
        float pulse = 0.7 + 0.3 * sin(t * 4.0);
        fragColor = vec4(GOLD, 0.4 * pulse * (1.0 - (p.x - filled) / 3.0)) * ColorModulator;
        return;
    }
    discard;
}

void main() {
    vec2 p = floor(texCoord0);
    if (Area.z < 0.5) {
        trial(p, Area.x, Area.y, Area.w);
    } else {
        molten(p, Area.y, Area.w);
    }
}
