package nl.tue.bpmgame.bpmgamegui.controllers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import nl.tue.bpmgame.assignment.Assignment;
import nl.tue.bpmgame.bpmgamegui.dao.GameGroupDAO;
import nl.tue.bpmgame.bpmgamegui.dao.ModelDAO;
import nl.tue.bpmgame.bpmgamegui.dao.PersistentKPIDAO;
import nl.tue.bpmgame.bpmgamegui.dao.PersistentLogEventDAO;
import nl.tue.bpmgame.bpmgamegui.dao.UserDAO;
import nl.tue.bpmgame.bpmgamegui.services.ConstantProviderService;
import nl.tue.bpmgame.bpmgamegui.viewmodels.CaseStateViewModel;
import nl.tue.bpmgame.bpmgamegui.viewmodels.ModelViewModel;
import nl.tue.bpmgame.bpmgamegui.viewmodels.TaskStateViewModel;
import nl.tue.bpmgame.dataaccess.model.GameGroup;
import nl.tue.bpmgame.dataaccess.model.LogAttribute;
import nl.tue.bpmgame.dataaccess.model.Model;
import nl.tue.bpmgame.dataaccess.model.PersistentKPI;
import nl.tue.bpmgame.dataaccess.model.PersistentLogEvent;
import nl.tue.bpmgame.dataaccess.model.User;
import nl.tue.bpmn.concepts.BPMNArc;
import nl.tue.bpmn.concepts.BPMNNode;
import nl.tue.bpmn.concepts.BPMNRole;
import nl.tue.bpmn.concepts.BPMNNode.NodeType;
import nl.tue.bpmn.parser.BPMNParseException;
import nl.tue.bpmn.parser.BPMNParser;
import nl.tue.bpmn.parser.DataItemExtractor;
import nl.tue.util.TimeUtils;

@Controller
public class ModelController {

	@Autowired
	private UserDAO userDao;
	@Autowired
	private ModelDAO modelDao;
	@Autowired
	private GameGroupDAO gameGroupDao;
	@Autowired
	private PersistentLogEventDAO persistentLogEventDao;
	@Autowired
	private PersistentKPIDAO persistentKPIDao;
	

	@RequestMapping(value = ConstantProviderService.modelUpload, method = RequestMethod.GET)
	public ModelAndView upload(HttpServletRequest request) {
		User u = userDao.findByEmail(request.getUserPrincipal().getName());		
		
		if (u.getGroup() == null) {
			ModelAndView mav = new ModelAndView();
			mav.setViewName("response");
			mav.addObject("alertType","alert-danger");
			mav.addObject("message", "You can only upload a model if you are in a group.");
			return mav;
		}
		
		return createModelManagementView(u.getGroup());
	}

