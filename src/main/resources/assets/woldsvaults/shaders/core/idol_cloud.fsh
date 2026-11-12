#version 150

in vec2 texCoord0;

out vec4 fragColor;

// The smoke around the transfer slots on a God's Mastery idol: the painted cloud (Sampler0), kept clear of the slot
// openings and moved by breathing, swirling and warping, with flicker and rising wisps, coloured through the god's
// row of the ramps (Sampler1). The God Idol Cloud tuning page runs this same body in WebGL; keep the two in step.
// Positions and distances are pixels of the 256 px idol the cloud was painted on.

uniform sampler2D Sampler0; // painted cloud, 256 px: rgb = tone x alpha, a = alpha
uniform sampler2D Sampler1; // ramps, 256 x 4, one row per god

uniform vec4 ColorModulator;
uniform vec4 CloudTime;   // seconds, ramp row, texture size (0 draws it smooth), unused
uniform vec4 Opening0;    // slot centre x, y, radius, 1 when the opening exists
uniform vec4 Opening1;
uniform vec4 Opening2;
uniform vec4 CloudShape;  // opacity, clearance (x the radius), clearance edge, unused
uniform vec4 Warp;        // strength, feature size, speed, octaves
uniform vec4 Swirl;       // turn in radians, rate, reach, unused
uniform vec4 Breathe;     // amount, rate, unused, unused
uniform vec4 Flicker;     // amount, patch size, speed, unused
uniform vec4 Wisps;       // amount, spread, size, rise
uniform vec4 CloudColour; // brightness, moving highlights, tone steps, opacity steps
uniform float WispThinness;

const float TAU = 6.28318530718;

float cloudHash(int ix, int iy, int seed) {
    uint h = uint(ix) * 374761393u + uint(iy) * 668265263u + uint(seed) * 362437u;
    h = (h ^ (h >> 13u)) * 1274126177u;
    h = h ^ (h >> 16u);
    return float(h & 0xFFFFu) / 65535.0;
}

float valueNoise(vec2 p, int seed) {
    vec2 i = floor(p);
    vec2 f = p - i;
    vec2 u = f * f * (3.0 - 2.0 * f);
    int ix = int(i.x);
    int iy = int(i.y);
    float a = cloudHash(ix, iy, seed);
    float b = cloudHash(ix + 1, iy, seed);
    float c = cloudHash(ix, iy + 1, seed);
    float d = cloudHash(ix + 1, iy + 1, seed);
    return a + (b - a) * u.x + (c - a) * u.y + (a - b - c + d) * u.x * u.y;
}

float fbm(vec2 p, int seed, int octaves) {
    float sum = 0.0;
    float amp = 1.0;
    float norm = 0.0;
    float scale = 1.0;
    for (int o = 0; o < 4; o++) {
        if (o >= octaves) {
            break;
        }
        sum += amp * valueNoise(p * scale, seed + o * 101);
        norm += amp;
        amp *= 0.5;
        scale *= 2.0;
    }
    return sum / norm;
}

vec4 opening(int i) {
    return i == 0 ? Opening0 : i == 1 ? Opening1 : Opening2;
}

void main() {
    float res = CloudTime.z;
    vec2 p = res > 0.0 ? (floor(texCoord0 * res) + 0.5) * 256.0 / res : texCoord0 * 256.0;
    float t = CloudTime.x;

    vec2 centre = vec2(0.0);
    float count = 0.0;
    for (int i = 0; i < 3; i++) {
        vec4 o = opening(i);
        centre += o.xy * o.w;
        count += o.w;
    }
    centre /= max(count, 1.0);

    // breathe: the whole cloud swells and settles about the slots' centre
    float breathe = 1.0 + Breathe.x * sin(TAU * Breathe.y * t);
    vec2 q = centre + (p - centre) / breathe;
    // swirl: the smoke near each slot twists back and forth, each slot a third of a cycle apart
    for (int i = 0; i < 3; i++) {
        vec4 o = opening(i);
        if (o.w > 0.0) {
            vec2 d = q - o.xy;
            float turn = Swirl.x * sin(TAU * Swirl.y * t + float(i) * 2.0944) * exp(-dot(d, d) / (Swirl.z * Swirl.z));
            if (turn != 0.0) {
                float cs = cos(turn);
                float sn = sin(turn);
                q = o.xy + vec2(d.x * cs - d.y * sn, d.x * sn + d.y * cs);
            }
        }
    }
    // warp: two drifting noise fields push where the cloud is read from
    if (Warp.x > 0.0) {
        float wt = t * Warp.z;
        float wx = fbm(vec2(q.x / Warp.y + wt, q.y / Warp.y), 11, int(Warp.w));
        float wy = fbm(vec2(q.x / Warp.y + 7.31, q.y / Warp.y - wt), 23, int(Warp.w));
        q += (vec2(wx, wy) * 2.0 - 1.0) * Warp.x;
    }

    vec4 painted = texture(Sampler0, q / 256.0);
    float a = painted.a;
    float tone = a > 0.0001 ? painted.r / a : 0.0;
    // flicker: patches thin and thicken; the same field moves the highlights
    float f = fbm(vec2(p.x / Flicker.y + t * Flicker.z * 0.7, p.y / Flicker.y - t * Flicker.z * 0.4), 37, 2);
    a *= clamp(1.0 - Flicker.x + 2.0 * Flicker.x * f, 0.0, 2.0);
    tone += CloudColour.y * (f - 0.5);
    a = min(a, 1.0);
    // wisps: strands rising through a copy of the cloud spread wider than the painting
    if (Wisps.x > 0.0) {
        float n = fbm(vec2(p.x / Wisps.z + sin(t * 0.3) * 0.5, p.y / Wisps.z + t * Wisps.w / Wisps.z), 51, 3);
        float reach = texture(Sampler0, (centre + (p - centre) / (1.0 + Wisps.y)) / 256.0).a;
        float wisp = min(1.0, max(0.0, (n - WispThinness) / (1.0 - WispThinness)) * reach * Wisps.x);
        float merged = a + wisp * (1.0 - a);
        if (merged > 0.0001) {
            tone = (tone * a + (0.3 + 0.4 * n) * wisp * (1.0 - a)) / merged;
        }
        a = merged;
    }

    // keep the slot openings clear
    float gap = 1.0e9;
    for (int i = 0; i < 3; i++) {
        vec4 o = opening(i);
        if (o.w > 0.0) {
            gap = min(gap, length(p - o.xy) - o.z * CloudShape.y);
        }
    }
    a *= CloudShape.z > 0.0 ? smoothstep(0.0, CloudShape.z, gap) : step(0.0, gap);
    a = clamp(a * CloudShape.x, 0.0, 1.0);
    if (CloudColour.w > 0.0) {
        a = floor(a * CloudColour.w + 0.5) / CloudColour.w;
    }
    tone = clamp(tone * CloudColour.x, 0.0, 1.0);
    if (CloudColour.z > 0.0) {
        tone = floor(tone * CloudColour.z + 0.5) / CloudColour.z;
    }
    if (a <= 0.0) {
        discard;
    }
    vec3 colour = texelFetch(Sampler1, ivec2(int(floor(tone * 255.0 + 0.5)), int(CloudTime.y)), 0).rgb;
    fragColor = vec4(colour, a) * ColorModulator;
}
