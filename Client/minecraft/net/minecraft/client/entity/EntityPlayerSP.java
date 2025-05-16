package net.minecraft.client.entity;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.event.events.ChatEvent;
import baritone.api.event.events.PlayerUpdateEvent;
import baritone.api.event.events.type.EventState;
import baritone.behavior.LookBehavior;
import ipana.Ipana;
import ipana.events.*;
import ipana.irc.packet.Packets;
import ipana.managements.module.ModuleManager;
import ipana.managements.module.Modules;
import ipana.modules.exploit.LessPackets;
import ipana.modules.movement.Fly;
import ipana.modules.movement.modes.fly.Vehicle;
import ipana.utils.baritone.BaritoneHelper;
import ipana.utils.player.PlayerUtils;
import ipana.utils.render.Anims;
import ipana.utils.render.EmoteUtils;
import ipana.utils.render.PlayerAnim;
import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockPackedIce;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSoundMinecartRiding;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.potion.Potion;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.*;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class EntityPlayerSP extends AbstractClientPlayer
{
    public final NetHandlerPlayClient sendQueue;
    private final StatFileWriter statWriter;

    //private double lastReportedPosX;

    //private double lastReportedPosY;

    //private double lastReportedPosZ;

    /**
     * The last yaw value which was transmitted to the server, used to determine when the yaw changes and needs to be
     * re-transmitted
     */
    public float lastReportedYaw;

    /**
     * The last pitch value which was transmitted to the server, used to determine when the pitch changes and needs to
     * be re-transmitted
     */
    public float lastReportedPitch;

    public double lastReportedPosX;
    public double lastReportedPosY;
    public double lastReportedPosZ;
    private int positionUpdateTicks;
    /** the last sneaking state sent to the server */
    public boolean serverSneakState;

    /** the last sprinting state sent to the server */
    public boolean serverSprintState;

    /**
     * Reset to 0 every time position is sent to the server, used to send periodic updates every 20 ticks even when the
     * player is not moving.
     */
    private boolean hasValidHealth;
    private String clientBrand;
    protected Minecraft mc;
    public MovementInput movementInput;
    /**
     * Used to tell if the player pressed forward twice. If this is at 0 and it's pressed (And they are allowed to
     * sprint, aka enough food on the ground etc) it sets this to 7. If it's pressed and it's greater than 0 enable
     * sprinting.
     */
    protected int sprintToggleTimer;

    /** Ticks left before sprinting is disabled. */
    public int sprintingTicksLeft;
    public float renderArmYaw;
    public float renderArmPitch;
    public float prevRenderArmYaw;
    public float prevRenderArmPitch;
    private int horseJumpPowerCounter;
    private float horseJumpPower;

    /** The amount of time an entity has been in a Portal */
    public float timeInPortal;

    /** The amount of time an entity has been in a Portal the previous tick */
    public float prevTimeInPortal;
    public float worldTime;
    private boolean timeChange;
    private PlayerUtils.Face8Direction[] faces;
    public int iceTicks;
    public boolean prevOnIce, onIce;
    public float prevForward, prevStrafe;
    public EnumFacing[] visibleFaces = new EnumFacing[4];
    public double viewX, viewY, viewZ;
    public ArrayDeque<LessPackets.PlayerPosLook> packetQueue = new ArrayDeque<>();
    public int stopSprint;


    public EntityPlayerSP(Minecraft mcIn, World worldIn, NetHandlerPlayClient netHandler, StatFileWriter statFile) {
        super(worldIn, netHandler.getGameProfile());
        this.sendQueue = netHandler;
        this.statWriter = statFile;
        this.mc = mcIn;
        movementInput = new MovementInputFromOptions(mc.gameSettings);
        this.dimension = 0;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        return false;
    }

    private boolean godVelocity;
    private double velX, velY, velZ;

    @Override
    public void setVelocity(double x, double y, double z) {
        super.setVelocity(x, y, z);
        velX = x;
        velY = y;
        velZ = z;
        if (Math.hypot(x,z) >= 1E-4 && y > 0) {
            godVelocity = true;
        }
    }

    @Override
    protected void moveSetup() {
        EventTravel event = new EventTravel(this.moveForward, this.moveStrafing, this.rotationYaw);
        event.fire();
        float lastYaw = this.rotationYaw;
        this.rotationYaw = event.yaw();

        if (this.onGround && this.godVelocity && !Modules.ANTI_KB.isEnabled()) {
            double preHVel = Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ);
            this.jump();
            double postHVel = Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ);
            //PlayerUtils.debug(preHVel - postHVel);
            //this.motionY = 0.41;
        }

        if (this.isJumping) {
            if (this.isInWater()) {
                this.updateAITick();
            } else if (this.isInLava()) {
                this.handleJumpLava();
            } else if (this.onGround && this.jumpTicks == 0) {
                this.jump();
                this.jumpTicks = 10;
            }
        } else {
            this.jumpTicks = 0;
        }

        this.worldObj.theProfiler.endSection();
        this.worldObj.theProfiler.startSection("travel");
        event.setStrafe(event.strafe() * 0.98f);
        event.setForward(event.forward() * 0.98f);
        this.moveStrafing *= 0.98f;
        this.moveForward *= 0.98f;
        this.randomYawVelocity *= 0.9F;
        this.moveEntityWithHeading(event.strafe(), event.forward());
        this.worldObj.theProfiler.endSection();
        this.worldObj.theProfiler.startSection("push");
        this.godVelocity = false;
        this.rotationYaw = lastYaw;
    }

    /**
     * Heal living entity (param: amount of half-hearts)
     */



    public void moveEntity(double x, double y, double z) {
        EventMoving event = new EventMoving(x,y,z);
        event.fire();

        super.moveEntity(event.getX(), event.getY(), event.getZ());
    }

    public void moveEntityNoEvent(double x, double y, double z) {
        super.moveEntity(x, y, z);
    }

    public void heal(float healAmount)
    {
    }

    /**
     * Called when a player mounts an entity. e.g. mounts a pig, mounts a boat.
     */
    public void mountEntity(Entity entityIn)
    {
        super.mountEntity(entityIn);

        if (entityIn instanceof EntityMinecart)
        {
            this.mc.getSoundHandler().playSound(new MovingSoundMinecartRiding(this, (EntityMinecart)entityIn));
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {

        if (this.worldObj.isBlockLoaded(new BlockPos(this.posX, 0.0D, this.posZ)))
        {
            IBaritone baritone = BaritoneAPI.getProvider().getBaritoneForPlayer(this);

            worldTime+=timeChange?0.05f:-0.05f;
            if (worldTime<=0) {
                timeChange = true;
            } else if (worldTime >= 254) {
                timeChange = false;
            }
            new EventExcuseMeWTF().fire();
            super.onUpdate();
            if (iceTicks > 0) {
                iceTicks--;
            }
            prevOnIce = onIce;
            onIce = iceTicks > 0;
            Block block = mc.theWorld.getBlockState(mc.thePlayer.getPositionVector().add(0, -1E-4, 0).toPos()).getBlock();
            if ((block instanceof BlockPackedIce) || (block instanceof BlockIce)) {
                iceTicks = 20;
            }
            if (baritone != null) {
                baritone.getGameEventHandler().onPlayerUpdate(new PlayerUpdateEvent(EventState.PRE));
            }
            setupFaces();
            EventPreUpdate pre = new EventPreUpdate(posX,posY,posZ,rotationYaw,rotationPitch,onGround);
            if (this.isRiding()) {
                pre.fire();
                this.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(pre.getYaw(), pre.getPitch(), pre.isOnGround()));
                boolean ka = Modules.FLY.isEnabled() && Modules.FLY.mode.getValue() instanceof Vehicle;
                this.sendQueue.addToSendQueue(new C0CPacketInput(this.moveStrafing, this.moveForward, this.movementInput.jump, !ka && this.movementInput.sneak));
                if (mc.gameSettings.keyBindJump.pressed && mc.gameSettings.keyBindSprint.pressed && mc.gameSettings.keyBindSneak.pressed) {
                    PlayerUtils.packet(new C02PacketUseEntity(mc.thePlayer, C02PacketUseEntity.Action.ATTACK));
                }
                new EventPostUpdate(pre).fire();
            } else {
                this.onUpdateWalkingPlayer(pre);
            }
        }
    }

    private void setupFaces() {
        float yaw = mc.gameSettings.thirdPersonView == 2 ? rotationYaw-180 : rotationYaw;
        float pitch = rotationPitch;
        faces = PlayerUtils.getCardinalDirection(yaw);
        visibleFaces[0] = getHorizontalFacing(yaw-90);
        visibleFaces[1] = getHorizontalFacing(yaw);
        visibleFaces[2] = getHorizontalFacing(yaw+90);
        visibleFaces[3] = pitch > 10 ? EnumFacing.DOWN : pitch < -10 ? EnumFacing.UP : null;
        double speed = mc.gameSettings.thirdPersonView > 0 ? -10 : -6;
        double[] calc = PlayerUtils.calculate2(speed, yaw, 1);
        viewX = mc.thePlayer.posX+calc[0];
        viewY = mc.thePlayer.posY+mc.thePlayer.getEyeHeight();
        viewZ = mc.thePlayer.posZ+calc[1];
    }

    /**
     * called every tick when the player is on foot. Performs all the things that normally happen during movement.
     */
    public void onUpdateWalkingPlayer(EventPreUpdate pre) {
        boolean flag = this.isSprinting();

        if (flag != this.serverSprintState) {
            if (flag) {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.START_SPRINTING));
            } else {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.STOP_SPRINTING));
            }
            this.serverSprintState = flag;
        }

        boolean flag1 = this.isSneaking();

        if (flag1 != this.serverSneakState) {
            if (flag1) {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.START_SNEAKING));
            } else {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.STOP_SNEAKING));
            }

            this.serverSneakState = flag1;
        }
        if (this.isCurrentViewEntity()) {
            pre.fire();
            if (pre.isCancelled()) {
                return;
            }
            if (!packetQueue.isEmpty()) {
                LessPackets.PlayerPosLook values = packetQueue.poll();
                if (values != null) {
                    mc.thePlayer.setPosition(values.x, values.y, values.z);
                    mc.thePlayer.motionX = 0;
                    mc.thePlayer.motionY = 0;
                    mc.thePlayer.motionZ = 0;
                    pre.setX(values.x);
                    pre.setY(values.y);
                    pre.setZ(values.z);
                    pre.setYaw(values.yaw);
                    pre.setPitch(values.pitch);
                }
            }
            double d0 = pre.getX() - this.lastReportedPosX;
            double d1 = pre.getY() - this.lastReportedPosY;
            double d2 = pre.getZ() - this.lastReportedPosZ;
            double d3 = pre.getYaw() - this.lastReportedYaw;
            double d4 = pre.getPitch() - this.lastReportedPitch;
            boolean hasMoved = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4D || this.positionUpdateTicks >= 20;
            boolean hasLooked = d3 != 0.0D || d4 != 0.0D;
            sendWithType(pre, hasMoved, hasLooked);
            ++this.positionUpdateTicks;
            if (hasMoved) {
                this.lastReportedPosX = pre.getX();
                this.lastReportedPosY = pre.getY();
                this.lastReportedPosZ = pre.getZ();
                this.positionUpdateTicks = 0;
            }
            if (hasLooked) {
                this.lastReportedYaw = pre.getYaw();
                this.lastReportedPitch = pre.getPitch();
            }
            new EventPostUpdate(pre).fire();
        }
        IBaritone baritone = BaritoneAPI.getProvider().getBaritoneForPlayer(this);
        if (baritone != null) {
            baritone.getGameEventHandler().onPlayerUpdate(new PlayerUpdateEvent(EventState.POST));
        }
    }

    public void sendWithType(EventPreUpdate pre, boolean hasMoved, boolean hasLooked) {
        if (pre.type() == null) {
            if (hasMoved && hasLooked) {
                pre.setType(EventPreUpdate.UpdateType.MOVE_LOOK);
            } else if (hasMoved) {
                pre.setType(EventPreUpdate.UpdateType.MOVE);
            } else if (hasLooked) {
                pre.setType(EventPreUpdate.UpdateType.LOOK);
            } else {
                pre.setType(EventPreUpdate.UpdateType.GROUND);
            }
        }
        switch (pre.type()) {
            case MOVE_LOOK -> sendUpdatePackets(pre, new C03PacketPlayer.C06PacketPlayerPosLook(pre.getX(), pre.getY(), pre.getZ(), pre.getYaw(), pre.getPitch(), pre.isOnGround()));
            case MOVE -> sendUpdatePackets(pre, new C03PacketPlayer.C04PacketPlayerPosition(pre.getX(), pre.getY(), pre.getZ(), pre.isOnGround()));
            case LOOK -> sendUpdatePackets(pre, new C03PacketPlayer.C05PacketPlayerLook(pre.getYaw(), pre.getPitch(), pre.isOnGround()));
            case GROUND -> sendUpdatePackets(pre, new C03PacketPlayer(pre.isOnGround()));
        }
    }

    public void sendUpdatePackets(EventPreUpdate event, C03PacketPlayer packet) {
        event.setPacket(packet);
        if (!event.isCancelPackets()) {
            PlayerUtils.packet(packet);
        }
    }

    public void report(double x, double y, double z, float yaw, float pitch) {
        this.lastReportedPosX = x;
        this.lastReportedPosY = y;
        this.lastReportedPosZ = z;
        this.lastReportedYaw = yaw;
        this.lastReportedPitch = pitch;
    }
    public void report(double x, double y, double z) {
        this.lastReportedPosX = x;
        this.lastReportedPosY = y;
        this.lastReportedPosZ = z;
    }
    public void report(float yaw, float pitch) {
        this.lastReportedYaw = yaw;
        this.lastReportedPitch = pitch;
    }
    /**
     * Called when player presses the drop item key
     */
    public EntityItem dropOneItem(boolean dropAll)
    {
        C07PacketPlayerDigging.Action c07packetplayerdigging$action = dropAll ? C07PacketPlayerDigging.Action.DROP_ALL_ITEMS : C07PacketPlayerDigging.Action.DROP_ITEM;
        this.sendQueue.addToSendQueue(new C07PacketPlayerDigging(c07packetplayerdigging$action, BlockPos.ORIGIN, EnumFacing.DOWN));
        return null;
    }

    /**
     * Joins the passed in entity item with the world. Args: entityItem
     */
    protected void joinEntityItemWithWorld(EntityItem itemIn)
    {
    }
    private boolean naber;
    /**
     * Sends a chat message from the player. Args: chatMessage
     */
    public void sendChatMessage(String message) {
        ChatEvent event = new ChatEvent(message);
        IBaritone baritone = BaritoneAPI.getProvider().getBaritoneForPlayer(this);
        if (baritone == null) {
            return;
        }
        baritone.getGameEventHandler().onSendChatMessage(event);
        if (event.isCancelled()) {
            return;
        }
        for (EmoteUtils.Emote emote : EmoteUtils.getList()) {
            String emoteInChat = ":"+emote.getName()+":";
            if (message.contains(emoteInChat)) {
                Packets.EMOTE_PACKET.sendAsPacket(emote.getName());
                activeEmotes().add(new EmoteUtils.Action(emote));
            }
        }
        if (message.equalsIgnoreCase(":nah:")) {
            Anims.add(this, new PlayerAnim(":nah:",System.currentTimeMillis(),this));
        }
        if (message.equalsIgnoreCase(":emote:") || message.equalsIgnoreCase(":emoji:")) {
            for (EmoteUtils.Emote emote : EmoteUtils.getList()) {
                this.addChatMessage(new ChatComponentText(emote.getName()+" "+":"+emote.getName()+":"));
            }
        } else {
            if (message.equals("$naber knk$")) {
                naber=!naber;
            } else {
                if (naber) {
                    //Skidded from LiquidBounce
                    String msg = message;
                    StringBuilder stringBuilder = new StringBuilder();
                    List<Character> s = new ArrayList<>();
                    for (char c : msg.toCharArray()) {
                        s.add(c);
                    }
                    for (char c : s) {
                        if (c >= 33 && c <= 128) {
                            if (msg.startsWith("/")) {
                                System.out.println(c);
                                stringBuilder.append(c);
                            } else if (String.valueOf(c).equals("&")) {
                                stringBuilder.append(c);
                            } else if (s.indexOf(c) - 1 >= 0 && String.valueOf(s.get(s.indexOf(c) - 1)).equals("&")) {
                                stringBuilder.append(c);
                            } else {
                                stringBuilder.append(Character.toChars(c + 65248));
                            }
                        } else {
                            stringBuilder.append(c);
                        }
                    }
                    message = stringBuilder.toString();
                }
                this.sendQueue.addToSendQueue(new C01PacketChatMessage(message));
            }
        }
    }

    /**
     * Swings the item the player is holding.
     */
    public void swingItem()
    {
        super.swingItem();
        this.sendQueue.addToSendQueue(new C0APacketAnimation());
    }

    public void respawnPlayer()
    {
        this.sendQueue.addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.PERFORM_RESPAWN));
    }

    /**
     * Deals damage to the entity. If its a EntityPlayer then will take damage from the armor first and then health
     * second with the reduced value. Args: damageAmount
     */
    protected void damageEntity(DamageSource damageSrc, float damageAmount)
    {
        if (!this.isEntityInvulnerable(damageSrc))
        {
            this.setHealth(this.getHealth() - damageAmount);
        }
    }

    /**
     * set current crafting inventory back to the 2x2 square
     */
    public void closeScreen()
    {
        this.sendQueue.addToSendQueue(new C0DPacketCloseWindow(this.openContainer.windowId));
        this.closeScreenAndDropStack();
    }

    public void closeScreenAndDropStack()
    {
        this.inventory.setItemStack(null);
        super.closeScreen();
        this.mc.displayGuiScreen(null);
    }

    /**
     * Updates health locally.
     */
    public void setPlayerSPHealth(float health) {
        if (this.hasValidHealth) {
            float f = this.getHealth() - health;

            if (f <= 0.0F) {
                this.setHealth(health);

                if (f < 0.0F) {
                    //this.hurtResistantTime = this.maxHurtResistantTime / 2;
                }
            } else {
                this.setHealth(this.getHealth());
                this.lastDamage = f;
                this.hurtResistantTime = this.maxHurtResistantTime;
                this.damageEntity(DamageSource.generic, f);
                this.hurtTime = this.maxHurtTime = 10;
            }
        } else {
            this.setHealth(health);
            this.hasValidHealth = true;
        }
    }

    /**
     * Adds a value to a statistic field.
     */
    public void addStat(StatBase stat, int amount)
    {
        if (stat != null)
        {
            if (stat.isIndependent)
            {
                super.addStat(stat, amount);
            }
        }
    }

    /**
     * Sends the player's abilities to the server (if there is one).
     */
    public void sendPlayerAbilities()
    {
        this.sendQueue.addToSendQueue(new C13PacketPlayerAbilities(this.capabilities));
    }

    /**
     * returns true if this is an EntityPlayerSP, or the logged in player.
     */
    public boolean isUser()
    {
        return true;
    }

    protected void sendHorseJump()
    {
        this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.RIDING_JUMP, (int)(this.getHorseJumpPower() * 100.0F)));
    }

    public void sendHorseInventory()
    {
        this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.OPEN_INVENTORY));
    }

    public void setClientBrand(String brand)
    {
        this.clientBrand = brand;
    }

    public String getClientBrand()
    {
        return this.clientBrand;
    }

    public StatFileWriter getStatFileWriter()
    {
        return this.statWriter;
    }

    public void addChatComponentMessage(IChatComponent chatComponent)
    {
        this.mc.ingameGUI.getChatGUI().printChatMessage(chatComponent);
    }

    protected boolean pushOutOfBlocks(double x, double y, double z)
    {
        if (Modules.PHASE.isEnabled()) {
            return false;
        }
        if (!this.noClip) {
            BlockPos blockpos = new BlockPos(x, y, z);
            double d0 = x - (double) blockpos.getX();
            double d1 = z - (double) blockpos.getZ();

            if (!this.isOpenBlockSpace(blockpos)) {
                int i = -1;
                double d2 = 9999.0D;

                if (this.isOpenBlockSpace(blockpos.west()) && d0 < d2) {
                    d2 = d0;
                    i = 0;
                }

                if (this.isOpenBlockSpace(blockpos.east()) && 1.0D - d0 < d2) {
                    d2 = 1.0D - d0;
                    i = 1;
                }

                if (this.isOpenBlockSpace(blockpos.north()) && d1 < d2) {
                    d2 = d1;
                    i = 4;
                }

                if (this.isOpenBlockSpace(blockpos.south()) && 1.0D - d1 < d2) {
                    i = 5;
                }

                float f = 0.1F;

                if (i == 0) {
                    this.motionX = (-f);
                }

                if (i == 1) {
                    this.motionX = f;
                }

                if (i == 4) {
                    this.motionZ = (-f);
                }

                if (i == 5) {
                    this.motionZ = f;
                }
            }

        }
        return false;
    }

    /**
     * Returns true if the block at the given BlockPos and the block above it are NOT full cubes.
     */
    private boolean isOpenBlockSpace(BlockPos pos)
    {
        return !this.worldObj.getBlockState(pos).getBlock().isNormalCube() && !this.worldObj.getBlockState(pos.up()).getBlock().isNormalCube();
    }

    /**
     * Set sprinting switch for Entity.
     */
    public void setSprinting(boolean sprinting)
    {
        super.setSprinting(sprinting);
        this.sprintingTicksLeft = sprinting ? 600 : 0;
    }

    /**
     * Sets the current XP, total XP, and level number.
     */
    public void setXPStats(float currentXP, int maxXP, int level)
    {
        this.experience = currentXP;
        this.experienceTotal = maxXP;
        this.experienceLevel = level;
    }

    /**
     * Send a chat message to the CommandSender
     */
    public void addChatMessage(IChatComponent component)
    {
        this.mc.ingameGUI.getChatGUI().printChatMessage(component);
    }

    /**
     * Returns {@code true} if the CommandSender is allowed to execute the command, {@code false} if not
     */
    public boolean canCommandSenderUseCommand(int permLevel, String commandName)
    {
        return permLevel <= 0;
    }

    /**
     * Get the position in the world. <b>{@code null} is not allowed!</b> If you are not an entity in the world, return
     * the coordinates 0, 0, 0
     */
    public BlockPos getPosition()
    {
        return new BlockPos(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D);
    }
    public BlockPos getPosition2()
    {
        return new BlockPos(this.posX, this.posY, this.posZ);
    }

    public void playSound(String name, float volume, float pitch) {
        this.worldObj.playSound(this.posX, this.posY, this.posZ, name, volume, pitch, false);
    }

    /**
     * Returns whether the entity is in a server world
     */
    public boolean isServerWorld()
    {
        return true;
    }

    public boolean isRidingHorse()
    {
        return this.ridingEntity != null && this.ridingEntity instanceof EntityHorse && ((EntityHorse)this.ridingEntity).isHorseSaddled();
    }

    public float getHorseJumpPower()
    {
        return this.horseJumpPower;
    }

    public void openEditSign(TileEntitySign signTile)
    {
        this.mc.displayGuiScreen(new GuiEditSign(signTile));
    }

    public void openEditCommandBlock(CommandBlockLogic cmdBlockLogic)
    {
        this.mc.displayGuiScreen(new GuiCommandBlock(cmdBlockLogic));
    }

    /**
     * Displays the GUI for interacting with a book.
     */
    public void displayGUIBook(ItemStack bookStack)
    {
        Item item = bookStack.getItem();

        if (item == Items.writable_book)
        {
            this.mc.displayGuiScreen(new GuiScreenBook(this, bookStack, true));
        }
    }

    /**
     * Displays the GUI for interacting with a chest inventory. Args: chestInventory
     */
    public void displayGUIChest(IInventory chestInventory)
    {
        String s = chestInventory instanceof IInteractionObject ? ((IInteractionObject)chestInventory).getGuiID() : "minecraft:container";

        if ("minecraft:chest".equals(s))
        {
            this.mc.displayGuiScreen(new GuiChest(this.inventory, chestInventory));
        }
        else if ("minecraft:hopper".equals(s))
        {
            this.mc.displayGuiScreen(new GuiHopper(this.inventory, chestInventory));
        }
        else if ("minecraft:furnace".equals(s))
        {
            this.mc.displayGuiScreen(new GuiFurnace(this.inventory, chestInventory));
        }
        else if ("minecraft:brewing_stand".equals(s))
        {
            this.mc.displayGuiScreen(new GuiBrewingStand(this.inventory, chestInventory));
        }
        else if ("minecraft:beacon".equals(s))
        {
            this.mc.displayGuiScreen(new GuiBeacon(this.inventory, chestInventory));
        }
        else if (!"minecraft:dispenser".equals(s) && !"minecraft:dropper".equals(s))
        {
            this.mc.displayGuiScreen(new GuiChest(this.inventory, chestInventory));
        }
        else
        {
            this.mc.displayGuiScreen(new GuiDispenser(this.inventory, chestInventory));
        }
    }

    public void displayGUIHorse(EntityHorse horse, IInventory horseInventory)
    {
        this.mc.displayGuiScreen(new GuiScreenHorseInventory(this.inventory, horseInventory, horse));
    }

    public void displayGui(IInteractionObject guiOwner)
    {
        String s = guiOwner.getGuiID();

        if ("minecraft:crafting_table".equals(s))
        {
            this.mc.displayGuiScreen(new GuiCrafting(this.inventory, this.worldObj));
        }
        else if ("minecraft:enchanting_table".equals(s))
        {
            this.mc.displayGuiScreen(new GuiEnchantment(this.inventory, this.worldObj, guiOwner));
        }
        else if ("minecraft:anvil".equals(s))
        {
            this.mc.displayGuiScreen(new GuiRepair(this.inventory, this.worldObj));
        }
    }

    public void displayVillagerTradeGui(IMerchant villager)
    {
        this.mc.displayGuiScreen(new GuiMerchant(this.inventory, villager, this.worldObj));
    }

    /**
     * Called when the player performs a critical hit on the Entity. Args: entity that was hit critically
     */
    public void onCriticalHit(Entity entityHit)
    {
        this.mc.effectRenderer.emitParticleAtEntity(entityHit, EnumParticleTypes.CRIT);
    }

    public void onEnchantmentCritical(Entity entityHit)
    {
        this.mc.effectRenderer.emitParticleAtEntity(entityHit, EnumParticleTypes.CRIT_MAGIC);
    }

    /**
     * Returns if this entity is sneaking.
     */
    public boolean isSneaking()
    {
        if (movementInput == null) {
            return false;
        }
        EventMoveInput pre = new EventMoveInput(this.movementInput.sneak);
        boolean flag = pre.isSneaking();
        return flag && !this.sleeping;
    }

    public void updateEntityActionState()
    {
        super.updateEntityActionState();

        if (this.isCurrentViewEntity())
        {
            EventMoveInput pre = new EventMoveInput(this.movementInput.moveForward,this.movementInput.moveStrafe,this.movementInput.jump);
            pre.fire();
            this.prevForward = this.moveForward;
            this.prevStrafe = this.moveStrafing;
            this.moveForward = pre.getForward();
            this.moveStrafing = pre.getStrafe();
            this.isJumping = pre.isJumping();
            this.prevRenderArmYaw = this.renderArmYaw;
            this.prevRenderArmPitch = this.renderArmPitch;
            this.renderArmPitch = (float)(this.renderArmPitch + (this.rotationPitch - this.renderArmPitch) * 0.5D);
            this.renderArmYaw = (float)(this.renderArmYaw + (this.rotationYaw - this.renderArmYaw) * 0.5D);
        }
    }

    public PlayerUtils.Face8Direction[] getFaces() {
        return faces;
    }

    protected boolean isCurrentViewEntity()
    {
        return this.mc.getRenderViewEntity() == this;
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */

    public void onLivingUpdate()
    {
        if (this.sprintingTicksLeft > 0)
        {
            --this.sprintingTicksLeft;

            if (this.sprintingTicksLeft == 0)
            {
                this.setSprinting(false);
            }
        }

        if (this.sprintToggleTimer > 0)
        {
            --this.sprintToggleTimer;
        }

        this.prevTimeInPortal = this.timeInPortal;

        if (this.inPortal)
        {
            if (this.mc.currentScreen != null && !this.mc.currentScreen.doesGuiPauseGame())
            {
                this.mc.displayGuiScreen(null);
            }

            if (this.timeInPortal == 0.0F)
            {
                this.mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("portal.trigger"), this.rand.nextFloat() * 0.4F + 0.8F));
            }

            this.timeInPortal += 0.0125F;

            if (this.timeInPortal >= 1.0F)
            {
                this.timeInPortal = 1.0F;
            }

            this.inPortal = false;
        }
        else if (this.isPotionActive(Potion.confusion) && this.getActivePotionEffect(Potion.confusion).getDuration() > 60)
        {
            this.timeInPortal += 0.006666667F;

            if (this.timeInPortal > 1.0F)
            {
                this.timeInPortal = 1.0F;
            }
        }
        else
        {
            if (this.timeInPortal > 0.0F)
            {
                this.timeInPortal -= 0.05F;
            }

            if (this.timeInPortal < 0.0F)
            {
                this.timeInPortal = 0.0F;
            }
        }

        if (this.timeUntilPortal > 0)
        {
            --this.timeUntilPortal;
        }

        if (movementInput == null) {
            return;
        }
        boolean flag = this.movementInput.jump;
        boolean flag1 = this.movementInput.sneak;
        float f = 0.8F;
        boolean flag2 = this.movementInput.moveForward >= f;
        this.movementInput.updatePlayerMoveState();

        if (this.isUsingItem() && !this.isRiding() && !Modules.NO_SLOW_DOWN.isEnabled())
        {
            this.movementInput.moveStrafe *= 0.2F;
            this.movementInput.moveForward *= 0.2F;
            this.sprintToggleTimer = 0;
        }

        this.pushOutOfBlocks(this.posX - (double)this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ + (double)this.width * 0.35D);
        this.pushOutOfBlocks(this.posX - (double)this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ - (double)this.width * 0.35D);
        this.pushOutOfBlocks(this.posX + (double)this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ - (double)this.width * 0.35D);
        this.pushOutOfBlocks(this.posX + (double)this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ + (double)this.width * 0.35D);
        boolean flag3 = (float)this.getFoodStats().getFoodLevel() > 6.0F || this.capabilities.allowFlying;
        if (this.onGround && !flag1 && !flag2 && this.movementInput.moveForward >= f && !this.isSprinting() && flag3 && !this.isUsingItem() && !this.isPotionActive(Potion.blindness))
        {
            if (this.sprintToggleTimer <= 0 && !BaritoneHelper.isSprintKeyDown())
            {

                this.sprintToggleTimer = 7;
            }
            else
            {
                this.setSprinting(true);
            }
        }

        if (!this.isSprinting() && this.movementInput.moveForward >= f && flag3 && !this.isUsingItem() && !this.isPotionActive(Potion.blindness) && (BaritoneHelper.isSprintKeyDown() || (Modules.SPRINT.isEnabled() && !Modules.SPRINT.blatant.getValue())))
        {
            this.setSprinting(true);
        }

        if (this.isSprinting() && (this.movementInput.moveForward < f || this.isCollidedHorizontally || !flag3 || stopSprint-- > 0))
        {
            this.setSprinting(false);
        }
        IBaritone baritone = BaritoneAPI.getProvider().getBaritoneForPlayer((EntityPlayerSP) (Object) this);
        boolean allowFlying = baritone == null ? capabilities.allowFlying : !baritone.getPathingBehavior().isPathing() && capabilities.allowFlying;

        if (allowFlying)
        {
            if (this.mc.playerController.isSpectatorMode())
            {
                if (!this.capabilities.isFlying)
                {
                    this.capabilities.isFlying = true;
                    this.sendPlayerAbilities();
                }
            }
            else if (!flag && this.movementInput.jump)
            {
                if (this.flyToggleTimer == 0)
                {
                    this.flyToggleTimer = 7;
                }
                else
                {
                    this.capabilities.isFlying = !this.capabilities.isFlying;
                    this.sendPlayerAbilities();
                    this.flyToggleTimer = 0;
                }
            }
        }

        if (this.capabilities.isFlying && this.isCurrentViewEntity())
        {
            if (this.movementInput.sneak)
            {
                this.motionY -= (this.capabilities.getFlySpeed() * 3.0F);
            }

            if (this.movementInput.jump)
            {
                this.motionY += (this.capabilities.getFlySpeed() * 3.0F);
            }
        }

        if (this.isRidingHorse())
        {
            if (this.horseJumpPowerCounter < 0)
            {
                ++this.horseJumpPowerCounter;

                if (this.horseJumpPowerCounter == 0)
                {
                    this.horseJumpPower = 0.0F;
                }
            }

            if (flag && !this.movementInput.jump)
            {
                this.horseJumpPowerCounter = -10;
                this.sendHorseJump();
            }
            else if (!flag && this.movementInput.jump)
            {
                this.horseJumpPowerCounter = 0;
                this.horseJumpPower = 0.0F;
            }
            else if (flag)
            {
                ++this.horseJumpPowerCounter;

                if (this.horseJumpPowerCounter < 10)
                {
                    this.horseJumpPower = (float)this.horseJumpPowerCounter * 0.1F;
                }
                else
                {
                    this.horseJumpPower = 0.8F + 2.0F / (float)(this.horseJumpPowerCounter - 9) * 0.1F;
                }
            }
        }
        else
        {
            this.horseJumpPower = 0.0F;
        }

        super.onLivingUpdate();

        if (this.onGround && this.capabilities.isFlying && !this.mc.playerController.isSpectatorMode())
        {
            this.capabilities.isFlying = false;
            this.sendPlayerAbilities();
        }
    }

    @Override
    public void updateRidden() {
        IBaritone baritone = BaritoneAPI.getProvider().getBaritoneForPlayer((EntityPlayerSP) (Object) this);
        if (baritone != null) {
            ((LookBehavior) baritone.getLookBehavior()).pig();
        }
        super.updateRidden();
    }

    public float getDirection() {
        return getDirection(BaritoneHelper.lastBaritoneYaw());
    }

    public float getDirection(float yaw) {
        float forward = this.moveForward;
        float strafe = this.moveStrafing;

        yaw += (forward < 0.0F ? 180 : 0);
        float v = forward == 0.0F ? 90.0F : forward < 0.0F ? -45.0F : 45.0F;
        if (strafe < 0.0F) {
            yaw += v;
        }
        if (strafe > 0.0F) {
            yaw -= v;
        }
        return yaw * 0.017453292F;
    }

    public void expandPos(double x, double y, double z) {
        setPosition(posX+x,posY+y,posZ+z);
    }
}
