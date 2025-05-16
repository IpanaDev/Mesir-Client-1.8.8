package net.minecraft.client.multiplayer;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.event.events.ChunkEvent;
import baritone.api.event.events.type.EventState;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import ipana.utils.player.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSoundMinecart;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.EntityFirework;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveDataMemoryStorage;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import optifine.*;

import java.util.*;

public class WorldClient extends World
{
    /** The packets that need to be sent to the server. */
    public NetHandlerPlayClient sendQueue;

    /** The ChunkProviderClient instance */
    private ChunkProviderClient clientChunkProvider;

    /** Contains all entities for this client, both spawned and non-spawned. */
    private final Set<Entity> entityList = Sets.newHashSet();

    /**
     * Contains all entities for this client that were not spawned due to a non-present chunk. The game will attempt to
     * spawn up to 10 pending entities with each subsequent tick until the spawn queue is empty.
     */
    private final Set<Entity> entitySpawnQueue = Sets.newHashSet();
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Set<ChunkCoordIntPair> previousActiveChunkSet = Sets.newHashSet();

    private BlockPosM randomTickPosM = new BlockPosM(0, 0, 0, 3);
    private boolean playerUpdate = false;
    public WorldSettings settings;
    public int dimension;
    public Profiler profiler;

    public WorldClient(NetHandlerPlayClient p_i45063_1_, WorldSettings p_i45063_2_, int p_i45063_3_, EnumDifficulty p_i45063_4_, Profiler p_i45063_5_)
    {
        super(new SaveHandlerMP(), new WorldInfo(p_i45063_2_, "MpServer"), Objects.requireNonNull(WorldProvider.getProviderForDimension(p_i45063_3_)), p_i45063_5_, true);
        this.sendQueue = p_i45063_1_;
        settings = p_i45063_2_;
        dimension = p_i45063_3_;
        profiler = p_i45063_5_;
        this.getWorldInfo().setDifficulty(p_i45063_4_);
        this.provider.registerWorld(this);
        this.setSpawnPoint(new BlockPos(8, 64, 8));
        this.chunkProvider = this.createChunkProvider();
        this.mapStorage = new SaveDataMemoryStorage();
        this.calculateInitialSkylight();
        this.calculateInitialWeather();

        if (this.mc.playerController != null && this.mc.playerController.getClass() == PlayerControllerMP.class)
        {
            this.mc.playerController = new PlayerControllerOF(this.mc, p_i45063_1_);
        }
    }

    /**
     * Runs a single tick for the world
     */
    public void tick()
    {
        super.tick();
        this.setTotalWorldTime(this.getTotalWorldTime() + 1L);

        if (this.getGameRules().getBoolean("doDaylightCycle"))
        {
            this.setWorldTime(this.getWorldTime() + 1L);
        }

        this.theProfiler.startSection("reEntryProcessing");

        for (int i = 0; i < 10 && !this.entitySpawnQueue.isEmpty(); ++i)
        {
            Entity entity = this.entitySpawnQueue.iterator().next();
            this.entitySpawnQueue.remove(entity);

            if (!this.loadedEntityList.contains(entity))
            {
                this.spawnEntityInWorld(entity);
            }
        }

        this.theProfiler.endStartSection("chunkCache");
        this.clientChunkProvider.unloadQueuedChunks();
        this.theProfiler.endStartSection("blocks");
        this.updateBlocks();
        this.theProfiler.endSection();
    }

    /**
     * Creates the chunk provider for this world. Called in the constructor. Retrieves provider from worldProvider?
     */
    protected IChunkProvider createChunkProvider()
    {
        this.clientChunkProvider = new ChunkProviderClient(this);
        return this.clientChunkProvider;
    }

    protected void updateBlocks()
    {
        super.updateBlocks();
        this.previousActiveChunkSet.retainAll(this.activeChunkSet);

        if (this.previousActiveChunkSet.size() == this.activeChunkSet.size())
        {
            this.previousActiveChunkSet.clear();
        }

        int i = 0;

        for (ChunkCoordIntPair chunkcoordintpair : this.activeChunkSet)
        {
            if (!this.previousActiveChunkSet.contains(chunkcoordintpair))
            {
                int j = chunkcoordintpair.chunkXPos * 16;
                int k = chunkcoordintpair.chunkZPos * 16;
                this.theProfiler.startSection("getChunk");
                Chunk chunk = this.getChunkFromChunkCoords(chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos);
                this.playMoodSoundAndCheckLight(j, k, chunk);
                this.theProfiler.endSection();
                this.previousActiveChunkSet.add(chunkcoordintpair);
                ++i;

                if (i >= 10)
                {
                    return;
                }
            }
        }
    }

