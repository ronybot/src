package marina_CKY;




import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;



/**
 * Simple implementation of a PCFG grammar, offering the ability to look up
 * rules by their child symbols. Rule probability estimates are just relative
 * frequency estimates off of training trees.
 */
public class MarinaGrammar {
	// flag for a full grammar which supports all lookup
	boolean full;
	// rules, which is a grammar all about
	List<BinaryRule> binaryRules = new ArrayList<BinaryRule>();
	List<UnaryRule> unaryRules = new ArrayList<UnaryRule>();
	// lookup tables
	Map<String, List<BinaryRule>> binaryRulesByLeftChild = new HashMap<String, List<BinaryRule>>();
	Map<String, List<BinaryRule>> binaryRulesByParent = new HashMap<String, List<BinaryRule>>();
	Map<String, List<UnaryRule>> unaryRulesByParent = new HashMap<String, List<UnaryRule>>();
	// for EM algorithm
	Map<BinaryRule, Double> binaryRulesMap = new HashMap<BinaryRule, Double>();
	Map<UnaryRule, Double> unaryRulesMap = new HashMap<UnaryRule, Double>();
	Map<BinaryRule, BinaryRule> selfBinaryMap = new HashMap<BinaryRule, BinaryRule>();
	Map<UnaryRule, UnaryRule> selfUnaryMap = new HashMap<UnaryRule, UnaryRule>();

	// original Counter
	Counter<UnaryRule> unaryRuleCounter = new Counter<UnaryRule>();
	Counter<BinaryRule> binaryRuleCounter = new Counter<BinaryRule>();

	// states for statistic
	Set<String> states = new HashSet<String>();

	// lookup methods
	public List<BinaryRule> getBinaryRulesByLeftChild(String leftChild) {
		return Utils.getValueList(binaryRulesByLeftChild, leftChild);
	}

	public List<BinaryRule> getBinaryRulesByParent(String parent) {
		return Utils.getValueList(binaryRulesByParent, parent);
	}

	public List<BinaryRule> getBinaryRules() {
		return binaryRules;
	}

	public List<UnaryRule> getUnaryRulesByParent(String parent) {
		return Utils.getValueList(unaryRulesByParent, parent);
	}

	public List<UnaryRule> getUnaryRules() {
		return unaryRules;
	}

	public double getBinaryScore(String parent, String left, String right) {
		Double d = binaryRulesMap.get(new BinaryRule(parent, left, right));
		return d == null ? 0.0 : d.doubleValue();
	}

	public double getUnaryScore(String parent, String child) {
		Double d = unaryRulesMap.get(new UnaryRule(parent, child));
		return d == null ? 0.0 : d.doubleValue();
	}

	public Set<String> getStates() {
		return states;
	}

	// lookup methods end

	public String toString() {
		StringBuilder sb = new StringBuilder();
		List<String> ruleStrings = new ArrayList<String>();
		for (BinaryRule binaryRule : binaryRules)
			ruleStrings.add(binaryRule.toString());
		for (UnaryRule unaryRule : unaryRules)
			ruleStrings.add(unaryRule.toString());
		for (String ruleString : Utils.sort(ruleStrings)) {
			sb.append(ruleString);
			sb.append("\n");
		}
		return sb.toString();
	}

	// constructor helper
	private void addBinary(BinaryRule binaryRule) {
		states.add(binaryRule.getParent());
		states.add(binaryRule.getLeftChild());
		states.add(binaryRule.getRightChild());
		binaryRules.add(binaryRule);
		// optional
		if (full) {
			addBinaryLookup(binaryRule);
		}
		// selfBinaryMap.put(binaryRule, binaryRule);
	}

	private void addBinaryLookup(BinaryRule binaryRule) {
		Utils.addToValueList(binaryRulesByParent,
				binaryRule.getParent(), binaryRule);
		Utils.addToValueList(binaryRulesByLeftChild,
				binaryRule.getLeftChild(), binaryRule);
	}

