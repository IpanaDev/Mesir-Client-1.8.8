package ipana.renders.games.snake;

import java.util.ArrayList;
import java.util.List;

public class Snake {
    public List<Tail> tailList;
    private Facing facing;
    public boolean dead;
    public boolean foodEaten;
    public int lastTailX = 0;
    public int lastTailY = 0;

    public Snake() {
        this.tailList = new ArrayList<>();
        tailList.add(new Tail(99,99,true));
        tailList.add(new Tail(99,110,false));
        tailList.add(new Tail(99,121,false));
        tailList.add(new Tail(99,132,false));
        facing = Facing.UP;
    }

    public void goTo(int x, int y, int width, int height, int foodX, int foodY) {
        int snakeSpeed = 11;
        int lastX = 0;
        int lastY = 0;
        for (Tail tail : tailList) {
            if (tail.head) {
                lastX = tail.x;
                lastY = tail.y;
                move(tail, snakeSpeed);
                if (tail.x+x+11 >= x+width || tail.x+x < x || tail.y+y+11 >= y+height || tail.y+y < y) {
                    dead = true;
                }
                if (!dead) {
                    for (int i = 1; i < tailList.size(); i++) {
                        Tail tails = tailList.get(i);
                        if (tail.x == tails.x && tail.y == tails.y) {
                            dead = true;
                            break;
                        }
                    }
                }
                if (!dead) {
                    if (tail.x == foodX && tail.y == foodY) {
                        foodEaten = true;
                        if (SnakeGUI.instance.foodType == 0) {
                            SnakeGUI.instance.tailAdd = 1;
                        } else if (SnakeGUI.instance.foodType == 1) {
                            SnakeGUI.instance.tailAdd = 3;
                        } else if (SnakeGUI.instance.foodType == 2) {
                            SnakeGUI.instance.tailAdd = 5;
                        }
                    }
                }
            }
            if (!tail.head) {
                int preX = tail.x;
                int preY = tail.y;
                tail.x = lastX;
                tail.y = lastY;
                lastX = preX;
                lastY = preY;
            }
        }
        lastTailX = lastX;
        lastTailY = lastY;
    }

    private void move(Tail tail, int snakeSpeed) {
        Facing facing = getFacing();
        if (facing == Facing.UP) {
            updateTo(tail, 0, -snakeSpeed);
        } else if (facing == Facing.DOWN) {
            updateTo(tail, 0, snakeSpeed);
        } else if (facing == Facing.RIGHT) {
            updateTo(tail, snakeSpeed, 0);
        } else if (facing == Facing.LEFT) {
            updateTo(tail, -snakeSpeed, 0);
        }
    }

    private void updateTo(Tail tail,int x, int y) {
        tail.x+=x;
        tail.y+=y;
    }

    public Facing getFacing() {
        return facing;
    }

    public void setFacing(Facing facing) {
        this.facing = facing;
    }

    public enum Facing {
        UP,DOWN,RIGHT,LEFT
    }

    public static class Tail {
        public int x;
        public int y;
        public boolean head;

        public Tail(int x, int y,boolean head) {
            this.x = x;
            this.y = y;
            this.head = head;
        }
    }
}
