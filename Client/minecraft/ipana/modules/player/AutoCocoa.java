package ipana.modules.player;

import ipana.Ipana;
import ipana.events.*;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.values.BoolValue;
import ipana.utils.gl.GList;
import ipana.utils.math.Pair;
import ipana.utils.pathfind.astar.cocoa.CocoaAStar;
import ipana.utils.pathfind.astar.cocoa.CocoaNode;
import ipana.utils.player.PlayerUtils;
import ipana.utils.player.RotationUtils;
import ipana.utils.render.RenderUtils;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.util.*;

import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;

public class AutoCocoa extends Module {
    public AutoCocoa() {
        super("AutoCocoa", Keyboard.KEY_NONE, Category.Player, "Auto harvest cocoa.");
    }
    private BoolValue renderCorridors = new BoolValue("Corridors", this, true, "Render the corridors.");
    private BoolValue renderPath = new BoolValue("Path", this, true, "Render the path.");
    private ArrayList<CocoaData> cocoasToPlace = new ArrayList<>();
    private ArrayList<CocoaData> cocoasToBreak = new ArrayList<>();
    private ArrayList<CollectData> itemsToCollect = new ArrayList<>();
    private HashMap<Pair<Double, Double>, Corridor> corridors = new HashMap<>();
    private BlockPos areaStart;
    private BlockPos areaEnd;
    private ArrayList<VisitableVec> pathGoal = new ArrayList<>();
    private CocoaData rightClickData;
    private CocoaData leftClickData;
    private double corridorY = -1;
    private boolean corridorSetup;
    private GList<Double> gList = new GList<>();
    private CocoaAStar aStar;
    private Action action = Action.Nothing;

    @Override
    public void onEnable() {
        aStar = null;
        pathGoal.clear();
        corridors.clear();
        cocoasToPlace.clear();
        cocoasToBreak.clear();
        rightClickData = null;
        leftClickData = null;
        areaEnd = null;
        areaStart = null;
        corridorSetup = false;
        corridorY = -1;
        gList.deleteList();
        super.onEnable();
    }

    private Listener<EventMouse> onMouse = new Listener<EventMouse>(event -> {
        //Mouse Button 2 : Corridor Y
        //Mouse Button 3 : Area Start
        //Mouse Button 4 : Area End

        BlockPos pos = mc.objectMouseOver.getBlockPos();
        if (event.getKey() == 3) {
            if (areaStart != null && areaStart.equals(pos)) {
                areaStart = null;
            } else {
                areaStart = pos;
            }
        } else if (event.getKey() == 4) {
            if (areaEnd != null && areaEnd.equals(pos)) {
                areaEnd = null;
            } else {
                areaEnd = pos;
            }
        } else if (event.getKey() == 2) {
            corridorY = pos.getY()+1;
            corridorSetup = false;
            corridors.clear();
            gList.deleteList();
        }
    }).filter(eventMouse -> mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK);

    private Listener<EventMoving> onMove = new Listener<EventMoving>(event -> {
        if (areaStart == null || areaEnd == null || corridors.isEmpty()) {
            return;
        }
        if (!pathGoal.isEmpty()) {
            double speed = Math.hypot(event.getX(), event.getZ());
            VisitableVec visitableVec = getFirst();
            Vec3 first = null;
            if (visitableVec != null) {
                first = visitableVec.vec3;
                double distTo = mc.thePlayer.getDistance(first.xCoord, first.yCoord, first.zCoord);
                if (distTo <= speed) {
                    visitableVec.visited = true;
                    VisitableVec visitableVec2 = getFirst();
                    if (visitableVec2 != null) {
                        first = visitableVec2.vec3;
                    }
                }
            }
            if (first != null) {
                float yawToPath = RotationUtils.getRotationFromPosition(first.xCoord, first.zCoord, first.yCoord)[0];
                double[] calc = PlayerUtils.calculate2(speed, yawToPath, 1);
                event.setX(calc[0]);
                event.setZ(calc[1]);
            } else if (!mc.thePlayer.isSneaking()) {
                event.setZ(0);
                event.setX(0);
            }
        } else if (!mc.thePlayer.isSneaking()) {
            event.setZ(0);
            event.setX(0);
        }
    }).weight(Event.Weight.LOWEST);

