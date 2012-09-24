#ifdef GL_ES
precision mediump float;
#endif


uniform vec3 u_lightDir;
uniform float u_halfWallHeight;

varying vec4 v_normal;
varying vec4 v_position;

void main() {
    float nDotL = dot(v_normal.xyz, u_lightDir);
    
    // must match wall height in pong
    float col = nDotL+0.3;
    
    gl_FragColor = vec4(col, col, col, ((v_position.z+u_halfWallHeight) / (2.0*u_halfWallHeight)));
}