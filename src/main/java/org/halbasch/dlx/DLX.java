package org.halbasch.dlx;

import java.util.LinkedList;
import java.util.Random;

/**
 * An iterative implementation of Donald Knuth's dancing links algorithm. Unlike
 * Knuth's algorithm {@code search()} has been modified to be iterative and to
 * return once a solution is found. You can then use {@code next()} along with
 * the previous solution to continue where you left off. This makes it easy to
 * handle each solution.
 * <p>
 * It is important to note that the answer provided by a search matches the
 * state of the {@code Node}s after it returns. If you want to return the nodes
 * to their originally initialized state you need to perform an {@code undo()}
 * with that last answer.
 * <p>
 * This abstract class does not implement any code to initialize the
 * {@code Column} headers or {@code Node}s. It is up to subclasses to handle the
 * initialization of these for the particular problem being implemented. It is
 * important to remember when building them that {@code this} instance is the
 * root {@code Column} object <i>h</i>.
 * 
 * @author rhalbasch
 * 
 */
public abstract class DLX extends Column {
	protected Random rand = new Random();
	protected final boolean optimized;

	protected DLX() {
		this(true);
	}

	protected DLX(boolean optimized) {
		this.optimized = optimized;
	}

	private Column selectMinColumn() {
		int min = Integer.MAX_VALUE;
		Column ret = null;
		for (Column c = (Column) R; c != this; c = (Column) c.R) {
			if (c.getSize() < min) {
				min = c.getSize();
				ret = c;
			}
		}
		return ret;
	}

	private Column selectNextColumn() {
		return (Column) R;
	}

	private enum Action {
		NEW, PROC, NEXT;
	}

	/**
	 * Searches for a solution it will return when the first solution is found. Each
	 * {@code Node} in the answer represents a row in the answer. If the answer is
	 * empty then no solution was found.
	 * 
	 * @return A {@code LinkedList} of {@code Node}s representing the answer or an
	 *         empty {@code LinkedList} if no answer can be found
	 */
	protected LinkedList<Node> search() {
		return next(new LinkedList<>());
	}

	/**
	 * Continues a previous search where it left off looking for the next solution.
	 * Please note this method will modify the {@code LinkedList} that is passed in
	 * as a parameter. If you want to preserve it for later you must copy it before
	 * passing it to this method.
	 * 
	 * @return {@code LinkedList} of {@code Node}s representing the answer or an
	 *         empty {@code LinkedList} if no answer can be found
	 */
	protected LinkedList<Node> next(LinkedList<Node> answer) {
		Action action = Action.NEW;
		if (R == this)
			action = Action.NEXT;
		while (true) {
			if (action == Action.NEW) {
				Column c;
				if (optimized)
					c = selectMinColumn();
				else
					c = selectNextColumn();
				c.cover();
				answer.addLast(c.D);
				action = Action.PROC;
			} else if (action == Action.NEXT) {
				if (answer.isEmpty())
					return answer;
				for (Node n = answer.peekLast().L; n != answer.peekLast(); n = n.L)
					n.C.uncover();
				answer.addLast(answer.removeLast().D);
				action = Action.PROC;
			} else if (action == Action.PROC) {
				if (answer.peekLast() instanceof Column) {
					((Column) answer.removeLast()).uncover();
					action = Action.NEXT;
				} else {
					for (Node n = answer.peekLast().R; n != answer.peekLast(); n = n.R)
						n.C.cover();
					if (R == this)
						return answer;
					action = Action.NEW;
				}
			}
		}
	}

	/**
	 * Searches for a valid solution like {@code search()} but randomizes the order
	 * of choices so it will result in a different solution any time it is called.
	 * Please note this method assumes the grid is in a clean state so undo any
	 * previous searches before calling it.
	 * 
	 * @return {@code LinkedList} of {@code Node}s representing the answer or an
	 *         empty {@code LinkedList} if no answer can be found
	 */
	protected LinkedList<Node> randomSearch() {
		LinkedList<Node> answer = new LinkedList<>();
		LinkedList<LinkedList<Node>> options = new LinkedList<>();
		Action action = Action.NEW;
		while (true) {
			if (action == Action.NEW) {
				Column c = selectMinColumn();
				c.cover();
				Node node = c;
				options.addLast(new LinkedList<>());
				for (int i = 0; i < c.getSize(); i++) {
					node = node.D;
					options.peekLast().addLast(node);
				}
				if (options.peekLast().isEmpty()) {
					answer.addLast(c);
				} else {
					int index = rand.nextInt(options.peekLast().size());
					answer.addLast(options.peekLast().remove(index));
				}
				action = Action.PROC;
			} else if (action == Action.NEXT) {
				if (answer.isEmpty())
					return answer;
				for (Node n = answer.peekLast().L; n != answer.peekLast(); n = n.L)
					n.C.uncover();
				if (options.peekLast().isEmpty()) {
					answer.addLast(answer.removeLast().C);
				} else {
					answer.removeLast();
					int index = rand.nextInt(options.peekLast().size());
					answer.addLast(options.peekLast().remove(index));
				}
				action = Action.PROC;
			} else if (action == Action.PROC) {
				if (answer.peekLast() instanceof Column) {
					((Column) answer.removeLast()).uncover();
					options.removeLast();
					action = Action.NEXT;
				} else {
					for (Node n = answer.peekLast().R; n != answer.peekLast(); n = n.R)
						n.C.cover();
					if (R == this)
						return answer;
					action = Action.NEW;
				}
			}
		}
	}

	/**
	 * Returns the state of the nodes to a previous state by undoing a
	 * {@code LinkedList} of previously chosen rows, such as an answer provided by a
	 * previous call to {@code search()} or {@code next()}. Please note this method
	 * will modify the {@code LinkedList} that is passed in as a parameter. If you
	 * want to preserve it for later you must copy it before passing it to this
	 * method.
	 * 
	 * @param stack
	 *            {@code LinkedList} of rows to undo
	 * @return the empty {@code LinkedList} after it has been undone
	 */
	protected LinkedList<Node> undo(LinkedList<Node> stack) {
		while (stack.size() > 0) {
			for (Node n = stack.peekLast().L; n != stack.peekLast(); n = n.L)
				n.C.uncover();
			stack.removeLast().C.uncover();
		}
		return stack;
	}

	/**
	 * Manually removes a row from the nodes, and adds it to a stack. This can be
	 * useful for finding solutions given a partial solution.
	 * 
	 * @param node
	 *            {@code Node} representing the row to remove
	 * @param stack
	 *            {@code LinkedList} to add the node too
	 * @return the removed {@code Node}
	 */
	protected LinkedList<Node> removeRow(Node node, LinkedList<Node> stack) {
		node.C.cover();
		for (Node n = node.R; n != node; n = n.R)
			n.C.cover();
		stack.addLast(node);
		return stack;
	}
}
