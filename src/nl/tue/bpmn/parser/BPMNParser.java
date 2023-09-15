package nl.tue.bpmn.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import nl.tue.bpmn.concepts.*;
import nl.tue.bpmn.concepts.BPMNNode.NodeType;

public class BPMNParser extends DefaultHandler{
	
	private XMLErrorHandler errorHandler;
	private boolean hasPool;
	private BPMNRole roleBeingParsed;
	private String nodeRefBeingParsed;
	private String documentationBeingParsed;
	private Map<BPMNRole, List<String>> role2containedIds;
	private Map<String, BPMNNode> id2node;
	private Map<BPMNArc,String> arc2sourceId;
	private Map<BPMNArc,String> arc2targetId;
	private BPMNModel result;
	private List<String> errors;
	private BPMNNode nodeBeingParsed;
	
	public BPMNParser(){		
		errorHandler = new XMLErrorHandler();
		hasPool = false;
		roleBeingParsed = null;
		nodeRefBeingParsed = null;
		documentationBeingParsed = null;
		role2containedIds = new HashMap<BPMNRole, List<String>>();
		id2node = new HashMap<String, BPMNNode>();
		arc2sourceId = new HashMap<BPMNArc,String>();
		arc2targetId = new HashMap<BPMNArc,String>();		
		result = new BPMNModel();
		errors = new ArrayList<String>();
	}
		
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		// XML qNames to ignore
		if (qName.equals("definitions") || qName.equals("process") || qName.equals("extensionElements") || qName.equals("timerEventDefinition") || qName.equals("messageEventDefinition") ||
			qName.equals("outgoing") || qName.equals("incoming") || qName.equals("collaboration") || qName.equals("laneSet") || 
			qName.startsWith("signavio:") || qName.startsWith("bpmndi:") || qName.startsWith("omgdi:") || qName.startsWith("omgdc:") || qName.startsWith("conditionExpression")){
			return;
		}
		
		String id = null;
		if (qName.equals("startEvent") || qName.equals("intermediateCatchEvent") || qName.equals("endEvent") || qName.equals("task") || qName.equals("exclusiveGateway") || qName.equals("eventBasedGateway") || qName.equals("parallelGateway")){
			id = atts.getValue("id");
			if (id == null){
				result = null;
				throw new SAXException("Unexpected error: the model contains an event that has no identifier.");
			}
			if (id2node.get(id) != null){
				result = null;
				throw new SAXException("Unexpected error: the model contains a two nodes with the same identifier.");				
			}
		}
		
		if (qName.equals("participant")){
			if (hasPool){
				errors.add("The model contains more than one pool.");
			}else{
				hasPool = true;
			}
		}else if (qName.equals("lane")){
			String name = atts.getValue("name");
			if ((name == null) || (name.length() == 0)){
				errors.add("The model contains a lane that has no name.");
			}
			BPMNRole role = new BPMNRole(name);
			result.addRole(role);
			roleBeingParsed = role;
			//A lane contains flowNodeRef tags that contain the IDs of nodes that are contained in that lane.
			//Create a map that maps each lane (role) to the contained IDs. 
			role2containedIds.put(role, new ArrayList<String>());
		}else if (qName.equals("flowNodeRef")){
			nodeRefBeingParsed = "";
		}else if (qName.equals("documentation")){
			documentationBeingParsed = "";			
		}else if (qName.equals("startEvent") || qName.equals("endEvent") || qName.equals("intermediateCatchEvent") || qName.equals("task")){
			String name = atts.getValue("name");
			name = (name == null)?"":name;
			if (qName.equals("startEvent")){
				if (name.length() == 0){
					errors.add("The model contains a start event that has no name.");
				}
				nodeBeingParsed = new BPMNNode(name, NodeType.StartEvent);
			}else if (qName.equals("endEvent")){
				if (name.length() == 0){
					errors.add("The model contains an end event that has no name.");
				}
				nodeBeingParsed = new BPMNNode(name, NodeType.EndEvent);
			}else if (qName.equals("intermediateCatchEvent")){
				if (name.length() == 0){
					errors.add("The model contains an intermediate event that has no name.");
				}
				nodeBeingParsed = new BPMNNode(name, NodeType.IntermediateEvent);
			}else if (qName.equals("task")){
				if (name.length() == 0){
					errors.add("The model contains a task that has no name.");
				}
				nodeBeingParsed = new BPMNNode(name, NodeType.Task);
			}
			result.addNode(nodeBeingParsed);
			id2node.put(id, nodeBeingParsed);
		}else if (qName.equals("exclusiveGateway")){
			nodeBeingParsed = new BPMNNode("", NodeType.ExclusiveSplit); //The gateway must later be classified as a split or join 
			result.addNode(nodeBeingParsed);
			id2node.put(id, nodeBeingParsed);
		}else if (qName.equals("eventBasedGateway")){
			nodeBeingParsed = new BPMNNode("", NodeType.EventBasedGateway); 
			result.addNode(nodeBeingParsed);
			id2node.put(id, nodeBeingParsed);
		}else if (qName.equals("parallelGateway")){
			nodeBeingParsed = new BPMNNode("", NodeType.ParallelSplit); //The gateway must later be classified as a split or join
			result.addNode(nodeBeingParsed);
			id2node.put(id, nodeBeingParsed);
		}else if (qName.equals("sequenceFlow")){
			//flow (sequenceFlow), read sourceRef, targetRef attributes that contain id, read name that can contain condition
			String name = atts.getValue("name");
			name = ((name == null) || (name.length() == 0))?null:name;
			if (name != null){
				errors.addAll(ConditionEvaluator.getInstance().validate(name));
			}
			BPMNArc arc = new BPMNArc(name);
			result.addArc(arc);
			String sourceRef = atts.getValue("sourceRef");
			String targetRef = atts.getValue("targetRef");
			if ((sourceRef == null) || (sourceRef.length() == 0) || (targetRef == null) || (targetRef.length() == 0)){
				errors.add("The model contains an arc that is not connected at the beginning or at the end.");
			}
			arc2sourceId.put(arc, sourceRef);
			arc2targetId.put(arc,targetRef);
		}else{
			errors.add("The model contains an illegal model element: " + qName + ".");
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length){
		if (nodeRefBeingParsed != null){
			for (int i = start; i < start+length; i++){
				nodeRefBeingParsed += ch[i];
			}
		}else if (documentationBeingParsed != null){
			for (int i = start; i < start+length; i++){
				documentationBeingParsed += ch[i];
			}
		}
	}
	
