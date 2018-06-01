package marina_CKY;


import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Tools for displaying, reading, and modifying trees.
 */
public class Trees {
	
  public static interface TreeTransformer<E> {
    MarinaTree<E> transformTree(MarinaTree<E> tree);
  }
  
  public static class AnnotationStripper  {
	  
	public static String transformLabel(MarinaTree<String> tree) {
		String transformedLabel = tree.getLabel();
		int cutIndex = transformedLabel.indexOf('^');
		if (cutIndex > 0 && !tree.isLeaf()) {
			transformedLabel = new String(transformedLabel.substring(0,
					cutIndex));
		}
		return transformedLabel;
	}
	  
    public MarinaTree<String> transformTree(MarinaTree<String> tree) {
      if (tree.isLeaf()) {
    	return new MarinaTree<String>(tree.getLabel());
      }
      String transformedLabel = transformLabel( tree ); 
      List<MarinaTree<String>> transformedChildren = new ArrayList<MarinaTree<String>>();
      for (MarinaTree<String> child : tree.getChildren()) {
        transformedChildren.add(transformTree(child));
      }
      return new MarinaTree<String>(transformedLabel, transformedChildren);
    }

	
  }

  public static class FunctionNodeStripper implements TreeTransformer<String> {
	  
	public static String transformLabel(MarinaTree<String> tree) {
		String transformedLabel = tree.getLabel();
	      int cutIndex = transformedLabel.indexOf('-');
	      int cutIndex2 = transformedLabel.indexOf('=');
	      if (cutIndex2 > 0 && (cutIndex2 < cutIndex || cutIndex == -1))
	        cutIndex = cutIndex2;
	      if (cutIndex > 0 && ! tree.isLeaf()) {
	        transformedLabel = new String(transformedLabel.substring(0,cutIndex));
	      }
	      return transformedLabel;
	}
	  
    public MarinaTree<String> transformTree(MarinaTree<String> tree) {
      String transformedLabel = transformLabel( tree ); 
      if (tree.isLeaf()) {
        return new MarinaTree<String>(transformedLabel);
      }
      List<MarinaTree<String>> transformedChildren = new ArrayList<MarinaTree<String>>();
      for (MarinaTree<String> child : tree.getChildren()) {
        transformedChildren.add(transformTree(child));
      }
      return new MarinaTree<String>(transformedLabel, transformedChildren);
    }
  }

  public static class EmptyNodeStripper implements TreeTransformer<String> {
    public MarinaTree<String> transformTree(MarinaTree<String> tree) {
      String label = tree.getLabel();
      if (label.equals("-NONE-")) {
        return null;
      }
      if (tree.isLeaf()) {
        return new MarinaTree<String>(label);
      }
      List<MarinaTree<String>> children = tree.getChildren();
      List<MarinaTree<String>> transformedChildren = new ArrayList<MarinaTree<String>>();
      for (MarinaTree<String> child : children) {
    	  MarinaTree<String> transformedChild = transformTree(child);
        if (transformedChild != null)
          transformedChildren.add(transformedChild);
      }
      if (transformedChildren.size() == 0)
        return null;
      return new MarinaTree<String>(label, transformedChildren);
    }
  }

  public static class XOverXRemover<E> implements TreeTransformer<E> {
    public MarinaTree<E> transformTree(MarinaTree<E> tree) {
      E label = tree.getLabel();
      List<MarinaTree<E>> children = tree.getChildren();
      while (children.size() == 1 && ! children.get(0).isLeaf() && label.equals(children.get(0).getLabel())) {
        children = children.get(0).getChildren();
      }
      List<MarinaTree<E>> transformedChildren = new ArrayList<MarinaTree<E>>();
      for (MarinaTree<E> child : children) {
        transformedChildren.add(transformTree(child));
      }
      return new MarinaTree<E>(label, transformedChildren);
    }
  }

