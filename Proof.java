import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Stack;

public class Proof {
	private TheoremSet theoremSet;
	private LineNumber currentLineNumber;
	private ArrayList<ProofLine> proofLines;
	private Stack<ProofLine> toProve;

	// ProofLine structure
	// class is public for tests (JUnit) but should be private
	public class ProofLine {
		private LineNumber lineNumber;
		private String reason;
		private LineNumber[] args = new LineNumber[3]; // fixed length = 3
		private Expression expr;

		public String toString() {
			String rtn = reason;
			for (int i = 0; i < args.length && args[i] != null; i++)
				rtn += " " + args[i];
			rtn += " " + expr;
			return rtn;
		}
	}

	// /Don't we need a constructor that takes no arguments?
	// Constructor
	public Proof(TheoremSet theorems) {
		theoremSet = theorems;
		currentLineNumber = new LineNumber();
		proofLines = new ArrayList<ProofLine>();
		toProve = new Stack<ProofLine>();
	}

	// returns the number of the next line of the proof to be filled in
	public LineNumber nextLineNumber() {
		return currentLineNumber.getNext();
	}

	// find the ProofLine which has a specific LineNumber in the array
	public ProofLine getProofLine(LineNumber l) {
		for (int i = 0; i < proofLines.size(); i++) {
			if (proofLines.get(i).lineNumber.equals(l))
				return proofLines.get(i);
		}
		return null; // should never happen
	}

	public ArrayList<Expression> getAssumptions(int linesize) {
		ArrayList<Expression> assumptions = new ArrayList<Expression>();
		for (ProofLine l : proofLines) {
			if ((l.lineNumber.getSize() == linesize)
					&& l.reason.equals("assume")) {
				assumptions.add(l.expr);
			}
		}
		return assumptions;
	}

	public Iterator<Expression> Assumptions(ArrayList<Expression> assumptions) {
		return assumptions.iterator();
	}

	// tries to extend the proof with the given input line
	public void extendProof(String x) throws IllegalLineException,
			IllegalInferenceException {
		try {
			ProofLine newProofLine = parser(x);
			// / why say print
			// if action is "print", we must print the proof so far
			if (newProofLine.reason.equals("print")) {
				System.out.println("print");
				currentLineNumber.setPrintCalled();
				Iterator<ProofLine> it = proofLines.iterator();
				while (it.hasNext()) {
					ProofLine pl = it.next();
					String rtn = pl.lineNumber + "\t" + pl.reason;
					for (int i = 0; i < pl.args.length && pl.args[i] != null; i++)
						rtn += " " + pl.args[i];
					rtn += " " + pl.expr;
					System.out.println(rtn);

				}
			} else {
				proofLines.add(newProofLine);
				check();
				if (newProofLine.reason.equals("show"))
					toProve.add(newProofLine);
				System.out.print(newProofLine.reason);
				int argsNumber = howManyArgs(newProofLine.reason);
				for (int i = 0; i < argsNumber; i++)
					System.out.print(" " + newProofLine.args[i]);
				System.out.println(" " + newProofLine.expr);
				if (newProofLine.reason.equals("show") && proofLines.size() > 1)
					currentLineNumber.setNewProof();
				else if (isSubProofComplete()) {
					currentLineNumber.setEndProof();
					toProve.remove(toProve.size() - 1);
				}
			}
		} catch (Exception exc) {
			currentLineNumber.setPrintCalled();
			proofLines.remove(proofLines.size()-1);
			throw exc;
		}
	}

	private int howManyArgs(String s) {
		String[] possibleReasons = { "show", "assume", "ic", "repeat", "mp",
				"mt", "co" };
		int pos;
		for (pos = 0; pos < possibleReasons.length; pos++)
			if (s.equals(possibleReasons[pos]))
				break;

		if (pos < 2 || pos >= 7)
			return 0;
		else if (pos < 4)
			return 1;
		else
			return 2;
	}

	// returns true if the most recent line matches the expression given in
	// the outermost show step and is not part of a subproof and returns false
	// otherwise
	public boolean isComplete() {
		return proofLines.size() > 1 && toProve.size() == 0;
	}

	// true if the last show is complete
	public boolean isSubProofComplete() {
		ProofLine lastToProve = toProve.peek();
		ProofLine lastLine = proofLines.get(proofLines.size() - 1);
		return !lastLine.reason.equals("show")
				&& !lastLine.reason.equals("assume")
				&& lastToProve.expr.equals(lastLine.expr);
	}

