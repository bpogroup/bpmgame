package nl.tue.bpmn.parser;

public class BPMNParseException extends Exception {
	private static final long serialVersionUID = 4528027285511547544L;

	public BPMNParseException() { 
		super(); 
	}
	
	public BPMNParseException(String message) {
		super(message); 
	}
	
	public BPMNParseException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public BPMNParseException(Throwable cause) {
		super(cause);
	}
}
