package ipana.modules.movement.modes.phase;

import ipana.events.*;
import ipana.managements.module.Modules;
import ipana.modules.movement.Phase;
import ipana.utils.math.MathUtils;
import ipana.utils.player.PlayerUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import static ipana.utils.player.PlayerUtils.*;

public class AntiPhase extends PhaseMode {

    public AntiPhase(Phase parent) {
        super("AntiPhase", parent);
    }
    private int fast;
    private boolean goToNextBlock;
    private AxisAlignedBB prevBB;
    private int downwardsCoolDown;
    private int troll;

    @Override
    public void onMove(EventMoving event) {
        boolean isInBlock = isInBlock();
        if (mc.gameSettings.keyBindJump.pressed && isInBlock) {
            event.setY(mc.thePlayer.motionY = 0);
        }
        if (Modules.QUICK_USE.canUse() && isInBlock) {
            event.setX(0);
            event.setZ(0);
        }
        if (!goToNextBlock) {
            AxisAlignedBB bb = getBBFromXYZ(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            if (isInBlock && isInBox(bb) && !mc.gameSettings.keyBindSneak.pressed && !mc.gameSettings.keyBindJump.pressed) {
                double base = baseSpeed311(event.getX(), event.getZ());
                double[] c = PlayerUtils.calculate(mc.gameSettings.keyBindSprint.pressed ? Math.min(0.25, base) : base);
                BlockPos pos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                BlockPos offsetPos = new BlockPos(mc.thePlayer.posX+c[0], mc.thePlayer.posY, mc.thePlayer.posZ+c[1]);
                if ((pos.getX() != offsetPos.getX() || pos.getZ() != offsetPos.getZ())) {
                    double dist = calcDistToNextBlock(base);
                    c = PlayerUtils.calculate(dist);
                    goToNextBlock = true;
                }
                event.setX(c[0]);
                event.setZ(c[1]);
            }
        }
        if (isInBlock && mc.thePlayer.movementInput.jump) {
            event.setX(0);
            event.setZ(0);
        }
        if (troll != 0) {
            event.setX(0);
            event.setZ(0);
        }
    }

    private double calcDistToNextBlock(double hDist) {
        int i = 0;
        double increment = 1E-4;
        while (true) {
            double multiplier = increment*i;
            if (multiplier > hDist) {
                return hDist;
            }
            double[] c = PlayerUtils.calculate(multiplier);
            BlockPos pos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            BlockPos offsetPos = new BlockPos(mc.thePlayer.posX+c[0], mc.thePlayer.posY, mc.thePlayer.posZ+c[1]);
            if ((pos.getX() != offsetPos.getX() || pos.getZ() != offsetPos.getZ())) {
                double[] atat = PlayerUtils.calculate(increment*(i-1));
                return Math.hypot(atat[0], atat[1]);
            }
            i++;
        }
    }

    @Override
    public void onBeforePre(EventExcuseMeWTF event) {
        mc.thePlayer.stepHeight = 0.0f;
        mc.thePlayer.noClip = true;
    }
    @Override
    public void onPre(EventPreUpdate event) {
        boolean isInBlock = isInBlock();
        mc.thePlayer.stepHeight = 0.0f;
        mc.thePlayer.noClip = true;
        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
        double lastDist = Math.hypot(xDist, zDist);
        if (mc.thePlayer.movementInput.sneak) {
            AxisAlignedBB bb = getBBFromXYZ(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            if (!isInBox(bb) || !isInBox(prevBB) || downwardsCoolDown > 0 || mc.gameSettings.keyBindSprint.pressed) {
                fast *= -1;
                if (!(mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)).getBlock() instanceof BlockAir)) {
                    event.setYaw(event.getYaw() + (fast > 0 ? 35 : -35));
                }
            }
        }
        if (mc.thePlayer.movementInput.jump && isInBlock) {
            if ((int)mc.thePlayer.posY == mc.thePlayer.posY) {
                event.setY(event.getY() + 0.0624);
            }
        }
        if (!PlayerUtils.isMoving2() && !mc.thePlayer.isUsingItem() && isInBlock && (!Modules.KILL_AURA.isEnabled() || Modules.KILL_AURA.targets.isEmpty()) && lastDist == 0 && mc.thePlayer.prevPosY == mc.thePlayer.posY && !mc.gameSettings.keyBindSneak.pressed && !mc.gameSettings.keyBindJump.pressed) {
            event.setCancelPackets(true);
        }
        if (troll != 0) {
            event.setCancelPackets(true);
        }
        if (goToNextBlock) {
            float pitch = event.getPitch()+20;
            if (pitch >= 90) {
                pitch -= 40;
            }
            event.setPitch(pitch);
        }
    }

