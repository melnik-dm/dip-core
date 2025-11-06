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
package ru.dip.editors.incorrectview;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.ViewPart;

import ru.dip.core.link.CorrectLink;
import ru.dip.core.link.IncorrectLinkChangeListener;
import ru.dip.core.link.Link;
import ru.dip.core.link.LinkInteractor;
import ru.dip.core.model.DipProject;
import ru.dip.core.unit.ReportRefPresentation;
import ru.dip.core.utilities.ReportUtils;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.core.utilities.ui.image.DownRightOverlayImageIcon;
import ru.dip.editors.Messages;
import ru.dip.editors.formeditor.FormsEditor;
import ru.dip.editors.md.ISaveNotifier;
import ru.dip.editors.md.MDEditor;
import ru.dip.ui.utilities.image.ImageProvider;

public class IncorrectLinksView extends ViewPart implements IncorrectLinkChangeListener, IPropertyListener {

	private TreeViewer fViewer;
	private DipProject fCurrentProject;	
	private boolean fHideCorrectLinks = false;
	private Composite fMainComposite;
	private Set<IEditorPart> fEditorWithListeners = new HashSet<>(); // связанные редакторы слушатели (на save)
	
	public IncorrectLinksView() {
		LinkInteractor.instance().setIncorrectChangeListener(this);
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
		toolbar.add(new HideCorrectReferencesAction());		
	}
	
	class HideCorrectReferencesAction extends Action {
		
		public HideCorrectReferencesAction() {
			setText(Messages.IncorrectLinksView_HideCorrecReferencesAction);
			setImageDescriptor(ImageProvider.HIDE_LINKS_DESCRIPTOR);
			setChecked(fHideCorrectLinks);
		}
		
		@Override
		public void run() {		
			fHideCorrectLinks = !fHideCorrectLinks;
			fViewer.refresh();
		}	
	}

	public boolean isVisible() {
		return fMainComposite != null && fMainComposite.isVisible();
	}
	
