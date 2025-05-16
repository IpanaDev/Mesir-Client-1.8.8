package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.List;
import net.minecraft.entity.DataWatcher;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S1CPacketEntityMetadata implements Packet<INetHandlerPlayClient>
{
    private int entityId;
    private List<DataWatcher.WatchableObject> watchableObjects;

    public S1CPacketEntityMetadata()
    {
    }

    public S1CPacketEntityMetadata(int entityIdIn, DataWatcher dataWatcher, boolean all)
    {
        this.entityId = entityIdIn;

        if (all)
        {
            this.watchableObjects = dataWatcher.getAllWatched();
        }
        else
        {
            this.watchableObjects = dataWatcher.getChanged();
        }
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.entityId = buf.readVarIntFromBuffer();
        this.watchableObjects = DataWatcher.readWatchedListFromPacketBuffer(buf);
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(this.entityId);
        DataWatcher.writeWatchedListToPacketBuffer(this.watchableObjects, buf);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleEntityMetadata(this);
    }

    public List<DataWatcher.WatchableObject> watchableObjects()
    {
        return this.watchableObjects;
    }

    public int getEntityId()
    {
        return this.entityId;
    }
}
