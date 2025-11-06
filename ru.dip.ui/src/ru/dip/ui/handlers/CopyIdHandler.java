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
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

import ru.dip.ui.Messages;
import ru.dip.ui.table.editor.DipTableEditor;
import ru.dip.ui.table.ktable.actions.manager.CopyIdIneractor.CopyIdType;

public class CopyIdHandler extends AbstractHandler {
	
	private static final String COMMAND_ID = "ru.dip.ui.command.copy.id"; //$NON-NLS-1$
	
	private static final String COPY_RELATIVE_COMMAND = Messages.CopyIdHandler_RelativeCopyActionName;
	private static final String COPY_ID_COMMAND = Messages.CopyIdHandler_CopyIdActionName;
	private static final String COPY_VERSION_ID_COMMAND = Messages.CopyIdHandler_CopyIdWithRevisionActionName;
		
	private static IContributionItem createMenuItem(String command) {
		IServiceLocator locator = PlatformUI.getWorkbench();
		CommandContributionItemParameter copyParameter = new CommandContributionItemParameter(
				locator, command, COMMAND_ID, 0);
		copyParameter.label = command;
		return new CommandContributionItem(copyParameter);
	}
	
	public static IContributionItem getRelationPathItem() {	
		return createMenuItem(COPY_RELATIVE_COMMAND);
	}
		
	public static IContributionItem getFuldIdItem() {
		return createMenuItem(COPY_ID_COMMAND);
	}
	
	public static IContributionItem getRevisionIdItem() {
		return createMenuItem(COPY_VERSION_ID_COMMAND);
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {				
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (part instanceof DipTableEditor) {
			DipTableEditor tableEditor = (DipTableEditor) part;
			CopyIdType type = typeFromEvent(event);
			tableEditor.kTable().copyIdInteractor().doCopyID(type);
		}
		return null;
	}
	
	private CopyIdType typeFromEvent(ExecutionEvent execEvent) {
		if (execEvent.getTrigger() instanceof Event) {
			Event event = (Event) execEvent.getTrigger();
			if (event.widget instanceof MenuItem) {
				return typeFromCommandLabel(((MenuItem) event.widget).getText());
			}			
		}
		return CopyIdType.RELATIVE;
	}
	
	private CopyIdType typeFromCommandLabel(String commandLabel) {
		if (commandLabel.startsWith(COPY_RELATIVE_COMMAND)) {
			return CopyIdType.RELATIVE;
		} else if (commandLabel.startsWith(COPY_VERSION_ID_COMMAND)) {
			return CopyIdType.REVISION;
		} else {
			return CopyIdType.FULL;
		}	
	}

}
