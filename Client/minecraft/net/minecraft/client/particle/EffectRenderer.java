package net.minecraft.client.particle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ipana.utils.player.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import optifine.Config;

public class EffectRenderer {
    private static final ResourceLocation particleTextures = new ResourceLocation("textures/particle/particles.png");

    /**
     * Reference to the World object.
     */
    protected World worldObj;
    private List<EntityFX>[][] fxLayers = new List[4][];
    private List<EntityParticleEmitter> particleEmitters = Lists.newArrayList();
    private TextureManager renderer;

    /**
     * RNG.
     */
    private Random rand = new Random();
    private Map<Integer, IParticleFactory> particleTypes = Maps.newHashMap();


    public EffectRenderer(World worldIn, TextureManager rendererIn) {
        this.worldObj = worldIn;
        this.renderer = rendererIn;

        for (int i = 0; i < 4; ++i) {
            this.fxLayers[i] = new List[2];

            for (int j = 0; j < 2; ++j) {
                this.fxLayers[i][j] = Lists.newArrayList();
            }
        }

        this.registerVanillaParticles();
    }

    private void registerVanillaParticles() {
        this.registerParticle(EnumParticleTypes.EXPLOSION_NORMAL.getParticleID(), new EntityExplodeFX.Factory());
        this.registerParticle(EnumParticleTypes.WATER_BUBBLE.getParticleID(), new EntityBubbleFX.Factory());
        this.registerParticle(EnumParticleTypes.WATER_SPLASH.getParticleID(), new EntitySplashFX.Factory());
        this.registerParticle(EnumParticleTypes.WATER_WAKE.getParticleID(), new EntityFishWakeFX.Factory());
        this.registerParticle(EnumParticleTypes.WATER_DROP.getParticleID(), new EntityRainFX.Factory());
        this.registerParticle(EnumParticleTypes.SUSPENDED.getParticleID(), new EntitySuspendFX.Factory());
        this.registerParticle(EnumParticleTypes.SUSPENDED_DEPTH.getParticleID(), new EntityAuraFX.Factory());
        this.registerParticle(EnumParticleTypes.CRIT.getParticleID(), new EntityCrit2FX.Factory());
        this.registerParticle(EnumParticleTypes.CRIT_MAGIC.getParticleID(), new EntityCrit2FX.MagicFactory());
        this.registerParticle(EnumParticleTypes.SMOKE_NORMAL.getParticleID(), new EntitySmokeFX.Factory());
        this.registerParticle(EnumParticleTypes.SMOKE_LARGE.getParticleID(), new EntityCritFX.Factory());
        this.registerParticle(EnumParticleTypes.SPELL.getParticleID(), new EntitySpellParticleFX.Factory());
        this.registerParticle(EnumParticleTypes.SPELL_INSTANT.getParticleID(), new EntitySpellParticleFX.InstantFactory());
        this.registerParticle(EnumParticleTypes.SPELL_MOB.getParticleID(), new EntitySpellParticleFX.MobFactory());
        this.registerParticle(EnumParticleTypes.SPELL_MOB_AMBIENT.getParticleID(), new EntitySpellParticleFX.AmbientMobFactory());
        this.registerParticle(EnumParticleTypes.SPELL_WITCH.getParticleID(), new EntitySpellParticleFX.WitchFactory());
        this.registerParticle(EnumParticleTypes.DRIP_WATER.getParticleID(), new EntityDropParticleFX.WaterFactory());
        this.registerParticle(EnumParticleTypes.DRIP_LAVA.getParticleID(), new EntityDropParticleFX.LavaFactory());
        this.registerParticle(EnumParticleTypes.VILLAGER_ANGRY.getParticleID(), new EntityHeartFX.AngryVillagerFactory());
        this.registerParticle(EnumParticleTypes.VILLAGER_HAPPY.getParticleID(), new EntityAuraFX.HappyVillagerFactory());
        this.registerParticle(EnumParticleTypes.TOWN_AURA.getParticleID(), new EntityAuraFX.Factory());
        this.registerParticle(EnumParticleTypes.NOTE.getParticleID(), new EntityNoteFX.Factory());
        this.registerParticle(EnumParticleTypes.PORTAL.getParticleID(), new EntityPortalFX.Factory());
        this.registerParticle(EnumParticleTypes.ENCHANTMENT_TABLE.getParticleID(), new EntityEnchantmentTableParticleFX.EnchantmentTable());
        this.registerParticle(EnumParticleTypes.FLAME.getParticleID(), new EntityFlameFX.Factory());
        this.registerParticle(EnumParticleTypes.LAVA.getParticleID(), new EntityLavaFX.Factory());
        this.registerParticle(EnumParticleTypes.FOOTSTEP.getParticleID(), new EntityFootStepFX.Factory());
        this.registerParticle(EnumParticleTypes.CLOUD.getParticleID(), new EntityCloudFX.Factory());
        this.registerParticle(EnumParticleTypes.REDSTONE.getParticleID(), new EntityReddustFX.Factory());
        this.registerParticle(EnumParticleTypes.SNOWBALL.getParticleID(), new EntityBreakingFX.SnowballFactory());
        this.registerParticle(EnumParticleTypes.SNOW_SHOVEL.getParticleID(), new EntitySnowShovelFX.Factory());
        this.registerParticle(EnumParticleTypes.SLIME.getParticleID(), new EntityBreakingFX.SlimeFactory());
        this.registerParticle(EnumParticleTypes.HEART.getParticleID(), new EntityHeartFX.Factory());
        this.registerParticle(EnumParticleTypes.BARRIER.getParticleID(), new Barrier.Factory());
        this.registerParticle(EnumParticleTypes.ITEM_CRACK.getParticleID(), new EntityBreakingFX.Factory());
        this.registerParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), new EntityDiggingFX.Factory());
        this.registerParticle(EnumParticleTypes.BLOCK_DUST.getParticleID(), new EntityBlockDustFX.Factory());
        this.registerParticle(EnumParticleTypes.EXPLOSION_HUGE.getParticleID(), new EntityHugeExplodeFX.Factory());
        this.registerParticle(EnumParticleTypes.EXPLOSION_LARGE.getParticleID(), new EntityLargeExplodeFX.Factory());
        this.registerParticle(EnumParticleTypes.FIREWORKS_SPARK.getParticleID(), new EntityFirework.Factory());
        this.registerParticle(EnumParticleTypes.MOB_APPEARANCE.getParticleID(), new MobAppearance.Factory());
    }

    public void registerParticle(int id, IParticleFactory particleFactory) {
        this.particleTypes.put(id, particleFactory);
    }

    public void emitParticleAtEntity(Entity entityIn, EnumParticleTypes particleTypes) {
        this.particleEmitters.add(new EntityParticleEmitter(this.worldObj, entityIn, particleTypes));
    }

    /**
     * Spawns the relevant particle according to the particle id.
     */
    public EntityFX spawnEffectParticle(int particleId, double p_178927_2_, double p_178927_4_, double p_178927_6_, double p_178927_8_, double p_178927_10_, double p_178927_12_, int... p_178927_14_) {
        IParticleFactory iparticlefactory = this.particleTypes.get(particleId);

        if (iparticlefactory != null) {
            EntityFX entityfx = iparticlefactory.getEntityFX(particleId, this.worldObj, p_178927_2_, p_178927_4_, p_178927_6_, p_178927_8_, p_178927_10_, p_178927_12_, p_178927_14_);

            if (entityfx != null) {
                this.addEffect(entityfx);
                return entityfx;
            }
        }

        return null;
    }

    public void addEffect(EntityFX effect) {
        if (effect != null) {
            if (!(effect instanceof EntityFirework.SparkFX) || Config.isFireworkParticles()) {
                int i = effect.getFXLayer();
                int j = effect.getAlpha() != 1.0F ? 0 : 1;

                if (this.fxLayers[i][j].size() >= 4000) {
                    this.fxLayers[i][j].remove(0);
                }

                if (!(effect instanceof Barrier) || !this.reuseBarrierParticle(effect, this.fxLayers[i][j])) {
                    this.fxLayers[i][j].add(effect);
                }
            }
        }
    }

    public void updateEffects() {
        for (int i = 0; i < 4; ++i) {
            this.updateEffectLayer(i);
        }

        ArrayList<EntityParticleEmitter> arraylist = Lists.newArrayList();

        for (EntityParticleEmitter entityparticleemitter0 : this.particleEmitters) {
            entityparticleemitter0.onUpdate();

            if (entityparticleemitter0.isDead) {
                arraylist.add(entityparticleemitter0);
            }
        }

        this.particleEmitters.removeAll(arraylist);
    }

    private void updateEffectLayer(int p_178922_1_) {
        for (int i = 0; i < 2; ++i) {
            this.updateEffectAlphaLayer(this.fxLayers[p_178922_1_][i]);
        }
    }

    private void updateEffectAlphaLayer(List<EntityFX> p_178925_1_) {
        ArrayList<EntityFX> arraylist = Lists.newArrayList();

        for (int i = 0; i < p_178925_1_.size(); ++i)
        {
            EntityFX entityfx = p_178925_1_.get(i);
            this.tickParticle(entityfx);

            if (entityfx.isDead)
            {
                arraylist.add(entityfx);
            }
        }

        p_178925_1_.removeAll(arraylist);
    }

    private void tickParticle(final EntityFX p_178923_1_) {
        try {
            p_178923_1_.onUpdate();
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking Particle");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being ticked");
            final int i = p_178923_1_.getFXLayer();
            crashreportcategory.addCrashSectionCallable("Particle", p_178923_1_::toString);
            crashreportcategory.addCrashSectionCallable("Particle Type", () -> i == 0 ? "MISC_TEXTURE" : (i == 1 ? "TERRAIN_TEXTURE" : (i == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + i)));
            throw new ReportedException(crashreport);
        }
    }

    /**
     * Renders all current particles. Args player, partialTickTime
     */
    public void renderParticles(Frustum frustum, Entity entityIn, double x, double y, double z, float partialTicks) {
        float f = ActiveRenderInfo.getRotationX();
        float f1 = ActiveRenderInfo.getRotationZ();
        float f2 = ActiveRenderInfo.getRotationYZ();
        float f3 = ActiveRenderInfo.getRotationXY();
        float f4 = ActiveRenderInfo.getRotationXZ();
        EntityFX.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks + x;
        EntityFX.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks + y;
        EntityFX.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks + z;
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.alphaFunc(516, 0.003921569F);
        for (int i = 0; i < 3; ++i) {
            final int j = i;

            for (int k = 0; k < 2; ++k) {
                if (!this.fxLayers[j][k].isEmpty()) {
                    switch (k) {
                        case 0 -> GlStateManager.depthMask(false);
                        case 1 -> GlStateManager.depthMask(true);
                    }

                    switch (j) {
                        default -> this.renderer.bindTexture(particleTextures);
                        case 1 -> this.renderer.bindTexture(TextureMap.locationBlocksTexture);
                    }

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                    worldrenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);


                    for (int l = 0; l < this.fxLayers[j][k].size(); ++l) {
                        final EntityFX entityfx = this.fxLayers[j][k].get(l);
                        if (!frustum.isBoundingBoxInFrustum(entityfx.getEntityBoundingBox())) {
                            continue;
                        }
                        try {
                            entityfx.renderParticle(worldrenderer, entityIn, partialTicks, f, f4, f1, f2, f3);
                        } catch (Throwable throwable) {
                            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
                            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
                            crashreportcategory.addCrashSectionCallable("Particle", entityfx::toString);
                            crashreportcategory.addCrashSectionCallable("Particle Type", () -> j == 0 ? "MISC_TEXTURE" : j == 1 ? "TERRAIN_TEXTURE" : "Unknown - " + j);
                            throw new ReportedException(crashreport);
                        }
                    }

                    tessellator.draw();
                }
            }
        }
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(516, 0.1F);
    }

    public void renderLitParticles(Frustum frustum, Entity entityIn, float yaw, float pitch, float p_78872_2_) {
        float f = 0.017453292F;
        float f1 = MathHelper.cos(yaw * f);
        float f2 = MathHelper.sin(yaw * f);
        float f3 = -f2 * MathHelper.sin(pitch * f);
        float f4 = f1 * MathHelper.sin(pitch * f);
        float f5 = MathHelper.cos(pitch * f);
        for (int i = 0; i < 2; ++i) {
            List<EntityFX> list = this.fxLayers[3][i];

            if (!list.isEmpty()) {
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();

                for (EntityFX entityfx : list) {
                    if (!frustum.isBoundingBoxInFrustum(entityfx.getEntityBoundingBox())) {
                        continue;
                    }
                    entityfx.renderParticle(worldrenderer, entityIn, p_78872_2_, f1 + 180, f5, f2, f3, f4);
                }
            }
        }
    }

    public void clearEffects(World worldIn) {
        this.worldObj = worldIn;

        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 2; ++j) {
                this.fxLayers[i][j].clear();
            }
        }

        this.particleEmitters.clear();
    }

    public void addBlockDestroyEffects(BlockPos pos, IBlockState state) {
        boolean flag;
        flag = state.getBlock().getMaterial() != Material.air;


        if (flag) {
            state = state.getBlock().getActualState(state, this.worldObj, pos);
            byte b0 = 4;

            for (int i = 0; i < b0; ++i) {
                for (int j = 0; j < b0; ++j) {
                    for (int k = 0; k < b0; ++k) {
                        double d0 = (double) pos.getX() + ((double) i + 0.5D) / (double) b0;
                        double d1 = (double) pos.getY() + ((double) j + 0.5D) / (double) b0;
                        double d2 = (double) pos.getZ() + ((double) k + 0.5D) / (double) b0;
                        this.addEffect((new EntityDiggingFX(this.worldObj, d0, d1, d2, d0 - (double) pos.getX() - 0.5D, d1 - (double) pos.getY() - 0.5D, d2 - (double) pos.getZ() - 0.5D, state)).func_174846_a(pos));
                    }
                }
            }
        }
    }

    /**
     * Adds block hit particles for the specified block
     */
    public void addBlockHitEffects(BlockPos pos, EnumFacing side) {
        IBlockState iblockstate = this.worldObj.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (block.getRenderType() != -1) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            float f = 0.1F;
            double d0 = (double) i + this.rand.nextDouble() * (block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() - (double) (f * 2.0F)) + (double) f + block.getBlockBoundsMinX();
            double d1 = (double) j + this.rand.nextDouble() * (block.getBlockBoundsMaxY() - block.getBlockBoundsMinY() - (double) (f * 2.0F)) + (double) f + block.getBlockBoundsMinY();
            double d2 = (double) k + this.rand.nextDouble() * (block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() - (double) (f * 2.0F)) + (double) f + block.getBlockBoundsMinZ();

            if (side == EnumFacing.DOWN) {
                d1 = (double) j + block.getBlockBoundsMinY() - (double) f;
            }

            if (side == EnumFacing.UP) {
                d1 = (double) j + block.getBlockBoundsMaxY() + (double) f;
            }

            if (side == EnumFacing.NORTH) {
                d2 = (double) k + block.getBlockBoundsMinZ() - (double) f;
            }

            if (side == EnumFacing.SOUTH) {
                d2 = (double) k + block.getBlockBoundsMaxZ() + (double) f;
            }

            if (side == EnumFacing.WEST) {
                d0 = (double) i + block.getBlockBoundsMinX() - (double) f;
            }

            if (side == EnumFacing.EAST) {
                d0 = (double) i + block.getBlockBoundsMaxX() + (double) f;
            }

            this.addEffect((new EntityDiggingFX(this.worldObj, d0, d1, d2, 0.0D, 0.0D, 0.0D, iblockstate)).func_174846_a(pos).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
        }
    }

    public void moveToAlphaLayer(EntityFX effect) {
        this.moveToLayer(effect, 1, 0);
    }

    public void moveToNoAlphaLayer(EntityFX effect) {
        this.moveToLayer(effect, 0, 1);
    }

    private void moveToLayer(EntityFX effect, int p_178924_2_, int p_178924_3_) {
        for (int i = 0; i < 4; ++i) {
            if (this.fxLayers[i][p_178924_2_].contains(effect)) {
                this.fxLayers[i][p_178924_2_].remove(effect);
                this.fxLayers[i][p_178924_3_].add(effect);
            }
        }
    }

    public String getStatistics() {
        int i = 0;

        for (int j = 0; j < 4; ++j) {
            for (int k = 0; k < 2; ++k) {
                i += this.fxLayers[j][k].size();
            }
        }

        return "" + i;
    }

    private boolean reuseBarrierParticle(EntityFX p_reuseBarrierParticle_1_, List<EntityFX> p_reuseBarrierParticle_2_) {
        for (EntityFX entityfx : p_reuseBarrierParticle_2_) {
            if (entityfx instanceof Barrier && p_reuseBarrierParticle_1_.posX == entityfx.posX && p_reuseBarrierParticle_1_.posY == entityfx.posY && p_reuseBarrierParticle_1_.posZ == entityfx.posZ) {
                entityfx.particleAge = 0;
                return true;
            }
        }

        return false;
    }
}
