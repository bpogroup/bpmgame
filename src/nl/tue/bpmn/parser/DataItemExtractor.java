package nl.tue.bpmn.parser;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;

import nl.tue.bpmn.grammars.ConditionGrammarLexer;
import nl.tue.bpmn.grammars.ConditionGrammarParser;
import nl.tue.bpmn.grammars.ConditionGrammarParser.*;

public class DataItemExtractor {
	
	private static DataItemExtractor singleton = null;
	private Map<String,List<String>> dataItems;
	
	private DataItemExtractor() {		
	}
	
	public static DataItemExtractor getInstance(){
		if (singleton == null){
			singleton = new DataItemExtractor();
		}
		return singleton;
	}
	
	public Map<String,List<String>> extract(String condition) {
		dataItems = new HashMap<String,List<String>>();
		InputStream stream = new ByteArrayInputStream(condition.getBytes(StandardCharsets.UTF_8));
		ConditionGrammarLexer lexer = null;
		try {
			lexer = new ConditionGrammarLexer(CharStreams.fromStream(stream, StandardCharsets.UTF_8));
		} catch (IOException e) { /* this should not happen */ }
	    CommonTokenStream tokens = new CommonTokenStream(lexer);
	    ConditionGrammarParser parser = new ConditionGrammarParser(tokens);
	    extract(parser.condition()); 
	    return dataItems;
	}
	
	private void extract(ConditionContext condition) {
		for (Or_termContext otc: condition.or_term()){
			extract(otc); 
		}
	}
	
	private void extract(Or_termContext condition) {
		for (And_termContext otc: condition.and_term()){
			extract(otc);
		}
	}

	private void extract(And_termContext condition) {
		if (condition.getStart().getText().startsWith("NOT")){
			extract(condition.condition()); 
		}
		if (condition.getStart().getText().startsWith("(")){
			extract(condition.condition()); 
		}
		if (condition.basic_condition() != null){
			extract(condition.basic_condition()); 
		}
	}

	private void extract(Basic_conditionContext condition) {
		if (condition.nominal_condition() != null){
			extract(condition.nominal_condition()); 
		}
		if (condition.numeric_condition() != null){
			extract(condition.numeric_condition());
		}
	}
	
	private void extract(Nominal_conditionContext condition) {
		String dataItem = condition.LABEL(0).getText();
		List<String> dataItemValues = new ArrayList<String>();
		for (TerminalNode tn: condition.LABEL().subList(1, condition.LABEL().size())){
			dataItemValues.add(tn.getText());
		}
		dataItems.put(dataItem, dataItemValues);
	}

	private void extract(Numeric_conditionContext condition) {
		String dataItem = condition.LABEL().getText();
		dataItems.put(dataItem, new ArrayList<String>());
	}

}
