import edu.princeton.cs.algs4.*;

import java.util.LinkedList;
import java.util.List;

/*
 *
 * All methods should throw a java.lang.IllegalArgumentException if any argument is null
 * or if any argument vertex is invalid—not between 0 and G.V() - 1.
 *
 */

public class SAP {

	private Digraph G, reverse;
	private Iterable<Integer> topoSort;

	private class Ancestor {
		Ancestor(int ancestor, int distance) {
			this.ancestor = ancestor;
			this.distance = distance;
		}
		int distance;
		int ancestor;
	}

	// constructor takes a digraph (not necessarily a DAG)
	public SAP(Digraph G) {
		if (G == null) {
			throw new IllegalArgumentException();
		}
		this.G = G;
		DepthFirstOrder traversal = new DepthFirstOrder(G);
		topoSort = traversal.reversePost();
	}

	private boolean isValidVertex(int v) {
		return (v >= 0) && (v <= G.V() - 1);
	}

	private void validateArgs(Iterable<Integer>v, Iterable<Integer> w) {
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

	private Ancestor findCommonAncestor(Iterable<Integer> v, Iterable<Integer> w) {

		validateArgs(v, w);

		BreadthFirstDirectedPaths origPaths = new BreadthFirstDirectedPaths(G, v);
		BreadthFirstDirectedPaths destPaths = new BreadthFirstDirectedPaths(G, w);

		for (int root : topoSort) {
			if (origPaths.hasPathTo(root) && destPaths.hasPathTo(root)) {
				return new Ancestor(root, origPaths.distTo(root) + destPaths.distTo(root));
			}
		}
		return null;
	}

	private static List<Integer> listWith(Integer value) {
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
			int length   = sap.length(v, w);
			int ancestor = sap.ancestor(v, w);
			StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
		}
	}
}