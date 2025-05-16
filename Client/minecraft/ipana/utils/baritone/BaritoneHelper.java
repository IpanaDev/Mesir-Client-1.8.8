package ipana.utils.baritone;

import baritone.Baritone;
import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.event.events.RotationMoveEvent;
import baritone.api.event.events.SprintStateEvent;
import baritone.api.utils.IPlayerContext;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCache;

public class BaritoneHelper {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static float lastBaritoneYaw;

    public static boolean isSprintKeyDown() {
        IBaritone baritone = BaritoneAPI.getProvider().getBaritoneForPlayer(mc.thePlayer);
        if (baritone == null) {
            return mc.gameSettings.keyBindSprint.isKeyDown();
        }
        SprintStateEvent event = new SprintStateEvent();
        baritone.getGameEventHandler().onPlayerSprintState(event);
        if (event.getState() != null) {
            return event.getState();
        }
        if (baritone != BaritoneAPI.getProvider().getPrimaryBaritone()) {
            // hitting control shouldn't make all bots sprint
            return false;
        }
        return mc.gameSettings.keyBindSprint.isKeyDown();
    }

    public static boolean isAllowUserInput(GuiScreen screen) {
        // allow user input is only the primary baritone
        return (BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing() && mc.thePlayer != null) || screen.allowUserInput;
    }

    public static void travel(EntityLivingBase self, float strafe, float forward, float friction) {
        if (!(self instanceof EntityPlayerSP) || BaritoneAPI.getProvider().getBaritoneForPlayer((EntityPlayerSP) self) == null) {
            self.moveFlying(strafe, forward, friction);
            return;
        }
        RotationMoveEvent motionUpdateRotationEvent = new RotationMoveEvent(RotationMoveEvent.Type.MOTION_UPDATE, self.rotationYaw);
        BaritoneAPI.getProvider().getBaritoneForPlayer((EntityPlayerSP) self).getGameEventHandler().onPlayerRotationMove(motionUpdateRotationEvent);
        float originalYaw = self.rotationYaw;
        self.rotationYaw = motionUpdateRotationEvent.getYaw();
        lastBaritoneYaw = motionUpdateRotationEvent.getYaw();
        self.moveFlying(strafe, forward, friction);
        self.rotationYaw = originalYaw;
    }

    public static boolean isEmpty(RenderChunk renderChunk, BlockPos blockPos) {
        int i = blockPos.getY();
        int j = i + 15;
        if (renderChunk.getChunk(blockPos).getAreLevelsEmpty(i, j)) {
            return true;
        }
        if (Baritone.settings().renderCachedChunks.value && !Minecraft.getMinecraft().isSingleplayer()) {
            Baritone baritone = (Baritone) BaritoneAPI.getProvider().getPrimaryBaritone();
            IPlayerContext ctx = baritone.getPlayerContext();
            if (ctx.player() != null && ctx.world() != null && baritone.bsi != null) {
                BlockPos position = renderChunk.getPosition();
                // RenderChunk extends from -1,-1,-1 to +16,+16,+16
                // then the constructor of ChunkCache extends it one more (presumably to get things like the connected status of fences? idk)
                // so if ANY of the adjacent chunks are loaded, we are unempty
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (baritone.bsi.isLoaded(16 * dx + position.getX(), 16 * dz + position.getZ())) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public static IBlockState getBlockState(ChunkCache chunkCache, BlockPos pos) {
        if (Baritone.settings().renderCachedChunks.value && !Minecraft.getMinecraft().isSingleplayer()) {
            Baritone baritone = (Baritone) BaritoneAPI.getProvider().getPrimaryBaritone();
            IPlayerContext ctx = baritone.getPlayerContext();
            if (ctx.player() != null && ctx.world() != null && baritone.bsi != null) {
                return baritone.bsi.get0(pos);
            }
        }

        return chunkCache.getBlockState(pos);
    }

    public static float lastBaritoneYaw() {
        return lastBaritoneYaw;
    }
}
