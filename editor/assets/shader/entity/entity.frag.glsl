#ifdef GL_ES
precision mediump float;
#endif

const vec4 COLOR_TURQUOISE = vec4(0,0.714,0.586, 1.0);
const vec3 LIGHT_COLOR = vec3(1,1,1);

varying vec2 v_texCoord0;
varying vec3 v_vectorToLight;
varying vec3 v_surfaceNormal;

uniform sampler2D u_texture;

uniform float u_lightIntensity;

void main(void) {
    vec3 unitNormal = normalize(v_surfaceNormal);
    vec3 unitLightVector = normalize(v_vectorToLight);
    float nDot1 = u_lightIntensity * dot(unitNormal, unitLightVector);
    // diffuse
    float brightness = u_lightIntensity * max(nDot1, 0.2);

    vec3 light =  LIGHT_COLOR;
    vec4 color =  brightness * texture2D(u_texture, v_texCoord0);
    //vec4 color = COLOR_TURQUOISE * brightness;
    gl_FragColor = color;
}
