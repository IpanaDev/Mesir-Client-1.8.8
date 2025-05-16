#version 430 compatibility

varying vec4 passColour;
varying vec3 passNormal;
uniform sampler2D textureIn;

uniform vec3 size;

void main() {
    float Pi = 6.28318530718;
    float Directions = 12.0;
    float Quality = 6.0;
    vec2 radius = size.z/size.xy;
    vec4 Color = texture2D(textureIn, gl_TexCoord[0].st);
     for( float d=0.0; d<Pi; d+=Pi/Directions)
    {
        for(float i=1.0/Quality; i<=1.0; i+=1.0/Quality)
        {
            Color += texture2D(textureIn, gl_TexCoord[0].st+vec2(cos(d),sin(d))*radius*i);
        }
    }

    Color /= Quality * Directions;
    gl_FragColor =  Color * passColour;
}