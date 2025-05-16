/*
    Thanks to Sebastian Lague for A* algorithm explanation
    https://youtu.be/-L-WgKMFuhE
 */

package ipana.utils.pathfind.astar.cocoa;

import ipana.modules.player.AutoCocoa;
import ipana.utils.math.Pair;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import java.util.*;

import static ipana.modules.player.AutoCocoa.*;

public class CocoaAStar {
    Corridor start, end;
    Action action;

    public CocoaAStar(Corridor start, Corridor end, Action action) {
        this.end = end;
        this.start = start;
        this.action = action;
    }

    public ArrayList<VisitableVec> find(HashMap<Pair<Double, Double>, Corridor> availableCorridors) {
        ArrayList<CocoaNode> openNodes = new ArrayList<>();
        ArrayList<CocoaNode> closedNodes = new ArrayList<>();
        CocoaNode startNode = new CocoaNode(0, start.distToSq(end), null, start);

        openNodes.add(startNode);
        int elapsedFrame = 0;
        while (true) {
            openNodes.sort(Comparator.comparingDouble(CocoaNode::fCost).thenComparingDouble(CocoaNode::hCost));

            CocoaNode currentNode = openNodes.get(0);

            openNodes.remove(currentNode);
            closedNodes.add(currentNode);

            if (currentNode.corridor().equals(end)) {
                return pathTo(startNode, currentNode);
            }

            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                Corridor neighbour = getNeighbour(currentNode, facing, availableCorridors);
                if (neighbour != null) {
                    CocoaNode neighbourNode = new CocoaNode(neighbour.distToSq(start), neighbour.distToSq(end), currentNode, neighbour);
                    if (!isClosedNode(neighbourNode, closedNodes)) {
                        double moveCost = currentNode.gCost() + currentNode.corridor().distToSq(neighbour);
                        boolean isInOpen = isOpenNode(neighbourNode, openNodes);
                        if (moveCost < neighbourNode.gCost() || !isInOpen) {
                            neighbourNode.setGCost(moveCost);
                            neighbourNode.setHCost(neighbour.distToSq(end));
                            neighbourNode.setParent(currentNode);
                            if (!isInOpen) {
                                openNodes.add(neighbourNode);
                            }
                        }
                    }
                }
            }
            elapsedFrame++;
        }
    }

    public Corridor start() {
        return start;
    }

    public Corridor end() {
        return end;
    }

    public Action action() {
        return action;
    }

    private ArrayList<VisitableVec> pathTo(CocoaNode start, CocoaNode end) {
        ArrayList<VisitableVec> path = new ArrayList<>();
        CocoaNode currentNode = end;
        while (currentNode != start) {
            path.add(new VisitableVec(false, new Vec3(currentNode.corridor().x(), currentNode.corridor().y(), currentNode.corridor().z())));
            currentNode = currentNode.parent();
        }
        Collections.reverse(path);
        return path;
    }

    private boolean isClosedNode(CocoaNode node, ArrayList<CocoaNode> closed) {
        for (CocoaNode node2 : closed) {
            if (node2.corridor().equals(node.corridor())) {
                return true;
            }
        }
        return false;
    }
    private boolean isOpenNode(CocoaNode node, ArrayList<CocoaNode> open) {
        for (CocoaNode node2 : open) {
            if (node2.corridor().equals(node.corridor())) {
                return true;
            }
        }
        return false;
    }
    private AutoCocoa.Corridor getNeighbour(CocoaNode parent, EnumFacing facing, HashMap<Pair<Double, Double>, Corridor> corridors) {
        for (Map.Entry<Pair<Double, Double>, Corridor> entry : corridors.entrySet()) {
            if (entry.getKey().first().equals(parent.corridor().x() + facing.getFrontOffsetX()) && entry.getKey().second().equals(parent.corridor().z() + facing.getFrontOffsetZ())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
