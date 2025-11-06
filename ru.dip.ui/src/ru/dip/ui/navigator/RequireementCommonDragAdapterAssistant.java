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
package ru.dip.ui.navigator;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.navigator.CommonDragAdapterAssistant;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.part.ResourceTransfer;

public class RequireementCommonDragAdapterAssistant extends CommonDragAdapterAssistant {

	public static class  ReqTransfer extends Transfer {
		
		private static ReqTransfer transfer;		
		private static final String URI_LIST = "text/uri-list"; //$NON-NLS-1$
		private static final int URI_LIST_ID = registerType(URI_LIST);
		private static final String GNOME_LIST = "x-special/gnome-copied-files"; //$NON-NLS-1$
		private static final int GNOME_LIST_ID = registerType(GNOME_LIST);
			
		public static ReqTransfer getTransfer(){
			if (transfer == null){
				transfer = new ReqTransfer();
			}		
			return transfer;
		}
		
			
		@Override
		public TransferData[] getSupportedTypes() {
			return FileTransfer.getInstance().getSupportedTypes();
		}

		@Override
		protected int[] getTypeIds(){
			return new int[]{URI_LIST_ID, GNOME_LIST_ID};
		}

		
		@Override
		public boolean isSupportedType(TransferData transferData) {
			return false;
		}

		@Override
		protected String[] getTypeNames() {
			return new String[]{URI_LIST, GNOME_LIST};
		}

		@Override
		protected void javaToNative(Object object, TransferData transferData) {
			
		}

		@Override
		protected Object nativeToJava(TransferData transferData) {
			return null;
		}
		
	}
	
	private static final Transfer[] SUPPORTED_TRANSFERS = new Transfer[] {
			LocalSelectionTransfer.getTransfer(),
			ResourceTransfer.getInstance(),
			FileTransfer.getInstance(), 
			ReqTransfer.getTransfer()	
	};

	public RequireementCommonDragAdapterAssistant() {
	}

	@Override
	public void dragStart(DragSourceEvent anEvent, IStructuredSelection aSelection) {
		super.dragStart(anEvent, aSelection);		
	}
	
	@Override
	public INavigatorContentService getContentService() {
		return super.getContentService();
	}
	
	@Override
	public Transfer[] getSupportedTransferTypes() {
		return SUPPORTED_TRANSFERS;
	}

	@Override
	public boolean setDragData(DragSourceEvent anEvent, IStructuredSelection aSelection) {
		return false;
	}

}
