package nl.tue.bpmgame.simulator;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.SingleUnitTimeFormatter;
import desmoj.core.simulator.TimeInstant;
import nl.tue.bpmgame.assignment.Assignment;
import nl.tue.bpmgame.dataaccess.config.ApplicationContext;
import nl.tue.bpmgame.dataaccess.model.LogAttribute;
import nl.tue.bpmgame.dataaccess.model.Model;
import nl.tue.bpmgame.dataaccess.model.PersistentKPI;
import nl.tue.bpmgame.dataaccess.model.PersistentLogEvent;
import nl.tue.bpmgame.dataaccess.service.DAOService;
import nl.tue.bpmgame.executionmodel.Case;
import nl.tue.bpmn.concepts.BPMNModel;
import nl.tue.bpmn.concepts.BPMNRole;
import nl.tue.bpmn.parser.BPMNParseException;
import nl.tue.bpmn.parser.BPMNParser;
import nl.tue.util.TimeUtils;

public class Simulator {
	
	public static SimulatorModel simulateModel(String model, List<Case> cases, long startWorkdayTime, long stopWorkdayTime) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException, BPMNParseException {
		//Load the model
		BPMNParser parser = new BPMNParser();
		parser.parse(model);
		BPMNModel bpmnModel = parser.getParsedModel();
		//Fix the model
		for (BPMNRole role: bpmnModel.getRoles()) {
			if (role.getResources().isEmpty()) {
				role.addResource(Assignment.GENERAL_MANAGER);
			}
		}
				
		//Instantiate the simulation model
		SimulatorModel simulatorModel = new SimulatorModel(bpmnModel, cases, startWorkdayTime);
		
		//run the simulation model
		Experiment.setTimeFormatter(new SingleUnitTimeFormatter(TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS, 0, true));
		Experiment experiment = new Experiment("Experiment", false);		
		simulatorModel.connectToExperiment(experiment);

		experiment.setShowProgressBar(false);
		//experiment.stop(new TimeInstant(stopWorkdayTime, TimeUnit.MILLISECONDS));
		//experiment.tracePeriod(new TimeInstant(0), new TimeInstant(0));
		//experiment.debugPeriod(new TimeInstant(0), new TimeInstant(0));
		experiment.setSilent(true);

		experiment.start(new TimeInstant(0, TimeUnit.MILLISECONDS));
		
		return simulatorModel;
	}
		
