attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;

uniform mat4 u_view;
uniform mat4 u_world;
uniform mat4 u_proj;
uniform mat4 u_normalMat;
uniform vec3 u_lightDir;

varying float v_nDotL;
varying vec4 v_position;
varying vec2 v_texCoord0;

void main() {
	v_position = a_position;
    vec4 v_normal = u_normalMat*vec4(a_normal, 1.0);
    v_nDotL = max(dot(v_normal.xyz, u_lightDir), 0.0);
    
    v_texCoord0 = vec2(a_texCoord0.s, a_texCoord0.t*v_position.y);
    gl_Position = u_proj*u_view*u_world*a_position;
}