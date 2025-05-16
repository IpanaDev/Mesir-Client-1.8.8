package ipana.clickgui.autistic.panels;

import ipana.clickgui.autistic.GuiSettings;
import ipana.clickgui.autistic.NewClickGui;
import ipana.eventapi.EventManager;
import ipana.events.EventTick;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.managements.value.Type;
import ipana.managements.value.Value;
import ipana.managements.value.ValueManager;
import ipana.modules.render.CGui;
import ipana.utils.font.FontUtil;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.MathHelper;
import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static ipana.clickgui.autistic.GuiSettings.*;

public class ModulePanel extends Panel {

    private Module module;
    private int count;
    public float x, y, width, height, valueHeight;
    private FontUtil font = GuiSettings.getNormalFont();
    private boolean last;
    public CategoryPanel panel;
    public List<ValuePanel> values = new ArrayList<>();
    public boolean openedValues;
    private long lastCheck;
    private int hoverY, prevHoverY;
    private float infoAlpha, prevInfoAlpha;
    private boolean infoListen;
    private boolean hovered;

    public ModulePanel(Module module, int count, CategoryPanel panel, boolean last) {
        this.panel = panel;
        this.module = module;
        this.count = count;
        x = panel.x;
        y = panel.y + 15 * count + 20;
        width = panel.width;
        height = 15;
        this.last = last;
        for (Value<?> value : ValueManager.getValuesFromModule(module)) {
            ValuePanel panel1 = new ValuePanel(value,this);
            values.add(panel1);
            if (value.isOpen()) {
                valueHeight += panel1.height;
            }
        }
        EventManager.eventSystem.subscribeAll(this);
    }

    private Listener<EventTick> onTick = new Listener<EventTick>(event -> {
        CGui cGui = Modules.CLICK_GUI;
        if (cGui.anim.getValue()) {
            prevInfoAlpha = infoAlpha;
            if (infoListen) {
                if (infoAlpha < 1) {
                    infoAlpha += 0.05f;
                }
            } else {
                if (infoAlpha > 0) {
                    infoAlpha -= 0.05f;
                }
            }
            infoAlpha = MathHelper.clamp_float(infoAlpha, 0, 1);
            int increase = 2;
            int height = 6;
            prevHoverY = hoverY;

            if (hovered && hoverY <= height)
                hoverY+=increase;
            else if (!hovered && hoverY >= 0)
                hoverY-=increase;

            hoverY = MathHelper.clamp_int(hoverY, 0, height);
        } else {
            hoverY = prevHoverY = 6;
            prevInfoAlpha = infoAlpha = infoListen ? 1 : 0;
        }
    }).filter(f -> Minecraft.getMinecraft().currentScreen instanceof NewClickGui);