	@Override
	public void endElement(String namespaceURI, String localName, String qName){
		if (qName.equals("lane")){
			roleBeingParsed = null;
		}else if (qName.equals("flowNodeRef")){
			//A lane contains flowNodeRef tags that contain the IDs of nodes that are contained in that lane.
			//Create a map that maps each lane (role) to the contained IDs. 
			if (roleBeingParsed != null){
				role2containedIds.get(roleBeingParsed).add(nodeRefBeingParsed.trim());
			}
			nodeRefBeingParsed = null;
		}else if (qName.equals("documentation")){
			//A lane contains documentation tags that contain a comma-separated list of resources
			if (documentationBeingParsed != null) {
				for (String resource: documentationBeingParsed.split(",")) {
					if (roleBeingParsed != null) {
						roleBeingParsed.addResource(resource.trim());
					}
				}
			}
			documentationBeingParsed = null;
		}else if (qName.equals("startEvent") || qName.equals("endEvent") || qName.equals("intermediateCatchEvent") || qName.equals("task") || qName.equals("exclusiveGateway") || qName.equals("eventBasedGateway") || qName.equals("parallelGateway")){
			nodeBeingParsed = null;
		}
	}
			
	/*	When the XML is read, the elements are created, but arcs are not attached to their source/target and nodes
	 *  are not attached to their lanes. This is to allow lanes/arcs/nodes to be specified in any order in the XML.
	 *  This method connects the nodes/arcs/lanes.
	 *  The method also checks if a gateway is a join or a split and sets the gateway type accordingly.
	 */
	private void connectElements() throws BPMNParseException{
		for (Map.Entry<BPMNRole, List<String>> rcid: role2containedIds.entrySet()){
			BPMNRole role = rcid.getKey(); 
			for (String cid: rcid.getValue()){
				BPMNNode node = id2node.get(cid);
				if (node == null){
					result = null;
					throw new BPMNParseException("Unexpected error: lane '" + role.getName() + "' contains the identifier '" + cid + "' of a node that cannot be found." + ((errors.isEmpty()?"":" This may be caused by the following errors:\n"+errorsToString())));					
				}
				role.addContainedNode(node);
			}
		}
		for (Map.Entry<BPMNArc, String> asid: arc2sourceId.entrySet()){
			String sid = asid.getValue();
			BPMNNode node = id2node.get(sid);
			if (node == null){
				result = null;
				throw new BPMNParseException("Unexpected error: an arc contains the identifier of a node that cannot be found."  + ((errors.isEmpty()?"":" This may be caused by the following errors:\n"+errorsToString())));					
			}
			BPMNArc arc = asid.getKey();
			node.addOutgoing(arc);
			arc.setSource(node);
		}
		for (Map.Entry<BPMNArc, String> atid: arc2targetId.entrySet()){
			String tid = atid.getValue();
			BPMNNode node = id2node.get(tid);
			if (node == null){
				result = null;
				throw new BPMNParseException("Unexpected error: an arc contains the identifier of a node that cannot be found."  + ((errors.isEmpty()?"":" This may be caused by the following errors:\n"+errorsToString())));					
			}
			BPMNArc arc = atid.getKey();
			node.addIncoming(arc);
			arc.setTarget(node);			
		}
		for (BPMNNode node: result.getNodes()){
			if ((node.getType() == NodeType.ExclusiveSplit) || (node.getType() == NodeType.ParallelSplit)){
				if ((node.getIncoming().size() > 1) && (node.getOutgoing().size() == 1)){
					node.setType((node.getType() == NodeType.ParallelSplit)?NodeType.ParallelJoin:NodeType.ExclusiveJoin);					
				}
			}
		}
	}
	
