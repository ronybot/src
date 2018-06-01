package train;

import grammar.Event;
import grammar.Grammar;
import grammar.Rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import tree.Node;
import tree.Tree;
import treebank.Treebank;



/**
 * 
 * @author Reut Tsarfaty
 * 
 * CLASS: Train
 * 
 * Definition: a learning component
 * Role: reads off a grammar from a treebank
 * Responsibility: keeps track of rule counts
 * 
 */

public class Train {


    /**
     * Implementation of a singleton pattern
     * Avoids redundant instances in memory 
     */
	public static Train m_singTrainer = null;
	    
	public static Train getInstance()
	{
		if (m_singTrainer == null)
		{
			m_singTrainer = new Train();
		}
		return m_singTrainer;
	}
	
	public static void main(String[] args) {

	}
	
	public Grammar train(Treebank myTreebank)
	{
		Grammar myGrammar = new Grammar();
		for (int i = 0; i < myTreebank.size(); i++) {
			Tree myTree = myTreebank.getAnalyses().get(i);
			List<Rule> theRules = getRules(myTree);
			myGrammar.addAll(theRules);
		}
		setPropabilities(myGrammar);
		return myGrammar;
	}
	// Marina addinition
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

	public List<Rule> getRules(Tree myTree)
	{
		List<Rule> theRules = new ArrayList<Rule>();
		
		List<Node> myNodes = myTree.getNodes();
		for (int j = 0; j < myNodes.size(); j++) {
			Node myNode = myNodes.get(j);
			if (myNode.isInternal())
			{
				Event eLHS = new Event(myNode.getIdentifier());
				Iterator<Node> theDaughters = myNode.getDaughters().iterator();
				StringBuffer sb = new StringBuffer();
				while (theDaughters.hasNext()) {
					Node n = (Node) theDaughters.next();
					sb.append(n.getIdentifier());
					if (theDaughters.hasNext())
						sb.append(" ");
				}
				Event eRHS = new Event (sb.toString());
				Rule theRule = new Rule(eLHS, eRHS);
				if (myNode.isPreTerminal())
					theRule.setLexical(true);
				if (myNode.isRoot())
					theRule.setTop(true);
				theRules.add(theRule);
			}	
		}
		return theRules;
	}
	
}
