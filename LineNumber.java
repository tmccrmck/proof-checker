import java.util.ArrayList;

public class LineNumber {
	private ArrayList<Integer> numbers;
	private boolean newProof = false;
	private boolean endProof = false;

	public LineNumber(){
		numbers = new ArrayList<Integer>();
		numbers.add(0);
	}
	
	
	@SuppressWarnings("unchecked")
	public LineNumber(LineNumber l) {
		numbers = (ArrayList<Integer>) l.numbers.clone();
	}

	public LineNumber(String s) throws IllegalLineException {
		numbers = new ArrayList<Integer>();
		if (s.isEmpty())
			throw new IllegalLineException("LineNumber: empty line");
		for (int i = 0; i < s.length(); i++) {
			int nextPeriodPos = s.indexOf('.', i);
			nextPeriodPos += nextPeriodPos == -1 ? s.length() + 1 : 0; 
			int currentNumber;
			try {
				currentNumber = Integer.parseInt(s.substring(i, nextPeriodPos));
			} catch (NumberFormatException e) {
				throw new IllegalLineException("LineNumber: \"" + s + "\" is not a line number");
			}
			if (currentNumber < 1)
				throw new IllegalLineException("LineNumber: \"" + s + "\" is not a line number");
			numbers.add(currentNumber);
			i = nextPeriodPos;
		}
	}

	public LineNumber getNext(){
		if (newProof) {
			newProof = false;
			numbers.add(0);
		} else if (endProof) {
			endProof = false;
			numbers.remove(numbers.size() - 1);
		}
		int index = numbers.size() - 1;
		numbers.set(index, numbers.get(index) + 1);

		return this;
	}

	// return the first line number of the (sub)proof given the last one
	public static LineNumber getLastShowLineNumber(LineNumber lastLine) {
		LineNumber lastShowLine = new LineNumber(lastLine);
		lastShowLine.numbers.remove(lastShowLine.numbers.size() - 1);
		int index = lastShowLine.numbers.size() - 1;
		return lastShowLine;
	}

	public int getSize() {
		return numbers.size();
	}
	
	public void setNewProof() {
		newProof = true;
	}

	public void setEndProof() {
		assert numbers.size() > 1;
		endProof = true;
	}

	public String toString(){
		String rtn = "";
		rtn += numbers.get(0);
		for (int i = 1; i < numbers.size(); i++)
			rtn += '.' + numbers.get(i).toString();
		return rtn;
	}
	
	public boolean equals(Object l) {
		return this.toString().equals(l.toString());
	} 
}