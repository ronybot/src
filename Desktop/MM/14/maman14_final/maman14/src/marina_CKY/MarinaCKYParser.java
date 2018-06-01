package marina_CKY;

import grammar.Grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tree.Node;
import tree.Tree;



public class MarinaCKYParser {

	Lexicon lexicon;
	Grammar grammar;
	MarinaGrammar gram1;
	private UnaryClosure uc;
	

	public MarinaCKYParser(List<Tree> annotatedTrainTrees, Grammar gram, List<UnaryRule> rules) {

		System.out.print("Annotating / binarizing training trees ... ");
	//	List<MarinaTree<String>> annotatedTrainTrees = annotateTrees(trainTrees);
		System.out.println("done.");
	//	System.out.print("Building grammar ... ");
		grammar = gram;
		//buildGrammar(annotatedTrainTrees);
		System.out.println("done. Number of syntactic rules="
				+ grammar.getNumberOfSyntacticRuleTypes()
				+ " , "
				+ grammar.getNumberOfLexicalRuleTypes() + "lexical rules");
		
	  
		uc = new UnaryClosure(rules);
		// System.out.println(uc);
		buildLexicon(annotatedTrainTrees);
	}

//	protected void buildGrammar(List<Tree<String>> annotatedTrainTrees) {
//		DefaultGrammarBuilder grammarBuilder = new MarinaGrammar.DefaultGrammarBuilder(
//				annotatedTrainTrees);
//		grammar = grammarBuilder.buildGrammar();
//	}

//	public Grammar getGrammar() {
//		return grammar;
//	}

	protected void buildLexicon(List<Tree> annotatedTrainTrees) {
		System.out.print("Setting up a CKY parser ... ");
		lexicon = Lexicon.createLexicon(grammar);
		System.out.println("done.");
	}
/*
	protected List<Tree> annotateTrees(List<Tree> trees) {
		List<Tree> annotatedTrees = new ArrayList<Tree>();
		for (Tree tree : trees) {
			annotatedTrees.add(TreeAnnotations.annotateTree(tree));
		}
		return annotatedTrees;
	}

	
	public Tree getBestParse(List<String> sentence) {
		Tree annotatedBestParse = getBestAnnotatedParse(sentence);
		// System.out.println(nlp.ling.Trees.PennTreeRenderer
		// .render(annotatedBestParse));
		return TreeAnnotations.unAnnotateTree(annotatedBestParse);
	}
*/@SuppressWarnings("unchecked")

	protected Tree getBestAnnotatedParse(List<String> sentence) {
		int len = sentence.size();		
		Map<String, Double>[][] table = (HashMap<String, Double>[][]) new HashMap[len + 1][len + 1];
		
		Map<String, BackTrace>[][] back = (HashMap<String, BackTrace>[][]) new HashMap[len + 1][len + 1];
		for (int j = 1; j <= len; j++) { // loop the right end
			// initialize the word->tag
			table[j - 1][j] = new HashMap<String, Double>();
			back[j - 1][j] = new HashMap<String, BackTrace>();
			for (String tag : lexicon.getAllTags()) {
				double score = lexicon.scoreTagging(sentence.get(j - 1), tag);
				if (score == 0) // reduce states
					continue;
				table[j - 1][j].put(tag, score);
				// unary rules
				List<UnaryRule> urules = uc.getClosedUnaryRulesByChild(tag);
				for (UnaryRule unaryRule : urules) {
					String parent = unaryRule.getParent();
					if (parent.equals(tag)) {
						continue;
					}
					Double oldp = table[j - 1][j].get(parent);
					double p = score * unaryRule.getScore();
					if (oldp == null || oldp < p) {
						table[j - 1][j].put(parent, p);
						back[j - 1][j].put(parent,
								BackTrace.creatUnaryBack(unaryRule));
					}
				}
			}
			for (int i = j - 2; i >= 0; i--) { // loop the left end
				table[i][j] = new HashMap<String, Double>();
				back[i][j] = new HashMap<String, BackTrace>();
				for (int k = i + 1; k < j; k++) { // loop the separate position
					Map<String, Double> CMap = table[k][j];
					for (String B : table[i][k].keySet()) {
						// loop possible left child
						List<BinaryRule> bRules = gram1
								.getBinaryRulesByLeftChild(B);
						for (BinaryRule binaryRule : bRules) {
							String C = binaryRule.getRightChild();
							if (CMap.containsKey(C)) {
								String A = binaryRule.getParent();
								double score = table[i][k].get(B) * CMap.get(C)
										* binaryRule.getScore();
								Double oldScore = table[i][j].get(A);
								if (oldScore == null || oldScore < score) {
									table[i][j].put(A, score);
									back[i][j].put(A,
											BackTrace.creatBinaryBack(k, B, C));
								}
							}
						}
					}
				}
				// apply unary rules to (i, j)
				for (String A : table[i][j].keySet().toArray(new String[0])) {
					List<UnaryRule> unaryRules = uc
							.getClosedUnaryRulesByChild(A);
					double scoreA = table[i][j].get(A);
					for (UnaryRule unaryRule : unaryRules) {
						String P = unaryRule.getParent();
						if (P.equals(A)) {
							continue;
						}
						double score = scoreA * unaryRule.score;
						Double oldScoreP = table[i][j].get(P);
						if (oldScoreP == null || oldScoreP < score) {
							table[i][j].put(P, score);
							back[i][j].put(P,
									BackTrace.creatUnaryBack(unaryRule));
						}
					}
				}

			}
		}

		Node resNode = buildTree(sentence, 0, len, getRoot(), back);
		resNode.setRoot(true);
		Tree aTree = new Tree(resNode);
		return aTree;
	}

