#version 430 compatibility

uniform sampler2D textureIn;

void main() {
    vec4 texture = texture2D(textureIn, gl_TexCoord[0].st);
    float average = (texture.r + texture.g + texture.b) / 3.0;
    gl_FragColor = vec4(average,average,average,texture.a);
}