#version 430 compatibility

uniform sampler2D textureIn;


void main() {
    vec4 texture = texture2D(textureIn, gl_TexCoord[0].st);
    gl_FragColor = vec4(1.0 - texture.r, 1.0 - texture.g, 1.0 - texture.b, texture.a);
}