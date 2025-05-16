package ipana.renders.settings;

import ipana.utils.render.RenderUtils;
import ipana.utils.vbo.DynamicVBO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import optifine.MathUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DrawGui extends GuiScreen {
    private List<Position> positionList = new ArrayList<>();
    private FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
    private GuiTextField red;
    private GuiTextField green;
    private GuiTextField blue;
    private GuiTextField opacity;
    private Color color;
    private List<Button> buttons = new ArrayList<>();
    private List<Config> configs = new ArrayList<>();
    private List<Position> backedUpPositions = new ArrayList<>();
    private boolean setupListen;
    private Config selectedConfig;
    private boolean clicked;
    private boolean lineMode, lineListen;
    private int lineX, lineY;
    private DynamicVBO rectVbos = new DynamicVBO(false,true,false);
    private DynamicVBO lineVbos = new DynamicVBO(false,true,false);
    private ResourceLocation pixel = new ResourceLocation("mesir/1x1.png");

    public DrawGui() {
        ScaledResolution sr = RenderUtils.SCALED_RES;
        red = new GuiTextField(1,font,2,12,30,12);
        green = new GuiTextField(2,font,2,36,30,12);
        blue = new GuiTextField(3,font,2,60,30,12);
        opacity = new GuiTextField(4,font,2,84,30,12);
        red.setMaxStringLength(3);
        green.setMaxStringLength(3);
        blue.setMaxStringLength(3);
        opacity.setMaxStringLength(3);
        red.setText(""+0);
        green.setText(""+0);
        blue.setText(""+0);
        opacity.setText(""+255);
        buttons.add(new Button("Delete All",120,2,50,12));
        buttons.add(new Button("X Width",120,18,70,12,1,1,5,1));
        buttons.add(new Button("Y Height",120,34,70,12,1,1,5,1));
        buttons.add(new Button("Pixels",120,50,50,12,true));
        buttons.add(new Button("Undo",200,2,50,12));
        buttons.add(new Button("Redo",200,18,50,12));
        buttons.add(new Button("Line",200,34,50,12, false));
        buttons.add(new Button("Setups",200,50,50,12));
        buttons.add(new Button("Reload",120,66,50,12));

        buttons.add(new Button("Save",5,sr.getScaledHeight()-15,50,12).changeType(Type.SetupButton));
        buttons.add(new Button("Load",65,sr.getScaledHeight()-15,50,12).changeType(Type.SetupButton));
        buttons.add(new Button("Back",125,sr.getScaledHeight()-15,50,12).changeType(Type.SetupButton));
        color = Color.black;
    }
    public void renderDrawGui() {
        if (positionList.size() > 0) {
            mc.fontRendererObj.drawString(String.valueOf(positionList.size()), RenderUtils.SCALED_RES.getScaledWidth()/2f, 2, Color.red.getRGB());
            mc.getTextureManager().bindTexture(pixel);
            rectVbos.draw();
            lineVbos.draw();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = RenderUtils.SCALED_RES;

        Gui.drawRect(-1,-1,sr.getScaledWidth()+1,99,new Color(200,200,200).getRGB());

        font.drawStringWithShadow("Red : ",2,2,new Color(color.getRed(),0,0).getRGB());
        font.drawStringWithShadow("Green : ",2,26,new Color(0,color.getGreen(),0).getRGB());
        font.drawStringWithShadow("Blue : ",2,50,new Color(0,0,color.getBlue()).getRGB());
        font.drawStringWithShadow("Opacity : ",2,74,new Color(0,0,0,color.getAlpha()).getRGB());

        Gui.drawRect(0,97,sr.getScaledWidth(),99,Color.black.getRGB());

        red.drawTextBox();
        green.drawTextBox();
        blue.drawTextBox();
        opacity.drawTextBox();

        Gui.drawRect(40,5,100,65,Color.black.getRGB());
        Gui.drawRect(41,6,99,64,new Color(color.getRed(),color.getGreen(),color.getBlue()).getRGB());

        //Gui.drawRect(sr.getScaledWidth_double()/2-12,sr.getScaledHeight_double()/2-12,sr.getScaledWidth_double()/2+11,sr.getScaledHeight_double()/2+11,Color.black.getRGB());
        //Gui.drawRect(sr.getScaledWidth_double()/2-11,sr.getScaledHeight_double()/2-11,sr.getScaledWidth_double()/2+10,sr.getScaledHeight_double()/2+10,new Color(200,200,200).getRGB());
        Gui.drawRect(sr.getScaledWidth_double()/2-1,sr.getScaledHeight_double()/2-1,sr.getScaledWidth_double()/2+1,sr.getScaledHeight_double()/2+1,new Color(200,50,50,100).getRGB());
        int w = font.getStringWidth("Middle of screen")/2;
        int x = sr.getScaledWidth()/2-w;
        fontRendererObj.drawStringWithShadow("Middle of screen",x,(float)sr.getScaledHeight_double()/2-23,new Color(color.getRed(),color.getGreen(),color.getBlue()).getRGB());

        for (Button button : buttons) {
            switch (button.getType()) {
                case Button: {
                    button.drawButton(mouseX, mouseY);
                    break;
                }
                case Value: {
                    button.drawValue(mouseX, mouseY);
                    break;
                }
                case Boolean: {
                    button.drawBoolean(mouseX, mouseY);
                    break;
                }
            }
        }
        if (setupListen) {
            Gui.drawRect(-1,-1,sr.getScaledWidth_double(),sr.getScaledHeight_double(),new Color(1,1,1,200).getRGB());
            for (Button button : buttons) {
                if (button.getType() == Type.SetupButton) {
                    button.drawButton(mouseX, mouseY);
                }
            }
            for (Config config : configs) {
                config.drawButton(mouseX,mouseY);
            }
        }
        if (clicked) {
            if (!setupListen) {
                if (!lineMode) {
                    int width = (int) getButton("X Width").getValue();
                    int height = (int) getButton("Y Height").getValue();

                    Position pos = new Position(mouseX - 1, mouseY - 1, mouseX + width - 1, mouseY + height - 1, color);
                    boolean ez = false;
                    boolean mal = getButton("Pixels").isEnabled();
                    if (mal) {
                        for (Position position : positionList) {
                            Rectangle who = new Rectangle(
                                    pos.getX(),
                                    pos.getY(),
                                    pos.getWidth() - pos.getX(),
                                    pos.getHeight() - pos.getY());

                            Rectangle toWhat = new Rectangle(
                                    position.getX(),
                                    position.getY(),
                                    position.getWidth() - position.getX(),
                                    position.getHeight() - position.getY());

                            if (isColliding(who, toWhat)) {
                                ez = true;
                            }
                        }
                    }
                    boolean malv2 = mal ? !ez : !positionList.contains(pos);
                    if (malv2 && mouseY > 98) {
                        positionList.add(pos);
                    }
                } else {
                    if (lineListen) {
                        Tessellator tessellator = Tessellator.getInstance();
                        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                        GlStateManager.enableBlend();
                        GlStateManager.disableTexture2D();
                        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                        double w1 = getButton("X Width").getValue();
                        double h1 = getButton("Y Height").getValue();
                        double width = (w1 + h1) / 2;
                        GL11.glLineWidth((float) width);
                        worldrenderer.begin(1, DefaultVertexFormats.POSITION_COLOR);
                        float red = color.getRed() / 255f;
                        float green = color.getGreen() / 255f;
                        float blue = color.getBlue() / 255f;
                        float alpha = color.getAlpha() / 255f;
                        int mY = Math.max(98, mouseY);
                        worldrenderer.pos(lineX, lineY, 0).color(red, green, blue, alpha).endVertex();
                        worldrenderer.pos(mouseX, mY, 0).color(red, green, blue, alpha).endVertex();
                        tessellator.draw();
                        GlStateManager.enableTexture2D();
                        GlStateManager.disableBlend();
                        GL11.glLineWidth(1);
                    }
                }

                for (Button button : buttons) {
                    if (button.getType() == Type.Value) {
                        if (button.hovered(mouseX, mouseY)) {
                            double min = button.getMin();
                            double max = button.getMax();
                            double inc = button.getInc();
                            double valAbs = mouseX - (button.getX());
                            double perc = valAbs / ((button.getX() + button.getWidth()) - (button.getX()));
                            perc = Math.min(Math.max(0.0D, perc), 1.0D);
                            double valRel = (max - min) * perc;
                            double val1 = min + valRel;
                            val1 = Math.round(val1 * (1.0D / inc)) / (1.0D / inc);
                            button.setValue(val1);
                        }
                    }
                }
            }
        }
        if (positionList.size() > 0) {
            GlStateManager.pushMatrix();
            draw(1);
            renderDrawGui();
            GlStateManager.popMatrix();
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!setupListen) {
            if (keyCode == Keyboard.KEY_0 ||
                    keyCode == Keyboard.KEY_1 ||
                    keyCode == Keyboard.KEY_2 ||
                    keyCode == Keyboard.KEY_3 ||
                    keyCode == Keyboard.KEY_4 ||
                    keyCode == Keyboard.KEY_5 ||
                    keyCode == Keyboard.KEY_6 ||
                    keyCode == Keyboard.KEY_7 ||
                    keyCode == Keyboard.KEY_8 ||
                    keyCode == Keyboard.KEY_9 ||
                    keyCode == Keyboard.KEY_RETURN ||
                    keyCode == Keyboard.KEY_BACK) {
                if (red.isFocused()) {
                    red.textboxKeyTyped(typedChar, keyCode);
                    if (keyCode == Keyboard.KEY_RETURN) {
                        if (red.getText().equals("") || red.getText() == null) {
                            red.setText("" + 0);
                        } else if (Integer.parseInt(red.getText()) > 255) {
                            red.setText("" + 255);
                        }
                        red.setFocused(false);
                    }
                }
                if (green.isFocused()) {
                    green.textboxKeyTyped(typedChar, keyCode);
                    if (keyCode == Keyboard.KEY_RETURN) {
                        if (green.getText().equals("") || green.getText() == null) {
                            green.setText("" + 0);
                        } else if (Integer.parseInt(green.getText()) > 255) {
                            green.setText("" + 255);
                        }
                        green.setFocused(false);
                    }
                }
                if (blue.isFocused()) {
                    blue.textboxKeyTyped(typedChar, keyCode);
                    if (keyCode == Keyboard.KEY_RETURN) {
                        if (blue.getText().equals("") || blue.getText() == null) {
                            blue.setText("" + 0);
                        } else if (Integer.parseInt(blue.getText()) > 255) {
                            blue.setText("" + 255);
                        }
                        blue.setFocused(false);
                    }
                }
                if (opacity.isFocused()) {
                    opacity.textboxKeyTyped(typedChar, keyCode);
                    if (keyCode == Keyboard.KEY_RETURN) {
                        if (opacity.getText().equals("") || opacity.getText() == null) {
                            opacity.setText("" + 0);
                        } else if (Integer.parseInt(opacity.getText()) > 255) {
                            opacity.setText("" + 255);
                        }
                        opacity.setFocused(false);
                    }
                }
            }
            if (keyCode == Keyboard.KEY_ESCAPE) {

            }
            if (!opacity.isFocused() && !red.isFocused() && !green.isFocused() && !blue.isFocused()) {
                color = new Color(Integer.parseInt(red.getText()), Integer.parseInt(green.getText()), Integer.parseInt(blue.getText()), Integer.parseInt(opacity.getText()));
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    public boolean isColliding(Rectangle who,Rectangle toWhat) {
        return who.intersects(toWhat);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {

        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        clicked = true;
        if (!setupListen) {
            if (mouseY <= 98) {
                for (Button button : buttons) {
                    switch (button.getType()) {
                        case Value: {

                            break;
                        }
                        case Boolean: {
                            if (button.hovered(mouseX, mouseY)) {
                                button.setEnabled(!button.isEnabled());
                                if (button.getName().equals("Line")) {
                                    lineMode = button.isEnabled();
                                }
                            }
                            break;
                        }
                        case Button: {
                            if (button.hovered(mouseX, mouseY)) {
                                switch (button.getName()) {
                                    case "Delete All": {
                                        backedUpPositions.clear();
                                        backedUpPositions.addAll(positionList);
                                        positionList.clear();
                                        break;
                                    }
                                    case "Undo": {
                                        if (positionList.size() > 0) {
                                            for (int i = 0; i < (lineMode ? 1 : 100); i++) {
                                                if (positionList.isEmpty()) {
                                                    break;
                                                }
                                                Position pos = positionList.get(positionList.size()-1);
                                                positionList.remove(pos);
                                                backedUpPositions.add(pos);
                                            }
                                        }
                                        break;
                                    }
                                    case "Reload": {
                                        new DrawGui();
                                        break;
                                    }
                                    case "Redo": {
                                        if (positionList.size() > 0) {
                                            if (backedUpPositions.size() > 0) {
                                                Position newest = backedUpPositions.get(backedUpPositions.size() - 1);
                                                positionList.add(newest);
                                                backedUpPositions.remove(newest);
                                            }
                                        } else {
                                            positionList.addAll(backedUpPositions);
                                        }
                                        break;
                                    }
                                    case "Setups": {
                                        setupListen = true;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            } else {
                if (lineMode) {
                    lineListen = true;
                    lineX = mouseX;
                    lineY = mouseY;
                }
            }
            if (isHovered(red.xPosition, red.yPosition, red.getWidth(), 12, mouseX, mouseY)) {
                green.setFocused(false);
                blue.setFocused(false);
                opacity.setFocused(false);
                red.setFocused(true);
                red.setText(String.valueOf(color.getRed()));
            }
            if (isHovered(green.xPosition, green.yPosition, green.getWidth(), 12, mouseX, mouseY)) {
                red.setFocused(false);
                blue.setFocused(false);
                opacity.setFocused(false);
                green.setFocused(true);
                green.setText(String.valueOf(color.getGreen()));
            }
            if (isHovered(blue.xPosition, blue.yPosition, blue.getWidth(), 12, mouseX, mouseY)) {
                green.setFocused(false);
                red.setFocused(false);
                opacity.setFocused(false);
                blue.setFocused(true);
                blue.setText(String.valueOf(color.getBlue()));
            }
            if (isHovered(opacity.xPosition, opacity.yPosition, opacity.getWidth(), 12, mouseX, mouseY)) {
                green.setFocused(false);
                red.setFocused(false);
                blue.setFocused(false);
                opacity.setFocused(true);
                opacity.setText(String.valueOf(color.getAlpha()));
            }
        } else {
            for (Config button : configs) {
                if (button.hovered(mouseX, mouseY)) {
                    selectedConfig = button;
                }
            }
            for (Button button : buttons) {
                if (button.hovered(mouseX, mouseY)) {
                    ScaledResolution sr = RenderUtils.SCALED_RES;
                    if (button.getType() == Type.SetupButton) {
                        switch (button.getName()) {
                            case "Back": {
                                setupListen = false;
                                break;
                            }
                            case "Save": {
                                int number = configs.size()+1;
                                int y = (configs.size()+1)*25;
                                int w = font.getStringWidth("Config"+number);
                                int x = sr.getScaledWidth()/2-w/2;
                                Config config = new Config("Config"+number,x,y,75,15);
                                configs.add(config);
                                break;
                            }
                            case "Load": {
                                positionList.clear();
                                break;
                            }
                        }
                    }
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void draw(int method) {
        if (method == 0) {
            for (Position pos : positionList) {
                Gui.drawRect(pos.getX(), pos.getY(), pos.getWidth(), pos.getHeight(), pos.getColor().getRGB());
            }
        } else {
            double w1 = getButton("X Width").getValue();
            double h1 = getButton("Y Height").getValue();
            double width = (w1+h1)/2;
            GL11.glLineWidth((float) width);
            rectVbos.preCompile(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            positionList.stream().filter(p -> p.type == Position.Type.Rect).forEach(pos -> {
                rectVbos.pos(pos.x, pos.height, 0.0D).tex(0,1).color(pos.getColor()).end();
                rectVbos.pos(pos.width, pos.height, 0.0D).tex(1,1).color(pos.getColor()).end();
                rectVbos.pos(pos.width, pos.y, 0.0D).tex(1,0).color(pos.getColor()).end();
                rectVbos.pos(pos.x, pos.y, 0.0D).tex(0,0).color(pos.getColor()).end();
            });
            rectVbos.postCompile();

            lineVbos.preCompile(GL11.GL_LINES, DefaultVertexFormats.POSITION_TEX_COLOR);
            positionList.stream().filter(p -> p.type == Position.Type.Line).forEach(pos -> {
                lineVbos.pos(pos.x, pos.y, 0).tex(0,0).color(pos.getColor()).end();
                lineVbos.pos(pos.x2, pos.y2, 0).tex(1,1).color(pos.getColor()).end();
            });
            lineVbos.postCompile();
            GL11.glLineWidth(1);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        clicked = false;
        if (lineMode && lineListen) {
            double w = getButton("X Width").getValue();
            double h = getButton("Y Height").getValue();
            double width = (w+h)/2;
            int mY = Math.max(98, mouseY);
            positionList.add(new Position(lineX, lineY, mouseX, mY, (int) width, color));
            lineListen = false;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    private boolean isHovered(int x,int y,int width,int height,int mouseX,int mouseY) {
        return mouseX >= x && mouseX <= x+width && mouseY >= y && mouseY <= y+height;
    }

    private Button getButton(String name) {
        Button b = null;
        for (Button button : buttons) {
            if (button.getName().equals(name)) {
                b = button;
            }
        }
        return b;
    }

    public class Button {
        private String name;
        private int x,y,width,height;
        private Type type;
        private boolean enabled;

        private double value,min,max,inc;

        public Button changeType(Type type) {
            this.type = type;
            return this;
        }

        public void drawButton(int mouseX,int mouseY) {
            Gui.drawRect(x-1,y-1,x+width+1,y+height+1,new Color(255,255,255).getRGB());
            Gui.drawRect(x,y,x+width,y+height,new Color(175,175,175).getRGB());
            if (hovered(mouseX,mouseY) && (!setupListen || type == Type.SetupButton)) {
                Gui.drawRect(x,y,x+width,y+height,new Color(175,100,100,100).getRGB());
            }
            int w = font.getStringWidth(name)/2;
            int x1 = x+width/2-w;
            font.drawStringWithShadow(name,x1,y+2,new Color(200,2,2).getRGB());
        }
        public void drawBoolean(int mouseX,int mouseY) {
            Gui.drawRect(x-1,y-1,x+width+1,y+height+1,new Color(255,255,255).getRGB());
            Gui.drawRect(x,y,x+width,y+height,new Color(175,175,175).getRGB());
            if (enabled) {
                Gui.drawRect(x,y,x+width,y+height,new Color(175,100,100,100).getRGB());
            }
            int w = font.getStringWidth(name)/2;
            int x1 = x+width/2-w;
            font.drawStringWithShadow(name,x1,y+2,new Color(200,2,2).getRGB());
        }
        public void drawValue(int mouseX,int mouseY) {
            Gui.drawRect(x-1,y-1,x+width+1,y+height+1,new Color(255,255,255).getRGB());
            double value = (getValue() - getMin()) / (getMax() - getMin());
            Gui.drawRect(x,y,x+value*width,y+height,new Color(255,175,175).getRGB());
            if (hovered(mouseX,mouseY) && !setupListen) {
                Gui.drawRect(x,y,x+value*width,y+height,new Color(175,100,100,100).getRGB());
            }

            font.drawStringWithShadow(name+" : "+getValue(),x+1,y+2,new Color(200,2,2).getRGB());
        }
        private boolean hovered(int mouseX,int mouseY) {
            return mouseX >= x && mouseX <= x+width && mouseY >= y && mouseY <= y+height;
        }
        public Button(String name, int x, int y, int width, int height) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            type = Type.Button;
        }
        public Button(String name, int x, int y, int width, int height,boolean enabled) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.enabled = enabled;
            type = Type.Boolean;
        }
        public Button(String name, int x, int y, int width, int height, double value, double min, double max, double inc) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.value = value;
            this.min = min;
            this.max = max;
            this.inc = inc;
            type = Type.Value;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getInc() {
            return inc;
        }

        public void setInc(double inc) {
            this.inc = inc;
        }

        public double getMax() {
            return max;
        }

        public void setMax(double max) {
            this.max = max;
        }

        public double getMin() {
            return min;
        }

        public void setMin(double min) {
            this.min = min;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public class Config {
        private String name;
        private int x,y,width,height;

        public void drawButton(int mouseX,int mouseY) {
            Gui.drawRect(x-1,y-1,x+width+1,y+height+1,new Color(255,255,255).getRGB());
            Gui.drawRect(x,y,x+width,y+height,new Color(175,175,175).getRGB());
            if (selectedConfig == this) {
                Gui.drawRect(x,y,x+width,y+height,new Color(100, 100, 255,100).getRGB());
            }
            if (hovered(mouseX,mouseY)) {
                Gui.drawRect(x,y,x+width,y+height,new Color(175,100,100,100).getRGB());
            }
            int w = font.getStringWidth(name)/2;
            int x1 = x+width/2-w;
            font.drawStringWithShadow(name,x1,y+3,new Color(200,2,2).getRGB());
        }

        public Config(String name, int x, int y, int width, int height) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        private boolean hovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x+width && mouseY >= y && mouseY <= y+height;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public enum Type {
        Value,Button,Boolean,SetupButton
    }
    public static class Position {
        private int x,y,width,height,x2,y2;
        private Color color;
        private Type type;

        public Position(int x, int y, int width, int height, Color color) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.type = Type.Rect;
        }
        public Position(int x, int y, int x2, int y2, int width, Color color) {
            this.x = x;
            this.y = y;
            this.x2 = x2;
            this.y2 = y2;
            this.width = width;
            this.color = color;
            this.type = Type.Line;
        }
        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        enum Type{
            Rect, Line
        }
    }
}