	private void addUnary(UnaryRule unaryRule) {
		states.add(unaryRule.getParent());
		states.add(unaryRule.getChild());
		unaryRules.add(unaryRule);
		if (full) {
			addUnaryLookup(unaryRule);
		}
		// selfUnaryMap.put(unaryRule, unaryRule);
	}

	private void addUnaryLookup(UnaryRule unaryRule) {
		Utils.addToValueList(unaryRulesByParent,
				unaryRule.getParent(), unaryRule);
	}

	protected MarinaGrammar() {
	}

	/**
	 * Construct a grammar with binary, unary rules. Set the probability as
	 * relative frequency.
	 * 
	 * @param unaryRuleCounter
	 *            Counter for all unary rules
	 * @param binaryRuleCounter
	 *            Counter for all binary rules
	 */
	public MarinaGrammar(Counter<UnaryRule> unaryRuleCounter,
			Counter<BinaryRule> binaryRuleCounter) {
		this(unaryRuleCounter, binaryRuleCounter, true);
	}

	public MarinaGrammar(Counter<UnaryRule> unaryRuleCounter,
			Counter<BinaryRule> binaryRuleCounter, boolean isFull) {
		full = isFull;
		this.binaryRuleCounter = binaryRuleCounter;
		this.unaryRuleCounter = unaryRuleCounter;
		// unaryRuleCounter = Counters.cleanCounter(unaryRuleCounter);
		// binaryRuleCounter = Counters.cleanCounter(binaryRuleCounter);
		// construct symbolCounter
		Counter<String> symbolCounter = new Counter<String>();
		for (UnaryRule unaryRule : unaryRuleCounter.keySet()) {
			symbolCounter.incrementCount(unaryRule.getParent(),
					unaryRuleCounter.getCount(unaryRule));
		}
		for (BinaryRule binaryRule : binaryRuleCounter.keySet()) {
			symbolCounter.incrementCount(binaryRule.getParent(),
					binaryRuleCounter.getCount(binaryRule));
		}

		// set score and build lookup table
		for (UnaryRule unaryRule : unaryRuleCounter.keySet()) {
			double unaryProbability = unaryRuleCounter.getCount(unaryRule)
					/ symbolCounter.getCount(unaryRule.getParent());
			unaryRule.setScore(unaryProbability);
			addUnary(unaryRule);
		}
		for (BinaryRule binaryRule : binaryRuleCounter.keySet()) {
			double binaryProbability = binaryRuleCounter.getCount(binaryRule)
					/ symbolCounter.getCount(binaryRule.getParent());
			binaryRule.setScore(binaryProbability);
			addBinary(binaryRule);
		}

		// set relative probability for EM algorithm
		if (!isFull) {
			Counter<BinaryRule> baseBinaryCounter = new Counter<BinaryRule>();
			Counter<UnaryRule> baseUnaryCounter = new Counter<UnaryRule>();
			for (UnaryRule unaryRule : unaryRuleCounter.keySet()) {
				baseUnaryCounter.incrementCount(makeBaseRule(unaryRule),
						unaryRuleCounter.getCount(unaryRule));
			}
			for (BinaryRule binaryRule : binaryRuleCounter.keySet()) {
				baseBinaryCounter.incrementCount(makeBaseRule(binaryRule),
						binaryRuleCounter.getCount(binaryRule));
			}

			// set score and build lookup table
			for (UnaryRule unaryRule : unaryRuleCounter.keySet()) {
				double unaryProbability = unaryRuleCounter.getCount(unaryRule)
						/ baseUnaryCounter.getCount(makeBaseRule(unaryRule));
				unaryRulesMap.put(unaryRule, unaryProbability);
			}
			for (BinaryRule binaryRule : binaryRuleCounter.keySet()) {
				double binaryProbability = binaryRuleCounter
						.getCount(binaryRule)
						/ baseBinaryCounter.getCount(makeBaseRule(binaryRule));
				binaryRulesMap.put(binaryRule, binaryProbability);
			}
		}
	}

