package net.minecraft.client.renderer;

import ipana.utils.render.BufferBridge;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import optifine.Config;
import optifine.TextureUtils;
import org.lwjgl.MemoryUtil;
import org.lwjgl.opengl.GL11;
import shadersmod.client.SVertexBuilder;

import java.awt.*;
import java.nio.*;
import java.util.Arrays;
import java.util.BitSet;

public class WorldRenderer {
    private ByteBuffer byteBuffer;
    public IntBuffer rawIntBuffer;
    private ShortBuffer rawShortBuffer;
    public FloatBuffer rawFloatBuffer;
    public int vertexCount;
    private VertexFormatElement field_181677_f;
    private int field_181678_g;

    /**
     * Boolean for whether this renderer needs to be updated or not
     */
    private boolean needsUpdate;
    public int drawMode;
    public double xOffset;
    public double yOffset;
    public double zOffset;
    private VertexFormat vertexFormat;
    private boolean isDrawing;

    private EnumWorldBlockLayer blockLayer = null;
    private boolean[] drawnIcons = new boolean[256];
    private TextureAtlasSprite[] quadSprites = null;
    private TextureAtlasSprite[] quadSpritesPrev = null;
    private TextureAtlasSprite quadSprite = null;
    public SVertexBuilder sVertexBuilder;
    public boolean glMode;
    public long bufferPointer;
    private BufferBridge bridge;

    public WorldRenderer(int bufferSizeIn) {
        if (Config.isShaders()) {
            bufferSizeIn *= 2;
        }

        this.byteBuffer = GLAllocation.createDirectByteBuffer(bufferSizeIn * 4);
        this.rawIntBuffer = this.byteBuffer.asIntBuffer();
        this.rawShortBuffer = this.byteBuffer.asShortBuffer();
        this.rawFloatBuffer = this.byteBuffer.asFloatBuffer();
        this.bridge = new BufferBridge(this);
        SVertexBuilder.initVertexBuilder(this);
        setPointer();
    }

    private void func_181670_b(int p_181670_1_) {
        if (Config.isShaders()) {
            p_181670_1_ *= 2;
        }

        if (p_181670_1_ > this.rawIntBuffer.remaining()) {
            int i = this.byteBuffer.capacity();
            int j = i % 2097152;
            //int k = j + (((this.rawIntBuffer.position() + p_181670_1_) * 4 - j) / 2097152 + 1) * 2097152;
            int k = j + ((this.rawIntBuffer.position() + p_181670_1_) * 4 - j) + 2097152;
            //LogManager.getLogger().warn("Needed to grow BufferBuilder buffer: Old size " + i + " bytes, new size " + k + " bytes.");
            System.out.println("Needed to grow BufferBuilder buffer: Old size " + i + " bytes, new size " + k + " bytes.");
            int l = this.rawIntBuffer.position();
            ByteBuffer bytebuffer = GLAllocation.createDirectByteBuffer(k);
            this.byteBuffer.position(0);
            bytebuffer.put(this.byteBuffer);
            bytebuffer.rewind();
            this.byteBuffer = bytebuffer;
            this.rawFloatBuffer = this.byteBuffer.asFloatBuffer();
            this.rawIntBuffer = this.byteBuffer.asIntBuffer();
            this.rawIntBuffer.position(l);
            this.rawShortBuffer = this.byteBuffer.asShortBuffer();
            this.rawShortBuffer.position(l << 1);
            setPointer();
            if (this.quadSprites != null) {
                TextureAtlasSprite[] atextureatlassprite = this.quadSprites;
                int i1 = this.getBufferQuadSize();
                this.quadSprites = new TextureAtlasSprite[i1];
                System.arraycopy(atextureatlassprite, 0, this.quadSprites, 0, Math.min(atextureatlassprite.length, this.quadSprites.length));
                this.quadSpritesPrev = null;
            }
        }
    }

