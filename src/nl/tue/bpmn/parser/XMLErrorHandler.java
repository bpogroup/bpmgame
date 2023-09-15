package nl.tue.bpmn.parser;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLErrorHandler implements ErrorHandler {

	private List<String> errors;
	
	public XMLErrorHandler(){
		errors = new ArrayList<String>();
	}
	
    public void warning(SAXParseException spe) throws SAXException {
        errors.add("Warning at line " + spe.getLineNumber() + " in BPMN file: " + spe.getMessage());
    }
        
    public void error(SAXParseException spe) throws SAXException {
        errors.add("Error at line " + spe.getLineNumber() + " in BPMN file: " + spe.getMessage());
    }

    public void fatalError(SAXParseException spe) throws SAXException {
        errors.add("Error at line " + spe.getLineNumber() + " in BPMN file: " + spe.getMessage());
    }
    
    public void addError(String error){
    	errors.add(error);
    }
    
    public boolean hasErrors(){
    	return !errors.isEmpty();
    }
    
    public String errorsAsString(){
    	String result = "";
    	for (String error: errors){
    		result += error + "\n";
    	}
    	return result;
    }
}