package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Map;

import ipana.managements.module.Modules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelChicken;
import net.minecraft.client.model.ModelCow;
import net.minecraft.client.model.ModelHorse;
import net.minecraft.client.model.ModelOcelot;
import net.minecraft.client.model.ModelPig;
import net.minecraft.client.model.ModelRabbit;
import net.minecraft.client.model.ModelSheep2;
import net.minecraft.client.model.ModelSlime;
import net.minecraft.client.model.ModelSquid;
import net.minecraft.client.model.ModelWolf;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.RenderEnderCrystal;
import net.minecraft.client.renderer.tileentity.RenderItemFrame;
import net.minecraft.client.renderer.tileentity.RenderWitherSkull;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityMinecartMobSpawner;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.*;
import net.minecraft.world.World;
import optifine.PlayerItemsLayer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;

public class RenderManager
{
    /** A map of entity classes and the associated renderer. */
    private Map<Class<? extends Entity>, Render<? extends Entity>> entityRenderMap = Maps.newHashMap();

    /**
     * lists the various player skin types with their associated Renderer class instances.
     */
    private Map<String, RenderPlayer> skinMap = Maps.newHashMap();
    private RenderPlayer playerRenderer;
    public RenderAmongUs renderAmongUs;
    public RenderEla renderEla;
    public RenderCaveSpider renderCaveSpider;
    public RenderSpider<?> renderSpider;
    public RenderPig renderPig;
    public RenderSheep renderSheep;
    public RenderCow renderCow;
    public RenderMooshroom renderMooshroom;
    public RenderWolf renderWolf;
    public RenderChicken renderChicken;
    public RenderOcelot renderOcelot;
    public RenderRabbit renderRabbit;
    public RenderSilverfish renderSilverfish;
    public RenderEndermite renderEndermite;
    public RenderCreeper renderCreeper;
    public RenderEnderman renderEnderman;
    public RenderSnowMan renderSnowMan;
    public RenderSkeleton renderSkeleton;
    public RenderWitch renderWitch;
    public RenderBlaze renderBlaze;
    public RenderPigZombie renderPigZombie;
    public RenderZombie renderZombie;
    public RenderSlime renderSlime;
    public RenderMagmaCube renderMagmaCube;
    public RenderGiantZombie renderGiantZombie;
    public RenderGhast renderGhast;
    public RenderSquid renderSquid;
    public RenderVillager renderVillager;
    public RenderIronGolem renderIronGolem;
    public RenderBat renderBat;
    public RenderGuardian renderGuardian;
    public RenderDragon renderDragon;
    public RenderEnderCrystal renderEnderCrystal;
    public RenderWither renderWither;
    public RenderEntity renderEntity;
    public RenderPainting renderPainting;
    public RenderItemFrame renderItemFrame;
    public RenderLeashKnot renderLeashKnot;
    public RenderArrow renderArrow;
    public RenderSnowball<?> renderSnowball;
    public RenderSnowball<?> renderEnderPearl;
    public RenderSnowball<?> renderEnderEye;
    public RenderSnowball<?> renderEgg;
    public RenderSnowball<?> renderFireworkRocket;
    public RenderSnowball<?> renderExperienceBottle;
    public RenderPotion renderPotion;
    public RenderFireball renderLargeFireball;
    public RenderFireball renderSmallFireball;
    public RenderWitherSkull renderWitherSkull;
    public RenderEntityItem renderEntityItem;
    public RenderXPOrb renderXPOrb;
    public RenderTNTPrimed renderTNTPrimed;
    public RenderFallingBlock renderFallingBlock;
    public ArmorStandRenderer renderArmorStand;
    public RenderTntMinecart renderTntMinecart;
    public RenderMinecartMobSpawner renderMinecartMobSpawner;
    public RenderMinecart<?> renderMinecart;
    public RenderBoat renderBoat;
    public RenderFish renderFish;
    public RenderHorse renderHorse;
    public RenderLightningBolt renderLightningBolt;
    /** Renders fonts */
    private FontRenderer textRenderer;
    public double renderPosX;
    public double renderPosY;
    public double renderPosZ;
    public TextureManager renderEngine;