	/* Check the semantics of the BPMN model:
	 * - has at least one lane
	 * - no two roles with the same name
	 * - no two tasks with the same name
	 * - each node must be in a lane 
	 * - a gateway either has one incoming and multiple outgoing arcs, or multiple incoming and one outgoing arcs
	 * - tasks and intermediate events have exactly one incoming and one outgoing arc
	 * - events either have one incoming, one outgoing or one incoming and one outgoing arc
	 * - has exactly one start event
	 */
	private void checkSemantics() throws BPMNParseException{
		if (result.getRoles().size() == 0){
			errors.add("The model has no roles.");
		}
		Set<String> roleNames = new HashSet<String>();
		for (BPMNRole role: result.getRoles()){
			if (roleNames.contains(role.getName())){
				errors.add("There are two roles that have the name '" + role.getName() + "'.");
			}
			roleNames.add(role.getName());
		}
		Set<String> elementNames = new HashSet<String>();
		for (BPMNNode node: result.getNodes()){
			if ((node.getType() == NodeType.Task) || (node.getType() == NodeType.StartEvent) || (node.getType() == NodeType.IntermediateEvent)){
				if (elementNames.contains(node.getName())){
					errors.add("There are two elements in the model that have the name '" + node.getName() + "'.");
				}
				elementNames.add(node.getName());
			}
		}
		for (BPMNNode node: result.getNodes()){
			boolean inRole = false;
			for (BPMNRole role: result.getRoles()){
				if (role.getContainedNodes().contains(node)){
					inRole = true;
					break;
				}
			}
			if (!inRole){
				errors.add(((node.getType() == NodeType.Task)?("The task '" + node.getName() + "'"):"There is a node that") + " is not contained in a lane.");
			}
		}
		for (BPMNNode node: result.getNodes()){
			if (node.getType() == NodeType.Task){
				if (node.getIncoming().size() == 0) {
					errors.add("Task '" + node.getName() + "' has no incoming arc.");					
				}
				if (node.getOutgoing().size() != 1){
					errors.add("Task '" + node.getName() + "' does not have exactly one outgoig arc.");
				}
			}else if (node.getType() == NodeType.IntermediateEvent){
				if (node.getIncoming().size() == 0) {
					errors.add("Intermediate event '" + node.getName() + "' has no incoming arc.");					
				}
				if (node.getOutgoing().size() != 1){
					errors.add("Intermediate event '" + node.getName() + "' does not have exactly one outgoig arc.");
				}
			}else if (node.getType() == NodeType.StartEvent){
				if ((node.getIncoming().size() != 0) || (node.getOutgoing().size() != 1)){
					errors.add("Start event '" + node.getName() + "' does not have exactly one outgoing arc and zero incoming arcs.");
				}
			}else if (node.getType() == NodeType.EndEvent){
				if ((node.getIncoming().size() != 1) || (node.getOutgoing().size() != 0)){
					errors.add("End event '" + node.getName() + "' does not have exactly one incoming arc and zero outgoing arcs.");
				}
			}else if (node.getType() == NodeType.EventBasedGateway){
				if (node.getIncoming().size() == 0){
					errors.add("The model contains an event based gateway that has no incoming arc.");
				}	
				if (node.getOutgoing().size() < 2){
					errors.add("The model contains an event based gateway that has les than two outgoing arcs.");					
				}
			}else if ((node.getType() == NodeType.ParallelJoin) || (node.getType() == NodeType.ParallelSplit)){
				if (node.getIncoming().size() == 0){
					errors.add("The model contains a parallel gateway without incoming arcs.");
				}				
				if (node.getOutgoing().size() == 0){
					errors.add("The model contains a parallel gateway without outgoing arcs.");
				}
				if ((node.getIncoming().size() == 1) && (node.getOutgoing().size() == 1)){
					errors.add("The model contains a parallel gateway with only one incoming and one outgoing arc.");
				}
				if ((node.getIncoming().size() > 1) && (node.getOutgoing().size() > 1)){
					errors.add("The model contains a parallel gateway with multiple incoming and outgoing arcs.");
				}
			}else if ((node.getType() == NodeType.ExclusiveJoin) || (node.getType() == NodeType.ExclusiveSplit)){
				if (node.getIncoming().size() == 0){
					errors.add("The model contains an exclusive gateway without incoming arcs.");
				}				
				if (node.getOutgoing().size() == 0){
					errors.add("The model contains an exclusive gateway without outgoing arcs.");
				}
				if ((node.getIncoming().size() == 1) && (node.getOutgoing().size() == 1)){
					errors.add("The model contains an exclusive gateway with only one incoming and one outgoing arc.");
				}
				if ((node.getIncoming().size() > 1) && (node.getOutgoing().size() > 1)){
					errors.add("The model contains an exclusive gateway with multiple incoming and outgoing arcs.");
				}
			}
		}		
		boolean hasStart = false;
		for (BPMNNode node: result.getNodes()){
			if (node.getType() == NodeType.StartEvent){
				if (hasStart){
					errors.add("The model contains multiple start events.");
				}
				result.setStartEvent(node);
				hasStart = true;
			}
		}
		if (!hasStart){
			errors.add("The model has no start event.");
		}
	}

