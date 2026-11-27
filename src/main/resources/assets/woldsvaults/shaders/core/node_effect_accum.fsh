#version 150

// Sums one light primitive into the god node effect canvas (blend ONE, ONE into an RG float target: red = behind the
// star, green = over it). Mirrors GodNodeEffects.Canvas: dot (add / stamp), stroke and glow. Positions are relative to
// the primitive's integer origin, so a pixel centre is an exact half-integer here as it is on the CPU.

flat in vec4 seg;
flat in vec4 val;
flat in vec4 kind;

out vec4 fragColor;

// Canvas.stamp: at width <= 1.25 only the pixel holding c, else every pixel whose centre lies within width / 2.
float stamp(vec2 p, vec2 c, float w, float v) {
    if (w <= 1.25) {
        return floor(c) == floor(p) ? v : 0.0;
    }
    float r = w / 2.0;
    vec2 d = p - c;
    return d.x * d.x + d.y * d.y <= r * r ? v : 0.0;
}

void main() {
    vec2 p = gl_FragCoord.xy - kind.zw;
    int code = int(kind.x + 0.5);
    int type = code / 2;
    float sum = 0.0;
    if (type == 0) {
        sum = stamp(p, seg.xy, val.z, val.x);
    } else if (type == 2) {
        float d = length(p - seg.xy);
        if (d < val.z) {
            float f = 1.0 - d / val.z;
            sum = val.x * f * f;
        }
    } else {
        // Canvas.stroke: steps + 1 stamps at t = i / steps; only those near this pixel's projection can reach it
        int steps = int(kind.y + 0.5);
        vec2 dir = seg.zw;
        float len2 = dot(dir, dir);
        int lo = 0;
        int hi = steps;
        if (len2 > 1.0e-10) {
            float tp = dot(p - seg.xy, dir) / len2;
            float reach = (max(max(val.z, val.w) * 0.5, 1.5) + 1.0) / sqrt(len2);
            lo = max(0, int(floor((tp - reach) * float(steps))) - 1);
            hi = min(steps, int(ceil((tp + reach) * float(steps))) + 1);
        }
        for (int i = lo; i <= hi; i++) {
            float t = float(i) / float(steps);
            float w = val.z + (val.w - val.z) * t;
            float v = (val.x + (val.y - val.x) * t) * 0.5 / max(1.0, w);
            sum += stamp(p, seg.xy + dir * t, w, v);
        }
    }
    if (sum == 0.0) {
        discard;
    }
    fragColor = (code & 1) == 0 ? vec4(sum, 0.0, 0.0, 0.0) : vec4(0.0, sum, 0.0, 0.0);
}