    public void preRender(int mouseX, int mouseY, float partialTicks) {
        List<ModulePanel> modules = panel.modules;
        rePositionValues();
        if (openedValues) {
            panel.totalHeight+=valueHeight;
            for (int i = count+1; i < panel.modules.size(); i++) {
                panel.modules.get(i).y+=valueHeight;
            }
        }
        if (infoAlpha > 0) {
            float alpha = prevInfoAlpha + (infoAlpha - prevInfoAlpha) * partialTicks;
            double infoX = x + width;
            double infoWidth = font.getWidth(module.getDescription())+2;
            RenderUtils.drawRoundedRect(infoX+3, y - 5, infoWidth+2, height,5, CLIENT_COLOR,alpha);
            RenderUtils.drawRoundedRect(infoX+4, y - 4, infoWidth, height-2,5, CATEGORY_COLOR,alpha);
            font.drawString(module.getDescription(), (int)infoX+6, (int)y-1, new Color(CLIENT_COLOR.getRed(), CLIENT_COLOR.getGreen(), CLIENT_COLOR.getBlue(), MathHelper.clamp_int((int) (alpha*255), 0 , 255)));
        }
        if (isHovered(mouseX, mouseY)) {
            startListen();
            float animatedHoverY = prevHoverY + (hoverY - prevHoverY)*partialTicks;
            int maxHeight = 6;
            if (count == 0) {
                if (!openedValues) {
                    RenderUtils.drawFixedRect(x + 2, y - 8, x + width - 2, y + height - 2, CATEGORY_COLOR);
                    RenderUtils.drawRoundedRect(x + 2, y+10-(maxHeight-animatedHoverY), width - 4, 10+(maxHeight-animatedHoverY), 5, modules.get(count + 1).getModule().isEnabled() ? CLIENT_COLOR : CATEGORY_COLOR2);
                } else {
                    RenderUtils.drawFixedRect(x + 2, y - 8, x + width - 2, y + height - 6, CATEGORY_COLOR);
                }
            } else if (last) {
                RenderUtils.drawFixedRect(x+2, y-8, x+width-2, y+height-4,  CATEGORY_COLOR);
                RenderUtils.drawRoundedRect(x+2,y-14,width-4,10+(maxHeight-animatedHoverY),5,(modules.get(count-1).getModule().isEnabled() && !modules.get(count-1).openedValues)? CLIENT_COLOR : CATEGORY_COLOR2);
            } else {
                RenderUtils.drawFixedRect(x+2, y-8, x+width-2, y+height-2,  CATEGORY_COLOR);
                RenderUtils.drawRoundedRect(x+2,y-14,width-4,10+(maxHeight-animatedHoverY),5,modules.get(count-1).getModule().isEnabled() && !modules.get(count-1).openedValues ? CLIENT_COLOR : CATEGORY_COLOR2);
                RenderUtils.drawRoundedRect(x + 2, y + 10-(maxHeight-animatedHoverY), width - 4, 10+(maxHeight-animatedHoverY), 5, modules.get(count + 1).getModule().isEnabled() && !openedValues ? CLIENT_COLOR : CATEGORY_COLOR2);
            }
            hovered = true;
        } else {
            stopListen();
            hovered = false;
        }
        if (module.isEnabled() && !isHovered(mouseX, mouseY)) {
            if (count==0) {
                RenderUtils.drawRoundedRect(x + 2, y - 5, width - 4, height,5, CLIENT_COLOR);
                RenderUtils.drawFixedRect(x + 2, y + height-10, x + width - 2, y + height - 5, CLIENT_COLOR);
            } else if (last) {
                RenderUtils.drawRoundedRect(x + 2, y - 5, width - 4, height,5, CLIENT_COLOR);
                if (!modules.get(count - 1).isHovered(mouseX, mouseY)) {
                    RenderUtils.drawFixedRect(x + 2, y - 5, x + width - 2, y, CLIENT_COLOR);
                }
            } else {
                boolean gaming = count > 0 && modules.get(count - 1).isHovered(mouseX, mouseY) && !modules.get(count-1).openedValues;
                RenderUtils.drawFixedRect(x + 2, gaming ? y : y - 5, x + width - 2, y + height - 5, CLIENT_COLOR);
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (isHovered(mouseX,mouseY) && module.isEnabled()) {
            if (module.visible) {
                font.drawString(module.getName(), (int)(x + (width / 2 - font.getWidth(module.getName()) / 2)), y, CLIENT_COLOR);
            } else {
                font.drawStringWithShadow(module.getName(), (int)(x + (width / 2 - font.getWidth(module.getName()) / 2)), y, CLIENT_COLOR);
            }
        } else {
            if (module.visible) {
                font.drawString(module.getName(), (int) (x + (width / 2 - font.getWidth(module.getName()) / 2)), y, module.isEnabled() ? MODULE_ENABLED : MODULE_DISABLED);
            } else {
                font.drawStringWithShadow(module.getName(), (int) (x + (width / 2 - font.getWidth(module.getName()) / 2)), y, module.isEnabled() ? MODULE_ENABLED : MODULE_DISABLED);
            }
        }
        if (openedValues) {
            for (ValuePanel valuePanel : values) {
                valuePanel.render(mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0) {
            module.toggle();
        } else if (button == 1) {
            if (ValueManager.getValuesFromModule(module).size() > 0) {
                openedValues ^= true;
            }
        } else if (button == 2) {
            module.visible ^= true;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {

    }

    @Override
    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY > y-5 && mouseY <= y + height - 5;
    }

    public Module getModule() {
        return module;
    }

    public void rePositionValues() {
        valueHeight = 0;
        x = panel.x;
        for (ValuePanel valuePanel : values) {
            if (valuePanel.value.isOpen()) {
                double insane = 14;
                if (valuePanel.value.getType() == Type.NUMBER) {
                    insane = 24;
                } else if (valuePanel.value.getType() == Type.POSITION) {
                    insane = 100;
                } else if (valuePanel.value.getType() == Type.COLOR) {
                    insane = 86;
                }
                valuePanel.x = x;
                valuePanel.y = y+valueHeight+15;
                valueHeight += insane;
            }
        }
    }

    private void startListen() {
        infoListen = System.currentTimeMillis()-lastCheck >= 750;
    }
    private void stopListen() {
        infoListen = false;
        lastCheck = System.currentTimeMillis();
    }
}
