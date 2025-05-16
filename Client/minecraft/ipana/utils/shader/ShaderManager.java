package ipana.utils.shader;

import java.io.*;
import java.nio.FloatBuffer;
import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.resources.IResource;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL43;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import net.minecraft.util.ResourceLocation;

import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.glGetProgrami;

public class ShaderManager {
    private final ResourceLocation shaderLocation = new ResourceLocation("mesir/shaders");
    private final HashMap<String, Shader> shaderMap = new HashMap<>();
    private static final ShaderManager INSTANCE = new ShaderManager();
    public long initMS = System.currentTimeMillis();

    public static ShaderManager getInstance() { return INSTANCE; }

    public static class Shader {
        public final int program;
        public int prevProgram;

        public Shader(final int program) {
            this.program = program;
        }
    }

    public int getShader(final String name) {
        if (!shaderMap.containsKey(name)) {
            reloadShader(name);
        }
        return shaderMap.get(name).program;
    }

    public int loadShader(final String name) {
        try {
            if (!shaderMap.containsKey(name)) {
                reloadShader(name);
            }
            shaderMap.get(name).prevProgram = glGetInteger(GL20.GL_CURRENT_PROGRAM);
            GL20.glUseProgram(shaderMap.get(name).program);
        }
        catch (final Exception e) {
            e.printStackTrace();
        }
        return shaderMap.get(name).program;
    }

    public int getDataLocation(String name, String data) {
        return GL20.glGetUniformLocation(shaderMap.get(name).program, data);
    }

    public void loadData(final String name, final String data, final Object value) {
        final var location = getDataLocation(name, data);
        switch (value) {
            case Integer i -> GL20.glUniform1i(location, i);
            case Float f -> GL20.glUniform1f(location, f);
            case Vector2f vec -> GL20.glUniform2f(location, vec.x, vec.y);
            case Vector3f vec -> GL20.glUniform3f(location, vec.x, vec.y, vec.z);
            case Vector4f vec -> GL20.glUniform4f(location, vec.x, vec.y, vec.z, vec.w);
            case Matrix4f matrix -> {
                var buffer = GLAllocation.createDirectFloatBuffer(16);
                var floats = new float[] {
                        matrix.m00, matrix.m01, matrix.m02, matrix.m03,
                        matrix.m10, matrix.m11, matrix.m12, matrix.m13,
                        matrix.m20, matrix.m21, matrix.m22, matrix.m23,
                        matrix.m30, matrix.m31, matrix.m32, matrix.m33
                };
                for (var element : floats)
                    buffer.put(element);
                GL20.glUniformMatrix4(location, false, buffer);
            }
            default -> throw new UnsupportedOperationException("Failed to load data into shader: Unsupported data type.");
        }
    }

    public void reloadShader(final String name) {
        try {
            var vertex = -1;
            final var sourceVert = getShaderSource(name, GL20.GL_VERTEX_SHADER);
            if (!sourceVert.isEmpty()) {
                vertex = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
                GL20.glShaderSource(vertex, sourceVert);
                GL20.glCompileShader(vertex);
                System.err.println(GL20.glGetShaderInfoLog(vertex, 100));
            }
            var fragment = -1;
            final var sourceFrag = getShaderSource(name, GL20.GL_FRAGMENT_SHADER);
            if (!sourceFrag.isEmpty()) {
                fragment = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
                GL20.glShaderSource(fragment, sourceFrag);
                GL20.glCompileShader(fragment);
                System.err.println(GL20.glGetShaderInfoLog(fragment, 100));
            }
            var compute = -1;
            final var sourceCompute = getShaderSource(name, GL43.GL_COMPUTE_SHADER);
            if (!sourceCompute.isEmpty()) {
                compute = GL20.glCreateShader(GL43.GL_COMPUTE_SHADER);
                GL20.glShaderSource(compute, sourceCompute);
                GL20.glCompileShader(compute);
                System.err.println(GL20.glGetShaderInfoLog(compute, 100));
            }
            final var program = GL20.glCreateProgram();
            if (vertex != -1) {
                GL20.glAttachShader(program, vertex);
            }
            if (fragment != -1) {
                GL20.glAttachShader(program, fragment);
            }
            if (compute != -1) {
                GL20.glAttachShader(program, compute);
            }
            GL20.glLinkProgram(program);
            final var linked = glGetProgrami(program, GL_LINK_STATUS);
            if (vertex != -1) {
                GL20.glDeleteShader(vertex);
            }
            if (fragment != -1) {
                GL20.glDeleteShader(fragment);
            }
            if (compute != -1) {
                GL20.glDeleteShader(compute);
            }
            GL20.glValidateProgram(program);
            shaderMap.put(name, new Shader(program));
            System.err.println("put :DDDD");
        }
        catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void stop(String shaderName) {
        GL20.glUseProgram(shaderMap.get(shaderName).prevProgram);
    }

    public String getShaderSource(final String name, final int type) {
        var ext = switch (type) {
            case GL20.GL_VERTEX_SHADER -> ".vert";
            case GL20.GL_FRAGMENT_SHADER -> ".frag";
            case GL43.GL_COMPUTE_SHADER -> ".compute";
            default -> null;
        };
        if (ext == null) return "";
        final var location = new ResourceLocation(shaderLocation.getResourceDomain(), String.format("%s/%s%s", shaderLocation.getResourcePath(), name, ext));
        //try (var is = new FileInputStream("F:\\antin guntin seyler\\Ipana\\src\\minecraft\\assets\\minecraft\\mesir\\shaders\\"+name+ext)) {
        try (var is = Minecraft.getMinecraft().getTextureManager().theResourceManager.getResource(location).getInputStream()) {
            final var source = new StringBuilder();
            final var br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                source.append(line).append("\n");
            }
            return source.toString();
        }
        catch (final IOException e) {}
        return "";
    }
}
