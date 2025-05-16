package ipana.clickgui.autistic.panels;

import ipana.Ipana;
import ipana.clickgui.autistic.NewClickGui;
import ipana.eventapi.EventManager;
import ipana.events.EventTick;
import ipana.managements.module.Modules;
import ipana.managements.value.Type;
import ipana.managements.value.Value;
import ipana.managements.value.values.*;
import ipana.modules.render.CGui;
import ipana.utils.math.MathUtils;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import pisi.unitedmeows.eventapi.event.listener.Listener;

import static ipana.clickgui.autistic.GuiSettings.*;

public class ValuePanel/*L*/ extends Panel {

    public Value<?> value;
    public float x, y, width, height;
    private boolean numberDrag,positionDrag;
    public ModulePanel panel;
    private int boolAlpha, prevBoolAlpha;
    private double numberWidth, prevNumberWidth;
    private float hoverX, prevHoverX;
    private boolean hovered;

    public ValuePanel(Value<?> value, ModulePanel panel) {
        this.value = value;
        height = 14;
        if (value.getType() == Type.NUMBER) {
            height = 24;
        } else if (value.getType() == Type.POSITION) {
            height = 100;
        } else if (value.getType() == Type.COLOR) {
            height = 70;
        }
        x = panel.x;
        y = panel.y + panel.valueHeight+15;
        width = panel.width;
        this.panel = panel;
        EventManager.eventSystem.subscribeAll(this);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!value.isOpen()) {
            return;
        }
        hovered = isHovered(mouseX, mouseY);
        switch (value.getType()) {
            case COLOR -> {
                ColorValue v = (ColorValue) value;
                getNormalFont().drawString(value.getName() + " : ", (int) x + 2, (int) y-2, v.getValue());
                v.picker().draw((int)x+3, (int)y+7, 68,70,mouseX, mouseY, v.getValue());
            }
            case POSITION -> {
                PositionValue v = (PositionValue) value;
                if (positionDrag && hovered) {
                    v.setX(mouseX - x);
                    v.setY(mouseY - y - 12);
                }
                getNormalFont().drawString(v.getName() + " : ", (int) x + 2, (int) y - 1, MODULE_DISABLED);
                RenderUtils.drawFixedRect(x + 2, y + 9, x + width - 2, y + 95, CLIENT_COLOR);
                RenderUtils.drawFixedRect(x + 3, y + 10, x + width - 3, y + 94, COL_VALUE1);
                for (double i = y + 20; i < y + 95; i += 10) {
                    RenderUtils.drawFixedRect(x + 3, i, x + width - 3, i + 1, COL_VALUE2);
                }
                for (double i = x + 10; i < x + width - 3; i += 10.1) {
                    RenderUtils.drawFixedRect(i, y + 10, i + 1, y + 94, COL_VALUE2);
                }
                RenderUtils.drawFixedRect(x + 2 + v.getX() - 1, y + 12 + v.getY(), x + 1 + v.getX() + 3, y + 13 + v.getY(), COL_VALUE3);
                RenderUtils.drawFixedRect(x + 2 + v.getX() - 5, y + 12 + v.getY(), x + 1 + v.getX() - 1, y + 13 + v.getY(), COL_VALUE3);
                RenderUtils.drawFixedRect(x + 2 + v.getX() - 1, y + 12 + v.getY() - 3, x + 1 + v.getX() - 1, y + 12 + v.getY(), COL_VALUE3);
                RenderUtils.drawFixedRect(x + 2 + v.getX() - 1, y + 12 + v.getY() + 1, x + 1 + v.getX() - 1, y + 12 + v.getY() + 4, COL_VALUE3);
            }
            case MODE -> {
                ModeValue<?> modeValue = (ModeValue<?>) value;
                float animX = prevHoverX + (hoverX - prevHoverX)*partialTicks;
                RenderUtils.drawFixedRect(x + 2+width/2-animX, y - 5, x + width - 2 - width/2 + animX, y + height - 5, CATEGORY_COLOR);
                if (hovered) {
                    RenderUtils.drawRoundedRect(x + width - 0.5, y - 5.5, getNormalFont().getWidth(value.getName() + ": " + value.getDescription()) + 1, height + 1, 2, CLIENT_COLOR);
                    RenderUtils.drawRoundedRect(x + width - 1, y - 5, getNormalFont().getWidth(value.getName() + ": " + value.getDescription()) + 1, height, 2, CATEGORY_COLOR);
                    getNormalFont().drawString(value.getName() + ": " + value.getDescription(), (int) (x + width - 1), (int) (y - 1), MODULE_DISABLED);
                }
                getNormalFont().drawString("<" + modeValue.getValue().getName() + ">", (int) (x + (width / 2 - getNormalFont().getWidth("<" + modeValue.getValue().getName() + ">") / 2)), (int) y - 1, CLIENT_COLOR);
                getNormalFont().drawString("|", (int) x + 2, (int) y - 1, CLIENT_COLOR);
                getNormalFont().drawString("|", (int) x + (int) width - 6, (int) y - 1, CLIENT_COLOR);
            }
            case ENUM -> {
                EnumValue<?> modeValue = (EnumValue<?>) value;
                float animX = prevHoverX + (hoverX - prevHoverX)*partialTicks;
                RenderUtils.drawFixedRect(x + 2+width/2-animX, y - 5, x + width - 2 - width/2 + animX, y + height - 5, CATEGORY_COLOR);
                if (hovered) {
                    RenderUtils.drawRoundedRect(x + width - 0.5, y - 5.5, getNormalFont().getWidth(value.getName() + ": " + value.getDescription()) + 1, height + 1, 2, CLIENT_COLOR);
                    RenderUtils.drawRoundedRect(x + width - 1, y - 5, getNormalFont().getWidth(value.getName() + ": " + value.getDescription()) + 1, height, 2, CATEGORY_COLOR);
                    getNormalFont().drawString(value.getName() + ": " + value.getDescription(), (int) (x + width - 1), (int) (y - 1), MODULE_DISABLED);
                }
                getNormalFont().drawString("<" + modeValue.getValue().name() + ">", (int) (x + (width / 2 - getNormalFont().getWidth("<" + modeValue.getValue().name() + ">") / 2)), (int) y - 1, CLIENT_COLOR);
                getNormalFont().drawString("|", (int) x + 2, (int) y - 1, CLIENT_COLOR);
                getNormalFont().drawString("|", (int) x + (int) width - 6, (int) y - 1, CLIENT_COLOR);
            }
            case TEXT -> {
                TextValue textValue = (TextValue) value;
                float animX = prevHoverX + (hoverX - prevHoverX)*partialTicks;
                RenderUtils.drawFixedRect(x + 2+width/2-animX, y - 5, x + width - 2 - width/2 + animX, y + height - 5, CATEGORY_COLOR);
                if (hovered) {
                    RenderUtils.drawRoundedRect(x + width - 0.5, y - 5.5, getNormalFont().getWidth(value.getDescription()) + 1, height + 1, 2, CLIENT_COLOR);
                    RenderUtils.drawRoundedRect(x + width - 1, y - 5, getNormalFont().getWidth(value.getDescription()) + 1, height, 2, CATEGORY_COLOR);
                    getNormalFont().drawString(value.getDescription(), (int) x + (int) width - 1, y - 1, MODULE_DISABLED);
                }
                getNormalFont().drawString(textValue.getName() + ": " + textValue.getValue(), (int) x + 2, (int) y, MODULE_DISABLED);
                if (Ipana.newClickGui.textField != null && Ipana.newClickGui.textField.isFocused()) {
                    textValue.setValue(Ipana.newClickGui.textField.getText());
                }
            }
            case NUMBER -> {
                var numberValue = (NumberValue) value;
                float animX = prevHoverX + (hoverX - prevHoverX)*partialTicks;
                RenderUtils.drawFixedRect(x + 2+width/2-animX, y - 5, x + width - 2 - width/2 + animX, y + height - 5, CATEGORY_COLOR);
                if (hovered) {
                    RenderUtils.drawRoundedRect(x + width - 0.5, y - 1.5, getNormalFont().getWidth(value.getDescription()) + 1, 15, 2, CLIENT_COLOR);
                    RenderUtils.drawRoundedRect(x + width - 1, y - 1, getNormalFont().getWidth(value.getDescription()) + 1, 14, 2, CATEGORY_COLOR);
                    getNormalFont().drawString(value.getDescription(), (int) x + (int) width - 1, (int) y + 3, MODULE_DISABLED);
                }
                if (numberDrag) {
                    float min = numberValue.getMin().floatValue();
                    float max = numberValue.getMax().floatValue();
                    float inc = numberValue.getIncrement().floatValue();
                    float valAbs = mouseX - (x);
                    float perc = valAbs / (width - 8);
                    perc = Math.min(Math.max(0.0f, perc), 1.0f);
                    float valRel = (max - min) * perc;
                    float val1 = min + valRel;
                    val1 = Math.round(val1 * (1.0f / inc)) / (1.0f / inc);
                    if (numberValue.getIncrement() instanceof Float) {
                        numberValue.setValue(val1);
                    } else if (numberValue.getIncrement() instanceof Double) {
                        numberValue.setValue(MathUtils.fixFormat((double) val1, 6));
                    } else if (numberValue.getIncrement() instanceof Integer) {
                        numberValue.setValue((int) val1);
                    }
                }
                getNormalFont().drawString(numberValue.getName(), (int) (x + (width / 2 - getNormalFont().getWidth(numberValue.getName()) / 2)), (int) (y - 2), MODULE_DISABLED);
                RenderUtils.drawRoundedRect(x + 3, y + 6, width - 6, 10, 1, MODULE_DISABLED);
                RenderUtils.drawRoundedRect(x + 3.5, y + 6.5, width - 7, 9, 1, CATEGORY_COLOR);
                RenderUtils.drawRoundedRect(x + 4, y + 7, prevNumberWidth + (numberWidth - prevNumberWidth)*partialTicks, 8, 1.5, CLIENT_COLOR);
                getNormalFont().drawString(String.valueOf(numberValue.getValue()), (int) (x + (width / 2 - getNormalFont().getWidth(numberValue.getValue().toString()) / 2)), (int) (y + 8),MODULE_DISABLED);
            }
            case BOOLEAN -> {
                float animX = prevHoverX + (hoverX - prevHoverX)*partialTicks;
                RenderUtils.drawFixedRect(x + 2+width/2-animX, y - 5, x + width - 2 - width/2 + animX, y + height - 5, CATEGORY_COLOR);
                if (hovered) {
                    RenderUtils.drawRoundedRect(x + width - 0.5, y - 5.5, getNormalFont().getWidth(value.getDescription()) + 1, height + 1, 2, CLIENT_COLOR);
                    RenderUtils.drawRoundedRect(x + width - 1, y - 5, getNormalFont().getWidth(value.getDescription()) + 1, height, 2, CATEGORY_COLOR);
                    getNormalFont().drawString(value.getDescription(), (int) x + (int) width - 1, (int) y - 1, MODULE_DISABLED);
                }
                getNormalFont().drawString(value.getName(), (int) x + 2, (int) y, MODULE_DISABLED);
                RenderUtils.drawRoundedRect(x + width - 15, y - 3, 10, 10, 1, MODULE_DISABLED);
                RenderUtils.drawRoundedRect(x + width - 14.5, y - 2.5, 9, 9, 1, CATEGORY_COLOR);
                drawCheck(partialTicks);
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (value.getType() == Type.NUMBER) {
            numberDrag = true;
        } else if (value.getType() == Type.BOOLEAN) {
            BoolValue boolValue = (BoolValue) value;
            boolValue.setValue(!boolValue.getValue());
        } else if (value.getType() == Type.MODE) {
            ModeValue<?> modeValue = (ModeValue<?>)value;
            if (button == 0) {
                modeValue.setNext();
            } else if (button == 1) {
                modeValue.setPrevious();
            }
        } else if (value.getType() == Type.ENUM) {
            EnumValue<?> modeValue = (EnumValue<?>) value;
            if (button == 0) {
                modeValue.setNext();
            } else if (button == 1) {
                modeValue.setPrevious();
            }
        } else if (value.getType() == Type.POSITION) {
            positionDrag = true;
        } else if (value.getType() == Type.TEXT) {
            TextValue v = (TextValue) value;
            if (Ipana.newClickGui.textField == null) {
                Ipana.newClickGui.textField = new GuiTextField(1, Minecraft.getMinecraft().fontRendererObj, 1, 1, 1, 1);
            }
            Ipana.newClickGui.textField.setFocused(true);
            Ipana.newClickGui.textField.setMaxStringLength(2173);
            Ipana.newClickGui.textField.setText("");
        }
    }

    public void keyTyped(char charCode, int keyCode) {

    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        numberDrag = false;
        positionDrag = false;
    }

    @Override
    public boolean isHovered(int mouseX, int mouseY) {
        if (value.getType() == Type.NUMBER) {
            return mouseX >= x+3 && mouseX <= x + width - 6 && mouseY > y + 6 && mouseY <= y + 16;
        } else if (value.getType() == Type.POSITION) {
            return mouseX >= x+3 && mouseX <= x + width-3 && mouseY > y + 10 && mouseY <= y + 94;
        } else if (value.getType() == Type.COLOR) {
            return mouseX >= x+2 && mouseX <= x + width-2 && mouseY > y + 10 && mouseY <= y + 109;
        } else {
            return mouseX >= x && mouseX <= x + width && mouseY > y - 5 && mouseY <= y + height - 5;
        }
    }
    public boolean isHovered(double x, double y, double width, double height,int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY > y - 5 && mouseY <= y + height - 5;
    }

    private Listener<EventTick> onTick = new Listener<EventTick>(event -> {
        CGui cGui = Modules.CLICK_GUI;
        float m = 10;
        prevHoverX = hoverX;
        if (hovered && hoverX < width/2f)
            hoverX += m;
        else if (!hovered && hoverX > 2)
            hoverX -= m;

        hoverX = MathHelper.clamp_float(hoverX, 2, width/2f);

        if (value instanceof BoolValue boolValue) {
            if (cGui.anim.getValue()) {
                int increase = 20;
                prevBoolAlpha = boolAlpha;
                if (boolAlpha < 255 && boolValue.getValue())
                    boolAlpha+=increase;
                else if (boolAlpha > 0 && !boolValue.getValue())
                    boolAlpha-=increase;
                boolAlpha = MathHelper.clamp_int(boolAlpha, 0, 255);
            } else {
                boolAlpha = boolValue.getValue() ? CLIENT_COLOR.getAlpha() : 0;
                prevBoolAlpha = boolAlpha;
            }
        } else if (value instanceof NumberValue numberValue) {
            double value = (numberValue.getValue().doubleValue() - numberValue.getMin().doubleValue()) / (numberValue.getMax().doubleValue() - numberValue.getMin().doubleValue());
            double valueWidth = value * width - 8;
            if (cGui.anim.getValue()) {
                double increase = 5;
                prevNumberWidth = numberWidth;
                if (numberWidth > valueWidth) {
                    numberWidth -= increase;
                    numberWidth = Math.max(numberWidth, valueWidth);
                } else if (numberWidth < valueWidth) {
                    numberWidth += increase;
                    numberWidth = Math.min(numberWidth, valueWidth);
                }
            } else {
                prevNumberWidth = numberWidth = valueWidth;
            }
        }
    }).filter(f -> Minecraft.getMinecraft().currentScreen instanceof NewClickGui);

    public void drawCheck(float partialTicks) {
        if (prevBoolAlpha > 0 || boolAlpha > 0) {
            GL11.glPushMatrix();
            GlStateManager.enableBlend();
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(3553);
            GL11.glEnable(2848);
            GL11.glBlendFunc(770, 771);
            GL11.glLineWidth(1.5f);
            GL11.glBegin(3);
            float animate = (prevBoolAlpha + (boolAlpha-prevBoolAlpha)*partialTicks) / 255f;
            double m = 4;
            double posAnimate = animate*(m+1);
            GL11.glColor4f(CLIENT_COLOR.getRed() / 255f, CLIENT_COLOR.getGreen() / 255f, CLIENT_COLOR.getBlue() / 255f, animate);

            GL11.glVertex2d(x + width - 6.5 + posAnimate - m - 1, y - posAnimate + m);
            GL11.glVertex2d(x + width - 11.5, y + 6);
            GL11.glVertex2d(x + width - 13.5, y + 4);
            GL11.glEnd();
            GL11.glEnable(3553);
            GlStateManager.disableBlend();
            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

}
