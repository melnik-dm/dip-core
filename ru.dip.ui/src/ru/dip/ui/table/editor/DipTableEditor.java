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
package ru.dip.ui.table.editor;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.EditorPart;

import ru.dip.core.model.DipElementType;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.DnfoTable;
import ru.dip.core.model.finder.IFinder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipEditor;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IFindSupport;
import ru.dip.core.table.TableWriter;
import ru.dip.core.unit.UnitPresentationCache;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.ui.Messages;
import ru.dip.ui.ReqUIPlugin;
import ru.dip.ui.markview.IMarksUpdateListener;
import ru.dip.ui.table.editor.toolbar.ButtonManager;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.table.TableModel;
import ru.dip.ui.toc.DipTocView;
import ru.dip.ui.utilities.image.ImageProvider;

public class DipTableEditor extends EditorPart implements IResourceChangeListener, IDipEditor, IFindSupport {

	public static final String EDITOR_ID = "ru.dip.editor.table"; //$NON-NLS-1$
	public static final String DIP_CONTEXT = "ru.dip.ui.context.dip";
	public static final int VISIBLE_EVENT = 93;
	public static final int SELECTION_EVENT = 94;
	public static final int UPDATE_VIEWER_EVENT = 95;

	// model
	private DipEditorUpdater fUpdater;
	private TableModel fTableModel;
	private IContainer fContainer;	
	private boolean fDirty = false;
	private boolean fModelChanged = false;
	private String fLastSearchedText; // последний искомый текст в диалоге Find
	// для определения режимов maximize/restore
	private int fOldPartSizeState = -1;
	private int fCurrentPartSizeState = -1;
	private boolean fStart = false;
	// listeners
	private IMarksUpdateListener fMarksUpdateListener;
	private EditorControlListener fParentControlListener;
	// control
	private Composite fParentComposite;
	private Composite fMainComposite;
	private Label fProjectLabel;
	private Label fFileLabel;
	private ButtonManager fButtonManger;
	private KTableComposite fKTableComposite;
	
	private MouseListener fDeselectMouseListener = new MouseListener(){

		@Override
		public void mouseDoubleClick(MouseEvent e) {}

		@Override
		public void mouseDown(MouseEvent e) {}

		@Override
		public void mouseUp(MouseEvent e) {
			if (kTable() != null) {
				kTable().deselect();
				fButtonManger.checkFilterHelp();
			}
		}

	};
	
