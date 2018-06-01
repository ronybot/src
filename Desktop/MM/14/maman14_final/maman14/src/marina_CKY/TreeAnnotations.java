package marina_CKY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class which contains code for annotating and binarizing trees for the
 * parser's use, and debinarizing and unannotating them for scoring.
 */
public class TreeAnnotations {
	public static MarinaTree<String> annotateTree(MarinaTree<String> unAnnotatedTree) {
		
		return binarizeTree(unAnnotatedTree);
	}

	private static MarinaTree<String> binarizeTree(MarinaTree<String> tree) {
		String label = tree.getLabel();
		if (tree.isLeaf())
			return new MarinaTree<String>(label);
		if (tree.getChildren().size() == 1) {
			return new MarinaTree<String>(label,
					Collections.singletonList(binarizeTree(tree.getChildren()
							.get(0))));
		}
		// otherwise, it's a binary-or-more local tree, so decompose it into a
		// sequence of binary and unary trees.
		String intermediateLabel = "@" + label + "->";
		MarinaTree<String> intermediateTree = binarizeTreeHelper(tree, 0,
				intermediateLabel);
		return new MarinaTree<String>(label, intermediateTree.getChildren());
	}

	private static MarinaTree<String> binarizeTreeHelper(MarinaTree<String> tree,
			int numChildrenGenerated, String intermediateLabel) {
		MarinaTree<String> leftTree = tree.getChildren().get(numChildrenGenerated);
		List<MarinaTree<String>> children = new ArrayList<MarinaTree<String>>();
		children.add(binarizeTree(leftTree));
		if (numChildrenGenerated < tree.getChildren().size() - 1) {
			MarinaTree<String> rightTree = binarizeTreeHelper(tree,
					numChildrenGenerated + 1, intermediateLabel + "_"
							+ leftTree.getLabel());
			children.add(rightTree);
		}
		return new MarinaTree<String>(intermediateLabel, children);
	}

	public static MarinaTree<String> unAnnotateTree(MarinaTree<String> annotatedTree) {
		// Remove intermediate nodes (labels beginning with "@"
		// Remove all material on node labels which follow their base symbol
		// (cuts at the leftmost -, ^, or : character)
		// Examples: a node with label @NP->DT_JJ will be spliced out, and a
		// node with label NP^S will be reduced to NP
		MarinaTree<String> debinarizedTree = Trees.spliceNodes(annotatedTree,
				new MyVal<String>() {
					public boolean accept(String s) {
						return s.startsWith("@");
					}
				});
		MarinaTree<String> unAnnotatedTree = (new Trees.AnnotationStripper())
				.transformTree(debinarizedTree);
		return unAnnotatedTree;
	}

	static int horizontal = 0;
	static boolean useparent = false;
	final static String VIRTUALROOT = "root";

//	public static Tree<String> annotateTreeMarkov(Tree<String> unAnnotatedTree) {
//		Tree<String> annotatedTree = binarizeTreeMarkov(unAnnotatedTree,
//				VIRTUALROOT);
//		// System.out.println(Trees.PennTreeRenderer.render(unAnnotatedTree));
//		// System.out.println(Trees.PennTreeRenderer.render(annotatedTree));
//		// System.out.println(Trees.PennTreeRenderer.render(unAnnotateTree(annotatedTree)));
//		return annotatedTree;
//	}
//
//	private static Tree<String> binarizeTreeMarkov(Tree<String> tree,
//			String parent) {
//		String label = tree.getLabel();
//		String currentLabel = useparent ? String.format("%s^%s", label, parent)
//				: label;
//		if (tree.isLeaf()) {
//			return new Tree<String>(label);
//		}
//		// if (tree.isPreTerminal()) {
//		// currentLabel = label;
//		// }
//		if (tree.getChildren().size() == 1) {
//			return new Tree<String>(currentLabel,
//					Collections.singletonList(binarizeTreeMarkov(tree
//							.getChildren().get(0), label)));
//		}
//		// otherwise, it's a binary-or-more local tree, so decompose it into a
//		// sequence of binary and unary trees.
//
//		Tree<String> intermediateTree = binarizeTreeMarkovHelper(tree, tree
//				.getChildren().size() - 1, currentLabel, label);
//		// return new Tree<String>(currentLabel,
//		// Collections.singletonList(intermediateTree));
//		return new Tree<String>(currentLabel, intermediateTree.getChildren());
//	}
//
//	private static Tree<String> binarizeTreeMarkovHelper(Tree<String> tree,
//			int numChildrenToBeGenerated, String currentLabel,
//			String parentForLowerLevel) {
//
//		Tree<String> rightTree = binarizeTreeMarkov(
//				tree.getChildren().get(numChildrenToBeGenerated),
//				parentForLowerLevel);
//		List<Tree<String>> children = new ArrayList<Tree<String>>();
//		if (numChildrenToBeGenerated > 0) {
//			Tree<String> leftTree = binarizeTreeMarkovHelper(tree,
//					numChildrenToBeGenerated - 1, currentLabel,
//					parentForLowerLevel);
//			// left
//			children.add(leftTree);
//			// right
//			children.add(rightTree);
//		} else {
//			// compress the only subtree by deleting this node itself
//			return rightTree;
//		}
//		// intermediate label
//		StringBuilder sb = new StringBuilder();
//		for (int i = Math.max(0, numChildrenToBeGenerated + 1 - horizontal); i <= numChildrenToBeGenerated; i++) {
//			sb.append(String
//					.format("_%s", tree.getChildren().get(i).getLabel()));
//		}
//		String intermediateLabel = String.format("@%s->..%s", currentLabel,
//				sb.toString());
//		return new Tree<String>(intermediateLabel, children);
//	}
}
