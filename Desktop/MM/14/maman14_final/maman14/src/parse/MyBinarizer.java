package parse;

import java.util.ArrayList;
import java.util.List;

import tree.Node;
import tree.Terminal;

/**
 * Return a clone of just the root node of this tree (with no children)
 * 
 * @return
 */

public class MyBinarizer {
	 MyBinarizer() {
	 }
	public  Node binarizeTree(Node tree, Node parent) {
		String label = tree.getIdentifier();
		List<Node> children = tree.getDaughters();
		if (children.size() == 1 && (children.get(0) instanceof Terminal))// isLeaf yyodot->yyodot
			return (Node) tree.clone();
										
		else if (children.size() == 1) {
			Node aTree = binarizeTree(children.get(0), parent); 
			return aTree;
		}
		// otherwise, it's a binary-or-more local tree, so decompose it into a
		// sequence of binary and unary trees.
	//	String labelInTheMiddle = "@" + label + "->";
		String labelInTheMiddle = "@" + label + "_";
		Node tmpTree = rightBinarizeTreeHelper(tree,
				children.size() - 1, labelInTheMiddle);

		return new Node(labelInTheMiddle, tmpTree.getDaughters(), parent);
	}

	private  Node rightBinarizeTreeHelper(Node tree, int numChildrenLeft,
			String intermediateLabel) {
		Node rightTree = tree.getDaughters().get(numChildrenLeft);
		List<Node> children = new ArrayList<Node>(2);
		if (numChildrenLeft == 1  && !(tree.getDaughters().get(0) instanceof Terminal)) {
			children.add(binarizeTree(tree.getDaughters().get(
					numChildrenLeft - 1), tree) );
		} else if (numChildrenLeft > 1) {
			Node leftTree = rightBinarizeTreeHelper(tree, numChildrenLeft - 1,
					intermediateLabel + "_" + rightTree.getLabel());
			children.add(leftTree);
		}
		children.add(binarizeTree(rightTree, tree));
		return new Node(intermediateLabel, children, tree);
	}

}

