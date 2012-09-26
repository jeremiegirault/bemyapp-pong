attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;

uniform mat4 u_world;
uniform mat4 u_view;
uniform mat4 u_proj;
uniform vec3 u_lightDir;

uniform mat4 u_normalMat;

varying float v_nDotL;
varying vec2 v_texCoord0;

void main() {
    vec4 v_normal = u_normalMat*vec4(a_normal, 1.0);
    v_nDotL = max(dot(v_normal.xyz, u_lightDir), 0.0);
    v_texCoord0 = vec2(a_texCoord0.s, a_texCoord0.t);
    gl_Position = u_proj*u_view*u_world*a_position;
}