    /** Reference to the World object. */
    public World worldObj;

    /** Rendermanager's variable for the player */
    public Entity livingPlayer;
    public Entity pointedEntity;
    public float playerViewY;
    public float playerViewX;

    /** Reference to the GameSettings object. */
    public GameSettings options;
    public double viewerPosX;
    public double viewerPosY;
    public double viewerPosZ;
    private boolean renderOutlines = false;
    private boolean renderShadow = true;

    /** whether bounding box should be rendered or not */
    private boolean debugBoundingBox = false;


    public RenderManager(TextureManager renderEngineIn, RenderItem itemRendererIn)
    {
        this.renderEngine = renderEngineIn;
        this.entityRenderMap.put(EntityCaveSpider.class, renderCaveSpider = new RenderCaveSpider(this));
        this.entityRenderMap.put(EntitySpider.class, renderSpider = new RenderSpider<>(this));
        this.entityRenderMap.put(EntityPig.class, renderPig = new RenderPig(this, new ModelPig(), 0.7F));
        this.entityRenderMap.put(EntitySheep.class, renderSheep = new RenderSheep(this, new ModelSheep2(), 0.7F));
        this.entityRenderMap.put(EntityCow.class, renderCow = new RenderCow(this, new ModelCow(), 0.7F));
        this.entityRenderMap.put(EntityMooshroom.class, renderMooshroom = new RenderMooshroom(this, new ModelCow(), 0.7F));
        this.entityRenderMap.put(EntityWolf.class, renderWolf = new RenderWolf(this, new ModelWolf(), 0.5F));
        this.entityRenderMap.put(EntityChicken.class, renderChicken = new RenderChicken(this, new ModelChicken(), 0.3F));
        this.entityRenderMap.put(EntityOcelot.class, renderOcelot = new RenderOcelot(this, new ModelOcelot(), 0.4F));
        this.entityRenderMap.put(EntityRabbit.class, renderRabbit = new RenderRabbit(this, new ModelRabbit(), 0.3F));
        this.entityRenderMap.put(EntitySilverfish.class, renderSilverfish = new RenderSilverfish(this));
        this.entityRenderMap.put(EntityEndermite.class, renderEndermite = new RenderEndermite(this));
        this.entityRenderMap.put(EntityCreeper.class, renderCreeper = new RenderCreeper(this));
        this.entityRenderMap.put(EntityEnderman.class, renderEnderman = new RenderEnderman(this));
        this.entityRenderMap.put(EntitySnowman.class, renderSnowMan = new RenderSnowMan(this));
        this.entityRenderMap.put(EntitySkeleton.class, renderSkeleton = new RenderSkeleton(this));
        this.entityRenderMap.put(EntityWitch.class, renderWitch = new RenderWitch(this));
        this.entityRenderMap.put(EntityBlaze.class, renderBlaze = new RenderBlaze(this));
        this.entityRenderMap.put(EntityPigZombie.class, renderPigZombie = new RenderPigZombie(this));
        this.entityRenderMap.put(EntityZombie.class, renderZombie = new RenderZombie(this));
        this.entityRenderMap.put(EntitySlime.class, renderSlime = new RenderSlime(this, new ModelSlime(16), 0.25F));
        this.entityRenderMap.put(EntityMagmaCube.class, renderMagmaCube = new RenderMagmaCube(this));
        this.entityRenderMap.put(EntityGiantZombie.class, renderGiantZombie = new RenderGiantZombie(this, new ModelZombie(), 0.5F, 6.0F));
        this.entityRenderMap.put(EntityGhast.class, renderGhast = new RenderGhast(this));
        this.entityRenderMap.put(EntitySquid.class, renderSquid = new RenderSquid(this, new ModelSquid(), 0.7F));
        this.entityRenderMap.put(EntityVillager.class, renderVillager = new RenderVillager(this));
        this.entityRenderMap.put(EntityIronGolem.class, renderIronGolem = new RenderIronGolem(this));
        this.entityRenderMap.put(EntityBat.class, renderBat = new RenderBat(this));
        this.entityRenderMap.put(EntityGuardian.class, renderGuardian = new RenderGuardian(this));
        this.entityRenderMap.put(EntityDragon.class, renderDragon = new RenderDragon(this));
        this.entityRenderMap.put(EntityEnderCrystal.class, renderEnderCrystal = new RenderEnderCrystal(this));
        this.entityRenderMap.put(EntityWither.class, renderWither = new RenderWither(this));
        this.entityRenderMap.put(Entity.class, renderEntity = new RenderEntity(this));
        this.entityRenderMap.put(EntityPainting.class, renderPainting = new RenderPainting(this));
        this.entityRenderMap.put(EntityItemFrame.class, renderItemFrame = new RenderItemFrame(this, itemRendererIn));
        this.entityRenderMap.put(EntityLeashKnot.class, renderLeashKnot = new RenderLeashKnot(this));
        this.entityRenderMap.put(EntityArrow.class, renderArrow = new RenderArrow(this));
        this.entityRenderMap.put(EntitySnowball.class, renderSnowball = new RenderSnowball<>(this, Items.snowball, itemRendererIn));
        this.entityRenderMap.put(EntityEnderPearl.class, renderEnderPearl = new RenderSnowball<>(this, Items.ender_pearl, itemRendererIn));
        this.entityRenderMap.put(EntityEnderEye.class, renderEnderEye = new RenderSnowball<>(this, Items.ender_eye, itemRendererIn));
        this.entityRenderMap.put(EntityEgg.class, renderEgg = new RenderSnowball<>(this, Items.egg, itemRendererIn));
        this.entityRenderMap.put(EntityPotion.class, renderPotion = new RenderPotion(this, itemRendererIn));
        this.entityRenderMap.put(EntityExpBottle.class, renderExperienceBottle = new RenderSnowball<>(this, Items.experience_bottle, itemRendererIn));
        this.entityRenderMap.put(EntityFireworkRocket.class, renderFireworkRocket = new RenderSnowball<>(this, Items.fireworks, itemRendererIn));
        this.entityRenderMap.put(EntityLargeFireball.class, renderLargeFireball = new RenderFireball(this, 2.0F));
        this.entityRenderMap.put(EntitySmallFireball.class, renderSmallFireball = new RenderFireball(this, 0.5F));
        this.entityRenderMap.put(EntityWitherSkull.class, renderWitherSkull = new RenderWitherSkull(this));
        this.entityRenderMap.put(EntityItem.class, renderEntityItem = new RenderEntityItem(this, itemRendererIn));
        this.entityRenderMap.put(EntityXPOrb.class, renderXPOrb = new RenderXPOrb(this));
        this.entityRenderMap.put(EntityTNTPrimed.class, renderTNTPrimed = new RenderTNTPrimed(this));
        this.entityRenderMap.put(EntityFallingBlock.class, renderFallingBlock = new RenderFallingBlock(this));
        this.entityRenderMap.put(EntityArmorStand.class, renderArmorStand = new ArmorStandRenderer(this));
        this.entityRenderMap.put(EntityMinecartTNT.class, renderTntMinecart = new RenderTntMinecart(this));
        this.entityRenderMap.put(EntityMinecartMobSpawner.class, renderMinecartMobSpawner = new RenderMinecartMobSpawner(this));
        this.entityRenderMap.put(EntityMinecart.class, renderMinecart = new RenderMinecart<>(this));
        this.entityRenderMap.put(EntityBoat.class, renderBoat = new RenderBoat(this));
        this.entityRenderMap.put(EntityFishHook.class, renderFish = new RenderFish(this));
        this.entityRenderMap.put(EntityHorse.class, renderHorse = new RenderHorse(this, new ModelHorse(), 0.75F));
        this.entityRenderMap.put(EntityLightningBolt.class, renderLightningBolt = new RenderLightningBolt(this));
        this.playerRenderer = new RenderPlayer(this);
        this.renderAmongUs = new RenderAmongUs(this);
        this.renderEla = new RenderEla(this);
        this.skinMap.put("default", this.playerRenderer);
        this.skinMap.put("slim", new RenderPlayer(this, true));
        PlayerItemsLayer.register(this.skinMap);
    }