    public void doPreChunk(int chunkX, int chunkZ, boolean loadChunk)
    {
        for (IBaritone ibaritone : BaritoneAPI.getProvider().getAllBaritones()) {
            if (ibaritone.getPlayerContext().world() == this) {
                ibaritone.getGameEventHandler().onChunkEvent(new ChunkEvent(EventState.PRE, loadChunk ? ChunkEvent.Type.LOAD : ChunkEvent.Type.UNLOAD, chunkX, chunkZ));
            }
        }
        if (loadChunk)
        {
            this.clientChunkProvider.loadChunk(chunkX, chunkZ);
        }
        else
        {
            this.clientChunkProvider.unloadChunk(chunkX, chunkZ);
        }

        if (!loadChunk)
        {
            this.markBlockRangeForRenderUpdate(chunkX * 16, 0, chunkZ * 16, chunkX * 16 + 15, 256, chunkZ * 16 + 15);
        }
        for (IBaritone ibaritone : BaritoneAPI.getProvider().getAllBaritones()) {
            if (ibaritone.getPlayerContext().world() == this) {
                ibaritone.getGameEventHandler().onChunkEvent(new ChunkEvent(EventState.POST, loadChunk ? ChunkEvent.Type.LOAD : ChunkEvent.Type.UNLOAD, chunkX, chunkZ));
            }
        }
    }

    /**
     * Called when an entity is spawned in the world. This includes players.
     */
    public boolean spawnEntityInWorld(Entity entityIn)
    {
        boolean flag = super.spawnEntityInWorld(entityIn);
        this.entityList.add(entityIn);

        if (!flag)
        {
            this.entitySpawnQueue.add(entityIn);
        }
        else if (entityIn instanceof EntityMinecart)
        {
            this.mc.getSoundHandler().playSound(new MovingSoundMinecart((EntityMinecart)entityIn));
        }

        return flag;
    }

    /**
     * Schedule the entity for removal during the next tick. Marks the entity dead in anticipation.
     */
    public void removeEntity(Entity entityIn)
    {
        super.removeEntity(entityIn);

        this.entityList.remove(entityIn);
    }

    protected void onEntityAdded(Entity entityIn)
    {
        super.onEntityAdded(entityIn);

        this.entitySpawnQueue.remove(entityIn);
    }

    protected void onEntityRemoved(Entity entityIn)
    {
        super.onEntityRemoved(entityIn);
        if (entityIn instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityIn;
            //playerEntities.remove(player);
        }
        if (this.entityList.contains(entityIn))
        {
            if (entityIn.isEntityAlive())
            {
                this.entitySpawnQueue.add(entityIn);
            }
            else
            {
                this.entityList.remove(entityIn);
            }
        }
    }

    /**
     * Add an ID to Entity mapping to entityHashSet
     */
    public void addEntityToWorld(int p_73027_1_, Entity p_73027_2_)
    {
        Entity entity = this.getEntityByID(p_73027_1_);

        if (entity != null)
        {
            this.removeEntity(entity);
        }

        this.entityList.add(p_73027_2_);
        p_73027_2_.setEntityId(p_73027_1_);

        if (!this.spawnEntityInWorld(p_73027_2_))
        {
            this.entitySpawnQueue.add(p_73027_2_);
        }

        this.entitiesById.addKey(p_73027_1_, p_73027_2_);
    }

    /**
     * Returns the Entity with the given ID, or null if it doesn't exist in this World.
     */
    public Entity getEntityByID(int id)
    {
        return (id == this.mc.thePlayer.getEntityId() ? this.mc.thePlayer : super.getEntityByID(id));
    }

    public void removeEntityFromWorld(int p_73028_1_)
    {
        Entity entity = (Entity)this.entitiesById.removeObject(p_73028_1_);

        if (entity != null)
        {
            if (entity instanceof EntityItemFrame) {
                this.markBlockForUpdate(entity.getPosition());
            }
            this.entityList.remove(entity);
            this.removeEntity(entity);
        }

    }

    public void invalidateRegionAndSetBlock(BlockPos p_180503_1_, IBlockState p_180503_2_)
    {
        super.setBlockState(p_180503_1_, p_180503_2_, 3);
    }

    /**
     * If on MP, sends a quitting packet.
     */
    public void sendQuittingDisconnectingPacket()
    {
        this.sendQueue.getNetworkManager().closeChannel(new ChatComponentText("Quitting"));
    }

    /**
     * Updates all weather states.
     */
    protected void updateWeather()
    {
    }

    protected int getRenderDistanceChunks()
    {
        return this.mc.gameSettings.renderDistanceChunks;
    }

