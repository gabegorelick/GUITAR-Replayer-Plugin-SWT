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

import edu.umd.cs.guitar.util.Util;


public class SWTReplayerConfiguration extends GReplayerConfiguration{

	@Option(name = "-cf", usage = "Configure file for the gui recorder to recognize the terminal widgets", aliases = "--configure-file")
	private String configFile = "configuration.xml";

	// GUITAR runtime parameters
	@Option(name = "-g", usage = "<REQUIRED> GUI file path", aliases = "--gui-file")
	private String guiFile = null;

	@Option(name = "-e", usage = "<REQUIRED> EFG file path", aliases = "--efg-file")
	private String efgFile = null;

	@Option(name = "-t", usage = "<REQUIRED> testcase file path", aliases = "--testcase-file")
	private String testcase = null;

	@Option(name = "-gs", usage = "gui state file path", aliases = "--gui-state")
	private String guiStateFile = "GUITAR-Default.STA";

	@Option(name = "-l", usage = "log file name ", aliases = "--log-file")
	private String logFile = Util.getTimeStamp() + ".log";;

	@Option(name = "-i", usage = "initial waiting time for the application to get stablized before being ripped", aliases = "--wait-time")
	private int initialWaitTime = 0;

	@Option(name = "-d", usage = "step delay time", aliases = "--delay")
	private int delay = 0;

	@Option(name = "-to", usage = "testcase timeout", aliases = "--testcase-timeout")
	private int testCaseTimeout = 30000;

	@Option(name = "-so", usage = "test steptimeout", aliases = "--teststep-timeout")
	private int testStepTimeout = 4000;
	
	// Application Under Test
	@Option(name = "-c", usage = "<REQUIRED> main class name for the Application Under Test ", aliases = "--main-class")
	private String mainClass = null;

	@Option(name = "-a", usage = "arguments for the Application Under Test, separated by ';' ", aliases = "--arguments")
	private String argumentList;

	@Option(name = "-u", usage = "URLs for the Application Under Test, separated by ';' ", aliases = "--urls")
	private String urlList;

	@Option(name = "-p", usage = "Pause after each step", aliases = "--pause")
	private boolean pause = false;
	
	@Option(name = "-r", usage = "Compare string using regular expression", aliases = "--regular-expression")
	private boolean regUsed= false;

	// Cobertura Coverage collection
//	@Option(name = "-cd", usage = "Cobertura coverage output dir", aliases = "--coverage-dir")
//	private String COVERAGE_DIR = null;
//
//	@Option(name = "-cc", usage = "Cobertura coverage clean file ", aliases = "--coverage-clean")
//	private String COVERAGE_CLEAN_FILE = null;
	
	
	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public String getGuiFile() {
		return guiFile;
	}

	public void setGuiFile(String guiFile) {
		this.guiFile = guiFile;
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

	public String getLogFile() {
		return logFile;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}

	public int getInitialWaitTime() {
		return initialWaitTime;
	}

	public void setInitialWaitTime(int initialWaitTime) {
		this.initialWaitTime = initialWaitTime;
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

	public String getMainClass() {
		return mainClass;
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	public String getArgumentList() {
		return argumentList;
	}

	public void setArgumentList(String argumentList) {
		this.argumentList = argumentList;
	}

	public String getUrlList() {
		return urlList;
	}

	public void setUrlList(String urlList) {
		this.urlList = urlList;
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
