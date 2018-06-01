package decode;

import grammar.Grammar;
import grammar.Rule;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import marina_CKY.CKYParser;
import marina_CKY.MarinaTree;
import marina_CKY.PennTreebankReader;
import marina_CKY.Trees;
import tree.Node;
import tree.Terminal;
import tree.Tree;
import treebank.Treebank;

public class Decode {

	public static Set<Rule> m_setGrammarRules = null;
	public static Map<String, Set<Rule>> m_mapLexicalRules = null;
	private static  CKYParser myParser=null;
    /**
     * Implementation of a singleton pattern
     * Avoids redundant instances in memory 
     */
	public static Decode m_singDecoder = null;
	    
	public static Decode getInstance(Grammar g)
	{
		if (m_singDecoder == null)
		{
			m_singDecoder = new Decode();
			m_setGrammarRules = g.getSyntacticRules();
			m_mapLexicalRules = g.getLexicalEntries();			
		}
		return m_singDecoder;
	}

	
    
	public Tree decode(List<String> input){
		
		// Done: Baseline Decoder
		//       Returns a flat tree with NN labels on all leaves 
		
		Tree t = new Tree(new Node("TOP"));
		Iterator<String> theInput = input.iterator();
		while (theInput.hasNext()) {
			String theWord = (String) theInput.next();
			Node preTerminal = new Node("NN");
			Terminal terminal = new Terminal(theWord);
			preTerminal.addDaughter(terminal);
			t.getRoot().addDaughter(preTerminal);
		}
		// Marina
		// TODO: CYK decoder
		//       if CYK fails, 
		//       use the baseline outcome
		try{
		 t = myParser.getBestParse(input);	
		}
		catch(Exception e){
		 int i = 0;	
		}
		return t;
		
	}
//	private static void ckyParserWorkedRun(String[] args,
//			Treebank myTrainTreebank, String goldFilePath,
//			 String trainFilePath) throws FileNotFoundException {
//		
//		
//		// List<Tree> myParseTreesRes = new ArrayList<Tree>();
//	//	List<MarinaTree<String>> ckyParseTreesRes = new ArrayList<MarinaTree<String>>();
//		//Object[] goldMarinaTrees = PennTreebankReader.readTrees(goldFilePath)
//		//		.toArray();
//	//	File file = new File("./file.txt");
//	//	PrintWriter printWriter = new PrintWriter("file.txt");
//		List<Tree> myParseTrees = new ArrayList<Tree>();
//		// i = 0; i < myTrainTreebank.size(); i++) {
//		for (int i = 6; i < 7; i++) {// 200
//			List<String> mySentence = myTrainTreebank.getAnalyses().get(i)
//					.getYield();
////			MarinaTree<String> guessedBestTree = newParser
////					.getBestParse(mySentence);
//		//	Tree guessedBestTree = newParser.getBestParse(mySentence);
//		//	 myParseTrees.add(guessedBestTree);
//		//	int h=9;
//		//	int aa=9;
////			System.out.println(" before eval "
////					+ Trees.PennTreeRenderer.render(guessedBestTree));
////			printWriter.print("i= " + i + " before eval "
////					+ Trees.PennTreeRenderer.render(guessedBestTree));
////			// Tree myParseTree =
////			// Decode.getInstance(myGrammar).decode(mySentence);
////			// myParseTrees.add(myParseTree);
////			LabeledConstituentEval<String> eval = new LabeledConstituentEval<String>(
////					Collections.singleton("ROOT"), new HashSet<String>());
////
////			for (int j = 0; j < goldMarinaTrees.length; j++) {
////				printWriter.println("j=" + j);
////				eval.evaluate(guessedBestTree,
////						(MarinaTree<String>) goldMarinaTrees[j], printWriter);
////				eval.evaluate(guessedBestTree,
////						(MarinaTree<String>) goldMarinaTrees[j]);
////
////				eval.display(true, printWriter);
////				eval.display(true);
//				// if( i==73 && j==374){
//				// System.out.print("J = " +j);
//				// System.out.println(" best match "
//				// +Trees.PennTreeRenderer.render(guessedBestTree));
//				// printWriter.println(Trees.PennTreeRenderer.render(guessedBestTree));
//				// }
//			}
////			 MarinaTree<String> NormTree = (new
////			 Trees.StandardTreeNormalizer()).transformTree(bestTree);
//			//ckyParseTreesRes.add(guessedBestTree);
//		
//		
////		   writeOutput(args[2]+"myCKY", myGrammarRes, myParseTrees);
////		}
	//	writeCKYOutput(args[2], newParser.getGrammar(), ckyParseTreesRes);
		// for (MarinaTree<String> tree : trees) {
		// tree = (new Trees.StandardTreeNormalizer()).transformTree(tree);
		// System.out.println(Trees.PennTreeRenderer.render(tree));
		// List<MarinaTree<String>> treesList= new
		// ArrayList<MarinaTree<String>>();
		// treesList.add(tree);
		// CKYParser newParser =new CKYParser(treesList);
		// int i=9;
		// }
//	}

	public void setParser(CKYParser parser) {
		
		myParser = parser;
	}

	
	
	
}
