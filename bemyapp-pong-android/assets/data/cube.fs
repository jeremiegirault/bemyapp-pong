#ifdef GL_ES
precision mediump float;
#endif

uniform vec3 u_lightDir;
uniform sampler2D u_tex;

varying vec4 v_normal;

void main() {
    float nDotL = dot(v_normal.xyz, u_lightDir);
    gl_FragColor = vec4(nDotL,nDotL,nDotL, 1.0);
}