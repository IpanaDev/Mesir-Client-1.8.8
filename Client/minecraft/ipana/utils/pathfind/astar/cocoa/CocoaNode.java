package ipana.utils.pathfind.astar.cocoa;

import ipana.modules.player.AutoCocoa;

public class CocoaNode {
    /* Distance between start node*/
    private double gCost;
    /* Distance between end node */
    private double hCost;
    /* Sum of gCost and hCost */
    private double fCost;
    private AutoCocoa.Corridor corridor;
    private CocoaNode parentNode;

    public CocoaNode(double gCost, double hCost, CocoaNode parentNode, AutoCocoa.Corridor corridor) {
        this.gCost = gCost;
        this.hCost = hCost;
        this.fCost = gCost + hCost;
        this.parentNode = parentNode;
        this.corridor = corridor;
    }

    public AutoCocoa.Corridor corridor() {
        return corridor;
    }

    public double gCost() {
        return gCost;
    }

    public double hCost() {
        return hCost;
    }

    public double fCost() {
        return fCost;
    }

    public CocoaNode parent() {
        return parentNode;
    }

    public void setParent(CocoaNode _parentNode) {
        parentNode = _parentNode;
    }

    public void setGCost(double _gCost) {
        gCost = _gCost;
        this.fCost = gCost + hCost;
    }

    public void setHCost(double _hCost) {
        hCost = _hCost;
        this.fCost = gCost + hCost;
    }
}
