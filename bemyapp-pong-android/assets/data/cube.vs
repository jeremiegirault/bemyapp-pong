attribute vec4 a_position;
attribute vec3 a_normal;

uniform mat4 u_world;
uniform mat4 u_view;
uniform mat4 u_proj;

uniform mat4 u_normalMat;

varying vec4 v_normal;

void main() {
    v_normal = u_normalMat*vec4(a_normal, 0.0);
    gl_Position = u_proj*u_view*u_world*a_position;
}