    public void sortVertexData(float x, float y, float z) {
        int i = this.vertexCount / 4;
        float[] afloat = new float[i];

        for (int j = 0; j < i; ++j) {
            afloat[j] = func_181665_a(this.rawFloatBuffer, (float) ((double) x + this.xOffset), (float) ((double) y + this.yOffset), (float) ((double) z + this.zOffset), this.vertexFormat.getIntegerSize(), j * this.vertexFormat.getNextOffset());
        }

        Integer[] ainteger = new Integer[i];

        for (int k = 0; k < ainteger.length; ++k) {
            ainteger[k] = k;
        }

        Arrays.sort(ainteger, new WorldRenderer$1(this, afloat));
        BitSet bitset = new BitSet();
        int l = this.vertexFormat.getNextOffset();
        int[] aint = new int[l];

        for (int l1 = 0; (l1 = bitset.nextClearBit(l1)) < ainteger.length; ++l1) {
            int i1 = ainteger[l1];

            if (i1 != l1) {
                this.rawIntBuffer.limit(i1 * l + l);
                this.rawIntBuffer.position(i1 * l);
                this.rawIntBuffer.get(aint);
                int j1 = i1;

                for (int k1 = ainteger[i1]; j1 != l1; k1 = ainteger[k1]) {
                    this.rawIntBuffer.limit(k1 * l + l);
                    this.rawIntBuffer.position(k1 * l);
                    IntBuffer intbuffer = this.rawIntBuffer.slice();
                    this.rawIntBuffer.limit(j1 * l + l);
                    this.rawIntBuffer.position(j1 * l);
                    this.rawIntBuffer.put(intbuffer);
                    bitset.set(j1);
                    j1 = k1;
                }

                this.rawIntBuffer.limit(l1 * l + l);
                this.rawIntBuffer.position(l1 * l);
                this.rawIntBuffer.put(aint);
            }

            bitset.set(l1);
        }

        this.rawIntBuffer.limit(this.rawIntBuffer.capacity());
        this.rawIntBuffer.position(this.func_181664_j());

        if (this.quadSprites != null) {
            TextureAtlasSprite[] atextureatlassprite = new TextureAtlasSprite[this.vertexCount / 4];

            for (int j2 = 0; j2 < ainteger.length; ++j2) {
                int k2 = ainteger[j2];
                atextureatlassprite[j2] = this.quadSprites[k2];
            }

            System.arraycopy(atextureatlassprite, 0, this.quadSprites, 0, atextureatlassprite.length);
        }

    }

    public WorldRenderer.State getVertexData() {

        this.rawIntBuffer.rewind();
        int i = this.func_181664_j();
        this.rawIntBuffer.limit(i);
        int[] aint = new int[i];
        this.rawIntBuffer.get(aint);
        this.rawIntBuffer.limit(this.rawIntBuffer.capacity());
        this.rawIntBuffer.position(i);
        TextureAtlasSprite[] atextureatlassprite = null;

        if (this.quadSprites != null) {
            int j = this.vertexCount / 4;
            atextureatlassprite = new TextureAtlasSprite[j];
            System.arraycopy(this.quadSprites, 0, atextureatlassprite, 0, j);
        }

        return new State(aint, new VertexFormat(this.vertexFormat), atextureatlassprite);
    }

    public int func_181664_j() {
        return this.vertexCount * this.vertexFormat.getIntegerSize();
    }

    private static float func_181665_a(FloatBuffer buffer, float x, float y, float z, int p_181665_4_, int p_181665_5_) {
        float f = buffer.get(p_181665_5_);
        float f1 = buffer.get(p_181665_5_ + 1);
        float f2 = buffer.get(p_181665_5_ + 2);
        float f3 = buffer.get(p_181665_5_ + p_181665_4_);
        float f4 = buffer.get(p_181665_5_ + p_181665_4_ + 1);
        float f5 = buffer.get(p_181665_5_ + p_181665_4_ + 2);
        float f6 = buffer.get(p_181665_5_ + p_181665_4_ * 2);
        float f7 = buffer.get(p_181665_5_ + p_181665_4_ * 2 + 1);
        float f8 = buffer.get(p_181665_5_ + p_181665_4_ * 2 + 2);
        float f9 = buffer.get(p_181665_5_ + p_181665_4_ * 3);
        float f10 = buffer.get(p_181665_5_ + p_181665_4_ * 3 + 1);
        float f11 = buffer.get(p_181665_5_ + p_181665_4_ * 3 + 2);
        float f12 = (f + f3 + f6 + f9) * 0.25F - x;
        float f13 = (f1 + f4 + f7 + f10) * 0.25F - y;
        float f14 = (f2 + f5 + f8 + f11) * 0.25F - z;
        return f12 * f12 + f13 * f13 + f14 * f14;
    }

    public void setVertexState(WorldRenderer.State state) {

        this.rawIntBuffer.clear();
        this.func_181670_b(state.getRawBuffer().length);
        this.rawIntBuffer.put(state.getRawBuffer());
        this.vertexCount = state.getVertexCount();
        this.vertexFormat = new VertexFormat(state.getVertexFormat());

        if (state.stateQuadSprites != null) {
            if (this.quadSprites == null) {
                this.quadSprites = this.quadSpritesPrev;
            }

            if (this.quadSprites == null || this.quadSprites.length < this.getBufferQuadSize()) {
                this.quadSprites = new TextureAtlasSprite[this.getBufferQuadSize()];
            }

            TextureAtlasSprite[] atextureatlassprite = state.stateQuadSprites;
            System.arraycopy(atextureatlassprite, 0, this.quadSprites, 0, atextureatlassprite.length);
        } else {
            if (this.quadSprites != null) {
                this.quadSpritesPrev = this.quadSprites;
            }

            this.quadSprites = null;
        }
    }

