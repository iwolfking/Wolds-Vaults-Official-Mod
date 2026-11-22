#version 150

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// A greed milestone row's frame in its rarity's colour (common 1 .. mythic 5). FrameSize.w picks what is drawn:
// 0 = the frame itself, one quad over the row plus a glow margin, built here pixel by pixel; 1 = its flourishes,
// greyscale sprites from the ornament atlas recoloured by brightness, with their frame-local position in
// vertexColor.rg (offset 16 px so they can sit outside the row). Everything that moves is here: a light running
// round the border (rare and up), a breathing glow (epic and up), sparkles (omega and up), twinkling gems and, for
// mythic, a colour that drifts along the frame. Distances are GUI pixels, so each one stays a crisp block.

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform vec4 FrameSize;   // row width, row height, glow margin, mode (0 frame, 1 flourishes)
uniform vec4 FrameStyle;  // rarity 1..5, seconds, per-row seed, 1 for the navy row plate (0 grey)
uniform vec4 FrameColour; // rarity colour rgb, unused

const float TAU = 6.28318530718;
const vec3 PLATE = vec3(0.168, 0.168, 0.168);
const vec3 NAVY_PLATE = vec3(0.106, 0.133, 0.192);

float hash(float n) {
    return fract(sin(n * 127.1 + 311.7) * 43758.5453);
}

vec3 hueShift(vec3 c, float a) {
    const vec3 k = vec3(0.57735);
    float ca = cos(a);
    return c * ca + cross(k, c) * sin(a) + k * dot(k, c) * (1.0 - ca);
}

// the rarity colour at a brightness: 0 its darkest shade, 0.55 the colour itself, 1 nearly white
vec3 tone(vec3 c, float v) {
    return v < 0.55 ? mix(c * 0.16, c, v / 0.55) : mix(c, vec3(1.0), (v - 0.55) / 0.45 * 0.85);
}

// how far clockwise from the top-left corner the border point nearest p is
float perimeter(vec2 p, vec2 size) {
    vec2 q = clamp(p, vec2(0.0), size - 1.0);
    float dl = q.x;
    float dr = size.x - 1.0 - q.x;
    float dt = q.y;
    float db = size.y - 1.0 - q.y;
    float m = min(min(dl, dr), min(dt, db));
    if (m == dt) {
        return q.x;
    }
    if (m == dr) {
        return size.x + q.y;
    }
    if (m == db) {
        return size.x + size.y + (size.x - 1.0 - q.x);
    }
    return 2.0 * size.x + size.y + (size.y - 1.0 - q.y);
}

// the running light: one band (two on mythic) chasing round the border
float shimmer(float s, float around, float time, float rarity) {
    float bands = rarity >= 5.0 ? 2.0 : 1.0;
    float speed = around / (rarity >= 4.0 ? 2.6 : 3.6);
    float width = 5.0 + rarity * 2.0;
    float best = 0.0;
    for (int i = 0; i < 2; i++) {
        if (float(i) >= bands) {
            break;
        }
        float c = mod(time * speed + float(i) * around / bands, around);
        float d = abs(s - c);
        d = min(d, around - d);
        best = max(best, exp(-(d * d) / (width * width)));
    }
    return best;
}

// a sparkle at this point of the border, 0..1: each 9 px stretch has one spot that flashes now and then
float sparkle(float s, float time, float seed, float rarity) {
    float cell = floor(s / 9.0);
    float spot = floor(hash(cell + seed) * 9.0);
    if (floor(mod(s, 9.0)) != spot) {
        return 0.0;
    }
    float period = 1.7;
    float clock = time / period + hash(cell * 3.1 + seed);
    float turn = floor(clock);
    if (hash(cell * 7.7 + turn * 13.7 + seed) < (rarity >= 5.0 ? 0.8 : 0.88)) {
        return 0.0;
    }
    float t = fract(clock);
    return pow(sin(t * 3.14159), 4.0);
}