    public void doVoidFogParticles(int p_73029_1_, int p_73029_2_, int p_73029_3_)
    {
        byte b0 = 16;
        Random random = new Random();
        ItemStack itemstack = this.mc.thePlayer.getHeldItem();
        boolean flag = this.mc.playerController.getCurrentGameType() == WorldSettings.GameType.CREATIVE && itemstack != null && Block.getBlockFromItem(itemstack.getItem()) == Blocks.barrier;
        BlockPosM blockposm = this.randomTickPosM;

        for (int i = 0; i < 1000; ++i)
        {
            int j = p_73029_1_ + this.rand.nextInt(b0) - this.rand.nextInt(b0);
            int k = p_73029_2_ + this.rand.nextInt(b0) - this.rand.nextInt(b0);
            int l = p_73029_3_ + this.rand.nextInt(b0) - this.rand.nextInt(b0);
            blockposm.setXyz(j, k, l);
            IBlockState iblockstate = this.getBlockState(blockposm);
            iblockstate.getBlock().randomDisplayTick(this, blockposm, iblockstate, random);

            if (flag && iblockstate.getBlock() == Blocks.barrier)
            {
                this.spawnParticle(EnumParticleTypes.BARRIER, ((float)j + 0.5F), ((float)k + 0.5F), ((float)l + 0.5F), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    /**
     * also releases skins.
     */
    public void removeAllEntities()
    {
        for (Entity ent : unloadedEntityList) {
            ent.deleteGLs();
            this.loadedEntityList.remove(ent);
            if (ent instanceof EntityPlayer) {
                ((EntityPlayer) ent).nameTagsList.deleteLists();
                this.playerEntities.remove(ent);
            }
            if (ent instanceof EntityLivingBase) {
                this.livingEntities.remove(ent);
            }
        }
        for (Entity entity : this.unloadedEntityList) {
            int j = entity.chunkCoordX;
            int k = entity.chunkCoordZ;

            if (entity.addedToChunk && this.isChunkLoaded(j, k, true)) {
                this.getChunkFromChunkCoords(j, k).removeEntity(entity);
            }
        }
        for (Entity entity : this.unloadedEntityList) {
            this.onEntityRemoved(entity);
        }

        this.unloadedEntityList.clear();

        for (int i1 = 0; i1 < this.loadedEntityList.size(); ++i1)
        {
            Entity entity1 = this.loadedEntityList.get(i1);

            if (entity1.ridingEntity != null)
            {
                if (!entity1.ridingEntity.isDead && entity1.ridingEntity.riddenByEntity == entity1)
                {
                    continue;
                }

                entity1.ridingEntity.riddenByEntity = null;
                entity1.ridingEntity = null;
            }

            if (entity1.isDead)
            {

                int j1 = entity1.chunkCoordX;
                int k1 = entity1.chunkCoordZ;

                if (entity1.addedToChunk && this.isChunkLoaded(j1, k1, true)) {
                    this.getChunkFromChunkCoords(j1, k1).removeEntity(entity1);
                }
                entity1.deleteGLs();
                this.loadedEntityList.remove(i1--);
                this.onEntityRemoved(entity1);
            }
        }
    }

    /**
     * Adds some basic stats of the world to the given crash report.
     */
    public CrashReportCategory addWorldInfoToCrashReport(CrashReport report)
    {
        CrashReportCategory crashreportcategory = super.addWorldInfoToCrashReport(report);
        crashreportcategory.addCrashSectionCallable("Forced entities", () -> WorldClient.this.entityList.size() + " total; " + WorldClient.this.entityList.toString());
        crashreportcategory.addCrashSectionCallable("Retry entities",  () -> WorldClient.this.entitySpawnQueue.size() + " total; " + WorldClient.this.entitySpawnQueue.toString());
        crashreportcategory.addCrashSectionCallable("Server brand",  () -> WorldClient.this.mc.thePlayer.getClientBrand());
        crashreportcategory.addCrashSectionCallable("Server type",  () -> WorldClient.this.mc.getIntegratedServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server");
        return crashreportcategory;
    }

    /**
     * Plays a sound at the specified position.
     */
    public void playSoundAtPos(BlockPos p_175731_1_, String p_175731_2_, float p_175731_3_, float p_175731_4_, boolean p_175731_5_)
    {
        this.playSound((double)p_175731_1_.getX() + 0.5D, (double)p_175731_1_.getY() + 0.5D, (double)p_175731_1_.getZ() + 0.5D, p_175731_2_, p_175731_3_, p_175731_4_, p_175731_5_);
    }

    /**
     * par8 is loudness, all pars passed to minecraftInstance.sndManager.playSound
     */
    public void playSound(double x, double y, double z, String soundName, float volume, float pitch, boolean distanceDelay)
    {
        double d0 = this.mc.getRenderViewEntity().getDistanceSq(x, y, z);
        PositionedSoundRecord positionedsoundrecord = new PositionedSoundRecord(new ResourceLocation(soundName), volume, pitch, (float)x, (float)y, (float)z);

        if (distanceDelay && d0 > 100.0D)
        {
            double d1 = Math.sqrt(d0) / 40.0D;
            this.mc.getSoundHandler().playDelayedSound(positionedsoundrecord, (int)(d1 * 20.0D));
        }
        else
        {
            this.mc.getSoundHandler().playSound(positionedsoundrecord);
        }
    }

    public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ, NBTTagCompound compund)
    {
        this.mc.effectRenderer.addEffect(new EntityFirework.StarterFX(this, x, y, z, motionX, motionY, motionZ, this.mc.effectRenderer, compund));
    }

    public void setWorldScoreboard(Scoreboard p_96443_1_)
    {
        this.worldScoreboard = p_96443_1_;
    }

    /**
     * Sets the world time.
     */
    public void setWorldTime(long time)
    {
        if (time < 0L)
        {
            time = -time;
            this.getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
        }
        else
        {
            this.getGameRules().setOrCreateGameRule("doDaylightCycle", "true");
        }

        super.setWorldTime(time);
    }

    public int getCombinedLight(BlockPos pos, int lightValue)
    {
        int i = super.getCombinedLight(pos, lightValue);

        if (Config.isDynamicLights())
        {
            i = DynamicLights.getCombinedLight(pos, i);
        }

        return i;
    }

    /**
     * Sets the block state at a given location. Flag 1 will cause a block update. Flag 2 will send the change to
     * clients (you almost always want this). Flag 4 prevents the block from being re-rendered, if this is a client
     * world. Flags can be added together.
     */
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags)
    {
        this.playerUpdate = this.isPlayerActing();
        boolean flag = super.setBlockState(pos, newState, flags);
        this.playerUpdate = false;
        return flag;
    }

    private boolean isPlayerActing()
    {
        if (this.mc.playerController instanceof PlayerControllerOF)
        {
            PlayerControllerOF playercontrollerof = (PlayerControllerOF)this.mc.playerController;
            return playercontrollerof.isActing();
        }
        else
        {
            return false;
        }
    }

    public boolean isPlayerUpdate()
    {
        return this.playerUpdate;
    }

    public Block getBlock(int p_147439_1_, int p_147439_2_, int p_147439_3_)
    {
        if ((p_147439_1_ >= -30000000) && (p_147439_3_ >= -30000000) && (p_147439_1_ < 30000000) && (p_147439_3_ < 30000000) && (p_147439_2_ >= 0) && (p_147439_2_ < 256))
        {
            Chunk var4 = null;
            try
            {
                var4 = getChunkFromChunkCoords(p_147439_1_ >> 4, p_147439_3_ >> 4);
                return var4.getBlock0(p_147439_1_ & 0xF, p_147439_2_, p_147439_3_ & 0xF);
            }
            catch (Throwable var6)
            {
                CrashReport var5 = CrashReport.makeCrashReport(var6, "Exception getting block type in world");
                CrashReportCategory var7 = var5.makeCategory("Requested block coordinates");
                var7.addCrashSection("Found chunk", var4 == null);
                throw new ReportedException(var5);
            }
        }
        return Blocks.air;
    }

    public List<AxisAlignedBB> getCollidingBlockBoundingBoxes(EntityPlayerSP thePlayer, AxisAlignedBB offset) {
        ArrayList<AxisAlignedBB> var3 = Lists.newArrayList();
        int var4 = MathHelper.floor_double(offset.minX);
        int var5 = MathHelper.floor_double(offset.maxX + 1.0D);
        int var6 = MathHelper.floor_double(offset.minY);
        int var7 = MathHelper.floor_double(offset.maxY + 1.0D);
        int var8 = MathHelper.floor_double(offset.minZ);
        int var9 = MathHelper.floor_double(offset.maxZ + 1.0D);
        for (int var10 = var4; var10 < var5; var10++) {
            for (int var11 = var8; var11 < var9; var11++) {
                if (isBlockLoaded(new BlockPos(var10, 64, var11))) {
                    for (int var12 = var6 - 1; var12 < var7; var12++) {
                        BlockPos var13 = new BlockPos(var10, var12, var11);
                        boolean var14 = thePlayer.isOutsideBorder();
                        boolean var15 = isInsideBorder(getWorldBorder(), thePlayer);
                        if ((var14) && (var15)) {
                            thePlayer.setOutsideBorder(false);
                        } else if ((!var14) && (!var15)) {
                            thePlayer.setOutsideBorder(true);
                        }
                        IBlockState var16;
                        if ((!getWorldBorder().contains(var13)) && (var15)) {
                            var16 = Blocks.stone.getDefaultState();
                        } else {
                            var16 = getBlockState(var13);
                        }
                        var16.getBlock().addCollisionBoxesToList(this, var13, var16, offset, var3, thePlayer);
                    }
                }
            }
        }
        return var3;
    }
}