    public void setRenderPosition(double renderPosXIn, double renderPosYIn, double renderPosZIn)
    {
        this.renderPosX = renderPosXIn;
        this.renderPosY = renderPosYIn;
        this.renderPosZ = renderPosZIn;
    }

    /*public <T extends Entity, R extends Render<T>> R getEntityClassRenderObject(Class<T> p_78715_1_) {
        R render = (R) this.entityRenderMap.get(p_78715_1_);

        if (render == null && p_78715_1_ != Entity.class)
        {
            render = this.getEntityClassRenderObject((Class<T>) p_78715_1_.getSuperclass());
            this.entityRenderMap.put(p_78715_1_, render);
        }

        return render;
    }*/

    public <T extends Entity, R extends Render<T>> R getEntityRenderObject(T entityIn)
    {
        if (entityIn instanceof AbstractClientPlayer)
        {
            String s = ((AbstractClientPlayer)entityIn).getSkinType();
            RenderPlayer renderplayer = this.skinMap.get(s);
            return (R) (renderplayer != null ? renderplayer : this.playerRenderer);
        }
        else
        {
            return (R) entityIn.renderObject();
        }
    }

    public void cacheActiveRenderInfo(World worldIn, FontRenderer textRendererIn, Entity livingPlayerIn, Entity pointedEntityIn, GameSettings optionsIn, float partialTicks)
    {
        this.worldObj = worldIn;
        this.options = optionsIn;
        this.livingPlayer = livingPlayerIn;
        this.pointedEntity = pointedEntityIn;
        this.textRenderer = textRendererIn;

        if (livingPlayerIn instanceof EntityLivingBase && ((EntityLivingBase)livingPlayerIn).isPlayerSleeping())
        {
            IBlockState iblockstate = worldIn.getBlockState(new BlockPos(livingPlayerIn));
            Block block = iblockstate.getBlock();

            if (block == Blocks.bed) {
                int j = (iblockstate.getValue(BlockBed.FACING)).getHorizontalIndex();
                this.playerViewY = (float)(j * 90 + 180);
                this.playerViewX = 0.0F;
            }
        }
        else
        {
            this.playerViewY = livingPlayerIn.prevRotationYaw + (livingPlayerIn.rotationYaw - livingPlayerIn.prevRotationYaw) * partialTicks;
            this.playerViewX = livingPlayerIn.prevRotationPitch + (livingPlayerIn.rotationPitch - livingPlayerIn.prevRotationPitch) * partialTicks;
        }

        if (optionsIn.thirdPersonView == 2)
        {
            this.playerViewY += 180.0F;
        }

        this.viewerPosX = livingPlayerIn.lastTickPosX + (livingPlayerIn.posX - livingPlayerIn.lastTickPosX) * (double)partialTicks;
        this.viewerPosY = livingPlayerIn.lastTickPosY + (livingPlayerIn.posY - livingPlayerIn.lastTickPosY) * (double)partialTicks;
        this.viewerPosZ = livingPlayerIn.lastTickPosZ + (livingPlayerIn.posZ - livingPlayerIn.lastTickPosZ) * (double)partialTicks;
    }