	@RequestMapping(value = ConstantProviderService.modelUpload, method = RequestMethod.POST)
	public ModelAndView upload(HttpServletRequest request,
			@RequestParam("file") CommonsMultipartFile[] files) throws IOException {
		User u = userDao.findByEmail(request.getUserPrincipal().getName());		
		
		if (u.getGroup() == null) {
			ModelAndView mav = new ModelAndView();
			mav.setViewName("response");
			mav.addObject("alertType","alert-danger");
			mav.addObject("message", "You can only upload a model if you are in a group.");
			return mav;
		}

		if (files.length != 1) {
			ModelAndView mav = new ModelAndView();
			mav.setViewName("response");
			mav.addObject("alertType","alert-danger");
			mav.addObject("message", "An unexpected error occurred: not exactly one model was uploaded.");
			return mav;
		}

		String xml = new String(files[0].getBytes());
		
		//Check if the model is correctly formatted
		BPMNParser parser = new BPMNParser();
		String errorMessage = "";
		try {
			parser.parse(xml);
			Assignment.initializeAssignment(Assignment.ACTIVE_ASSIGNMENT);
		} catch (BPMNParseException e) {
			errorMessage = e.getMessage();
		} catch (Exception e) {
			errorMessage = "An unexpected error occurred while initializing the assignment.";
		}
		//Check if the elements of the model are defined in the assignment
		if (parser.getParsedModel() != null) {
			//Check tasks/events
			for (BPMNNode node: parser.getParsedModel().getNodes()) {
				String label = node.getName();
				if (node.getType() == NodeType.Task) {
					if (Assignment.getAssignment().getTask(label) == null) {
						errorMessage += "- Task '" + label + "', which is used in your model, does not exist.\n";
					}
				}else if ((node.getType() == NodeType.StartEvent) || (node.getType() == NodeType.EndEvent) || (node.getType() == NodeType.IntermediateEvent)) {
					if (Assignment.getAssignment().getEvent(label) == null) {
						errorMessage += "- Event '" + label + "', which is used in your model, does not exist.\n";
					}				
				}
			}
			//Check roles/resources
			for (BPMNRole role: parser.getParsedModel().getRoles()) {
				for (String r: role.getResources()) {
					if (Assignment.getAssignment().getResource(r) == null) {
						errorMessage += "- Role '" + role.getName() + "' from your model contains a resource: '" + r + "', which does not exist.\n";
					}
				}
			}
			//Check data items/values
			for (BPMNArc arc: parser.getParsedModel().getArcs()) {
				String condition = arc.getCondition();
				if (condition.length() > 0) {
					Map<String,List<String>> dataItems = DataItemExtractor.getInstance().extract(condition);
					for (Map.Entry<String, List<String>> dataItem: dataItems.entrySet()) {
						if (!Assignment.getAssignment().checkDataItemExists(dataItem.getKey())) {
							errorMessage += "- Data item '" + dataItem.getKey() + "' in condition '" + condition + "', does not exist.\n";
						} else {
							for (String value: dataItem.getValue()) {
								if (!Assignment.getAssignment().checkDataValueExists(dataItem.getKey(), value)) {
									errorMessage += "- Data item '" + dataItem.getKey() + "' has no value '" + value + "' as assumed in condition '" + condition + "'.\n";
								}
							}
						}
					}
				}
			}
		}

		//Return an error if not
		if (errorMessage.length() > 0) {
			ModelAndView mav = new ModelAndView();
			mav.setViewName("response");
			mav.addObject("alertType","alert-danger");
			errorMessage = "Your model contains problems that must be solved before it can be deployed.\n" + errorMessage;
			errorMessage = errorMessage.replaceAll("\\n", "<br>");
			mav.addObject("message", errorMessage);
			return mav;			
		}
		
		Model activeModel = modelDao.findActiveByGroup(u.getGroup());
		
		if (activeModel != null) {
			activeModel.setActive(false);
			modelDao.update(activeModel);
		}
		
		Model newModel = new Model();
		newModel.setActive(true);
		newModel.setGroup(u.getGroup());
		newModel.setSafe(false);
		newModel.setXml(xml);
		newModel.setUploader(u);
		newModel.setFileName(files[0].getOriginalFilename());
		newModel.setDeployedOn(Calendar.getInstance().getTime());
		modelDao.create(newModel);
		
		u.getGroup().addModel(newModel);
		gameGroupDao.update(u.getGroup());
		
		return createModelManagementView(u.getGroup());
	}
	
	private ModelAndView createModelManagementView(GameGroup gameGroup) {
		List<Model> models = gameGroup.getModels();		
		ModelAndView mav = new ModelAndView();
		ModelViewModel viewModel[] = new ModelViewModel[models.size()];
		int i = models.size()-1;
		for (Model model: models) {
			viewModel[i] = new ModelViewModel();
			viewModel[i].setId(model.getId());
			viewModel[i].setDeployedOn(model.getDeployedOn());
			viewModel[i].setFileName(model.getFileName());
			viewModel[i].setUploaderName(model.getUploader().getName());
			i--;
		}
		mav.setViewName("model");
		mav.addObject("models", viewModel);
		return mav;				
	}

	@RequestMapping(value = ConstantProviderService.modelLogDownload, method = RequestMethod.GET)
	public ModelAndView logDownload(HttpServletRequest request) {
		User u = userDao.findByEmail(request.getUserPrincipal().getName());		
		
		if (u.getGroup() == null) {
			ModelAndView mav = new ModelAndView();
			mav.setViewName("response");
			mav.addObject("alertType","alert-danger");
			mav.addObject("message", "You can only upload a model if you are in a group.");
			return mav;
		}

		ModelAndView mav = new ModelAndView();
		mav.setViewName("log");
		return mav;
	}
	
