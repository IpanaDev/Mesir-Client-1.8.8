package ipana.modules.combat.modes.killaura;

import ipana.Ipana;
import ipana.managements.module.Modules;
import ipana.modules.combat.KillAura;
import ipana.utils.font.FontHelper;
import ipana.utils.font.FontUtil;
import ipana.utils.render.ColorUtil;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class TargetInfo {
    public static final TargetInfo INSTANCE = new TargetInfo();
    private Minecraft mc = Minecraft.getMinecraft();
    private FontUtil font = FontHelper.SIZE_18;
    private List<Color> colorList;
    private long ms;
    private double prevAnimX, animX;
    public float prevScale, scale;

    public TargetInfo() {
        colorList = ColorUtil.straightLine();
        Collections.reverse(colorList);
    }

    public void onTick(EntityLivingBase target) {
        KillAura ka = Modules.KILL_AURA;
        prevScale = scale;
        if (ka.targets.isEmpty() && scale > 0) {
            scale -= 0.3f;
        } else if (!ka.targets.isEmpty() && scale < 1) {
            scale += 0.3f;
        }
        scale = MathHelper.clamp_float(scale, 0, 1);
        if (!ka.targets.isEmpty()) {
            int width = 120;
            double colorWidth2 = (width) / target.getMaxHealth() * target.getHealth();
            double m = 5;
            prevAnimX = animX;
            if (animX > colorWidth2) {
                animX -= m;
                if (animX < colorWidth2) {
                    animX = colorWidth2;
                }
            }
            if (animX < colorWidth2) {
                animX += m;
                if (animX > colorWidth2) {
                    animX = colorWidth2;
                }
            }
        }
    }

    public void draw(EntityLivingBase target, float partialTicks) {
        if (scale == 0 && prevScale == 0 || target == null) {
            return;
        }

        ScaledResolution sr = RenderUtils.SCALED_RES;
        int helmetDiff = getDiff(target.getInventory()[3],mc.thePlayer.getInventory()[3]);
        int chestDiff = getDiff(target.getInventory()[2],mc.thePlayer.getInventory()[2]);
        int legsDiff = getDiff(target.getInventory()[1],mc.thePlayer.getInventory()[1]);
        int bootsDiff = getDiff(target.getInventory()[0],mc.thePlayer.getInventory()[0]);
        KillAura ka = Modules.KILL_AURA;
        double[] p = ka.infoPosition.getPosition(sr);
        int x = (int) p[0]-20;
        int y = (int) p[1];
        int width = 120;
        int height = 80;
        boolean shouldScale = scale != 1 || prevScale != 1;
        float scaleFactor = prevScale + (scale - prevScale) * partialTicks;
        if (shouldScale) {
            GlStateManager.translate(x-2+width/2f, y+height/2f, 0);
            GlStateManager.scale(scaleFactor, scaleFactor, 1);
            GlStateManager.translate(-(x-2+width/2f), -(y+height/2f), 0);
        }
        Color bodyColor = Ipana.getClientColor();
        Color top = new Color(Math.max(0,Ipana.getClientColor().getRed()-50),Math.max(0,Ipana.getClientColor().getGreen()-50),Math.max(0,Ipana.getClientColor().getBlue()-50));
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        RenderUtils.drawRoundedRect(x-2,y-20,width+4,height,5f,top);
        Gui.drawRect(x-2,y-2,x+width+2,y+4,bodyColor.getRGB());
        RenderUtils.drawRoundedRect(x-2,y,width+4,height,5f,bodyColor);
        //RenderUtils.drawBorderedGradientRect(x, y, x+width, y+height, 1000, 2, fadedColor,reversedFadedColor,reversedColor,color);
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();

        GlStateManager.color(1, 1, 1,1);
        drawEntityOnScreen(x+19, y+67, 35, -25, 0, target);

        //RenderUtils.rectangleBordered(x+36, y+7, x+42, y+65, 1, new Color(1,1,1,1).getRGB(), Color.black.getRGB());
        font.drawString(target.getName(),x+(width/2f-font.getWidth(target.getName())/2),y-14,Color.white.getRGB());
        double colorWidth = width / (double) ColorUtil.straightLine().size();
        double colorX = x;
        RenderUtils.drawRoundedRect(x-1,y+height-9,width+2, 5,2f, Color.darkGray);
        double animTroll = prevAnimX + (animX - prevAnimX) * partialTicks;
        for (Color color : colorList) {
            if (colorX-x > animTroll) {
                break;
            }
            Gui.drawRect(colorX,y+height-8,colorX+colorWidth,y+height-5,color.getRGB());
            colorX+=colorWidth;
        }
        font.drawStringWithShadow(getDiffText(helmetDiff), x+40, y+5, Color.white.getRGB());
        font.drawStringWithShadow(getDiffText(chestDiff), x+40, y+23, Color.white.getRGB());
        font.drawStringWithShadow(getDiffText(legsDiff), x+40, y+41, Color.white.getRGB());
        font.drawStringWithShadow(getDiffText(bootsDiff), x+40, y+59, Color.white.getRGB());
        double pHealth = Math.ceil(mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount()) / 2;
        double tHealth = Math.ceil(target.getHealth() + target.getAbsorptionAmount()) / 2;
        mc.getTextureManager().bindTexture(GuiIngame.icons);
        Gui.drawModalRectWithCustomSizedTexture(x+65, y+4, 34,0,9,9,256,256);
        font.drawStringWithShadow(getDiffText(pHealth - tHealth), x+75, y+5, Color.white.getRGB());
        ItemStack[] stacks = new ItemStack[]{new ItemStack(Items.potionitem, 0, 8229), new ItemStack(Items.potionitem, 0, 8233), new ItemStack(Items.potionitem, 0, 8226)};
        int[] sizes = new int[3];
        ItemStack[] inv = target instanceof EntityOtherPlayerMP player ? player.inventory.mainInventory : target.getInventory();
        for (ItemStack stack : inv) {
            if (stack != null && stack.getItem() instanceof ItemPotion) {
                for (int i = 0; i < stacks.length; i++) {
                    ItemStack stack1 = stacks[i];
                    if (stack1.getMetadata() == stack.getMetadata()) {
                        sizes[i] += stack.stackSize;
                    }
                }
            }
        }
        int xOffset = x+61;
        int yOffset = y+15;
        int yInc = 18;
        for (int i = 0; i < 3; i++) {
            mc.getRenderItem().renderItemIntoGUI(stacks[i], xOffset,yOffset+yInc*i);
            font.drawStringWithShadow(String.valueOf(sizes[i]), xOffset+16, yOffset+8+yInc*i, Color.white);
        }
        for (PotionEffect effect : target.getActivePotionEffects()) {
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            if (potion != Potion.moveSpeed && potion != Potion.damageBoost && potion != Potion.heal) {
                continue;
            }
            int idFromPot = potion == Potion.moveSpeed ? 2 : potion == Potion.damageBoost ? 1 : 0;
            String duration = Potion.getDurationString(effect);
            String amplifier = String.valueOf(effect.getAmplifier() + 1);
            String finalString = "§f(" + I18n.format("enchantment.level."+amplifier) + "§8-§f" + duration+")";
            font.drawStringWithShadow(finalString, xOffset+16+font.getWidth(String.valueOf(sizes[idFromPot])), yOffset+8+yInc*idFromPot, potion.getLiquidColor());
        }
        if (shouldScale) {
            GlStateManager.translate(x-2+width/2f, y+height/2f, 0);
            GlStateManager.scale(1f / scaleFactor, 1f / scaleFactor, 1);
            GlStateManager.translate(-(x-2+width/2f), -(y+height/2f), 0);
        }
        //font.drawStringWithShadow("§fHealth : "+getDiffText((pHealth-tHealth)/2), x+45, y+45, Color.white.getRGB());
    }

    private String getDiffText(int diff) {
        if (diff < 0) {
            return ("§c"+diff).replace("-","");
        } else if (diff > 0) {
            return "§a"+diff;
        } else {
            return "§e"+diff;
        }
    }
    private String getDiffText(double diff) {
        if (diff < 0) {
            return ("§c"+diff);
        } else if (diff > 0) {
            return "§a+"+diff;
        } else {
            return "§e"+diff;
        }
    }
    public void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)posX, (float)posY, 50.0F);
        GlStateManager.scale((float)(-scale), (float)scale, (float)scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;
        float f5 = ent.prevRotationPitch;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-((float)Math.atan((mouseY / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        ent.renderYawOffset = mouseX;
        ent.rotationYaw = mouseX;
        ent.prevRotationPitch = -mouseY;
        ent.rotationPitch = -mouseY;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        rendermanager.setRenderShadow(true);
        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationPitch = f5;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
    private int getDiff(ItemStack stack, ItemStack pStack) {
        int diff = 0;
        if (pStack == null && stack != null) {
            diff = -(stack.getMaxDamage()-stack.getItemDamage());
        } else if (stack == null && pStack != null) {
            diff = (pStack.getMaxDamage()-pStack.getItemDamage());
        } else if (stack != null) {
            diff = (pStack.getMaxDamage()-pStack.getItemDamage())-(stack.getMaxDamage()-stack.getItemDamage());
        }
        return diff;
    }
}
