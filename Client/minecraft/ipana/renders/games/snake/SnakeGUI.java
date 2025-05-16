package ipana.renders.games.snake;

import ipana.renders.ingame.cosmetics.ColorPicker;
import ipana.utils.font.FontHelper;
import ipana.utils.font.FontUtil;
import ipana.utils.math.MathUtils;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;

public class SnakeGUI extends GuiScreen {

    private Snake snake = new Snake();

    public final static SnakeGUI instance = new SnakeGUI();
    private long ms;
    private int foodX = -1;
    private int foodY = -1;
    public int foodType = 0;
    public int tailAdd = 0;
    private int gameSpeed = 500;
    private int score;
    private final FontUtil font = FontHelper.SIZE_18;
    public Color selected = Color.green;

    private ColorPicker picker = new ColorPicker(yok -> {
        selected = yok.currentColor;
    }, selected.getRGB());

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        ScaledResolution sr = RenderUtils.SCALED_RES;
        int x = sr.getScaledWidth() / 2 - 100;
        int y = sr.getScaledHeight() / 2 - 100;
        int width = 200;
        int height = 200;
        font.drawStringWithShadow("Score : "+score,x,y-12,Color.white.getRGB());
        Gui.drawRect(x, y, x + width-1, y + height-1, Color.gray.getRGB());
        if (foodX == -1 && foodY == -1) {
            resetFood();
            while (spawnCheck()) {
                resetFood();
            }
        }
        int ananX = x;
        int ananY = y;
        for (int a = 0; a < 18; a++) {
            for (int i = 0; i < 18; i++) {
                Gui.drawRect(ananX + i * 10 + 1, ananY + 1, ananX + i * 10 + 11, ananY + 11, Color.darkGray.getRGB());
                if (i == foodX && a == foodY) {
                    if (foodType == 0) {
                        Gui.drawRect(ananX + i * 10 + 3, ananY + 3, ananX + i * 10 + 9, ananY + 9, Color.yellow.getRGB());
                    } else if (foodType == 1) {
                        Gui.drawRect(ananX + i * 10 + 2, ananY + 2, ananX + i * 10 + 10, ananY + 10, Color.yellow.getRGB());
                    } else if (foodType == 2) {
                        Gui.drawRect(ananX + i * 10 + 2, ananY + 2, ananX + i * 10 + 10, ananY + 10, new Color(255, 111, 0, 255).getRGB());
                    }
                }
                ananX += 1;
            }
            ananY += 11;
            ananX = x;
        }

        int cX = x+width/2-40;
        int cY = y+height+5;
        picker.draw(cX, cY, 80,80, mouseX, mouseY, selected);
        updateSnake(x,y,width, height, foodX*11, foodY*11);
        postUpdate();
        renderSnake(x, y);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ScaledResolution sr = RenderUtils.SCALED_RES;
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private boolean spawnCheck() {
        for (Snake.Tail tail : snake.tailList) {
            if (foodX*11 == tail.x && foodY*11 == tail.y) {
                return true;
            }
        }
        return false;
    }

    private void postUpdate() {
        if (snake.dead) {
            snake.dead = false;
            score = 0;
            gameSpeed = 500;
            snake = new Snake();
        } else {
            if (tailAdd > 0 && snake.foodEaten) {
                if ((foodType == 2 && tailAdd == 5) || (foodType == 1 && tailAdd == 3) || (foodType == 0 && tailAdd == 1)) {
                    resetFood();
                    while (spawnCheck()) {
                        resetFood();
                    }
                }
                score+=1;
                if (gameSpeed > 200) {
                    gameSpeed-=15;
                }
                snake.tailList.add(new Snake.Tail(snake.lastTailX,snake.lastTailY,false));
                tailAdd--;
                if (tailAdd == 0) {
                    snake.foodEaten = false;
                }
            }
        }
    }

    private void resetFood() {
        foodX = MathUtils.random(0,18);
        foodY = MathUtils.random(0,18);
        int random = MathUtils.random(0,100);
        if (random <= 15) {
            foodType = 1;
        }
        if (random == 96) {
            foodType = 2;
        }
        if (random != 96 && random > 15) {
            foodType = 0;
        }
    }

    private void renderSnake(int x, int y) {
        for (Snake.Tail tail : snake.tailList) {
            if (tail.head) {
                Gui.drawRect(x + tail.x + 1, y + tail.y + 1, x + tail.x + 11, y + tail.y + 11, new Color(255-selected.getRed(), 255-selected.getGreen(), 255-selected.getBlue()));
            } else {
                Gui.drawRect(x + tail.x + 1, y + tail.y + 1, x + tail.x + 11, y + tail.y + 11, selected.getRGB());
            }
        }
    }

    public void updateSnake(int x, int y, int width, int height, int foodX, int foodY) {
        if (System.currentTimeMillis() - ms >= gameSpeed) {
            snake.goTo(x,y,width,height, foodX, foodY);
            ms = System.currentTimeMillis();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_UP) {
            if (snake.getFacing() != Snake.Facing.DOWN) {
                snake.setFacing(Snake.Facing.UP);
            }
        } else if (keyCode == Keyboard.KEY_DOWN) {
            if (snake.getFacing() != Snake.Facing.UP) {
                snake.setFacing(Snake.Facing.DOWN);
            }
        } else if (keyCode == Keyboard.KEY_RIGHT) {
            if (snake.getFacing() != Snake.Facing.LEFT) {
                snake.setFacing(Snake.Facing.RIGHT);
            }
        } else if (keyCode == Keyboard.KEY_LEFT) {
            if (snake.getFacing() != Snake.Facing.RIGHT) {
                snake.setFacing(Snake.Facing.LEFT);
            }
        }
    }
}

