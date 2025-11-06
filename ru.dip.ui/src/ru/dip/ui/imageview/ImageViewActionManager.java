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
package ru.dip.ui.imageview;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IViewSite;

import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.ui.Messages;
import ru.dip.ui.imageview.interfaces.IImageView;
import ru.dip.ui.imageview.interfaces.IImageViewPreferences;
import ru.dip.ui.utilities.GlobalKeyListener;
import ru.dip.ui.utilities.dip.DipUnitManager;
import ru.dip.ui.utilities.image.ImageProvider;

public class ImageViewActionManager {

	private final IImageView fImageView;	
	// actions
	private OpenInEditor fOpenInEditor = new OpenInEditor();
	private OpenInDocument fOpenInDocument = new  OpenInDocument();
	private CopyID fCopyID = new CopyID();
	private CopyFullID fCopyFullID = new CopyFullID();
	
	
	public ImageViewActionManager(IImageView imageView) {
		this.fImageView = imageView;
	}

	public void init(IViewSite site) {
		IToolBarManager toolbar = site.getActionBars().getToolBarManager();
		toolbar.add(new HorizontalModeAction());
		toolbar.add(new ViewTypeAction());
		toolbar.add(new InFolderAction());
	}
	
	public void addMenu() {
		MenuManager popupMenuManager = new MenuManager();	
		popupMenuManager.setRemoveAllWhenShown(true);
		IMenuListener listener = new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(fOpenInEditor);
				manager.add(fOpenInDocument);
				manager.add(new Separator());
				manager.add(fCopyID);
				manager.add(fCopyFullID);
			}
		};
		popupMenuManager.addMenuListener(listener);
		Menu menu = popupMenuManager.createContextMenu(control());
		control().setMenu(menu);
	}
	
	//=========================
	// toolbar actions
	
	class HorizontalModeAction extends Action {
		
		public HorizontalModeAction() {
			setText(Messages.ImagesView_HorizontalOrentationActionName);
			setImageDescriptor(ImageProvider.HORIZONTAl_DESCRIPTOR);
			setChecked(preferences().isHorizontalMode());
		}
		
		public void run() {
			preferences().setHorizontalMode(isChecked());
			fImageView.createComposite();
		}	
	}
	
	class ViewTypeAction extends Action {
		
		public ViewTypeAction() {
			setText(Messages.ImagesView_AlligmentActionName);
			setImageDescriptor(ImageProvider.ALLIGNMENT_DESCRIPTOR);
		}
		
		@Override
		public void run() {
			preferences().nextAllignment();
			fImageView.createComposite();
		}
	}
	
	class InFolderAction extends Action {
		
		public InFolderAction() {
			setText(Messages.ImagesView_InFolderActionName);
			setChecked(preferences().isFolderMode());
			setImageDescriptor(ImageProvider.FOLDER_DESCRIPTOR);
		}
		
		@Override
		public void run() {
			preferences().setFolderMode(isChecked());
			fImageView.createComposite();
		}	
	}
	
	//===================
	// context menu actions
	
	class OpenInEditor extends Action {
		
		public OpenInEditor() {
			setText(Messages.ImagesView_OpenInEditorActionName);
		}
		
		@Override
		public void run() {
			dipUnitManager().openInEditor();
		}
	}
	
	class OpenInDocument extends Action {
		
		public OpenInDocument() {
			setText(Messages.ImagesView_OpenInDocumentActionName);
		}
		
		@Override
		public void run() {
			dipUnitManager().openInDocument();
		}
	}
	
	class CopyID extends Action {
		
		public CopyID() {
			setText(Messages.ImagesView_CopyIDActionName);
			setAccelerator(SWT.CTRL + 'C');			
		}
		
		@Override
		public void run() {
			dipUnitManager().copyID();
		}
	}
	
	class CopyFullID extends Action {
		
		public CopyFullID() {
			setText(Messages.ImagesView_CopyFullIDActionName);
			setAccelerator(SWT.CTRL + SWT.SHIFT + 'C');
		}
		
		@Override
		public void run() {
			dipUnitManager().fullCopyID();
		}
	}
	
	//=======================
	// hot keys
	
	public void addHotKeyListener() {
		
		control().addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (((e.stateMask & SWT.SHIFT) == SWT.SHIFT))						
						&& (e.keyCode == 'c' || e.keyCode == 'с')) {
					dipUnitManager().fullCopyID();
				} else if (((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 'c' || e.keyCode == 'с')) {
					dipUnitManager().copyID();
				}			
			}
		});
	}
	
	//=============
	// DnD
	
	public void addDragListener() {
		final Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		final int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		
		final DragSource source = new DragSource(control(), operations);
		source.setTransfer(types);
		source.addDragListener(new DragSourceListener() {
			
			private IDipUnit unit;
			
			@Override
			public void dragStart(DragSourceEvent event) {
				unit = fImageView.getImageSelector().selectionUnit();			
				event.doit = (unit != null);			
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				if (unit == null) {
					return;
				}				
				String id =  DipUtilities.relativeProjectID(unit);
				if (GlobalKeyListener.isCtrl()) {
					event.data = id;
				} else {				
					event.data = "[](" + id + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			@Override
			public void dragFinished(DragSourceEvent event) {}
		});
	}
	
	
	//===================
	// getters
	
	private IImageViewPreferences preferences() {
		return fImageView.getPreferences();
	}
	
	private Table control() {
		return fImageView.getTable();
	}
	
	private DipUnitManager dipUnitManager() {
		return fImageView.getDipUnitManager();
	}
	
}