    public void setPlayerViewY(float playerViewYIn)
    {
        this.playerViewY = playerViewYIn;
    }

    public boolean isRenderShadow()
    {
        return this.renderShadow;
    }

    public void setRenderShadow(boolean renderShadowIn)
    {
        this.renderShadow = renderShadowIn;
    }

    public void setDebugBoundingBox(boolean debugBoundingBoxIn)
    {
        this.debugBoundingBox = debugBoundingBoxIn;
    }

    public boolean isDebugBoundingBox()
    {
        return this.debugBoundingBox;
    }

    public void renderEntitySimple(Entity entityIn, float partialTicks)
    {
        this.renderEntityStatic(entityIn, partialTicks, false);
    }

    public boolean shouldRender(Entity entityIn, ICamera camera, double camX, double camY, double camZ)
    {
        Render<Entity> render = this.getEntityRenderObject(entityIn);
        return render != null && render.shouldRender(entityIn, camera, camX, camY, camZ);
    }

    public void renderEntityStatic(Entity entity, float partialTicks, boolean p_147936_3_) {
        if (entity instanceof EntityArmorStand && entity.isInvisible()) {
            String name = entity.getDisplayName().getFormattedText();
            if (!name.isEmpty()) {
                Render<Entity> render = this.getEntityRenderObject(entity);
                double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
                double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
                double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
                GlStateManager.enableBlend();
                if (GlStateManager.blendState.field_179213_a.state() && !GL11.glIsEnabled(GL11.GL_BLEND)) {
                    GL11.glEnable(GL11.GL_BLEND);
                }

                render.renderLivingLabel(entity, name, d0 - renderPosX, d1 - renderPosY, d2 - renderPosZ);
            }
            return;
        }
        if (entity.ticksExisted == 0) {
            entity.lastTickPosX = entity.posX;
            entity.lastTickPosY = entity.posY;
            entity.lastTickPosZ = entity.posZ;
        }


        double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
        double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
        double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;

        float f = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;

        int i;
        if (Modules.BRIGHTNESS.isEnabled()) {
            i = 15728880;
        } else {
            i = entity.getBrightnessForRender(partialTicks);
        }

        if (entity.isBurning()) {
            i = 15728880;
        }

        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
        this.doRenderEntity(entity, d0 - this.renderPosX, d1 - this.renderPosY, d2 - this.renderPosZ, f, partialTicks, p_147936_3_);

        /*if (entity instanceof EntityOtherPlayerMP mp) {
            GlStateManager.colorMask(true, false, false, true);
            this.doRenderEntity(entity, mp.otherPlayerMPX - this.renderPosX, mp.otherPlayerMPY - this.renderPosY, mp.otherPlayerMPZ - this.renderPosZ, f, partialTicks, p_147936_3_);
            GlStateManager.colorMask(true, true, true, true);
        }*/
    }

