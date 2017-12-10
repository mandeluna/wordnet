import edu.princeton.cs.algs4.BreadthFirstDirectedPaths;
import edu.princeton.cs.algs4.DepthFirstOrder;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

import java.util.LinkedList;
import java.util.List;

/*
 *
 * All methods should throw a java.lang.IllegalArgumentException if any argument is null
 * or if any argument vertex is invalid—not between 0 and G.V() - 1.
 *
 */

public class SAP {

    private final Digraph G;
    private final Iterable<Integer> topoSort;

    private class Ancestor {
        int distance;
        int ancestor;

        Ancestor(int ancestor, int distance) {
            this.ancestor = ancestor;
            this.distance = distance;
        }
    }

    // constructor takes a digraph (not necessarily a DAG)
    // complexity due to DFS traversal O(V + E)
    public SAP(Digraph G) {
        if (G == null) {
            throw new IllegalArgumentException();
        }
        this.G = new Digraph(G);
        DepthFirstOrder traversal = new DepthFirstOrder(G);
        topoSort = traversal.reversePost();
    }

    private boolean isValidVertex(int v) {
        return (v >= 0) && (v <= G.V() - 1);
    }

    private void validateArgs(Iterable<Integer> v, Iterable<Integer> w) {
        if ((v == null) || (w == null)) {
            throw new IllegalArgumentException("Null argument");
        }
        for (int orig : v) {
            if (!isValidVertex(orig)) {
                throw new IllegalArgumentException("Invalid origin vertex " + orig);
            }
        }
        for (int dest : w) {
            if (!isValidVertex(dest)) {
                throw new IllegalArgumentException("Invalid destination vertex " + dest);
            }
        }
    }

    // Complexity: 2 * O(V + E) + O(V) due to 2 BFS traversals + visiting V roots (worst case)
    private Ancestor findCommonAncestor(Iterable<Integer> v, Iterable<Integer> w) {
        validateArgs(v, w);

        BreadthFirstDirectedPaths origPaths = new BreadthFirstDirectedPaths(G, v);
        BreadthFirstDirectedPaths destPaths = new BreadthFirstDirectedPaths(G, w);

        int minDistance = Integer.MAX_VALUE;
        Ancestor ancestor = null;
        for (int root : topoSort) {
            if (origPaths.hasPathTo(root) && destPaths.hasPathTo(root)) {
                int distance = origPaths.distTo(root) + destPaths.distTo(root);
                if (distance >= 0 && distance < minDistance) {
                    minDistance = distance;
                    ancestor = new Ancestor(root, distance);
                }
            }
        }
        return ancestor;
    }

    private static List<Integer> listWith(int value) {
        List<Integer> list = new LinkedList<>();
        list.add(value);
        return list;
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        return length(listWith(v), listWith(w));
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        return ancestor(listWith(v), listWith(w));
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        Ancestor common = findCommonAncestor(v, w);
        if (common != null) {
            return common.distance;
        }
        return -1;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        Ancestor common = findCommonAncestor(v, w);
        if (common != null) {
            return common.ancestor;
        }
        return -1;
    }

    // do unit testing of this class
    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }
}