  public static class PunctuationNodeStripper implements TreeTransformer<String> {
    private final static Pattern punctuationPattern = Pattern.compile("\\W+");
  		public MarinaTree<String> transformTree(MarinaTree<String> tree) {
      String label = tree.getLabel();
      Matcher matcher = punctuationPattern.matcher(label);
      if (matcher.matches()) {
      		return null;
      }
      if (tree.isLeaf()) {
        return new MarinaTree<String>(label);
      }
      List<MarinaTree<String>> children = tree.getChildren();
      List<MarinaTree<String>> transformedChildren = new ArrayList<MarinaTree<String>>();
      for (MarinaTree<String> child : children) {
    	  MarinaTree<String> transformedChild = transformTree(child);
        if (transformedChild != null)
          transformedChildren.add(transformedChild);
      }
      if (transformedChildren.size() == 0)
        return null;
      return new MarinaTree<String>(label, transformedChildren);
    }
  }
  
  public static class StandardTreeNormalizer implements TreeTransformer<String> {
    EmptyNodeStripper emptyNodeStripper = new EmptyNodeStripper();
    XOverXRemover<String> xOverXRemover = new XOverXRemover<String>();
    FunctionNodeStripper functionNodeStripper = new FunctionNodeStripper();

    public MarinaTree<String> transformTree(MarinaTree<String> tree) {
      tree = functionNodeStripper.transformTree(tree);
      tree = emptyNodeStripper.transformTree(tree);
      tree = xOverXRemover.transformTree(tree);
      return tree;
    }
  }

  /**
   * Generate a Label from a <code>Tree</code>. 
   * 
   * Interface for generating labels of a tree from a given node with a different label type.
   * Note that the node with the old label is passed in so that the new label <code>T</code> can
   * have features of the original tree. See <code>transformTreeLabels</code> for
   * a function which recursively calls the label transformer on each node of a tree. 
   * @param <S> Original label type
   * @param <T> Output label type
   */
  public static interface LabelFactory<S,T> {    
	T newLabel(MarinaTree<S> node);
  }

  /**
   * Recursively transforms each node of <code>tree</code> parameter according to the passed in
   * <code>LabelFactory</code>. None of the structure of the <code>Tree</code> is altered only
   * the information stored in the Label at each node.
   * @param <S> Original Tree label type
   * @param <T> Output tree label type
   * @param tree
   * @param labelTransformer
   * @return Tree with transformed labels according to <code>labelTransformer</code>
   */
  public static <S,T> MarinaTree<T> transformTreeLabels(MarinaTree<S> tree, LabelFactory<S,T> labelFactory) {
    T newLabel = labelFactory.newLabel(tree);
    List<MarinaTree<T>> newChildren = new ArrayList<MarinaTree<T>>();
    for (MarinaTree<S> node: tree.getChildren()) {
    	MarinaTree<T> newNode = transformTreeLabels(node,labelFactory);
    	  newChildren.add(newNode);
    }
    return new MarinaTree<T>(newLabel,newChildren);
  }
  
  public static class PennTreeReader implements Iterator<MarinaTree<String>> {
    public static String ROOT_LABEL = "ROOT";

    PushbackReader in;
    MarinaTree<String> nextTree;

    public boolean hasNext() {
      return (nextTree != null);
    }

    public MarinaTree<String> next() {
      if (!hasNext()) throw new NoSuchElementException();
      MarinaTree<String> tree = nextTree;
      nextTree = readRootTree();
      return tree;
    }

    public  MarinaTree<String> readRootTree() {
      try {
        readWhiteSpace();
        if (!isLeftParen(peek())) return null;
        return readTree(true);
      } catch (IOException e) {
        throw new RuntimeException("Error reading tree.");
      }
    }

    private MarinaTree<String> readTree(boolean isRoot) throws IOException {
      readLeftParen();
      String label = readLabel();
      if (label.length() == 0 && isRoot) label = ROOT_LABEL;
      List<MarinaTree<String>> children = readChildren();
      readRightParen();
      return new MarinaTree<String>(label, children);
    }

    private String readLabel() throws IOException {
      readWhiteSpace();
      return readText();
    }