	// return a ProofLine from the parsed line
	// method is public for tests (JUnit) but should be private
	public ProofLine parser(String line) throws IllegalLineException {
		ProofLine proofLine = new ProofLine();
		Scanner scanner = new Scanner(line);

		try {
			// get & check NUMBER
			// LineNumber has its own tests
			proofLine.lineNumber = new LineNumber(currentLineNumber);

			// get & check REASON
			// empty line already tested before, no need to check hasNext()
			String[] possibleReasons = { "show", "assume", "ic", "repeat",
					"mp", "mt", "co" };
			boolean ok = false;

			proofLine.reason = scanner.next();
			if (proofLine.reason.equals("print")){
				return proofLine;
			}
			for (int i = 0; i < possibleReasons.length; i++)
				if (proofLine.reason.equals(possibleReasons[i])) {
					ok = true;
					break;
				}
			if (!ok && !theoremSet.contains(proofLine.reason))
				throw new IllegalLineException("Parser: \"" + line
						+ "\"\nUnknown reason : \"" + proofLine.reason + "\"");

			// get & check ARGS
			// depending of the REASON, we expect between 0 and 2 arguments
			// all arguments are line numbers
			// LineNumber(String s) will throw an exception itself if the string
			// is incorrect
			int argsNumber = howManyArgs(proofLine.reason);
			for (int i = 0; i < argsNumber; i++) {
				if (!scanner.hasNext())
					throw new IllegalLineException("Parser: \"" + line
							+ "\"\nReason \"" + proofLine.reason
							+ "\" requires " + argsNumber + "arguments");
				proofLine.args[i] = new LineNumber(scanner.next());
			}
			// get & check EXPR
			if (!scanner.hasNext())
				throw new IllegalLineException("Parser: \"" + line
						+ "\"\nAn expression is expected as last argument");
			proofLine.expr = new Expression(scanner.next());

			// check this is the end of the line
			if (scanner.hasNext())
				throw new IllegalLineException("Parser: \"" + line
						+ "\"\nThis action doesn't need more arguments");
		} catch (Exception e) {
			throw e;
		} finally {
			scanner.close();
		}
		return proofLine;
	}

	// operate the action and determine the expression
	public void check() throws IllegalInferenceException, IllegalLineException {
		ProofLine lastProofLine = proofLines.get(proofLines.size() - 1);
		LineNumber ln = lastProofLine.lineNumber;
		for (LineNumber l : lastProofLine.args) {
			if (l != null) {
				if (!ln.isValidLineRef(l)){
					throw new IllegalInferenceException("Out of scope reference");}
			}
		}
		Expression expr = lastProofLine.expr;
		Expression[] arguments = new Expression[2];
		for (int i = 0; i < 2 && lastProofLine.args[i] != null; i++)
			arguments[i] = getProofLine(lastProofLine.args[i]).expr;

		if (lastProofLine.reason.equalsIgnoreCase("mp")) {
			checkMP(expr, arguments);
		} else if (lastProofLine.reason.equalsIgnoreCase("mt")) {
			checkMT(expr, arguments);
		} else if (lastProofLine.reason.equals("ic")) {
			checkIC(expr, arguments);
		} else if (lastProofLine.reason.equals("co")) {
			checkCO(expr, arguments);
		} else if (lastProofLine.reason.equals("assume")) {
			checkAssume();
		} else if (lastProofLine.reason.equals("repeat")) {
			checkRepeat();
		} else if (theoremSet.contains(lastProofLine.reason)){
			if (!theoremSet.get(lastProofLine.reason).match(lastProofLine.expr)){
				throw new IllegalInferenceException("Theorem and given expression do not match");
			}
		}
	}

	public void checkMP(Expression expr, Expression[] args)
			throws IllegalInferenceException {
		if (args[0].toString().length() > args[1].toString().length()) {
			// switches them
			Expression temp = args[0];
			args[0] = args[1];
			args[1] = temp;
		}
		if (!args[1].isRootNodeImplies() || !args[1].leftNodeMatch(args[0])
				|| !args[1].rightNodeEquals(expr)) {
			throw new IllegalInferenceException("Not valid use of mp");
		}
	}

	public void checkMT(Expression expr, Expression[] args)
			throws IllegalInferenceException {
		if (args[0].toString().length() > args[1].toString().length()) {
			// switches them
			Expression temp = args[0];
			args[0] = args[1];
			args[1] = temp;
		}
		if (!args[0].isRootNodeNot() || !args[1].isRootNodeImplies()
				|| !args[1].rightNodeMatch(args[0].getRight())
				|| !args[1].leftNodeEquals(expr.getRight())) {
			throw new IllegalInferenceException("Mt disagree");

		}
	}