    public void renderWitherSkull(Entity entityIn, float partialTicks) {
        double d0 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
        double d1 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
        double d2 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
        Render<Entity> render = this.getEntityRenderObject(entityIn);

        if (render != null && this.renderEngine != null) {
            int i = entityIn.getBrightnessForRender(partialTicks);
            int j = i % 65536;
            int k = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            render.renderName(entityIn, d0 - this.renderPosX, d1 - this.renderPosY, d2 - this.renderPosZ);
        }
    }

    public boolean renderEntityWithPosYaw(Entity entityIn, double x, double y, double z, float entityYaw, float partialTicks)
    {
        return this.doRenderEntity(entityIn, x, y, z, entityYaw, partialTicks, false);
    }
    public <T extends Entity> boolean doRenderEntity(T entity, double x, double y, double z, float entityYaw, float partialTicks, boolean p_147939_10_) {
        try {
            Render<T> render = this.getEntityRenderObject(entity);

            if (render != null && this.renderEngine != null) {
                try {
                    if (render instanceof RendererLivingEntity) {
                        ((RendererLivingEntity<?>)render).setRenderOutlines(this.renderOutlines);
                    }
                    render.doRender(entity, x, y, z, entityYaw, partialTicks);
                } catch (Throwable throwable2) {
                    throw new ReportedException(CrashReport.makeCrashReport(throwable2, "Rendering entity in world"));
                }
                if (!this.renderOutlines && !(entity instanceof EntityItemFrame)) {
                    try {
                        render.doRenderShadowAndFire(entity, x, y, z, partialTicks);
                    } catch (Throwable throwable1) {
                        throw new ReportedException(CrashReport.makeCrashReport(throwable1, "Post-rendering entity in world"));
                    }
                }
                if (this.debugBoundingBox && !entity.isInvisible() && !p_147939_10_) {
                    try {
                        this.renderDebugBoundingBox(entity, x, y, z, partialTicks);
                    } catch (Throwable throwable) {
                        throw new ReportedException(CrashReport.makeCrashReport(throwable, "Rendering entity hitbox in world"));
                    }
                }
            }
            else return this.renderEngine == null;
            return true;
        } catch (Throwable throwable3) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable3, "Rendering entity in world");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being rendered");
            entity.addEntityCrashInfo(crashreportcategory);
            CrashReportCategory crashreportcategory1 = crashreport.makeCategory("Renderer details");
            crashreportcategory1.addCrashSection("Assigned renderer", this.getEntityRenderObject(entity));
            crashreportcategory1.addCrashSection("Location", CrashReportCategory.getCoordinateInfo(x, y, z));
            crashreportcategory1.addCrashSection("Rotation", entityYaw);
            crashreportcategory1.addCrashSection("Delta", partialTicks);
            throw new ReportedException(crashreport);
        }
    }

    /**
     * Renders the bounding box around an entity when F3+B is pressed
     */
    private void renderDebugBoundingBox(Entity entityIn, double p_85094_2_, double p_85094_4_, double p_85094_6_, float p_85094_9_)
    {
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        float f = entityIn.width / 2.0F;
        float f1 = entityIn.getCollisionBorderSize();
        AxisAlignedBB axisalignedbb = entityIn.getEntityBoundingBox().expand(f1, f1, f1);
        AxisAlignedBB axisalignedbb1 = new AxisAlignedBB(axisalignedbb.minX - entityIn.posX + p_85094_2_, axisalignedbb.minY - entityIn.posY + p_85094_4_, axisalignedbb.minZ - entityIn.posZ + p_85094_6_, axisalignedbb.maxX - entityIn.posX + p_85094_2_, axisalignedbb.maxY - entityIn.posY + p_85094_4_, axisalignedbb.maxZ - entityIn.posZ + p_85094_6_);
        RenderGlobal.func_181563_a(axisalignedbb1, 255, 255, 255, 255);

        if (entityIn instanceof EntityLivingBase)
        {
            RenderGlobal.func_181563_a(new AxisAlignedBB(p_85094_2_ - (double)f, p_85094_4_ + (double)entityIn.getEyeHeight() - 0.009999999776482582D, p_85094_6_ - (double)f, p_85094_2_ + (double)f, p_85094_4_ + (double)entityIn.getEyeHeight() + 0.009999999776482582D, p_85094_6_ + (double)f), 255, 0, 0, 255);
        }

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        Vec3 vec3 = entityIn.getLook(p_85094_9_);
        worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(p_85094_2_, p_85094_4_ + (double)entityIn.getEyeHeight(), p_85094_6_).color(0, 0, 255, 255).endVertex();
        worldrenderer.pos(p_85094_2_ + vec3.xCoord * 2.0D, p_85094_4_ + (double)entityIn.getEyeHeight() + vec3.yCoord * 2.0D, p_85094_6_ + vec3.zCoord * 2.0D).color(0, 0, 255, 255).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    /**
     * World sets this RenderManager's worldObj to the world provided
     */
    public void set(World worldIn)
    {
        this.worldObj = worldIn;
    }

    public double getDistanceToCamera(double p_78714_1_, double p_78714_3_, double p_78714_5_)
    {
        double d0 = p_78714_1_ - this.viewerPosX;
        double d1 = p_78714_3_ - this.viewerPosY;
        double d2 = p_78714_5_ - this.viewerPosZ;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    /**
     * Returns the font renderer
     */
    public FontRenderer getFontRenderer()
    {
        return this.textRenderer;
    }

    public void setRenderOutlines(boolean renderOutlinesIn)
    {
        this.renderOutlines = renderOutlinesIn;
    }

    public Map<Class<? extends Entity>, Render<? extends Entity>> getEntityRenderMap()
    {
        return this.entityRenderMap;
    }

}