	@RequestMapping(value = ConstantProviderService.modelLogDownload, method = RequestMethod.POST)
	public void logDownload(HttpServletRequest request, HttpServletResponse response) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		User u = userDao.findByEmail(request.getUserPrincipal().getName());		
		
		if (u.getGroup() == null) {
			return;
		}
		
	    response.setContentType("text/csv");
	    response.setHeader("Content-Disposition", "attachment;filename=log.csv");

		List<PersistentLogEvent> log = persistentLogEventDao.listByGroup(u.getGroup());				
	    ServletOutputStream outStream = response.getOutputStream();
	    Assignment.initializeAssignment(Assignment.ACTIVE_ASSIGNMENT);
	    List<String> dataItems = new ArrayList<String>(Assignment.getAssignment().getDataItems());
	    
	    String header = "Case ID,Activity,Resource,Start Timestamp,Complete Timestamp";
	    for (String dataItem: dataItems) {
	    	header += "," + dataItem;
	    }
	    outStream.println(header);
	    
	    for (PersistentLogEvent ple: log) {
	    	if (ple.getCompletionTime() <= TimeUtils.currentTime()) {
		    	String startTime = TimeUtils.formatTime(ple.getStartTime());
		    	String completionTime = TimeUtils.formatTime(ple.getCompletionTime());
		    	String line = ple.getCaseID() + "," + ple.getEvent() + "," + (ple.getResource()==null?"":ple.getResource()) + "," + startTime + "," + completionTime;
		    	List<LogAttribute> attributes = new ArrayList<LogAttribute>(ple.getAttributes());
			    for (String dataItem: dataItems) {
			    	String value = "";
			    	int i = 0;
			    	boolean found = false;
			    	while ((i < attributes.size()) && !found) {
			    		if (attributes.get(i).getAttributeName().equals(dataItem)) {
			    			value = attributes.get(i).getAttributeValue();
			    			attributes.remove(i);
			    			found = true;
			    		}
			    		i++;
			    	}
		    		line += "," + value;
			    }
			    outStream.println(line);
	    	}
	    }
	    