	private UnaryRule makeBaseRule(UnaryRule unaryRule) {
		return new UnaryRule(unaryRule.getParent(),
				GrammarSpliter.getBaseState(unaryRule.getChild()));
	}

	private BinaryRule makeBaseRule(BinaryRule binaryRule) {
		return new BinaryRule(binaryRule.getParent(),
				GrammarSpliter.getBaseState(binaryRule.getLeftChild()),
				GrammarSpliter.getBaseState(binaryRule.getRightChild()));
	}

	public void becomeFull() {
		// set score and build lookup table
		for (UnaryRule unaryRule : unaryRules) {
			addUnaryLookup(unaryRule);
		}
		for (BinaryRule binaryRule : binaryRules) {
			addBinaryLookup(binaryRule);
		}

	}

	/**
	 * Use intern String in the rule
	 */
	public static UnaryRule makeUnaryRule(MarinaTree<String> tree) {
		// return new UnaryRule(tree.getLabel().intern(), tree.getChildren()
		// .get(0).getLabel().intern());
		return new UnaryRule(tree.getLabel(), tree.getChildren().get(0)
				.getLabel());
	}

	/**
	 * Use intern String in the rule
	 */
	public static BinaryRule makeBinaryRule(MarinaTree<String> tree) {
		// return new BinaryRule(tree.getLabel().intern(), tree.getChildren()
		// .get(0).getLabel().intern(), tree.getChildren().get(1)
		// .getLabel().intern());
		return new BinaryRule(tree.getLabel(), tree.getChildren().get(0)
				.getLabel(), tree.getChildren().get(1).getLabel());
	}

	public static interface GrammarBuilder {
		public MarinaGrammar buildGrammar();
	}

	public static class DefaultGrammarBuilder implements GrammarBuilder {
		List<MarinaTree<String>> trainTrees;
		boolean isFull;

		public DefaultGrammarBuilder(List<MarinaTree<String>> trainTrees,
				boolean isFull) {
			this.trainTrees = trainTrees;
			this.isFull = isFull;
		}

		public DefaultGrammarBuilder(List<MarinaTree<String>> trainTrees) {
			this(trainTrees, true);
		}
   
		@Override
		public MarinaGrammar buildGrammar() {
			Counter<UnaryRule> unaryRuleCounter = new Counter<UnaryRule>();
			Counter<BinaryRule> binaryRuleCounter = new Counter<BinaryRule>();
			for (MarinaTree<String> trainTree : trainTrees) {
				tallyTree(trainTree, unaryRuleCounter, binaryRuleCounter);
			}
			return new MarinaGrammar(unaryRuleCounter, binaryRuleCounter, isFull);
		}

		private void tallyTree(MarinaTree<String> tree,
				Counter<UnaryRule> unaryRuleCounter,
				Counter<BinaryRule> binaryRuleCounter) {
			if (tree.isLeaf())
				return;
			if (tree.isPreTerminal())
				return;
			if (tree.getChildren().size() == 1) {
				UnaryRule unaryRule = makeUnaryRule(tree);
				unaryRuleCounter.incrementCount(unaryRule, 1.0);
			}
			if (tree.getChildren().size() == 2) {
				BinaryRule binaryRule = makeBinaryRule(tree);
				binaryRuleCounter.incrementCount(binaryRule, 1.0);
			}
			if (tree.getChildren().size() < 1 || tree.getChildren().size() > 2) {
				throw new RuntimeException(
						"Attempted to construct a Grammar with an illegal tree (unbinarized?): "
								+ tree);
			}
			for (MarinaTree<String> child : tree.getChildren()) {
				tallyTree(child, unaryRuleCounter, binaryRuleCounter);
			}
		}
	}

	public List<BinaryRule> getSyntacticRules() {
		
		return getBinaryRules();
	}
}

