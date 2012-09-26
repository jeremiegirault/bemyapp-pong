#ifdef GL_ES
precision highp float;
#endif

uniform sampler2D u_tex;

varying float v_nDotL;
varying vec2 v_texCoord0;

void main() {
    vec4 color = texture2D(u_tex, v_texCoord0);
    color += vec4(0.1, 0.1, 0.1, 1);
    gl_FragColor = vec4(color.rgb * v_nDotL, 1.0);
}