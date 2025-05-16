package ipana.renders.ingame.cosmetics;

import ipana.Ipana;
import ipana.irc.user.PlayerCosmetics;
import ipana.irc.user.User;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import static ipana.irc.user.PlayerCosmetics.*;

public class CosmeticRenders {
    Minecraft mc = Minecraft.getMinecraft();
    CosmeticsGui gui;

    public CosmeticRenders(CosmeticsGui gui) {
        this.gui = gui;
    }

    public void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, AbstractClientPlayer ent) {
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
        float f5 = ent.rotationPitchHead;
        float f6 = ent.prevRotationPitchHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        ent.renderYawOffset = mouseX+135;
        ent.rotationYaw = mouseX+135;
        ent.rotationPitch = mouseY;
        ent.rotationPitchHead = mouseY;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationPitchHead = ent.rotationPitchHead;
        ent.prevRotationYawHead = ent.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        ItemStack[] stacks = ent.getInventory().clone();
        if (!gui.renderArmor) {
            for (int i = 0; i < 4; i++) {
                ent.getInventory()[i] = null;
            }
        }
        ItemStack held = ent.getHeldItem();
        ent.inventory.setCurrentItem(null);
        User self = Ipana.mainIRC().self();
        PlayerCosmetics cosmetics = self.cosmetics();
        float scale2 = (float) cosmetics.getCosmetic(CHILD).params()[0];
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        cosmetics.setCosmetics(CHILD, 1f);
        rendermanager.renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        rendermanager.setRenderShadow(true);
        ent.inventory.setCurrentItem(held);
        for (int i = 0; i < 4; i++) {
            ent.getInventory()[i] = stacks[i];
        }
        cosmetics.setCosmetics(CHILD, scale2);
        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;
        ent.rotationPitchHead = f5;
        ent.prevRotationPitchHead = f6;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
    public void drawAmongus(boolean lgbt) {
        GlStateManager.enableColorMaterial();

        GlStateManager.translate((float) 0, (float) 0, 50.0F);
        GlStateManager.scale((float)(-10), (float) 10, (float) 10);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        AbstractClientPlayer ent = mc.thePlayer;
        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;
        float f5 = ent.rotationPitchHead;
        float f6 = ent.prevRotationPitchHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        ent.renderYawOffset = (float) -45 +135;
        ent.rotationYaw = (float) -45 +135;
        ent.rotationPitch = (float) 0;
        ent.rotationPitchHead = (float) 0;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationPitchHead = ent.rotationPitchHead;
        ent.prevRotationYawHead = ent.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        ItemStack[] stacks = ent.getInventory().clone();
        if (!gui.renderArmor) {
            for (int i = 0; i < 4; i++) {
                ent.getInventory()[i] = null;
            }
        }
        ItemStack held = ent.getHeldItem();
        ent.inventory.setCurrentItem(null);
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        User self = Ipana.mainIRC().self();
        PlayerCosmetics cosmetics = self.cosmetics();
        String model = String.valueOf(cosmetics.getCosmetic(PlayerCosmetics.MODELS).params()[0]);
        ResourceLocation location = (ResourceLocation) cosmetics.getCosmetic(PlayerCosmetics.MODELS).params()[1];
        String modelName = lgbt ? "lgbt_amongus" : "normal_amongus";
        float scale = (float) cosmetics.getCosmetic(CHILD).params()[0];
        cosmetics.setCosmetics(CHILD, 1f);
        cosmetics.setCosmetics(PlayerCosmetics.MODELS, modelName,new ResourceLocation("mesir/models/"+modelName+".png"));
        Minecraft.getMinecraft().getRenderManager().renderAmongUs.doRender( ent, 0, 0, 0, 0, 1);
        rendermanager.setRenderShadow(true);
        ent.inventory.setCurrentItem(held);
        for (int i = 0; i < 4; i++) {
            ent.getInventory()[i] = stacks[i];
        }
        cosmetics.setCosmetics(CHILD, scale);
        cosmetics.setCosmetics(PlayerCosmetics.MODELS, model,location);
        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;
        ent.rotationPitchHead = f5;
        ent.prevRotationPitchHead = f6;

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
    public void drawEla() {
        GlStateManager.enableColorMaterial();
        GlStateManager.translate(0, 0, 50.0F);
        GlStateManager.scale((-10), 10, 10);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        AbstractClientPlayer ent = mc.thePlayer;
        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        ent.renderYawOffset = (float) -45 + 135;
        ent.rotationYaw = (float) -45 + 135;
        ent.rotationPitch = 0;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        ItemStack[] stacks = ent.getInventory().clone();
        if (!gui.renderArmor) {
            for (int i = 0; i < 4; i++) {
                ent.getInventory()[i] = null;
            }
        }
        ItemStack held = ent.getHeldItem();
        ent.inventory.setCurrentItem(null);
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        User self = Ipana.mainIRC().self();
        PlayerCosmetics cosmetics = self.cosmetics();
        String model = String.valueOf(cosmetics.getCosmetic(PlayerCosmetics.MODELS).params()[0]);
        ResourceLocation location = (ResourceLocation) cosmetics.getCosmetic(PlayerCosmetics.MODELS).params()[1];
        cosmetics.setCosmetics(PlayerCosmetics.MODELS, "ela", new ResourceLocation("mesir/models/ela.png"));
        float scale = (float) cosmetics.getCosmetic(CHILD).params()[0];
        cosmetics.setCosmetics(CHILD, 1f);
        Minecraft.getMinecraft().getRenderManager().doRenderEntity(ent, 0, 0, 0, 0, 1, false);
        rendermanager.setRenderShadow(true);
        ent.inventory.setCurrentItem(held);
        for (int i = 0; i < 4; i++) {
            ent.getInventory()[i] = stacks[i];
        }
        cosmetics.setCosmetics(PlayerCosmetics.MODELS, model, location);
        cosmetics.setCosmetics(CHILD, scale);
        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
    public void drawWithCape(Element element, String shaderName, PlayerCosmetics.CapeType type) {
        GlStateManager.enableColorMaterial();
        GlStateManager.translate(-5, 12, 50.0F);
        GlStateManager.scale((-20), 20, 20);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        AbstractClientPlayer ent = mc.thePlayer;
        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        ent.renderYawOffset = -70;
        ent.rotationYaw = -70;
        ent.rotationPitch = 0;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = mc.getRenderManager();
        ItemStack[] stacks = ent.getInventory().clone();
        if (!gui.renderArmor) {
            for (int i = 0; i < 4; i++) {
                ent.getInventory()[i] = null;
            }
        }
        ItemStack held = ent.getHeldItem();
        ent.inventory.setCurrentItem(null);
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        double d0 = ent.chasingPosX, d1 = ent.chasingPosY, d2 = ent.chasingPosZ,
                d3 = ent.prevChasingPosX, d4 = ent.prevChasingPosY, d5 = ent.prevChasingPosZ,
                d6 = ent.cameraYaw, d7 = ent.prevCameraYaw,
                d8 = ent.distanceWalkedModified, d9 = ent.prevDistanceWalkedModified,
                d10 = ent.rotationYaw, d11 = ent.prevRotationYaw,
                d12 = ent.posX, d13 = ent.posY, d14 = ent.posZ,
                d15 = ent.prevPosX, d16 = ent.prevPosY, d17 = ent.prevPosZ;
        ent.posX = ent.posY = ent.posZ = ent.prevPosX = ent.prevPosY = ent.prevPosZ = ent.chasingPosX = ent.chasingPosY = ent.chasingPosZ = ent.prevChasingPosX = ent.prevChasingPosY = ent.prevChasingPosZ = ent.cameraYaw = ent.prevCameraYaw = ent.distanceWalkedModified = ent.prevDistanceWalkedModified = ent.rotationYaw = ent.prevRotationYaw = 0.2f;


        User self = Ipana.mainIRC().self();
        PlayerCosmetics cosmetics = self.cosmetics();
        //Old model properties
        String model = String.valueOf(cosmetics.getCosmetic(MODELS).params()[0]);
        ResourceLocation modelLocation = (ResourceLocation) cosmetics.getCosmetic(MODELS).params()[1];
        //Old cape properties
        String capeName = String.valueOf(cosmetics.getCosmetic(CAPE).params()[0]);
        String shader = String.valueOf(cosmetics.getCosmetic(CAPE).params()[1]);
        PlayerCosmetics.CapeType capeType = PlayerCosmetics.CapeType.valueOf(String.valueOf(cosmetics.getCosmetic(CAPE).params()[2]));
        ResourceLocation capeLocation = (ResourceLocation) cosmetics.getCosmetic(CAPE).params()[3];

        cosmetics.setCosmetics(MODELS, "none", null);
        String capeShit = "Custom".equals(element.elementName) ? gui.capeText.getText() : element.elementName;
        cosmetics.setCosmetics(CAPE, capeShit, shaderName, type, cosmetics.parseCape(capeShit));
        Integer earColor = Integer.valueOf(String.valueOf(cosmetics.getCosmetic(EARS).params()[0]));
        cosmetics.setCosmetics(EARS, -2173);
        float scale = (float) cosmetics.getCosmetic(CHILD).params()[0];
        cosmetics.setCosmetics(CHILD, 1f);
        boolean invis = mc.thePlayer.isInvisible();
        mc.thePlayer.setInvisible(true);
        mc.getRenderManager().doRenderEntity(mc.thePlayer, 0, 0, 0, 0, 1, false);
        mc.thePlayer.setInvisible(invis);
        rendermanager.setRenderShadow(true);
        for (int i = 0; i < 4; i++) {
            ent.getInventory()[i] = stacks[i];
        }
        ent.inventory.setCurrentItem(held);
        cosmetics.setCosmetics(MODELS, model, modelLocation);
        cosmetics.setCosmetics(CAPE, capeName, shader, capeType, capeLocation);
        cosmetics.setCosmetics(EARS, earColor);
        cosmetics.setCosmetics(CHILD, scale);

        ent.chasingPosX = d0; ent.chasingPosY = d1; ent.chasingPosZ = d2;
        ent.prevChasingPosX = d3; ent.prevChasingPosY = d4; ent.prevChasingPosZ = d5;
        ent.cameraYaw = (float) d6; ent.prevCameraYaw = (float) d7;
        ent.distanceWalkedModified = (float) d8; ent.prevDistanceWalkedModified = (float) d9;
        ent.rotationYaw = (float) d10; ent.prevRotationYaw = (float) d11;
        ent.posX = d12; ent.posY = d13; ent.posZ = d14; ent.prevPosX = d15; ent.prevPosY = d16; ent.prevPosZ = d17;
        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public void drawWithEars(Element element, int color) {
        GlStateManager.enableColorMaterial();
        GlStateManager.translate(-5, 32, 50.0F);
        GlStateManager.scale((-20), 20, 20);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        AbstractClientPlayer ent = mc.thePlayer;
        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        ent.renderYawOffset = -70;
        ent.rotationYaw = -70;
        ent.rotationPitch = 0;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = mc.getRenderManager();
        ItemStack[] stacks = ent.getInventory().clone();
        ItemStack held = ent.getHeldItem();
        ent.inventory.setCurrentItem(null);
        if (!gui.renderArmor) {
            for (int i = 0; i < 4; i++) {
                ent.getInventory()[i] = null;
            }
        }
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        User self = Ipana.mainIRC().self();
        PlayerCosmetics cosmetics = self.cosmetics();
        String model = String.valueOf(cosmetics.getCosmetic(MODELS).params()[0]);
        ResourceLocation modelLocation = (ResourceLocation) cosmetics.getCosmetic(MODELS).params()[1];
        Integer earColor = Integer.valueOf(String.valueOf(cosmetics.getCosmetic(EARS).params()[0]));
        String capeName = String.valueOf(cosmetics.getCosmetic(CAPE).params()[0]);
        String shader = String.valueOf(cosmetics.getCosmetic(CAPE).params()[1]);
        PlayerCosmetics.CapeType capeType = PlayerCosmetics.CapeType.valueOf(String.valueOf(cosmetics.getCosmetic(CAPE).params()[2]));
        ResourceLocation capeLocation = (ResourceLocation) cosmetics.getCosmetic(CAPE).params()[3];
        float scale = (float) cosmetics.getCosmetic(CHILD).params()[0];
        cosmetics.setCosmetics(CHILD, 1f);
        cosmetics.setCosmetics(CAPE, "none", "none", PlayerCosmetics.CapeType.EMPTY, new ResourceLocation(""));
        cosmetics.setCosmetics(MODELS, "none", null);
        cosmetics.setCosmetics(EARS, color);
        boolean invis = mc.thePlayer.isInvisible();
        mc.thePlayer.setInvisible(true);
        mc.getRenderManager().doRenderEntity(mc.thePlayer, 0, 0, 0, 0, 1, false);
        mc.thePlayer.setInvisible(invis);
        rendermanager.setRenderShadow(true);
        ent.inventory.setCurrentItem(held);
        for (int i = 0; i < 4; i++) {
            ent.getInventory()[i] = stacks[i];
        }
        cosmetics.setCosmetics(MODELS, model, modelLocation);
        cosmetics.setCosmetics(EARS, earColor);
        cosmetics.setCosmetics(CAPE, capeName, shader, capeType, capeLocation);
        cosmetics.setCosmetics(CHILD, scale);
        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}
