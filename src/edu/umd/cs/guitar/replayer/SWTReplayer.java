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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import edu.umd.cs.guitar.model.GIDGenerator;
import edu.umd.cs.guitar.model.IO;
import edu.umd.cs.guitar.model.SWTDefaultIDGenerator;
import edu.umd.cs.guitar.model.data.TestCase;
import edu.umd.cs.guitar.replayer.monitor.GTestMonitor;
import edu.umd.cs.guitar.replayer.monitor.PauseMonitor;
import edu.umd.cs.guitar.replayer.monitor.StateMonitorFull;
import edu.umd.cs.guitar.replayer.monitor.TimeMonitor;
import edu.umd.cs.guitar.ripper.SWTApplicationRunner;
import edu.umd.cs.guitar.ripper.SWTGuitarExecutor;
import edu.umd.cs.guitar.util.GUITARLog;

/**
 * Adapts a {@link Replayer} for use with SWT GUIs. 
 * 
 * @author Gabe Gorelick
 *
 */
public class SWTReplayer extends SWTGuitarExecutor {

	private final SWTReplayerConfiguration config;
	private final SWTReplayerMonitor monitor;
	private final Replayer replayer;

	/**
	 * Constructs a new <code>SWTReplayer</code>. This constructor is equivalent
	 * to
	 * 
	 * <pre>
	 * SWTReplayer(config, Thread.currentThread())
	 * </pre>
	 * 
	 * Consequently, this constructor must be called on the same thread that the
	 * application under test is running on (usually the <code>main</code>
	 * thread).
	 * 
	 * @param config
	 *            configuration
	 * 
	 * @see SWTApplicationRunner
	 */
	public SWTReplayer(SWTReplayerConfiguration config) {
		this(config, Thread.currentThread());
	}
	
	/**
	 * Constructs a new <code>SWTRreplayer</code>. The thread passed in is the
	 * thread on which the SWT application under test runs. This is almost
	 * always the <code>main</code> thread (and actually must be the
	 * <code>main</code> thread on Cocoa).
	 * 
	 * @param config
	 *            configuration
	 * @param guiThread
	 *            thread the GUI runs on
	 * 
	 * @see SWTApplicationRunner
	 */
	public SWTReplayer(SWTReplayerConfiguration config, Thread guiThread) {
		super(config, guiThread);
		this.config = config;
		this.monitor = new SWTReplayerMonitor(config, getApplication());
		replayer = initReplayer();
	}

	/**
	 * Initializes the replayer
	 * 
	 */
	private Replayer initReplayer() {
		TestCase tc = (TestCase) IO.readObjFromFile(config.getTestcase(), TestCase.class);
		if (tc == null) {
			GUITARLog.log.error("Test case not found");
			throw new RuntimeException(); // TODO throw better exception
		}
		
		Replayer replayer = null;
		
		try {
			replayer = new Replayer(tc, config.getGuiFile(), config.getEfgFile());
			
			// TODO subclass StateMonitorFull and remove dependency on Jemmy
			StateMonitorFull stateMonitor = new StateMonitorFull(
					config.getGuiStateFile(), config.getDelay());

			GIDGenerator idGenerator = SWTDefaultIDGenerator.getInstance();
			stateMonitor.setIdGenerator(idGenerator);

			replayer.addTestMonitor(stateMonitor);
			
			// Add a pause monitor and ignore time out monitor if needed
			if (config.getPause()) {
				GTestMonitor pauseMonitor = new PauseMonitor();
				replayer.addTestMonitor(pauseMonitor);
			} else {
				// Add a timeout monitor
				GTestMonitor timeoutMonitor = new TimeMonitor(
						config.getTestStepTimeout(),
						config.getTestCaseTimeout());
				
				replayer.addTestMonitor(timeoutMonitor);
			}
			
			replayer.setMonitor(monitor);
			replayer.setTimeOut(config.getTestCaseTimeout());
			
		} catch (ParserConfigurationException e) {
			GUITARLog.log.error(e);
		} catch (SAXException e) {
			GUITARLog.log.error(e);
		} catch (IOException e) {
			GUITARLog.log.error(e);
		}
		
		return replayer;		
	}
	
	/**
	 * Do some logging before the replayer is executed.
	 */
	@Override
	protected void onBeforeExecute() {
		// do setup
		super.onBeforeExecute();
		
		GUITARLog.log.info("Testcase: " + config.getTestcase());
		GUITARLog.log.info("Log file: " + config.getLogFile());
		GUITARLog.log.info("GUI state file: " + config.getGuiStateFile());
	}
	
	/**
	 * Execute the replayer.
	 * 
	 * @see Replayer#execute()
	 */
	@Override
	public void onExecute() {
		try {		
			replayer.execute();
		} catch (Exception e) {
			GUITARLog.log.error(e);
		} 		
	}
	
	/**
	 * Do some logging after the replayer has finished.
	 */
	@Override
	protected void onAfterExecute() {
		GUITARLog.log.info("NORMALLY TERMINATED");
		
		// print time elapsed
		super.onAfterExecute();		
	}

	/**
	 * Get the <code>SWTReplayerMonitor</code> associated with this
	 * <code>SWTReplayer</code>.
	 * 
	 * @return the monitor used to communicate with the GUI
	 */
	public SWTReplayerMonitor getMonitor() {
		return monitor;
	}

}