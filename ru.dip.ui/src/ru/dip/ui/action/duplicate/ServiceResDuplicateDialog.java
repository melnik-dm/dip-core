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
package ru.dip.ui.action.duplicate;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.model.DipContainer;
import ru.dip.core.model.DipProject;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.ui.Messages;

public class ServiceResDuplicateDialog extends DuplicateDialog {

	public ServiceResDuplicateDialog(Shell parentShell, DipContainer container, IResource second) {
		super(parentShell, container, null, second);
	} 
	
	protected void createMessageLabel(Composite composite) {
		StringBuilder builder = new StringBuilder();
		String name = fSecond.getName();

		if (fContainer instanceof DipProject) {
			builder.append(Messages.ServiceResDuplicateDialog_ProjectLabel);
		} else {
			builder.append(Messages.ServiceResDuplicateDialog_DirectoryLabel);
		}
		
		int offset = builder.length();		
		String id =  DipUtilities.fullIDWithoutRevision(fContainer);
		int length = id.length();
		ArrayList<Point> points = new ArrayList<>();
		points.add(new Point(offset, length));
		
		builder.append(id);
		builder.append(Messages.ServiceResDuplicateDialog_Contains_objects);		
		points.add(new Point(builder.length(), name.length()));
		builder.append(name);
		builder.append(Messages.ServiceResDuplicateDialog_Name_reserved);
		builder.append(Messages.ServiceResDuplicateDialog_Need_rename_delete);
		points.add(new Point(builder.length(), name.length()));
		builder.append(name);
		builder.append("."); //$NON-NLS-1$
		
		StyledText messageLabel = new StyledText(composite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);		
		messageLabel.setText(builder.toString());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 640;
		messageLabel.setLayoutData(gd);
		for (Point point: points) {
			StyleRange range = new StyleRange(point.x, point.y, null, null, SWT.BOLD);
			messageLabel.setStyleRange(range);
		}
	}
	
}