    private String readText() throws IOException {
      StringBuilder sb = new StringBuilder();
      int ch = in.read();
      while (!isWhiteSpace(ch) && !isLeftParen(ch) && !isRightParen(ch)) {
        sb.append((char) ch);
        ch = in.read();
      }
      in.unread(ch);
//      System.out.println("Read text: ["+sb+"]");
      return sb.toString().intern();
    }

    private List<MarinaTree<String>> readChildren() throws IOException {
      readWhiteSpace();
      if (!isLeftParen(peek()))
        return Collections.singletonList(readLeaf());
      return readChildList();
    }

    private int peek() throws IOException {
      int ch = in.read();
      in.unread(ch);
      return ch;
    }

    private MarinaTree<String> readLeaf() throws IOException {
      String label = readText();
      return new MarinaTree<String>(label);
    }

    private List<MarinaTree<String>> readChildList() throws IOException {
      List<MarinaTree<String>> children = new ArrayList<MarinaTree<String>>();
      readWhiteSpace();
      while (!isRightParen(peek())) {
        children.add(readTree(false));
        readWhiteSpace();
      }
      return children;
    }

    private void readLeftParen() throws IOException {
//      System.out.println("Read left.");
      readWhiteSpace();
      int ch = in.read();
      if (!isLeftParen(ch)) throw new RuntimeException("Format error reading tree with character: (" + Character.valueOf((char) ch) + ")");
    }

    private void readRightParen() throws IOException {
//      System.out.println("Read right.");
      readWhiteSpace();
      int ch = in.read();
      if (!isRightParen(ch)) throw new RuntimeException("Format error reading tree.");
    }

    private void readWhiteSpace() throws IOException {
      int ch = in.read();
      while (isWhiteSpace(ch)) {
        ch = in.read();
      }
      in.unread(ch);
    }

    private boolean isWhiteSpace(int ch) {
      return (ch == ' ' || ch == '\t' || ch == '\f' || ch == '\r' || ch == '\n');
    }

    private boolean isLeftParen(int ch) {
      return ch == '(';
    }

    private boolean isRightParen(int ch) {
      return ch == ')';
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

    public PennTreeReader(Reader in) {
      this.in = new PushbackReader(in);
      nextTree = readRootTree();
    }
  }

  /**
   * Renderer for pretty-printing trees according to the Penn Treebank indenting
   * guidelines (mutliline). 
   */
  public static class PennTreeRenderer {

    /**
     * Print the tree as done in Penn Treebank merged files. The formatting
     * should be exactly the same, but we don't print the trailing whitespace
     * found in Penn Treebank trees. The basic deviation from a bracketed
     * indented tree is to in general collapse the printing of adjacent
     * preterminals onto one line of tags and words.  Additional complexities
     * are that conjunctions (tag CC) are not collapsed in this way, and that
     * the unlabeled outer brackets are collapsed onto the same line as the next
     * bracket down.
     */
    public static <L> String render(MarinaTree<L> tree) {
      StringBuilder sb = new StringBuilder();
      renderTree(tree, 0, false, false, false, true, sb);
      sb.append('\n');
      return sb.toString();
    }

    /**
     * Display a node, implementing Penn Treebank style layout
     */
    private static <L> void renderTree(MarinaTree<L> tree, int indent, boolean parentLabelNull,
                                       boolean firstSibling,
                                       boolean leftSiblingPreTerminal,
                                       boolean topLevel,
                                       StringBuilder sb) {
      // the condition for staying on the same line in Penn Treebank
      boolean suppressIndent = (parentLabelNull ||
              (firstSibling && tree.isPreTerminal()) ||
              (leftSiblingPreTerminal && tree.isPreTerminal() &&
              (tree.getLabel() == null ||
              !tree.getLabel().toString().startsWith("CC"))));
      if (suppressIndent) {
        sb.append(' ');
      } else {
        if (!topLevel) {
          sb.append('\n');
        }
        for (int i = 0; i < indent; i++) {
          sb.append("  ");
        }
      }
      if (tree.isLeaf() || tree.isPreTerminal()) {
        renderFlat(tree, sb);
        return;
      }
      sb.append('(');
      sb.append(tree.getLabel());
      renderChildren(tree.getChildren(), indent + 1,
              tree.getLabel() == null || tree.getLabel().toString() == null, sb);
      sb.append(')');
    }

