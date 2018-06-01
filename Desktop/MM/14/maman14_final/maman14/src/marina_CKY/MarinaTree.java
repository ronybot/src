package marina_CKY;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represent linguistic trees, with each node consisting of a label and a list of children.
 */
public class MarinaTree <L> {
  L label;
  List<MarinaTree<L>> children;

  public List<MarinaTree<L>> getChildren() {
    return children;
  }
  public void setChildren(List<MarinaTree<L>> children) {
    this.children = children;
  }
  public L getLabel() {
    return label;
  }
  public void setLabel(L label) {
    this.label = label;
  }

  public boolean isLeaf() {
    return getChildren().isEmpty();
  }
  public boolean isPreTerminal() {
    return getChildren().size() == 1 && getChildren().get(0).isLeaf();
  }
  public boolean isPhrasal() {
    return ! (isLeaf() || isPreTerminal());
  }

  public List<L> getYield() {
    List<L> yield = new ArrayList<L>();
    appendYield(this, yield);
    return yield;
  }

  private static <L> void appendYield(MarinaTree<L> tree, List<L> yield) {
    if (tree.isLeaf()) {
      yield.add(tree.getLabel());
      return;
    }
    for (MarinaTree<L> child : tree.getChildren()) {
      appendYield(child, yield);
    }
  }

  public List<L> getPreTerminalYield() {
    List<L> yield = new ArrayList<L>();
    appendPreTerminalYield(this, yield);
    return yield;
  }

  private static <L> void appendPreTerminalYield(MarinaTree<L> tree, List<L> yield) {
    if (tree.isPreTerminal()) {
      yield.add(tree.getLabel());
      return;
    }
    for (MarinaTree<L> child : tree.getChildren()) {
      appendPreTerminalYield(child, yield);
    }
  }

  public List<MarinaTree<L>> getPreOrderTraversal() {
    ArrayList<MarinaTree<L>> traversal = new ArrayList<MarinaTree<L>>();
    traversalHelper(this, traversal, true);
    return traversal;
  }

  public List<MarinaTree<L>> getPostOrderTraversal() {
    ArrayList<MarinaTree<L>> traversal = new ArrayList<MarinaTree<L>>();
    traversalHelper(this, traversal, false);
    return traversal;
  }

  private static <L> void traversalHelper(MarinaTree<L> tree, List<MarinaTree<L>> traversal, boolean preOrder) {
    if (preOrder)
      traversal.add(tree);
    for (MarinaTree<L> child : tree.getChildren()) {
      traversalHelper(child, traversal, preOrder);
    }
    if (! preOrder)
      traversal.add(tree);
  }

  public List<MarinaTree<L>> toSubTreeList() {
    return getPreOrderTraversal();
  }

  public List<Constituent<L>> toConstituentList() {
    List<Constituent<L>> constituentList = new ArrayList<Constituent<L>>();
    toConstituentCollectionHelper(this, 0, constituentList);
    return constituentList;
  }

  private static <L> int toConstituentCollectionHelper(MarinaTree<L> tree, int start, List<Constituent<L>> constituents) {
    if (tree.isLeaf() || tree.isPreTerminal())
      return 1;
    int span = 0;
    for (MarinaTree<L> child : tree.getChildren()) {
      span += toConstituentCollectionHelper(child, start+span, constituents);
    }
    constituents.add(new Constituent<L>(tree.getLabel(), start, start+span));
    return span;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    toStringBuilder(sb);
    return sb.toString();
  }

  public void toStringBuilder(StringBuilder sb) {
    if (! isLeaf()) sb.append('(');
    if (getLabel() != null) {
      sb.append(getLabel());
    }
    if (! isLeaf()) {
      for (MarinaTree<L> child : getChildren()) {
        sb.append(' ');
        child.toStringBuilder(sb);
      }
      sb.append(')');
    }
  }

  public MarinaTree<L> deepCopy() {
    return deepCopy(this);
  }

  private static <L> MarinaTree<L> deepCopy(MarinaTree<L> tree) {
    List<MarinaTree<L>> childrenCopies = new ArrayList<MarinaTree<L>>();
    for (MarinaTree<L> child : tree.getChildren()) {
      childrenCopies.add(deepCopy(child));
    }
    return new MarinaTree<L>(tree.getLabel(), childrenCopies);
  }

  public MarinaTree(L label, List<MarinaTree<L>> children) {
    this.label = label;
    this.children = children;
  }

  public MarinaTree(L label) {
    this.label = label;
    this.children = Collections.emptyList();
  }
}