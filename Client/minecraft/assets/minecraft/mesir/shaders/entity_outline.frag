#version 430 compatibility

uniform sampler2D textureIn;

varying vec2 texCoord;

void main(){
    vec4 center = texture2D(textureIn, texCoord);
    vec4 left = texture2D(textureIn, texCoord - vec2(1.0, 0.0));
    vec4 right = texture2D(textureIn, texCoord + vec2(1.0, 0.0));
    vec4 up = texture2D(textureIn, texCoord - vec2(0.0, 1.0));
    vec4 down = texture2D(textureIn, texCoord + vec2(0.0, 1.0));
    float leftDiff  = abs(center.a - left.a);
    float rightDiff = abs(center.a - right.a);
    float upDiff    = abs(center.a - up.a);
    float downDiff  = abs(center.a - down.a);
    float total = clamp(leftDiff + rightDiff + upDiff + downDiff, 0.0, 1.0);
    vec3 outColor = center.rgb * center.a + left.rgb * left.a + right.rgb * right.a + up.rgb * up.a + down.rgb * down.a;
    gl_FragColor = vec4(outColor * 0.2, total);
}