	public void parseFile(String fileName) throws BPMNParseException{
		String xml = null;
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(fileName));
			xml = new String(encoded);
		} catch (IOException e) {
	    	result = null;
	    	throw new BPMNParseException("An unexpected error occurred while reading the BPMN file '" + fileName + "'.", e);			
		}
		parse(xml);
	}
	
	public void parse(String xml) throws BPMNParseException{
		result = new BPMNModel();
		SAXParserFactory spf = SAXParserFactory.newInstance();
	    spf.setNamespaceAware(true);
	    
	    try{
			Source[] BPMN_XSD = new Source[5];
			BPMN_XSD[0] = new StreamSource(BPMNParser.class.getResource("/nl/tue/bpmn/specification/BPMN20.xsd").toExternalForm());
			BPMN_XSD[1] = new StreamSource(BPMNParser.class.getResource("/nl/tue/bpmn/specification/BPMNDI.xsd").toExternalForm());
			BPMN_XSD[2] = new StreamSource(BPMNParser.class.getResource("/nl/tue/bpmn/specification/DC.xsd").toExternalForm());
			BPMN_XSD[3] = new StreamSource(BPMNParser.class.getResource("/nl/tue/bpmn/specification/DI.xsd").toExternalForm());
			BPMN_XSD[4] = new StreamSource(BPMNParser.class.getResource("/nl/tue/bpmn/specification/Semantic.xsd").toExternalForm());

	    	SAXParser saxParser = spf.newSAXParser();
	    	SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	    	Schema schema = schemaFactory.newSchema(BPMN_XSD);
	    	Validator validator = schema.newValidator();
	    	validator.setErrorHandler(errorHandler);
	    	Reader xmlStringReader = new StringReader(xml);
	    	validator.validate(new StreamSource(xmlStringReader));
	    	xmlStringReader.close();

	    	if (!errorHandler.hasErrors()){
	    		XMLReader xmlReader = saxParser.getXMLReader();
	    		xmlReader.setContentHandler(this);
	    		xmlReader.setErrorHandler(errorHandler);
		    	xmlStringReader = new StringReader(xml);
	    		xmlReader.parse(new InputSource(xmlStringReader));
	    		xmlStringReader.close();
	    	}
	    }catch (SAXException e){
	    	result = null;
	    	throw new BPMNParseException("An unexpected error occurred while reading the BPMN XML.", e);
	    }catch (ParserConfigurationException e){
	    	result = null;
	    	throw new BPMNParseException("An unexpected error occurred while reading the BPMN XML.", e);
	    }catch (IOException e){
	    	result = null;
	    	throw new BPMNParseException("The BPMN XML could not be read.", e);	    	
	    }
	    if (errorHandler.hasErrors()){
	    	result = null;
	    	throw new BPMNParseException("The BPMN XML contains unexpected errors:\n" + errorHandler.errorsAsString());
	    }
	    connectElements();
	    checkSemantics();
	    if (!errors.isEmpty()){
	    	result = null;
	    	throw new BPMNParseException("The BPMN Model contains the following error(s):\n" + errorsToString());
	    }
	}
	
	private String errorsToString(){
		String errorString = "";
    	for (Iterator<String> errorIterator = errors.iterator(); errorIterator.hasNext();){
    		errorString += "- " + errorIterator.next() + ((errorIterator.hasNext())?"\n":"");
    	}		
    	return errorString;
	}
	
	public BPMNModel getParsedModel(){
		return result;
	}
}
