package org.webcurator.core.visualization.networkmap.graph.pyhsics;

import org.webcurator.core.visualization.networkmap.graph.GraphNode;
import org.webcurator.core.visualization.networkmap.graph.NetworkMapOptions;

import java.util.List;

public class BarnesHutSolver {
    private NetworkMapOptions options;
    private List<GraphNode> nodes;
    private List<GraphNode> forces;
    private int width, height;

    public BarnesHutSolver(NetworkMapOptions options, int width, int height) {
        this.options = options;
        this.width = width;
        this.height = height;
    }
//
//    public void init(NetworkMapDomain root) {
//        this.nodes = new ArrayList<>();
//        this.forces = new ArrayList<>();
//
//        root.getChildren().forEach(domain -> {
//            GraphNode graphNode = new GraphNode();
//            graphNode.setId(domain.getId());
//            graphNode.setX(0);
//            graphNode.setY(0);
//            nodes.add(graphNode);
//
//            GraphNode forceNode = new GraphNode();
//            forceNode.setId(domain.getId());
//            forceNode.setX(0);
//            forceNode.setY(0);
//            forces.add(forceNode);
//        });
//    }
//
//    public void resolveGravity() {
//        for (int i = 0; i < nodes.size(); i++) {
//            GraphNode node = nodes.get(i);
//            double dx = -node.x;
//            double dy = -node.y;
//            double distance = Math.sqrt(dx * dx + dy * dy);
//
//            this.calculateForces(distance, dx, dy, forces, i);
//        }
//    }
//
//    private void calculateForces(double distance, double dx, double dy, int index) {
//        double gravityForce = distance == 0 ? 0 : this.options.centralGravity / distance;
//        forces.get(index).x = dx * gravityForce;
//        forces.get(index).y = dy * gravityForce;
//    }
//
//
//    public void resolveNode() {
//        var nodeCount = nodeIndices.length; // create the tree
//
//        var barnesHutTree = this.formBarnesHutTree(); // for debugging
//
//
//        this.barnesHutTree = barnesHutTree; // place the nodes one by one recursively
//
//        for (var i = 0; i < nodeCount; i++) {
//            node = nodes[nodeIndices[i]];
//
//            if (node.options.mass > 0) {
//                // starting with root is irrelevant, it never passes the BarnesHutSolver condition
//                this._getForceContributions(barnesHutTree.root, node);
//            }
//        }
//    }
//
//    @SuppressWarnings("all")
//    public BarnesHutTree formBarnesHutTree() {
//        double minX = this.nodes.stream().mapToDouble(GraphNode::getX).min().getAsDouble();
//        double minY = this.nodes.stream().mapToDouble(GraphNode::getY).min().getAsDouble();
//        double maxX = this.nodes.stream().mapToDouble(GraphNode::getX).max().getAsDouble();
//        double maxY = this.nodes.stream().mapToDouble(GraphNode::getY).max().getAsDouble();
//
//        double sizeDiff = Math.abs(maxX - minX) - Math.abs(maxY - minY); // difference between X and Y
//        if (sizeDiff > 0) {
//            minY -= 0.5 * sizeDiff;
//            maxY += 0.5 * sizeDiff;
//        } // xSize > ySize
//        else {
//            minX += 0.5 * sizeDiff;
//            maxX -= 0.5 * sizeDiff;
//        } // xSize < ySize
//
//
//        double minimumTreeSize = 1e-5;
//        double rootSize = Math.max(minimumTreeSize, Math.abs(maxX - minX));
//        double halfRootSize = 0.5 * rootSize;
//        double centerX = 0.5 * (minX + maxX);
//        double centerY = 0.5 * (minY + maxY); // construct the barnesHutTree
//
//        BarnesHutTree barnesHutTree = new BarnesHutTree();
//        barnesHutTree.centerOfMass.x = 0;
//        barnesHutTree.centerOfMass.y = 0;
//        barnesHutTree.mass = this.nodes.size();
//        barnesHutTree.range.minX = centerX - halfRootSize;
//        barnesHutTree.range.maxX = centerX + halfRootSize;
//        barnesHutTree.range.minY = centerY - halfRootSize;
//        barnesHutTree.range.maxY = centerY + halfRootSize;
//        barnesHutTree.size = rootSize;
//        barnesHutTree.calcSize = 1 / rootSize;
//        barnesHutTree.maxWidth = 0;
//        barnesHutTree.childrenCount = 4;
//        barnesHutTree.level = 0;
//
//        this.splitBranch(barnesHutTree); // place the nodes one by one recursively
//
//        for (var _i = 0; _i < nodeCount; _i++) {
//            node = nodes[nodeIndices[_i]];
//
//            if (node.options.mass > 0) {
//                this._placeInTree(barnesHutTree.root, node);
//            }
//        } // make global
//
//
//        return barnesHutTree;
//    }
//
//    private void splitBranch(BarnesHutTree parentBranch) {
//        Object containedNode = null;
//
//        if (parentBranch.childrenCount == 1) {
//            containedNode = parentBranch.children.data;
//            parentBranch.mass = 0;
//            parentBranch.centerOfMass.x = 0;
//            parentBranch.centerOfMass.y = 0;
//        }
//
//        parentBranch.childrenCount = 4;
//        parentBranch.children.data = null;
//
//        double childSize = 0.5 * parentBranch.size;
//
//        double minX, maxX, minY, maxY;
//        minX = parentBranch.range.minX;
//        maxX = parentBranch.range.minX + childSize;
//        minY = parentBranch.range.minY;
//        maxY = parentBranch.range.minY + childSize;
//        parentBranch.children.NW = createRegion(parentBranch, minX, minY, maxX, maxY);
//
//        minX = parentBranch.range.minX + childSize;
//        maxX = parentBranch.range.maxX;
//        minY = parentBranch.range.minY;
//        maxY = parentBranch.range.minY + childSize;
//        parentBranch.children.NE = createRegion(parentBranch, minX, minY, maxX, maxY);
//
//        minX = parentBranch.range.minX;
//        maxX = parentBranch.range.minX + childSize;
//        minY = parentBranch.range.minY + childSize;
//        maxY = parentBranch.range.maxY;
//        parentBranch.children.SW = createRegion(parentBranch, minX, minY, maxX, maxY);
//
//        minX = parentBranch.range.minX + childSize;
//        maxX = parentBranch.range.maxX;
//        minY = parentBranch.range.minY + childSize;
//        maxY = parentBranch.range.maxY;
//        parentBranch.children.SE = createRegion(parentBranch, minX, minY, maxX, maxY);
//
//        if (containedNode != null) {
//            this._placeInTree(parentBranch, containedNode);
//        }
//
//    }
//
//    private void placeInTree(BarnesHutTree parentBranch, GraphNode node, boolean skipMassUpdate) {
//        if (skipMassUpdate != true || skipMassUpdate == = undefined) {
//            // update the mass of the branch.
//            this._updateBranchMass(parentBranch, node);
//        }
//
//        var range = parentBranch.children.NW.range;
//        var region;
//
//        if (range.maxX > node.x) {
//            // in NW or SW
//            if (range.maxY > node.y) {
//                region = "NW";
//            } else {
//                region = "SW";
//            }
//        } else {
//            // in NE or SE
//            if (range.maxY > node.y) {
//                region = "NE";
//            } else {
//                region = "SE";
//            }
//        }
//
//        this._placeInRegion(parentBranch, node, region);
//    }
//
//    public void placeInRegion(parentBranch, node, region) {
//        var children = parentBranch.children[region];
//
//        switch (children.childrenCount) {
//            case 0:
//                // place node here
//                children.children.data = node;
//                children.childrenCount = 1;
//
//                this._updateBranchMass(children, node);
//
//                break;
//
//            case 1:
//                // convert into children
//                // if there are two nodes exactly overlapping (on init, on opening of cluster etc.)
//                // we move one node a little bit and we do not put it in the tree.
//                if (children.children.data.x == = node.x && children.children.data.y == = node.y) {
//                    node.x += this._rng();
//                    node.y += this._rng();
//                } else {
//                    this._splitBranch(children);
//
//                    this._placeInTree(children, node);
//                }
//
//                break;
//
//            case 4:
//                // place in branch
//                this._placeInTree(children, node);
//
//                break;
//        }
//    }
//
//    private BarnesHutTree createRegion(BarnesHutTree parentBranch, double minX, double minY, double maxX, double maxY) {
//        BarnesHutTree region = new BarnesHutTree();
//        region.range.minX = minX;
//        region.range.minY = minY;
//        region.range.maxX = maxX;
//        region.range.maxY = maxY;
//        region.size = 0.5 * parentBranch.size;
//        region.calcSize = 2 * parentBranch.calcSize;
//        region.level = parentBranch.level + 1;
//        region.childrenCount = 0;
//    }
//
//    public int getWidth() {
//        return width;
//    }
//
//    public void setWidth(int width) {
//        this.width = width;
//    }
//
//    public int getHeight() {
//        return height;
//    }
//
//    public void setHeight(int height) {
//        this.height = height;
//    }
//
//    public NetworkMapOptions getOptions() {
//        return options;
//    }
//
//    public void setOptions(NetworkMapOptions options) {
//        this.options = options;
//    }
//
//    public List<GraphNode> getNodes() {
//        return nodes;
//    }
//
//    public void setNodes(List<GraphNode> nodes) {
//        this.nodes = nodes;
//    }
//
//    public List<GraphNode> getForces() {
//        return forces;
//    }
//
//    public void setForces(List<GraphNode> forces) {
//        this.forces = forces;
//    }
}

class BarnesHutTree {
    public Point centerOfMass = new Point();
    public int mass;
    public Range range = new Range();
    public double size;
    public double calcSize;
    public Children children = new Children();
    public double maxWidth;
    public int level;
    public int childrenCount;
}

class Point {
    public double x, y;
}

class Range {
    public double minX, minY, maxX, maxY;
}

class Children {
    public Object data;
    public BarnesHutTree NW, NE, SW, SE;
}