package ipana.modules.render;

import ipana.Ipana;
import ipana.events.EventRender3D;
import ipana.irc.user.PlayerCosmetics;
import ipana.irc.user.User;
import ipana.irc.user.UserProperties;
import ipana.managements.friend.FriendManager;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.managements.value.values.BoolValue;
import ipana.utils.StringUtil;
import ipana.utils.font.FontHelper;
import ipana.utils.font.FontUtil;
import ipana.utils.gl.GLCall;
import ipana.utils.gl.GList;
import ipana.utils.gl.GLists;
import ipana.utils.player.PlayerUtils;
import ipana.utils.render.EmoteUtils;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import optifine.Config;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class NameTags extends Module {
    public NameTags() {
        super("NameTags", Keyboard.KEY_NONE, Category.Render, "Better name tags.");
        initEnchants();
    }

    private BoolValue items = new BoolValue("Items", this, true, "Shows items.");
    private BoolValue health = new BoolValue("Health", this, true, "Shows health.");
    private BoolValue font = new BoolValue("Font", this, true, "Use the client font.");
    public BoolValue averageEnjoyer = new BoolValue("Average", this, false, "Shows health.");
    private ResourceLocation average1 = new ResourceLocation("mesir/average1.png");
    private ResourceLocation average2 = new ResourceLocation("mesir/average2.png");
    private ResourceLocation averageGhost = new ResourceLocation("mesir/averageGhost.png");
    private ResourceLocation averageOnurEymen = new ResourceLocation("mesir/averageOnurEymen.png");
    private ResourceLocation averageJakless = new ResourceLocation("mesir/averageJakless.png");
    private ResourceLocation averageHakanAbi = new ResourceLocation("mesir/averageHakanAbi.png");
    private HashMap<Enchantment, String> armorEnchants = new HashMap<>();
    private HashMap<Enchantment, String> toolEnchants = new HashMap<>();
    private HashMap<String, ResourceLocation> locations = new HashMap<>();
    private boolean fontToggled;
    private GList<Object> imageRect = new GList<>();
    private boolean initialized;
    private GLists<Object> gLists;

    private Listener<EventRender3D> onDraw = new Listener<>(event -> {
        PlayerUtils.getPlayers().stream().filter(e -> this.angleCheck(e, event.camera())).sorted(Comparator.comparingDouble(e -> -mc.thePlayer.getDistanceSqToEntity(e))).forEach(ent -> {
            double posX = ent.lastTickPosX + (ent.posX - ent.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosX;
            double posY = ent.lastTickPosY + (ent.posY - ent.lastTickPosY) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosY + ent.height + 0.5D;
            double posZ = ent.lastTickPosZ + (ent.posZ - ent.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosZ;
            User user = Ipana.mainIRC().getUser(ent.getName());
            GlStateManager.pushMatrix();
            //RenderUtils.renderVBO(tagsVbo);
            if (user != Ipana.mainIRC().NULL_USER) {
                float scale2 = (float) user.cosmetics().getCosmetic(PlayerCosmetics.CHILD).params()[0];
                double height = 2.3;
                GlStateManager.translate(posX, posY+(height*scale2-height), posZ);
                GlStateManager.scale(scale2, scale2, scale2);
            } else {
                GlStateManager.translate(posX, posY, posZ);
            }
            GlStateManager.disableDepth();
            Camera camera = Modules.CAMERA;
            if (camera.isEnabled()) {
                GlStateManager.rotate(-camera.yaw - 180, 0.0F, 1.0F, 0.0F);
            } else {
                GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            }
            //GL11.glRotatef(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
            float scale = 0.009F;
            float dist = mc.thePlayer.getDistanceToEntity(ent) / 15;
            if (dist < 1) {
                dist = 1;
            }
            GlStateManager.scale(-scale * dist, -scale * dist, -scale * dist);
            int eX = 65;
            int eY = 65;
            float increase = ent == mc.thePlayer ? 15 : 0;
            GlStateManager.translate(0,-increase,0);
            for (EmoteUtils.Action action : ent.activeEmotes()) {
                float scales;
                float ticks = action.prevTicks + (action.leftTicks - action.prevTicks) * event.partialTicks();
                if (action.leftTicks > 10) {
                    scales = Math.min((60-ticks)/3f, 1.1f);
                } else {
                    scales = Math.min((ticks)/2f,1.1f);
                }
                GlStateManager.translate(eX, eY, 0);
                GlStateManager.scale(scales,scales,scales);
                action.emote.render(eX-69, eY-69, 70, 70);
                GlStateManager.disableDepth();
                GlStateManager.scale(1/scales,1/scales,1/scales);
                GlStateManager.translate(-eX, -eY, 0);
                GlStateManager.bindTexture(0);
                eX+=40;
                if (eX > 150) {
                    eX = 65;
                    eY+=40;
                }
            }
            GlStateManager.translate(0,increase,0);
            if (ent == mc.thePlayer) {
                GlStateManager.enableDepth();
                GlStateManager.popMatrix();
                return;
            }
            String name = ent.getDisplayName().getFormattedText();
            String healthText = (health.getValue() ? health(ent) : "");
            boolean isFriend = FriendManager.isFriend(ent.getName());
            if (isFriend) {
                name = StringUtil.combine("§b", ent.getName());
            }
            if (user != Ipana.mainIRC().NULL_USER) {
                name = StringUtil.combine("§7[§d",user.getProperty(UserProperties.CLIENT),"§7] ",(isFriend?"§b":"§f"),ent.getName());
            }
            boolean fontChanged = font.getValue() != fontToggled;
            if (fontChanged) {
                fontToggled = font.getValue();
            }
            String finalName = name;
            if (font.getValue()) {
                GlStateManager.bindTexture(FontHelper.SIZE_48.textureId());
            } else {
                mc.fontRendererObj.bindFontTexture();
            }
            if (fontChanged && Config.isGlCalls()) {
                GLCall.compile(ent.nameTagsList, 0, c -> drawStringWithShadow(FontHelper.SIZE_48, finalName, -50, -5, Color.white));
                GLCall.compile(ent.nameTagsList, 1, c -> drawStringWithShadow(FontHelper.SIZE_48, healthText, 45, 25, Color.white));
            }
            if (GLCall.check(ent.nameTagsList, name, 0)) {
                GLCall.compile(ent.nameTagsList, 0, c -> drawStringWithShadow(FontHelper.SIZE_48, finalName, -50, -5, Color.white));
            }
            if (GLCall.check(ent.nameTagsList, healthText, 1)) {
                GLCall.compile(ent.nameTagsList, 1, c -> drawStringWithShadow(FontHelper.SIZE_48, healthText, 45, 25, Color.white));
            }
            GLCall.drawLists(ent.nameTagsList);
            GlStateManager.scale(2.9, 2.9, 2.9);
            if (items.getValue()) {
                renderItems(ent);
            }
            if (averageEnjoyer.getValue()) {
                ResourceLocation loc;
                if (ent.getName().equals("OnurSins") || ent.getName().equals("DarkChace") || ent.getName().equals("EprocularXx")) {
                    loc = averageOnurEymen;
                } else if (ent.getName().equals("ghost2173") || ent.getName().equals("Diamantiferous")) {
                    loc = averageGhost;
                } else if (ent.getName().equals("J3y_Jakless") || ent.getName().equals("jakless2173")) {
                    loc = averageGhost;
                } else if (ent.getName().equals("cheater09") || ent.getName().equals("Pyle") || ent.getName().equals("Flena")) {
                    loc = averageHakanAbi;
                } else if (FriendManager.isFriend(ent.getName())) {
                    loc = average2;
                } else {
                    loc = average1;
                }
                RenderUtils.drawImage(-22, 16, 40, 74, loc);
            }
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        });
        GlStateManager.resetColor();
    });


    private void renderItems(EntityPlayer ent) {
        float scale = 0.3F;
        mc.getRenderItem().zLevel = -150.0F;
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableAlpha();
        boolean begun = false;
        ItemStack heldItem = ent.getHeldItem();
        drawItemTextures(ent);
        if (heldItem != null && heldItem.getItem() != null) {
            final int[] enchY2 = {0};
            //mc.getRenderItem().renderItemAndEffectIntoGUI(ent.getHeldItem(), -40, -5);
            GlStateManager.scale(scale, scale, scale);
            int x = -130;
            begin(FontHelper.SIZE_18_BOLD);
            ArrayList<PlayerUtils.Enchant> enchantArrayList = PlayerUtils.getEnchants(heldItem);
            for (PlayerUtils.Enchant enchants : enchantArrayList) {
                String enchantName = toolEnchants.get(enchants.enchantment());
                if (enchantName != null) {
                    drawStringWithShadow(FontHelper.SIZE_18_BOLD, StringUtil.combine(enchantName, ": ", enchants.lvl()), x, -15 + enchY2[0], Color.white);
                    enchY2[0] += 10;
                }
            }

            drawStringWithShadow(FontHelper.SIZE_18_BOLD, String.valueOf(heldItem.getMaxDamage() - heldItem.getItemDamage()), -135.0F, 30.0F, Color.yellow);
            drawStringWithShadow(FontHelper.SIZE_18_BOLD, String.valueOf(heldItem.stackSize), -90.0F, 30.0F, Color.white);
            begun = true;
        }
        ItemStack[] inv = ent.getInventory();
        int yPos2 = 95;
        int enchY = 52;
        for (int i = 3; i >= 0; i--) {
            ItemStack item = inv[i];
            if (item != null && item.getItem() != null) {
                int enchY2 = 0;
                int x = -130;
                //mc.getRenderItem().renderItemAndEffectIntoGUI(item, -40, yPos);
                if (!begun) {
                    GlStateManager.scale(scale, scale, scale);
                    begin(FontHelper.SIZE_18_BOLD);
                    begun = true;
                }
                for (PlayerUtils.Enchant enchants : PlayerUtils.getEnchants(item)) {
                    String enchantName = armorEnchants.get(enchants.enchantment());
                    if (enchantName != null) {
                        drawStringWithShadow(FontHelper.SIZE_18_BOLD, StringUtil.combine(enchantName, ": ", enchants.lvl()), x, enchY + enchY2, Color.white);
                        enchY2 += 10;
                    }
                }
                drawStringWithShadow(FontHelper.SIZE_18_BOLD, String.valueOf(item.getMaxDamage() - item.getItemDamage()), -115.0F, yPos2, Color.yellow);
                enchY += 67;
                yPos2 += 65;
            }
        }
        if (begun) {
            end(FontHelper.SIZE_18_BOLD);
        }
        mc.getRenderItem().zLevel = 0.0F;
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private void drawItemTextures(EntityPlayer ent) {
        ItemStack heldItem = ent.getHeldItem();
        if (heldItem != null && heldItem.getItem() != null) {
            if (heldItem.getItem() instanceof ItemTool || heldItem.getItem() instanceof ItemSword) {
                GL11.glTranslatef(0, -5, 0);
                drawImage(-40, 0, 16, 16, heldItem.textureLocation());
                GL11.glTranslatef(0, 5, 0);
            } else {
                mc.getRenderItem().renderItemAndEffectIntoGUI(heldItem, -40, -5);
                GlStateManager.disableDepth();
            }
        }
        ItemStack[] inv = ent.getInventory();
        int yPos = 15;
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = inv[i];
            if (stack != null && stack.getItem() != null) {
                if (stack.getItem() instanceof ItemBlock || stack.getItem() instanceof ItemSkull) {
                    mc.getRenderItem().renderItemAndEffectIntoGUI(stack, -40, yPos);
                    GlStateManager.disableDepth();
                } else {
                    int index = (yPos + 5) / 20;
                    GL11.glTranslatef(0, (index) * 20 - 5, 0);
                    drawImage(-40, 0, 16, 16, stack.textureLocation());
                    GL11.glTranslatef(0, -(index) * 20 + 5, 0);
                }
                yPos += 20;
            }
        }
    }

    private String health(EntityPlayer ent) {
        double h = (Math.ceil(ent.getHealth() + ent.getAbsorptionAmount()) / 2.0D);
        String colorCode;
        if (ent.getHealth() >= 15) {
            colorCode = "§a";
        } else if (ent.getHealth() >= 5) {
            colorCode = "§e";
        } else {
            colorCode = "§c";
        }
        return StringUtil.combine(colorCode,h);
    }

    private boolean angleCheck(EntityPlayer ent, Frustum camera) {
        return ent.getEntityBoundingBox() != null && camera.isBoundingBoxInFrustum(ent.getEntityBoundingBox());
    }

    private void drawStringWithShadow(FontUtil font, String text, float x, float y, Color color) {
        if (this.font.getValue()) {
            font.drawStringWithShadow(text,x,y,color);
        } else {
            if (x == -50 && y == -5) {
                float scale2 = 2.5f;
                GlStateManager.scale(scale2,scale2,scale2);
                mc.fontRendererObj.drawStringWithShadow(text,x+30,y+5,color.getRGB());
            } else if (x == 45 && y == 25) {
                mc.fontRendererObj.drawStringWithShadow(text,x-20,y-10,color.getRGB());
                float scale2 = 2.5f;
                GlStateManager.scale(1/scale2, 1/scale2, 1/scale2);
            } else {
                mc.fontRendererObj.drawStringWithShadow(text,x,y,color.getRGB());
            }
        }
    }

    private void begin(FontUtil font) {
        if (this.font.getValue()) {
            font.setAutoBegin(false);
            font.begin();
        } else {
            mc.fontRendererObj.setAutoBegin(false);
            mc.fontRendererObj.begin();
        }
    }

    private void end(FontUtil font) {
        if (this.font.getValue()) {
            font.end();
            font.setAutoBegin(true);
        } else {
            mc.fontRendererObj.end();
            mc.fontRendererObj.setAutoBegin(true);
        }
    }

    private ResourceLocation getLocation(String location) {
        return locations.computeIfAbsent(location, a -> new ResourceLocation(location));
    }

    private void initEnchants() {
        armorEnchants.put(Enchantment.protection, "P");
        armorEnchants.put(Enchantment.thorns, "Th");
        armorEnchants.put(Enchantment.aquaAffinity, "Aq");
        armorEnchants.put(Enchantment.depthStrider, "Ds");
        armorEnchants.put(Enchantment.unbreaking, "Unb");

        toolEnchants.put(Enchantment.sharpness, "Sh");
        toolEnchants.put(Enchantment.unbreaking, "Unb");
        toolEnchants.put(Enchantment.smite, "Sm");
        toolEnchants.put(Enchantment.knockback, "Kb");
        toolEnchants.put(Enchantment.fireAspect, "Fa");
        toolEnchants.put(Enchantment.efficiency, "Ef");
        toolEnchants.put(Enchantment.fortune, "Fo");
        toolEnchants.put(Enchantment.flame, "Fl");
        toolEnchants.put(Enchantment.silkTouch, "St");
        toolEnchants.put(Enchantment.looting, "Lo");
        toolEnchants.put(Enchantment.lure, "Lu");
        toolEnchants.put(Enchantment.luckOfTheSea, "Ls");
    }

    public void drawImage(double x, double y, double xWidth, double yWidth, ResourceLocation image) {
        double par1 = x + xWidth;
        double par2 = y + yWidth;
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        if (!initialized) {
            imageRect.compile(c -> {
                GL11.glColor4f(1,1,1,1);
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
                worldrenderer.pos(x, par2, 0.0D).tex(0.0D, 1.0D).endVertex();
                worldrenderer.pos(par1, par2, 0.0D).tex(1.0D, 1.0D).endVertex();
                worldrenderer.pos(par1, y, 0.0D).tex(1.0D, 0.0D).endVertex();
                worldrenderer.pos(x, y, 0.0D).tex(0.0D, 0.0D).endVertex();
                tessellator.draw();
            });
            initialized = true;
        }
        imageRect.render();
        GlStateManager.resetColor();
    }
}
