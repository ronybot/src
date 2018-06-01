package parse;

import grammar.Grammar;
import grammar.Rule;
import marina_CKY.BinaryRule;
import marina_CKY.CKYParser;
import marina_CKY.Counter;
import marina_CKY.MarinaCKYParser;
import marina_CKY.MarinaGrammar;
import marina_CKY.MarinaTree;
import marina_CKY.PennTreebankReader;
import marina_CKY.Trees;
import marina_CKY.UnaryRule;
import marina_CKY.EnglishPennTreebankParseEvaluator.LabeledConstituentEval;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import bracketimport.TreebankReader;
import train.Train;
import tree.Node;
import tree.Tree;
import treebank.Treebank;
import utils.LineWriter;
import decode.Decode;

public class Parse {

	private static Grammar myGrammarRes;

	/**
	 * 
	 * @author Reut Tsarfaty
	 * @date 27 April 2013
	 * 
	 * @param train
	 *            -set
	 * @param test
	 *            -set
	 * @param exp
	 *            -name
	 * @throws FileNotFoundException
	 * 
	 */

	public static void main(String[] args) throws FileNotFoundException {

		// **************************//
		// * NLP@IDC PA2 *//
		// * Statistical Parsing *//
		// * Point-of-Entry *//
		// **************************//
		args = new String[3];

		// args[0]=
		// "C:\\OpenUniv\\Natural Languages\\maman14\\SyntaxTree\\data\\heb-ctrees.gold";
		// args[1]=
		// "C:\\OpenUniv\\Natural Languages\\maman14\\SyntaxTree\\data\\heb-ctrees.gold";

		args[0] = ".\\data\\heb-ctrees.gold";
		// args[0] = "C:\\OpenUniv\\Natural Languages\\heb-ctrees.gold";
		args[1] = ".\\data\\heb-ctrees.train";

		// args[1] = "C:\\OpenUniv\\Natural Languages\\heb-ctrees.train";


		// args[2]= "\\exps\\test\res";
		args[2] = ".\\exps\\test11";
		if (args.length < 3) {
			System.out
					.println("Usage: Parse <goldset> <trainset> <experiment-identifier-string>");
			return;
		}
		String goldFilePath = args[0];
		String trainFilePath = args[1]; /* ".\\data\\heb-ctrees.train"; */

		// 1. read input
		Treebank myGoldTreebank = TreebankReader.getInstance().read(true,
				args[0]);
		

		// 2. transform trees
		// TODO

		// Marina
		Treebank binarizedTreesBank = transformTreeMarina(myGoldTreebank);
	
		//TODO: use reut gramer.
		// 3. train
		//  connechttps://hangouts.google.com/call/hUx-EZNfG3TYQR7RJXfiAAEI
		 myGrammarRes = Train.getInstance().train(binarizedTreesBank);
		 binarizedTreesBank = null;
		 myGoldTreebank = null;
		 
	   
//		MyGrammar myGram = new MyGrammar(unaryRuleCounter, binaryRuleCounter);
//		System.out.println(" lex rules=" + myGrammarRes.getLexicalRules());
//		System.out.println(" syntactic rules="
//				+ myGrammarRes.getSyntacticRules());
//		MarinaCKYParser parser = new MarinaCKYParser(myTrees, myGrammarRes, myGram.getUnaryRules());	
//		
//		// 4. decode it was
		List<Tree> myParseTrees = new ArrayList<Tree>();
		System.gc();
		CKYParser parser = createCKYParser( goldFilePath);
		Treebank myTrainTreebank = TreebankReader.getInstance().read(true,args[1]);
		Decode decoder =Decode.getInstance(myGrammarRes);
		decoder.setParser(parser);
		for (int i = 0; i < 10; // myTrainTreebank.size();
		    i++) {
			List<String> mySentence = myTrainTreebank.getAnalyses().get(i)
					.getYield();
			
			Tree myParseTree = decoder.decode(
					mySentence);
			myParseTrees.add(myParseTree);
		}
		// ////////////////////////////// Marina Part
		
		// ///////////////////////////// Marina part
		// 5. de-transform trees
		// TODO

		// 6. write output
	    writeOutput(args[2], myGrammarRes, myParseTrees);
	//	myTrees = null;
	//	myGoldTreebank = null;
		

		//ckyParserWorkedRun(args, myTrainTreebank, goldFilePath, trainFilePath);
	}
	private static CKYParser createCKYParser(
			 String goldFilePath) throws FileNotFoundException {// myGrammar = null;
		//TODO: use reut tree ?.
		Collection<MarinaTree<String>> trees = PennTreebankReader
				.readTrees(goldFilePath);
		List<MarinaTree<String>> treesList = new ArrayList<MarinaTree<String>>();
		//TODO: check memory allocation.
		for (MarinaTree<String> tree : trees) {
			tree = (new Trees.StandardTreeNormalizer()).transformTree(tree);
			System.out.println(Trees.PennTreeRenderer.render(tree));

			treesList.add(tree);
		}
		
		trees =null;
		System.gc();
		CKYParser newParser = new CKYParser(treesList);
	
		return newParser;
	}
	private static void ckyParserWorkedRun(String[] args,
			Treebank myTrainTreebank, String goldFilePath,
			 String trainFilePath) throws FileNotFoundException {// myGrammar = null;
		
		Collection<MarinaTree<String>> trees = PennTreebankReader
				.readTrees(trainFilePath);
		List<MarinaTree<String>> treesList = new ArrayList<MarinaTree<String>>();

		for (MarinaTree<String> tree : trees) {
			tree = (new Trees.StandardTreeNormalizer()).transformTree(tree);
			System.out.println(Trees.PennTreeRenderer.render(tree));

			treesList.add(tree);
		}
		trees = null;
		// myTrainTreebank = null;
		CKYParser newParser = new CKYParser(treesList);
		// List<Tree> myParseTreesRes = new ArrayList<Tree>();
	//	List<MarinaTree<String>> ckyParseTreesRes = new ArrayList<MarinaTree<String>>();
		//Object[] goldMarinaTrees = PennTreebankReader.readTrees(goldFilePath)
		//		.toArray();
	//	File file = new File("./file.txt");
	//	PrintWriter printWriter = new PrintWriter("file.txt");
		List<Tree> myParseTrees = new ArrayList<Tree>();
		// i = 0; i < myTrainTreebank.size(); i++) {
		for (int i = 6; i < 7; i++) {// 200
			List<String> mySentence = myTrainTreebank.getAnalyses().get(i)
					.getYield();
//			MarinaTree<String> guessedBestTree = newParser
//					.getBestParse(mySentence);
			Tree guessedBestTree = newParser.getBestParse(mySentence);
			 myParseTrees.add(guessedBestTree);
			int h=9;
			int aa=9;
//			System.out.println(" before eval "
//					+ Trees.PennTreeRenderer.render(guessedBestTree));
//			printWriter.print("i= " + i + " before eval "
//					+ Trees.PennTreeRenderer.render(guessedBestTree));
//			// Tree myParseTree =
//			// Decode.getInstance(myGrammar).decode(mySentence);
//			// myParseTrees.add(myParseTree);
//			LabeledConstituentEval<String> eval = new LabeledConstituentEval<String>(
//					Collections.singleton("ROOT"), new HashSet<String>());
//
//			for (int j = 0; j < goldMarinaTrees.length; j++) {
//				printWriter.println("j=" + j);
//				eval.evaluate(guessedBestTree,
//						(MarinaTree<String>) goldMarinaTrees[j], printWriter);
//				eval.evaluate(guessedBestTree,
//						(MarinaTree<String>) goldMarinaTrees[j]);
//
//				eval.display(true, printWriter);
//				eval.display(true);
				// if( i==73 && j==374){
				// System.out.print("J = " +j);
				// System.out.println(" best match "
				// +Trees.PennTreeRenderer.render(guessedBestTree));
				// printWriter.println(Trees.PennTreeRenderer.render(guessedBestTree));
				// }
			}
//			 MarinaTree<String> NormTree = (new
//			 Trees.StandardTreeNormalizer()).transformTree(bestTree);
			//ckyParseTreesRes.add(guessedBestTree);
		
		
		   writeOutput(args[2]+"myCKY", myGrammarRes, myParseTrees);
		}

	
	private static void setPropabilities(Grammar myGrammarRes) {
		Set<Rule> lexRules = myGrammarRes.getLexicalRules();

		HashMap<String, Integer> lexCounter = new HashMap<String, Integer>();
		Iterator<Rule> myItrRules = lexRules.iterator();
		while (myItrRules.hasNext()) {
			Rule rule = (Rule) myItrRules.next();
			String key = rule.getLHS().toString();
			if (lexCounter.containsKey(key)) {
				lexCounter.put(key, lexCounter.get(key) + 1);
			} else {
				lexCounter.put(key, 1);
			}

		}
		myItrRules = lexRules.iterator();

		while (myItrRules.hasNext()) {
			Rule rule = (Rule) myItrRules.next();
			String key = rule.getLHS().toString();
			if (lexCounter.containsKey(key)) {
				int count = lexCounter.get(key);
				rule.setMinusLogProb(Math.log(1.0 / count));
			}

		}
		HashMap<String, Integer> syntaxCounter = new HashMap<String, Integer>();
		Set<Rule> syntacticRules = myGrammarRes.getSyntacticRules();
		Iterator<Rule> itrRules = syntacticRules.iterator();
		while (itrRules.hasNext()) {
			Rule rule = (Rule) itrRules.next();
			String key = rule.getLHS().toString();
			if (syntaxCounter.containsKey(key)) {
				syntaxCounter.put(key, syntaxCounter.get(key) + 1);
			} else {
				syntaxCounter.put(key, 1);
			}

		}
		itrRules = syntacticRules.iterator();
		while (itrRules.hasNext()) {
			Rule rule = (Rule) itrRules.next();
			String val = rule.getLHS().toString();
			if (syntaxCounter.containsKey(val)) {
				int count = syntaxCounter.get(val);
				rule.setMinusLogProb(Math.log(1.0 / (count + 1)));
			}
		}

	}

