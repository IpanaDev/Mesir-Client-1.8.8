package ipana.utils.pathfind.astar;

import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AStar {
    private Minecraft mc = Minecraft.getMinecraft();

    public List<BlockPos> findPath(BlockPos start, BlockPos end, int searchCapacity) {
        List<Node> openedNodes = new ArrayList<>();
        List<Node> closedNodes = new ArrayList<>();
        Node startNode = new Node(start.distanceTo(start.add(1,0,0)),start.distanceTo(end),start,null);
        openedNodes.add(startNode);
        int capacity = 0;
        while (true) {
            openedNodes.sort(Comparator.comparingDouble(n -> ((Node)n).getFCost()).thenComparing(n -> ((Node)n).getHCost()));
            if (openedNodes.isEmpty()) {
                return new ArrayList<>();
            }
            Node currentNode = openedNodes.get(0);
            openedNodes.remove(currentNode);
            closedNodes.add(currentNode);

            if (currentNode.getPosition().equals(end) || capacity >= searchCapacity) {
                //System.out.println("Path Search End");
                return getPath(startNode,currentNode);
            }

            for (Node neighbours : getNeighbours(start,end,currentNode)) {
                if (isValid(neighbours.getPosition()) && !isClosed(closedNodes,neighbours.getPosition())) {
                    int moveCost = currentNode.getGCost() + currentNode.getPosition().distanceTo(neighbours.getPosition());
                    if (moveCost < neighbours.getGCost() || !isOpen(openedNodes,neighbours.getPosition())) {
                        neighbours.setGCost(moveCost);
                        neighbours.setHCost(neighbours.getPosition().distanceTo(end));
                        neighbours.setParent(currentNode);
                        openedNodes.add(neighbours);
                    }
                }
            }
            capacity++;
        }
    }
    private List<Node> getNeighbours(BlockPos start, BlockPos end, Node current) {
        List<Node> neighbours = new ArrayList<>();
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0 && y == 0)
                        continue;

                    BlockPos pos = current.getPosition().add(x, y, z);

                    //System.out.println(x+" : "+z);
                    boolean deads = false;
                    if (x != 0 && z != 0) {
                        int bX = current.getPosition().x - x;
                        int bZ = current.getPosition().z - z;
                        if (!isValid(pos.add(-bX, 0, 0)) || !isValid(pos.add(0, 0, -bZ))) {
                            deads = true;
                            //System.out.println("aga b");
                        }
                    }
                    if (!deads) {
                        neighbours.add(new Node(pos.distanceTo(start), pos.distanceTo(end), pos, current));
                    }
                }
            }
        }
        return neighbours;
    }

    private List<BlockPos> getPath(Node start, Node end) {
        List<BlockPos> path = new ArrayList<>();
        Node currentNode = end;
        while (currentNode != start) {
            path.add(currentNode.getPosition());
            currentNode = currentNode.getParent();
        }
        Collections.reverse(path);
        return path;
    }

    private boolean isClosed(List<Node> closed, BlockPos vec3) {
        for (Node node : closed) {
            if (node.getPosition().equals(vec3)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOpen(List<Node> open, BlockPos vec3) {
        for (Node node : open) {
            if (node.getPosition().equals(vec3)) {
                return true;
            }
        }
        return false;
    }


    private boolean isValid(BlockPos position) {
        return mc.theWorld.getBlockState(position).getBlock() instanceof BlockAir && mc.theWorld.getBlockState(position.add(0,1,0)).getBlock() instanceof BlockAir;
    }
}