    private Listener<EventMoveInput> onInput = new Listener<>(event -> {
        if (!pathGoal.isEmpty()) {
            event.setForward(mc.thePlayer.moveForward = mc.thePlayer.movementInput.moveForward = 1);
        }
    });

    private Listener<EventPreUpdate> onPre = new Listener<>(event -> {
        if (corridorY > -1 && !corridorSetup && areaStart != null && areaEnd != null) {
            if (areaDistCheck()) {
                return;
            }
            corridorSetup = true;
            corridors = new HashMap<>();
            int minX = Math.min(areaStart.getX(), areaEnd.getX());
            int maxX = Math.max(areaStart.getX(), areaEnd.getX());
            int minZ = Math.min(areaStart.getZ(), areaEnd.getZ());
            int maxZ = Math.max(areaStart.getZ(), areaEnd.getZ());
            setupCorridors(minX, maxX, minZ, maxZ);
        }
        if (!cocoasToPlace.isEmpty() && !hasCocoaBeans()) {
            cocoasToPlace.clear();
        }
        //Search the area if cocoas to place is empty
        if (cocoasToPlace.isEmpty() && cocoasToBreak.isEmpty() && action == Action.Nothing) {
            if (areaStart != null && areaEnd != null) {
                if (areaDistCheck()) {
                    return;
                }
                int minX = Math.min(areaStart.getX(), areaEnd.getX());
                int maxX = Math.max(areaStart.getX(), areaEnd.getX());
                int minZ = Math.min(areaStart.getZ(), areaEnd.getZ());
                int maxZ = Math.max(areaStart.getZ(), areaEnd.getZ());
                int minY = Math.min(areaStart.getY(), areaEnd.getY());
                int maxY = Math.max(areaStart.getY(), areaEnd.getY());
                boolean hasCocoaBeans = hasCocoaBeans();
                for (int x = minX; x < maxX; x++) {
                    for (int z = minZ; z < maxZ; z++) {
                        for (int y = minY; y < maxY; y++) {
                            //Positions, states of every block in area
                            BlockPos pos = new BlockPos(x, y, z);
                            IBlockState state = mc.theWorld.getBlockState(pos);
                            Block block = state.getBlock();

                            if (block == Blocks.log && state.getValue(BlockPlanks.VARIANT) == BlockPlanks.EnumType.JUNGLE) {
                                //Check for horizontal sides
                                for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                                    BlockPos offsetPos = pos.offset(facing);
                                    IBlockState offsetState = mc.theWorld.getBlockState(offsetPos);
                                    Block offsetBlock = offsetState.getBlock();
                                    if (offsetBlock instanceof BlockAir) {
                                        if (hasCocoaBeans) {
                                            cocoasToPlace.add(new CocoaData(pos, facing));
                                        }
                                    } else if (offsetBlock instanceof BlockCocoa && offsetState.getValue(BlockCocoa.AGE) == 2) {
                                        cocoasToBreak.add(new CocoaData(offsetPos, facing));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!cocoasToPlace.isEmpty()) {
            action = Action.Place;
        } else if (!cocoasToBreak.isEmpty()) {
            action = Action.Break;
        } else {
            action = Action.Nothing;
        }

        if (areaStart != null && areaEnd != null) {
            int minX = Math.min(areaStart.getX(), areaEnd.getX());
            int maxX = Math.max(areaStart.getX(), areaEnd.getX());
            int minZ = Math.min(areaStart.getZ(), areaEnd.getZ());
            int maxZ = Math.max(areaStart.getZ(), areaEnd.getZ());
            int minY = Math.min(areaStart.getY(), areaEnd.getY());
            int maxY = Math.max(areaStart.getY(), areaEnd.getY());
            itemsToCollect.clear();
            //TODO: Optimize
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (entity.posX > minX && entity.posX < maxX &&
                        entity.posY > minY && entity.posY < minY + 3/*didn't use maxY because we can't get items from 4 blocks or higher*/ &&
                        entity.posZ > minZ && entity.posZ < maxZ) {
                    if (entity instanceof EntityItem item && item.getEntityItem() != null && item.getEntityItem().getItem() instanceof ItemDye && item.getEntityItem().getMetadata() == 3) {
                        if (getCollectDataById(item.getEntityId()) == null && item.getAge() >= 10) {
                            itemsToCollect.add(new CollectData(item.getEntityId()));
                        }
                    }
                }
            }
            if (!itemsToCollect.isEmpty()) {
                action = Action.Collect;
            }
        }

        switch (action) {
            case Collect -> {
                itemsToCollect.sort(Comparator.comparingDouble(c -> {
                    Entity entity = mc.theWorld.getEntityByID(c.id);
                    if (entity == null) {
                        return 0;
                    }
                    return mc.thePlayer.getDistanceToEntity(entity);
                }));
                CollectData collectData = itemsToCollect.get(0);
                Entity entity = mc.theWorld.getEntityByID(collectData.id);
                if (entity == null) {
                    itemsToCollect.remove(collectData);
                    action = Action.Nothing;
                    pathGoal.clear();
                } else {
                    double dist = mc.thePlayer.getDistance(entity.posX, mc.thePlayer.posY, entity.posZ);
                    if (dist > 1.4) {
                        if ((pathGoal.isEmpty() || aStar != null &&  (aStar.action() == Action.Place || aStar.action() == Action.Break || isAllVisited() && aStar.end().distToSq(entity.posX, entity.posZ) > 2)) && !corridors.isEmpty()) {
                            Corridor nearest = nearestCorridorTo(entity.posX, entity.posZ);
                            Corridor player = nearestCorridorTo(mc.thePlayer.posX, mc.thePlayer.posZ);
                            if (nearest != null && player != null) {
                                aStar = new CocoaAStar(player, nearest, Action.Collect);
                                pathGoal = aStar.find(corridors);
                            }
                        }
                    } else {
                        if (!pathGoal.isEmpty()) {
                            pathGoal.clear();
                            aStar = null;
                        }
                        if (entity.posY>mc.thePlayer.posY+2 && mc.thePlayer.onGround) {
                            mc.thePlayer.jump();
                        }
                    }
                }
                leftClickData = null;
                rightClickData = null;
            }
            case Place -> {
                sortList(cocoasToPlace, true);
                CocoaData first = cocoasToPlace.get(0);
                BlockPos pos = first.pos.offset(first.side);
                IBlockState state = mc.theWorld.getBlockState(pos);
                Block block = state.getBlock();
                //Post check if any blocks changed by other players
                while (first != null && !(block instanceof BlockAir)) {
                    cocoasToPlace.remove(first);
                    first = cocoasToPlace.isEmpty() ? null : cocoasToPlace.get(0);
                }
                if (first == null) {
                    return;
                }
                if (preChecks(event, first, true)) {
                    rightClickData = first;
                }
            }

            case Break -> {
                sortList(cocoasToBreak, false);
                CocoaData first = cocoasToBreak.get(0);
                IBlockState state = mc.theWorld.getBlockState(first.pos);
                Block block = state.getBlock();
                //Post check if any blocks changed by other players
                while (first != null && (!(block instanceof BlockCocoa) || state.getValue(BlockCocoa.AGE) < 2)) {
                    cocoasToBreak.remove(first);
                    first = cocoasToBreak.isEmpty() ? null : cocoasToBreak.get(0);
                    leftClickData = null;
                }
                if (first == null) {
                    return;
                }
                if (preChecks(event, first, false)) {
                    leftClickData = first;
                }
            }
        }
    });

    private Listener<EventPostUpdate> onPost = new Listener<>(event -> {
        if (rightClickData != null && hasCocoaBeans()) {
            //TODO: FastPlace check: full limit: 22, ShortTerm ticks: 10, ShortTerm limit: 6
            int tempSlot = mc.thePlayer.inventory.currentItem;
            setSlot(getCocoaBeans());
            mc.thePlayer.swingItem();
            mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), rightClickData.pos, rightClickData.side, new Vec3(rightClickData.pos.getX()+0.5, rightClickData.pos.getY()+0.5, rightClickData.pos.getZ()+0.5));
            setSlot(tempSlot);
            cocoasToPlace.remove(rightClickData);
            rightClickData = null;
            leftClickData = null;
            action = Action.Nothing;
        } else if (leftClickData != null) {
            int tempSlot = mc.thePlayer.inventory.currentItem;
            setSlot(getAxe());
            mc.thePlayer.swingItem();

            mc.playerController.onPlayerDamageBlock(leftClickData.pos, leftClickData.side);
            setSlot(tempSlot);
            /*
             //block gn (i think?)
            if (!mc.playerController.isHittingBlock() && mc.playerController.curBlockDamageMP == 0.0f && mc.playerController.blockHitDelay == 5) {
                cocoasToBreak.remove(leftClickData);
                leftClickData = null;
            }
             */
            //Cocoas can be placed after block is broken
            rightClickData = new CocoaData(leftClickData.pos.offset(leftClickData.side.getOpposite()), leftClickData.side);
            setSlot(getCocoaBeans());
            mc.thePlayer.swingItem();
            mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), rightClickData.pos, rightClickData.side, new Vec3(rightClickData.pos.getX()+0.5, rightClickData.pos.getY()+0.5, rightClickData.pos.getZ()+0.5));
            setSlot(tempSlot);
            rightClickData = null;
            action = Action.Nothing;
        }
    });

    private Listener<EventPacketReceive> onReceive = new Listener<EventPacketReceive>(event -> {
        S0DPacketCollectItem collectItem = (S0DPacketCollectItem) event.getPacket();
        Entity item = mc.theWorld.getEntityByID(collectItem.getCollectedItemEntityID());
        CollectData data = getCollectDataById(collectItem.getCollectedItemEntityID());
        if (item != null && data != null) {
            itemsToCollect.remove(data);
        }
    }).filter(eventPacketReceive -> eventPacketReceive.getState() == EventPacketReceive.PacketState.PRE && !itemsToCollect.isEmpty() && eventPacketReceive.getPacket() instanceof S0DPacketCollectItem);


    private CollectData getCollectDataById(int id) {
        for (CollectData collectData : itemsToCollect) {
            if (collectData.id == id) {
                return collectData;
            }
        }
        return null;
    }

    private boolean preChecks(EventPreUpdate event, CocoaData first, boolean place) {
        double[] sidePositions = getSidePositions(first, place);
        double x = sidePositions[0];
        double y = sidePositions[1];
        double z = sidePositions[2];
        double dist = mc.thePlayer.getDistance(x, mc.thePlayer.posY, z);
        boolean blockChecks = (dist > 1.5 || !mc.thePlayer.canEntityBeSeen(x, y, z));
        if (blockChecks) {
            if ((pathGoal.isEmpty() || aStar != null && isAllVisited() && aStar.end().distToSq(x, z) > 2) && !corridors.isEmpty()) {
                Corridor nearestCorridorEnd = null;
                Corridor nearestCorridorStart = null;
                double distToEnd = Integer.MAX_VALUE;
                double distToStart = Integer.MAX_VALUE;
                for (Corridor corridor : corridors.values())  {
                    if (corridor != null) {
                        double cDistToEnd = corridor.distToSq(x, z);
                        double cDistToStart = corridor.distToSq(mc.thePlayer.posX, mc.thePlayer.posZ);
                        if (distToEnd > cDistToEnd) {
                            distToEnd = cDistToEnd;
                            nearestCorridorEnd = corridor;
                        }
                        if (distToStart > cDistToStart) {
                            distToStart = cDistToStart;
                            nearestCorridorStart = corridor;
                        }
                    }
                }
                if (nearestCorridorEnd != null && nearestCorridorStart != null) {
                    aStar = new CocoaAStar(nearestCorridorStart, nearestCorridorEnd, place ? Action.Place : Action.Break);
                    pathGoal = aStar.find( corridors);
                }
            }
            return false;
        } else {
            if (!pathGoal.isEmpty()) {
                pathGoal.clear();
                aStar = null;
            }
            //Not %100 exact rotations but works
            float[] rotations = RotationUtils.getDirectionToBlock(first.pos.getX(), first.pos.getY(), first.pos.getZ(), first.side);
            event.setYaw(rotations[0]);
            event.setPitch(rotations[1]);
            mc.thePlayer.rotationYawHead = event.getYaw();
            mc.thePlayer.renderYawOffset = event.getYaw();
            mc.thePlayer.rotationPitchHead = event.getPitch();
            return true;
        }
    }

    private void sortList(ArrayList<CocoaData> cocoaData, boolean place) {
        if (!corridors.isEmpty()) {
            //JAVA IS GAY,

            cocoaData.sort((o1, o2) -> {
                double[] positions1 = getSidePositions(o1, place, true);
                double[] positions2 = getSidePositions(o2, place, true);

                double dist1 = mc.thePlayer.getDistance(positions1[0], mc.thePlayer.posY, positions1[2]);
                double dist2 = mc.thePlayer.getDistance(positions2[0], mc.thePlayer.posY, positions2[2]);
                if (!canBeSeen(positions1)) {
                    dist1 += 1000;
                }
                if (!canBeSeen(positions2)) {
                    dist2 += 1000;
                }
                return Double.compare(dist1, dist2);
            });
            ArrayList<CocoaData> bokData = new ArrayList<>();
            CocoaData lastCocoa = null;
            for (CocoaData cocoa : cocoaData) {
                if (lastCocoa != null && cocoa.side == lastCocoa.side.getOpposite()) {
                    if (cocoa.side.getFrontOffsetX() != 0 && n(cocoa.pos.getX()-lastCocoa.pos.getX()) == n(cocoa.side.getFrontOffsetX()) || cocoa.side.getFrontOffsetZ() != 0 && n(cocoa.pos.getZ()-lastCocoa.pos.getZ()) == n(cocoa.side.getFrontOffsetZ())) {
                        bokData.add(cocoa);
                    } else {
                        lastCocoa = cocoa;
                    }
                } else {
                    lastCocoa = cocoa;
                }
            }
            cocoaData.removeAll(bokData);
            cocoaData.addAll(bokData);
            bokData.clear();
        }
    }

    public boolean canBeSeen(double[] positions) {
        return mc.thePlayer.canEntityBeSeen(positions[0], positions[1], positions[2]);
    }

    //Used in old sort
    private EnumFacing getFacingHorizontal(double x, double z, double x2, double z2) {
        double xDiff = x - x2;
        double zDiff = z - z2;
        double absX = Math.abs(xDiff);
        double absZ = Math.abs(zDiff);

        if (absX > absZ) {
            if (xDiff > 0) {
                return EnumFacing.WEST;
            } else if (xDiff < 0) {
                return EnumFacing.EAST;
            } else {
                return null;
            }
        } else if (absZ > absX) {
            if (zDiff > 0) {
                return EnumFacing.NORTH;
            } else if (zDiff < 0) {
                return EnumFacing.SOUTH;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private Corridor nearestCorridorTo(double x, double z) {
        Corridor nearestCorridor = null;
        double distTo = Integer.MAX_VALUE;
        for (Corridor corridor : corridors.values())  {
            if (corridor != null) {
                double cDistTo = corridor.distTo(x, z);
                if (distTo > cDistTo) {
                    distTo = cDistTo;
                    nearestCorridor = corridor;
                }
            }
        }
        return nearestCorridor;
    }

    private double[] getSidePositions(CocoaData first, boolean place) {
        return getSidePositions(first, place, false);
    }
    private double[] getSidePositions(CocoaData first, boolean place, boolean sorting) {
        BlockPos pos = first.pos;
        double off = sorting ? 0.5 : place ? 0.51 : 0.1;
        double x = pos.x + 0.5 + first.side.getFrontOffsetX()*off;
        double y = pos.y + 0.5;
        double z = pos.z + 0.5 + first.side.getFrontOffsetZ()*off;
        return new double[]{x,y,z};
    }
    private void setupCorridors(int minX, int maxX, int minZ, int maxZ) {
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                double expand = 0.4375;
                AxisAlignedBB bb = new AxisAlignedBB(x - expand, corridorY+0.1, z - expand, x + expand, corridorY + 1, z + expand);
                if (mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                    addCorridor(x, z);
                }
            }
        }
    }

    private void addCorridor(double x, double z) {
        Pair<Double, Double> coords = new Pair<>(x, z);
        Corridor corridor = corridors.get(coords);
        if (corridor == null) {
            corridor = new Corridor(x, corridorY, z);
            corridors.put(coords, corridor);
        }
    }

    private int getCocoaBeans() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() instanceof ItemDye && stack.getMetadata() == 3) {
                return i;
            }
        }
        return mc.thePlayer.inventory.currentItem;
    }

    private int getAxe() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() instanceof ItemAxe) {
                return i;
            }
        }
        return mc.thePlayer.inventory.currentItem;
    }

    private boolean hasCocoaBeans() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() instanceof ItemDye && stack.getMetadata() == 3) {
                return true;
            }
        }
        return false;
    }

    private void setSlot(int slot) {
        mc.thePlayer.inventory.currentItem = slot;
        mc.playerController.syncCurrentPlayItem();
    }

    private boolean areaDistCheck() {
        if (areaStart.getDistance(areaEnd.getX(), areaEnd.getY(), areaEnd.getZ()) > 300) {
            PlayerUtils.debug("bok bypass kanka");
            areaStart = null;
            areaEnd = null;
            return true;
        }
        return false;
    }

    private VisitableVec getFirst() {
        for (VisitableVec visitableVec : pathGoal) {
            if (!visitableVec.visited) {
                return visitableVec;
            }
        }
        return null;
    }

    private boolean isAllVisited() {
        for (VisitableVec visitableVec : pathGoal) {
            if (!visitableVec.visited) {
                return false;
            }
        }
        return true;
    }


    private Listener<EventRender3D> onRender3D = new Listener<>(event -> {
        BlockPos[] positions = new BlockPos[]{areaStart, areaEnd};
        for (BlockPos pos : positions) {
            if (pos != null) {
                double posX = pos.getX() - mc.getRenderManager().renderPosX;
                double posY = pos.getY() - mc.getRenderManager().renderPosY;
                double posZ = pos.getZ() - mc.getRenderManager().renderPosZ;
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GL11.glLineWidth(1.0F);
                GlStateManager.disableTexture2D();
                GlStateManager.disableDepth();
                GlStateManager.depthMask(false);
                AxisAlignedBB bb = new AxisAlignedBB(posX,posY,posZ,posX+1,posY+1,posZ+1);
                RenderUtils.drawOutlineBox(bb);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }

        //WIP
        if (corridorY > -1 && corridorSetup && renderCorridors.getValue()) {
            GlStateManager.pushMatrix();
            int minX = Math.min(areaStart.getX(), areaEnd.getX());
            int minZ = Math.min(areaStart.getZ(), areaEnd.getZ());
            GlStateManager.translate(minX-mc.getRenderManager().renderPosX, -mc.getRenderManager().renderPosY, minZ-mc.getRenderManager().renderPosZ);
            gList.render(corridorY, c -> {
                for (Corridor corridor : corridors.values()) {
                    if (corridor != null) {
                        GlStateManager.pushMatrix();
                        GlStateManager.enableBlend();
                        GL11.glLineWidth(1.0F);
                        GlStateManager.disableTexture2D();
                        GlStateManager.disableDepth();
                        GlStateManager.depthMask(false);
                        GL11.glEnable(GL_LINE_SMOOTH);
                        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                            Corridor neighbour = null;
                            for (Map.Entry<Pair<Double, Double>, Corridor> entry : corridors.entrySet()) {
                                if (entry.getKey().first().equals(corridor.x+facing.getFrontOffsetX()) && entry.getKey().second().equals(corridor.z+facing.getFrontOffsetZ())) {
                                    neighbour = entry.getValue();
                                    break;
                                }
                            }
                            if (neighbour != null) {
                                GL11.glBegin(3);
                                GL11.glColor4f(Ipana.getClientColor().getRed()/255f, Ipana.getClientColor().getGreen()/255f, Ipana.getClientColor().getBlue()/255f, Ipana.getClientColor().getAlpha()/255f);
                                double offX = facing.getFrontOffsetX();
                                double offZ = facing.getFrontOffsetZ();
                                double posX = corridor.x - minX;
                                double posY = corridorY;
                                double posZ = corridor.z - minZ;
                                GL11.glVertex3d(posX, posY, posZ);
                                GL11.glVertex3d(posX+offX, posY, posZ+offZ);
                                GL11.glEnd();
                            }
                        }
                        GL11.glDisable(GL_LINE_SMOOTH);
                        GlStateManager.depthMask(true);
                        GlStateManager.enableDepth();
                        GlStateManager.enableTexture2D();
                        GlStateManager.disableBlend();
                        GlStateManager.popMatrix();
                    }
                }
            });
            GlStateManager.popMatrix();
        }

        if (renderPath.getValue() && !pathGoal.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GL11.glLineWidth(1.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GL11.glEnable(GL_LINE_SMOOTH);
            GL11.glBegin(3);
            GL11.glColor4f(1, 1, 1, 1);
            for (VisitableVec pair : pathGoal) {
                Vec3 vec3 = pair.vec3;
                GL11.glVertex3d(vec3.xCoord-mc.getRenderManager().renderPosX, vec3.yCoord-mc.getRenderManager().renderPosY, vec3.zCoord-mc.getRenderManager().renderPosZ);
            }
            GL11.glEnd();
            GL11.glDisable(GL_LINE_SMOOTH);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    });

    private Num n(double number) {
        if (number > 0) {
            return Num.POSITIVE;
        } else if (number < 0) {
            return Num.NEGATIVE;
        } else {
            return Num.ZERO;
        }
    }

    enum Num {
        NEGATIVE, POSITIVE, ZERO
    }

    public enum Action {
        Collect, Break, Place, Nothing
    }

    class CollectData {
        int id;

        public CollectData(int id) {
            this.id = id;
        }
    }

    class CocoaData {
        BlockPos pos;
        EnumFacing side;

        CocoaData(BlockPos pos, EnumFacing side) {
            this.pos = pos;
            this.side = side;
        }
    }

    public static class VisitableVec {
        public boolean visited;
        public Vec3 vec3;

        public VisitableVec(boolean visited, Vec3 vec3) {
            this.visited = visited;
            this.vec3 = vec3;
        }
    }

    public static class Corridor {
        double x, y, z;

        Corridor(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double x() {
            return x;
        }

        public double y() {
            return y;
        }

        public double z() {
            return z;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Corridor)) {
                return false;
            } else if (this == o) {
                return true;
            } else {
                return x == ((Corridor) o).x && z == ((Corridor) o).z;
            }
        }
        public double distTo(double x, double z) {
            double deltaX = this.x - x;
            double deltaZ = this.z - z;
            return deltaX*deltaX + deltaZ*deltaZ;
        }
        public double distToSq(double x, double z) {
            double deltaX = Math.abs(this.x - x);
            double deltaZ = Math.abs(this.z - z);
            return Math.sqrt(deltaX*deltaX + deltaZ*deltaZ);
        }
        public double distToSq(Corridor other) {
            double deltaX = Math.abs(this.x - other.x);
            double deltaZ = Math.abs(this.z - other.z);
            return Math.sqrt(deltaX*deltaX + deltaZ*deltaZ);
        }
    }
}