	public static Treebank transformTreeMarina(Treebank myTreebank) {
		Treebank resTreebank = new Treebank();
		for (int i = 0; i < myTreebank.size(); i++) {
			Tree myTree = myTreebank.getAnalyses().get(i);
			Tree newTree = convertToBinaryTree(myTree);
			resTreebank.add(newTree);
			System.out.println("------1--------");
			System.out.println(myTree);
			System.out.println("---------2-----");
			System.out.println(newTree);
		}
		return resTreebank;
	}

	static Tree convertToBinaryTree(Tree aTree) {

		if (aTree == null || aTree.getRoot() == null)
			return aTree;
		MyBinarizer binarizer = new MyBinarizer();
		Node newNode = binarizer.binarizeTree(aTree.getRoot(), null);
		Tree resTree = new Tree(newNode);
		return resTree;
	}

	/**
	 * Writes output to files: = the trees are written into a .parsed file = the
	 * grammar rules are written into a .gram file = the lexicon entries are
	 * written into a .lex file
	 */
	private static void writeOutput(String sExperimentName, Grammar myGrammar,
			List<Tree> myTrees) {

		writeParseTrees(sExperimentName, myTrees);
		writeGrammarRules(sExperimentName, myGrammar);
		writeLexicalEntries(sExperimentName, myGrammar);
	}

