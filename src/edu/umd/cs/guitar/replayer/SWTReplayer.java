/*  
 *  Copyright (c) 2009-@year@. The GUITAR group at the University of Maryland. Names of owners of this group may
 *  be obtained by sending an e-mail to atif@cs.umd.edu
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 *  documentation files (the "Software"), to deal in the Software without restriction, including without 
 *  limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so, subject to the following 
 *  conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in all copies or substantial 
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
 *  LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO 
 *  EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
 */
package edu.umd.cs.guitar.replayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;

import org.kohsuke.args4j.CmdLineException;
import org.xml.sax.SAXException;

import edu.umd.cs.guitar.exception.GException;
import edu.umd.cs.guitar.model.GIDGenerator;
import edu.umd.cs.guitar.model.GUITARConstants;
import edu.umd.cs.guitar.model.IO;
import edu.umd.cs.guitar.model.SWTApplication;
import edu.umd.cs.guitar.model.SWTConstants;
import edu.umd.cs.guitar.model.SWTDefaultIDGenerator;
import edu.umd.cs.guitar.model.data.AttributesType;
import edu.umd.cs.guitar.model.data.ComponentListType;
import edu.umd.cs.guitar.model.data.ComponentType;
import edu.umd.cs.guitar.model.data.Configuration;
import edu.umd.cs.guitar.model.data.FullComponentType;
import edu.umd.cs.guitar.model.data.TestCase;
import edu.umd.cs.guitar.model.wrapper.AttributesTypeWrapper;
import edu.umd.cs.guitar.model.wrapper.ComponentTypeWrapper;
import edu.umd.cs.guitar.replayer.monitor.GTestMonitor;
import edu.umd.cs.guitar.replayer.monitor.PauseMonitor;
import edu.umd.cs.guitar.replayer.monitor.StateMonitorFull;
import edu.umd.cs.guitar.replayer.monitor.TimeMonitor;
import edu.umd.cs.guitar.util.GUITARLog;

public class SWTReplayer {
	
	private final SWTReplayerConfiguration config;
	private final SWTApplication application;
	
	public SWTReplayer(SWTReplayerConfiguration config, Thread guiThread) {
		this.config = config;
		this.application = new SWTApplication(config.getMainClass(), guiThread);
	}