	/**
	 * Simulates the assignment for the specified workday (in milliseconds since 1 jan 1970, assuming 00:00 of the day is specified)
	 * for each model in the database.
	 * The start (time in milliseconds since 00:00) of the workday 
	 * and the duration of the workday (in milliseconds) is specified in the assignment.
	 * The duration of case generation (in milliseconds) is also specified in the assignment. 
	 * 
	 * @param assignment the fully qualified name of the class of the assignment
	 * @param isDryrun if isDryrun the results are not stored in the database, 
	 *                 but all 'active' models that succeed on the dry run are marked as 'safe'
	 *                 all other models are not marked as 'safe'
	 * @param onlySafeModels if onlySafeModels 'safe' models are run (see isDryRun), normally 'active' models are run 
	 * @param workday the workday to simulate the models for.
	 */
	public static void simulate(String assignment, boolean isDryrun, boolean onlySafeModels, long workday) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException, BPMNParseException {
		Logger logger = Logger.getLogger(Simulator.class);
		
		//Initialize the assignment for all models
		Assignment.initializeAssignment(assignment);
				
		long startWorkdayTime = workday + Assignment.getAssignment().getWorkdayStart(); //in milliseconds since 1 jan 1970
		long stopGenerationTime = startWorkdayTime + Assignment.getAssignment().getGenerationDuration(); //in milliseconds since 1 jan 1970
		long stopWorkdayTime = startWorkdayTime + Assignment.getAssignment().getWorkdayDuration(); //in milliseconds since 1 jan 1970

		//Instantiate the cases for all models
		CaseGenerator cg = new CaseGenerator();
		cg.generateCases(startWorkdayTime, stopGenerationTime);

		//keep track of minimum and maximum scores for later normalization and total score calculation
		double minAvgThroughputTime = Double.MAX_VALUE, maxAvgThroughputTime = Double.MIN_VALUE;
		double minAvgCustomerSatisfaction = Double.MAX_VALUE, maxAvgCustomerSatisfaction = Double.MIN_VALUE;
		double minTotalCost = Double.MAX_VALUE, maxTotalCost = Double.MIN_VALUE;		
		
		//if isDryRun, first clear all the safe flags
		if (isDryrun) {
			for (Model m: DAOService.modelDAO().safeModels()) {
				m.setSafe(false);
				DAOService.modelDAO().update(m);
			}
		}
		
		//for each safe/active model in the database (safe models, if onlySafeModels, active models otherwise)
		List<Model> modelsToSimulate = onlySafeModels?DAOService.modelDAO().safeModels():DAOService.modelDAO().activeModels();
		int iModel = 1;
		int nrModels = modelsToSimulate.size();
		Set<Long> groups = new HashSet<Long>();
		for (Model model: modelsToSimulate) {
			if (groups.contains(model.getGroup().getId())) {
				logger.error("ERROR: Group '" + model.getGroup().getName() + "' has multiple active models.");
			}
			groups.add(model.getGroup().getId());
			long startTime = System.currentTimeMillis();
			logger.info("INFO: Simulating model " + iModel + " of " + nrModels + " with ID " + model.getId());
			iModel++;
			//simulate the model
			SimulatorModel result = simulateModel(model.getXml(), cg.getCases(), startWorkdayTime, stopWorkdayTime);
			long duration = (System.currentTimeMillis() - startTime)/1000;
			logger.info(" took " + duration + " s.");

			if (!isDryrun) {
				//store the generated log in the database
				for (LogEvent le: result.getLog()) {
					PersistentLogEvent ple = new PersistentLogEvent();
					ple.setCaseID(le.getCaseID());
					ple.setStartTime(le.getStartTime());
					ple.setCompletionTime(le.getCompletionTime());
					ple.setEvent(le.getNodeLabel());
					ple.setResource(le.getResourceName());
					ple.setGroup(model.getGroup());
					DAOService.persistentLogEventDAO().create(ple);
					for (String attributeName: le.getAttributes()) {
						LogAttribute pa = new LogAttribute();
						pa.setEvent(ple);
						pa.setAttributeName(attributeName);
						pa.setAttributeValue(le.getAttributeValue(attributeName));
						DAOService.logAttributeDAO().create(pa);
						ple.addAttribute(pa);
					}
					DAOService.persistentLogEventDAO().update(ple);
				}
				
				//store the KPI's in the database, all KPI's are stored by date/group combination
				PersistentKPI pkpi = new PersistentKPI();
				pkpi.setTime(workday);
				pkpi.setGroup(model.getGroup());
				pkpi.setAvgCustomerSatisfaction(result.avgCustomerSatisfaction());
				pkpi.setAvgServiceTime(result.avgServiceTime());
				pkpi.setAvgThroughputTime(result.avgThroughputTime());
				pkpi.setAvgWaitingTime(result.avgWaitingTime());
				pkpi.setTotalCost(result.totalCost());
				DAOService.persistentKPIDAO().create(pkpi);
				
				//Update the min/max KPIs
				if (result.avgCustomerSatisfaction() > maxAvgCustomerSatisfaction) maxAvgCustomerSatisfaction = result.avgCustomerSatisfaction();
				if (result.avgCustomerSatisfaction() < minAvgCustomerSatisfaction) minAvgCustomerSatisfaction = result.avgCustomerSatisfaction();
				if (result.avgThroughputTime() > maxAvgThroughputTime) maxAvgThroughputTime = result.avgThroughputTime();
				if (result.avgThroughputTime() < minAvgThroughputTime) minAvgThroughputTime = result.avgThroughputTime();
				if (result.totalCost() > maxTotalCost) maxTotalCost = result.totalCost();
				if (result.totalCost() < minTotalCost) minTotalCost = result.totalCost();
			} else {
				model.setSafe(true);
				DAOService.modelDAO().update(model);
			}
		}
		
		//Calculate and store total scores
		for (PersistentKPI pkpi: DAOService.persistentKPIDAO().kpisAtTime(workday)) {
			
			double customerSatisfactionScore = pkpi.getAvgCustomerSatisfaction(); //higher is better
			if (minAvgCustomerSatisfaction == maxAvgCustomerSatisfaction) {
				customerSatisfactionScore = 1.0;
			} else {
				customerSatisfactionScore = (customerSatisfactionScore - minAvgCustomerSatisfaction)/(maxAvgCustomerSatisfaction - minAvgCustomerSatisfaction);  
			}
			
			double totalCostScore = pkpi.getTotalCost(); //lower is better
			if (minTotalCost == maxTotalCost) {
				totalCostScore = 1.0;
			} else {
				totalCostScore = (maxTotalCost - totalCostScore)/(maxTotalCost - minTotalCost);
			}
			
			double throughputTimeScore = pkpi.getAvgThroughputTime(); //lower is better
			if (minAvgThroughputTime == maxAvgThroughputTime) {
				throughputTimeScore = 1.0;
			} else {
				throughputTimeScore = (maxAvgThroughputTime - throughputTimeScore)/(maxAvgThroughputTime - minAvgThroughputTime);
			}
			
			double totalScore = (customerSatisfactionScore + totalCostScore + throughputTimeScore) / 3.0;
			if (Double.isInfinite(totalScore) || Double.isNaN(totalScore)) {
				totalScore = 1.0;
			}
			pkpi.setTotalScore(totalScore);
			
			DAOService.persistentKPIDAO().update(pkpi);
		}
	}
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException, BPMNParseException, IOException {
		Properties props = new Properties();
		Class<?> cls = Class.forName("nl.tue.bpmgame.simulator.Simulator");
        ClassLoader cLoader = cls.getClassLoader();
		props.load(cLoader.getResourceAsStream("nl/tue/bpmgame/bpmgamegui/resources/log4j.simulator.properties"));
		PropertyConfigurator.configure(props);
		
		boolean isDryRun = true;
		boolean onlySafeModels = false;
		boolean initialize = false;
		long startDay = TimeUtils.tomorrow();
		
		if ((args.length > 0) && (args[0].equals("-help") || args[0].equals("-h") || args[0].equals("/?"))){
			System.out.println("Simulates the models in the database. There are three modes of execution, indicated by flags:\n"
					+ "-runDry     simulates all active models, marks the succesful ones as safe, does not store results (default behavior)\n"
					+ "-runActive  simulates all active models, stores results\n"
					+ "-runSafe    simulates all safe models, stores results\n"
					+ "\n"
					+ "Additional flags:\n"
					+ "-startDay <offset>      day for which to run the simulation as offset from today (defaults -startDay 1, i.e. tomorrow)\n"
					+ "-initialize             initializes a new database (WARNING: this will clear all your data)");
			return;
		}
		
		for (int i = 0; i < args.length; i++) {
			String flag = args[i];
			if (flag.equals("-runActive")) {
				isDryRun = false;
				onlySafeModels = false;
			}else if (flag.equals("-runSafe")) {
				isDryRun = false;
				onlySafeModels = true;				
			}else if (flag.equals("-runDry")) {
			}else if (flag.equals("-startDay")) {
				try {
					long offset = Long.parseLong(args[i+1]);
					startDay = TimeUtils.today() + offset * TimeUtils.DAY;
					i++;
				} catch (Exception e) {
					System.out.println("-startDay specified without a properly formatted offet.");
					return;
				}
			}else if (flag.equals("-initialize")) {
				initialize = true;
			} else {
				System.out.println("Invalid flag: " + flag);
				return;				
			}
		}
		
		if (initialize) {
			ApplicationContext.initializeNewDatabase();
		}
		
		simulate(Assignment.ACTIVE_ASSIGNMENT, isDryRun, onlySafeModels, startDay);
		
		ApplicationContext.getApplicationContext().close();
		//System.exit should not be necessary,
		//but somehow the simulator does not close properly,
		//probably due to some Hibernate leftovers.
		//TODO: gracefully shut down.
		System.exit(0);
	}

}
