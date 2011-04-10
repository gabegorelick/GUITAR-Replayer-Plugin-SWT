package edu.umd.cs.guitar.replayer;

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

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Shell;

import edu.umd.cs.guitar.event.EventManager;
import edu.umd.cs.guitar.event.GEvent;
import edu.umd.cs.guitar.exception.ApplicationConnectException;
import edu.umd.cs.guitar.model.GUITARConstants;
import edu.umd.cs.guitar.model.GWindow;
import edu.umd.cs.guitar.model.SWTApplication;
import edu.umd.cs.guitar.model.SWTConstants;
import edu.umd.cs.guitar.model.SWTWindow;
import edu.umd.cs.guitar.model.data.AttributesType;
import edu.umd.cs.guitar.model.data.ComponentType;
import edu.umd.cs.guitar.model.data.PropertyType;
import edu.umd.cs.guitar.util.GUITARLog;

/**
 * Replayer monitor for Java Swing (JFC) application
 * 
 * <p>
 * 
 * @author <a href="mailto:baonn@cs.umd.edu"> Bao Nguyen </a>
 */
public class SWTReplayerMonitor extends GReplayerMonitor {

	private static final int INITIAL_DELAY = 1000;
	String MAIN_CLASS;

	private SWTReplayerConfiguration config;

	/**
	 * Delay for widget searching loop
	 */
	private static final int DELAY_STEP = 50;

	private final SWTApplication application;

	public SWTReplayerMonitor(GReplayerConfiguration config,
			SWTApplication application) {
		this.config = (SWTReplayerConfiguration) config;
		MAIN_CLASS = this.config.MAIN_CLASS;
		this.application = application;
	}

	/**
	 * Class used to disable System.exit()
	 * 
	 * @author Bao Nguyen
	 * 
	 */
	private static class ExitTrappedException extends SecurityException {
		private static final long serialVersionUID = 1L;
	}

