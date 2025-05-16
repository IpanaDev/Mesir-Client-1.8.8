package ipana.clickgui.autistic.panels;

import ipana.clickgui.autistic.GuiSettings;
import ipana.eventapi.EventManager;
import ipana.events.EventTick;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.ModuleManager;
import ipana.utils.font.FontUtil;
import ipana.utils.math.MathUtils;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;

import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static ipana.clickgui.autistic.GuiSettings.*;

public class CategoryPanel extends Panel {

    public Category category;
    public List<ModulePanel> modules = new ArrayList<>();
    public float x, y, height;
    private FontUtil bigFont = GuiSettings.getCategoryFont();
    public float width = 85;
    public float totalHeight;
    private boolean dragging;
    public float transparent, prevTransparent;
    private float prevX, prevY;

    public CategoryPanel(int x, int y, Category category) {
        this.x = x;
        this.y = y;
        this.category = category;
        int index = 0;
        List<Module> DDDDDDD = ModuleManager.getModulesFromCategory(category);
        DDDDDDD.sort(Comparator.comparingDouble(m -> GuiSettings.getNormalFont().getWidth(m.getName())));
        for (Module module : DDDDDDD) {
            modules.add(new ModulePanel(module, index,this,index+1==DDDDDDD.size()));
            index++;
        }
        height = modules.size()*15+17;
        EventManager.eventSystem.subscribeAll(this);
        transparent = prevTransparent = 1;
    }

    private Listener<EventTick> onTick = new Listener<>(event -> {
        prevTransparent = transparent;
        float increase = 0.1f;
        if (dragging) {
            if (transparent > 0.5) {
                transparent -= increase;
            }
        } else {
            if (transparent < 1) {
                transparent += increase;
            }
        }
        transparent = MathHelper.clamp_float(transparent, 0.0f, 1);
    });


    public void render(int mouseX, int mouseY, float partialTicks) {
        resetModulePosition();
        if (dragging) {
            x = mouseX + prevX;
            y = mouseY + prevY;
        }
        height = modules.size()*15+17 + totalHeight;
        totalHeight = 0;
        //RenderUtils.shadowAround(x+2, y+2, width-4, height-4, GRADIENT_START, GRADIENT_END);
        RenderUtils.drawRoundedGradientRect(x-8, y-8, width+16, height+16, 11, 15, GRADIENT_START, GRADIENT_END);
        RenderUtils.drawRoundedRect(x-0.5,y-0.5,width+1,height+1,5, CLIENT_COLOR);
        RenderUtils.drawRoundedRect(x,y,width,height,5, CATEGORY_COLOR);
        bigFont.drawString(category.name(),x+(width/2-bigFont.getWidth(category.name())/2),y+3, MODULE_DISABLED);
        RenderUtils.drawRoundedRect(x+2,y+15,width-4,height-17,5, CATEGORY_COLOR2);
        for (ModulePanel modulePanel : modules) {
            modulePanel.preRender(mouseX,mouseY, partialTicks);
        }
        boolean positionReset = true;
        Gui.drawRect(1,1,1,1,1);
        for (ModulePanel modulePanel : modules) {
            modulePanel.render(mouseX,mouseY, partialTicks);
            if (modulePanel.openedValues) {
                positionReset = false;
            }
        }
        if (positionReset) {
            resetCategoryPosition();
        }
        prevTransparent = transparent;
    }
    private float getRotationFromPosition(double x, double y) {
        double xDiff = x - this.x;
        double zDiff = y - this.y;
        return (float) ((Math.atan2(zDiff, xDiff) * 180.0D / (Math.PI)) - 90.0F);
    }
    private double[] calculate(float yaw, double speed) {
        double forward = 1;
        double strafe = 0;
        double xSpeed = forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * MathHelper.sin(Math.toRadians(yaw + 90.0F));
        double zSpeed = forward * speed * MathHelper.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F));
        return new double[]{xSpeed,zSpeed};
    }
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            dragging = true;
            prevX = x - mouseX;
            prevY = y - mouseY;
        }
        for (ModulePanel modulePanel : modules) {
            if (modulePanel.isHovered(mouseX, mouseY)) {
                modulePanel.mouseClicked(mouseX, mouseY, button);
            }
            if (modulePanel.openedValues) {
                for (ValuePanel valuePanel : modulePanel.values) {
                    if (valuePanel.isHovered(mouseX, mouseY)) {
                        valuePanel.mouseClicked(mouseX, mouseY, button);
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (dragging) {
            x = mouseX - 50;
            y = mouseY;
            dragging = false;
        }
        for (ModulePanel modulePanel : modules) {
            if (modulePanel.openedValues) {
                for (ValuePanel valuePanel : modulePanel.values) {
                    valuePanel.mouseReleased(mouseX, mouseY, button);
                }
            }
        }
    }

    @Override
    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x+width && mouseY>=y && mouseY <= y+15;
    }

    public void keyTyped(char charCode, int keyCode) {
        for (ModulePanel modulePanel : modules) {
            if (modulePanel.openedValues) {
                for (ValuePanel valuePanel : modulePanel.values) {
                    valuePanel.keyTyped(charCode, keyCode);
                }
            }
        }
    }

    public void resetModulePosition() {
        int index = 0;
        for (ModulePanel module : modules) {
            module.y = y + 15 * index + 20;
            index++;
        }
    }
    public void resetCategoryPosition() {
        height=modules.size()*15+17;
    }
}