    private static <L> void renderFlat(MarinaTree<L> tree, StringBuilder sb) {
      if (tree.isLeaf()) {
        sb.append(tree.getLabel().toString());
        return;
      }
      sb.append('(');
      sb.append(tree.getLabel().toString());
      sb.append(' ');
      sb.append(tree.getChildren().get(0).getLabel().toString());
      sb.append(')');
    }


    private static <L> void renderChildren(List<MarinaTree<L>> children, int indent, boolean parentLabelNull, StringBuilder sb) {
      boolean firstSibling = true;
      boolean leftSibIsPreTerm = true;  // counts as true at beginning
      for (MarinaTree<L> child : children) {
        renderTree(child, indent, parentLabelNull, firstSibling, leftSibIsPreTerm, false, sb);
        leftSibIsPreTerm = child.isPreTerminal();
        // CC is a special case
        if (child.getLabel() != null && child.getLabel().toString().startsWith("CC")) {
          leftSibIsPreTerm = false;
        }
        firstSibling = false;
      }
    }
  }

  public static void main(String[] args) {
    PennTreeReader reader = new PennTreeReader(new StringReader("((S (NP (DT the) (JJ quick) (JJ brown) (NN fox)) (VP (VBD jumped) (PP (IN over) (NP (DT the) (JJ lazy) (NN dog)))) (. .)))"));
    MarinaTree<String> tree = reader.next();
    System.out.println(PennTreeRenderer.render(tree));
    System.out.println(tree);
  }

  /**
   * Splices out all nodes which match the provided filter.
   *
   * @param tree
   * @param filter
   * @return
   */
  public static <L> MarinaTree<L> spliceNodes(MarinaTree<L> tree, MyVal<L> filter) {
    List<MarinaTree<L>> rootList = spliceNodesHelper(tree, filter);
    if (rootList.size() > 1) throw new IllegalArgumentException("spliceNodes: no unique root after splicing");
    if (rootList.size() < 1) return null;
    return rootList.get(0);
  }

  private static <L> List<MarinaTree<L>> spliceNodesHelper(MarinaTree<L> tree, MyVal<L> filter) {
    List<MarinaTree<L>> splicedChildren = new ArrayList<MarinaTree<L>>();
    for (MarinaTree<L> child : tree.getChildren()) {
      List<MarinaTree<L>> splicedChildList = spliceNodesHelper(child, filter);
      splicedChildren.addAll(splicedChildList);
    }
    if (filter.accept(tree.getLabel()))
      return splicedChildren;
    return Collections.singletonList(new MarinaTree<L>(tree.getLabel(), splicedChildren));
  }

  /**
   * Prunes out all nodes which match the provided filter (and nodes which dominate only pruned nodes).
   *
   * @param tree
   * @param filter
   * @return
   */
  public static <L> MarinaTree<L> pruneNodes(MarinaTree<L> tree, MyVal<L> filter) {
    return pruneNodesHelper(tree, filter);
  }

  private static <L> MarinaTree<L> pruneNodesHelper(MarinaTree<L> tree, MyVal<L> filter) {
    if (filter.accept(tree.getLabel()))
      return null;
    List<MarinaTree<L>> prunedChildren = new ArrayList<MarinaTree<L>>();
    for (MarinaTree<L> child : tree.getChildren()) {
      MarinaTree<L> prunedChild = pruneNodesHelper(child, filter);
      if (prunedChild != null)
      prunedChildren.add(prunedChild);
    }
    if (prunedChildren.isEmpty() && ! tree.isLeaf())
      return null;
    return new MarinaTree<L>(tree.getLabel(), prunedChildren);
  }

}