#version 150

// One light primitive of a god node effect canvas, as a quad in canvas pixels (y down, row y lands on texture row y).

in vec3 Position;
in vec4 Seg;  // start relative to the integer origin (x, y), then end minus start (x, y)
in vec4 Val;  // value at start, value at end, width at start, width at end
in vec4 Kind; // kind * 2 + channel, stamp count, integer origin (x, y)

uniform vec2 CanvasSize;

flat out vec4 seg;
flat out vec4 val;
flat out vec4 kind;

void main() {
    gl_Position = vec4(Position.xy / CanvasSize * 2.0 - 1.0, 0.0, 1.0);
    seg = Seg;
    val = Val;
    kind = Kind;
}
