#version 430 compatibility

uniform sampler2D textureIn;

void main() {
    vec2 v_texCoord0 = gl_TexCoord[0].st;
    vec2 numTiles = floor(v_texCoord0);
    vec2 tilingTexCoords = v_texCoord0;
    if (numTiles.xy != vec2(0,0)) {
        tilingTexCoords = (v_texCoord0 - numTiles);
        vec2 flooredTexCoords = floor((v_texCoord0 - numTiles) * 16) / 16;
        numTiles = numTiles + vec2(1,1);

        tilingTexCoords = flooredTexCoords + mod(((tilingTexCoords - flooredTexCoords) * numTiles) * 16, 1) / 16;
    }
    vec4 texture = texture(textureIn, tilingTexCoords);
    gl_FragColor = texture;
}