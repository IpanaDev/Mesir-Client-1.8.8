package ipana.bot;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.*;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.io.FileWriter;

public class PlayListener implements INetHandlerPlayClient {

    private Runnable onSuccess;
    private Runnable onFail;
    private NetworkManager manager;

    public PlayListener(NetworkManager manager, Runnable onSuccess, Runnable onFail) {
        this.manager = manager;
        this.onSuccess = onSuccess;
        this.onFail = onFail;
    }

    @Override
    public void handleSpawnObject(S0EPacketSpawnObject packetIn) {

    }

    @Override
    public void handleSpawnExperienceOrb(S11PacketSpawnExperienceOrb packetIn) {

    }

    @Override
    public void handleSpawnGlobalEntity(S2CPacketSpawnGlobalEntity packetIn) {

    }

    @Override
    public void handleSpawnMob(S0FPacketSpawnMob packetIn) {

    }

    @Override
    public void handleScoreboardObjective(S3BPacketScoreboardObjective packetIn) {

    }

    @Override
    public void handleSpawnPainting(S10PacketSpawnPainting packetIn) {

    }

    @Override
    public void handleSpawnPlayer(S0CPacketSpawnPlayer packetIn) {

    }

    @Override
    public void handleAnimation(S0BPacketAnimation packetIn) {

    }

    @Override
    public void handleStatistics(S37PacketStatistics packetIn) {

    }

    @Override
    public void handleBlockBreakAnim(S25PacketBlockBreakAnim packetIn) {

    }

    @Override
    public void handleSignEditorOpen(S36PacketSignEditorOpen packetIn) {

    }

    @Override
    public void handleUpdateTileEntity(S35PacketUpdateTileEntity packetIn) {

    }

    @Override
    public void handleBlockAction(S24PacketBlockAction packetIn) {

    }

    @Override
    public void handleBlockChange(S23PacketBlockChange packetIn) {

    }

    @Override
    public void handleChat(S02PacketChat packetIn) {
        var response = packetIn.getChatComponent().getUnformattedText();
        if (response.equals("Yanlış bir şifre girdiniz. Tekrar denemek için yeniden giriş yapın. ")) {
            onFail.run();
            manager.closeChannel(new ChatComponentText("Quitting"));
        } else if (response.equals("Giriş başarılı! ")) {
            onSuccess.run();
            manager.closeChannel(new ChatComponentText("Quitting"));
        } else {
            System.out.println(response);
        }
    }

    @Override
    public void handleTabComplete(S3APacketTabComplete packetIn) {

    }

    @Override
    public void handleMultiBlockChange(S22PacketMultiBlockChange packetIn) {

    }

    @Override
    public void handleMaps(S34PacketMaps packetIn) {

    }

    @Override
    public void handleConfirmTransaction(S32PacketConfirmTransaction packetIn) {

    }

    @Override
    public void handleCloseWindow(S2EPacketCloseWindow packetIn) {

    }

    @Override
    public void handleWindowItems(S30PacketWindowItems packetIn) {

    }

    @Override
    public void handleOpenWindow(S2DPacketOpenWindow packetIn) {

    }

    @Override
    public void handleWindowProperty(S31PacketWindowProperty packetIn) {

    }

    @Override
    public void handleSetSlot(S2FPacketSetSlot packetIn) {

    }

    @Override
    public void handleCustomPayload(S3FPacketCustomPayload packetIn) {

    }

    @Override
    public void handleDisconnect(S40PacketDisconnect packetIn) {
        System.out.println("Disconnected: "+packetIn.getReason().getUnformattedText());
    }

    @Override
    public void handleUseBed(S0APacketUseBed packetIn) {

    }

    @Override
    public void handleEntityStatus(S19PacketEntityStatus packetIn) {

    }

    @Override
    public void handleEntityAttach(S1BPacketEntityAttach packetIn) {

    }

    @Override
    public void handleExplosion(S27PacketExplosion packetIn) {

    }

    @Override
    public void handleChangeGameState(S2BPacketChangeGameState packetIn) {

    }