	public void checkCO(Expression expr, Expression[] args)
			throws IllegalInferenceException, IllegalLineException {

		ProofLine lastProofLine = proofLines.get(proofLines.size() - 1);
		Expression tempEx = new Expression("~" + args[0].toString());
		if (!tempEx.equals(args[1])) {
			throw new IllegalInferenceException(
					"Arguments must be contradications");
		} else if (!args[0].isRootNodeNot()) {
			Expression e = args[0];
			args[0] = args[1];
			args[1] = e;
//		} else if (!(args[0].rightNodeMatch(args[1])) {
//			throw new IllegalInferenceException(
//					"Argument root statements are not equivalent");
		} else if (!expr.equals(LastLeveltoShow(lastProofLine.lineNumber))) {
			throw new IllegalInferenceException(
					"Not what you're trying to show");
		}
	}

	public void checkIC(Expression expr, Expression[] args)
			throws IllegalInferenceException {
		ProofLine lastProofLine = proofLines.get(proofLines.size() - 1);
		Expression implied = lastProofLine.expr.getRight();
		Expression implier = lastProofLine.expr.getLeft();

		if (!expr.isRootNodeImplies()) {
			throw new IllegalInferenceException("Needs to be an implication");
		}
		if (!implied.equals(args[0])) {
			throw new IllegalInferenceException(
					"Implication is not implied from given line number");
		}
		Iterator<Expression> assumptions = Assumptions(getAssumptions(lastProofLine.lineNumber
				.getSize()));
		boolean implication = false;
		while (assumptions.hasNext()) {
			if (implier.equals(assumptions.next())) {
				implication = true;
			}
		}
		if (!implication) {
			throw new IllegalInferenceException(
					"Appropiate assumption was never made");
		}
	}

	public void checkAssume() throws IllegalInferenceException {
		ProofLine lastProofLine = proofLines.get(proofLines.size() - 1);
		if (!proofLines.get(proofLines.size() - 2).reason.equals("show")) {
			throw new IllegalInferenceException("Must follow a show statement");
		}
		if (!LastLeveltoShow(lastProofLine.lineNumber).isRootNodeImplies()) {
			Expression e = new Expression(new Expression.TreeNode("~", null,
					LastLeveltoShow(lastProofLine.lineNumber).getTree()));
			if (!e.equals(lastProofLine.expr)) {
				throw new IllegalInferenceException("Not a valid assumption");
			}
		} else if (!LastLeveltoShow(lastProofLine.lineNumber).leftNodeMatch(
				lastProofLine.expr)) {
			throw new IllegalInferenceException("Not a valid assumption");
		}
	}

	public void checkRepeat() throws IllegalInferenceException {
		ProofLine lastProofLine = proofLines.get(proofLines.size() - 1);
		LineNumber l1 = lastProofLine.args[0];
		Expression e1 = lastProofLine.expr;
		if (!getProofLine(l1).expr.equals(e1)) {
			throw new IllegalInferenceException(
					"Requested implication not avaiable at given line");
		}
		if (l1.getNumbers().size() == 1
				&& l1.getNumbers().get(0).equals(new Integer(1))) {
			throw new IllegalInferenceException("Cannot repeat unproven steps");
		}
		boolean match = true;
		for (int i = 0; i < l1.getNumbers().size(); i++) {
			if (!l1.getNumbers().get(i)
					.equals(lastProofLine.lineNumber.getNumbers().get(i))) {
				match = false;
			}
		}
		if (match) {
			throw new IllegalInferenceException("Cannot repeat unproven steps");
		}
	}

	// returns printable version of the legal steps typed so far
	public String toString() {
		String rtn = "";
		Iterator<ProofLine> it = proofLines.iterator();
		while (it.hasNext())
			rtn += it.next();
		return rtn;
	}

	public Expression LastLeveltoShow(LineNumber l) {
		Expression lastshow = null;
		int linesize = l.getSize() - 1;
		if (linesize == 0) {
			linesize = 1;
		}
		for (ProofLine pl : proofLines) {
			if ((pl.lineNumber.getSize() == linesize)
					&& pl.reason.equals("show")) {
				lastshow = pl.expr;
			}
		}
		return lastshow;
	}

}	