package nl.tue.bpmn.parser;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

public class GrammarErrorHandler implements ANTLRErrorListener{
	
	List<String> errors;
	String expression;
	
	public GrammarErrorHandler(String expression){
		errors = new ArrayList<String>();
		this.expression = expression;
	}

	@Override
	public void reportAmbiguity(Parser arg0, DFA arg1, int arg2, int arg3, boolean arg4, BitSet arg5, ATNConfigSet arg6) {
		errors.add("Ambiguity in expression '" + expression + "'");
	}

	@Override
	public void reportAttemptingFullContext(Parser arg0, DFA arg1, int arg2, int arg3, BitSet arg4, ATNConfigSet arg5) {
		//errors.add("Ambiguity in expression '" + expression + "'");
	}

	@Override
	public void reportContextSensitivity(Parser arg0, DFA arg1, int arg2, int arg3, int arg4, ATNConfigSet arg5) {
		//errors.add("Ambiguity in expression '" + expression + "'");
	}

	@Override
	public void syntaxError(Recognizer<?, ?> arg0, Object arg1, int line, int position, String msg, RecognitionException arg5) {
		errors.add("Error at position " + position + " in expression '" + expression + "': " + msg);
	}
	
	public List<String> getErrors(){
		return errors;
	}
}
