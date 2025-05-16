package ipana.renders.settings.nbt;

import ipana.utils.font.FontHelper;
import ipana.utils.player.PlayerUtils;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//TODO: better nbt gui
public class NbtGui extends GuiScreen {
    private Minecraft mc = Minecraft.getMinecraft();
    private GuiTextField itemField = new GuiTextField(2,mc.fontRendererObj,25,5,75,15);
    private GuiTextField enchantField = new GuiTextField(3,mc.fontRendererObj,120,5,75,15);
    private Item selectedItem;
    private int amount = 1;
    private int meta = 0;
    private List<NbtButton> buttons = new ArrayList<>();
    private List<EnchantmentData> enchants = new ArrayList<>();


    public NbtGui() {
        itemField.setText("apple:1:0");
        itemField.setCanLoseFocus(true);
        itemField.setFocused(false);
        enchantField.setText("unbreaking:1");
        enchantField.setCanLoseFocus(true);
        enchantField.setFocused(false);
        buttons.add(new NbtButton("AddItem",50,12));
        buttons.add(new NbtButton("Enchant",50,12));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = RenderUtils.SCALED_RES;
        itemField.drawTextBox();
        enchantField.drawTextBox();
        if (selectedItem != null) {
            ItemStack stack = new ItemStack(selectedItem);
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableDepth();
            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 5, 25);
            GlStateManager.disableDepth();
            GlStateManager.popMatrix();
            for (NbtButton nbt : buttons) {
                int x = 0;
                int y = 0;
                switch (nbt.getName()) {
                    case "AddItem": {
                        x = 10;
                        y = sr.getScaledHeight()-20;
                        break;
                    }
                    case "Enchant": {
                        x = 75;
                        y = sr.getScaledHeight()-20;
                        break;
                    }
                }

                nbt.render(x,y,mouseX,mouseY);
            }
            int y = 25;
            for (EnchantmentData enchantmentData : enchants) {
                FontHelper.SIZE_18.drawStringWithShadow(enchantmentData.enchantmentobj.getTranslatedName(enchantmentData.enchantmentLevel),125,y, Color.white.getRGB());
                y+=12;
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (itemField.isFocused()) {
            itemField.textboxKeyTyped(typedChar, keyCode);
            if (keyCode == Keyboard.KEY_RETURN) {
                String[] split = itemField.getText().split(":");
                if (split.length == 3) {
                    selectedItem = Item.getByNameOrId(split[0]);
                    try {
                        amount = Integer.parseInt(split[1]);
                        meta = Integer.parseInt(split[2]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    itemField.setFocused(false);
                }
            }
        }
        if (enchantField.isFocused()) {
            enchantField.textboxKeyTyped(typedChar, keyCode);
            if (keyCode == Keyboard.KEY_RETURN) {
                if (enchantField.getText().equalsIgnoreCase("clear")) {
                    enchants.clear();
                    enchantField.setText("");
                } else {
                    String[] split = enchantField.getText().split(":");
                    if (split.length == 2) {
                        enchants.add(new EnchantmentData(Enchantment.getEnchantmentByLocation(split[0]), Integer.parseInt(split[1])));
                        itemField.setFocused(false);
                    }
                }
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        itemField.mouseClicked(mouseX, mouseY, mouseButton);
        enchantField.mouseClicked(mouseX, mouseY, mouseButton);
        if (selectedItem != null) {
            for (NbtButton nbt : buttons) {
                if (nbt.isHovered(mouseX, mouseY)) {
                    switch (nbt.getName()) {
                        case "AddItem": {
                            if (availableSlot() != -1) {
                                ItemStack stack = new ItemStack(selectedItem,amount,meta);
                                if (stack.getItem() instanceof ItemPotion) {
                                    stack.setItemDamage(16384);
                                    NBTTagList effects = new NBTTagList();
                                    NBTTagCompound effect = new NBTTagCompound();
                                    effect.setInteger("Amplifier", 125);
                                    effect.setInteger("Duration", 2000);
                                    effect.setInteger("Id", 6);
                                    effects.appendTag(effect);
                                    stack.setTagInfo("CustomPotionEffects", effects);
                                    stack.setStackDisplayName("\2474Killer \247cPot.");
                                } else {
                                    NBTTagCompound tags = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
                                    if (!tags.hasKey("ench", 9)) {
                                        tags.setTag("ench", new NBTTagList());
                                    }
                                    NBTTagList nbttaglist = tags.getTagList("ench", 10);
                                    for (EnchantmentData enchantmentData : enchants) {
                                        NBTTagCompound nbttagcompound = new NBTTagCompound();
                                        nbttagcompound.setInteger("id", enchantmentData.enchantmentobj.effectId);
                                        nbttagcompound.setInteger("lvl", enchantmentData.enchantmentLevel);
                                        nbttaglist.appendTag(nbttagcompound);
                                    }
                                    stack.setTagCompound(tags);
                                    stack.setStackDisplayName("§d.hurttime");
                                }
                                PlayerUtils.packet(new C10PacketCreativeInventoryAction(availableSlot(),stack));
                            }
                            break;
                        }
                    }
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private int availableSlot() {
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.thePlayer.inventory.getStackInSlot(i) == null) {
                slot = i;
            }
        }
        return slot;
    }
}
