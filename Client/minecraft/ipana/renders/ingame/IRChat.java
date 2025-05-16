package ipana.renders.ingame;

import ipana.irc.IRC;
import ipana.irc.user.User;
import ipana.utils.font.FontHelper;
import ipana.utils.font.FontUtil;
import ipana.utils.render.RenderUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class IRChat {
    private List<Line> lines = new ArrayList<>();
    private FontUtil font = FontHelper.SIZE_18;
    private final Color inGame = new Color(50,50,50,125);
    private IRC irc;
    public int messagesUnseen;

    public IRChat(IRC irc) {
        this.irc = irc;
    }

    public void tick() {
        for (Line line : lines) {
            if ((line.leftTicks <= 0 && line.prevX == RenderUtils.SCALED_RES.getScaledWidth()) || line.sentBySelf) {
                continue;
            }
            int x = line.leftTicks > 0 ? (int) (RenderUtils.SCALED_RES.getScaledWidth()-font.getWidth(line.message)-2) : RenderUtils.SCALED_RES.getScaledWidth();
            if (line.animateX(x, 10)) {
                if (line.leftTicks > 0) {
                    line.leftTicks--;
                }
            }
        }
    }

    public void inGameRender(float partialTicks) {
        float scale = 1.2f;
        int y = RenderUtils.SCALED_RES.getScaledHeight()-70;
        for (int i = lines.size()-1; i >= Math.max(0, lines.size()-6); i--) {
            Line line = lines.get(i);
            if (line.sentBySelf) {
                continue;
            }
            if (line.prevX < RenderUtils.SCALED_RES.getScaledWidth()) {
                float x = line.renderX(partialTicks);
                RenderUtils.drawFixedRect(x, y, RenderUtils.SCALED_RES.getScaledWidth(), y + 10, inGame);
                font.drawStringWithShadow(line.message, x + 1, y + 1, Color.white);
                y -= 11;
            }
        }
    }

    public void onMessage(String sender, String message, User user) {
        lines.add(Line.create(sender, message, user,false));
    }

    public IRC irc() {
        return irc;
    }

    public List<Line> lines() {
        return lines;
    }
}
