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
package ru.dip.editors.md;

import java.io.IOException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.TextOperationAction;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.external.editors.IDipHtmlRenderExtension;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.EditorUtils;
import ru.dip.core.utilities.UmlUtilities;
import ru.dip.core.utilities.ui.WSelectionListener;
import ru.dip.editors.Messages;
import ru.dip.editors.md.MarkdownDocument.MdDocumentListener;
import ru.dip.editors.md.actions.MdActionUpdater;
import ru.dip.editors.md.actions.TextTransferExecutor;
import ru.dip.editors.md.comment.CommentManager;
import ru.dip.editors.md.comment.ICommentManagerHolder;
import ru.dip.editors.md.model.MdDocumentModel;
import ru.dip.editors.md.partitions.PartitionStyles;
import ru.dip.ui.preferences.MdPreferences;
import ru.dip.ui.preferences.MdPreferences.MdPreferenciesListener;

public class MDEditor extends TextEditor implements MdPreferenciesListener, IDocumentListener, MdDocumentListener,
		ISaveNotifier, IMdEditor, ICommentManagerHolder, IDipHtmlRenderExtension {

	public static final String ID = Messages.MDEditor_ID;
	public static final String MD_CONTEXT = "ru.dip.editors.context.mdeditor";

	public static final String ANNOTATION_BOLD = Messages.MDEditor_BoldTypeID;
	public static final String ANNOTATION_ITALIC = Messages.MDEditor_ItalicTypeID;

	private IDocument fDocument;
	private DipUnit fDipUnit;
	private IFile fFile;
	private DipProject fDipProject;
	private MDViewerConfiguration fViewerConfiguration;
	private MdDocumentModel fModel;
	private MarkdownDocument fMdDocument;
	private TextTransferExecutor fTextTransferExecutor;
		
	// for save-dirty  (счетчики состояний)  
	private long fLastTimeStamp = 1;
	private long fCurrentLastTimeStamp = 1;	
	
	// controls
	private Composite fMainComposite;
	private Composite fEditorComposite;
	private SourceViewer fViewer;
	
	// comments	
	public boolean fAutoFormatProcess;  // флаг - показывает, что запущена команда автоформат    
	private CommentManager fCommentManager;
	
	
	public MDEditor() {
		setDocumentProvider(new MDDocumentProvider(this));
		MdPreferences.instance().addListener(this);
		fTextTransferExecutor = new TextTransferExecutor(this);
		fCommentManager = new CommentManager(this);
	}
	
	@Override
	public void firePropertyChange(int property) {
		super.firePropertyChange(property);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		fMainComposite = new Composite(parent, SWT.BORDER);
		fMainComposite.setLayout(new GridLayout(2, false));
		fMainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		fEditorComposite = new Composite(fMainComposite, SWT.BORDER);
		fEditorComposite.setLayout(new FillLayout());
		fEditorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		if (fCommentManager.isShowComment()) {
			fCommentManager.createCommentComposite(fMainComposite);
		}
		
		super.createPartControl(fEditorComposite);
	    if (isWordWrapSupported()) {
	    	setWordWrap(true);
	    }
	}
	
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		if (input == null) {
			close(false);
			return;
		}
		super.doSetInput(input);
		if (input instanceof FileEditorInput) {
			FileEditorInput fileEditorInput = (FileEditorInput) input;
			fFile = fileEditorInput.getFile();
			if (!fFile.exists()) {
				return;
			}			
			fDocument = getDocumentProvider().getDocument(input);
			fDipProject = DipUtilities.findDipProject(fFile);
			IDipElement dipElement = DipUtilities.findDipElementInProject(fFile, fDipProject);
			fDipUnit = (DipUnit) dipElement;
			if (fDipUnit == null) {
				DipCorePlugin.logError("MDEDITOR NOT FOUND DIPUNIT: " + fFile + "  "  + fDipProject);
			}
			fCommentManager.setCommentModel(fDipUnit);
			fCommentManager.setCommentModel(fDocument);
		} else {
			fDocument = getDocumentProvider().getDocument(input);
		}
		fDocument.addDocumentListener(this);
		fModel = new MdDocumentModel(fDocument, fDipProject);
		fModel.createModel();
		setSourceViewerConfiguration(fViewerConfiguration = new MDViewerConfiguration(this, fDipUnit, fDocument));
		ISourceViewer viewer = getSourceViewer();
		if (viewer != null) {
			initViewer(viewer);
		}
	}
	
	@Override
	protected void initializeViewerColors(ISourceViewer viewer) {
		super.initializeViewerColors(viewer);
	}
	
	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		fAnnotationAccess= getAnnotationAccess();
		fOverviewRuler= createOverviewRuler(getSharedColors());
		fViewer= new MDViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		initViewer(fViewer);
		fCommentManager.addCommentDecorationInSourceViewer();
		return fViewer;
	}
	
	private void initViewer(ISourceViewer viewer) {
		fMdDocument = new MarkdownDocument(viewer.getTextWidget(), fDocument, fModel);
		fMdDocument.addListener(this);
		
		getSourceViewerDecorationSupport(viewer);
			
		viewer.getTextWidget().addCaretListener(new CaretListener() {

			@Override
			public void caretMoved(CaretEvent event) {
				fMdActionUpdater.updateActionStatus();

			}
		});

		viewer.getTextWidget().addSelectionListener(new WSelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				fMdActionUpdater.updateActionStatus();
			}
		});
	}
	
	//=================
	// actions
		
	private MdActionUpdater fMdActionUpdater = new MdActionUpdater(this) {
		
		public void updateActionStatus() {
			if (checkEditor()) {
				super.updateActionStatus();
			}		
		}
	
		protected boolean checkEditor() {
			IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			if (part == null || !part.equals(MDEditor.this)) {
				return false;
			}
			return true;
		}
	};
	
	@Override
	public void mdDocumentUpdated() {
		fMdActionUpdater.updateActionStatus();
	}
		
	//==================================================
	
	@Override
	public void setFocus() {
		super.setFocus();
		fMdActionUpdater.updateActionStatus();
	}

	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
	}

	@Override
	protected void setEditorContextMenuId(String contextMenuId) {
		// иначе отображается 2 команды format
		// super.setEditorContextMenuId(contextMenuId);
	}

	@Override
	protected void createActions() {		
		String BUNDLE_FOR_CONSTRUCTED_KEYS = Messages.MDEditor_BundleForConstructedKeys;
		ResourceBundle fgBundleForConstructedKeys = ResourceBundle.getBundle(BUNDLE_FOR_CONSTRUCTED_KEYS);
		super.createActions();
		IAction action = new TextOperationAction(fgBundleForConstructedKeys, "Format.", this, ISourceViewer.FORMAT); //$NON-NLS-1$
		action.setActionDefinitionId(Messages.MDEditor_FormatActionID);
		setAction(Messages.MDEditor_FormatActionName, action);
		markAsStateDependentAction("Format", true); //$NON-NLS-1$
		markAsSelectionDependentAction(Messages.MDEditor_FormatActionName, true);
	}
	
	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] {MD_CONTEXT});
	}
		
	//==================
	// save
	
	@Override
	public void doSave(IProgressMonitor progressMonitor) {
		fLastTimeStamp = fCurrentLastTimeStamp;
		super.doSave(progressMonitor);
		try {
			fCommentManager.deleteTextCommentFromMainFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		firePropertyChange(EditorUtils.SAVE_EVENT);
	}
	
	@Override
	public boolean isDirty() {
		return fLastTimeStamp != fCurrentLastTimeStamp;
	}
	
	@Override
	public void dispose() {	
		MdPreferences.instance().removeListener(this);
		super.dispose();
	}

	@Override
	public void mardownPreferencesChanged() {		
		PartitionStyles.updateTokens();		
		fViewerConfiguration.getReconciler(getSourceViewer()).install(getSourceViewer());
		fViewerConfiguration.getPresentationReconciler(getSourceViewer()).install(getSourceViewer());
	}
		
	//============================
	// Document Listener
	
	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		if (fAutoFormatProcess) {
			fCommentManager.saveAnnotationSelectedTexts();
		} else {
			fCommentManager.savedCoveredAnnotations(event);
		}
	}

	@Override
	public void documentChanged(DocumentEvent event) {		
		// замена кавычек, длинное тире
		fAutoCorrector.autoCorrect(event);
		fCommentManager.updateCommentPositions(event, fAutoFormatProcess);
		
		// автоперенос текста
		if (MdPreferences.autoTextTransfer()) {
			if (fTextTransferExecutor.transfer(event)) {
				return;
			}
		}

		fModel.createModel();
		fCurrentLastTimeStamp = event.getModificationStamp();
		firePropertyChange(PROP_DIRTY);
		fMdActionUpdater.updateActionStatus();

	}
	
	private MdAutoCorrector fAutoCorrector = new MdAutoCorrector() {
		
		@Override
		protected void replace(int offset, int length, String text) {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					TextViewer viewer = (TextViewer) getViewer();
					viewer.getTextWidget().replaceTextRange(offset, length, text);
				}
			});		
		}
	};
	
	public void incrementCurrentTimeStamp() {
		fCurrentLastTimeStamp++;
	}
	
	@Override
	public String getHtmlPresentation() {
		return UmlUtilities.getHtmlFromMDText(fFile, fDipUnit);
	}
	
	//==================
	// getters
	
	public StyledText textWidget() {
		return getSourceViewer().getTextWidget();
	}
	
	public TextViewer getViewer() {
		return (TextViewer) super.getSourceViewer();
	}
	
	/*public ISourceViewer getMDViewer() {
		return fViewer;
	}*/
	
	public MdDocumentModel mdModel() {
		return fModel;
	}
	
	public MarkdownDocument mdDocument() {
		return fMdDocument;
	}
		
	public IDocument document() {
		return fDocument;
	}

	public DipUnit getUnit() {
		return fDipUnit;
	}

	@Override
	public CommentManager getCommentManager() {
		return fCommentManager;
	}

	public Composite getMainComposite() {
		return fMainComposite;
	}

	@Override
	public TextViewer getMDViewer() {
		return fViewer;
	}

	@Override
	public StyledText styledText() {
		return textWidget();
	}

	@Override
	public IFile getFile() {
		return fFile;
	}

}
