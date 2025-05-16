package ipana.modules.movement.modes.speed;

import ipana.events.*;
import ipana.modules.movement.Speed;
import ipana.utils.player.PlayerUtils;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.Vec3;

import static ipana.utils.player.PlayerUtils.*;

public class Cold extends SpeedMode {
    public Cold(Speed parent) {
        super("Cold", parent);
    }
    private int motionTicks;
    private int cooldownHops;
    private int state;
    private double moveSpeed;
    private double lastDist;
    private double nextYDist;
    private Vec3 predictedFlag;
    private boolean wasOnWater;
    private boolean flagOut;

    @Override
    public void onEnable() {
        motionTicks = -1;
        state = 0;
        //sendOffset(0, 0.8, 0);
        nextYDist = Double.MAX_VALUE;

        if (mc.thePlayer.onGround) {
            double END_VALUE = 0.016;
            double INC = 0.0626;


            sendOffset(0,0,0);
            sendOffset(0, END_VALUE + INC * 2 + 1E-4, 0);
            sendOffset(0, END_VALUE + INC, 0);
            sendOffset(0, END_VALUE, 0);
            sendOffset(0, 0.45, 0);
            cancelFlag(mc.thePlayer.posX, mc.thePlayer.posY + END_VALUE + INC, mc.thePlayer.posZ, false);
        }
        cooldownHops = 0;
        super.onEnable();
    }

    @Override
    public void onMoving(EventMoving event) {

        double hAllowedBase = getBaseMoveSpeed();


        if (motionTicks == -2) {
            moveSpeed = 0.0;
            event.setY(-0.21);
        } else if (motionTicks == -1) {
            moveSpeed = hAllowedBase;
            event.setY(-0.21);
            motionTicks = 0;
        } else if (motionTicks == 0) {
            moveSpeed = lastDist * 2.15 - 1E-7;
            //event.setY(mc.thePlayer.motionY = 0.42);
            motionTicks = 1;
        } else if (motionTicks == 1) {
            double difference = 0.66 * (lastDist - hAllowedBase);
            moveSpeed = lastDist - difference;

            cooldownHops++;
            if (cooldownHops >= 15) {
                cooldownHops = 0;

                double[] currentH = calculate(moveSpeed);
                double collisionOff = 0.0161 - 0.4;
                mc.thePlayer.expandPos(0, 0.4, 0);
                mc.thePlayer.expandPos(currentH[0], collisionOff / 2 - 1E-4, currentH[1]);
                //PlayerUtils.debug(mc.thePlayer.posY);
                PlayerUtils.sendOffset(0, 0, 0);

                predictedFlag = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                //PlayerUtils.debug(predictedFlag);
                double[] nextH = calculate(moveSpeed - moveSpeed / 160 - 1E-7);
                mc.thePlayer.expandPos(nextH[0], collisionOff / 2, nextH[1]);
                //PlayerUtils.debug(mc.thePlayer.posY);
                PlayerUtils.sendOffset(0, 0, 0);
                flagOut = true;
                state = 1;
            }
            motionTicks = 0;
        }

        event.setSpeed(moveSpeed);

        if (state == 1) {
            event.setX(0);
            event.setY(0);
            event.setZ(0);
            state = 0;
        }

        super.onMoving(event);
    }

    @Override
    public void onPre(EventPreUpdate event) {
        lastDist = Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ);
        //PlayerUtils.debug(flagOut+", "+lastDist);
        if (motionTicks == 1) {
            event.setY(event.getY() + 0.4);
        }
        if (flagOut) {
            event.setY(event.getY() + 0.45);
        }
        super.onPre(event);
    }

    @Override
    public void onPost(EventPostUpdate event) {
        if (flagOut) {
            motionTicks = -1;
            //PlayerUtils.debug("PREDICT: "+predictedFlag);
            cancelFlag(predictedFlag.xCoord, predictedFlag.yCoord, predictedFlag.zCoord, true);
            flagOut = false;
        }
        super.onPost(event);
    }

    @Override
    public void onReceive(EventPacketReceive event) {
        if (event.getState() != EventPacketReceive.PacketState.PRE) {
            return;
        }
        if (event.getPacket() instanceof S08PacketPlayerPosLook s08) {
            //PlayerUtils.debug("FLAG: "+s08.x+" , "+s08.y+" , "+s08.z);
            //cooldownHops = 0;
            //motionTicks = -1;
        }
        super.onReceive(event);
    }

    /*@Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onMoving(EventMoving event) {
        Speed speed = getParent();
        double base = getBaseMoveSpeed();
        if (speed.ticks == 0) {
            speed.spd = base;
        } else if (speed.ticks == 1) {
            speed.spd *= 1.7;
        } else if (speed.ticks == 2) {
            double difference = 0.66 * (speed.lastDist - base);
            speed.spd = speed.lastDist - difference;
        } else if (speed.ticks >= 3) {
            speed.spd = speed.lastDist - speed.lastDist / (160 - 1E-7);
        }
        double[] c = PlayerUtils.calculate(speed.spd);
        event.setX(c[0]);
        event.setZ(c[1]);
        super.onMoving(event);
    }

    @Override
    public void onPre(EventPreUpdate event) {
        Speed speed = getParent();
        if (speed.ticks == 1) {
            double yDiff = event.getY()-(int)mc.thePlayer.posY;
            event.setY(event.getY()+0.4-yDiff);
        }
        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
        speed.lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
        super.onPre(event);
    }

    @Override
    public void onMove(EventMoveInput event) {
        Speed speed = getParent();
        super.onMove(event);
    }

    @Override
    public void onPost(EventPostUpdate event) {
        Speed speed = getParent();
        speed.ticks++;
        if (!PlayerUtils.isMoving2()) {
            speed.ticks = 0;
        }
    }

     */
}