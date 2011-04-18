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
 * Replayer monitor for Java SWT application
 * 
 */
public class SWTReplayerMonitor extends GReplayerMonitor {
	
	private static final int INITIAL_DELAY = 1000;
	
	
	/**
	 * Delay for widget searching loop
	 */
	private static final int DELAY_STEP = 50;

	private SWTReplayerConfiguration config;
	private final SWTApplication application;

	/**
	 * Instantiation. We set our configuration file for our replayer as our parameter
	 * and our application as that from the parameter as well.
	 * 
	 * @param config
	 * @param application
	 */
	public SWTReplayerMonitor(SWTReplayerConfiguration config,
			SWTApplication application) {
		this.config = config;
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

	/**
	 * Setting up the Monitor. Setting up security manager and event manager.
	 */
	@Override
	public void setUp() {
		GUITARLog.log.info("Setting up SWTReplayer...");
		
		// Add handler for all uncaught exceptions
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				GUITARLog.log.error("Uncaught exception", e);
			}
		});

		
		// Disable any calls to System.exit() (which would terminate the JVM) by the GUI
		oldSecurityManager = System.getSecurityManager();
		final SecurityManager securityManager = new SecurityManager() {
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

	/**
	 * disposes the display and sets the security manager to the one created 
	 * at creation
	 */
	@Override
	public void cleanUp() {
		System.setSecurityManager(oldSecurityManager);
		application.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				application.getDisplay().dispose();
			}
		});
		GUITARLog.log.info("Display disposed");
	}

	/**
	 * returns a GEvent for the action the user provides
	 * 
	 * @param actionName
	 * 		action that user wants to track
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

	/**
	 * Does nothing at the moment
	 */
	@Override
	public Object getArguments(String action) {
		// not being used by JFC
		return null;
	}

	/**
	 * Returns the window corresponding to the provided title.
	 * 
	 * @param sWindowTitle
	 * 			title of the window to return
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

			for (Shell s : shells[0]) {
				Shell shell = getOwnedWindowByID(s, sWindowTitle);
				if (shell != null) {
					retGXWindow = new SWTWindow(shell);
					break;
				}
			}
			
		}
		return retGXWindow;
	}

	/**
	 * Returns the id properties for the component type in a list object of property types.
	 * 
	 * @param comp
	 * 		component type 
	 * @return List 
	 */
	@Override
	public List<PropertyType> selectIDProperties(ComponentType comp) {
		if (comp == null) {
			return new ArrayList<PropertyType>();
		}

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
	private Shell getOwnedWindowByID(final Shell parent, String sWindowID) {

		if (parent == null) {
			return null;
		}

		GWindow gWindow = new SWTWindow(parent);

		String title = gWindow.getTitle();
		if (title == null) {
			return null;
		}

		if (isUseReg) {
			if (isRegMatched(title, sWindowID)) {
				return parent;
			}
		} else {
			if (sWindowID.equals(title)) {
				return parent;
			}
		}

		final Shell[][] childShells = new Shell[1][];
		application.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				childShells[0] = parent.getShells();
			}
		});
		
		Shell retShell = null;
		for (Shell s : childShells[0]) {
			// keep searching children
			retShell = getOwnedWindowByID(s, sWindowID);
			if (retShell != null) {
				return retShell;	
			}
		}
		
		return retShell;
	}

	/**
	 * connection to application through parameter url
	 * 
	 */
	@Override
	public void connectToApplication() {
		GUITARLog.log.info("Loading URL....");

		// TODO refactor with SWTRipperMonitor
		String[] URLs;
		if (config.getUrlList() != null)
			URLs = config.getUrlList()
					.split(GUITARConstants.CMD_ARGUMENT_SEPARATOR);
		else
			URLs = new String[0];

		// application = new SWTApplication(config.MAIN_CLASS, null);

		String[] args;

		if (config.getArgumentList() != null)
			args = config.getArgumentList()
					.split(GUITARConstants.CMD_ARGUMENT_SEPARATOR);
		else
			args = new String[0];

		GUITARLog.log.info("Loading URL.... DONE");

		application.connect(args);

		GUITARLog.log.info("Initial waiting for " + config.getInitialWaitTime()
				+ "ms");

		try {
			Thread.sleep(config.getInitialWaitTime());
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
	
	/**
	 * returns the application
	 */
	public SWTApplication getApplication() {
		return application;
	}

}