	SecurityManager oldSecurityManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cs.guitar.replayer.AbsReplayerMonitor#setUp()
	 */
	@Override
	public void setUp() {
		GUITARLog.log.info("Setting up SWTReplayer...");
		// -------------------------------------
		// Add handler for all uncaught exceptions
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				GUITARLog.log.error("Uncaught exception", e);
			}
		});

		// -------------------------------------
		// Disable System.exit() call by changing SecurityManager

		oldSecurityManager = System.getSecurityManager();
		final SecurityManager securityManager = new SecurityManager() {
			// public void checkExit(int status) {
			// //throw new ApplicationTerminatedException(status);
			// }

			@Override
			public void checkPermission(Permission permission, Object context) {
				if ("exitVM".equals(permission.getName())) {
					throw new ExitTrappedException();
				}
			}

			@Override
			public void checkPermission(Permission permission) {
				if ("exitVM".equals(permission.getName())) {
					throw new ExitTrappedException();
				}
			}
		};
		System.setSecurityManager(securityManager);

		// Registering default supported events
		EventManager em = EventManager.getInstance();

		for (Class<? extends GEvent> event : SWTConstants.DEFAULT_SUPPORTED_EVENTS) {
			em.registerEvent(event);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cs.guitar.replayer.AbsReplayerMonitor#cleanUp()
	 */
	@Override
	public void cleanUp() {
		System.setSecurityManager(oldSecurityManager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.umd.cs.guitar.replayer.AbsReplayerMonitor#getAction(java.lang.String)
	 */
	@Override
	public GEvent getAction(String actionName) {
		GEvent retAction = null;
		try {
			Class<?> c = Class.forName(actionName);
			Object action = c.newInstance();

			retAction = (GEvent) action;

		} catch (Exception e) {
			GUITARLog.log.error("Error in getting action", e);
		}

		return retAction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.umd.cs.guitar.replayer.AbsReplayerMonitor#getArguments(java.lang.
	 * String)
	 */
	@Override
	public Object getArguments(String action) {
		// EventData event = new EventData(action);
		// return event.getParameters();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.umd.cs.guitar.replayer.AbsReplayerMonitor#getWindow(java.lang.String)
	 */
	@Override
	public GWindow getWindow(String sWindowTitle) {

		GWindow retGXWindow = null;
		while (retGXWindow == null) {

			final Shell[][] shells = new Shell[1][];

			application.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					shells[0] = application.getDisplay().getShells();
				}
			});

			if (shells == null) {
				continue;
			}

			for (Shell s : shells[0]) {
				Shell shell = getOwnedWindowByID(s, sWindowTitle);
				if (shell != null) {
					retGXWindow = new SWTWindow(shell);
					break;
				}
			}
			// try {
			// Thread.sleep(DELAY_STEP);
			// } catch (InterruptedException e) {
			// GUITARLog.log.error(e);
			// }

			// new EventTool().waitNoEvent(DELAY_STEP);
		}
		return retGXWindow;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.umd.cs.guitar.replayer.GReplayerMonitor#selectIDProperties(edu.umd
	 * .cs.guitar.model.data.ComponentType)
	 */
	@Override
	public List<PropertyType> selectIDProperties(ComponentType comp) {
		if (comp == null)
			return new ArrayList<PropertyType>();

		List<PropertyType> retIDProperties = new ArrayList<PropertyType>();

		AttributesType attributes = comp.getAttributes();
		List<PropertyType> lProperties = attributes.getProperty();
		for (PropertyType p : lProperties) {
			if (SWTConstants.ID_PROPERTIES.contains(p.getName()))
				retIDProperties.add(p);
		}
		return retIDProperties;
	}

	/**
	 * 
	 * Recursively search a window
	 * 
	 * @param parent
	 * @param sWindowID
	 * @return Window
	 */
	private Shell getOwnedWindowByID(Shell parent, String sWindowID) {

		if (parent == null) {
			return null;
		}

		GWindow gWindow = new SWTWindow(parent);

		String title = gWindow.getTitle();
		if (title == null) {
			return null;
		}

		// if (sWindowID.equals(title )) {

		if (isUseReg) {
			if (isRegMatched(title, sWindowID)) {
				return parent;
			}
		} else {
			if (sWindowID.equals(title)) {
				return parent;
			}
		}

		Shell retShell = null; // TODO convert this code to SWT
		// Window[] wOwnedWins = parent.getOwnedWindows();
		// for (Window aOwnedWin : wOwnedWins) {
		// retWin = getOwnedWindowByID(aOwnedWin, sWindowID);
		// if (retWin != null)
		// return retWin;
		// }

		return retShell;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cs.guitar.replayer.AbsReplayerMonitor#connectToApplication()
	 */
	@Override
	public void connectToApplication() {
		GUITARLog.log.info("Loading URL....");

		// TODO refactor with SWTRipperMonitor
		String[] URLs;
		if (config.URL_LIST != null)
			URLs = config.URL_LIST
					.split(GUITARConstants.CMD_ARGUMENT_SEPARATOR);
		else
			URLs = new String[0];

		// application = new SWTApplication(config.MAIN_CLASS, null);

		String[] args;

		if (config.ARGUMENT_LIST != null)
			args = config.ARGUMENT_LIST
					.split(GUITARConstants.CMD_ARGUMENT_SEPARATOR);
		else
			args = new String[0];

		GUITARLog.log.info("Loading URL.... DONE");

		application.connect(args);

		GUITARLog.log.info("Initial waiting for " + config.INITIAL_WAITING_TIME
				+ "ms");

		try {
			Thread.sleep(config.INITIAL_WAITING_TIME);
		} catch (InterruptedException e) {
			GUITARLog.log.error(e);
			throw new ApplicationConnectException();
		}

	}

	/**
	 * Check if a string is match by a regular expression temporarily used for
	 * matching window titles. Should move to some more general modules for
	 * future use.
	 * 
	 * <p>
	 * 
	 * @param input
	 * @param regExp
	 * @return
	 */
	private boolean isRegMatched(String input, String regExp) {

		Pattern pattern;
		Matcher matcher;
		pattern = Pattern.compile(regExp);
		matcher = pattern.matcher(input);
		if (matcher.matches())
			return true;

		return false;
	}

}