void main() {
    vec2 size = FrameSize.xy;
    float pad = FrameSize.z;
    float rarity = FrameStyle.x;
    float time = FrameStyle.y;
    float seed = FrameStyle.z;
    float around = 2.0 * (size.x + size.y);
    float shimmerAmount = rarity >= 2.0 ? 0.35 + 0.1 * rarity : 0.0;

    if (FrameSize.w > 0.5) {
        vec4 texel = texture(Sampler0, texCoord0);
        if (texel.a < 0.5) {
            discard;
        }
        vec2 local = vertexColor.rg * (size + 32.0) - 16.0;
        float s = perimeter(local, size);
        vec3 c = FrameColour.rgb;
        if (rarity >= 5.0) {
            c = hueShift(c, 0.25 * sin(time * 0.8 + s / around * TAU));
        }
        vec3 col = tone(c, texel.r);
        col += vec3(1.0) * shimmer(s, around, time, rarity) * shimmerAmount * smoothstep(0.2, 0.8, texel.r);
        if (rarity >= 3.0 && texel.r > 0.95) {
            float twinkle = sin(time * 3.5 + hash(floor(local.x) + floor(local.y) * 31.0 + seed) * TAU);
            col = mix(col, vec3(1.0), 0.45 * max(0.0, twinkle));
        }
        if (rarity >= 5.0) {
            col *= 1.0 + 0.08 * sin(time * 2.5 + seed);
        }
        fragColor = vec4(col, 1.0) * ColorModulator;
        return;
    }

    vec2 p = floor(texCoord0 * (size + 2.0 * pad)) - pad;
    bool inside = p.x >= 0.0 && p.y >= 0.0 && p.x < size.x && p.y < size.y;
    float s = perimeter(p, size);
    vec3 c = FrameColour.rgb;
    if (rarity >= 5.0) {
        c = hueShift(c, 0.25 * sin(time * 0.8 + s / around * TAU));
    }
    float pulse = 0.7 + 0.3 * sin(time * 2.2 + seed);

    // rounded corners: the outermost corner pixel is left out
    bool corner = (p.x == 0.0 || p.x == size.x - 1.0) && (p.y == 0.0 || p.y == size.y - 1.0);

    if (!inside || corner) {
        float od = inside ? 1.0 : max(max(-p.x, p.x - size.x + 1.0), max(-p.y, p.y - size.y + 1.0));
        float glow = rarity >= 3.0 ? 0.08 + 0.1 * (rarity - 2.0) : (rarity >= 2.0 ? 0.08 : 0.0);
        float fade = clamp(1.0 - (od - 1.0) / max(pad, 1.0), 0.0, 1.0);
        float a = glow * pulse * fade * fade;
        vec3 col = tone(c, 0.7);
        if (rarity >= 4.0 && od <= 1.0) {
            float sp = sparkle(s, time, seed, rarity);
            col = mix(col, vec3(1.0), sp);
            a = max(a, sp * 0.6);
        }
        if (a <= 0.003) {
            discard;
        }
        fragColor = vec4(col, a) * ColorModulator;
        return;
    }

    float e = min(min(p.x, p.y), min(size.x - 1.0 - p.x, size.y - 1.0 - p.y));
    bool topLeft = min(p.x, p.y) <= min(size.x - 1.0 - p.x, size.y - 1.0 - p.y);

    // the border, outermost line first; the line pattern grows with the rarity
    float border = rarity >= 5.0 ? 5.0 : (rarity >= 4.0 ? 4.0 : (rarity >= 3.0 ? 3.0 : 2.0));
    vec3 col;
    bool onBorder = true;
    if (e < 0.5) {
        col = c * 0.22;
    } else if (rarity < 2.0) {
        col = e < 1.5 ? tone(c, 0.45) : vec3(-1.0);
    } else if (rarity < 3.0) {
        col = e < 1.5 ? c : vec3(-1.0);
    } else if (rarity < 4.0) {
        col = e < 1.5 ? tone(c, 0.8) : (e < 2.5 ? c : vec3(-1.0));
    } else if (rarity < 5.0) {
        col = e < 1.5 ? c : (e < 2.5 ? c * 0.3 : (e < 3.5 ? tone(c, 0.75) : vec3(-1.0)));
    } else {
        // mythic: a bright line, the colour, a dark gap, then a dashed inner line
        float dash = mod(floor(s / 3.0), 2.0);
        col = e < 1.5 ? tone(c, 0.85) : (e < 2.5 ? c : (e < 3.5 ? c * 0.25 : (e < 4.5 ? tone(c, dash > 0.5 ? 0.7 : 0.4) : vec3(-1.0))));
    }
    if (col.x < 0.0) {
        onBorder = false;
        // the bevel just inside the border, then the plate faintly tinted by the rarity
        vec3 plate = mix(mix(PLATE, NAVY_PLATE, FrameStyle.w), c * 0.35, 0.05 * rarity) * (1.04 - 0.12 * p.y / size.y);
        if (e < border + 0.5) {
            col = topLeft ? mix(plate, tone(c, 0.55), 0.35) : plate * 0.6;
        } else {
            col = plate;
        }
    }
    if (onBorder && e > 0.5) {
        col += vec3(1.0) * shimmer(s, around, time, rarity) * shimmerAmount;
        if (rarity >= 3.0) {
            col *= 0.9 + 0.1 * pulse;
        }
        if (rarity >= 4.0 && e < 1.5) {
            col = mix(col, vec3(1.0), sparkle(s, time, seed, rarity));
        }
    }
    fragColor = vec4(col, 1.0) * ColorModulator;
}
