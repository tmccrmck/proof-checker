import java.util.ArrayList;

public class Expression {
	private TreeNode tree;

	public TreeNode getTree() {
		return tree;
	}

	public Expression(String s) throws IllegalLineException {
		tree = TreeNode.exprTree(s);
	}

	public Expression(TreeNode t) {
		tree = t;
	}

	public static class TreeNode {
		private String myItem;
		private TreeNode myLeft;
		private TreeNode myRight;

		public TreeNode() {
			myItem = null;
			myLeft = null;
			myRight = null;
		}

		public TreeNode(String s) {
			myItem = s;
			myLeft = null;
			myRight = null;
		}

		public TreeNode(String s, TreeNode l, TreeNode r) {
			myItem = s;
			myLeft = l;
			myRight = r;
		}

		public boolean hasChildren() {
			return myLeft != null && myRight != null;
		}

		public static TreeNode exprTree(String s) throws IllegalLineException {
			return exprTreeHelper(s);
		}

		private static TreeNode exprTreeHelper(String expr)
				throws IllegalLineException {
			if (expr.charAt(0) != '(' && isCorrect(expr))
				return new TreeNode(expr);
			else if (expr.charAt(0) == '~') {
				return new TreeNode("~", null,
						exprTreeHelper(expr.substring(1)));
			} else {
				if (expr.length() > 1 && expr.charAt(1) == ')')
					throw new IllegalLineException("exprTree: \"" + expr
							+ "\"\nBad parenthesis");
				int nesting = 0;
				int opPos = 0;
				boolean isInfer = false;
				for (int k = 1; k < expr.length() - 1; k++) {
					char c = expr.charAt(k);
					if (c == '(')
						nesting++;
					if (c == ')')
						nesting--;
					if (nesting == 0 && (c == '&' || c == '|')) {
						opPos = k;
						break;
					}
					if (nesting == 0 && k != expr.length() - 1
							&& expr.substring(k, k + 2).equals("=>")) {
						opPos = k;
						isInfer = true;
						break;
					}
				}
				if (opPos == 0)
					throw new IllegalLineException("exprTree: \"" + expr
							+ "\" is an incorrect expression");
				String opnd1 = expr.substring(1, opPos);
				String opnd2 = expr.substring(opPos + 1 + (isInfer ? 1 : 0),
						expr.length() - 1);
				String op = expr
						.substring(opPos, opPos + 1 + (isInfer ? 1 : 0));
				if ((opnd1.isEmpty() && !op.equals("~")) || opnd2.isEmpty())
					throw new IllegalLineException("exprTree: \"" + expr
							+ "\" is an incorrect expression");
				return new TreeNode(op, exprTreeHelper(opnd1),
						exprTreeHelper(opnd2));
			}
		}
	}

	// check if the expression is a correct variable name
	public static boolean isCorrect(String expr) {
		return (expr.length() == 1 && expr.charAt(0) >= 'a' && expr.charAt(0) <= 'z');
	}

	public boolean isRootNodeImplies() {
		return tree.myItem.equals("=>");
	}

	public boolean isRootNodeNot() {
		return tree.myItem.equals("~");
	}

	// matching for theorems
	public boolean match(Expression e) {
//		ArrayList<String> eLst = new ArrayList<String>();
//		ArrayList<String> cLst = new ArrayList<String>();
//		if (helperMatch(this.tree, e.tree) == false) {
//			return false;
//		}
		return helperMatch(this.tree, e.tree);
	}

	public boolean helperMatchNew(TreeNode curr, TreeNode t,
			ArrayList<String> cLst, ArrayList<String> tLst) {
		if (!t.hasChildren() || !curr.hasChildren()) {
			System.out.println(Expression.toStringHelper(curr));
			System.out.println(Expression.toStringHelper(t));
			cLst.add(Expression.toStringHelper(curr));
			tLst.add(Expression.toStringHelper(t));
		}
		return helperMatchNew(curr.myLeft, t.myLeft, cLst, tLst)
				&& helperMatchNew(curr.myRight, t.myRight, cLst, tLst);

	}

	public boolean matchArray(ArrayList<String> cLst, ArrayList<String> tLst) {
		return true;
	}

	public static boolean helperMatch(TreeNode curr, TreeNode t) {
		if ((t.myItem.equals("~") && !curr.myItem.equals("~"))
				|| (!t.myItem.equals("~") && curr.myItem.equals("~"))) {
			return false;
		}
		if ((t.myItem.equals("~") && curr.myItem.equals("~"))) {
			return helperMatch(t.myRight, curr.myRight);
		}
		if (!t.hasChildren() || !curr.hasChildren())
			return true;
		else if (!t.myItem.equals(curr.myItem)) {
			return false;
		}
		return helperMatch(curr.myLeft, t.myLeft)
				&& helperMatch(curr.myRight, t.myRight);
	}

	public String toString() {
		return Expression.toStringHelper(tree);
	}

	private static String toStringHelper(TreeNode node) {
		String rtn = "";
		boolean parenthesis = node.myItem.equals("&")
				|| node.myItem.equals("|") || node.myItem.equals("=>");
		if (node.myLeft != null)
			rtn += (parenthesis ? "(" : "") + toStringHelper(node.myLeft);
		rtn += node.myItem;
		if (node.myRight != null)
			rtn += toStringHelper(node.myRight) + (parenthesis ? ")" : "");
		return rtn;
	}

	public boolean leftNodeEquals(Expression e) {
		return toStringHelper(tree.myLeft).equals(e.toString());
	}

	public boolean rightNodeEquals(Expression e) {
		return toStringHelper(tree.myRight).equals(e.toString());
	}

	public boolean rightNodeMatch(Expression e) {
		return helperMatch(tree.myRight, e.tree);
	}

	public boolean leftNodeMatch(Expression e) {
		return helperMatch(tree.myLeft, e.tree);
	}

	public Expression getRight() {
		return new Expression(tree.myRight);

	}

	public Expression getLeft() {
		return new Expression(tree.myLeft);

	}

	public boolean equals(Object o) {
		Expression newo = null;
		Expression newThis = null;
		if (((Expression) o).isRootNodeNot()
				&& ((Expression) o).getRight().isRootNodeNot()) {
			newo = ((Expression) o).getRight().getRight();
		}
		if ((this.isRootNodeNot() && this.getRight().isRootNodeNot())) {
			newThis = this.getRight().getRight();
		}
		if (newo != null || newThis != null) {
			if (newo == null) {
				return newThis.toString().equals(o.toString());
			}
			if (newThis == null) {
				return this.toString().equals(newo.toString());
			}
			return newThis.toString().equals(newo.toString());
		}
		return this.toString().equals(o.toString());
	}

	public int depth() {
		if (tree == null) {
			return 0;
		}
		return depthhelper(tree);
	}

	public int depthhelper(TreeNode t) {
		if (t == null) {
			return 0;
		}
		return 1 + Math.max(depthhelper(t.myLeft), depthhelper(t.myRight));
	}
}