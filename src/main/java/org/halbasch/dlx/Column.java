package org.halbasch.dlx;

/**
 * Simple implementation of a column header for Donald Knuth's dancing links algorithm.
 * 
 * @author rhalbasch
 *
 */
public class Column extends Node {
	private int size = 0;
	private Object name;

	protected Column() {
		C = this;
	}

	public Column(Object name) {
		this();
		this.name = name;
	}

	public int getSize() {
		return size;
	}

	public Object getName() {
		return name;
	}

	/**
	 * Appends the provided node to the end of this columns cycle
	 * 
	 * @param node the Node to append
	 * @return the appended node
	 */
	public Node appendNode(Node node) {
		size++;
		node.C = this;
		node.D = this;
		node.U = U;
		U.D = node;
		return U = node;
	}
	
	protected void hide() {
		L.R = R;
		R.L = L;
	}

	protected void show() {
		L.R = this;
		R.L = this;
	}
	
	protected void cover() {
		hide();
		for (Node i = D; i != this; i = i.D) {
			for (Node j = i.R; j != i; j = j.R) {
				j.hide();
				j.C.size--;
			}
		}
	}

	protected void uncover() {
		for (Node i = U; i != this; i = i.U) {
			for (Node j = i.L; j != i; j = j.L) {
				j.C.size++;
				j.show();
			}
		}
		show();
	}
}
