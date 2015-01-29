import java.util.HashMap;
import java.util.Map;

public class TheoremSet {
	private Map<String, Expression> map;

	public TheoremSet() {
		map = new HashMap<String, Expression>();
	}

	public Expression put(String s, Expression e) {
		return map.put(s, e);
	}

	public int howManyArgs(String name) {
		return 0;
	}

	public Expression get(String s) {
		return map.get(s);
	}

	public boolean contains(String name) {
		return map.containsKey(name);
	}
}