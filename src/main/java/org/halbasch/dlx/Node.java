package org.halbasch.dlx;

/**
 * Simple implementation of a node for Donald Knuth's dancing links algorithm.
 * 
 * @author rhalbasch
 *
 */
public class Node {
	protected Node L, R, U, D;
	protected Column C;

	public Node() {
		L = R = U = D = this;
	}
	
	public Node L() {
		return L;
	}

	public Node R() {
		return R;
	}
	
	public Node U() {
		return U;
	}
	
	public Node D() {
		return D;
	}
	
	public Column C() {
		return C;
	}
	
	/**
	 * Inserts the provided node to the right of this node in the rows cycle.
	 * 
	 * @param node Node to insert
	 * @return the inserted node
	 */
	public Node insert(Node node) {
		R.L = node;
		node.R = R;
		node.L = this;
		return R = node;
	}

	protected void hide() {
		U.D = D;
		D.U = U;
	}

	protected void show() {
		U.D = this;
		D.U = this;
	}
}