	/**
	 * Writes output to files: = the trees are written into a .parsed file = the
	 * grammar rules are written into a .gram file = the lexicon entries are
	 * written into a .lex file
	 */
	private static void writeCKYOutput(String sExperimentName,
			MarinaGrammar marinaGrammar,
			List<MarinaTree<String>> ckyParseTreesRes) {

		writeCKYParseTrees(sExperimentName + "_cky", ckyParseTreesRes);
		writeCKYGrammarRules(sExperimentName + "_cky", marinaGrammar);
		writeCKYLexicalEntries(sExperimentName + "_cky", marinaGrammar);
	}


	private static void writeCKYLexicalEntries(String sExperimentName,
			MarinaGrammar myGrammar) {
		LineWriter writer;
		// todo uncomment Iterator<Rule> myItrRules;
		// writer = new LineWriter(sExperimentName + ".lex");
		// Set<String> myEntries = myGrammar.getLexicalEntries().keySet();
		// Iterator<String> myItrEntries = myEntries.iterator();
		// while (myItrEntries.hasNext()) {
		// String myLexEntry = myItrEntries.next();
		// StringBuffer sb = new StringBuffer();
		// sb.append(myLexEntry);
		// sb.append("\t");
		// Set<Rule> myLexRules = myGrammar.getLexicalEntries()
		// .get(myLexEntry);
		// myItrRules = myLexRules.iterator();
		// while (myItrRules.hasNext()) {
		// Rule r = (Rule) myItrRules.next();
		// sb.append(r.getLHS().toString());
		// sb.append(" ");
		// sb.append(r.getMinusLogProb());
		// sb.append(" ");
		// }
		// writer.writeLine(sb.toString());
		// }

	}

