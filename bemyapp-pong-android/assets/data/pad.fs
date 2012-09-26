#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_tex;
uniform vec3 u_pointLightPos;
uniform vec3 u_pointLightCol;

varying vec4 v_position;
varying float v_nDotL;
varying vec2 v_texCoord0;

void main() {
    float d = length(u_pointLightPos-v_position.xyz);
    float attenuation = 1.0/(.5 + .7*d+.9*d*d);
    vec4 pointLight = attenuation*vec4(u_pointLightCol.rgb,1.0);
    
    float dirLight = (v_nDotL+0.5);
    vec4 col = pointLight+dirLight*texture2D(u_tex, v_texCoord0);
    
    gl_FragColor = vec4(col.rgb, v_position.z+0.5);
}