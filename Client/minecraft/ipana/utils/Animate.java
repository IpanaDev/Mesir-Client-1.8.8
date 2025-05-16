package ipana.utils;

public class Animate {
    public int x, prevX, y, prevY;

    public Animate(int startX, int startY) {
        this.x = startX;
        this.prevX = startX;
        this.y = startY;
        this.prevY = startY;
    }


    public boolean animateX(int toX, int speed) {
        prevX = x;
        if (x > toX) {
            x -= speed;
            if (x < toX) {
                x = toX;
            }
            return false;
        } else if (x < toX) {
            x += speed;
            if (x > toX) {
                x = toX;
            }
            return false;
        } else {
            /* Returns true when animation finished */
            return true;
        }
    }

    public boolean animateY(int toY, int speed) {
        prevY = y;
        if (y > toY) {
            y -= speed;
            if (y < toY) {
                y = toY;
            }
            return false;
        } else if (y < toY) {
            y += speed;
            if (y > toY) {
                y = toY;
            }
            return false;
        } else {
            /* Returns true when animation finished */
            return true;
        }
    }

    public float renderX(float partialTicks) {
        return prevX + (x - prevX) * partialTicks;
    }
    public float renderY(float partialTicks) {
        return prevY + (y - prevY) * partialTicks;
    }
}
