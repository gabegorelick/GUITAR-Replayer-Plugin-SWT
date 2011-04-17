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

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Shell;

import edu.umd.cs.guitar.event.GEvent;
import edu.umd.cs.guitar.model.GWindow;
import edu.umd.cs.guitar.model.SWTApplication;
import edu.umd.cs.guitar.model.SWTConstants;
import edu.umd.cs.guitar.model.SWTWindow;
import edu.umd.cs.guitar.model.data.AttributesType;
import edu.umd.cs.guitar.model.data.ComponentType;
import edu.umd.cs.guitar.model.data.PropertyType;
import edu.umd.cs.guitar.ripper.SWTMonitor;
import edu.umd.cs.guitar.util.GUITARLog;

/**
 * Monitor for {@link SWTReplayer} to handle SWT specific features. Adapted from
 * <code>JFCReplayerMonitor</code>.
 * 
 * @author Gabe Gorelick
 */
public class SWTReplayerMonitor extends GReplayerMonitor {

	private final SWTApplication application;
	
	// monitor to delegate actions shared with ripper to
	private final SWTMonitor monitor;

	private SecurityManager oldSecurityManager;
	
	public SWTReplayerMonitor(SWTReplayerConfiguration config, SWTApplication app) {
		this.application = app;
		this.monitor = new SWTMonitor(config, app);
	}

	/**
	 * Class used to disable calls to System.exit().
	 * 
	 * @author Bao Nguyen
	 * 
	 */
	private static class ExitTrappedException extends SecurityException {
		private static final long serialVersionUID = 1L;
	}

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
		
		monitor.registerEvents();
	}

	@Override
	public void cleanUp() {
		System.setSecurityManager(oldSecurityManager);
		monitor.cleanUp();
	}

	@Override
	public GEvent getAction(String actionName) {
		GEvent retAction = null;
		try {
			Class<?> c = Class.forName(actionName);
			Object action = c.newInstance();
			retAction = (GEvent) action;
		} catch (ClassNotFoundException e) {
			GUITARLog.log.error("Error in getting action", e);
		} catch (InstantiationException e) {
			GUITARLog.log.error("Error in getting action", e);
		} catch (IllegalAccessException e) {
			GUITARLog.log.error("Error in getting action", e);
		}

		return retAction;
	}

	@Override
	public Object getArguments(String action) {
		// not being used by JFC
		return null;
	}

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
			if (Pattern.matches(sWindowID, title)) {
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
	
	@Override
	public void connectToApplication() {
		application.connect();
	}
	
	@Override
	public SWTApplication getApplication() {
		return application;
	}

}