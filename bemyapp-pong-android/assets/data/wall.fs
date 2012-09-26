#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_tex;
uniform vec3 u_lightDir;
uniform float u_halfWallHeight;
uniform vec3 u_pointLightPos;

varying float v_nDotL;
varying vec4 v_position;
varying vec2 v_texCoord0;

void main() {
    float d = length(u_pointLightPos-v_position.xyz);
    float attenuation = 1.0/(.1 + .1*d+.2*d*d);
    vec4 pointLight = attenuation*vec4(0.2,0.2,0.2,1.0);
    
    // must match wall height in pong
    float dirLight = (v_nDotL+0.5);
    vec4 col = pointLight+dirLight*texture2D(u_tex, v_texCoord0);
    
    gl_FragColor = vec4(col.rgb, ((v_position.z+u_halfWallHeight) / (2.0*u_halfWallHeight)));
}