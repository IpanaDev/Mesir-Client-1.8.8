package net.minecraft.client.renderer;

import com.google.common.collect.Maps;
import com.google.gson.JsonSyntaxException;
import ipana.managements.module.Modules;
import ipana.modules.render.Hud;
import ipana.utils.StringUtil;
import ipana.utils.chunk.AsyncChunkBuilder;
import ipana.utils.chunk.ChunkBuildType;
import ipana.utils.chunk.ChunkFacings;
import ipana.utils.player.PlayerUtils;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.chunk.*;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemRecord;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.*;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.IChunkProvider;
import optifine.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector4f;
import shadersmod.client.Shaders;
import shadersmod.client.ShadersRender;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RenderGlobal implements IWorldAccess, IResourceManagerReloadListener {
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation locationMoonPhasesPng = new ResourceLocation("textures/environment/moon_phases.png");
    private static final ResourceLocation locationSunPng = new ResourceLocation("textures/environment/sun.png");
    private static final ResourceLocation locationCloudsPng = new ResourceLocation("textures/environment/clouds.png");
    private static final ResourceLocation locationEndSkyPng = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation locationForcefieldPng = new ResourceLocation("textures/misc/forcefield.png");

    /** A reference to the Minecraft object. */
    public final Minecraft mc;

    /** The RenderEngine instance used by RenderGlobal */
    private final TextureManager renderEngine;
    private final RenderManager renderManager;
    private WorldClient theWorld;

    /** List of OpenGL lists for the current render pass */
    //public transient volatile ChunkList[] allChunks = new ChunkList[EnumWorldBlockLayer.VALUES.length];
    private final ExecutorService SINGLE_THREAD = Executors.newSingleThreadExecutor();
    private final List<RenderChunk>[] THREADED_CHUNKS = new List[EnumWorldBlockLayer.VALUES.length];
    public volatile List<RenderChunk>[] allChunks = new List[EnumWorldBlockLayer.VALUES.length];
    //public SwapList allChunks = new SwapList(69696);
    //private final Set<TileEntity> field_181024_n = Sets.newHashSet();
    public ViewFrustum viewFrustum;

    /** The star GL Call list */
    private int starGLCallList = -1;

    /** OpenGL sky list */
    private int glSkyList = -1;

    /** OpenGL sky list 2 */
    private int glSkyList2 = -1;
    public VertexFormat vertexBufferFormat;
    private VertexBuffer starVBO;
    private VertexBuffer skyVBO;
    private VertexBuffer sky2VBO;

    /**
     * counts the cloud render updates. Used with mod to stagger some updates
     */
    private int cloudTickCounter;

    /**
     * Stores blocks currently being broken. Key is entity ID of the thing doing the breaking. Value is a
     * DestroyBlockProgress
     */
    public final Map<Integer,DestroyBlockProgress> damagedBlocks = Maps.newHashMap();

    /** Currently playing sounds.  Type:  HashMap<ChunkCoordinates, ISound> */
    private final Map<BlockPos,PositionedSoundRecord> mapSoundPositions = Maps.newHashMap();
    private final TextureAtlasSprite[] destroyBlockIcons = new TextureAtlasSprite[10];
    private Framebuffer entityOutlineFramebuffer;

    /** Stores the shader group for the entity_outline shader */
    private ShaderGroup entityOutlineShader;
    public double frustumUpdatePosX = Double.MIN_VALUE;
    public double frustumUpdatePosY = Double.MIN_VALUE;
    public double frustumUpdatePosZ = Double.MIN_VALUE;
    public int frustumUpdatePosChunkX = Integer.MIN_VALUE;
    public int frustumUpdatePosChunkY = Integer.MIN_VALUE;
    public int frustumUpdatePosChunkZ = Integer.MIN_VALUE;
    private double lastViewEntityX = Double.MIN_VALUE;
    private double lastViewEntityY = Double.MIN_VALUE;
    private double lastViewEntityZ = Double.MIN_VALUE;
    private double lastViewEntityPitch = Double.MIN_VALUE;
    private double lastViewEntityYaw = Double.MIN_VALUE;
    //public final ChunkRenderDispatcher renderDispatcher = new ChunkRenderDispatcher();
    public ChunkRenderContainer renderContainer;
    private int renderDistanceChunks = -1;

    /** Render entities startup counter (init value=2) */
    private int renderEntitiesStartupCounter = 2;

    /** Count entities total */
    private int countEntitiesTotal;

    /** Count entities rendered */
    private int countEntitiesRendered;

    /** Count entities hidden */
    private int countEntitiesHidden;
    private boolean debugFixTerrainFrustum = false;
    private ClippingHelper debugFixedClippingHelper;
    private final Vector4f[] debugTerrainMatrix = new Vector4f[8];
    private final Vector3d debugTerrainFrustumPosition = new Vector3d();
    private boolean vboEnabled;
    IRenderChunkFactory renderChunkFactory;
    private double prevRenderSortX;
    private double prevRenderSortY;
    private double prevRenderSortZ;
    public boolean displayListEntitiesDirty = true;

    private CloudRenderer cloudRenderer;
    public Entity renderedEntity;
    public Set<RenderChunk> chunksToResortTransparency = new LinkedHashSet<>();
    public Set<RenderChunk> chunksToUpdate = new LinkedHashSet<>();
    //private List<RenderChunk>[] allChunksShadow = new ArrayList[4];
    //private SwapList allChunksShadow = new SwapList(1024);
    public int renderDistance = 0;
    public int renderDistanceSq = 0;
    private int countTileEntitiesRendered;
    private IChunkProvider worldChunkProvider = null;
    private LongHashMap worldChunkProviderMap = null;
    private int countLoadedChunksPrev = 0;
    public boolean wasDirtyLastFrame;
    public long viewerRegionX, viewerRegionZ;

    public RenderGlobal(Minecraft mcIn) {
        this.cloudRenderer = new CloudRenderer(mcIn);
        this.mc = mcIn;
        this.renderManager = mcIn.getRenderManager();
        this.renderEngine = mcIn.getTextureManager();
        this.renderEngine.bindTexture(locationForcefieldPng);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GlStateManager.bindTexture(0);
        this.updateDestroyBlockIcons();
        this.vboEnabled = OpenGlHelper.useVbo();

        if (this.vboEnabled) {
            this.renderContainer = new VboRenderList();
            this.renderChunkFactory = new VboChunkFactory();
        } else {
            this.renderContainer = new RenderList();
            this.renderChunkFactory = new ListChunkFactory();
        }

        this.vertexBufferFormat = new VertexFormat();
        this.vertexBufferFormat.func_181721_a(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3));
        this.generateStars();
        this.generateSky();
        this.generateSky2();
        for (EnumWorldBlockLayer layer : EnumWorldBlockLayer.VALUES) {
            allChunks[layer.ordinal()] = new ArrayList<>(69696);
            THREADED_CHUNKS[layer.ordinal()] = new ArrayList<>(69696);
        }

    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        this.updateDestroyBlockIcons();
    }

    private void updateDestroyBlockIcons() {
        TextureMap texturemap = this.mc.getTextureMapBlocks();

        for (int i = 0; i < this.destroyBlockIcons.length; ++i)
        {
            this.destroyBlockIcons[i] = texturemap.getAtlasSprite("minecraft:blocks/destroy_stage_" + i);
        }
    }

    /**
     * Creates the entity outline shader to be stored in RenderGlobal.entityOutlineShader
     */
    public void makeEntityOutlineShader() {
        if (OpenGlHelper.shadersSupported) {
            if (ShaderLinkHelper.getStaticShaderLinkHelper() == null) {
                ShaderLinkHelper.setNewStaticShaderLinkHelper();
            }

            ResourceLocation resourcelocation = new ResourceLocation("shaders/post/entity_outline.json");

            try {
                this.entityOutlineShader = new ShaderGroup(this.mc.getTextureManager(), this.mc.getResourceManager(), this.mc.getFramebuffer(), resourcelocation);
                this.entityOutlineShader.createBindFramebuffers(this.mc.displayWidth, this.mc.displayHeight);
                this.entityOutlineFramebuffer = this.entityOutlineShader.getFramebufferRaw("final");
            } catch (IOException | JsonSyntaxException ioexception) {
                logger.warn(("Failed to load shader: " + resourcelocation), ioexception);
                this.entityOutlineShader = null;
                this.entityOutlineFramebuffer = null;
            }
        } else {
            this.entityOutlineShader = null;
            this.entityOutlineFramebuffer = null;
        }
    }

    public void renderEntityOutlineFramebuffer() {
        if (this.isRenderEntityOutlines()) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            //int scaleWidth = Minecraft.getMinecraft().getFramebuffer().scaledWidth;
            //int scaleHeight = Minecraft.getMinecraft().getFramebuffer().scaledHeight;
            this.entityOutlineFramebuffer.framebufferRenderExt((mc.displayWidth), (mc.displayHeight), false);
            GlStateManager.disableBlend();
        }
    }

    protected boolean isRenderEntityOutlines() {
        return (!Config.isFastRender() && !Config.isShaders() && !Config.isAntialiasing()) && (this.entityOutlineFramebuffer != null && this.entityOutlineShader != null && this.mc.thePlayer != null && this.mc.thePlayer.isSpectator() && this.mc.gameSettings.keyBindSpectatorOutlines.isKeyDown());
    }

    private void generateSky2() {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        if (this.sky2VBO != null) {
            this.sky2VBO.deleteGlBuffers();
        }

        if (this.glSkyList2 >= 0) {
            GLAllocation.deleteDisplayLists(this.glSkyList2);
            this.glSkyList2 = -1;
        }

        if (this.vboEnabled) {
            this.sky2VBO = new VertexBuffer(this.vertexBufferFormat);
            this.renderSky(worldrenderer, -16.0F, true);
            worldrenderer.finishDrawing();
            worldrenderer.reset();
            this.sky2VBO.uploadBufferData(worldrenderer.getByteBuffer());
        } else {
            this.glSkyList2 = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(this.glSkyList2, GL11.GL_COMPILE);
            this.renderSky(worldrenderer, -16.0F, true);
            tessellator.draw();
            GL11.glEndList();
        }
    }

    private void generateSky() {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        if (this.skyVBO != null) {
            this.skyVBO.deleteGlBuffers();
        }

        if (this.glSkyList >= 0) {
            GLAllocation.deleteDisplayLists(this.glSkyList);
            this.glSkyList = -1;
        }

        if (this.vboEnabled) {
            this.skyVBO = new VertexBuffer(this.vertexBufferFormat);
            this.renderSky(worldrenderer, 16.0F, false);
            worldrenderer.finishDrawing();
            worldrenderer.reset();
            this.skyVBO.uploadBufferData(worldrenderer.getByteBuffer());
        } else {
            this.glSkyList = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(this.glSkyList, GL11.GL_COMPILE);
            this.renderSky(worldrenderer, 16.0F, false);
            tessellator.draw();
            GL11.glEndList();
        }
    }

    private void renderSky(WorldRenderer worldRendererIn, float p_174968_2_, boolean p_174968_3_) {

        worldRendererIn.begin(7, DefaultVertexFormats.POSITION);

        for (int i = -384; i <= 384; i += 64)
        {
            for (int j = -384; j <= 384; j += 64)
            {
                float f = (float)i;
                float f1 = (float)(i + 64);

                if (p_174968_3_)
                {
                    f1 = (float)i;
                    f = (float)(i + 64);
                }

                worldRendererIn.pos(f, p_174968_2_, j).endVertex();
                worldRendererIn.pos(f1, p_174968_2_, j).endVertex();
                worldRendererIn.pos(f1, p_174968_2_, (j + 64)).endVertex();
                worldRendererIn.pos(f, p_174968_2_, (j + 64)).endVertex();
            }
        }
    }

    private void generateStars()
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        if (this.starVBO != null)
        {
            this.starVBO.deleteGlBuffers();
        }

        if (this.starGLCallList >= 0)
        {
            GLAllocation.deleteDisplayLists(this.starGLCallList);
            this.starGLCallList = -1;
        }

        if (this.vboEnabled)
        {
            this.starVBO = new VertexBuffer(this.vertexBufferFormat);
            this.renderStars(worldrenderer);
            worldrenderer.finishDrawing();
            worldrenderer.reset();
            this.starVBO.uploadBufferData(worldrenderer.getByteBuffer());
        }
        else
        {
            this.starGLCallList = GLAllocation.generateDisplayLists(1);
            GlStateManager.pushMatrix();
            GL11.glNewList(this.starGLCallList, GL11.GL_COMPILE);
            this.renderStars(worldrenderer);
            tessellator.draw();
            GL11.glEndList();
            GlStateManager.popMatrix();
        }
    }

    private void renderStars(WorldRenderer worldRendererIn)
    {
        Random random = new Random(10842L);
        worldRendererIn.begin(7, DefaultVertexFormats.POSITION);

        for (int i = 0; i < 1500; ++i)
        {
            double d0 = (random.nextFloat() * 2.0F - 1.0F);
            double d1 = (random.nextFloat() * 2.0F - 1.0F);
            double d2 = (random.nextFloat() * 2.0F - 1.0F);
            double d3 = (0.15F + random.nextFloat() * 0.1F);
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;

            if (d4 < 1.0D && d4 > 0.01D)
            {
                d4 = 1.0D / Math.sqrt(d4);
                d0 = d0 * d4;
                d1 = d1 * d4;
                d2 = d2 * d4;
                double d5 = d0 * 100.0D;
                double d6 = d1 * 100.0D;
                double d7 = d2 * 100.0D;
                double d8 = Math.atan2(d0, d2);
                double d9 = Math.sin(d8);
                double d10 = Math.cos(d8);
                double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
                double d12 = Math.sin(d11);
                double d13 = Math.cos(d11);
                double d14 = random.nextDouble() * Math.PI * 2.0D;
                double d15 = Math.sin(d14);
                double d16 = Math.cos(d14);

                for (int j = 0; j < 4; ++j)
                {
                    double d18 = (double)((j & 2) - 1) * d3;
                    double d19 = (double)((j + 1 & 2) - 1) * d3;
                    double d21 = d18 * d16 - d19 * d15;
                    double d22 = d19 * d16 + d18 * d15;
                    double d23 = d21 * d12 + 0.0D * d13;
                    double d24 = 0.0D * d12 - d21 * d13;
                    double d25 = d24 * d9 - d22 * d10;
                    double d26 = d22 * d9 + d24 * d10;
                    worldRendererIn.pos(d5 + d25, d6 + d23, d7 + d26).endVertex();
                }
            }
        }
    }

    /**
     * set null to clear
     */
    public void setWorldAndLoadRenderers(WorldClient worldClientIn) {
        if (this.theWorld != null) {
            this.theWorld.removeWorldAccess(this);
        }

        this.frustumUpdatePosX = Double.MIN_VALUE;
        this.frustumUpdatePosY = Double.MIN_VALUE;
        this.frustumUpdatePosZ = Double.MIN_VALUE;
        this.frustumUpdatePosChunkX = Integer.MIN_VALUE;
        this.frustumUpdatePosChunkY = Integer.MIN_VALUE;
        this.frustumUpdatePosChunkZ = Integer.MIN_VALUE;
        this.renderManager.set(worldClientIn);
        this.theWorld = worldClientIn;

        if (Config.isDynamicLights())
        {
            DynamicLights.clear();
        }
        this.worldChunkProvider = null;
        this.worldChunkProviderMap = null;
        if (worldClientIn != null) {
            worldClientIn.addWorldAccess(this);
            this.loadRenderers();
        } else {
            this.clearRenderChunks();
            this.stopChunkUpdates();
            if (this.viewFrustum != null) {
                this.viewFrustum.deleteGlResources();
            }
            this.viewFrustum = null;
        }
    }

    /**
     * Loads all the renderers and sets up the basic settings usage
     */
    public void loadRenderers() {
        if (this.theWorld != null) {
            this.displayListEntitiesDirty = true;
            Blocks.leaves.setGraphicsLevel(Config.isTreesFancy());
            Blocks.leaves2.setGraphicsLevel(Config.isTreesFancy());
            BlockModelRenderer.updateAoLightValue();

            if (Config.isDynamicLights()) {
                DynamicLights.clear();
            }
            this.renderDistanceChunks = this.mc.gameSettings.renderDistanceChunks;
            this.renderDistance = this.renderDistanceChunks * 16;
            this.renderDistanceSq = this.renderDistance * this.renderDistance;
            boolean flag = this.vboEnabled;
            this.vboEnabled = OpenGlHelper.useVbo();

            if (flag && !this.vboEnabled) {
                this.renderContainer = new RenderList();
                this.renderChunkFactory = new ListChunkFactory();
            } else if (!flag && this.vboEnabled) {
                this.renderContainer = new VboRenderList();
                this.renderChunkFactory = new VboChunkFactory();
            }

            if (flag != this.vboEnabled) {
                this.generateStars();
                this.generateSky();
                this.generateSky2();
            }

            this.clearRenderChunks();
            this.stopChunkUpdates();
            if (this.viewFrustum != null) {
                this.viewFrustum.deleteGlResources();
            }

            /*synchronized (this.field_181024_n) {
                this.field_181024_n.clear();
            }*/

            this.viewFrustum = new ViewFrustum(this.theWorld, this.mc.gameSettings.renderDistanceChunks, this, this.renderChunkFactory);

            if (this.theWorld != null) {
                Entity entity = this.mc.getRenderViewEntity();

                if (entity != null) {
                    float partialTicks = 1;
                    double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
                    double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
                    double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
                    BlockPos blockpos = new BlockPos(MathHelper.floor_double(d0 / 16.0D) * 16, MathHelper.floor_double(d1 / 16.0D) * 16, MathHelper.floor_double(d2 / 16.0D) * 16);
                    viewerRegionX = blockpos.x;
                    viewerRegionZ = blockpos.z;
                    this.viewFrustum.updateChunkPositions(entity.posX, entity.posZ);
                }
            }
            this.renderEntitiesStartupCounter = 2;
        }
    }

    public void clearRenderChunks() {
        for (EnumWorldBlockLayer layer : EnumWorldBlockLayer.VALUES) {
            this.allChunks[layer.ordinal()].clear();
        }
    }
    protected void stopChunkUpdates() {
        this.chunksToUpdate.clear();
        this.renderDispatcher.stopChunkUpdates();
    }

    public void createBindEntityOutlineFbs(int p_72720_1_, int p_72720_2_) {
        if (OpenGlHelper.shadersSupported && this.entityOutlineShader != null) {
            this.entityOutlineShader.createBindFramebuffers(p_72720_1_, p_72720_2_);
        }
    }

    public void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks) {
        if (this.renderEntitiesStartupCounter > 0) {
            --this.renderEntitiesStartupCounter;
        } else {
            double d0 = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * (double) partialTicks;
            double d1 = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * (double) partialTicks;
            double d2 = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * (double) partialTicks;
            this.theWorld.theProfiler.startSection("prepare");
            TileEntityRendererDispatcher.instance.cacheActiveRenderInfo(this.theWorld, this.mc.getTextureManager(), this.mc.fontRendererObj, this.mc.getRenderViewEntity(), partialTicks);
            this.renderManager.cacheActiveRenderInfo(this.theWorld, this.mc.fontRendererObj, this.mc.getRenderViewEntity(), this.mc.pointedEntity, this.mc.gameSettings, partialTicks);
            this.countEntitiesTotal = 0;
            this.countEntitiesRendered = 0;
            this.countEntitiesHidden = 0;
            this.countTileEntitiesRendered = 0;

            Entity entity = this.mc.getRenderViewEntity();
            double d3 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
            double d4 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
            double d5 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
            TileEntityRendererDispatcher.staticPlayerX = d3;
            TileEntityRendererDispatcher.staticPlayerY = d4;
            TileEntityRendererDispatcher.staticPlayerZ = d5;
            this.renderManager.setRenderPosition(d3, d4, d5);
            this.mc.entityRenderer.enableLightmap();
            this.theWorld.theProfiler.endStartSection("global");
            List<Entity> list = this.theWorld.getLoadedEntityList();

            this.countEntitiesTotal = list.size();

            if (Config.isFogOff() && this.mc.entityRenderer.fogStandard) {
                GlStateManager.disableFog();
            }

            /*
            for (int j = 0; j < this.theWorld.weatherEffects.size(); ++j) {
                Entity entity1 = this.theWorld.weatherEffects.get(j);

                if (!flag || Reflector.callBoolean(entity1, Reflector.ForgeEntity_shouldRenderInPass, i)) {
                    ++this.countEntitiesRendered;

                    if (entity1.isInRangeToRender3d(d0, d1, d2)) {
                        this.renderManager.renderEntitySimple(entity1, partialTicks);
                    }
                }
            }

             */

            if (this.isRenderEntityOutlines()) {
                GlStateManager.depthFunc(519);
                GlStateManager.disableFog();
                this.entityOutlineFramebuffer.framebufferClear();
                this.entityOutlineFramebuffer.bindFramebuffer(false);
                this.theWorld.theProfiler.endStartSection("entityOutlines");
                RenderHelper.disableStandardItemLighting();
                this.renderManager.setRenderOutlines(true);
                for (Entity entity3 : list) {
                    boolean flag2 = this.mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) this.mc.getRenderViewEntity()).isPlayerSleeping();
                    boolean flag3 = entity3.isInRangeToRender3d(d0, d1, d2) && (entity3.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(entity3.getEntityBoundingBox()) || entity3.riddenByEntity == this.mc.thePlayer) && entity3 instanceof EntityPlayer;

                    if ((entity3 != this.mc.getRenderViewEntity() || this.mc.gameSettings.thirdPersonView != 0 || flag2) && flag3) {
                        this.renderManager.renderEntitySimple(entity3, partialTicks);
                    }
                }
                this.renderManager.setRenderOutlines(false);
                RenderHelper.enableStandardItemLighting();
                GlStateManager.depthMask(false);
                this.entityOutlineShader.loadShaderGroup(partialTicks);
                GlStateManager.enableLighting();
                GlStateManager.depthMask(true);
                this.mc.getFramebuffer().bindFramebuffer(false);
                GlStateManager.enableFog();
                GlStateManager.enableBlend();
                GlStateManager.enableColorMaterial();
                GlStateManager.depthFunc(515);
                GlStateManager.enableDepth();
                GlStateManager.enableAlpha();
            }

            this.theWorld.theProfiler.endStartSection("entities");
            boolean flag7 = Config.isShaders();

            if (flag7) {
                Shaders.beginEntities();
            }

            boolean flag4 = this.mc.gameSettings.fancyGraphics;
            this.mc.gameSettings.fancyGraphics = Config.isDroppedItemsFancy();
            if (Config.isFogOff()) {
                GlStateManager.disableFog();
            }
            GlStateManager.enableCull();
            GlStateManager.color(1, 1, 1, 1);
                for (Entity entity3 : list) {
                    if (entity3 instanceof EntityWitherSkull) {
                        if (flag7) {
                            Shaders.nextEntity(entity3);
                        }
                        this.mc.getRenderManager().renderWitherSkull(entity3, partialTicks);
                    } else {
                        if (entity3 == this.mc.getRenderViewEntity()) {
                            boolean flag2 = this.mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) this.mc.getRenderViewEntity()).isPlayerSleeping();
                            boolean za = Modules.CAMERA.isEnabled() || Modules.COOL_PERSPECTIVE.isEnabled() || this.mc.gameSettings.thirdPersonView != 0 || flag2;
                            if (!za) {
                                continue;
                            }
                        }
                        if ((this.renderManager.shouldRender(entity3, camera, d0, d1, d2) || entity3.riddenByEntity == mc.thePlayer) && (entity3.posY > 0.0D || entity3.posY <= 256.0D)) {
                            ++countEntitiesRendered;
                            if (entity3 instanceof EntityItemFrame) {
                                entity3.renderDistanceWeight = 0.06D;
                            }
                            this.renderedEntity = entity3;
                            this.renderManager.renderEntitySimple(entity3, partialTicks);
                            this.renderedEntity = null;
                        }
                    }
                }

            //System.out.println(troll);
            this.mc.gameSettings.fancyGraphics = flag4;
            FontRenderer fontrenderer = TileEntityRendererDispatcher.instance.getFontRenderer();

            if (flag7) {
                Shaders.endEntities();
                Shaders.beginBlockEntities();
            }

            this.theWorld.theProfiler.endStartSection("blockentities");
            RenderHelper.enableStandardItemLighting();

            for (int i = 0; i < theWorld.loadedTileEntityList.size(); i++) {
                TileEntity tileEntity = theWorld.loadedTileEntityList.get(i);
                if (!camera.isBoundingBoxInFrustum(tileEntity.boundingBox())) {
                    continue;
                }

                if (!tileEntity.canBeSeen(tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ())) {
                    continue;
                }
                if (tileEntity instanceof TileEntitySign sign && !Config.zoomMode) {
                    if (sign.distForRender > 256.0D) {
                        //fontrenderer.enabled = false;
                    }
                }

                if (flag7) {
                    Shaders.nextBlockEntity(tileEntity);
                }
                TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, partialTicks, -1);
                ++this.countTileEntitiesRendered;
                fontrenderer.enabled = true;
            }

            /*
            synchronized (this.field_181024_n) {
                for (TileEntity tileentity1 : this.field_181024_n) {
                    if (flag1) {
                        if (!Reflector.callBoolean(tileentity1, Reflector.ForgeTileEntity_shouldRenderInPass, i)) {
                            continue;
                        }
                        AxisAlignedBB axisalignedbb1 = (AxisAlignedBB) Reflector.call(tileentity1, Reflector.ForgeTileEntity_getRenderBoundingBox);

                        if (axisalignedbb1 != null && !camera.isBoundingBoxInFrustum(axisalignedbb1)) {
                            continue;
                        }
                    }

                    Class<?> oclass1 = tileentity1.getClass();

                    if (oclass1 == TileEntitySign.class && !Config.zoomMode) {
                        EntityPlayer entityplayer1 = this.mc.thePlayer;
                        double d7 = tileentity1.getDistanceSq(entityplayer1.posX, entityplayer1.posY, entityplayer1.posZ);

                        if (d7 > 256.0D) {
                            fontrenderer.enabled = false;
                        }
                    }

                    if (flag7) {
                        Shaders.nextBlockEntity(tileentity1);
                    }

                    TileEntityRendererDispatcher.instance.renderTileEntity(tileentity1, partialTicks, -1);
                    fontrenderer.enabled = true;
                }
            }

             */

            boolean hasDamaged = !this.damagedBlocks.isEmpty();
            if (hasDamaged) {
                this.preRenderDamagedBlocks();
            }
            for (DestroyBlockProgress destroyblockprogress : this.damagedBlocks.values()) {
                BlockPos blockpos = destroyblockprogress.getPosition();
                TileEntity tileentity2 = this.theWorld.getTileEntity(blockpos);

                if (tileentity2 instanceof TileEntityChest tileentitychest) {

                    if (tileentitychest.adjacentChestXNeg != null) {
                        blockpos = blockpos.offset(EnumFacing.WEST);
                        tileentity2 = this.theWorld.getTileEntity(blockpos);
                    } else if (tileentitychest.adjacentChestZNeg != null) {
                        blockpos = blockpos.offset(EnumFacing.NORTH);
                        tileentity2 = this.theWorld.getTileEntity(blockpos);
                    }
                }

                Block block = this.theWorld.getBlockState(blockpos).getBlock();
                boolean flag8;
                flag8 = tileentity2 != null && (block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockSign || block instanceof BlockSkull);

                if (flag8) {
                    if (flag7) {
                        Shaders.nextBlockEntity(tileentity2);
                    }

                    TileEntityRendererDispatcher.instance.renderTileEntity(tileentity2, partialTicks, destroyblockprogress.getPartialBlockDamage());
                }
            }

            if (hasDamaged) {
                this.postRenderDamagedBlocks();
            }
            this.mc.entityRenderer.disableLightmap();
            this.mc.mcProfiler.endSection();
        }
    }

    /**
     * Gets the render info for use on the Debug screen
     */
    public String getDebugInfoRenders() {
        int i = this.viewFrustum.renderChunks.length;
        int j = 0;
        for (EnumWorldBlockLayer layer : EnumWorldBlockLayer.VALUES) {
            List<RenderChunk> chunks = allChunks[layer.ordinal()];
            for (RenderChunk chunk : chunks) {
                CompiledChunk compiledchunk = chunk.getCompiledChunk();
                if (compiledchunk != CompiledChunk.DUMMY && !compiledchunk.isEmpty()) {
                    ++j;
                }
            }
        }

        return String.format("C: %d/%d %sD: %d, %s", j, i, this.mc.renderChunksMany ? "(s) " : "", this.renderDistanceChunks, this.renderDispatcher.getDebugInfo());
    }

    /**
     * Gets the entities info for use on the Debug screen
     */
    public String getDebugInfoEntities() {
        return "E: " + this.countEntitiesRendered + "/" + this.countEntitiesTotal + ", B: " + this.countEntitiesHidden + ", I: " + (this.countEntitiesTotal - this.countEntitiesHidden - this.countEntitiesRendered) + ", " + Config.getVersionDebug();
    }

    public void setupTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator) {
        if (this.mc.gameSettings.renderDistanceChunks != this.renderDistanceChunks) {
            this.loadRenderers();

        }

        this.theWorld.theProfiler.startSection("camera");
        double d0 = viewEntity.posX - this.frustumUpdatePosX;
        double d1 = viewEntity.posY - this.frustumUpdatePosY;
        double d2 = viewEntity.posZ - this.frustumUpdatePosZ;

        if (this.frustumUpdatePosChunkX != viewEntity.chunkCoordX || this.frustumUpdatePosChunkY != viewEntity.chunkCoordY || this.frustumUpdatePosChunkZ != viewEntity.chunkCoordZ || d0 * d0 + d1 * d1 + d2 * d2 > 16.0D) {
            this.frustumUpdatePosX = viewEntity.posX;
            this.frustumUpdatePosY = viewEntity.posY;
            this.frustumUpdatePosZ = viewEntity.posZ;
            this.frustumUpdatePosChunkX = viewEntity.chunkCoordX;
            this.frustumUpdatePosChunkY = viewEntity.chunkCoordY;
            this.frustumUpdatePosChunkZ = viewEntity.chunkCoordZ;
            this.viewFrustum.updateChunkPositions(viewEntity.posX, viewEntity.posZ);
        }

        if (Config.isDynamicLights()) {
            DynamicLights.update(this);
        }

        this.theWorld.theProfiler.endStartSection("renderlistcamera");
        double d3 = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTicks;
        double d4 = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTicks;
        double d5 = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTicks;
        this.renderContainer.initialize(d3, d4, d5);
        this.theWorld.theProfiler.endStartSection("cull");

        if (this.debugFixedClippingHelper != null) {
            Frustum frustum = new Frustum(this.debugFixedClippingHelper);
            frustum.setPosition(this.debugTerrainFrustumPosition.field_181059_a, this.debugTerrainFrustumPosition.field_181060_b, this.debugTerrainFrustumPosition.field_181061_c);
        }

        this.mc.mcProfiler.endStartSection("culling");

        BlockPos blockpos2 = new BlockPos(d3, d4 + (double) viewEntity.getEyeHeight(), d5);

        RenderChunk renderchunk = this.viewFrustum.getRenderChunk(blockpos2);

        //BlockPos blockpos = new BlockPos(MathHelper.floor_double(d3 / 16.0D) * 16, MathHelper.floor_double(d4 / 16.0D) * 16, MathHelper.floor_double(d5 / 16.0D) * 16);

        this.displayListEntitiesDirty = this.displayListEntitiesDirty || !this.chunksToUpdate.isEmpty()
                || viewEntity.posX != this.lastViewEntityX || viewEntity.posY != this.lastViewEntityY || viewEntity.posZ != this.lastViewEntityZ ||
                (double) viewEntity.rotationPitch != this.lastViewEntityPitch || (double) viewEntity.rotationYaw != this.lastViewEntityYaw;
        this.lastViewEntityX = viewEntity.posX;
        this.lastViewEntityY = viewEntity.posY;
        this.lastViewEntityZ = viewEntity.posZ;
        this.lastViewEntityPitch = viewEntity.rotationPitch;
        this.lastViewEntityYaw = viewEntity.rotationYaw;
        boolean flag = this.debugFixedClippingHelper != null;
        Lagometer.timerVisibility.start();
        //TODO: tf
        /*if (Shaders.isShadowPass) {
            if (!flag && this.displayListEntitiesDirty) {
                Iterator<RenderChunk> iterator = ShadowUtils.makeShadowChunkIterator(this.theWorld, partialTicks, viewEntity, this.renderDistanceChunks, this.viewFrustum);
                while (iterator.hasNext()) {
                    RenderChunk renderchunk1 = iterator.next();
                    if (renderchunk1 != null) {
                        if (!renderchunk1.compiledChunk.isEmpty() || renderchunk1.isNeedsUpdate()) {
                            //addToChunks(renderinfolazy.getRenderInfo().renderChunk);
                        }
                    }
                }
            }
        }*/
        this.wasDirtyLastFrame = displayListEntitiesDirty;
        if (!flag && this.displayListEntitiesDirty && !Shaders.isShadowPass) {
            this.displayListEntitiesDirty = false;
            SINGLE_THREAD.submit(() -> {
                if (renderchunk != null) {
                    Hud.DEBUG_MS = 0;
                    renderchunk.facingIndex = 0;

                    for (EnumWorldBlockLayer layer : EnumWorldBlockLayer.VALUES) {
                        THREADED_CHUNKS[layer.ordinal()].clear();
                    }

                    addToChunks(renderchunk);
                    sortFilterChunks(renderchunk, null, frameCount, blockpos2, camera);
                    sortTranslucentChunks(viewEntity);

                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        this.clearRenderChunks();
                        for (EnumWorldBlockLayer layer : EnumWorldBlockLayer.VALUES) {
                            allChunks[layer.ordinal()].addAll(THREADED_CHUNKS[layer.ordinal()]);
                        }
                    });
                }
            });
        }

        if (this.debugFixTerrainFrustum) {
            this.fixTerrainFrustum(d3, d4, d5);
            this.debugFixTerrainFrustum = false;
        }

        Lagometer.timerVisibility.end();
        this.mc.mcProfiler.endSection();
    }

    private void sortTranslucentChunks(Entity viewEntity) {
        //this.mc.mcProfiler.startSection("translucent_sort");
        List<RenderChunk> translucentChunks = THREADED_CHUNKS[EnumWorldBlockLayer.TRANSLUCENT.ordinal()];
        translucentChunks.sort(Comparator.comparingDouble(chunk -> {
            BlockPos pos = chunk.getPosition().add(8,8,8);
            double xDiff = viewEntity.posX - pos.getX();
            double yDiff = viewEntity.posY - pos.getY();
            double zDiff = viewEntity.posZ - pos.getZ();
            return -(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
        }));
        //this.mc.mcProfiler.endSection();
    }

    private void sortFilterChunks(RenderChunk parentChunk, EnumFacing parentFacing, int frameCount, BlockPos viewPos, ICamera camera) {
        EnumFacing[] facings = ChunkFacings.getFacingsNotOpposite(parentChunk.facingIndex);
        for (EnumFacing facing : facings) {
            if (!Modules.X_RAY.isEnabled() && parentFacing != null && !parentChunk.getCompiledChunk().isVisible(parentFacing.getOpposite(), facing)) {
                continue;
            }
            RenderChunk neighbourChunk = getRenderChunk(parentChunk, facing);
            if (neighbourChunk != null) {
                boolean newFrame = neighbourChunk.isNewFrame(frameCount);
                if (newFrame) {
                    neighbourChunk.facingIndex = parentChunk.facingIndex | 1 << facing.ordinal();
                } else {
                    neighbourChunk.facingIndex = neighbourChunk.facingIndex | 1 << facing.ordinal();
                    continue;
                }
                int chunkX = viewPos.getX() - neighbourChunk.getPosition().getX() - 7;
                int chunkZ = viewPos.getZ() - neighbourChunk.getPosition().getZ() - 7;
                if (chunkX * chunkX + chunkZ * chunkZ > renderDistanceSq) {
                    continue;
                }
                if (neighbourChunk.boundingBox == null && !camera.isBoundingBoxInFrustum(neighbourChunk.fullBox)) {
                    continue;
                }
                if (camera.isBoundingBoxInFrustum(neighbourChunk.boundingBox)) {
                    addToChunks(neighbourChunk);
                }
                sortFilterChunks(neighbourChunk, facing, frameCount, viewPos, camera);
            }
        }
    }

    private void addToChunks(RenderChunk chunk) {
        for (EnumWorldBlockLayer layer : EnumWorldBlockLayer.VALUES) {
            if (chunk.getCompiledChunk().isLayerUsed(layer)) {
                THREADED_CHUNKS[layer.ordinal()].add(chunk);
            }
        }
    }

    /*private void dequeCheck(Entity viewEntity, int frameCount, boolean flag1, BlockPos blockpos2, ICamera camera, ContainerLocalRenderInformation localRenderInformation, boolean checkForSides) {
        Hud.DEBUG_MS++;
        RenderChunk renderchunk4 = localRenderInformation.renderChunk;
        EnumFacing enumfacing2 = localRenderInformation.facing;
        if (!renderchunk4.compiledChunk.isEmpty() || renderchunk4.isNeedsUpdate()) {
            if (!renderchunk4.isEmptyChunk()) {
                //this.allChunks.add(localRenderInformation.renderChunk);
                addToChunks(localRenderInformation.renderChunk);
            }
        }

        if (checkForSides) {
            for (EnumFacing allFaces : flag1 ? ChunkVisibility.getFacingsNotOpposite(localRenderInformation.setFacing) : EnumFacing.VALUES) {
                if (!flag1 || enumfacing2 == null || renderchunk4.getCompiledChunk().isVisible(enumfacing2.getOpposite(), allFaces)) {
                    RenderChunk newRenderChunk = this.getRenderChunkOffset(blockpos2, renderchunk4, allFaces, true, 256);
                    if (newRenderChunk != null && camera.isBoundingBoxInFrustum(newRenderChunk.boundingBox) && newRenderChunk.setFrameIndex(frameCount)) {
                        int i1 = localRenderInformation.setFacing | 1 << allFaces.ordinal();
                        ContainerLocalRenderInformation info = newRenderChunk.getRenderInfo();
                        info.initialize(allFaces, i1);
                        dequeCheck(viewEntity, frameCount, flag1, blockpos2, camera, info, true);
                    }
                }
            }
        }
    }*/

    public boolean isPositionInRenderChunk(BlockPos pos, RenderChunk renderChunkIn) {
        BlockPos blockpos = renderChunkIn.getPosition();
        return MathHelper.abs_int(pos.getX() - blockpos.getX()) <= 16 && (MathHelper.abs_int(pos.getY() - blockpos.getY()) <= 16 && MathHelper.abs_int(pos.getZ() - blockpos.getZ()) <= 16);
    }

    private void fixTerrainFrustum(double x, double y, double z) {
        this.debugFixedClippingHelper = new ClippingHelperImpl();
        ((ClippingHelperImpl) this.debugFixedClippingHelper).init();
        Matrix4f matrix4f = new Matrix4f(this.debugFixedClippingHelper.modelviewMatrix);
        matrix4f.transpose();
        Matrix4f matrix4f1 = new Matrix4f(this.debugFixedClippingHelper.projectionMatrix);
        matrix4f1.transpose();
        Matrix4f matrix4f2 = new Matrix4f();
        Matrix4f.mul(matrix4f1, matrix4f, matrix4f2);
        matrix4f2.invert();
        this.debugTerrainFrustumPosition.field_181059_a = x;
        this.debugTerrainFrustumPosition.field_181060_b = y;
        this.debugTerrainFrustumPosition.field_181061_c = z;
        this.debugTerrainMatrix[0] = new Vector4f(-1.0F, -1.0F, -1.0F, 1.0F);
        this.debugTerrainMatrix[1] = new Vector4f(1.0F, -1.0F, -1.0F, 1.0F);
        this.debugTerrainMatrix[2] = new Vector4f(1.0F, 1.0F, -1.0F, 1.0F);
        this.debugTerrainMatrix[3] = new Vector4f(-1.0F, 1.0F, -1.0F, 1.0F);
        this.debugTerrainMatrix[4] = new Vector4f(-1.0F, -1.0F, 1.0F, 1.0F);
        this.debugTerrainMatrix[5] = new Vector4f(1.0F, -1.0F, 1.0F, 1.0F);
        this.debugTerrainMatrix[6] = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.debugTerrainMatrix[7] = new Vector4f(-1.0F, 1.0F, 1.0F, 1.0F);

        for (int i = 0; i < 8; ++i) {
            Matrix4f.transform(matrix4f2, this.debugTerrainMatrix[i], this.debugTerrainMatrix[i]);
            this.debugTerrainMatrix[i].x /= this.debugTerrainMatrix[i].w;
            this.debugTerrainMatrix[i].y /= this.debugTerrainMatrix[i].w;
            this.debugTerrainMatrix[i].z /= this.debugTerrainMatrix[i].w;
            this.debugTerrainMatrix[i].w = 1.0F;
        }
    }

    /*protected Vector3f getViewVector(Entity entityIn, double partialTicks) {
        float f = (float) ((double) entityIn.prevRotationPitch + (double) (entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks);
        float f1 = (float) ((double) entityIn.prevRotationYaw + (double) (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks);

        if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2) {
            f += 180.0F;
        }

        float f2 = MathHelper.cos(-f1 * 0.017453292F - (float) Math.PI);
        float f3 = MathHelper.sin(-f1 * 0.017453292F - (float) Math.PI);
        float f4 = -MathHelper.cos(-f * 0.017453292F);
        float f5 = MathHelper.sin(-f * 0.017453292F);
        return new Vector3f(f3 * f4, f5, f2 * f4);
    }*/

    public void renderBlockLayer(EnumWorldBlockLayer blockLayerIn, Entity entityIn, boolean toggleStates) {
        List<RenderChunk> renderChunks = allChunks[blockLayerIn.ordinal()];
        if (renderChunks.isEmpty()) {
            return;
        }
        RenderHelper.disableStandardItemLighting();
        if (Config.isFogOff() && this.mc.entityRenderer.fogStandard) {
            GlStateManager.disableFog();
        }
        this.mc.mcProfiler.startSection(StringUtil.combine("render_",blockLayerIn));
        this.renderBlockLayer(blockLayerIn, toggleStates, renderChunks);
        this.mc.mcProfiler.endSection();
    }

    public void enableLayerState() {
        this.mc.entityRenderer.enableLightmap();

        if (OpenGlHelper.useVbo()) {

            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        }
    }

    public void disableLayerState() {
        if (OpenGlHelper.useVbo()) {
            for (VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements()) {
                VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
                int i = vertexformatelement.getIndex();

                switch (RenderGlobal$2.field_178037_a[vertexformatelement$enumusage.ordinal()]) {
                    case 1 -> GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                    case 2 -> {
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + i);
                        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                    }
                    case 3 -> {
                        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
                        GlStateManager.resetColor();
                    }
                }
            }
        }
        this.mc.entityRenderer.disableLightmap();
    }

    @SuppressWarnings("incomplete-switch")
    private void renderBlockLayer(EnumWorldBlockLayer blockLayerIn, boolean toggleStates, List<RenderChunk> chunks) {
        if (toggleStates) {
            enableLayerState();
        }

        if (Config.isShaders()) {
            ShadersRender.preRenderChunkLayer(blockLayerIn);
        }
        this.renderContainer.renderChunkLayer(blockLayerIn, chunks);

        if (Config.isShaders()) {
            ShadersRender.postRenderChunkLayer(blockLayerIn);
        }

        if (toggleStates) {
            disableLayerState();
        }
    }

    private void cleanupDamagedBlocks(Iterator<DestroyBlockProgress> iteratorIn) {
        while (iteratorIn.hasNext()) {
            DestroyBlockProgress destroyblockprogress = iteratorIn.next();
            int i = destroyblockprogress.getCreationCloudUpdateTick();

            if (this.cloudTickCounter - i > 400) {
                iteratorIn.remove();
            }
        }
    }

    public void updateClouds() {
        if (Config.isShaders() && Keyboard.isKeyDown(61) && Keyboard.isKeyDown(19)) {
            Shaders.uninit();
            Shaders.loadShaderPack();
        }

        ++this.cloudTickCounter;

        if (this.cloudTickCounter % 20 == 0) {
            this.cleanupDamagedBlocks(this.damagedBlocks.values().iterator());
        }
    }

    private void renderSkyEnd() {
        if (Config.isSkyEnabled()) {
            GlStateManager.disableFog();
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.depthMask(false);
            this.renderEngine.bindTexture(locationEndSkyPng);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();

            for (int i = 0; i < 6; ++i) {
                GlStateManager.pushMatrix();

                if (i == 1) {
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 2) {
                    GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 3) {
                    GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 4) {
                    GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                }

                if (i == 5) {
                    GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
                }

                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                worldrenderer.pos(-100.0D, -100.0D, -100.0D).tex(0.0D, 0.0D).color(40, 40, 40, 255).endVertex();
                worldrenderer.pos(-100.0D, -100.0D, 100.0D).tex(0.0D, 16.0D).color(40, 40, 40, 255).endVertex();
                worldrenderer.pos(100.0D, -100.0D, 100.0D).tex(16.0D, 16.0D).color(40, 40, 40, 255).endVertex();
                worldrenderer.pos(100.0D, -100.0D, -100.0D).tex(16.0D, 0.0D).color(40, 40, 40, 255).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
            }

            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.enableAlpha();
        }
    }

    public void renderSky(float partialTicks, int pass) {

        if (this.mc.theWorld.provider.getDimensionId() == 1) {
            this.renderSkyEnd();
        } else if (this.mc.theWorld.provider.isSurfaceWorld()) {
            GlStateManager.disableTexture2D();
            boolean flag1 = Config.isShaders();

            if (flag1) {
                Shaders.disableTexture2D();
            }

            Vec3 vec3 = this.theWorld.getSkyColor(this.mc.getRenderViewEntity(), partialTicks);
            vec3 = CustomColors.getSkyColor(vec3, this.mc.theWorld, this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().posY + 1.0D, this.mc.getRenderViewEntity().posZ);

            if (flag1) {
                Shaders.setSkyColor(vec3);
            }

            float f = (float) vec3.xCoord;
            float f1 = (float) vec3.yCoord;
            float f2 = (float) vec3.zCoord;

            if (pass != 2) {
                float f3 = (f * 30.0F + f1 * 59.0F + f2 * 11.0F) / 100.0F;
                float f4 = (f * 30.0F + f1 * 70.0F) / 100.0F;
                float f5 = (f * 30.0F + f2 * 70.0F) / 100.0F;
                f = f3;
                f1 = f4;
                f2 = f5;
            }
            GlStateManager.color(f, f1, f2);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            GlStateManager.depthMask(false);
            GlStateManager.enableFog();

            if (flag1) {
                Shaders.enableFog();
            }

            GlStateManager.color(f, f1, f2);

            if (flag1) {
                Shaders.preSkyList();
            }

            if (Config.isSkyEnabled()) {
                if (this.vboEnabled) {
                    this.skyVBO.bindBuffer();
                    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    GL11.glVertexPointer(3, GL11.GL_FLOAT, 12, 0L);
                    this.skyVBO.drawArrays(7);
                    this.skyVBO.unbindBuffer();
                    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                } else {
                    GlStateManager.callList(this.glSkyList);
                }
            }

            GlStateManager.disableFog();

            if (flag1) {
                Shaders.disableFog();
            }

            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.disableStandardItemLighting();

            if (Config.isSunMoonEnabled()) {
                float[] afloat = this.theWorld.provider.calcSunriseSunsetColors(this.theWorld.getCelestialAngle(partialTicks), partialTicks);
                if (afloat != null) {
                    GlStateManager.disableTexture2D();

                    if (flag1) {
                        Shaders.disableTexture2D();
                    }

                    GlStateManager.shadeModel(7425);
                    GlStateManager.pushMatrix();
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(MathHelper.sin(this.theWorld.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                    float f6 = afloat[0];
                    float f7 = afloat[1];
                    float f8 = afloat[2];

                    if (pass != 2) {
                        float f9 = (f6 * 30.0F + f7 * 59.0F + f8 * 11.0F) / 100.0F;
                        float f10 = (f6 * 30.0F + f7 * 70.0F) / 100.0F;
                        float f11 = (f6 * 30.0F + f8 * 70.0F) / 100.0F;
                        f6 = f9;
                        f7 = f10;
                        f8 = f11;
                    }

                    worldrenderer.begin(6, DefaultVertexFormats.POSITION_COLOR);
                    worldrenderer.pos(0.0D, 100.0D, 0.0D).color(f6, f7, f8, afloat[3]).endVertex();

                    for (int i = 0; i <= 16; ++i) {
                        float f20 = (float) i * (float) Math.PI * 2.0F / 16.0F;
                        float f12 = MathHelper.sin(f20);
                        float f13 = MathHelper.cos(f20);
                        worldrenderer.pos((f12 * 120.0F), (f13 * 120.0F), (-f13 * 40.0F * afloat[3])).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
                    }

                    tessellator.draw();
                    GlStateManager.popMatrix();
                    GlStateManager.shadeModel(7424);
                }
            }

            GlStateManager.enableTexture2D();

            if (flag1) {
                Shaders.enableTexture2D();
            }

            GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
            GlStateManager.pushMatrix();
            float f15 = 1.0F - this.theWorld.getRainStrength(partialTicks);
            GlStateManager.color(1.0F, 1.0F, 1.0F, f15);
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            CustomSky.renderSky(this.theWorld, this.renderEngine, this.theWorld.getCelestialAngle(partialTicks), f15);

            if (flag1) {
                Shaders.preCelestialRotate();
            }

            GlStateManager.rotate(this.theWorld.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);

            if (flag1) {
                Shaders.postCelestialRotate();
            }

            float f16 = 30.0F;

            if (Config.isSunTexture()) {
                this.renderEngine.bindTexture(locationSunPng);
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
                worldrenderer.pos((-f16), 100.0D, (-f16)).tex(0.0D, 0.0D).endVertex();
                worldrenderer.pos(f16, 100.0D, (-f16)).tex(1.0D, 0.0D).endVertex();
                worldrenderer.pos(f16, 100.0D, f16).tex(1.0D, 1.0D).endVertex();
                worldrenderer.pos((-f16), 100.0D, f16).tex(0.0D, 1.0D).endVertex();
                tessellator.draw();
            }

            f16 = 20.0F;

            if (Config.isMoonTexture()) {
                this.renderEngine.bindTexture(locationMoonPhasesPng);
                int l = this.theWorld.getMoonPhase();
                int j = l % 4;
                int k = l / 4 % 2;
                float f21 = (float) (j) / 4.0F;
                float f22 = (float) (k) / 2.0F;
                float f23 = (float) (j + 1) / 4.0F;
                float f14 = (float) (k + 1) / 2.0F;
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
                worldrenderer.pos((-f16), -100.0D, f16).tex(f23, f14).endVertex();
                worldrenderer.pos(f16, -100.0D, f16).tex(f21, f14).endVertex();
                worldrenderer.pos(f16, -100.0D, (-f16)).tex(f21, f22).endVertex();
                worldrenderer.pos((-f16), -100.0D, (-f16)).tex(f23, f22).endVertex();
                tessellator.draw();
            }

            GlStateManager.disableTexture2D();

            if (flag1) {
                Shaders.disableTexture2D();
            }

            float f24 = this.theWorld.getStarBrightness(partialTicks) * f15;

            if (f24 > 0.0F && Config.isStarsEnabled() && !CustomSky.hasSkyLayers(this.theWorld)) {
                GlStateManager.color(f24, f24, f24, f24);

                if (this.vboEnabled) {
                    this.starVBO.bindBuffer();
                    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    GL11.glVertexPointer(3, GL11.GL_FLOAT, 12, 0L);
                    this.starVBO.drawArrays(7);
                    this.starVBO.unbindBuffer();
                    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                } else {
                    GlStateManager.callList(this.starGLCallList);
                }
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableFog();

            if (flag1) {
                Shaders.enableFog();
            }

            GlStateManager.popMatrix();
            GlStateManager.disableTexture2D();

            if (flag1) {
                Shaders.disableTexture2D();
            }

            GlStateManager.color(0.0F, 0.0F, 0.0F);
            double d0 = this.mc.thePlayer.getPositionEyes(partialTicks).yCoord - this.theWorld.getHorizon();

            if (d0 < 0.0D) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0F, 12.0F, 0.0F);

                if (this.vboEnabled) {
                    this.sky2VBO.bindBuffer();
                    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    GL11.glVertexPointer(3, GL11.GL_FLOAT, 12, 0L);
                    this.sky2VBO.drawArrays(7);
                    this.sky2VBO.unbindBuffer();
                    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                } else {
                    GlStateManager.callList(this.glSkyList2);
                }

                GlStateManager.popMatrix();
                float f18 = -((float) (d0 + 65.0D));
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                worldrenderer.pos(-1.0D, f18, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, f18, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, f18, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, f18, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, f18, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, f18, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, f18, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, f18, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
            }

            if (this.theWorld.provider.isSkyColored()) {
                GlStateManager.color(f * 0.2F + 0.04F, f1 * 0.2F + 0.04F, f2 * 0.6F + 0.1F);
            } else {
                GlStateManager.color(f, f1, f2);
            }

            if (this.mc.gameSettings.renderDistanceChunks <= 4) {
                GlStateManager.color(this.mc.entityRenderer.fogColorRed, this.mc.entityRenderer.fogColorGreen, this.mc.entityRenderer.fogColorBlue);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, -((float) (d0 - 16.0D)), 0.0F);

            if (Config.isSkyEnabled()) {
                GlStateManager.callList(this.glSkyList2);
            }

            GlStateManager.popMatrix();
            GlStateManager.enableTexture2D();

            if (flag1) {
                Shaders.enableTexture2D();
            }

            GlStateManager.depthMask(true);
        }
    }
    public int getCountLoadedChunks() {
        if (this.theWorld == null) {
            return 0;
        } else {
            IChunkProvider ichunkprovider = this.theWorld.getChunkProvider();
            if (ichunkprovider == null) {
                return 0;
            } else {
                if (ichunkprovider != this.worldChunkProvider) {
                    this.worldChunkProvider = ichunkprovider;
                    this.worldChunkProviderMap = (LongHashMap) Reflector.getFieldValue(ichunkprovider, Reflector.ChunkProviderClient_chunkMapping);
                }
                return this.worldChunkProviderMap == null ? 0 : this.worldChunkProviderMap.getNumHashElements();
            }
        }
    }
    public void renderClouds(float partialTicks, int pass) {
        if (!Config.isCloudsOff()) {

            if (this.mc.theWorld.provider.isSurfaceWorld()) {
                if (Config.isShaders()) {
                    Shaders.beginClouds();
                }

                if (Config.isCloudsFancy()) {
                    this.renderCloudsFancy(partialTicks, pass);
                } else {
                    this.cloudRenderer.prepareToRender(false, this.cloudTickCounter, partialTicks);
                    partialTicks = 0.0F;
                    GlStateManager.disableCull();
                    float f9 = (float) (this.mc.getRenderViewEntity().lastTickPosY + (this.mc.getRenderViewEntity().posY - this.mc.getRenderViewEntity().lastTickPosY) * (double) partialTicks);
                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                    this.renderEngine.bindTexture(locationCloudsPng);
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

                    if (this.cloudRenderer.shouldUpdateGlList()) {
                        this.cloudRenderer.startUpdateGlList();
                        Vec3 vec3 = this.theWorld.getCloudColour(partialTicks);
                        float f = (float) vec3.xCoord;
                        float f1 = (float) vec3.yCoord;
                        float f2 = (float) vec3.zCoord;

                        if (pass != 2) {
                            float f3 = (f * 30.0F + f1 * 59.0F + f2 * 11.0F) / 100.0F;
                            float f4 = (f * 30.0F + f1 * 70.0F) / 100.0F;
                            float f5 = (f * 30.0F + f2 * 70.0F) / 100.0F;
                            f = f3;
                            f1 = f4;
                            f2 = f5;
                        }

                        double d2 = ((float) this.cloudTickCounter + partialTicks);
                        double d0 = this.mc.getRenderViewEntity().prevPosX + (this.mc.getRenderViewEntity().posX - this.mc.getRenderViewEntity().prevPosX) * (double) partialTicks + d2 * 0.029999999329447746D;
                        double d1 = this.mc.getRenderViewEntity().prevPosZ + (this.mc.getRenderViewEntity().posZ - this.mc.getRenderViewEntity().prevPosZ) * (double) partialTicks;
                        int i = MathHelper.floor_double(d0 / 2048.0D);
                        int j = MathHelper.floor_double(d1 / 2048.0D);
                        d0 = d0 - (double) (i * 2048);
                        d1 = d1 - (double) (j * 2048);
                        float f6 = this.theWorld.provider.getCloudHeight() - f9 + 0.33F;
                        f6 = f6 + this.mc.gameSettings.ofCloudsHeight * 128.0F;
                        float f7 = (float) (d0 * 4.8828125E-4D);
                        float f8 = (float) (d1 * 4.8828125E-4D);
                        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

                        for (int k = -256; k < 256; k += 32) {
                            for (int l = -256; l < 256; l += 32) {
                                worldrenderer.pos((k), f6, (l + 32)).tex(((float) (k) * 4.8828125E-4F + f7), ((float) (l + 32) * 4.8828125E-4F + f8)).color(f, f1, f2, 0.8F).endVertex();
                                worldrenderer.pos((k + 32), f6, (l + 32)).tex(((float) (k + 32) * 4.8828125E-4F + f7), ((float) (l + 32) * 4.8828125E-4F + f8)).color(f, f1, f2, 0.8F).endVertex();
                                worldrenderer.pos((k + 32), f6, (l)).tex(((float) (k + 32) * 4.8828125E-4F + f7), ((float) (l) * 4.8828125E-4F + f8)).color(f, f1, f2, 0.8F).endVertex();
                                worldrenderer.pos((k), f6, (l)).tex(((float) (k) * 4.8828125E-4F + f7), ((float) (l) * 4.8828125E-4F + f8)).color(f, f1, f2, 0.8F).endVertex();
                            }
                        }

                        tessellator.draw();
                        this.cloudRenderer.endUpdateGlList();
                    }

                    this.cloudRenderer.renderGlList();
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.disableBlend();
                    GlStateManager.enableCull();
                }

                if (Config.isShaders()) {
                    Shaders.endClouds();
                }
            }
        }
    }

    /**
     * Checks if the given position is to be rendered with cloud fog
     */
    public boolean hasCloudFog()
    {
        return false;
    }

    private void renderCloudsFancy(float partialTicks, int pass) {
        if (Config.isCloudsOff() || Minecraft.getMinecraft().gameSettings.ofClouds == 0 || Minecraft.getMinecraft().gameSettings.clouds == 0) {
            return;
        }
        this.cloudRenderer.prepareToRender(true, this.cloudTickCounter, partialTicks);
        partialTicks = 0.0F;
        GlStateManager.disableCull();
        float f = (float) (this.mc.getRenderViewEntity().lastTickPosY + (this.mc.getRenderViewEntity().posY - this.mc.getRenderViewEntity().lastTickPosY) * (double) partialTicks);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        double d0 = ((float) this.cloudTickCounter + partialTicks);
        double d1 = (this.mc.getRenderViewEntity().prevPosX + (this.mc.getRenderViewEntity().posX - this.mc.getRenderViewEntity().prevPosX) * (double) partialTicks + d0 * 0.029999999329447746D) / 12.0D;
        double d2 = (this.mc.getRenderViewEntity().prevPosZ + (this.mc.getRenderViewEntity().posZ - this.mc.getRenderViewEntity().prevPosZ) * (double) partialTicks) / 12.0D + 0.33000001311302185D;
        float f3 = this.theWorld.provider.getCloudHeight() - f + 0.33F;
        f3 = f3 + this.mc.gameSettings.ofCloudsHeight * 128.0F;
        int i = MathHelper.floor_double(d1 / 2048.0D);
        int j = MathHelper.floor_double(d2 / 2048.0D);
        d1 = d1 - (double) (i * 2048);
        d2 = d2 - (double) (j * 2048);
        this.renderEngine.bindTexture(locationCloudsPng);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Vec3 vec3 = this.theWorld.getCloudColour(partialTicks);
        float f4 = (float) vec3.xCoord;
        float f5 = (float) vec3.yCoord;
        float f6 = (float) vec3.zCoord;

        if (pass != 2) {
            float f7 = (f4 * 30.0F + f5 * 59.0F + f6 * 11.0F) / 100.0F;
            float f8 = (f4 * 30.0F + f5 * 70.0F) / 100.0F;
            float f9 = (f4 * 30.0F + f6 * 70.0F) / 100.0F;
            f4 = f7;
            f5 = f8;
            f6 = f9;
        }

        float f26 = f4 * 0.9F;
        float f27 = f5 * 0.9F;
        float f28 = f6 * 0.9F;
        float f10 = f4 * 0.7F;
        float f11 = f5 * 0.7F;
        float f12 = f6 * 0.7F;
        float f13 = f4 * 0.8F;
        float f14 = f5 * 0.8F;
        float f15 = f6 * 0.8F;
        float f17 = (float) MathHelper.floor_double(d1) * 0.00390625F;
        float f18 = (float) MathHelper.floor_double(d2) * 0.00390625F;
        float f19 = (float) (d1 - (double) MathHelper.floor_double(d1));
        float f20 = (float) (d2 - (double) MathHelper.floor_double(d2));
        GlStateManager.scale(12.0F, 1.0F, 12.0F);

        for (int k = 0; k < 2; ++k) {
            if (k == 0) {
                GlStateManager.colorMask(false, false, false, false);
            } else {
                switch (pass) {
                    case 0 -> GlStateManager.colorMask(false, true, true, true);
                    case 1 -> GlStateManager.colorMask(true, false, false, true);
                    case 2 -> GlStateManager.colorMask(true, true, true, true);
                }
            }

            this.cloudRenderer.renderGlList();
        }

        if (this.cloudRenderer.shouldUpdateGlList()) {
            this.cloudRenderer.startUpdateGlList();

            for (int j1 = -3; j1 <= 4; ++j1) {
                for (int l = -3; l <= 4; ++l) {
                    worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
                    float f22 = (float) (j1 * 8);
                    float f23 = (float) (l * 8);
                    float f24 = f22 - f19;
                    float f25 = f23 - f20;

                    if (f3 > -5.0F) {
                        worldrenderer.pos((f24 + 0.0F), (f3 + 0.0F), (f25 + 8.0F)).tex(((f22 + 0.0F) * 0.00390625F + f17), ((f23 + 8.0F) * 0.00390625F + f18)).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        worldrenderer.pos((f24 + 8.0F), (f3 + 0.0F), (f25 + 8.0F)).tex(((f22 + 8.0F) * 0.00390625F + f17), ((f23 + 8.0F) * 0.00390625F + f18)).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        worldrenderer.pos((f24 + 8.0F), (f3 + 0.0F), (f25 + 0.0F)).tex(((f22 + 8.0F) * 0.00390625F + f17), ((f23 + 0.0F) * 0.00390625F + f18)).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        worldrenderer.pos((f24 + 0.0F), (f3 + 0.0F), (f25 + 0.0F)).tex(((f22 + 0.0F) * 0.00390625F + f17), ((f23 + 0.0F) * 0.00390625F + f18)).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                    }

                    if (f3 <= 5.0F) {
                        worldrenderer.pos((f24 + 0.0F), (f3 + 4.0F - 9.765625E-4F), (f25 + 8.0F)).tex(((f22 + 0.0F) * 0.00390625F + f17), ((f23 + 8.0F) * 0.00390625F + f18)).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        worldrenderer.pos((f24 + 8.0F), (f3 + 4.0F - 9.765625E-4F), (f25 + 8.0F)).tex(((f22 + 8.0F) * 0.00390625F + f17), ((f23 + 8.0F) * 0.00390625F + f18)).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        worldrenderer.pos((f24 + 8.0F), (f3 + 4.0F - 9.765625E-4F), (f25 + 0.0F)).tex(((f22 + 8.0F) * 0.00390625F + f17), ((f23 + 0.0F) * 0.00390625F + f18)).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        worldrenderer.pos((f24 + 0.0F), (f3 + 4.0F - 9.765625E-4F), (f25 + 0.0F)).tex(((f22 + 0.0F) * 0.00390625F + f17), ((f23 + 0.0F) * 0.00390625F + f18)).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                    }

                    if (j1 > -1) {
                        for (int i1 = 0; i1 < 8; ++i1) {
                            worldrenderer.pos((f24 + (float) i1 + 0.0F), (f3 + 0.0F), (f25 + 8.0F)).tex(((f22 + (float) i1 + 0.5F) * 0.00390625F + f17), ((f23 + 8.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos((f24 + (float) i1 + 0.0F), (f3 + 4.0F), (f25 + 8.0F)).tex(((f22 + (float) i1 + 0.5F) * 0.00390625F + f17), ((f23 + 8.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos((f24 + (float) i1 + 0.0F), (f3 + 4.0F), (f25 + 0.0F)).tex(((f22 + (float) i1 + 0.5F) * 0.00390625F + f17), ((f23 + 0.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos((f24 + (float) i1 + 0.0F), (f3 + 0.0F), (f25 + 0.0F)).tex(((f22 + (float) i1 + 0.5F) * 0.00390625F + f17), ((f23 + 0.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                        }
                    }

                    if (j1 <= 1) {
                        for (int k1 = 0; k1 < 8; ++k1) {
                            worldrenderer.pos((f24 + (float) k1 + 1.0F - 9.765625E-4F), (f3 + 0.0F), (f25 + 8.0F)).tex(((f22 + (float) k1 + 0.5F) * 0.00390625F + f17), ((f23 + 8.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos((f24 + (float) k1 + 1.0F - 9.765625E-4F), (f3 + 4.0F), (f25 + 8.0F)).tex(((f22 + (float) k1 + 0.5F) * 0.00390625F + f17), ((f23 + 8.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos((f24 + (float) k1 + 1.0F - 9.765625E-4F), (f3 + 4.0F), (f25 + 0.0F)).tex(((f22 + (float) k1 + 0.5F) * 0.00390625F + f17), ((f23 + 0.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos((f24 + (float) k1 + 1.0F - 9.765625E-4F), (f3 + 0.0F), (f25 + 0.0F)).tex(((f22 + (float) k1 + 0.5F) * 0.00390625F + f17), ((f23 + 0.0F) * 0.00390625F + f18)).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                        }
                    }

                    if (l > -1) {
                        for (int l1 = 0; l1 < 8; ++l1) {
                            worldrenderer.pos((f24 + 0.0F), (f3 + 4.0F), (f25 + (float) l1 + 0.0F)).tex(((f22 + 0.0F) * 0.00390625F + f17), ((f23 + (float) l1 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            worldrenderer.pos((f24 + 8.0F), (f3 + 4.0F), (f25 + (float) l1 + 0.0F)).tex(((f22 + 8.0F) * 0.00390625F + f17), ((f23 + (float) l1 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            worldrenderer.pos((f24 + 8.0F), (f3 + 0.0F), (f25 + (float) l1 + 0.0F)).tex(((f22 + 8.0F) * 0.00390625F + f17), ((f23 + (float) l1 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            worldrenderer.pos((f24 + 0.0F), (f3 + 0.0F), (f25 + (float) l1 + 0.0F)).tex(((f22 + 0.0F) * 0.00390625F + f17), ((f23 + (float) l1 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                        }
                    }

                    if (l <= 1) {
                        for (int i2 = 0; i2 < 8; ++i2) {
                            worldrenderer.pos((f24 + 0.0F), (f3 + 4.0F), (f25 + (float) i2 + 1.0F - 9.765625E-4F)).tex(((f22 + 0.0F) * 0.00390625F + f17), ((f23 + (float) i2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            worldrenderer.pos((f24 + 8.0F), (f3 + 4.0F), (f25 + (float) i2 + 1.0F - 9.765625E-4F)).tex(((f22 + 8.0F) * 0.00390625F + f17), ((f23 + (float) i2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            worldrenderer.pos((f24 + 8.0F), (f3 + 0.0F), (f25 + (float) i2 + 1.0F - 9.765625E-4F)).tex(((f22 + 8.0F) * 0.00390625F + f17), ((f23 + (float) i2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            worldrenderer.pos((f24 + 0.0F), (f3 + 0.0F), (f25 + (float) i2 + 1.0F - 9.765625E-4F)).tex(((f22 + 0.0F) * 0.00390625F + f17), ((f23 + (float) i2 + 0.5F) * 0.00390625F + f18)).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                        }
                    }

                    tessellator.draw();
                }
            }

            this.cloudRenderer.endUpdateGlList();
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
    }

    public AsyncChunkBuilder renderDispatcher = new AsyncChunkBuilder();

    public void updateChunks(long finishTimeNano) {
        boolean shouldRun = this.renderDispatcher.runChunkUploads();
        this.displayListEntitiesDirty |= shouldRun;

        if (!this.chunksToUpdate.isEmpty()) {
            Iterator<RenderChunk> iterator = this.chunksToUpdate.iterator();

            while (iterator.hasNext()) {
                RenderChunk renderchunk = iterator.next();

                this.renderDispatcher.updateChunk(ChunkBuildType.REBUILD_CHUNK, renderchunk);

                this.chunksToResortTransparency.remove(renderchunk);
                iterator.remove();
            }
        }

        if (!this.chunksToResortTransparency.isEmpty()) {
            Iterator<RenderChunk> iterator2 = this.chunksToResortTransparency.iterator();
            if (iterator2.hasNext()) {
                RenderChunk renderchunk2 = iterator2.next();
                this.renderDispatcher.updateChunk(ChunkBuildType.RESORT_TRANSPARENCY, renderchunk2);
                iterator2.remove();
            }
        }
    }

    public void renderWorldBorder(Entity p_180449_1_, float partialTicks) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        WorldBorder worldborder = this.theWorld.getWorldBorder();
        double d0 = (this.mc.gameSettings.renderDistanceChunks * 16);

        if (p_180449_1_.posX >= worldborder.maxX() - d0 || p_180449_1_.posX <= worldborder.minX() + d0 || p_180449_1_.posZ >= worldborder.maxZ() - d0 || p_180449_1_.posZ <= worldborder.minZ() + d0) {
            double d1 = 1.0D - worldborder.getClosestDistance(p_180449_1_) / d0;
            d1 = Math.pow(d1, 4.0D);
            float d2 = (float) (p_180449_1_.lastTickPosX + (p_180449_1_.posX - p_180449_1_.lastTickPosX) * partialTicks);
            float d3 = (float) (p_180449_1_.lastTickPosY + (p_180449_1_.posY - p_180449_1_.lastTickPosY) * partialTicks);
            float d4 = (float) (p_180449_1_.lastTickPosZ + (p_180449_1_.posZ - p_180449_1_.lastTickPosZ) * partialTicks);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
            this.renderEngine.bindTexture(locationForcefieldPng);
            GlStateManager.depthMask(false);
            GlStateManager.pushMatrix();
            int i = worldborder.getStatus().getID();
            float f = (float) (i >> 16 & 255) / 255.0F;
            float f1 = (float) (i >> 8 & 255) / 255.0F;
            float f2 = (float) (i & 255) / 255.0F;
            GlStateManager.color(f, f1, f2, (float) d1);
            GlStateManager.doPolygonOffset(-3.0F, -3.0F);
            GlStateManager.enablePolygonOffset();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableAlpha();
            GlStateManager.disableCull();
            float f3 = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F;
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.setTranslation(-d2, -d3, -d4);
            double d5 = Math.max(MathHelper.floor_double(d4 - d0), worldborder.minZ());
            double d6 = Math.min(MathHelper.ceiling_double_int(d4 + d0), worldborder.maxZ());

            if (d2 > worldborder.maxX() - d0) {
                float f7 = 0.0F;

                for (double d7 = d5; d7 < d6; f7 += 0.5F) {
                    double d8 = Math.min(1.0D, d6 - d7);
                    float f8 = (float) d8 * 0.5F;
                    worldrenderer.pos(worldborder.maxX(), 256.0D, d7).tex((f3 + f7), (f3 + 0.0F)).endVertex();
                    worldrenderer.pos(worldborder.maxX(), 256.0D, d7 + d8).tex((f3 + f8 + f7), (f3 + 0.0F)).endVertex();
                    worldrenderer.pos(worldborder.maxX(), 0.0D, d7 + d8).tex((f3 + f8 + f7), (f3 + 128.0F)).endVertex();
                    worldrenderer.pos(worldborder.maxX(), 0.0D, d7).tex((f3 + f7), (f3 + 128.0F)).endVertex();
                    ++d7;
                }
            }

            if (d2 < worldborder.minX() + d0) {
                float f9 = 0.0F;

                for (double d9 = d5; d9 < d6; f9 += 0.5F) {
                    double d12 = Math.min(1.0D, d6 - d9);
                    float f12 = (float) d12 * 0.5F;
                    worldrenderer.pos(worldborder.minX(), 256.0D, d9).tex((f3 + f9), (f3 + 0.0F)).endVertex();
                    worldrenderer.pos(worldborder.minX(), 256.0D, d9 + d12).tex((f3 + f12 + f9), (f3 + 0.0F)).endVertex();
                    worldrenderer.pos(worldborder.minX(), 0.0D, d9 + d12).tex((f3 + f12 + f9), (f3 + 128.0F)).endVertex();
                    worldrenderer.pos(worldborder.minX(), 0.0D, d9).tex((f3 + f9), (f3 + 128.0F)).endVertex();
                    ++d9;
                }
            }

            d5 = Math.max(MathHelper.floor_double(d2 - d0), worldborder.minX());
            d6 = Math.min(MathHelper.ceiling_double_int(d2 + d0), worldborder.maxX());

            if (d4 > worldborder.maxZ() - d0) {
                float f10 = 0.0F;

                for (double d10 = d5; d10 < d6; f10 += 0.5F) {
                    double d13 = Math.min(1.0D, d6 - d10);
                    float f13 = (float) d13 * 0.5F;
                    worldrenderer.pos(d10, 256.0D, worldborder.maxZ()).tex((f3 + f10), (f3 + 0.0F)).endVertex();
                    worldrenderer.pos(d10 + d13, 256.0D, worldborder.maxZ()).tex((f3 + f13 + f10), (f3 + 0.0F)).endVertex();
                    worldrenderer.pos(d10 + d13, 0.0D, worldborder.maxZ()).tex((f3 + f13 + f10), (f3 + 128.0F)).endVertex();
                    worldrenderer.pos(d10, 0.0D, worldborder.maxZ()).tex((f3 + f10), (f3 + 128.0F)).endVertex();
                    ++d10;
                }
            }

            if (d4 < worldborder.minZ() + d0) {
                float f11 = 0.0F;

                for (double d11 = d5; d11 < d6; f11 += 0.5F) {
                    double d14 = Math.min(1.0D, d6 - d11);
                    float f14 = (float) d14 * 0.5F;
                    worldrenderer.pos(d11, 256.0D, worldborder.minZ()).tex((f3 + f11), (f3 + 0.0F)).endVertex();
                    worldrenderer.pos(d11 + d14, 256.0D, worldborder.minZ()).tex((f3 + f14 + f11), (f3 + 0.0F)).endVertex();
                    worldrenderer.pos(d11 + d14, 0.0D, worldborder.minZ()).tex((f3 + f14 + f11), (f3 + 128.0F)).endVertex();
                    worldrenderer.pos(d11, 0.0D, worldborder.minZ()).tex((f3 + f11), (f3 + 128.0F)).endVertex();
                    ++d11;
                }
            }

            tessellator.draw();
            worldrenderer.setTranslation(0.0f, 0.0f, 0.0f);
            GlStateManager.enableCull();
            GlStateManager.disableAlpha();
            GlStateManager.doPolygonOffset(0.0F, 0.0F);
            GlStateManager.disablePolygonOffset();
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
            GlStateManager.depthMask(true);
        }
    }

    private void preRenderDamagedBlocks() {
        GlStateManager.tryBlendFuncSeparate(774, 768, 1, 0);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
        GlStateManager.doPolygonOffset(-3.0F, -3.0F);
        GlStateManager.enablePolygonOffset();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();

        if (Config.isShaders()) {
            ShadersRender.beginBlockDamage();
        }
    }

    private void postRenderDamagedBlocks() {
        GlStateManager.disableAlpha();
        GlStateManager.doPolygonOffset(0.0F, 0.0F);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();

        if (Config.isShaders()) {
            ShadersRender.endBlockDamage();
        }
    }

    public void drawBlockDamageTexture(Tessellator tessellatorIn, WorldRenderer worldRendererIn, Entity entityIn, float partialTicks) {
        double d0 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * partialTicks;
        double d1 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * partialTicks;
        double d2 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * partialTicks;

        if (!this.damagedBlocks.isEmpty()) {
            //GlStateManager.forceBindTexture(TextureMap.locationBlocksTexture.textureId.getGlTextureId());
            mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            this.preRenderDamagedBlocks();
            worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
            worldRendererIn.setTranslation(-d0, -d1, -d2);
            worldRendererIn.markDirty();
            Iterator<DestroyBlockProgress> iterator = this.damagedBlocks.values().iterator();

            while (iterator.hasNext()) {
                DestroyBlockProgress destroyblockprogress = iterator.next();
                BlockPos blockpos = destroyblockprogress.getPosition();
                double d3 = (double) blockpos.getX() - d0;
                double d4 = (double) blockpos.getY() - d1;
                double d5 = (double) blockpos.getZ() - d2;
                Block block = this.theWorld.getBlockState(blockpos).getBlock();
                boolean flag;
                flag = !(block instanceof BlockChest) && !(block instanceof BlockEnderChest) && !(block instanceof BlockSign) && !(block instanceof BlockSkull);

                if (flag) {
                    if (d3 * d3 + d4 * d4 + d5 * d5 > 1024.0D) {
                        iterator.remove();
                    } else {
                        IBlockState iblockstate = this.theWorld.getBlockState(blockpos);

                        if (iblockstate.getBlock().getMaterial() != Material.air) {
                            int i = destroyblockprogress.getPartialBlockDamage();
                            TextureAtlasSprite textureatlassprite = this.destroyBlockIcons[i];
                            BlockRendererDispatcher blockrendererdispatcher = this.mc.getBlockRendererDispatcher();
                            blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, textureatlassprite, this.theWorld);
                        }
                    }
                }
            }

            tessellatorIn.draw();
            worldRendererIn.setTranslation(0.0f, 0.0f, 0.0f);
            this.postRenderDamagedBlocks();
        }
    }

    /**
     * Draws the selection box for the player. Args: entityPlayer, rayTraceHit, i, itemStack, partialTickTime
     */
    public void drawSelectionBox(EntityPlayer player, MovingObjectPosition movingObjectPositionIn, int p_72731_3_, float partialTicks) {
        if (p_72731_3_ == 0 && movingObjectPositionIn.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            GlStateManager.enableBlend();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(770,771);
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
            GL11.glLineWidth(2.0F);
            GlStateManager.disableTexture2D();

            if (Config.isShaders()) {
                Shaders.disableTexture2D();
            }

            GlStateManager.depthMask(false);
            BlockPos blockpos = movingObjectPositionIn.getBlockPos();
            Block block = this.theWorld.getBlockState(blockpos).getBlock();

            if (block.getMaterial() != Material.air) {
                block.setBlockBoundsBasedOnState(this.theWorld, blockpos);
                double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
                double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
                double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;
                var selectionBox = block.getSelectedBoundingBox(this.theWorld, blockpos);
                if (selectionBox != null) {
                    func_181561_a(selectionBox.expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D).offset(-d0, -d1, -d2));
                }
            }

            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();

            if (Config.isShaders()) {
                Shaders.enableTexture2D();
            }

            GlStateManager.disableBlend();
        }
    }

    public static void func_181561_a(AxisAlignedBB p_181561_0_) {
        if (p_181561_0_ == null) {
            return;
        }
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(1, DefaultVertexFormats.POSITION);
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
        tessellator.draw();
    }

    public static void func_181563_a(AxisAlignedBB p_181563_0_, int p_181563_1_, int p_181563_2_, int p_181563_3_, int p_181563_4_) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.minY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        tessellator.draw();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.maxY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        tessellator.draw();
        worldrenderer.begin(1, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.minY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.maxY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        tessellator.draw();
    }

    /**
     * Marks the blocks in the given range for update
     */
    private void markBlocksForUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.viewFrustum.markBlocksForUpdate(x1, y1, z1, x2, y2, z2);
    }

    public void markBlockForUpdate(BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        this.markBlocksForUpdate(i - 1, j - 1, k - 1, i + 1, j + 1, k + 1);
    }

    public void notifyLightSet(BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        this.markBlocksForUpdate(i - 1, j - 1, k - 1, i + 1, j + 1, k + 1);
    }

    /**
     * On the client, re-renders all blocks in this range, inclusive. On the server, does nothing. Args: min x, min y,
     * min z, max x, max y, max z
     */
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.markBlocksForUpdate(x1 - 1, y1 - 1, z1 - 1, x2 + 1, y2 + 1, z2 + 1);
    }

    public void playRecord(String recordName, BlockPos blockPosIn) {
        ISound isound = this.mapSoundPositions.get(blockPosIn);

        if (isound != null) {
            this.mc.getSoundHandler().stopSound(isound);
            this.mapSoundPositions.remove(blockPosIn);
        }

        if (recordName != null) {
            ItemRecord itemrecord = ItemRecord.getRecord(recordName);

            if (itemrecord != null) {
                this.mc.ingameGUI.setRecordPlayingMessage(itemrecord.getRecordNameLocal());
            }

            ResourceLocation resourcelocation = new ResourceLocation(recordName);

            PositionedSoundRecord positionedsoundrecord = PositionedSoundRecord.create(resourcelocation, (float) blockPosIn.getX(), (float) blockPosIn.getY(), (float) blockPosIn.getZ());
            this.mapSoundPositions.put(blockPosIn, positionedsoundrecord);
            this.mc.getSoundHandler().playSound(positionedsoundrecord);
        }
    }

    /**
     * Plays the specified sound. Arg: soundName, x, y, z, volume, pitch
     */
    public void playSound(String soundName, double x, double y, double z, float volume, float pitch)
    {
    }

    /**
     * Plays sound to all near players except the player reference given
     */
    public void playSoundToNearExcept(EntityPlayer except, String soundName, double x, double y, double z, float volume, float pitch)
    {
    }

    public void spawnParticle(int particleID, boolean ignoreRange, final double xCoord, final double yCoord, final double zCoord, double xOffset, double yOffset, double zOffset, int... p_180442_15_) {
        try {
            this.spawnEntityFX(particleID, ignoreRange, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, p_180442_15_);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while adding particle");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being added");
            crashreportcategory.addCrashSection("ID", particleID);

            if (p_180442_15_ != null) {
                crashreportcategory.addCrashSection("Parameters", p_180442_15_);
            }

            crashreportcategory.addCrashSectionCallable("Position", () -> CrashReportCategory.getCoordinateInfo(xCoord, yCoord, zCoord));
            throw new ReportedException(crashreport);
        }
    }

    private void spawnParticle(EnumParticleTypes particleIn, double p_174972_2_, double p_174972_4_, double p_174972_6_, double p_174972_8_, double p_174972_10_, double p_174972_12_, int... p_174972_14_) {
        this.spawnParticle(particleIn.getParticleID(), particleIn.getShouldIgnoreRange(), p_174972_2_, p_174972_4_, p_174972_6_, p_174972_8_, p_174972_10_, p_174972_12_, p_174972_14_);
    }

    private EntityFX spawnEntityFX(int p_174974_1_, boolean ignoreRange, double p_174974_3_, double p_174974_5_, double p_174974_7_, double p_174974_9_, double p_174974_11_, double p_174974_13_, int... p_174974_15_) {
        if (this.mc != null && this.mc.getRenderViewEntity() != null && this.mc.effectRenderer != null) {
            int i = this.mc.gameSettings.particleSetting;

            if (i == 1 && this.theWorld.rand.nextInt(3) == 0) {
                i = 2;
            }

            double d0 = this.mc.getRenderViewEntity().posX - p_174974_3_;
            double d1 = this.mc.getRenderViewEntity().posY - p_174974_5_;
            double d2 = this.mc.getRenderViewEntity().posZ - p_174974_7_;

            if (p_174974_1_ == EnumParticleTypes.EXPLOSION_HUGE.getParticleID() && !Config.isAnimatedExplosion()) {
                return null;
            } else if (p_174974_1_ == EnumParticleTypes.EXPLOSION_LARGE.getParticleID() && !Config.isAnimatedExplosion()) {
                return null;
            } else if (p_174974_1_ == EnumParticleTypes.EXPLOSION_NORMAL.getParticleID() && !Config.isAnimatedExplosion()) {
                return null;
            } else if (p_174974_1_ == EnumParticleTypes.SUSPENDED.getParticleID() && !Config.isWaterParticles()) {
                return null;
            } else if (p_174974_1_ == EnumParticleTypes.SUSPENDED_DEPTH.getParticleID() && !Config.isVoidParticles()) {
                return null;
            } else if (p_174974_1_ == EnumParticleTypes.SMOKE_NORMAL.getParticleID() && !Config.isAnimatedSmoke()) {
                return null;
            } else if (p_174974_1_ == EnumParticleTypes.SMOKE_LARGE.getParticleID() && !Config.isAnimatedSmoke()) {
                return null;
            } else if (p_174974_1_ == EnumParticleTypes.SPELL_MOB.getParticleID() && !Config.isPotionParticles()) {
                return null;
            } else if (p_174974_1_ == EnumParticleTypes.SPELL_MOB_AMBIENT.getParticleID() && !Config.isPotionParticles()) {
                return null;
            } else if (p_174974_1_ == EnumParticleTypes.SPELL.getParticleID() && !Config.isPotionParticles()) {
                return null;
            } else if (p_174974_1_ == EnumParticleTypes.SPELL_INSTANT.getParticleID() && !Config.isPotionParticles()) {
                return null;
            } else if (p_174974_1_ == EnumParticleTypes.SPELL_WITCH.getParticleID() && !Config.isPotionParticles()) {
                return null;
            } else if (p_174974_1_ == EnumParticleTypes.PORTAL.getParticleID() && !Config.isAnimatedPortal()) {
                return null;
            } else if (p_174974_1_ == EnumParticleTypes.FLAME.getParticleID() && !Config.isAnimatedFlame()) {
                return null;
            } else if (p_174974_1_ == EnumParticleTypes.REDSTONE.getParticleID() && !Config.isAnimatedRedstone()) {
                return null;
            } else if (p_174974_1_ == EnumParticleTypes.DRIP_WATER.getParticleID() && !Config.isDrippingWaterLava()) {
                return null;
            } else if (p_174974_1_ == EnumParticleTypes.DRIP_LAVA.getParticleID() && !Config.isDrippingWaterLava()) {
                return null;
            } else if (p_174974_1_ == EnumParticleTypes.FIREWORKS_SPARK.getParticleID() && !Config.isFireworkParticles()) {
                return null;
            } else if (ignoreRange) {
                return this.mc.effectRenderer.spawnEffectParticle(p_174974_1_, p_174974_3_, p_174974_5_, p_174974_7_, p_174974_9_, p_174974_11_, p_174974_13_, p_174974_15_);
            } else {
                double d4 = 256.0D;

                if (p_174974_1_ == EnumParticleTypes.CRIT.getParticleID()) {
                    d4 = 38416.0D;
                }

                if (d0 * d0 + d1 * d1 + d2 * d2 > d4) {
                    return null;
                } else if (i > 1) {
                    return null;
                } else {
                    EntityFX entityfx = this.mc.effectRenderer.spawnEffectParticle(p_174974_1_, p_174974_3_, p_174974_5_, p_174974_7_, p_174974_9_, p_174974_11_, p_174974_13_, p_174974_15_);

                    if (p_174974_1_ == EnumParticleTypes.WATER_BUBBLE.getParticleID()) {
                        CustomColors.updateWaterFX(entityfx, this.theWorld, p_174974_3_, p_174974_5_, p_174974_7_);
                    }

                    if (p_174974_1_ == EnumParticleTypes.WATER_SPLASH.getParticleID()) {
                        CustomColors.updateWaterFX(entityfx, this.theWorld, p_174974_3_, p_174974_5_, p_174974_7_);
                    }

                    if (p_174974_1_ == EnumParticleTypes.WATER_DROP.getParticleID()) {
                        CustomColors.updateWaterFX(entityfx, this.theWorld, p_174974_3_, p_174974_5_, p_174974_7_);
                    }

                    if (p_174974_1_ == EnumParticleTypes.TOWN_AURA.getParticleID()) {
                        CustomColors.updateMyceliumFX(entityfx);
                    }

                    if (p_174974_1_ == EnumParticleTypes.PORTAL.getParticleID()) {
                        CustomColors.updatePortalFX(entityfx);
                    }

                    if (p_174974_1_ == EnumParticleTypes.REDSTONE.getParticleID()) {
                        CustomColors.updateReddustFX(entityfx, this.theWorld, p_174974_3_, p_174974_5_, p_174974_7_);
                    }

                    return entityfx;
                }
            }
        } else {
            return null;
        }
    }

    /**
     * Called on all IWorldAccesses when an entity is created or loaded. On client worlds, starts downloading any
     * necessary textures. On server worlds, adds the entity to the entity tracker.
     */
    public void onEntityAdded(Entity entityIn) {
        RandomMobs.entityLoaded(entityIn, this.theWorld);

        if (Config.isDynamicLights()) {
            DynamicLights.entityAdded(entityIn, this);
        }
    }

    /**
     * Called on all IWorldAccesses when an entity is unloaded or destroyed. On client worlds, releases any downloaded
     * textures. On server worlds, removes the entity from the entity tracker.
     */
    public void onEntityRemoved(Entity entityIn) {
        if (Config.isDynamicLights()) {
            DynamicLights.entityRemoved(entityIn, this);
        }
    }

    /**
     * Deletes all display lists
     */
    public void deleteAllDisplayLists()
    {
    }

    public void broadcastSound(int p_180440_1_, BlockPos p_180440_2_, int p_180440_3_) {
        switch (p_180440_1_) {
            case 1013:
            case 1018:
                if (this.mc.getRenderViewEntity() != null) {
                    double d0 = (double) p_180440_2_.getX() - this.mc.getRenderViewEntity().posX;
                    double d1 = (double) p_180440_2_.getY() - this.mc.getRenderViewEntity().posY;
                    double d2 = (double) p_180440_2_.getZ() - this.mc.getRenderViewEntity().posZ;
                    double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    double d4 = this.mc.getRenderViewEntity().posX;
                    double d5 = this.mc.getRenderViewEntity().posY;
                    double d6 = this.mc.getRenderViewEntity().posZ;

                    if (d3 > 0.0D) {
                        d4 += d0 / d3 * 2.0D;
                        d5 += d1 / d3 * 2.0D;
                        d6 += d2 / d3 * 2.0D;
                    }

                    if (p_180440_1_ == 1013) {
                        this.theWorld.playSound(d4, d5, d6, "mob.wither.spawn", 1.0F, 1.0F, false);
                    } else {
                        this.theWorld.playSound(d4, d5, d6, "mob.enderdragon.end", 5.0F, 1.0F, false);
                    }
                }

            default:
        }
    }

    public void playAuxSFX(EntityPlayer player, int sfxType, BlockPos blockPosIn, int p_180439_4_) {
        Random random = this.theWorld.rand;

        switch (sfxType) {
            case 1000 -> this.theWorld.playSoundAtPos(blockPosIn, "random.click", 1.0F, 1.0F, false);
            case 1001 -> this.theWorld.playSoundAtPos(blockPosIn, "random.click", 1.0F, 1.2F, false);
            case 1002 -> this.theWorld.playSoundAtPos(blockPosIn, "random.bow", 1.0F, 1.2F, false);
            case 1003 ->
                    this.theWorld.playSoundAtPos(blockPosIn, "random.door_open", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
            case 1004 ->
                    this.theWorld.playSoundAtPos(blockPosIn, "random.fizz", 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);
            case 1005 -> {
                if (Item.getItemById(p_180439_4_) instanceof ItemRecord) {
                    this.theWorld.playRecord(blockPosIn, "records." + ((ItemRecord) Item.getItemById(p_180439_4_)).recordName);
                } else {
                    this.theWorld.playRecord(blockPosIn, null);
                }
            }
            case 1006 ->
                    this.theWorld.playSoundAtPos(blockPosIn, "random.door_close", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
            case 1007 ->
                    this.theWorld.playSoundAtPos(blockPosIn, "mob.ghast.charge", 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            case 1008 ->
                    this.theWorld.playSoundAtPos(blockPosIn, "mob.ghast.fireball", 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            case 1009 ->
                    this.theWorld.playSoundAtPos(blockPosIn, "mob.ghast.fireball", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            case 1010 ->
                    this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.wood", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            case 1011 ->
                    this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.metal", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            case 1012 ->
                    this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.woodbreak", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            case 1014 ->
                    this.theWorld.playSoundAtPos(blockPosIn, "mob.wither.shoot", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            case 1015 ->
                    this.theWorld.playSoundAtPos(blockPosIn, "mob.bat.takeoff", 0.05F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            case 1016 ->
                    this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.infect", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            case 1017 ->
                    this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.unfect", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
            case 1020 ->
                    this.theWorld.playSoundAtPos(blockPosIn, "random.anvil_break", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
            case 1021 ->
                    this.theWorld.playSoundAtPos(blockPosIn, "random.anvil_use", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
            case 1022 ->
                    this.theWorld.playSoundAtPos(blockPosIn, "random.anvil_land", 0.3F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
            case 2000 -> {
                int k = p_180439_4_ % 3 - 1;
                int l = p_180439_4_ / 3 % 3 - 1;
                double d13 = (double) blockPosIn.getX() + (double) k * 0.6D + 0.5D;
                double d15 = (double) blockPosIn.getY() + 0.5D;
                double d19 = (double) blockPosIn.getZ() + (double) l * 0.6D + 0.5D;
                for (int l1 = 0; l1 < 10; ++l1) {
                    double d20 = random.nextDouble() * 0.2D + 0.01D;
                    double d21 = d13 + (double) k * 0.01D + (random.nextDouble() - 0.5D) * (double) l * 0.5D;
                    double d22 = d15 + (random.nextDouble() - 0.5D) * 0.5D;
                    double d23 = d19 + (double) l * 0.01D + (random.nextDouble() - 0.5D) * (double) k * 0.5D;
                    double d24 = (double) k * d20 + random.nextGaussian() * 0.01D;
                    double d9 = -0.03D + random.nextGaussian() * 0.01D;
                    double d10 = (double) l * d20 + random.nextGaussian() * 0.01D;
                    this.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d21, d22, d23, d24, d9, d10);
                }
                return;
            }
            case 2001 -> {
                Block block = Block.getBlockById(p_180439_4_ & 4095);
                if (block.getMaterial() != Material.air) {
                    this.mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(block.stepSound.getBreakSound()), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getFrequency() * 0.8F, (float) blockPosIn.getX() + 0.5F, (float) blockPosIn.getY() + 0.5F, (float) blockPosIn.getZ() + 0.5F));
                }
                this.mc.effectRenderer.addBlockDestroyEffects(blockPosIn, block.getStateFromMeta(p_180439_4_ >> 12 & 255));
            }
            case 2002 -> {
                double d11 = blockPosIn.getX();
                double d12 = blockPosIn.getY();
                double d14 = blockPosIn.getZ();
                for (int i1 = 0; i1 < 8; ++i1) {
                    this.spawnParticle(EnumParticleTypes.ITEM_CRACK, d11, d12, d14, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D, Item.getIdFromItem(Items.potionitem), p_180439_4_);
                }
                int j1 = Items.potionitem.getColorFromDamage(p_180439_4_);
                float f = (float) (j1 >> 16 & 255) / 255.0F;
                float f1 = (float) (j1 >> 8 & 255) / 255.0F;
                float f2 = (float) (j1 & 255) / 255.0F;
                EnumParticleTypes enumparticletypes = EnumParticleTypes.SPELL;
                if (Items.potionitem.isEffectInstant(p_180439_4_)) {
                    enumparticletypes = EnumParticleTypes.SPELL_INSTANT;
                }
                for (int k1 = 0; k1 < 100; ++k1) {
                    double d16 = random.nextDouble() * 4.0D;
                    double d17 = random.nextDouble() * Math.PI * 2.0D;
                    double d18 = Math.cos(d17) * d16;
                    double d7 = 0.01D + random.nextDouble() * 0.5D;
                    double d8 = Math.sin(d17) * d16;
                    EntityFX entityfx = this.spawnEntityFX(enumparticletypes.getParticleID(), enumparticletypes.getShouldIgnoreRange(), d11 + d18 * 0.1D, d12 + 0.3D, d14 + d8 * 0.1D, d18, d7, d8);

                    if (entityfx != null) {
                        float f3 = 0.75F + random.nextFloat() * 0.25F;
                        entityfx.setRBGColorF(f * f3, f1 * f3, f2 * f3);
                        entityfx.multiplyVelocity((float) d16);
                    }
                }
                this.theWorld.playSoundAtPos(blockPosIn, "game.potion.smash", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
            }
            case 2003 -> {
                double var7 = (double) blockPosIn.getX() + 0.5D;
                double var9 = blockPosIn.getY();
                double var11 = (double) blockPosIn.getZ() + 0.5D;
                for (int var13 = 0; var13 < 8; ++var13) {
                    this.spawnParticle(EnumParticleTypes.ITEM_CRACK, var7, var9, var11, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D, Item.getIdFromItem(Items.ender_eye));
                }
                for (double var32 = 0.0D; var32 < (Math.PI * 2D); var32 += 0.15707963267948966D) {
                    this.spawnParticle(EnumParticleTypes.PORTAL, var7 + Math.cos(var32) * 5.0D, var9 - 0.4D, var11 + Math.sin(var32) * 5.0D, Math.cos(var32) * -5.0D, 0.0D, Math.sin(var32) * -5.0D);
                    this.spawnParticle(EnumParticleTypes.PORTAL, var7 + Math.cos(var32) * 5.0D, var9 - 0.4D, var11 + Math.sin(var32) * 5.0D, Math.cos(var32) * -7.0D, 0.0D, Math.sin(var32) * -7.0D);
                }
                return;
            }
            case 2004 -> {
                for (int var18 = 0; var18 < 20; ++var18) {
                    double d3 = (double) blockPosIn.getX() + 0.5D + ((double) this.theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    double d4 = (double) blockPosIn.getY() + 0.5D + ((double) this.theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    double d5 = (double) blockPosIn.getZ() + 0.5D + ((double) this.theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    this.theWorld.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d3, d4, d5, 0.0D, 0.0D, 0.0D);
                    this.theWorld.spawnParticle(EnumParticleTypes.FLAME, d3, d4, d5, 0.0D, 0.0D, 0.0D);
                }
                return;
            }
            case 2005 -> ItemDye.spawnBonemealParticles(this.theWorld, blockPosIn, p_180439_4_);
        }
    }

    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        if (progress >= 0 && progress < 10) {
            DestroyBlockProgress destroyblockprogress = this.damagedBlocks.get(breakerId);

            if (destroyblockprogress == null || destroyblockprogress.getPosition().getX() != pos.getX() || destroyblockprogress.getPosition().getY() != pos.getY() || destroyblockprogress.getPosition().getZ() != pos.getZ()) {
                destroyblockprogress = new DestroyBlockProgress(pos);
                this.damagedBlocks.put(breakerId, destroyblockprogress);
            }

            destroyblockprogress.setPartialBlockDamage(progress);
            destroyblockprogress.setCloudUpdateTick(this.cloudTickCounter);
        } else {
            this.damagedBlocks.remove(breakerId);
        }
    }

    public void setDisplayListEntitiesDirty()
    {
        this.displayListEntitiesDirty = true;
    }

    public void resetClouds()
    {
        this.cloudRenderer.reset();
    }

    public int getCountActiveRenderers()
    {
        int i = 0;
        for (EnumWorldBlockLayer layer : EnumWorldBlockLayer.VALUES) {
            i += allChunks[layer.ordinal()].size();
        }
        return i;
    }

    public int getCountEntitiesRendered()
    {
        return this.countEntitiesRendered;
    }

    public int getCountTileEntitiesRendered()
    {
        return this.countTileEntitiesRendered;
    }

    public RenderChunk getRenderChunk(BlockPos p_getRenderChunk_1_) {
        return this.viewFrustum.getRenderChunk(p_getRenderChunk_1_);
    }

    public RenderChunk getRenderChunk(RenderChunk baseChunk, EnumFacing facing) {
        if (baseChunk == null) {
            return null;
        } else {
            RenderChunk neighbour = baseChunk.getNeighbour(facing);
            if (neighbour == null) {
                neighbour = baseChunk.setNeighbour(facing, this.viewFrustum.getRenderChunk(baseChunk.getPosition().offset(facing, 16)));
            }
            return neighbour;
        }
    }

    public WorldClient getWorld()
    {
        return this.theWorld;
    }

    /*public void func_181023_a(Collection<TileEntity> p_181023_1_, Collection<TileEntity> p_181023_2_) {
        synchronized (this.field_181024_n) {
            this.field_181024_n.removeAll(p_181023_1_);
            this.field_181024_n.addAll(p_181023_2_);
        }
    }*/

    static final class RenderGlobal$2 {
        static final int[] field_178037_a = new int[VertexFormatElement.EnumUsage.VALUES.length];


        static {
            try {
                field_178037_a[VertexFormatElement.EnumUsage.POSITION.ordinal()] = 1;
            } catch (NoSuchFieldError var3) {
                var3.printStackTrace();
            }

            try {
                field_178037_a[VertexFormatElement.EnumUsage.UV.ordinal()] = 2;
            } catch (NoSuchFieldError var2) {
                var2.printStackTrace();
            }

            try {
                field_178037_a[VertexFormatElement.EnumUsage.COLOR.ordinal()] = 3;
            } catch (NoSuchFieldError var1) {
                var1.printStackTrace();
            }
        }
    }
}