	/**
	 * Writes the parsed trees into a file.
	 */
	private static void writeCKYParseTrees(String sExperimentName,
			List<MarinaTree<String>> ckyParseTreesRes) {
		LineWriter writer = new LineWriter(sExperimentName + ".parsed");
		for (int i = 0; i < ckyParseTreesRes.size(); i++) {
			writer.writeLine(ckyParseTreesRes.get(i).toString());
		}
		writer.close();
	}

	/**
	 * Writes the parsed trees into a file.
	 */
	private static void writeParseTrees(String sExperimentName,
			List<Tree> myTrees) {
		LineWriter writer = new LineWriter(sExperimentName + ".parsed");
		for (int i = 0; i < myTrees.size(); i++) {
			writer.writeLine(myTrees.get(i).toString());
		}
		writer.close();
	}

	/**
	 * Writes the grammar rules into a file.
	 */
	private static void writeCKYGrammarRules(String sExperimentName,
			MarinaGrammar myGrammar) {
		LineWriter writer;
		writer = new LineWriter(sExperimentName + ".gram");
		List<BinaryRule> myRules = myGrammar.getSyntacticRules();
		Iterator<BinaryRule> myItrRules = myRules.iterator();
		while (myItrRules.hasNext()) {
			BinaryRule r = (BinaryRule) myItrRules.next();
			writer.writeLine(r.getScore() + "\t" + r.getLeftChild() + "\t"
					+ r.getRightChild());
		}
		writer.close();
	}

	/**
	 * Writes the grammar rules into a file.
	 */
	private static void writeGrammarRules(String sExperimentName,
			Grammar myGrammar) {
		LineWriter writer;
		writer = new LineWriter(sExperimentName + ".gram");
		Set<Rule> myRules = myGrammar.getSyntacticRules();
		Iterator<Rule> myItrRules = myRules.iterator();
		while (myItrRules.hasNext()) {
			Rule r = (Rule) myItrRules.next();
			writer.writeLine(r.getMinusLogProb() + "\t" + r.getLHS() + "\t"
					+ r.getRHS());
		}
		writer.close();
	}

	/**
	 * Writes the lexical entries into a file.
	 */
	private static void writeLexicalEntries(String sExperimentName,
			Grammar myGrammar) {
		LineWriter writer;
		Iterator<Rule> myItrRules;
		writer = new LineWriter(sExperimentName + ".lex");
		Set<String> myEntries = myGrammar.getLexicalEntries().keySet();
		Iterator<String> myItrEntries = myEntries.iterator();
		while (myItrEntries.hasNext()) {
			String myLexEntry = myItrEntries.next();
			StringBuffer sb = new StringBuffer();
			sb.append(myLexEntry);
			sb.append("\t");
			Set<Rule> myLexRules = myGrammar.getLexicalEntries()
					.get(myLexEntry);
			myItrRules = myLexRules.iterator();
			while (myItrRules.hasNext()) {
				Rule r = (Rule) myItrRules.next();
				sb.append(r.getLHS().toString());
				sb.append(" ");
				sb.append(r.getMinusLogProb());
				sb.append(" ");
			}
			writer.writeLine(sb.toString());
		}
	}

}

//writeCKYOutput(args[2], newParser.getGrammar(), ckyParseTreesRes);
	// for (MarinaTree<String> tree : trees) {
	// tree = (new Trees.StandardTreeNormalizer()).transformTree(tree);
	// System.out.println(Trees.PennTreeRenderer.render(tree));
	// List<MarinaTree<String>> treesList= new
	// ArrayList<MarinaTree<String>>();
	// treesList.add(tree);
	// CKYParser newParser =new CKYParser(treesList);
	// int i=9;
	// }
//}
