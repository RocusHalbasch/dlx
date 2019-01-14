package org.halbasch;

import java.util.LinkedList;

import org.halbasch.dlx.Column;
import org.halbasch.dlx.DLX;
import org.halbasch.dlx.Node;

public class ExactCoverDLX extends DLX {
	public ExactCoverDLX(boolean optimized, int[][] matrix) {
		super(optimized);
		initialize(matrix);
	}

	private void initialize(int[][] matrix) {
		final int width = matrix[0].length;
		final int height = matrix.length;

		// Create the column headers
		Column last = this;
		for (int c = 0; c < width; c++) {
			Object name = (char) ('A' + c);
			Column cur = new Column(name);
			last.insert(cur);
			last = cur;
		}

		// Column by column insert the nodes represented by 1's in the matrix
		Node[] rightMost = new Node[height];
		Column curCol = (Column) R();
		for (int col = 0; col < width; col++) {
			for (int row = 0; row < height; row++) {
				if (matrix[row][col] == 1) {
					Node cur = curCol.appendNode(new Node());
					Node rm = rightMost[row];
					if (rm != null)
						rm.insert(cur);
					rightMost[row] = cur;
				}
			}
			curCol = (Column) curCol.R();
		}
	}

	// Knuth's original algorithm was a recursive algorithm that searched the
	// entire solution space however our DLX class makes the algorithm iterative
	// and changes it to break and return when a solution is found so to make
	// this behave like his original we need a while loop
	public LinkedList<Node> searchAll() {
		LinkedList<Node> answer = search();
		while (!answer.isEmpty()) {
			printSolution(answer);
			System.out.println();
			next(answer);
		}
		return answer;
	}

	private void printSolution(LinkedList<Node> answer) {
		for (Node node : answer) {
			StringBuilder builder = new StringBuilder();
			Node start = node;
			Node cur = node;
			do {
				builder.append(cur.C().getName() + " ");
				cur = cur.R();
			} while (cur != start);
			System.out.println(builder);
		}
	}

	public static void main(String[] args) {
		// Matrix (3) from Donald Knuth's Dancing Links paper
		int[][] matrix_3 = { 
			{ 0, 0, 1, 0, 1, 1, 0 }, 
			{ 1, 0, 0, 1, 0, 0, 1 }, 
			{ 0, 1, 1, 0, 0, 1, 0 }, 
			{ 1, 0, 0, 1, 0, 0, 0 },
			{ 0, 1, 0, 0, 0, 0, 1 }, 
			{ 0, 0, 0, 1, 1, 0, 1 } 
		};
		
		System.out.println("Un-optimized");
		new ExactCoverDLX(false, matrix_3).searchAll();
		
		System.out.println("Optimized");
		new ExactCoverDLX(true, matrix_3).searchAll();
	}
}