    public void reset() {
        this.vertexCount = 0;
        this.field_181677_f = null;
        this.field_181678_g = 0;
        this.quadSprite = null;
    }

    public void begin(int drawMode, VertexFormat format) {
        if (this.isDrawing) {
            throw new IllegalStateException("Already building!");
        } else {
            if (glMode) {
                this.isDrawing = true;
                GL11.glBegin(drawMode);
            } else {
                this.isDrawing = true;
                this.reset();
                this.drawMode = drawMode;
                this.vertexFormat = format;
                this.field_181677_f = format.getElement(this.field_181678_g);
                this.needsUpdate = false;
                this.byteBuffer.limit(this.byteBuffer.capacity());

                if (Config.isShaders()) {
                    SVertexBuilder.endSetVertexFormat(this);
                }

                if (Config.isMultiTexture()) {
                    if (this.blockLayer != null) {
                        if (this.quadSprites == null) {
                            this.quadSprites = this.quadSpritesPrev;
                        }

                        if (this.quadSprites == null || this.quadSprites.length < this.getBufferQuadSize()) {
                            this.quadSprites = new TextureAtlasSprite[this.getBufferQuadSize()];
                        }
                    }
                } else {
                    if (this.quadSprites != null) {
                        this.quadSpritesPrev = this.quadSprites;
                    }

                    this.quadSprites = null;
                }
            }
        }
    }

    public WorldRenderer tex(double u, double v) {
        if (glMode) {
            GL11.glTexCoord2d(u, v);
            return this;
        }
        if (this.quadSprite != null && this.quadSprites != null) {
            u = this.quadSprite.toSingleU((float) u);
            v = this.quadSprite.toSingleV((float) v);
            this.quadSprites[this.vertexCount / 4] = this.quadSprite;
        }

        int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.field_181678_g);
        
        switch (WorldRenderer$2.field_181661_a[this.field_181677_f.getType().ordinal()]) {
            case 1 -> {
                this.bridge.putFloat(i, (float) u);
                this.bridge.putFloat(i + 4, (float) v);
            }
            case 2, 3 -> {
                this.bridge.putInt(i, (int) u);
                this.bridge.putInt(i + 4, (int) v);
            }
            case 4, 5 -> {
                this.bridge.putShort(i, (short) ((int) v));
                this.bridge.putShort(i + 2, (short) ((int) u));
            }
            case 6, 7 -> {
                this.bridge.put(i, (byte) ((int) v));
                this.bridge.put(i + 1, (byte) ((int) u));
            }
        }

