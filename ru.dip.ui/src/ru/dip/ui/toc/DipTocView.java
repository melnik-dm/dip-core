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
package ru.dip.ui.toc;

import java.util.stream.Stream;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.IServiceLocator;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.interfaces.IDisable;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.Messages;
import ru.dip.ui.action.hyperlink.ReqLink;
import ru.dip.ui.table.editor.DipTableEditor;
import ru.dip.ui.utilities.GlobalKeyListener;
import ru.dip.ui.utilities.image.ImageProvider;

public class DipTocView extends ViewPart {
	
	public static final String ID = Messages.DipTocView_ID;
	
	private DipProject fProject;
	private TreeViewer fViewer;
	private int fLevel;
	private Composite fComposite;
	private CopyIDAction fCopyIDAction = new CopyIDAction();
	private CopyFullIDAction fFullCopyIDAction = new CopyFullIDAction();
	private boolean fLinkWithTable = true;
	private boolean fAutoWrapText = true;
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		initActions(site);	
	}
	
	private void initActions(IViewSite site) {
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(new EnableWrapTextAction());
		toolBar.add(new LinkedWithEditorAction());
		toolBar.add(new NestedLevelAction(Messages.DipTocView_LevelButton));		
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), fCopyIDAction);	
	}
		
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		fComposite = new Composite(parent, SWT.BORDER);
		fComposite.setLayout(new GridLayout());
		fComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Tree tree= new Tree(fComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);				
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		fViewer = new TreeViewer(tree);		
		fViewer.setContentProvider(new ITreeContentProvider() {
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				if (newInput != null && newInput.equals(oldInput)) {
					return;
				}			
				ITreeContentProvider.super.inputChanged(viewer, oldInput, newInput);
			}
			
			@Override
			public boolean hasChildren(Object element) {
				if (element instanceof IDipParent) {
					return ((IDipParent) element)
							.getDipDocChildrenList()
							.stream()
							.anyMatch(IDipParent.class::isInstance);										
				}				
				return false;
			}
			
			@Override
			public Object getParent(Object element) {
				if (element instanceof IDipDocumentElement) {
					((IDipDocumentElement) element).parent();
				}				
				return null;
			}
			
			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof DipProject) {
					DipProject model = (DipProject) inputElement;
					return model.getDipChildren();
				}			
				return new Object[0];
			}
			
			@Override
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof IDipParent) {					
					return ((IDipParent) parentElement).getDipChildren();
				}					
				return null;
			}
		});
		fViewer.addFilter(new ViewerFilter() {
			
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {								
				if (element instanceof IDipParent) {
					if (fLevel < 1) {
						return true;
					}
					
					IDipParent parent = (IDipParent) element;
					int level = DipUtilities.getNestedLevel(parent.resource());
					return level <= fLevel;
					
				}
				return false;
			}
		});
		
		fViewer.addFilter(new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (fProject != null && fProject.getProjectProperties().isHideDisableObjsEnable()) {
					if (element instanceof IDisable) {
						IDisable disableElement = (IDisable) element;
						return !disableElement.isDisabledInDocument();
					}
				}
				return true;
			}
		});

		fViewer.setLabelProvider(new StyledCellLabelProvider() {
		
			@Override
			public void update(ViewerCell cell) {
				clearCell(cell);
				Object element = cell.getElement();
				cell.setText(getText(element));
				if (element instanceof IDisable 
						&& ((IDisable) element).isDisabledInDocument()) {
					cell.setForeground(ColorProvider.RED);
				}
				
			}
			
			private void clearCell(ViewerCell cell) {
				cell.setImage(ImageProvider.EMPTY_IMAGE);
				cell.setText(""); //$NON-NLS-1$
				cell.setFont(null);
				cell.setForeground(null);
				cell.setStyleRanges(null);
			}
			
			@Override
			protected void measure(Event event, Object element) {			
				if (!fAutoWrapText) {
					event.width = fComposite.getClientArea().width;
					return;
				}
				
				TreeItem item = (TreeItem) event.item;				
			    int level = getLevel(item); 
				event.width = fComposite.getClientArea().width - 32 - level*16;				
				
				final String itemText = item.getText(event.index);				
				if (event.width == 0) {
					return;
				}
				Point size = event.gc.textExtent(itemText);
				int lines = size.x / event.width + 1;
				event.height = size.y * lines;
			}
			
			private String getText(Object element) {
				
				if (element instanceof IDipParent) {
					IDipParent parent = ((IDipParent) element);
					boolean showNumbers =  parent.dipProject().getProjectProperties().isNumeration();
					return parent.getNumberDescrition(showNumbers);
				}				
				return ""; //$NON-NLS-1$
			}

			private int getLevel (TreeItem item) {
				int level = 0;
				TreeItem parent = item.getParentItem();
				while (parent != null) {
					level++;
					parent = parent.getParentItem();
				}
				return level;
			}
			
			@Override
			protected void paint(Event event, Object element) {
				TreeItem item = (TreeItem) event.item;
				final String itemText = item.getText(event.index);			
			    final TextLayout layout = new TextLayout(event.gc.getDevice());			
			    int level = getLevel(item); 
				if (fAutoWrapText) {
					layout.setWidth(fComposite.getClientArea().width - 32 - level*16);
				}
			    layout.setText(itemText);		    
				layout.draw(event.gc, event.x, event.y);
			}
		});
		
		fComposite.addControlListener(new ControlListener() {
			
			@Override
			public void controlResized(ControlEvent e) {
				fViewer.refresh();
				fViewer.expandAll(true);
			}
			
			@Override
			public void controlMoved(ControlEvent e) {
				
			}
		});
							
		fViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);	
		addMouseListener();
		addContextMenu();
		addKeyListener();
		addSelectionListener();
	}
	
	private void addSelectionListener() {
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (fLinkWithTable) {
					DipTableContainer parent = getOneElementSelection();
					if (parent != null) {
						ReqLink.openElementInTable(parent);
					}
				}				
			}
		});
	}
	
	private void addMouseListener() {
		fViewer.getTree().addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				Point point = new Point(e.x, e.y);
				TreeItem item = fViewer.getTree().getItem(point);
				if (item == null) {
					return;
				}
				
				Object data = item.getData();				
				fViewer.setSelection(new StructuredSelection(data));				
				DipTableContainer parent = getOneElementSelection();
				if (parent != null) {
					if (fLinkWithTable) {
						ReqLink.openTable(parent.getTable());
					} else if (GlobalKeyListener.isCtrl()) {
						ReqLink.openTable(parent.getTable());
					} else {
						ReqLink.openElementInTable(parent);
					}
				}
			}
		});
	}

	private void addContextMenu() {
		MenuManager popupMenuManager = new MenuManager();
		Tree tree = fViewer.getTree();
		popupMenuManager.setRemoveAllWhenShown(true);
		IMenuListener listener = new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager mng) {
				
				IServiceLocator locator = getViewSite();				
				CommandContributionItemParameter copyParameter = new CommandContributionItemParameter(locator, Messages.DipTocView_CopyIDLabel, Messages.DipTocView_CopyCommandID, 0);
				copyParameter.label = Messages.DipTocView_CopyIDLabel;
				CommandContributionItem item = new CommandContributionItem(copyParameter);
				mng.add(item);
				mng.add(fFullCopyIDAction);
			}
		};

		popupMenuManager.addMenuListener(listener);
		Menu menu = popupMenuManager.createContextMenu(tree);
		tree.setMenu(menu);	
	}
		
	private void addKeyListener() {
		
		fViewer.getTree().addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (((e.stateMask & SWT.CTRL) == SWT.CTRL)
						&& (((e.stateMask & SWT.SHIFT) == SWT.SHIFT))						
						&& (e.keyCode == 'c' || e.keyCode == 'с')) {
					fFullCopyIDAction.run();
				}
			}
		});		
	}

	private DipTableContainer getOneElementSelection() {
		IStructuredSelection selection = fViewer.getStructuredSelection();
		if (selection != null && selection.size() == 1) {
			Object obj = selection.getFirstElement();
			if (obj instanceof DipTableContainer) {
				return (DipTableContainer) obj;
			}
		}
		return null;
	}
	
	private Stream<DipTableContainer> selectionElements() {
		IStructuredSelection selection = fViewer.getStructuredSelection();
		if (selection != null) {
			return Stream.of(selection.toArray())
			.filter(DipTableContainer.class::isInstance)
			.map(DipTableContainer.class::cast);			
		}
		return Stream.empty();
	}
	
	@Override
	public void setFocus() {

	}

	public void update() {
		Thread thread = new Thread(() -> {
		Display.getDefault().asyncExec(() -> {
				DipProject project = WorkbenchUtitlities.getDipProjectFromOpenedEditor();
				if (fViewer != null && !fViewer.getTree().isDisposed()) {
					fViewer.setInput("");
					setViewerInput(project);
				}
			});
		});
		thread.start();
	}

	public void update(DipTableEditor tableEditor) {		
		Thread thread = new Thread(() -> {
			Display.getDefault().asyncExec(() -> {
				DipProject project = tableEditor.model().dipProject();
				if (fViewer != null && !fViewer.getTree().isDisposed()) {
					fViewer.setInput("");
					setViewerInput(project);
				}
			});
		});
		thread.start();
	}
	
	public void refreshViewer() {
		if (fViewer != null) {
			fViewer.refresh();
		}
	}
	
	private synchronized void setViewerInput(DipProject project) {
		fProject = project;
		if (fViewer != null && !fViewer.getTree().isDisposed()) {
			if (project != null) {
				if (!project.equals(fViewer.getInput())) {
					fViewer.setInput(project);
				}
			} else {
				fViewer.setInput(""); //$NON-NLS-1$
			}
		}
	}
		
	private class LinkedWithEditorAction extends Action {
		
		public LinkedWithEditorAction() {
			setText(Messages.DipTocView_LinkWithEditorActionName);
			setImageDescriptor(ImageProvider.LINK_WITH_EDITOR);
			setChecked(true);
		}
		
		@Override
		public void run() {
			boolean value = isChecked();
			fLinkWithTable = value;
		}
		
	}
	
	private class NestedLevelAction extends  ControlContribution {

		protected NestedLevelAction(String id) {
			super(id);
		}

	    private Combo fCombo;        			
	 		
		@Override
		protected Control createControl(Composite parent) {
	        fCombo = new Combo(parent, SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
	        fCombo.setItems(new String[]{"Все  ", " 1 "," 2 "," 3 "," 4 "," 5 ", " 6" });        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	        fCombo.setText("Все  "); //$NON-NLS-1$
	        fCombo.setBackground(ColorProvider.DEFAULT_COLOR);
	        fCombo.addModifyListener(new ModifyListener() {
	
				@Override
				public void modifyText(ModifyEvent e) {
					fLevel = fCombo.getSelectionIndex();
					fViewer.refresh();
					fViewer.expandAll(true);
				}
			});
	        fCombo.setEnabled(true);
	        return fCombo;
		}	
	}
	
	private class CopyIDAction extends Action {
		
		public CopyIDAction() {
			setId(ActionFactory.COPY.getId());
			setText(Messages.DipTocView_CopyIDActionName);
		}
		
		@Override
		public void run() {
			Stream<DipTableContainer> elements = selectionElements();
			DipUtilities.copyRelativeIdsClipboard(elements,  fComposite.getDisplay());		
		}
	}
	
	private class CopyFullIDAction extends Action {
		
		public CopyFullIDAction() {
			setText(Messages.DipTocView_CopyFullIDActionName);
			setAccelerator(SWT.CTRL + SWT.SHIFT + 'C');
		}
		
		@Override
		public void run() {
			Stream<DipTableContainer> elements = selectionElements();
			DipUtilities.copyFullIdsClipboard(elements,  fComposite.getDisplay());		
		}
	}
	
	private class EnableWrapTextAction extends Action {
		
		public EnableWrapTextAction() {
			setText(Messages.DipTocView_WrapTextActionName);
			setImageDescriptor(ImageProvider.AUTO_TEXT_WRAPPING);
			setChecked(true);
		}
		
		@Override
		public void run() {
			boolean value = isChecked();
			fAutoWrapText = value;
			fViewer.refresh();
		}
		
	}
	
}