	protected String getRoot() {
		return "S";
	}
 
	protected Node buildTree(List<String> sentence, int i, int j,
			String label, Map<String, BackTrace>[][] back) {
		// for a single word
		if (i == j - 1) {
			BackTrace trace = back[i][j].get(label);
			if (trace == null) {
				
				Node root = new Node(label);
				
				root.setRoot(true); // check it
				root.addDaughter(new Node(sentence.get(i), null, root));
				
				return root;
			}
			List<String> path = uc.getPath(trace.unaryRule);
			List<Node> emptyList = Collections.emptyList();
			Node leaf = new Node(sentence.get(i), emptyList, null);
			return buildUnaryTree(path, Collections.singletonList(leaf));
		}
		// for more than one words
		BackTrace trace = back[i][j].get(label);
		// a unary rule
		if (trace!= null && trace.isUnary()) {
			UnaryRule unaryRule = trace.unaryRule;
			Node leafChildrenTree = buildTree(sentence, i, j,
					unaryRule.getChild(), back);
			List<String> path = uc.getPath(unaryRule);
			Node unaryTree = buildUnaryTree(
					path.subList(0, path.size() - 1),
					Collections.singletonList(leafChildrenTree));
			return unaryTree;
		}
		// a binary rule
		Node leftTree = buildTree(sentence, i, trace.k, trace.B, back);
		Node rightTree = buildTree(sentence, trace.k, j, trace.C, back);
		ArrayList<Node> childrenList = new ArrayList<Node>(2);
		childrenList.add(leftTree);
		childrenList.add(rightTree);
		return new Node(label, childrenList, null);
	}

	protected Node buildUnaryTree(List<String> path,
			List<Node> leafChildren) {
		List<Node> trees = leafChildren;
		for (int k = path.size() - 1; k >= 0; k--) {
			trees = Collections.singletonList(new Node(path.get(k), trees, null));
		}
		return trees.get(0);
	}

	protected static class BackTrace {
		public int k;
		public String B, C;
		private UnaryRule unaryRule;

		public static BackTrace creatBinaryBack(int k, String b, String c) {
			return new BackTrace(k, b, c);
		}

		public static BackTrace creatUnaryBack(UnaryRule unaryRule) {
			return new BackTrace(unaryRule);
		}

		private BackTrace(int k, String b, String c) {
			this.k = k;
			this.B = b;
			this.C = c;
		}

		private BackTrace(UnaryRule unaryRule) {
			this.unaryRule = unaryRule;
		}

		public boolean isUnary() {
			return unaryRule != null;
		}
	}
	public static void main(String[] args) {
//		Trees.PennTreeReader reader = new Trees.PennTreeReader(
//				new StringReader(
//						"((S (NP (DT the) (JJ quick) (JJ (AA (BB (CC brown)))) (NN fox)) (VP (VBD jumped) (PP (IN over) (NP (DT the) (JJ lazy) (NN dog)))) (. .)))"));
//		Tree tree = reader.next();
//		System.out.println("tree " + tree);

	//	CollinsHeadFinder headFinder = new CollinsHeadFinder();
//		while (!tree.isLeaf()) {
//			Tree<String> head = headFinder.determineHead(tree);
//			System.out.println("head " + head);
//			tree = head;
//		}
	}
	
}

