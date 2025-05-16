package ipana.renders.settings.anticheat;

import ipana.Ipana;
import ipana.utils.ncp.handler.NCP3_11_1Handler;
import ipana.utils.ncp.handler.VanillaHandler;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

import static ipana.Ipana.*;

@Deprecated
public class AntiCheatGui extends GuiScreen {
    public AntiCheat<NCP3_11_1Handler> ncp3_11_1 = new AntiCheat<>("NCP 3.11.1", Ipana.ncp3_11_1Handler, ncp3_11_1Handler.fightSpeed, ncp3_11_1Handler.combined.improbable());
    public AntiCheat<VanillaHandler> vanilla = new AntiCheat<>("Vanilla", new VanillaHandler());

    private static final Color BACKGROUND = new Color(100,100,100,100);
    private static final Color BACKGROUND2 = new Color(200,200,200,75);
    private ArrayList<ACButton> antiCheats = new ArrayList<>();
    private FontRenderer font = Minecraft.getMinecraft().fontRendererObj;

    public AntiCheatGui() {
        vanilla.setActive(true);
        antiCheats.add(new ACButton(vanilla));
        antiCheats.add(new ACButton(ncp3_11_1));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = RenderUtils.SCALED_RES;
        int w = 200;
        int h = 120;
        int x = sr.getScaledWidth()/2-w/2;
        int y = sr.getScaledHeight()/2-h/2;

        RenderUtils.drawFixedRect(x,y,x+w,y+h,BACKGROUND);
        RenderUtils.drawFixedRect(x-2,y-2,x+w+4,y+h+4,BACKGROUND2);

        antiCheats.forEach(ac -> ac.draw(mouseX,mouseY, x, y+2+(antiCheats.indexOf(ac)*12)));
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        antiCheats.forEach(ac -> ac.mouseClicked(mouseX, mouseY, mouseButton));
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    class ACButton {
        AntiCheat<?> antiCheat;
        int x,y,width,height;

        ACButton(AntiCheat<?> antiCheat) {
            width = 50;
            height = 11;
            this.antiCheat = antiCheat;
        }

        void draw(int mouseX, int mouseY, int drawX, int drawY) {
            x = drawX;
            y = drawY;
            if (hovered(mouseX, mouseY)) {
                RenderUtils.drawFixedRect(x+1,y,x+width+1,y+height,BACKGROUND2);
            }
            font.drawStringWithShadow(antiCheat.name(), x+2, y+2, antiCheat.isActive() ? Color.green.getRGB() : Color.white.getRGB());
            if (antiCheat.isActive()) {
                x += 60;
                ScaledResolution sr = RenderUtils.SCALED_RES;
                y = sr.getScaledHeight()/2-60;
                for (Check check : antiCheat.checks()) {
                    if (mouseX > x && mouseX < x + font.getStringWidth(check.name()) + 1 && mouseY > y && mouseY < y + 12) {
                        RenderUtils.drawFixedRect(x + 1, y, x + font.getStringWidth(check.name()) + 2, y + height, BACKGROUND2);
                    }
                    font.drawStringWithShadow(check.name(), x + 2, y + 2, check.isEnabled() ? Color.green.getRGB() : Color.red.brighter().getRGB());
                    y += 12;
                }
                x = drawX;
                y = drawY;
            }
        }
        void mouseClicked(int mouseX, int mouseY, int button) {
            if (hovered(mouseX, mouseY)) {
                for (ACButton acButton : antiCheats) {
                    acButton.antiCheat.setActive(acButton.antiCheat == antiCheat);
                }
            }
            if (antiCheat.isActive()) {
                int saveY = y;
                ScaledResolution sr = RenderUtils.SCALED_RES;
                y = sr.getScaledHeight()/2-60;
                for (Check check : antiCheat.checks()) {
                    if (mouseX > x + 60 && mouseX < x + 60 + font.getStringWidth(check.name()) + 1 && mouseY > y && mouseY < y + 12) {
                        check.setEnabled(!check.isEnabled());
                    }
                    y += 12;
                }
                y = saveY;
            }
        }
        boolean hovered(int mouseX, int mouseY) {
            return mouseX > x && mouseX < x+width && mouseY > y && mouseY < y+height;
        }
    }
}
