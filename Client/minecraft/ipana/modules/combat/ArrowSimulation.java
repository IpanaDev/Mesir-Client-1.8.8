package ipana.modules.combat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ArrowSimulation {
    private EntityLivingBase shooter;
    private double motionX, motionY, motionZ;
    private double posX, posY, posZ;

    public ArrowSimulation(EntityLivingBase shooter) {
        this.shooter = shooter;
    }

    public static ArrowSimulation newSimulation(EntityLivingBase shooter) {
        return new ArrowSimulation(shooter);
    }

    private List<Vec3> simulatePoints(int ticks) {
        float f = ticks / 20.0F;
        f *= f + 2.0f;
        f /= 3.0f;
        if (f < 0.1f) {
            return new ArrayList<>();
        }
        if (f > 1.0F) {
            f = 1.0F;
        }
        this.posX = shooter.posX;
        this.posY = shooter.posY + shooter.getEyeHeight();
        this.posZ = shooter.posZ;
        float yaw = shooter.rotationYaw / 180.0F * (float)Math.PI;
        float pitch = shooter.rotationPitch / 180.0F * (float)Math.PI;
        this.posX -= MathHelper.cos(yaw) * 0.16F;
        this.posY -= 0.10000000149011612D;
        this.posZ -= MathHelper.sin(yaw) * 0.16F;

        float velocity = f * 2.0f;
        this.motionX = -MathHelper.sin(yaw) * MathHelper.cos(pitch);
        this.motionZ = MathHelper.cos(yaw) * MathHelper.cos(pitch);
        this.motionY = -MathHelper.sin(pitch);
        this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, velocity * 1.5f);

        List<Vec3> points = new ArrayList<>();
        for (int i = 0; i < ticks; i++) {
            update();
            points.add(new Vec3(this.posX, this.posY, this.posZ));
        }

        return points;
    }

    public void setThrowableHeading(double x, double y, double z, float velocity) {
        double f = MathHelper.sqrt_double(x * x + y * y + z * z);
        x = x / f;
        y = y / f;
        z = z / f;
        x = x * (double)velocity;
        y = y * (double)velocity;
        z = z * (double)velocity;
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
    }

    public void update() {
        float f4 = 0.99F;
        float f6 = 0.05F;
        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;

        this.motionX *= f4;
        this.motionY *= f4;
        this.motionZ *= f4;
        this.motionY -= f6;
    }
}
