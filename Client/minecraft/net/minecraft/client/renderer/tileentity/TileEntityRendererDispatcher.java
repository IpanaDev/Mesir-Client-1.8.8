package net.minecraft.client.renderer.tileentity;

import com.google.common.collect.Maps;
import java.util.Map;

import ipana.managements.module.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;

public class TileEntityRendererDispatcher
{
    private Map < Class <? extends TileEntity > , TileEntitySpecialRenderer <? extends TileEntity >> mapSpecialRenderers = Maps.newHashMap();
    public static TileEntityRendererDispatcher instance = new TileEntityRendererDispatcher();
    private FontRenderer fontRenderer;

    /** The player's current X position (same as playerX) */
    public static double staticPlayerX;

    /** The player's current Y position (same as playerY) */
    public static double staticPlayerY;

    /** The player's current Z position (same as playerZ) */
    public static double staticPlayerZ;
    public TextureManager renderEngine;
    public World worldObj;
    public Entity entity;
    public float entityYaw;
    public float entityPitch;
    public double entityX;
    public double entityY;
    public double entityZ;
    public TileEntitySignRenderer tileEntitySignRenderer;
    public TileEntityMobSpawnerRenderer tileEntityMobSpawnerRenderer;
    public TileEntityPistonRenderer tileEntityPistonRenderer;
    public TileEntityChestRenderer tileEntityChestRenderer;
    public TileEntityEnderChestRenderer tileEntityEnderChestRenderer;
    public TileEntityEnchantmentTableRenderer tileEntityEnchantmentTableRenderer;
    public TileEntityEndPortalRenderer tileEntityEndPortalRenderer;
    public TileEntityBeaconRenderer tileEntityBeaconRenderer;
    public TileEntitySkullRenderer tileEntitySkullRenderer;
    public TileEntityBannerRenderer tileEntityBannerRenderer;

    private TileEntityRendererDispatcher()
    {
        this.mapSpecialRenderers.put(TileEntitySign.class, tileEntitySignRenderer = new TileEntitySignRenderer());
        this.mapSpecialRenderers.put(TileEntityMobSpawner.class, tileEntityMobSpawnerRenderer = new TileEntityMobSpawnerRenderer());
        this.mapSpecialRenderers.put(TileEntityPiston.class, tileEntityPistonRenderer = new TileEntityPistonRenderer());
        this.mapSpecialRenderers.put(TileEntityChest.class, tileEntityChestRenderer = new TileEntityChestRenderer());
        this.mapSpecialRenderers.put(TileEntityEnderChest.class, tileEntityEnderChestRenderer = new TileEntityEnderChestRenderer());
        this.mapSpecialRenderers.put(TileEntityEnchantmentTable.class, tileEntityEnchantmentTableRenderer = new TileEntityEnchantmentTableRenderer());
        this.mapSpecialRenderers.put(TileEntityEndPortal.class, tileEntityEndPortalRenderer = new TileEntityEndPortalRenderer());
        this.mapSpecialRenderers.put(TileEntityBeacon.class, tileEntityBeaconRenderer = new TileEntityBeaconRenderer());
        this.mapSpecialRenderers.put(TileEntitySkull.class, tileEntitySkullRenderer = new TileEntitySkullRenderer());
        this.mapSpecialRenderers.put(TileEntityBanner.class, tileEntityBannerRenderer = new TileEntityBannerRenderer());

        for (TileEntitySpecialRenderer<?> tileentityspecialrenderer : this.mapSpecialRenderers.values())
        {
            tileentityspecialrenderer.setRendererDispatcher(this);
        }
    }

    public <T extends TileEntity> TileEntitySpecialRenderer<T> getSpecialRendererByClass(Class <? extends TileEntity> teClass)
    {
        //TileEntitySpecialRenderer <? extends TileEntity > tileentityspecialrenderer = this.mapSpecialRenderers.get(teClass);
        TileEntitySpecialRenderer <? extends TileEntity > tileentityspecialrenderer = this.mapSpecialRenderers.get(teClass);
        if (tileentityspecialrenderer == null && teClass != TileEntity.class)
        {
            System.out.println("böyle bişeyin olma ihtimali yok kanka");
            //tileentityspecialrenderer = this.getSpecialRendererByClass((Class <? extends TileEntity >)teClass.getSuperclass());
            //this.mapSpecialRenderers.put(teClass, tileentityspecialrenderer);
        }

        return (TileEntitySpecialRenderer<T>)tileentityspecialrenderer;
    }

    public <T extends TileEntity> TileEntitySpecialRenderer<T> getSpecialRenderer(T tileEntityIn)
    {

        return tileEntityIn == null ? null : (TileEntitySpecialRenderer<T>) tileEntityIn.renderer();
        //return tileEntityIn == null ? null : this.getSpecialRendererByClass(tileEntityIn.getClass());
    }

    public void cacheActiveRenderInfo(World worldIn, TextureManager textureManagerIn, FontRenderer fontrendererIn, Entity entityIn, float partialTicks)
    {
        if (this.worldObj != worldIn)
        {
            this.setWorld(worldIn);
        }

        this.renderEngine = textureManagerIn;
        this.entity = entityIn;
        this.fontRenderer = fontrendererIn;
        this.entityYaw = entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks;
        this.entityPitch = entityIn.prevRotationPitch + (entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks;
        this.entityX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
        this.entityY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
        this.entityZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
    }

    public void renderTileEntity(TileEntity tileentityIn, float partialTicks, int destroyStage)
    {
        if (tileentityIn.distForRender < tileentityIn.getMaxRenderDistanceSquared())
        {
            int i;
            if (Modules.BRIGHTNESS.isEnabled()) {
                i = 15728880;
            } else {
                i = this.worldObj.getCombinedLight(tileentityIn.getPos(), 0);
            }
            //int i = 15;
            int j = i % 65536;
            int k = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos blockpos = tileentityIn.getPos();
            GlStateManager.enableCull();
            GlStateManager.resetColor();
            this.renderTileEntityAt(tileentityIn, (double)blockpos.getX() - staticPlayerX, (double)blockpos.getY() - staticPlayerY, (double)blockpos.getZ() - staticPlayerZ, partialTicks, destroyStage);
        }
    }

    /**
     * Render this TileEntity at a given set of coordinates
     */
    public void renderTileEntityAt(TileEntity tileEntityIn, double x, double y, double z, float partialTicks)
    {
        this.renderTileEntityAt(tileEntityIn, x, y, z, partialTicks, -1);
    }

    public void renderTileEntityAt(TileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage) {
        TileEntitySpecialRenderer<TileEntity> tileentityspecialrenderer = this.getSpecialRenderer(tileEntityIn);

        if (tileentityspecialrenderer != null) {
            try {
                tileentityspecialrenderer.renderTileEntityAt(tileEntityIn, x, y, z, partialTicks, destroyStage);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Block Entity");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Block Entity Details");
                tileEntityIn.addInfoToCrashReport(crashreportcategory);
                throw new ReportedException(crashreport);
            }
        }
    }

    public void setWorld(World worldIn)
    {
        this.worldObj = worldIn;
    }

    public FontRenderer getFontRenderer()
    {
        return this.fontRenderer;
    }
}
