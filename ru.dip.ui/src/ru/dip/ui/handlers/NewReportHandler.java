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
package ru.dip.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import ru.dip.core.model.reports.IReportContainer;
import ru.dip.core.utilities.ReportUtils;
import ru.dip.ui.dialog.NewReportDialog;

public class NewReportHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		Object firstElement = HandlerUtil.getCurrentStructuredSelection(event).getFirstElement();
        IReportContainer reportFolder = ReportUtils.getReportFolder(firstElement);
        if (reportFolder != null){
        	NewReportDialog reportDialog = new NewReportDialog(shell, reportFolder);
        	reportDialog.open(); 
        }
		return null;
	}
	
}