	public DipTableEditor() {
		fUpdater = new DipEditorUpdater(this);
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);	
		setContext();
	}
	
	/**
	 * Устанавливает контекст, нужен для отображения горячих клавиш которые конфликтуют с другими для обычного контекста
	 */
	private void setContext(){
		IContextService contextService = (IContextService)PlatformUI.getWorkbench()
				.getService(IContextService.class);
		contextService.activateContext(DIP_CONTEXT);
	}
	
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		if (input instanceof TableEditorInput){
			TableEditorInput tableInput = (TableEditorInput) input;
			IContainer container = tableInput.getContainer();
			IDipElement element = DipUtilities.findElement(container);
			if (element instanceof DipTableContainer){
				DipTableContainer tableContainer = (DipTableContainer) element;
				fTableModel = new TableModel(tableContainer);
				fContainer = (IContainer) tableContainer.resource();
				DipEditorRegister.instance.registerEditor(this);
			}	
		}
	}
	
	/** 
	 * Обновляет редактор, возвращает станартный input.
	 * Например при выходе из diff-режима
	 */
	public void udpateModelFromInput() {
		IEditorInput input = getEditorInput();
		if (input instanceof TableEditorInput){
			TableEditorInput tableInput = (TableEditorInput) input;
			IContainer container = tableInput.getContainer();
			IDipElement element = DipUtilities.findElement(container);
			if (element instanceof DipTableContainer){
				DipTableContainer tableContainer = (DipTableContainer) element;
				kTable().setDiffMode(false);
				kTable().setOnlyDiffMode(false);
				kTable().setDinamicallyDiffMode(false);
				setNewModel(new TableModel(tableContainer));			
			}	
		}
	}
	
	public void setNewModel(TableModel model){
		fTableModel = model;
		fContainer = model.resource();
		fKTableComposite.setTableModel(model);
		fModelChanged = false;
	}
		
	@Override
	public String getPartName() {
		if (fTableModel == null){
			return super.getPartName();
		}
		if (fTableModel.type() == DipElementType.RPOJECT){
			return(fTableModel.name() + " <Document>"); //$NON-NLS-1$
		} else {
			return(fTableModel.name() + " <Section>"); //$NON-NLS-1$
		}
	}
	
	@Override
	public String getTitleToolTip() {
		String path = fTableModel.resource().getFullPath().toString();
		if (path.startsWith("/")) { //$NON-NLS-1$
			path = path.substring(1);
		}		
		return path;
	}
	
	//==========================
	// content
	
	@Override
	public void createPartControl(Composite parent) {
		fParentComposite = parent;
		parent.addControlListener(fParentControlListener = new EditorControlListener());
		fMainComposite = new Composite(parent, SWT.BORDER);
		fMainComposite.setLayout(new GridLayout());
		fMainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		fMainComposite.setBackground(ColorProvider.WHITE);
		fMainComposite.addMouseListener(fDeselectMouseListener);	
		createFileLabelComposite(fMainComposite);
		fButtonManger = new ButtonManager(fMainComposite, this);	
		fButtonManger.addMouseListener(fDeselectMouseListener);
		fKTableComposite = new KTableComposite(fMainComposite, this);
		fKTableComposite.initialize();
	}
	
	private class EditorControlListener implements ControlListener {

		@Override
		public void controlMoved(ControlEvent e) {}

		@Override
		public void controlResized(ControlEvent e) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IWorkbenchPartReference partReferences = page.getActivePartReference();
			if (partReferences == null) {
				return;
			}
			
			int n = page.getPartState(page.getActivePartReference());			
			fOldPartSizeState = fCurrentPartSizeState;
			fCurrentPartSizeState = n;			
			if (isMaximize() || isRestore()) {
				fKTableComposite.sizeInteractor().setCurrentColumnsWidth(isMaximize());
			}
		}
	}
	
	private int getPartState() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IWorkbenchPartReference partReferences = page.getActivePartReference();
		if (partReferences == null) {
			return -1;
		}
		
		return page.getPartState(page.getActivePartReference());	
	}
	
	public boolean isMaximize() {
		return fCurrentPartSizeState == 1 && fOldPartSizeState == 2;
	}
	
	public boolean isRestore() {
		return fCurrentPartSizeState == 2 && fOldPartSizeState == 1;
	}

	private void createFileLabelComposite(Composite parent){		
		Composite topComposite = new Composite(parent, SWT.NONE);
		topComposite.setLayout(new FormLayout());
		topComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		topComposite.addMouseListener(fDeselectMouseListener);
		
		Composite labelComposite = new Composite(topComposite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 0;
		labelComposite.setLayout(layout);		

		FormData fd = new FormData();
		fd.right = new FormAttachment(50, 40);
		labelComposite.setLayoutData(fd);

		labelComposite.setBackground(ColorProvider.WHITE);
		labelComposite.addMouseListener(fDeselectMouseListener);
		
		if (fContainer == null){
			return;
		}
		
		fProjectLabel = new Label(labelComposite, SWT.NONE);
		fProjectLabel.setText(fTableModel.dipProject().name());
		fProjectLabel.setFont(FontManager.boldFont);
		fProjectLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
		fProjectLabel.addMouseListener(fDeselectMouseListener);
			
		fFileLabel = new Label(labelComposite, SWT.NONE);
		fFileLabel.setText(getIDLabelText(fContainer));
		fFileLabel.setFont(FontManager.boldFont);
		fFileLabel.setForeground(ColorProvider.BLACK);
		fFileLabel.addMouseListener(fDeselectMouseListener);
			
		ToolBar toolbar = new ToolBar(topComposite, SWT.NONE);
		FormData formData = new FormData();		
		formData.right = new FormAttachment(100, -4);
		toolbar.setLayoutData(formData);
		ToolItem item = new ToolItem(toolbar, SWT.PUSH);	
		item.setImage(ImageProvider.HELP);
		item.setToolTipText(Messages.DipTableEditor_HelpToolTip);
		item.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (Desktop.isDesktopSupported()) {
						Desktop desktop = Desktop.getDesktop();
						Path targetFolder = ResourcesUtilities.metadataPluginPath(ReqUIPlugin.getDefault(), null); 					
						Path target = targetFolder.resolve("tool-dip-guide.pdf");
						Path source = ResourcesUtilities.getPathFromPlugin(ReqUIPlugin.getDefault(), "content/tool-dip-guide.pdf");
						if (!Files.exists(target)) {
							Files.copy(source, target);
						} else if (!FileUtilities.equalsFilesByCheckSum(source, target)) {
							Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
						}
					    desktop.open(target.toFile());
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
	}

	private String getIDLabelText(IContainer container){
		String projectRelativePath = container.getProjectRelativePath().toOSString();	
		if (!projectRelativePath.isEmpty()) {
			return Messages.DipTableEditor_6 + projectRelativePath;
		}
		return projectRelativePath;
	}
	
	//=========================
	// update
	
	/*
	 * Полное обновление таблицы, сброс фильтров и т.п. (вызывается по кнопке update)
	 */
	
	// флаг, показывает, что идет полное обновление (чтобы не вызывался второй раз)
	private volatile boolean fFullUpdatingNow = false;
	
	public void setFullUpdate(boolean value) {
		fFullUpdatingNow = value;
	}
	
	public void fullUpdate() {
		fButtonManger.updateFilter();
		fButtonManger.setDiffModeSelection(false);
		kTable().resourceUpdate();
		udpateModelFromInput();
		updateDipToc();
	}
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getDelta() == null) {
			return;
		}
		IFile file = getModifitedFile(event.getDelta());
		if (file != null && !file.getName().equals(DnfoTable.TABLE_FILE_NAME)) {
			updater().updateFilePresentation(file);
			return;
		}
		
		if (fContainer != null && event.getDelta() != null){
			if (event.getDelta().findMember(fContainer.getFullPath()) != null){
				setModelChanged();
			}
		}
	}
	

	
	private IFile getModifitedFile(IResourceDelta delta) {
		if (delta.getKind() == IResourceDelta.CHANGED) {
			if (delta.getResource() instanceof IFile) {
				return (IFile) delta.getResource();
			}
			if (delta.getAffectedChildren().length == 1) {
				return getModifitedFile(delta.getAffectedChildren()[0]);
			}					
		} 
		return null;		
	}
	
	@Override
	public void setModelChanged() {
		fModelChanged = true;		
	}
	
	@Override
	public void setFocus() {
		if (!fStart) {
			fCurrentPartSizeState = getPartState();
			fStart = true;
		}
		fKTableComposite.setFocus();
		checkRenameInput();
		fKTableComposite.setViewModeProperties();
		if (fKTableComposite.isDinamicallyDiffMode()) {
			updater().additionalModelUpdate(fTableModel);
		}
		
		if (fTableModel != null && fModelChanged){
			updateEditor();
		} else {
			fKTableComposite.refreshTable();
		}
	}
	
	/**
	 * Обновление редактора, после изменения ресурсов
	 */
	@Override
	public void updateEditor() {
		if (fFullUpdatingNow) {
			return;
		}
		if (kTable().isDiffMode()) {
			fTableModel.updateModel();
		}
		if (fTableModel.getContainer() instanceof DipProject) {
			fKTableComposite.tableUpdate();
		} else {
			fTableModel.getContainer().refresh();
			fKTableComposite.tableUpdate();
		}
		fModelChanged = false;
	}
		
	private void checkRenameInput() {
		IEditorInput input = getEditorInput();
		if (input instanceof TableEditorInput) {
			TableEditorInput tableInput = (TableEditorInput) input;
			if (!tableInput.getContainer().equals(fTableModel.getContainer().resource())) {
				// update input
				tableInput.setContainer(fTableModel.getContainer().resource());
				fContainer = fTableModel.getContainer().resource();
				// update labels
				fProjectLabel.setText(fTableModel.dipProject().decorateName());
				fFileLabel.setText(getIDLabelText(fContainer));
				// update title
				firePropertyChange(PROP_TITLE);
			}
		}
	}
	
	public boolean isFocus() {
		return fMainComposite.isFocusControl() 
				|| kTable().isFocus()
				|| fButtonManger.isFocus();
	}
	
	public void select(IDipDocumentElement dipDocElement) {
		fKTableComposite.selector().setSelection(dipDocElement);
		kTable().refreshTable();		
	}
	
	//========================
	// fire properties
	
	public void visible() {
		firePropertyChange(VISIBLE_EVENT);
	}
	
	public void updateSelection(){
		firePropertyChange(SELECTION_EVENT);
	}
	
	public void fireUpdateTableComposite() {
		firePropertyChange(UPDATE_VIEWER_EVENT);
	}
	
	//==========================
	// find
	
	@Override
	public IFinder find(String text, boolean caseSensitive, boolean findInId) {
		return fKTableComposite.find(text, caseSensitive, findInId);
	}
	
	@Override
	public void selectNext(IDipDocumentElement dipDocElement){
		select(dipDocElement);
	}
	
	@Override
	public void cleanFind() {
		if (fKTableComposite != null && !kTable().isDisposed()) {
			fKTableComposite.cleanFind();
			kTable().refreshTable();
		}
	}
	
	@Override
	public void setFindMode(boolean value) {
		kTable().getTableSettings().setFindMode(value);
	}
	
	@Override
	public void updateFindedElements(List<IDipDocumentElement> findedElements) {
		updater().updateElements(findedElements);
		kTable().refreshTable();
	}
	
	@Override
	public String getLastSearchedText() {
		return fLastSearchedText;
	}

	@Override
	public void setLastSearchedText(String searchingText) {
		fLastSearchedText = searchingText;
	}
	
	//==========================
	// save
	
	@Override
	public boolean isDirty() {
		return fDirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			TableWriter.saveModel(fTableModel);
			ResourcesUtilities.updateProject(fTableModel.resource());
			fDirty = false;
			firePropertyChange(PROP_DIRTY);
			WorkbenchUtitlities.updateProjectExplorer();
			updateListeners();
		} catch (ParserConfigurationException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void updateListeners() {
		// DIP TOC
		refreshDipToc();
		// DIP MARKS
		fireMarkListener();
	}
		
	public void refreshDipToc() {
		DipTocView tocView = getIfVisible();
		if (tocView != null) {
			tocView.refreshViewer();
		}
	}
	
	public void updateDipToc() {
		DipTocView tocView = getIfVisible();
		if (tocView != null) {
			tocView.update(this);
		}
	}
	
	public DipTocView getIfVisible() {
		IViewPart part = getSite().getPage().findView(DipTocView.ID);
		if (part != null && getSite().getPage().isPartVisible(part)) {
			return (DipTocView) part;
		}
		return null;
	}
	
	public void addMarksListener(IMarksUpdateListener markView) {
		fMarksUpdateListener = markView;
	}

	public void fireMarkListener() {
		if (fMarksUpdateListener != null) {
			fMarksUpdateListener.updateMarks();
		}
	}
	
	@Override
	public void doSaveAs() {

	}
	
	//===============================
	// dispose
	
	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		fMarksUpdateListener = null;
		
		if (fParentComposite != null && !fParentComposite.isDisposed()) {
			fParentComposite.removeControlListener(fParentControlListener);
		}
		
		if (fMainComposite != null && !fMainComposite.isDisposed()) {
			fMainComposite.removeMouseListener(fDeselectMouseListener);
		}
		fDeselectMouseListener = null;
		
		fButtonManger.removeMouseListener();

		DipEditorRegister.instance.unregisterEditor(this);
		fParentControlListener = null;
		fUpdater = null;
		
		fTableModel.dispose();
		fTableModel = null;
		
		fContainer = null;
		fMarksUpdateListener = null;
		fParentControlListener = null;
		
		fButtonManger.dispose();
		fButtonManger = null;
		
		fKTableComposite.dispose();
		fKTableComposite = null;
		
		super.dispose();
		UnitPresentationCache.clearHash();
	}
	
	//==========================
	// getters & setters
	
	public KTableComposite kTable() {
		return fKTableComposite;
	}
	
	public ButtonManager getButtonManager(){
		return fButtonManger;
	}
	
	public DipProject getDipProject(){
		return fTableModel.dipProject();
	}
	
	@Override
	public TableModel model(){
		return fTableModel;
	}
	
	public DipEditorUpdater updater() {
		return fUpdater;
	}

	@Override
	public DipProject dipProject() {
		return getDipProject();
	}

}
