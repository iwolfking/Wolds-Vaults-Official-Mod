#version 150

// vanilla position_tex with a colour grade on top, for the god tree backdrop

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float Brightness; // added after contrast, 0 = unchanged
uniform float Contrast;   // scale around mid grey, 1 = unchanged
uniform float Gamma;      // 1 = unchanged, above 1 lifts the midtones
uniform float Vibrance;   // 0 = unchanged, boosts dull colours more than vivid ones

in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a == 0.0) {
        discard;
    }
    color *= ColorModulator;
    vec3 c = pow(color.rgb, vec3(1.0 / Gamma));
    c = (c - 0.5) * Contrast + 0.5 + Brightness;
    float luma = dot(c, vec3(0.2126, 0.7152, 0.0722));
    float saturation = max(c.r, max(c.g, c.b)) - min(c.r, min(c.g, c.b));
    c = mix(vec3(luma), c, 1.0 + Vibrance * (1.0 - saturation));
    fragColor = vec4(clamp(c, 0.0, 1.0), color.a);
}
