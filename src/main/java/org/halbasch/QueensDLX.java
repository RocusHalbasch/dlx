package org.halbasch;

import java.util.LinkedList;

import org.halbasch.dlx.Column;
import org.halbasch.dlx.DLX;
import org.halbasch.dlx.Node;

public class QueensDLX extends DLX {
	public QueensDLX(int n) {
		initialize(n);
	}

	private void initialize(int n) {
		Column last = this;

		// For this example I didn't bother with "organ pipe ordering"
		Column[] rs = new Column[n];
		last = addColumns(n, "R", last, rs);
		Column[] fs = new Column[n];
		last = addColumns(n, "F", last, fs);

		// Here we pass null as last to break these columns from the search
		Column[] as = new Column[2 * n - 1];
		last = addColumns(2 * n - 1, "A", null, as);
		Column[] bs = new Column[2 * n - 1];
		last = addColumns(2 * n - 1, "B", last, bs);

		for (int r = 0; r < n; r++) {
			for (int f = 0; f < n; f++) {
				Node cur = rs[r].appendNode(new Node());
				cur = cur.insert(fs[f].appendNode(new Node()));
				cur = cur.insert(as[r + f].appendNode(new Node()));
				cur = cur.insert(bs[n - 1 - r + f].appendNode(new Node()));
			}
		}
	}

	private Column addColumns(int n, String id, Column last, Column[] cs) {
		for (int c = 0; c < n; c++) {
			Column cur = new Column(id + c);
			if (last != null)
				last.insert(cur);
			last = cur;
			cs[c] = cur;
		}
		return last;
	}

	private void printNodes() {
		Column cur = this;
		while (((String)((Column) cur.R()).getName()).startsWith("R")) {
			cur = (Column) cur.R();
			for (Node rank = cur.D(); rank != cur; rank = rank.D()) {
				System.out.print(rank.C().getName() + " ");
				System.out.print(rank.R().C().getName() + " ");
				System.out.print(rank.R().R().C().getName() + " ");
				System.out.println(rank.R().R().R().C().getName());
			}
		}
		System.out.println();
	}

	// Knuth's original algorithm was a recursive algorithm that searched the
	// entire solution space however our DLX class makes the algorithm iterative
	// and changes it to break and return when a solution is found so to make
	// this behave like his original we need a while loop
	public LinkedList<Node> searchAll() {
		LinkedList<Node> answer = search();
		int solution = 1;
		while (!answer.isEmpty()) {
			System.out.println("Solution #" + solution++ + ":");
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
		// Search for all solutions to the 4 Queens problem
		search4Queens();
		
		System.out.println("----------------------------------");

		// Create an 8 queens board
		QueensDLX queens8 = new QueensDLX(8);

		// Find and print two random solutions
		find2random(queens8);
		
		System.out.println("----------------------------------");
		
		// Search all solutions with queens on R0F2 and R3F3
		partialSolution(queens8);
	}

	private static void search4Queens() {
		// Create a 4 queens board
		QueensDLX queens4 = new QueensDLX(4);
		// Print the initial nodes this should match the dancing links paper
		System.out.println("Initialized Nodes:");
		queens4.printNodes();
		// Search for all solutions
		queens4.searchAll();
	}
	
	private static void find2random(QueensDLX queens8) {
		// Find a random solution
		LinkedList<Node> answer1 = queens8.randomSearch();
		queens8.undo(new LinkedList<>(answer1));
		
		// Find another
		LinkedList<Node> answer2 = queens8.randomSearch();
		queens8.undo(new LinkedList<>(answer2));
		
		System.out.println("Random Solution #1:");
		queens8.printSolution(answer1);
		System.out.println();
		
		System.out.println("Random Solution #2:");
		queens8.printSolution(answer2);
	}
	
	private static void partialSolution(QueensDLX queens8) {
		// Create a partial solution
		LinkedList<Node> partial = new LinkedList<>();
		Node r0f2 = queens8.R().D().D().D();
		queens8.removeRow(r0f2, partial);
		Node r3f3 = queens8.R().R().R().D().D().D();
		queens8.removeRow(r3f3, partial);

		// Print the partial solution
		System.out.println("Using the partial solution:");
		queens8.printSolution(partial);
		System.out.println();

		// Search for solutions
		queens8.searchAll();

		// Remove the partial solution
		queens8.undo(partial);
	}
}
