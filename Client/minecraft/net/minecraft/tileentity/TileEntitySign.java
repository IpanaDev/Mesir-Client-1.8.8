package net.minecraft.tileentity;

import com.google.gson.JsonParseException;
import ipana.utils.gl.GList;
import ipana.utils.player.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySignRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S33PacketUpdateSign;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.lang.reflect.Array;
import java.util.Arrays;

public class TileEntitySign extends TileEntity {
    public final IChatComponent[] signText = new IChatComponent[] {new ChatComponentText(""), new ChatComponentText(""), new ChatComponentText(""), new ChatComponentText("")};

    /**
     * The index of the line currently being edited. Only used on client side, but defined on both. Note this is only
     * really used when the > < are going to be visible.
     */
    public int lineBeingEdited = -1;
    private boolean isEditable = true;
    private EntityPlayer player;
    private final CommandResultStats stats = new CommandResultStats();
    public GList<IChatComponent[]> signTexts = new GList<>();
    public boolean needsUpdate;
    public Block hangedBlock;
    public EnumFacing facing;

    public TileEntitySign() {
        setRenderer(TileEntityRendererDispatcher.instance.tileEntitySignRenderer);
    }


    @Override
    public void onBlockTypeChanged(BlockPos pos) {
        if (getBlockType() != Blocks.standing_sign) {
            switch (getBlockMetadata()) {
                case 3 -> hangedBlock = this.worldObj.getBlockState(this.pos.add(0,0,-1)).getBlock();
                case 4 -> hangedBlock = this.worldObj.getBlockState(this.pos.add(1,0,0)).getBlock();
                case 2 -> hangedBlock = this.worldObj.getBlockState(this.pos.add(0,0,1)).getBlock();
                case 5 -> hangedBlock = this.worldObj.getBlockState(this.pos.add(-1,0,0)).getBlock();
            }
        }
    }

    @Override
    public void onBlockMetadataChanged() {
        if (getBlockType() == Blocks.standing_sign) {
            switch (getBlockMetadata()) {
                case 0 -> facing = EnumFacing.SOUTH;
                case 4 -> facing = EnumFacing.WEST;
                case 8 -> facing = EnumFacing.NORTH;
                case 12 -> facing = EnumFacing.EAST;
            }
        } else {
            switch (getBlockMetadata()) {
                case 3 -> facing = EnumFacing.SOUTH;
                case 4 -> facing = EnumFacing.WEST;
                case 2 -> facing = EnumFacing.NORTH;
                case 5 -> facing = EnumFacing.EAST;
            }
        }
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        for (int i = 0; i < 4; ++i)
        {
            String s = IChatComponent.Serializer.componentToJson(this.signText[i]);
            compound.setString("Text" + (i + 1), s);
        }

        this.stats.writeStatsToNBT(compound);
    }

    public void readFromNBT(NBTTagCompound compound) {
        this.isEditable = false;
        super.readFromNBT(compound);
        ICommandSender icommandsender = new ICommandSender() {
            public String getName() {
                return "Sign";
            }

            public IChatComponent getDisplayName() {
                return new ChatComponentText(this.getName());
            }

            public void addChatMessage(IChatComponent component) {
            }

            public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
                return true;
            }

            public BlockPos getPosition() {
                return TileEntitySign.this.pos;
            }

            public Vec3 getPositionVector() {
                return new Vec3((double) TileEntitySign.this.pos.getX() + 0.5D, (double) TileEntitySign.this.pos.getY() + 0.5D, (double) TileEntitySign.this.pos.getZ() + 0.5D);
            }

            public World getEntityWorld() {
                return TileEntitySign.this.worldObj;
            }

            public Entity getCommandSenderEntity() {
                return null;
            }

            public boolean sendCommandFeedback() {
                return false;
            }

            public void setCommandStat(CommandResultStats.Type type, int amount) {
            }
        };

        for (int i = 0; i < 4; ++i) {
            String s = compound.getString("Text" + (i + 1));

            try {
                IChatComponent ichatcomponent = IChatComponent.Serializer.jsonToComponent(s);

                try {
                    this.signText[i] = ChatComponentProcessor.processComponent(icommandsender, ichatcomponent, null);
                    this.needsUpdate = true;
                } catch (CommandException var7) {
                    this.signText[i] = ichatcomponent;
                    this.needsUpdate = true;
                }
            } catch (JsonParseException var8) {
                this.signText[i] = new ChatComponentText(s);
                this.needsUpdate = true;
            }
        }

        this.stats.readStatsFromNBT(compound);
    }

    /**
     * Allows for a specialized description packet to be created. This is often used to sync tile entity data from the
     * server to the client easily. For example this is used by signs to synchronise the text to be displayed.
     */
    public S33PacketUpdateSign getDescriptionPacket()
    {
        IChatComponent[] aichatcomponent = new IChatComponent[4];
        System.arraycopy(this.signText, 0, aichatcomponent, 0, 4);
        return new S33PacketUpdateSign(this.worldObj, this.pos, aichatcomponent);
    }

    public boolean func_183000_F()
    {
        return true;
    }

    public boolean getIsEditable()
    {
        return this.isEditable;
    }

    /**
     * Sets the sign's isEditable flag to the specified parameter.
     */
    public void setEditable(boolean isEditableIn)
    {
        this.isEditable = isEditableIn;

        if (!isEditableIn)
        {
            this.player = null;
        }
    }

    public void setPlayer(EntityPlayer playerIn)
    {
        this.player = playerIn;
    }

    public EntityPlayer getPlayer()
    {
        return this.player;
    }

    public boolean executeCommand(final EntityPlayer playerIn)
    {
        ICommandSender icommandsender = new ICommandSender()
        {
            public String getName()
            {
                return playerIn.getName();
            }
            public IChatComponent getDisplayName()
            {
                return playerIn.getDisplayName();
            }
            public void addChatMessage(IChatComponent component)
            {
            }
            public boolean canCommandSenderUseCommand(int permLevel, String commandName)
            {
                return permLevel <= 2;
            }
            public BlockPos getPosition()
            {
                return TileEntitySign.this.pos;
            }
            public Vec3 getPositionVector()
            {
                return new Vec3((double)TileEntitySign.this.pos.getX() + 0.5D, (double)TileEntitySign.this.pos.getY() + 0.5D, (double)TileEntitySign.this.pos.getZ() + 0.5D);
            }
            public World getEntityWorld()
            {
                return playerIn.getEntityWorld();
            }
            public Entity getCommandSenderEntity()
            {
                return playerIn;
            }
            public boolean sendCommandFeedback()
            {
                return false;
            }
            public void setCommandStat(CommandResultStats.Type type, int amount)
            {
                TileEntitySign.this.stats.func_179672_a(this, type, amount);
            }
        };

        for (IChatComponent iChatComponent : this.signText) {
            ChatStyle chatstyle = iChatComponent == null ? null : iChatComponent.getChatStyle();

            if (chatstyle != null && chatstyle.getChatClickEvent() != null) {
                ClickEvent clickevent = chatstyle.getChatClickEvent();

                if (clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                    MinecraftServer.getServer().getCommandManager().executeCommand(icommandsender, clickevent.getValue());
                }
            }
        }

        return true;
    }

    public CommandResultStats getStats()
    {
        return this.stats;
    }

}
