#version 330 core
layout (location = 0) in vec2 position;
layout (location = 1) in vec2 in_texture;
layout (location = 2) in vec3 in_color;

uniform vec2 dimensions;
// In the range from 0.0-1.0
uniform float left_margin;
uniform float right_margin;
uniform float top_margin;
uniform float bottom_margin;

out vec2 texture_coord;
out vec3 color;

void main()
{
    texture_coord = in_texture;
    color = in_color;
    float x = position.x / dimensions.x * (1.0 - left_margin - right_margin) + left_margin;
    float y = position.y / dimensions.y * (1.0 - top_margin - bottom_margin) - top_margin;
    gl_Position = vec4(x * 2 - 1, y * 2 + 1, 0.0, 1.0);
}