    @Override
    public void onPost(EventPostUpdate event) {
        AxisAlignedBB bb = getBBFromXYZ(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        mc.timer.timerSpeed = 1f;
        if (mc.gameSettings.keyBindSprint.isPressed()) {
            troll = 0;
        }
        if (goToNextBlock && troll == 0) {
            //This thing doesn't work
            PlayerUtils.debug("PHASE START");
            send(mc.thePlayer.posX, 8.988465674311579E307,mc.thePlayer.posZ);
            double firstX = mc.thePlayer.posX;
            double firstY = mc.thePlayer.posY;
            double firstZ = mc.thePlayer.posZ;
            PlayerUtils.cancelFlag(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false);
            //When we came to the side of next block we offset 0.02 NCP can't detect values less than 0.0625
            double[] atat = PlayerUtils.calculate(0.02);
            mc.thePlayer.expandPos(atat[0], 0, atat[1]);
            PlayerUtils.sendOffset(0, 0, 0);
            double offsetX = mc.thePlayer.posX;
            double offsetY = mc.thePlayer.posY;
            double offsetZ = mc.thePlayer.posZ;
            //When we try to move NCP will send us a packet which are offsetX, offsetY, offsetZ
            double[] atat2 = PlayerUtils.calculate(0.0626);
            PlayerUtils.sendOffset(atat2[0], 0, atat2[1]);
            //Basically we tried to move and NCP is going to send us flag packets, but we already know the positions, so we send the packet.
            PlayerUtils.cancelFlag(offsetX, offsetY, offsetZ, true);
            PlayerUtils.debug("predict: "+firstX+", "+firstY+", "+firstZ+", "+mc.thePlayer.rotationYaw +", "+ mc.thePlayer.rotationPitch);

            goToNextBlock = false;
            //Timer can be increased
            mc.timer.timerSpeed = 0.9f;
            troll = 2173;
        }
        if (mc.thePlayer.movementInput.jump && isInBlock()) {
            double bokY = bb == null ? (int)mc.thePlayer.posY+1-mc.thePlayer.posY : bb.maxY-mc.thePlayer.posY;
            mc.thePlayer.motionY = 0;
            sendOffset(0, bokY-1E-3, 0, false);
            mc.thePlayer.expandPos(0, bokY, 0);
            sendOffset(0, 0, 0);
            //We offset 0.0624 to force ncp to send us flag packets.
            sendOffset(0, 0.0624, 0, false);
            //We send our flag packet.
            cancelFlag(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false);
            mc.timer.timerSpeed = 0.8f;
        }
        if (mc.thePlayer.movementInput.sneak) {
            mc.thePlayer.motionY = 0;
            if (!isInBox(bb)) {
                //First Collide
                double[] offset = PlayerUtils.calculate(0.1);
                AxisAlignedBB collision = PlayerUtils.getCollision(offset[0], 0, offset[1]);
                if (collision != null && diffBetweenCollision(collision) <= 0.0625) {
                    double colX = (collision.minX+collision.maxX)/2;
                    double colZ = (collision.minZ+collision.maxZ)/2;
                    mc.thePlayer.setPosition(colX, mc.thePlayer.posY, colZ);
                    PlayerUtils.sendOffset(0, 0, 0);
                    PlayerUtils.sendOffset(0, -1E-4, 0);
                    mc.thePlayer.expandPos(offset[0], 0, offset[1]);
                    PlayerUtils.sendOffset(0, 0, 0);
                    PlayerUtils.cancelFlag(colX, mc.thePlayer.posY - 1E-4, colZ, true);

                    if (getBBFromXYZ(mc.thePlayer.posX, mc.thePlayer.posY - 1.0001, mc.thePlayer.posZ) != null) {
                        PlayerUtils.sendOffset(0, -1E-4, 0);
                        mc.thePlayer.setPosition(colX, (int) mc.thePlayer.posY, colZ);
                        sendOffset(0, 0, 0);
                        PlayerUtils.sendOffset(0, -1, 0);
                    } else {
                        AxisAlignedBB downBlock = getBBFromXYZ(mc.thePlayer.posX, mc.thePlayer.posY - 1E-4, mc.thePlayer.posZ);
                        double startY = downBlock.maxY;
                        mc.thePlayer.setPosition(colX, startY - 0.0625, colZ);
                        for (int i = 0; i < 7; i++) {
                            PlayerUtils.sendOffset(0, 0, 0);
                            PlayerUtils.sendOffset(0, -0.0784, 0);
                            cancelFlag(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false, false);
                        }
                        PlayerUtils.sendOffset(0, 0, 0);
                        PlayerUtils.sendOffset(0, -0.0785, 0);
                        cancelFlag(mc.thePlayer.posX, mc.thePlayer.posY - 0.0784, mc.thePlayer.posZ, true, true);
                        mc.thePlayer.setPosition(colX, startY - 1, colZ);
                    }
                    downwardsCoolDown = 5;
                }
                if (mc.gameSettings.keyBindSprint.pressed) {
                    sendOffset(0, -1E-4, 0);
                    sendOffset(0, 1.34, 0);
                }
            } else {
                if (downwardsCoolDown <= 0 && getBBFromXYZ(mc.thePlayer.posX, mc.thePlayer.posY-1, mc.thePlayer.posZ) != null) {
                    //Instant vertical
                    sendOffset(0, -5E-4, 0);
                    sendOffset(0, -0.0626, 0);
                    cancelFlag(mc.thePlayer.posX, mc.thePlayer.posY - 5E-4, mc.thePlayer.posZ, false);
                    mc.thePlayer.setPosition(mc.thePlayer.posX, (int) (mc.thePlayer.posY - 1E-4), mc.thePlayer.posZ);
                    sendOffset(0, 0, 0);
                    mc.timer.timerSpeed = 0.8f;
                }
            }
        }
        if (mc.gameSettings.keyBindSprint.pressed && !mc.thePlayer.movementInput.sneak) {
            mc.thePlayer.motionY = 0;
        }
        prevBB = bb;
        downwardsCoolDown--;
    }

    @Override
    public void onBB(EventBoundingBox event) {
        if (isInBlock() && event.getBoundingBox() != null && event.getBoundingBox().maxY > mc.thePlayer.boundingBox.minY) {
            event.setBoundingBox(null);
        }
    }

    @Override
    public void onSetBack(EventSetBack event) {
        if (event.state() == EventSetBack.State.PRE) {
            if (troll == 2173 && !event.cancelPacket() && !event.cancelSetPos()) {
                PlayerUtils.debug("real?: "+event.cancelPacket()+", "+event.xOff()+", "+event.zOff());
                event.cancelSetPos(true);
                troll = 2174;
            }
        }
        if (event.state() == EventSetBack.State.POST) {
            PlayerUtils.debug("flag: "+event.cancelPacket()+", "+event.xOff()+", "+event.zOff());
            /*if (mc.gameSettings.keyBindSneak.pressed && !mc.gameSettings.keyBindSprint.pressed && !mc.gameSettings.keyBindJump.pressed) {
                AxisAlignedBB bb = getBBFromXYZ(mc.thePlayer.posX, mc.thePlayer.posY - 1E-4, mc.thePlayer.posZ);
                double diff = bb.maxY - event.yOff();
                boolean flagCheck = event.cancelPacket() || event.cancelSetPos();
                if ((diff > 0 && diff < 0.0625 || diff < 0 && diff > -(1 - 0.0625)) && !flagCheck) {
                    if (getBBFromXYZ(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ) != null) {
                        //Do I really need this rn?
                        //Instant vertical
                        downwardsCoolDown = 1;
                        sendOffset(0, -1E-4, 0);
                        mc.thePlayer.setPosition(event.xOff(), (int) mc.thePlayer.posY, event.zOff());
                        sendOffset(0, 0, 0);
                        PlayerUtils.sendOffset(0, -1, 0);
                    } else {
                        //Instant under block vertical abusing SurvivalFly's verticalAccounting
                        double startY = bb.maxY;
                        mc.thePlayer.setPosition(event.xOff(), startY - 0.0625, event.zOff());
                        for (int i = 0; i < 7; i++) {
                            PlayerUtils.sendOffset(0, 0, 0);
                            PlayerUtils.sendOffset(0, -0.0784, 0);
                            cancelFlag(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false, false);
                        }
                        PlayerUtils.sendOffset(0, 0, 0);
                        PlayerUtils.sendOffset(0, -0.0785, 0);
                        cancelFlag(mc.thePlayer.posX, mc.thePlayer.posY - 0.0784, mc.thePlayer.posZ, true, true);
                        mc.thePlayer.setPosition(event.xOff(), startY - 1, event.zOff());
                    }
                }
            }*/
        }
        super.onSetBack(event);
    }

    @Override
    public void onSend(EventPacketSend event) {

    }

    @Override
    public void onEnable() {
        fast = 90;
        prevBB = null;
        goToNextBlock = false;
        super.onEnable();
    }
}