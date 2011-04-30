/*	
 *  Copyright (c) 2009-@year@. The GUITAR group at the University of Maryland. Names of owners of this group may
 *  be obtained by sending an e-mail to atif@cs.umd.edu
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 *  documentation files (the "Software"), to deal in the Software without restriction, including without 
 *  limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *	the Software, and to permit persons to whom the Software is furnished to do so, subject to the following 
 *	conditions:
 * 
 *	The above copyright notice and this permission notice shall be included in all copies or substantial 
 *	portions of the Software.
 *
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
 *	LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO 
 *	EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
 *	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
 *	THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
 */
package edu.umd.cs.guitar.replayer;

import org.kohsuke.args4j.Option;

import edu.umd.cs.guitar.ripper.SitarConfiguration;

/**
 * Configuration specific to {@link SitarReplayer}. The configuration options
 * held by this class can be set through its setter methods or by passing
 * and instance of this class to an Args4j {@code CmdLineParser}.
 * 
 * @author Gabe Gorelick
 * 
 * @see SitarReplayerMain
 */
public class SitarReplayerConfiguration extends SitarConfiguration {

	// GUITAR runtime parameters	
	@Option(name = "-e", usage = "EFG file path", aliases = "--efg-file", required = true)
	private String efgFile = null;

	// this option is slightly different from ripper's
	@Option(name = "-g", usage = "GUI file path", aliases = "--gui-file", required = true)
	private String guiFile = "GUITAR-Default.GUI"; 
	
	@Option(name = "-t", usage = "testcase file path", aliases = "--testcase-file", required = true)
	private String testcase = null;

	@Option(name = "-gs", usage = "gui state file path", aliases = "--gui-state")
	private String guiStateFile = "GUITAR-Default.STA";

	@Option(name = "-d", usage = "step delay time", aliases = "--delay")
	private int delay = 0;

	@Option(name = "-to", usage = "testcase timeout", aliases = "--testcase-timeout")
	private int testCaseTimeout = 30000;

	@Option(name = "-so", usage = "test steptimeout", aliases = "--teststep-timeout")
	private int testStepTimeout = 4000;
	
	// Application Under Test
	
	@Option(name = "-p", usage = "Pause after each step", aliases = "--pause")
	private boolean pause = false;
	
	@Option(name = "-r", usage = "Compare string using regular expression", aliases = "--regular-expression")
	private boolean regUsed= false;
	
	// getters and setters
	
	public void setGuiFile(String guiFile) {
		this.guiFile = guiFile;
	}

	public String getGuiFile() {
		return guiFile;
	}
	
	public String getEfgFile() {
		return efgFile;
	}

	public void setEfgFile(String efgFile) {
		this.efgFile = efgFile;
	}

	public String getTestcase() {
		return testcase;
	}

	public void setTestcase(String testcase) {
		this.testcase = testcase;
	}

	public String getGuiStateFile() {
		return guiStateFile;
	}

	public void setGuiStateFile(String guiStateFile) {
		this.guiStateFile = guiStateFile;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public int getTestCaseTimeout() {
		return testCaseTimeout;
	}

	public void setTestCaseTimeout(int testCaseTimeout) {
		this.testCaseTimeout = testCaseTimeout;
	}

	public int getTestStepTimeout() {
		return testStepTimeout;
	}

	public void setTestStepTimeout(int testStepTimeout) {
		this.testStepTimeout = testStepTimeout;
	}

	public boolean getPause() {
		return pause;
	}

	public void setPause(boolean pause) {
		this.pause = pause;
	}

	public boolean getRegUsed() {
		return regUsed;
	}

	public void setRegUsed(boolean regUsed) {
		this.regUsed = regUsed;
	}	

}
