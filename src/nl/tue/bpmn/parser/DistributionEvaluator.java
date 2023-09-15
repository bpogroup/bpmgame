package nl.tue.bpmn.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import nl.tue.bpmn.grammars.DistributionGrammarLexer;
import nl.tue.bpmn.grammars.DistributionGrammarParser;
import nl.tue.bpmn.grammars.DistributionGrammarParser.*;
import nl.tue.util.RandomGenerator;

public class DistributionEvaluator {

	private static DistributionEvaluator singleton;
	
	private String valueFirstTime = null;
	private String valueSecondTime = null;
	
	private DistributionEvaluator(){}
	
	public static DistributionEvaluator getInstance(){
		if (singleton == null){
			singleton = new DistributionEvaluator();			
		}
		return singleton;
	}
	
	/**
	 * Evaluates a data item distribution and assigns values according to 
	 * the probability distributions specified. 
	 * Examples:
	 * [{anytime, N(4,10)}]
	 * [{anytime, exp(5)}]
	 * [{firsttime, [{correct, 0%}, {incorrect, 100%}]}, {secondtime, [{high, 10%}, {medium, 20%}, {low, 70%}]}]
	 *  
	 * @param distribution A string representing the data item distribution in the agreed upon format.
	 */
	public void evaluate(String distribution) {
		GrammarErrorHandler errorListener = new GrammarErrorHandler(distribution);
		InputStream stream = new ByteArrayInputStream(distribution.getBytes(StandardCharsets.UTF_8));
		DistributionGrammarLexer lexer = null;
		try {
			lexer = new DistributionGrammarLexer(CharStreams.fromStream(stream, StandardCharsets.UTF_8));
		} catch (IOException e) { /* this should not happen */ }		
		lexer.addErrorListener(errorListener);
	    CommonTokenStream tokens = new CommonTokenStream(lexer);
	    DistributionGrammarParser parser = new DistributionGrammarParser(tokens);
	    evaluate(parser.distribution());
	}
	
	private void evaluate(DistributionContext dc){
		for (Single_termContext stc: dc.single_term()){
			evaluate(stc);
		}
	}
	
	private void evaluate(Single_termContext stc){
		String value = evaluate(stc.distribution_term());
		if (stc.TERM().getText().startsWith("first")){
			valueFirstTime = value;
		}else if (stc.TERM().getText().startsWith("second")){
			valueSecondTime = value;			
		}else{
			valueFirstTime = value;
			valueSecondTime = value;			
		}
	}
	
	private String evaluate(Distribution_termContext dtc){
		if (dtc.getText().startsWith("exp")){
			long mean = Long.parseLong(dtc.NUMBER(0).getText());
			return Long.toString(RandomGenerator.generateExponential(mean));
		}else if (dtc.getText().startsWith("N")){
			long mu = Long.parseLong(dtc.NUMBER(0).getText());
			long sigma = Long.parseLong(dtc.NUMBER(1).getText());
			return Long.toString(RandomGenerator.generateNormal(mu,sigma));			
		}else{
			long r = RandomGenerator.generateUniform(100);
			long curr = 0;
			for (ValueContext vc: dtc.value_series().value()){
				curr += Long.parseLong(vc.NUMBER().getText());
				if (r < curr){
					return vc.TERM().getText();
				}
			}
		}
		return null;
	}

	public String getValueFirstTime() {
		return valueFirstTime;
	}

	public String getValueSecondTime() {
		return valueSecondTime;
	}

	/**
	 * Checks the syntax of the given distribution expression.
	 * Returns a list of errors, which is empty if there are no errors. 
	 *  
	 * @param expression A string representing a distribution in the agreed upon format.
	 * @return a list of errors.
	 */
	public List<String> validate(String distribution){
		GrammarErrorHandler errorListener = new GrammarErrorHandler(distribution);

		InputStream stream = new ByteArrayInputStream(distribution.getBytes(StandardCharsets.UTF_8));
		DistributionGrammarLexer lexer = null;
		try {
			lexer = new DistributionGrammarLexer(CharStreams.fromStream(stream, StandardCharsets.UTF_8));
		} catch (IOException e) { /* this should not happen */ }		
		lexer.addErrorListener(errorListener);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
	    DistributionGrammarParser parser = new DistributionGrammarParser(tokens);
	    parser.addErrorListener(errorListener);
	    parser.distribution();
	    return errorListener.getErrors();
	}
}