	@Override
	public void createPartControl(Composite parent) {
		fMainComposite = CompositeBuilder.instance(parent).full().build();
		TreeColumnLayout fTreeColumnlayout = new TreeColumnLayout(true);
		fMainComposite.setLayout(fTreeColumnlayout);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		fMainComposite.setLayoutData(gridData);

		Tree tree= new Tree(fMainComposite, SWT.BORDER | SWT.NO_SCROLL | SWT.V_SCROLL);
		fViewer = new TreeViewer(tree);		
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		GridData treeLayoutData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		treeLayoutData.grabExcessHorizontalSpace = true;
		tree.setLayoutData(treeLayoutData);
		
        TreeViewerColumn viewerColumn = new TreeViewerColumn(fViewer, SWT.CENTER);		
		TreeColumn fIDColumn = viewerColumn.getColumn();
		fIDColumn.setText(Messages.IncorrectLinksView_IdColumn);
		fIDColumn.setAlignment(SWT.LEFT);	
		fIDColumn.setResizable(true);
		fTreeColumnlayout.setColumnData(fIDColumn, new ColumnWeightData(40, 0, true));
		
        TreeViewerColumn viewerColumn2 = new TreeViewerColumn(fViewer, SWT.CENTER);		
		TreeColumn fProjectColumn = viewerColumn2.getColumn();
		fProjectColumn.setText(Messages.IncorrectLinksView_TextColumn);
		fProjectColumn.setAlignment(SWT.LEFT);	
		fProjectColumn.setResizable(true);
		fTreeColumnlayout.setColumnData(fProjectColumn, new ColumnWeightData(60, 0, true));
		
        TreeViewerColumn viewerColumn3 = new TreeViewerColumn(fViewer, SWT.CENTER);		
		TreeColumn fLinkColumn = viewerColumn3.getColumn();
		fLinkColumn.setText(Messages.IncorrectLinksView_LinkColumn);
		fLinkColumn.setAlignment(SWT.LEFT);	
		fLinkColumn.setResizable(true);
		fTreeColumnlayout.setColumnData(fLinkColumn, new ColumnWeightData(60, 0, true));
		
		fViewer.setContentProvider(new ITreeContentProvider() {
			
			@Override
			public boolean hasChildren(Object element) {
				return false;
			}
			
			@Override
			public Object getParent(Object element) {
				return null;
			}
			
			@Override
			public Object[] getElements(Object inputElement) {				
				if (fCurrentProject == null) {
					return new Object[0];
				}				

				List<Object> result = LinkInteractor.instance().allLinks(fCurrentProject);
				result.addAll(ReportUtils.getAllReportRefs(fCurrentProject));
				return result.toArray();
			}
			
			@Override
			public Object[] getChildren(Object parentElement) {
				return null;
			}
		});
		
		fViewer.setLabelProvider(new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell cell) {
				int index = cell.getColumnIndex();
				Object object = cell.getElement();
				if (object instanceof Link) {
					Link incorrectLink = (Link) object;
					if (index == 0) {
						cell.setText(incorrectLink.getSource().id());
						if (incorrectLink instanceof CorrectLink) {
							cell.setImage(ImageProvider.LINK_FOLDER);
						} else {
							cell.setImage(ImageProvider.ERROR);
						}
					} else if (index == 1) {
						cell.setText(incorrectLink.getText());
					} else if (index == 2) {
						cell.setText(incorrectLink.getLink());
					}
				} else if (object instanceof ReportRefPresentation) {
					ReportRefPresentation presentation = (ReportRefPresentation) object;
					presentation.checkUpdate();
					IFile reportFile = presentation.getReportFile();			
					if (index == 0) {
						cell.setText(presentation.getUnit().name());
						if (reportFile != null && reportFile.exists()) {
							cell.setImage(ImageProvider.FILE);
						} else {
							Image image = new DownRightOverlayImageIcon(ImageProvider.FILE, ImageProvider.ERROR_OVR).getImage();
							cell.setImage(image);
						}											
					} else if (index == 1) {
						String path = presentation.getRepoRefPath();
						if (path == null) {
							path =  presentation.getUnit().resource().getName();
						}				
						cell.setText(path);
					} else if (index == 2) {
						if (reportFile != null) {
							cell.setText(reportFile.getProjectRelativePath().toOSString());
						}
					}
				}
			}
		});
		fViewer.setInput(""); //$NON-NLS-1$
		
		fViewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object object = fViewer.getStructuredSelection().getFirstElement();
				if (object instanceof Link) {
					Link link = (Link) object;
					openFile(link.getSource().resource(), link.getText());
				} else if (object instanceof ReportRefPresentation){
					WorkbenchUtitlities.openFile(((ReportRefPresentation)object).getUnit().resource());
				}
			}
		});
		
		fViewer.addFilter(new ViewerFilter() {
			
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (fHideCorrectLinks && isCorrectLink(element)) {
					return false;
				}
				return true;
			}
		});
		
		checkLinks();
		addListenerToEditor();
	}
	
	void checkLinks() {
		Display.getCurrent().asyncExec(() -> {
			DipProject project = WorkbenchUtitlities.getDipProjectFromOpenedEditor();
			fCurrentProject = project;
			LinkInteractor.instance().clearAllLinks();
			LinkInteractor.instance().checkLinks(project);
			fViewer.refresh();
		});
	}
	
	private void openFile(IFile file, String text) {
		IEditorPart editorPart = WorkbenchUtitlities.openFile(file);
		if (editorPart instanceof TextEditor) {
			WorkbenchUtitlities.selectText((TextEditor) editorPart, text);
		} else if (editorPart instanceof FormsEditor) {
			FormsEditor formEditor = (FormsEditor) editorPart;
			formEditor.selectText(text);
		}
	}
	
	private boolean isCorrectLink(Object element) {
		if (element instanceof CorrectLink) {
			return true;
		}
		if (element instanceof ReportRefPresentation) {
			return((ReportRefPresentation) element).isCorrectRef();
		}
		return false;
	}
	

	@Override
	public void setFocus() {
		LinkInteractor.instance().checkIncorrectLinks();
		fViewer.refresh();
	}

	@Override
	public void linksChanged() {
		if (fViewer != null && !fViewer.getTree().isDisposed()){
			LinkInteractor.instance().checkIncorrectLinks();
			fViewer.refresh();
		}	
	}

	//============================
	// editor save-propery listener
	
	void addListenerToEditor() {
		IEditorPart editor = WorkbenchUtitlities.getActiveEditor();
		if (editor != null &&  editor instanceof ISaveNotifier) {
			addListenerToEditor(editor);
		}
	}
	
	void addListenerToEditor(IEditorPart editor) {
		if (!fEditorWithListeners.contains(editor)) {
			editor.addPropertyListener(this);
		}		
	}
	
	@Override
	public void propertyChanged(Object source, int propId) {
		if (propId == MDEditor.SAVE_EVENT) {
			checkLinks() ;
		}
	}
	
}