    @Override
    public void handleKeepAlive(S00PacketKeepAlive packetIn) {

    }

    @Override
    public void handleChunkData(S21PacketChunkData packetIn) {

    }

    @Override
    public void handleMapChunkBulk(S26PacketMapChunkBulk packetIn) {

    }

    @Override
    public void handleEffect(S28PacketEffect packetIn) {

    }

    @Override
    public void handleJoinGame(S01PacketJoinGame packetIn) {

    }

    @Override
    public void handleEntityMovement(S14PacketEntity packetIn) {

    }

    @Override
    public void handlePlayerPosLook(S08PacketPlayerPosLook packetIn) {

    }

    @Override
    public void handleParticles(S2APacketParticles packetIn) {

    }

    @Override
    public void handlePlayerAbilities(S39PacketPlayerAbilities packetIn) {

    }

    @Override
    public void handlePlayerListItem(S38PacketPlayerListItem packetIn) {

    }

    @Override
    public void handleDestroyEntities(S13PacketDestroyEntities packetIn) {

    }

    @Override
    public void handleRemoveEntityEffect(S1EPacketRemoveEntityEffect packetIn) {

    }

    @Override
    public void handleRespawn(S07PacketRespawn packetIn) {

    }

    @Override
    public void handleEntityHeadLook(S19PacketEntityHeadLook packetIn) {

    }

    @Override
    public void handleHeldItemChange(S09PacketHeldItemChange packetIn) {

    }

    @Override
    public void handleDisplayScoreboard(S3DPacketDisplayScoreboard packetIn) {

    }

    @Override
    public void handleEntityMetadata(S1CPacketEntityMetadata packetIn) {

    }

    @Override
    public void handleEntityVelocity(S12PacketEntityVelocity packetIn) {

    }

    @Override
    public void handleEntityEquipment(S04PacketEntityEquipment packetIn) {

    }

    @Override
    public void handleSetExperience(S1FPacketSetExperience packetIn) {

    }

    @Override
    public void handleUpdateHealth(S06PacketUpdateHealth packetIn) {

    }

    @Override
    public void handleTeams(S3EPacketTeams packetIn) {

    }

    @Override
    public void handleUpdateScore(S3CPacketUpdateScore packetIn) {

    }

    @Override
    public void handleSpawnPosition(S05PacketSpawnPosition packetIn) {

    }

    @Override
    public void handleTimeUpdate(S03PacketTimeUpdate packetIn) {

    }

    @Override
    public void handleUpdateSign(S33PacketUpdateSign packetIn) {

    }

    @Override
    public void handleSoundEffect(S29PacketSoundEffect packetIn) {

    }

    @Override
    public void handleCollectItem(S0DPacketCollectItem packetIn) {

    }

    @Override
    public void handleEntityTeleport(S18PacketEntityTeleport packetIn) {

    }

    @Override
    public void handleEntityProperties(S20PacketEntityProperties packetIn) {

    }

    @Override
    public void handleEntityEffect(S1DPacketEntityEffect packetIn) {

    }

    @Override
    public void handleCombatEvent(S42PacketCombatEvent packetIn) {

    }

    @Override
    public void handleServerDifficulty(S41PacketServerDifficulty packetIn) {

    }

    @Override
    public void handleCamera(S43PacketCamera packetIn) {

    }

    @Override
    public void handleWorldBorder(S44PacketWorldBorder packetIn) {

    }

    @Override
    public void handleTitle(S45PacketTitle packetIn) {

    }

    @Override
    public void handleSetCompressionLevel(S46PacketSetCompressionLevel packetIn) {

    }

    @Override
    public void handlePlayerListHeaderFooter(S47PacketPlayerListHeaderFooter packetIn) {

    }

    @Override
    public void handleResourcePack(S48PacketResourcePackSend packetIn) {

    }

    @Override
    public void handleEntityNBT(S49PacketUpdateEntityNBT packetIn) {

    }

    @Override
    public void onDisconnect(IChatComponent reason) {

    }
}
