#version 150

// GodNodeEffects.tone on the GPU, then vanilla position_color_tex: the summed light of a god node effect canvas,
// soft-clipped, banded into tone steps and coloured through the god's ramp row. Keep in step with tone().

uniform sampler2D Sampler0; // canvas light, RG float: red = behind the star, green = over it
uniform sampler2D Sampler1; // ramps, 256 x gods, RGBA8, one row per god

uniform vec4 ColorModulator;
uniform vec4 Tone;     // gain, tone floor, tone steps (0 = smooth), channel (0 = behind, 1 = over)
uniform float RampRow; // god ordinal

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    ivec2 size = textureSize(Sampler0, 0);
    ivec2 texel = clamp(ivec2(floor(texCoord0 * vec2(size))), ivec2(0), size - 1);
    vec2 light = texelFetch(Sampler0, texel, 0).rg;
    float value = Tone.w < 0.5 ? light.r : light.g;
    if (value <= 0.0) {
        discard;
    }
    float a = 1.0 - exp(-value * Tone.x);
    if (Tone.z > 0.0) {
        a = floor(a * Tone.z + 0.5) / Tone.z;
    }
    if (a <= 0.0) {
        discard;
    }
    int index = min(255, int(floor(Tone.y + (255.0 - Tone.y) * a)));
    vec3 rgb = texelFetch(Sampler1, ivec2(index, int(RampRow + 0.5)), 0).rgb;
    float alpha = floor(min(1.0, a) * 255.0 + 0.5) / 255.0;
    vec4 color = vec4(rgb, alpha) * vertexColor;
    if (color.a < 0.1) {
        discard;
    }
    fragColor = color * ColorModulator;
}
