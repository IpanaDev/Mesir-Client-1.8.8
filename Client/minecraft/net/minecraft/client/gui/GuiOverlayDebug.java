package net.minecraft.client.gui;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.Map.Entry;

import ipana.utils.StringUtil;
import ipana.utils.gl.GLCall;
import ipana.utils.gl.GList;
import ipana.utils.gl.GLists;
import ipana.utils.player.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import optifine.Config;
import optifine.Reflector;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class GuiOverlayDebug extends Gui
{
    private final Minecraft mc;
    private final FontRenderer fontRenderer;
    //private GList<String>[] left = new GList[15];
    private GLists<String> gLists = new GLists<>(35);

    public GuiOverlayDebug(Minecraft mc) {
        this.mc = mc;
        this.fontRenderer = mc.fontRendererObj;
    }

    public void renderDebugInfo(ScaledResolution scaledResolutionIn) {
        this.mc.mcProfiler.startSection("debug");
        GlStateManager.pushMatrix();
        fontRenderer.bindFontTexture();
        this.renderDebugInfoLeft();
        this.renderDebugInfoRight(scaledResolutionIn);
        GLCall.drawLists(gLists);
        GlStateManager.popMatrix();
        this.mc.mcProfiler.endSection();
    }

    private boolean isReducedDebug() {
        return this.mc.thePlayer.hasReducedDebug() || this.mc.gameSettings.reducedDebugInfo;
    }

    protected void renderDebugInfoLeft() {
        List<String> list = this.call();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        for (int i = 0; i < list.size(); ++i) {
            String s = list.get(i);
            int finalI = i;
            GLCall.checkAndCompile(gLists, s, i, c -> {
                if (!Strings.isNullOrEmpty(s)) {
                    int j = this.fontRenderer.FONT_HEIGHT;
                    int k = this.fontRenderer.getStringWidth(s);
                    int l = 2 + j * finalI;
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    drawRectNoStates(1, l - 1, 2 + k + 1, l + j - 1, -1873784752);
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                    this.fontRenderer.drawString(s, 2, l, 14737632);
                }
            });
        }
        GlStateManager.enableTexture2D();
        if (list.size() == 14 && Config.isGlCalls()) {
            gLists.compile(14, c -> {});
        }
    }

    protected void renderDebugInfoRight(ScaledResolution p_175239_1_) {
        List<String> list = this.getDebugInfoRight();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        for (int i = 0; i < list.size(); ++i) {
            String s = list.get(i);
            int finalI = i;
            GLCall.checkAndCompile(gLists, s, i+15, c -> {
                if (!Strings.isNullOrEmpty(s)) {
                    int j = this.fontRenderer.FONT_HEIGHT;
                    int k = this.fontRenderer.getStringWidth(s);
                    int l = p_175239_1_.getScaledWidth() - 2 - k;
                    int i1 = 2 + j * finalI;
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    drawRectNoStates(l - 1, i1 - 1, l + k + 1, i1 + j - 1, -1873784752);
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                    this.fontRenderer.drawString(s, l, i1, 14737632);
                }
            });
        }
        GlStateManager.enableTexture2D();
        if (list.size() == 9 && Config.isGlCalls()) {
            for (int i = 25; i < gLists.size(); i++) {
                gLists.checkAndCompile("", i, c-> {});
            }
        }
    }

    protected List<String> call() {
        BlockPos blockpos = new BlockPos(this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().getEntityBoundingBox().minY, this.mc.getRenderViewEntity().posZ);

        if (this.isReducedDebug()) {
            return Lists.newArrayList("Minecraft 1.8.8 (" + this.mc.getVersion() + "/" + ClientBrandRetriever.getClientModName() + ")", this.mc.debug, this.mc.renderGlobal.getDebugInfoRenders(), this.mc.renderGlobal.getDebugInfoEntities(), "P: " + this.mc.effectRenderer.getStatistics() + ". T: " + this.mc.theWorld.getDebugLoadedEntities(), this.mc.theWorld.getProviderName(), "", String.format("Chunk-relative: %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15));
        } else {
            Entity entity = this.mc.getRenderViewEntity();
            EnumFacing enumfacing = entity.getHorizontalFacing();
            String s = switch (GuiOverlayDebug$1.field_178907_a[enumfacing.ordinal()]) {
                case 1 -> "Towards negative Z";
                case 2 -> "Towards positive Z";
                case 3 -> "Towards negative X";
                case 4 -> "Towards positive X";
                default -> "Invalid";
            };
            ArrayList<String> arraylist = Lists.newArrayList(
                    StringUtil.combine("Minecraft 1.8.8 (", this.mc.getVersion(), "/", ClientBrandRetriever.getClientModName(), ")"),
                    this.mc.debug,
                    this.mc.renderGlobal.getDebugInfoRenders(),
                    this.mc.renderGlobal.getDebugInfoEntities(),
                    StringUtil.combine("P: " , this.mc.effectRenderer.getStatistics() , ". T: " , this.mc.theWorld.getDebugLoadedEntities()),
                    this.mc.theWorld.getProviderName(),
                    "",
                    String.format("XYZ: %.3f / %.5f / %.3f", this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().getEntityBoundingBox().minY, this.mc.getRenderViewEntity().posZ),
                    String.format("Block: %d %d %d", blockpos.getX(), blockpos.getY(), blockpos.getZ()),
                    String.format("Chunk: %d %d %d in %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15, blockpos.getX() >> 4, blockpos.getY() >> 4, blockpos.getZ() >> 4),
                    String.format("Facing: %s (%s) (%.1f / %.1f)", enumfacing, s, entity.rotationYaw % 360.0f, MathHelper.wrapAngleTo180_float(entity.rotationPitch)));
            if (this.mc.theWorld != null && this.mc.theWorld.isBlockLoaded(blockpos)) {
                Chunk chunk = this.mc.theWorld.getChunkFromBlockCoords(blockpos);
                arraylist.add(StringUtil.combine("Biome: " , chunk.getBiome(blockpos, this.mc.theWorld.getWorldChunkManager()).biomeName));
                arraylist.add(StringUtil.combine("Light: " , chunk.getLightSubtracted(blockpos, 0) , " (" , chunk.getLightFor(EnumSkyBlock.SKY, blockpos) , " sky, " , chunk.getLightFor(EnumSkyBlock.BLOCK, blockpos) , " block)"));
                DifficultyInstance difficultyinstance = this.mc.theWorld.getDifficultyForLocation(blockpos);

                if (this.mc.isIntegratedServerRunning() && this.mc.getIntegratedServer() != null) {
                    EntityPlayerMP entityplayermp = this.mc.getIntegratedServer().getConfigurationManager().getPlayerByUUID(this.mc.thePlayer.getUniqueID());

                    if (entityplayermp != null) {
                        difficultyinstance = entityplayermp.worldObj.getDifficultyForLocation(new BlockPos(entityplayermp));
                    }
                }

                arraylist.add(String.format("Local Difficulty: %.2f (Day %d)", difficultyinstance.getAdditionalDifficulty(), this.mc.theWorld.getWorldTime() / 24000L));
            }

            if (this.mc.entityRenderer != null && this.mc.entityRenderer.isShaderActive()) {
                arraylist.add("Shader: " + this.mc.entityRenderer.getShaderGroup().getShaderGroupName());
            }

            if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && this.mc.objectMouseOver.getBlockPos() != null) {
                BlockPos blockpos1 = this.mc.objectMouseOver.getBlockPos();
                arraylist.add(String.format("Looking at: %d %d %d", blockpos1.getX(), blockpos1.getY(), blockpos1.getZ()));
            }

            return arraylist;
        }
    }

    protected List<String> getDebugInfoRight()
    {
        long i = Runtime.getRuntime().maxMemory();
        long j = Runtime.getRuntime().totalMemory();
        long k = Runtime.getRuntime().freeMemory();
        long l = j - k;
        ArrayList<String> arraylist = Lists.newArrayList(
                String.format("Java: %s %dbit", System.getProperty("java.version"), this.mc.isJava64bit() ? 64 : 32),
                String.format("Mem: % 2d%% %03d/%03dMB", l * 100L / i, bytesToMb(l), bytesToMb(i)),
                String.format("Allocated: % 2d%% %03dMB", j * 100L / i, bytesToMb(j)), "",
                String.format("CPU: %s", OpenGlHelper.func_183029_j()), "",
                String.format("Display: %dx%d (%s)", Display.getWidth(), Display.getHeight(), GL11.glGetString(GL11.GL_VENDOR)),
                GL11.glGetString(GL11.GL_RENDERER),
                GL11.glGetString(GL11.GL_VERSION));

        if (!this.isReducedDebug()) {
            if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && this.mc.objectMouseOver.getBlockPos() != null) {
                BlockPos blockpos = this.mc.objectMouseOver.getBlockPos();
                IBlockState iblockstate = this.mc.theWorld.getBlockState(blockpos);

                if (this.mc.theWorld.getWorldType() != WorldType.DEBUG_WORLD) {
                    iblockstate = iblockstate.getBlock().getActualState(iblockstate, this.mc.theWorld, blockpos);
                }

                arraylist.add("");
                arraylist.add(String.valueOf(Block.blockRegistry.getNameForObject(iblockstate.getBlock())));
                Entry entry;
                String s;

                for (Iterator iterator = iblockstate.getProperties().entrySet().iterator(); (iterator).hasNext(); arraylist.add(((IProperty) entry.getKey()).getName() + ": " + s)) {
                    entry = (Entry) iterator.next();
                    s = (entry.getValue()).toString();

                    if (entry.getValue() == Boolean.TRUE) {
                        s = EnumChatFormatting.GREEN + s;
                    } else if (entry.getValue() == Boolean.FALSE) {
                        s = EnumChatFormatting.RED + s;
                    }
                }
            }

        }
        return arraylist;
    }

    private static long bytesToMb(long bytes)
    {
        return bytes / 1024L / 1024L;
    }

    static final class GuiOverlayDebug$1
    {
        static final int[] field_178907_a = new int[EnumFacing.VALUES.length];


        static
        {
            try
            {
                field_178907_a[EnumFacing.NORTH.ordinal()] = 1;
            }
            catch (NoSuchFieldError var4)
            {
                ;
            }

            try
            {
                field_178907_a[EnumFacing.SOUTH.ordinal()] = 2;
            }
            catch (NoSuchFieldError var3)
            {
                ;
            }

            try
            {
                field_178907_a[EnumFacing.WEST.ordinal()] = 3;
            }
            catch (NoSuchFieldError var2)
            {
                ;
            }

            try
            {
                field_178907_a[EnumFacing.EAST.ordinal()] = 4;
            }
            catch (NoSuchFieldError var1)
            {
                ;
            }
        }
    }
}