	public void execute() throws CmdLineException {

		long nStartTime = System.currentTimeMillis();
		checkArgs();
		setupEnv();

		System.setProperty(GUITARLog.LOGFILE_NAME_SYSTEM_PROPERTY, config.getLogFile());
		printInfo();

		TestCase tc = (TestCase) IO.readObjFromFile(config.getTestcase(), TestCase.class);

		Replayer replayer;
		try {
			if (tc == null) {
				GUITARLog.log.error("Test case not found");
				throw new FileNotFoundException();
			}

			replayer = new Replayer(tc, config.getGuiFile(), config.getEfgFile());
			GReplayerMonitor sMonitor = new SWTReplayerMonitor(config, application);
			
			GTestMonitor stateMonitor = new StateMonitorFull(config.getGuiStateFile(),
					                                         config.getDelay());
			
			GIDGenerator idGenerator = SWTDefaultIDGenerator.getInstance();
			((StateMonitorFull)stateMonitor).setIdGenerator(idGenerator);
			
			replayer.addTestMonitor(stateMonitor);

			// Add a pause monitor and ignore time out monitor if needed
			if (config.getPause()) {
				GTestMonitor pauseMonitor = new PauseMonitor();
				replayer.addTestMonitor(pauseMonitor);
			} else {
				// Add a timeout monitor
				GTestMonitor timeoutMonitor = new TimeMonitor(config.getTestStepTimeout(),
						                                      config.getTestCaseTimeout());
				replayer.addTestMonitor(timeoutMonitor);
			}

//			// Add a Cobertura code coverage collector
//			boolean isMeasureCoverage = (SWTReplayerConfiguration.COVERAGE_DIR != null
//					                     && SWTReplayerConfiguration.COVERAGE_CLEAN_FILE != null);
//
//			if (isMeasureCoverage) {
//				GTestMonitor coverageMonitor = new CoberturaCoverageMonitor(SWTReplayerConfiguration.COVERAGE_CLEAN_FILE,
//						                                                    SWTReplayerConfiguration.COVERAGE_DIR);
//				replayer.addTestMonitor(coverageMonitor);
//			}

			// Set up string comparator
//			jMonitor.setUseReg(SWTReplayerConfiguration.REG_USED);
			
			replayer.setMonitor(sMonitor);
			replayer.setTimeOut(config.getTestCaseTimeout());

			replayer.execute();
			
			GUITARLog.log.info("NORMALLY TERMINATED");

		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (GException e) {
			GUITARLog.log.error("GUITAR Exception thrown", e);
		} catch (Exception e) {
			GUITARLog.log.error("General Exception thrown", e);
		}

		// Elapsed time:
		long nEndTime = System.currentTimeMillis();
		long nDuration = nEndTime - nStartTime;
		DateFormat df = new SimpleDateFormat("HH : mm : ss: SS");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		GUITARLog.log.info("Time Elapsed: " + df.format(nDuration));

		printInfo();
	}

	private void printInfo() {
		GUITARLog.log.info("Testcase: " + config.getTestcase());
		GUITARLog.log.info("Log file: " + config.getLogFile());
		GUITARLog.log.info("GUI state file: " + config.getGuiStateFile());
	}

	/**
	 * 
	 * Check for command-line arguments
	 * 
	 * @throws CmdLineException
	 * 
	 */
	private void checkArgs() throws CmdLineException {
		// Check argument
		if (GReplayerConfiguration.HELP) {
			throw new CmdLineException("");
		}

		boolean isPrintUsage = false;

		if (config.getMainClass() == null) {
			System.err.println("missing '-c' argument");
			isPrintUsage = true;
		}

		if (config.getGuiFile() == null) {
			System.err.println("missing '-g' argument");
			isPrintUsage = true;
		}

		if (config.getEfgFile() == null) {
			System.err.println("missing '-e' argument");
			isPrintUsage = true;
		}

		if (config.getTestcase() == null) {
			System.err.println("missing '-t' argument");
			isPrintUsage = true;
		}
		
//		boolean isNotMeasureCoverage = SWTReplayerConfiguration.COVERAGE_DIR == null
//				                       && SWTReplayerConfiguration.COVERAGE_CLEAN_FILE == null;
//		boolean isMeasureCoverage = SWTReplayerConfiguration.COVERAGE_DIR != null
//				                    && SWTReplayerConfiguration.COVERAGE_CLEAN_FILE != null;
//
//		if (!isMeasureCoverage && !isNotMeasureCoverage) {
//			System.err
//					.println("'-cd,-cc' should be either all set or all unset");
//			isPrintUsage = true;
//		}

		if (isPrintUsage)
			throw new CmdLineException("");
	}

	/**
     * 
     */
	private void setupEnv() {
		// Terminal list
		// Try to find absolute path first then relative path

		Configuration conf;

		conf = (Configuration) IO.readObjFromFile(
				config.getConfigFile(), Configuration.class);
		if (conf == null) {
			InputStream in = getClass().getClassLoader().getResourceAsStream(
					config.getConfigFile());
			conf = (Configuration) IO.readObjFromFile(in, Configuration.class);
		}

		List<FullComponentType> cTerminalList = conf.getTerminalComponents()
				.getFullComponent();

		for (FullComponentType cTermWidget : cTerminalList) {
			ComponentType component = cTermWidget.getComponent();
			AttributesType attributes = component.getAttributes();
			if (attributes != null)
				SWTConstants.sTerminalWidgetSignature
						.add(new AttributesTypeWrapper(component
								.getAttributes()));
		}

		List<FullComponentType> lIgnoredComps = new ArrayList<FullComponentType>();
		List<String> ignoredWindow = new ArrayList<String>();

		ComponentListType ignoredAll = conf.getIgnoredComponents();

		if (ignoredAll != null)
			for (FullComponentType fullComp : ignoredAll.getFullComponent()) {
				ComponentType comp = fullComp.getComponent();

				if (comp == null) {
					ComponentType win = fullComp.getWindow();
					ComponentTypeWrapper winAdapter = new ComponentTypeWrapper(
							win);
					String ID = winAdapter
							.getFirstValueByName(GUITARConstants.ID_TAG_NAME);
					if (ID != null)
						SWTConstants.sIgnoredWins.add(ID);

				} else
					lIgnoredComps.add(fullComp);
			}
	}
}