        this.func_181667_k();
        return this;
    }

    public WorldRenderer tex(float u, float v) {
        if (glMode) {
            GL11.glTexCoord2f(u, v);
            return this;
        }
        if (this.quadSprite != null && this.quadSprites != null) {
            u = this.quadSprite.toSingleU(u);
            v = this.quadSprite.toSingleV(v);
            this.quadSprites[this.vertexCount / 4] = this.quadSprite;
        }

        int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.field_181678_g);
        
        switch (WorldRenderer$2.field_181661_a[this.field_181677_f.getType().ordinal()]) {
            case 1 -> {
                this.bridge.putFloat(i, u);
                this.bridge.putFloat(i + 4, v);
            }
            case 2, 3 -> {
                this.bridge.putInt(i, (int) u);
                this.bridge.putInt(i + 4, (int) v);
            }
            case 4, 5 -> {
                this.bridge.putShort(i, (short) ((int) v));
                this.bridge.putShort(i + 2, (short) ((int) u));
            }
            case 6, 7 -> {
                this.bridge.put(i, (byte) ((int) v));
                this.bridge.put(i + 1, (byte) ((int) u));
            }
        }

        this.func_181667_k();
        return this;
    }

    public WorldRenderer lightmap(int p_181671_1_, int p_181671_2_) {

        int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.field_181678_g);
        
        switch (WorldRenderer$2.field_181661_a[this.field_181677_f.getType().ordinal()]) {
            case 1 -> {
                this.bridge.putFloat(i, (float) p_181671_1_);
                this.bridge.putFloat(i + 4, (float) p_181671_2_);
            }
            case 2, 3 -> {
                this.bridge.putInt(i, p_181671_1_);
                this.bridge.putInt(i + 4, p_181671_2_);
            }
            case 4, 5 -> {
                this.bridge.putShort(i, (short) p_181671_2_);
                this.bridge.putShort(i + 2, (short) p_181671_1_);
            }
            case 6, 7 -> {
                this.bridge.put(i, (byte) p_181671_2_);
                this.bridge.put(i + 1, (byte) p_181671_1_);
            }
        }

        this.func_181667_k();
        return this;
    }

    public void putBrightness4(int p_178962_1_, int p_178962_2_, int p_178962_3_, int p_178962_4_) {
        int i = (this.vertexCount - 4) * this.vertexFormat.getIntegerSize() + this.vertexFormat.getUvOffsetById(1) / 4;
        int j = this.vertexFormat.getNextOffset() >> 2;
        this.rawIntBuffer.put(i, p_178962_1_);
        this.rawIntBuffer.put(i + j, p_178962_2_);
        this.rawIntBuffer.put(i + j * 2, p_178962_3_);
        this.rawIntBuffer.put(i + j * 3, p_178962_4_);
    }
    public void putPosition(double x, double y, double z) {
        int i = this.vertexFormat.getIntegerSize();
        int j = (this.vertexCount - 4) * i;

        for (int k = 0; k < 4; ++k) {
            int l = j + k * i;
            int i1 = l + 1;
            int j1 = i1 + 1;
            this.rawIntBuffer.put(l, Float.floatToRawIntBits((float) (x + this.xOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(l))));
            this.rawIntBuffer.put(i1, Float.floatToRawIntBits((float) (y + this.yOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(i1))));
            this.rawIntBuffer.put(j1, Float.floatToRawIntBits((float) (z + this.zOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(j1))));
        }
    }

    public boolean isDrawing() {
        return isDrawing;
    }

    /**
     * Takes in the pass the call list is being requested for. Args: renderPass
     */
    public int getColorIndex(int p_78909_1_) {
        return ((this.vertexCount - p_78909_1_) * this.vertexFormat.getNextOffset() + this.vertexFormat.getColorOffset()) / 4;
    }

    public void putColorMultiplier(float red, float green, float blue, int renderPass) {
        int i = this.getColorIndex(renderPass);
        int j = -1;

        if (!this.needsUpdate) {
            j = this.rawIntBuffer.get(i);

            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                int k = (int) ((float) (j & 255) * red);
                int l = (int) ((float) (j >> 8 & 255) * green);
                int i1 = (int) ((float) (j >> 16 & 255) * blue);
                j = j & -16777216;
                j = j | i1 << 16 | l << 8 | k;
            } else {
                int j1 = (int) ((float) (j >> 24 & 255) * red);
                int k1 = (int) ((float) (j >> 16 & 255) * green);
                int l1 = (int) ((float) (j >> 8 & 255) * blue);
                j = j & 255;
                j = j | j1 << 24 | k1 << 16 | l1 << 8;
            }
        }

        this.rawIntBuffer.put(i, j);
    }
    public void putColorMultiplierAlpha(float red, float green, float blue, float alpha, int renderPass) {
        int i = this.getColorIndex(renderPass);
        int j = -1;

        if (!this.needsUpdate) {
            j = this.rawIntBuffer.get(i);

            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                int k = (int) ((float) (j & 255) * red);
                int l = (int) ((float) (j >> 8 & 255) * green);
                int i1 = (int) ((float) (j >> 16 & 255) * blue);
                int he = (int) ((float) (j >> 24 & 255) * alpha);
                j = he << 24 | i1 << 16 | l << 8 | k;
            } else {
                int j1 = (int) ((float) (j >> 24 & 255) * red);
                int k1 = (int) ((float) (j >> 16 & 255) * green);
                int l1 = (int) ((float) (j >> 8 & 255) * blue);
                int m1 = (int) ((float) (j & 255) * alpha);
                j = j1 << 24 | k1 << 16 | l1 << 8 | m1;
            }
        }

        this.rawIntBuffer.put(i, j);
    }
    private void putColor(int argb, int p_178988_2_) {
        int i = this.getColorIndex(p_178988_2_);
        int j = argb >> 16 & 255;
        int k = argb >> 8 & 255;
        int l = argb & 255;
        int i1 = argb >> 24 & 255;
        this.putColorRGBA(i, j, k, l, i1);
    }

    public void putColorRGB_F(float red, float green, float blue, int p_178994_4_) {
        int i = this.getColorIndex(p_178994_4_);
        int j = MathHelper.clamp_int((int) (red * 255.0F), 0, 255);
        int k = MathHelper.clamp_int((int) (green * 255.0F), 0, 255);
        int l = MathHelper.clamp_int((int) (blue * 255.0F), 0, 255);
        this.putColorRGBA(i, j, k, l, 255);
    }

    public void putColorRGBA(int index, int red, int p_178972_3_, int p_178972_4_, int p_178972_5_) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            this.rawIntBuffer.put(index, p_178972_5_ << 24 | p_178972_4_ << 16 | p_178972_3_ << 8 | red);
        } else {
            this.rawIntBuffer.put(index, red << 24 | p_178972_3_ << 16 | p_178972_4_ << 8 | p_178972_5_);
        }
    }

    /**
     * Marks the current renderer data as dirty and needing to be updated.
     */
    public void markDirty() {
        this.needsUpdate = true;
    }

    public WorldRenderer color(Color color) {
        return this.color((color.getRed() / 255f), (color.getGreen() / 255f), (color.getBlue() / 255f), (color.getAlpha() / 255f));
    }
    public WorldRenderer color(int color) {
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
        float a = (float)(color >> 24 & 255) / 255.0F;
        return this.color(r, g, b, a);
    }

    public WorldRenderer color(float r, float g, float b, float a) {
        return this.color((int) (r * 255.0F), (int) (g * 255.0F), (int) (b * 255.0F), (int) (a * 255.0F));
    }

    public WorldRenderer color(int p_181669_1_, int p_181669_2_, int p_181669_3_, int p_181669_4_) {
        if (glMode) {
            GL11.glColor4f(p_181669_1_/255f, p_181669_2_/255f, p_181669_3_/255f, p_181669_4_/255f);
            return this;
        }
        if (!this.needsUpdate) {
            int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.field_181678_g);
            
            switch (WorldRenderer$2.field_181661_a[this.field_181677_f.getType().ordinal()]) {
                case 1:
                    this.bridge.putFloat(i, (float) p_181669_1_ / 255.0F);
                    this.bridge.putFloat(i + 4, (float) p_181669_2_ / 255.0F);
                    this.bridge.putFloat(i + 8, (float) p_181669_3_ / 255.0F);
                    this.bridge.putFloat(i + 12, (float) p_181669_4_ / 255.0F);
                    break;

                case 2:
                case 3:
                    this.bridge.putFloat(i, (float) p_181669_1_);
                    this.bridge.putFloat(i + 4, (float) p_181669_2_);
                    this.bridge.putFloat(i + 8, (float) p_181669_3_);
                    this.bridge.putFloat(i + 12, (float) p_181669_4_);
                    break;

                case 4:
                case 5:
                    this.bridge.putShort(i, (short) p_181669_1_);
                    this.bridge.putShort(i + 2, (short) p_181669_2_);
                    this.bridge.putShort(i + 4, (short) p_181669_3_);
                    this.bridge.putShort(i + 6, (short) p_181669_4_);
                    break;

                case 6:
                case 7:
                    if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                        this.bridge.put(i, (byte) p_181669_1_);
                        this.bridge.put(i + 1, (byte) p_181669_2_);
                        this.bridge.put(i + 2, (byte) p_181669_3_);
                        this.bridge.put(i + 3, (byte) p_181669_4_);
                    } else {
                        this.bridge.put(i, (byte) p_181669_4_);
                        this.bridge.put(i + 1, (byte) p_181669_3_);
                        this.bridge.put(i + 2, (byte) p_181669_2_);
                        this.bridge.put(i + 3, (byte) p_181669_1_);
                    }
            }

            this.func_181667_k();
        }
        return this;
    }

    public void addVertexData(int[] vertexData) {
        if (Config.isShaders()) {
            SVertexBuilder.beginAddVertexData(this, vertexData);
        }

        this.func_181670_b(vertexData.length);
        this.rawIntBuffer.position(this.func_181664_j());
        this.rawIntBuffer.put(vertexData);
        this.vertexCount += vertexData.length / this.vertexFormat.getIntegerSize();

        if (Config.isShaders()) {
            SVertexBuilder.endAddVertexData(this);
        }
    }

    public void endVertex() {
        ++this.vertexCount;
        this.func_181670_b(this.vertexFormat.getIntegerSize());
        this.field_181678_g = 0;
        this.field_181677_f = this.vertexFormat.getElement(this.field_181678_g);

        if (Config.isShaders()) {
            SVertexBuilder.endAddVertex(this);
        }
    }

    public WorldRenderer endVertexAndReturn() {
        ++this.vertexCount;
        this.func_181670_b(this.vertexFormat.getIntegerSize());
        this.field_181678_g = 0;
        this.field_181677_f = this.vertexFormat.getElement(this.field_181678_g);

        if (Config.isShaders()) {
            SVertexBuilder.endAddVertex(this);
        }
        return this;
    }

    public WorldRenderer pos(double x, double y, double z) {
        if (glMode) {
            GL11.glVertex3d(x, y, z);
            return this;
        }
        if (Config.isShaders()) {
            SVertexBuilder.beginAddVertex(this);
        }

        int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.field_181678_g);
        
        switch (WorldRenderer$2.field_181661_a[this.field_181677_f.getType().ordinal()]) {
            case 1 -> {
                this.bridge.putFloat(i, (float) (x + this.xOffset));
                this.bridge.putFloat(i + 4, (float) (y + this.yOffset));
                this.bridge.putFloat(i + 8, (float) (z + this.zOffset));
            }
            case 2, 3 -> {
                this.bridge.putInt(i, Float.floatToRawIntBits((float) (x + this.xOffset)));
                this.bridge.putInt(i + 4, Float.floatToRawIntBits((float) (y + this.yOffset)));
                this.bridge.putInt(i + 8, Float.floatToRawIntBits((float) (z + this.zOffset)));
            }
            case 4, 5 -> {
                this.bridge.putShort(i, (short) ((int) (x + this.xOffset)));
                this.bridge.putShort(i + 2, (short) ((int) (y + this.yOffset)));
                this.bridge.putShort(i + 4, (short) ((int) (z + this.zOffset)));
            }
            case 6, 7 -> {
                this.bridge.put(i, (byte) ((int) (x + this.xOffset)));
                this.bridge.put(i + 1, (byte) ((int) (y + this.yOffset)));
                this.bridge.put(i + 2, (byte) ((int) (z + this.zOffset)));
            }
        }

        this.func_181667_k();
        return this;
    }

    public WorldRenderer pos(float x, float y, float z) {
        if (glMode) {
            GL11.glVertex3f(x, y, z);
            return this;
        }
        if (Config.isShaders()) {
            SVertexBuilder.beginAddVertex(this);
        }

        int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.field_181678_g);
        
        switch (WorldRenderer$2.field_181661_a[this.field_181677_f.getType().ordinal()]) {
            case 1 -> {
                this.bridge.putFloat(i, (float) (x + this.xOffset));
                this.bridge.putFloat(i + 4, (float) (y + this.yOffset));
                this.bridge.putFloat(i + 8, (float) (z + this.zOffset));
            }
            case 2, 3 -> {
                this.bridge.putInt(i, Float.floatToRawIntBits((float) (x + this.xOffset)));
                this.bridge.putInt(i + 4, Float.floatToRawIntBits((float) (y + this.yOffset)));
                this.bridge.putInt(i + 8, Float.floatToRawIntBits((float) (z + this.zOffset)));
            }
            case 4, 5 -> {
                this.bridge.putShort(i, (short) ((int) (x + this.xOffset)));
                this.bridge.putShort(i + 2, (short) ((int) (y + this.yOffset)));
                this.bridge.putShort(i + 4, (short) ((int) (z + this.zOffset)));
            }
            case 6, 7 -> {
                this.bridge.put(i, (byte) ((int) (x + this.xOffset)));
                this.bridge.put(i + 1, (byte) ((int) (y + this.yOffset)));
                this.bridge.put(i + 2, (byte) ((int) (z + this.zOffset)));
            }
        }

        this.func_181667_k();
        return this;
    }

    public void putNormal(float x, float y, float z) {

        int i = (byte) ((int) (x * 127.0F)) & 255;
        int j = (byte) ((int) (y * 127.0F)) & 255;
        int k = (byte) ((int) (z * 127.0F)) & 255;
        int l = i | j << 8 | k << 16;
        int i1 = this.vertexFormat.getNextOffset() >> 2;
        int j1 = (this.vertexCount - 4) * i1 + this.vertexFormat.getNormalOffset() / 4;
        this.rawIntBuffer.put(j1, l);
        this.rawIntBuffer.put(j1 + i1, l);
        this.rawIntBuffer.put(j1 + i1 * 2, l);
        this.rawIntBuffer.put(j1 + i1 * 3, l);
    }

    public void func_181667_k() {
        ++this.field_181678_g;
        this.field_181678_g %= this.vertexFormat.getElementCount();
        this.field_181677_f = this.vertexFormat.getElement(this.field_181678_g);

        if (this.field_181677_f.getUsage() == VertexFormatElement.EnumUsage.PADDING) {
            this.func_181667_k();
        }
    }

    public WorldRenderer normal(float x, float y, float z) {
        if (glMode) {
            GL11.glNormal3f(x, y, z);
        }
        int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.field_181678_g);
        
        switch (WorldRenderer$2.field_181661_a[this.field_181677_f.getType().ordinal()]) {
            case 1 -> {
                this.bridge.putFloat(i, x);
                this.bridge.putFloat(i + 4, y);
                this.bridge.putFloat(i + 8, z);
            }
            case 2, 3 -> {
                this.bridge.putInt(i, (int) x);
                this.bridge.putInt(i + 4, (int) y);
                this.bridge.putInt(i + 8, (int) z);
            }
            case 4, 5 -> {
                this.bridge.putShort(i, (short) ((int) (x * 32767.0F) & 65535));
                this.bridge.putShort(i + 2, (short) ((int) (y * 32767.0F) & 65535));
                this.bridge.putShort(i + 4, (short) ((int) (z * 32767.0F) & 65535));
            }
            case 6, 7 -> {
                this.bridge.put(i, (byte) ((int) (x * 127.0F) & 255));
                this.bridge.put(i + 1, (byte) ((int) (y * 127.0F) & 255));
                this.bridge.put(i + 2, (byte) ((int) (z * 127.0F) & 255));
            }
        }

        this.func_181667_k();
        return this;
    }

    public void setTranslation(double x, double y, double z) {
        this.xOffset = x;
        this.yOffset = y;
        this.zOffset = z;
    }

    public void finishDrawing() {
        if (!this.isDrawing) {
            throw new IllegalStateException("Not building!");
        } else {
            if (glMode) {
                GL11.glEnd();
                this.isDrawing = false;
            } else {
                this.isDrawing = false;
                this.byteBuffer.position(0);
                this.byteBuffer.limit(this.func_181664_j() * 4);
                //setPointer();
            }
        }
    }

    public ByteBuffer getByteBuffer() {
        return this.byteBuffer;
    }

    public VertexFormat getVertexFormat() {
        return this.vertexFormat;
    }

    public int getVertexCount() {
        return this.vertexCount;
    }

    public int getDrawMode() {
        return this.drawMode;
    }

    public void putColor4(int argb) {
        for (int i = 0; i < 4; ++i) {
            this.putColor(argb, i + 1);
        }
    }

    public void putColorRGB_F4(float red, float green, float blue) {
        for (int i = 0; i < 4; ++i) {
            this.putColorRGB_F(red, green, blue, i + 1);
        }
    }

    public void putSprite(TextureAtlasSprite p_putSprite_1_) {
        if (this.quadSprites != null) {
            int i = this.vertexCount / 4;
            this.quadSprites[i - 1] = p_putSprite_1_;
        }
    }

    public void setSprite(TextureAtlasSprite p_setSprite_1_) {
        if (this.quadSprites != null) {
            this.quadSprite = p_setSprite_1_;
        }
    }

    public WorldRenderer setSpriteReturn(TextureAtlasSprite p_setSprite_1_) {
        if (this.quadSprites == null) {
            this.quadSprites = new TextureAtlasSprite[this.getBufferQuadSize()];
            //putSprite(p_setSprite_1_);
            quadSprites[0] = p_setSprite_1_;
        }
        this.quadSprite = p_setSprite_1_;
        return this;
    }

    public boolean isMultiTexture() {
        return this.quadSprites != null;
    }

    public void drawMultiTexture() {
        if (this.quadSprites != null) {
            int i = Config.getMinecraft().getTextureMapBlocks().getCountRegisteredSprites();

            if (this.drawnIcons.length <= i) {
                this.drawnIcons = new boolean[i + 1];
            }

            Arrays.fill(this.drawnIcons, false);
            int k = -1;
            int l = this.vertexCount / 4;

            for (int i1 = 0; i1 < l; ++i1) {
                TextureAtlasSprite textureatlassprite = this.quadSprites[i1];

                if (textureatlassprite != null) {
                    int j1 = textureatlassprite.getIndexInMap();

                    if (!this.drawnIcons[j1]) {
                        if (textureatlassprite == TextureUtils.iconGrassSideOverlay) {
                            if (k < 0) {
                                k = i1;
                            }
                        } else {
                            i1 = this.drawForIcon(textureatlassprite, i1) - 1;

                            if (this.blockLayer != EnumWorldBlockLayer.TRANSLUCENT) {
                                this.drawnIcons[j1] = true;
                            }
                        }
                    }
                }
            }

            if (k >= 0) {
                this.drawForIcon(TextureUtils.iconGrassSideOverlay, k);
            }

        }
    }

    private int drawForIcon(TextureAtlasSprite p_drawForIcon_1_, int p_drawForIcon_2_) {
        //GlStateManager.forceBindTexture(p_drawForIcon_1_.glSpriteTextureId);
        GlStateManager.bindTexture(p_drawForIcon_1_.glSpriteTextureId);
        int i = -1;
        int j = -1;
        int k = this.vertexCount / 4;

        for (int l = p_drawForIcon_2_; l < k; ++l) {
            TextureAtlasSprite textureatlassprite = this.quadSprites[l];

            if (textureatlassprite == p_drawForIcon_1_) {
                if (j < 0) {
                    j = l;
                }
            } else if (j >= 0) {
                this.draw(j, l);

                if (this.blockLayer == EnumWorldBlockLayer.TRANSLUCENT) {
                    return l;
                }

                j = -1;

                if (i < 0) {
                    i = l;
                }
            }
        }

        if (j >= 0) {
            this.draw(j, k);
        }

        if (i < 0) {
            i = k;
        }

        return i;
    }

    private void draw(int p_draw_1_, int p_draw_2_) {
        int i = p_draw_2_ - p_draw_1_;

        if (i > 0) {
            int j = p_draw_1_ * 4;
            int k = i * 4;
            GL11.glDrawArrays(this.drawMode, j, k);
        }
    }

    public void setBlockLayer(EnumWorldBlockLayer p_setBlockLayer_1_) {
        this.blockLayer = p_setBlockLayer_1_;

        if (p_setBlockLayer_1_ == null) {
            if (this.quadSprites != null) {
                this.quadSpritesPrev = this.quadSprites;
            }

            this.quadSprites = null;
            this.quadSprite = null;
        }
    }

    private int getBufferQuadSize() {
        return this.rawIntBuffer.capacity() * 4 / (this.vertexFormat.getIntegerSize() * 4);
    }

    public boolean drawing() {
        return isDrawing;
    }

    /*public void checkAndGrow()
    {
        this.func_181670_b(this.vertexFormat.getIntegerSize());
    }

     */

    /*public boolean isColorDisabled()
    {
        return this.needsUpdate;
    }

     */

    private void setPointer() {
        bufferPointer = MemoryUtil.getAddress(byteBuffer, byteBuffer.position());
    }

    static final class WorldRenderer$2 {
        static final int[] field_181661_a = new int[VertexFormatElement.EnumType.VALUES.length];


        static {
            try {
                field_181661_a[VertexFormatElement.EnumType.FLOAT.ordinal()] = 1;
            } catch (NoSuchFieldError var7) {
                var7.printStackTrace();
            }

            try {
                field_181661_a[VertexFormatElement.EnumType.UINT.ordinal()] = 2;
            } catch (NoSuchFieldError var6) {
                var6.printStackTrace();
            }

            try {
                field_181661_a[VertexFormatElement.EnumType.INT.ordinal()] = 3;
            } catch (NoSuchFieldError var5) {
                var5.printStackTrace();
            }

            try {
                field_181661_a[VertexFormatElement.EnumType.USHORT.ordinal()] = 4;
            } catch (NoSuchFieldError var4) {
                var4.printStackTrace();
            }

            try {
                field_181661_a[VertexFormatElement.EnumType.SHORT.ordinal()] = 5;
            } catch (NoSuchFieldError var3) {
                var3.printStackTrace();
            }

            try {
                field_181661_a[VertexFormatElement.EnumType.UBYTE.ordinal()] = 6;
            } catch (NoSuchFieldError var2) {
                var2.printStackTrace();
            }

            try {
                field_181661_a[VertexFormatElement.EnumType.BYTE.ordinal()] = 7;
            } catch (NoSuchFieldError var1) {
                var1.printStackTrace();
            }
        }
    }

    public static class State {
        private final int[] stateRawBuffer;
        private final VertexFormat stateVertexFormat;

        private TextureAtlasSprite[] stateQuadSprites;

        public State(int[] p_i2_2_, VertexFormat p_i2_3_, TextureAtlasSprite[] p_i2_4_) {
            this.stateRawBuffer = p_i2_2_;
            this.stateVertexFormat = p_i2_3_;
            this.stateQuadSprites = p_i2_4_;
        }

        /*public State(int[] p_i46453_2_, VertexFormat p_i46453_3_)
        {
            this.stateRawBuffer = p_i46453_2_;
            this.stateVertexFormat = p_i46453_3_;
        }

         */

        public int[] getRawBuffer() {
            return this.stateRawBuffer;
        }

        public int getVertexCount() {
            return this.stateRawBuffer.length / this.stateVertexFormat.getIntegerSize();
        }

        public VertexFormat getVertexFormat() {
            return this.stateVertexFormat;
        }
    }
}
