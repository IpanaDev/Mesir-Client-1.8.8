#version 430 compatibility

attribute vec4 Position;

varying vec2 texCoord;

void main(){
    vec4 outPos = gl_ModelViewProjectionMatrix * gl_Vertex * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    texCoord = Position.xy;
    texCoord.y = 1.0 - texCoord.y;
}
