package nl.tue.bpmn.parser;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;

import nl.tue.bpmgame.executionmodel.Case;
import nl.tue.bpmgame.executionmodel.State;
import nl.tue.bpmn.grammars.ConditionGrammarLexer;
import nl.tue.bpmn.grammars.ConditionGrammarParser;
import nl.tue.bpmn.grammars.ConditionGrammarParser.*;

public class ConditionEvaluator {
	
	private static ConditionEvaluator singleton = null;
	
	private ConditionEvaluator(){		
	}
	
	public static ConditionEvaluator getInstance(){
		if (singleton == null){
			singleton = new ConditionEvaluator();
		}
		return singleton;
	}
	
	/**
	 * Evaluates the given condition in the given model. The condition is evaluated on current data in the model.
	 * The condition has the form of the BNF in ConditionGrammar.g4. If a referenced data item does not (yet)
	 * have a value, the condition returns false.
	 * 
	 * @param condition A condition on data items.
	 * @param inCm The casemodel on which the condition will be evaluated.
	 * @param t the transition for which we are computing if it is enabled.
	 * @return True or False depending on how the condition evaluates, null if the value is not initialized.
	 */
	public boolean evaluate(String condition, Case c, State s, String t) {
		InputStream stream = new ByteArrayInputStream(condition.getBytes(StandardCharsets.UTF_8));
		ConditionGrammarLexer lexer = null;
		try {
			lexer = new ConditionGrammarLexer(CharStreams.fromStream(stream, StandardCharsets.UTF_8));
		} catch (IOException e) { /* this should not happen */ }
	    CommonTokenStream tokens = new CommonTokenStream(lexer);
	    ConditionGrammarParser parser = new ConditionGrammarParser(tokens);
	    Boolean result = evaluate(parser.condition(), c, s, t); 
	    return (result == null)?false:result; 
	}

	/**
	 * Evaluates the given condition in the given model. The condition is evaluated on current data in the model.
	 * The condition has the form of the BNF in ConditionGrammar.g4. If a referenced data item does not (yet)
	 * have a value, the condition returns false. The condition cannot reference transition occurrences. 
	 * 
	 * @param condition A condition on data items.
	 * @param inCm The casemodel on which the condition will be evaluated.
	 * @return True or False depending on how the condition evaluates, null if the value is not initialized.
	 */
	public boolean evaluate(String condition, Case c, State s) {
		return evaluate(condition, c, s, null);
	}
	
	private Boolean evaluate(ConditionContext condition, Case c, State s, String t) {
		for (Or_termContext otc: condition.or_term()){
			Boolean sub = evaluate(otc, c, s, t); 
			if (sub == null){
				return null;
			}else if (sub){
				return true;
			}
		}
		return false;
	}
	
	private Boolean evaluate(Or_termContext condition, Case c, State s, String t) {
		for (And_termContext otc: condition.and_term()){
			Boolean sub = evaluate(otc, c, s, t);
			if (sub == null){
				return null;
			}else if (!sub){
				return false;
			}
		}
		return true;
	}

	private Boolean evaluate(And_termContext condition, Case c, State s, String t) {
		if (condition.getStart().getText().startsWith("NOT")){
			Boolean sub = evaluate(condition.condition(), c, s, t); 
			return (sub == null)?null:!sub; 
		}
		if (condition.getStart().getText().startsWith("(")){
			Boolean sub = evaluate(condition.condition(), c, s, t); 
			return (sub == null)?null:sub;
		}
		if (condition.basic_condition() != null){
			Boolean sub = evaluate(condition.basic_condition(), c, s, t); 
			return (sub == null)?null:sub;
		}
		return false;
	}

	private Boolean evaluate(Basic_conditionContext condition, Case c, State s, String t) {
		if (condition.nominal_condition() != null){
			Boolean sub = evaluate(condition.nominal_condition(), c, s, t); 
			return (sub == null)?null:sub;
		}
		if (condition.numeric_condition() != null){
			Boolean sub = evaluate(condition.numeric_condition(), c, s, t);
			return (sub == null)?null:sub;
		}
		if (condition.transition_condition() != null){
			Boolean sub = evaluate(condition.transition_condition(), c, s, t);
			return (sub == null)?null:sub;
		}
		return false;
	}

	private Boolean evaluate(Transition_conditionContext condition, Case c, State s, String t) {
		String transition = condition.LABEL().getText().replaceAll("'", "");
		if (t == null){
			return s.transitionOccurrences(transition) > 0;
		}else{
			return s.transitionOccurrences(transition) > s.transitionOccurrences(t);
		}
	}
	
	private Boolean evaluate(Nominal_conditionContext condition, Case c, State s, String t) {
		String dataItem = condition.LABEL(0).getText();
		String dataItemValue = c.getValue(dataItem, s.dataItemTouches(dataItem));
		if (dataItemValue == null){
			return false;
		}
		for (TerminalNode tn: condition.LABEL().subList(1, condition.LABEL().size())){
			if (tn.getText().equals(dataItemValue)){
				return true;
			}
		}		
		return false;				
	}

	private Boolean evaluate(Numeric_conditionContext condition, Case c, State s, String t) {
		String dataItem = condition.LABEL().getText();
		String dataItemValue = c.getValue(dataItem, s.dataItemTouches(dataItem));
		if (dataItemValue == null){
			return false;
		}
		try{
			long numericValue = Long.parseLong(dataItemValue);
			long compareValue = Long.parseLong(condition.NUMBER().getText());
			String comparator = condition.COMPARATOR().getText();
			if (comparator.equals("=")){
				return numericValue == compareValue;
			}else if (comparator.equals(">")){
				return numericValue > compareValue;
			}else if (comparator.equals("<")){
				return numericValue < compareValue;
			}else if (comparator.equals(">=")){
				return numericValue >= compareValue;
			}else if (comparator.equals("<=")){
				return numericValue <= compareValue;			
			}
		}catch(Exception e){
			return false;
		}
		return false;				
	}

	/**
	 * Checks the syntax of the given condition expression.
	 * Returns a list of errors, which is empty if there are no errors. 
	 *  
	 * @param expression A string representing a condition expression in the agreed upon format.
	 * @return a list of errors.
	 */
	public List<String> validate(String condition){
		GrammarErrorHandler errorListener = new GrammarErrorHandler(condition);

		InputStream stream = new ByteArrayInputStream(condition.getBytes(StandardCharsets.UTF_8));
		ConditionGrammarLexer lexer = null;
		try {
			lexer = new ConditionGrammarLexer(CharStreams.fromStream(stream, StandardCharsets.UTF_8));
		} catch (IOException e) { /* this should not happen */ }		
		lexer.addErrorListener(errorListener);
	    CommonTokenStream tokens = new CommonTokenStream(lexer);
	    ConditionGrammarParser parser = new ConditionGrammarParser(tokens);
	    parser.addErrorListener(errorListener);
	    parser.condition();
	    return errorListener.getErrors();
	}
}
