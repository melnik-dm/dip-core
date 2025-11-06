/******************************************************************************* * 
 * Copyright (c) 2025 Denis Melnik.
 * Copyright (c) 2025 Ruslan Sabirov.
 * Copyright (c) 2025 Andrei Motorin.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package ru.dip.ui.utilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.navigator.resources.ProjectExplorer;

import ru.dip.core.utilities.WorkbenchUtitlities;

public class GlobalKeyListener {

	private static GlobalKeyListener instance;

	public static Object navigatorSelection;

	public static GlobalKeyListener instance() {
		if (instance == null) {
			instance = new GlobalKeyListener();
		}
		return instance;
	}

	private KeyUpListener fKeyUpListener;
	private KeyDownListener fKeyDownListener;
	private MouseDoubleClick fMouseDoublieClickListener;
	private boolean fCtrlPressed = false; // зажат Ctrl
	private boolean fCtrlShiftPressed = false; // зажат Ctrl + Shift

	private GlobalKeyListener() {

	}

	public void addListeners() {
		Display.getDefault().addFilter(SWT.KeyUp, fKeyUpListener = new KeyUpListener());
		Display.getDefault().addFilter(SWT.KeyDown, fKeyDownListener = new KeyDownListener());
		Display.getDefault().addFilter(SWT.MouseDoubleClick, fMouseDoublieClickListener = new MouseDoubleClick());

	}

	private class MouseDoubleClick implements Listener {

		@Override
		public void handleEvent(Event event) {
			ProjectExplorer explorer = WorkbenchUtitlities.getProjectExplorer();
			if (explorer != null) {
				TreeItem item = explorer.getCommonViewer().getTree().getItem(new Point(event.x, event.y));
				navigatorSelection = item != null ? item.getData() : null;
			} else {
				navigatorSelection = null;
			}
		}
	}

	private class KeyUpListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			fCtrlShiftPressed = false;
			fCtrlPressed = false;
		}
	}

	private class KeyDownListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			if (((event.stateMask & SWT.CTRL) == SWT.CTRL) && (event.keyCode == SWT.SHIFT)) {
				fCtrlShiftPressed = true;
			} else if (event.keyCode == SWT.CTRL) {
				fCtrlPressed = true;
			}
		}
	}

	public static boolean isCtrl() {
		return instance().fCtrlPressed;
	}

	public static boolean isShiftCtrl() {
		return instance().fCtrlShiftPressed;
	}

}