	    outStream.flush();
	    outStream.close();
	}
	
	@RequestMapping(value = ConstantProviderService.modelDownload, method = RequestMethod.POST)
	public void modelDownload(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("modelId") long modelId) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		User u = userDao.findByEmail(request.getUserPrincipal().getName());		
		
		if (u.getGroup() == null) {
			return;
		}
		
		Model modelToDownload = modelDao.read(modelId);
		if ((modelToDownload == null) || (modelToDownload.getGroup().getId() != u.getGroup().getId())) {
			return;
		}
		
		response.setContentType("text/txt");
	    response.setHeader("Content-Disposition", "attachment;filename=" + modelToDownload.getFileName());

	    ServletOutputStream outStream = response.getOutputStream();
	    outStream.print(modelToDownload.getXml());	    
	    outStream.flush();
	    outStream.close();
	}

	@RequestMapping(value = ConstantProviderService.modelStats, method = RequestMethod.GET)
	public ModelAndView stats(HttpServletRequest request) {
		GameGroup g = userDao.findByEmail(request.getUserPrincipal().getName()).getGroup();		
		
		if (g == null) {
			ModelAndView mav = new ModelAndView();
			mav.setViewName("response");
			mav.addObject("alertType","alert-danger");
			mav.addObject("message", "You can only upload a model if you are in a group.");
			return mav;
		}
		
		List<PersistentKPI> ranking = persistentKPIDao.onDayOrdered(TimeUtils.yesterday());
		List<PersistentKPI> historicalKPIS = persistentKPIDao.allForGroup(g, TimeUtils.yesterday());
		
		ModelAndView mav = new ModelAndView();
		mav.setViewName("stats");

		//Ranking graph
		JsonArrayBuilder groupBuilder = Json.createArrayBuilder();
		JsonArrayBuilder ownScoreBuilder = Json.createArrayBuilder();
		JsonArrayBuilder othersScoreBuilder = Json.createArrayBuilder();
		PersistentKPI ownKPIs = null;
		PersistentKPI bestKPIs = ranking.isEmpty()?null:ranking.get(0);
		PersistentKPI minKPIs = new PersistentKPI(); minKPIs.setTotalCost(Double.MAX_VALUE); minKPIs.setAvgCustomerSatisfaction(Double.MAX_VALUE); minKPIs.setAvgThroughputTime(Double.MAX_VALUE); minKPIs.setAvgServiceTime(Double.MAX_VALUE); minKPIs.setAvgWaitingTime(Double.MAX_VALUE);
		PersistentKPI maxKPIs = new PersistentKPI(); maxKPIs.setTotalCost(Double.MIN_VALUE); maxKPIs.setAvgCustomerSatisfaction(Double.MIN_VALUE); maxKPIs.setAvgThroughputTime(Double.MIN_VALUE); maxKPIs.setAvgServiceTime(Double.MIN_VALUE); maxKPIs.setAvgWaitingTime(Double.MIN_VALUE);		
		for (PersistentKPI pkpi: ranking) {
			groupBuilder.add(pkpi.getGroup().getName());
			if (pkpi.getGroup().getId().equals(g.getId())) {
				ownScoreBuilder.add(pkpi.getTotalScore());
				othersScoreBuilder.add(0);
				ownKPIs = pkpi;
			} else {
				ownScoreBuilder.add(0);
				othersScoreBuilder.add(pkpi.getTotalScore());				
			}
			if (pkpi.getAvgCustomerSatisfaction() < minKPIs.getAvgCustomerSatisfaction()) { minKPIs.setAvgCustomerSatisfaction(pkpi.getAvgCustomerSatisfaction()); }
			if (pkpi.getTotalCost() < minKPIs.getTotalCost()) { minKPIs.setTotalCost(pkpi.getTotalCost()); }
			if (pkpi.getAvgThroughputTime() < minKPIs.getAvgThroughputTime()) { minKPIs.setAvgThroughputTime(pkpi.getAvgThroughputTime()); }
			if (pkpi.getAvgServiceTime() < minKPIs.getAvgServiceTime()) { minKPIs.setAvgServiceTime(pkpi.getAvgServiceTime()); }
			if (pkpi.getAvgWaitingTime() < minKPIs.getAvgWaitingTime()) { minKPIs.setAvgWaitingTime(pkpi.getAvgWaitingTime()); }
			
			if (pkpi.getAvgCustomerSatisfaction() > maxKPIs.getAvgCustomerSatisfaction()) { maxKPIs.setAvgCustomerSatisfaction(pkpi.getAvgCustomerSatisfaction()); }
			if (pkpi.getTotalCost() > maxKPIs.getTotalCost()) { maxKPIs.setTotalCost(pkpi.getTotalCost()); }
			if (pkpi.getAvgThroughputTime() > maxKPIs.getAvgThroughputTime()) { maxKPIs.setAvgThroughputTime(pkpi.getAvgThroughputTime()); }
			if (pkpi.getAvgServiceTime() > maxKPIs.getAvgServiceTime()) { maxKPIs.setAvgServiceTime(pkpi.getAvgServiceTime()); }
			if (pkpi.getAvgWaitingTime() > maxKPIs.getAvgWaitingTime()) { maxKPIs.setAvgWaitingTime(pkpi.getAvgWaitingTime()); }
		}
		mav.addObject("groups", groupBuilder.build());
		mav.addObject("ownScore", ownScoreBuilder.build());
		mav.addObject("othersScore", othersScoreBuilder.build());
		
		//Historical graphs
		JsonArrayBuilder datesBuilder = Json.createArrayBuilder();
		JsonArrayBuilder customerSatisfactionBuilder = Json.createArrayBuilder();
		JsonArrayBuilder totalCostBuilder = Json.createArrayBuilder();
		JsonArrayBuilder serviceTimeBuilder = Json.createArrayBuilder();
		JsonArrayBuilder throughputTimeBuilder = Json.createArrayBuilder();
		JsonArrayBuilder waitingTimeBuilder = Json.createArrayBuilder();
		for (PersistentKPI pkpi: historicalKPIS) {
			datesBuilder.add(pkpi.getTime());
			customerSatisfactionBuilder.add(pkpi.getAvgCustomerSatisfaction());
			totalCostBuilder.add(pkpi.getTotalCost());
			serviceTimeBuilder.add(TimeUtils.simulationTimeInHours(pkpi.getAvgServiceTime()));
			throughputTimeBuilder.add(TimeUtils.simulationTimeInHours(pkpi.getAvgThroughputTime()));
			waitingTimeBuilder.add(TimeUtils.simulationTimeInHours(pkpi.getAvgWaitingTime()));
		}
		mav.addObject("dates", datesBuilder.build());
		mav.addObject("customerSatisfaction", customerSatisfactionBuilder.build());
		mav.addObject("totalCost", totalCostBuilder.build());
		mav.addObject("serviceTime", serviceTimeBuilder.build());
		mav.addObject("throughputTime", throughputTimeBuilder.build());
		mav.addObject("waitingTime", waitingTimeBuilder.build());		
		
		//Radar graph
		JsonArrayBuilder radarDimensionsBuilder = Json.createArrayBuilder();
		JsonArrayBuilder radarSelfBuilder = Json.createArrayBuilder();
		JsonArrayBuilder radarBestBuilder = Json.createArrayBuilder();
		radarDimensionsBuilder.add("Satisfaction"); radarDimensionsBuilder.add("Cost"); radarDimensionsBuilder.add("Throughput"); radarDimensionsBuilder.add("Service"); radarDimensionsBuilder.add("Waiting");
		if ((ownKPIs != null) && (bestKPIs != null)) {
			double scaleSatisfaction = maxKPIs.getAvgCustomerSatisfaction() - minKPIs.getAvgCustomerSatisfaction();
			double scaleCost = maxKPIs.getTotalCost() - minKPIs.getTotalCost();
			double scaleThroughput = maxKPIs.getAvgThroughputTime() - minKPIs.getAvgThroughputTime();
			double scaleService = maxKPIs.getAvgServiceTime() - minKPIs.getAvgServiceTime();
			double scaleWaiting = maxKPIs.getAvgWaitingTime() - minKPIs.getAvgWaitingTime();
			//It only makes sense if the results are not too close
			if ((scaleSatisfaction > 0.1) && (scaleCost > 0.1) && (scaleThroughput > 0.1) && (scaleService > 0.1) && (scaleWaiting > 0.1)) {
				radarSelfBuilder.add((ownKPIs.getAvgCustomerSatisfaction() - minKPIs.getAvgCustomerSatisfaction()) / scaleSatisfaction);
				radarSelfBuilder.add((maxKPIs.getTotalCost() - ownKPIs.getTotalCost()) / scaleCost);
				radarSelfBuilder.add((maxKPIs.getAvgThroughputTime() - ownKPIs.getAvgThroughputTime()) / scaleThroughput);
				radarSelfBuilder.add((maxKPIs.getAvgServiceTime() - ownKPIs.getAvgServiceTime()) / scaleService);
				radarSelfBuilder.add((maxKPIs.getAvgWaitingTime() - ownKPIs.getAvgWaitingTime()) / scaleWaiting);
				radarBestBuilder.add((bestKPIs.getAvgCustomerSatisfaction() - minKPIs.getAvgCustomerSatisfaction()) / scaleSatisfaction);
				radarBestBuilder.add((maxKPIs.getTotalCost() - bestKPIs.getTotalCost()) / scaleCost);
				radarBestBuilder.add((maxKPIs.getAvgThroughputTime() - bestKPIs.getAvgThroughputTime()) / scaleThroughput);
				radarBestBuilder.add((maxKPIs.getAvgServiceTime() - bestKPIs.getAvgServiceTime()) / scaleService);
				radarBestBuilder.add((maxKPIs.getAvgWaitingTime() - bestKPIs.getAvgWaitingTime()) / scaleWaiting);
			}
		}
		mav.addObject("radarDimensions", radarDimensionsBuilder.build());
		mav.addObject("radarSelf", radarSelfBuilder.build());
		mav.addObject("radarBest", radarBestBuilder.build());		

		return mav;	
	}
	
	@RequestMapping(value = ConstantProviderService.modelCases, method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView cases(HttpServletRequest request,
			@RequestParam(required = false) String datetime
			) {
		GameGroup g = userDao.findByEmail(request.getUserPrincipal().getName()).getGroup();		
		
		if (g == null) {
			ModelAndView mav = new ModelAndView();
			mav.setViewName("response");
			mav.addObject("alertType","alert-danger");
			mav.addObject("message", "You can only upload a model if you are in a group.");
			return mav;
		}
		
		long currentTime = TimeUtils.currentTime();
		long today = TimeUtils.today();
		if (datetime != null) {
			Long parsedTime = TimeUtils.parseDateTime(datetime);			
			if ((parsedTime != null) && (parsedTime < currentTime)){
				currentTime = parsedTime;
				today = TimeUtils.parseDate(TimeUtils.formatDate(currentTime)); //This is a bit of a hacky way to get the start of the day, we only parse the date
			}
		}
		String formattedDateTime = TimeUtils.formatTime(currentTime);
				
		List<PersistentLogEvent> events = persistentLogEventDao.listByGroupOnDay(g, today);
		
		Set<String> casesStarted = new HashSet<String>();
		Set<String> casesNotCompleted = new HashSet<String>();
		Set<String> casesBusy = new HashSet<String>();
		
		List<TaskStateViewModel> taskStates = new ArrayList<TaskStateViewModel>();
		Map<String,Long> employeeTime = new HashMap<String, Long>();
		for (PersistentLogEvent event: events) {
			if (event.getStartTime() <= currentTime) {
				casesStarted.add(event.getCaseID());
			}
			if (event.getCompletionTime() >= currentTime) {
				casesNotCompleted.add(event.getCaseID());
			} else {
				if (event.getResource() != null) {
					Long t = employeeTime.get(event.getResource());
					if (t == null) {
						t = 0l;
					}
					employeeTime.put(event.getResource(), t + event.getCompletionTime() - event.getStartTime());
				}
			}
			if ((event.getStartTime() <= currentTime) && (event.getCompletionTime() >= currentTime)) {
				taskStates.add(new TaskStateViewModel(event.getEvent(), event.getCaseID(), event.getResource(), (currentTime - event.getStartTime())/60000));
				casesBusy.add(event.getCaseID());
			}
		}

		ModelAndView mav = new ModelAndView();
		mav.setViewName("cases");
		mav.addObject("taskStates", taskStates);
		
		JsonArrayBuilder employeeNames = Json.createArrayBuilder();		
		JsonArrayBuilder employeeTimes = Json.createArrayBuilder();
		for (Map.Entry<String,Long> et: employeeTime.entrySet()) {
			employeeNames.add(et.getKey());
			employeeTimes.add(et.getValue());
		}
		mav.addObject("employeeNames", employeeNames.build());
		mav.addObject("employeeTimes", employeeTimes.build());
		
		List<CaseStateViewModel> caseStates = new ArrayList<CaseStateViewModel>();
		int idleCases = 0;
		int busyCases = 0;
		int completedCases = 0;
		for (String startedCase: casesStarted) {
			if (casesBusy.contains(startedCase)) {
				busyCases ++;
				caseStates.add(new CaseStateViewModel(startedCase, "X", "", ""));
			}else if (casesNotCompleted.contains(startedCase)) {
				idleCases ++;
				caseStates.add(new CaseStateViewModel(startedCase, "", "X", ""));
			}else {
				completedCases ++;
				caseStates.add(new CaseStateViewModel(startedCase, "", "", "X"));
			}
		}
		Collections.sort(caseStates);
		
		mav.addObject("caseStates", caseStates);
		mav.addObject("busyCases", busyCases);
		mav.addObject("completedCases", completedCases);
		mav.addObject("idleCases", idleCases);
		mav.addObject("datetime", formattedDateTime);
		return mav;
	}
}