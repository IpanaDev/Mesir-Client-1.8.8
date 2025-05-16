package ipana.utils.pathfind.astar;

import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class Node {
    /* Distance between start node*/
    private int gCost;
    /* Distance between end node */
    private int hCost;
    private Node parent;
    private BlockPos position;

    public Node(int gCost, int hCost, BlockPos position, Node parent) {
        this.gCost = gCost;
        this.hCost = hCost;
        this.parent = parent;
        this.position = position;
    }

    public void setGCost(int gCost) {
        this.gCost = gCost;
    }

    public void setHCost(int hCost) {
        this.hCost = hCost;
    }

    public int getGCost() {
        return gCost;
    }

    public int getHCost() {
        return hCost;
    }

    public int getFCost() {
        return gCost+hCost;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public BlockPos getPosition() {
        return position;
    }
}
