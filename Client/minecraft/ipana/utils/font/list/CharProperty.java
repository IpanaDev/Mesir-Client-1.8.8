package ipana.utils.font.list;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.awt.*;
import java.lang.reflect.Array;

public class CharProperty {
    private char aChar;
    private Position[] positions;
    private int posIndex;
    private boolean shadow;

    public CharProperty(char aChar, boolean shadow) {
        this.aChar = aChar;
        this.shadow = shadow;
        positions = (Position[]) Array.newInstance(Position.class,1000);
        posIndex = 0;
    }

    public void addPosition(float x, float y, Color color) {
        positions[posIndex] = new Position(x,y, color);
        posIndex++;
    }

    public void drawChar(ListFont.CharacterData[] characterData) {
        final ListFont.CharacterData charData = characterData[aChar];
        charData.bind();
        for (int i = 0; i < posIndex; i++) {
            Position position = positions[i];
            double par1 = position.x + charData.width;
            double par2 = position.y + charData.height;
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            if (shadow) {
                double p1 = position.x+0.5 + charData.width;
                double p2 = position.y+0.5 + charData.height;
                position.x += 0.5;
                position.y += 0.5;
                GlStateManager.color(0, 0, 0, 1);
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
                worldrenderer.pos(position.x, p2, 0.0D).tex(0.0D, 1.0D).endVertex();
                worldrenderer.pos(p1, p2, 0.0D).tex(1.0D, 1.0D).endVertex();
                worldrenderer.pos(p1, position.y, 0.0D).tex(1.0D, 0.0D).endVertex();
                worldrenderer.pos(position.x, position.y, 0.0D).tex(0.0D, 0.0D).endVertex();
                tessellator.draw();
                position.x -= 0.5;
                position.y -= 0.5;
            }
            GlStateManager.color(position.color.getRed()/255f, position.color.getGreen()/255f, position.color.getBlue()/255f, position.color.getAlpha()/255f);
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos(position.x, par2, 0.0D).tex(0.0D, 1.0D).endVertex();
            worldrenderer.pos(par1, par2, 0.0D).tex(1.0D, 1.0D).endVertex();
            worldrenderer.pos(par1, position.y, 0.0D).tex(1.0D, 0.0D).endVertex();
            worldrenderer.pos(position.x, position.y, 0.0D).tex(0.0D, 0.0D).endVertex();
            tessellator.draw();
        }
    }

    class Position {
        float x,y;
        Color color;

        public Position(float x, float y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }
}
