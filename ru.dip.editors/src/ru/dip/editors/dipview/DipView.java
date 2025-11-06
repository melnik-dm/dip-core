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
package ru.dip.editors.dipview;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;

import ru.dip.core.model.DipProject;
import ru.dip.core.unit.TextPresentation;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.UmlUtilities;
import ru.dip.core.utilities.xml.XmlStringUtilities;
import ru.dip.editors.Messages;
import ru.dip.editors.md.MDEditor;
import ru.dip.editors.utilities.image.ImageProvider;
import ru.dip.table.editor.MultiPageTableEditor;
import ru.dip.ui.action.hyperlink.ReqLink;

public class DipView extends ViewPart implements /*ISelectionListener,*/ IPropertyListener {

	public static final String ID = Messages.DipView_ID;

	public enum ViewType {
		UML, DOT, MD, TABLE
	}
	
	// model
	@SuppressWarnings("unused")
	private ViewType fViewType;
	private double fZoom = 1;
	private IEditorPart fEditor;	
	// control
	private Composite fParentComposite;
	private ScrolledComposite fScrolledComposite;
	private Composite fComposite;
	private Label fLabel;
	private boolean fIsImageComposite = false;
	
	public DipView() {
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
		createToolbar(toolbar);
	}
	
	private void createToolbar(IToolBarManager toolbarManager){
		toolbarManager.add(new ViewSizeAction());
		toolbarManager.add(new OriginalSizeAction());
		toolbarManager.add(new ZoomPlusAction());
		toolbarManager.add(new ZoomMinusAction());		
	}
	
	@Override
	public void createPartControl(Composite parent) {
		fParentComposite = parent;
		fScrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		fComposite = new Composite(fScrolledComposite, SWT.H_SCROLL);
		fComposite.setLayout(new FillLayout());
		fComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		fScrolledComposite.setExpandHorizontal(true);
		fScrolledComposite.setExpandVertical(true);
		fScrolledComposite.setMinSize(fComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void setNullComposite() {
		fIsImageComposite = false;
		fViewType = null;
		layoutComposite();
		fComposite.layout();
		fParentComposite.layout();
	}
	
	private void setHtmlComposite(String html, IFile file) {
		fIsImageComposite = false;
		layoutComposite();
		Browser browser = new Browser(fComposite, SWT.NONE);
		String newHtml = ReqLink.changeHtmlLink(html, file);
		browser.setText(newHtml);
		browser.addLocationListener(new LocationListener() {
			
			@Override
			public void changing(LocationEvent event) {
				if (!"about:blank".equals(event.location)){ //$NON-NLS-1$
					event.doit = false;			
					if (event.location.startsWith(Messages.DipView_2)){
						String location = event.location.substring(7);
						ReqLink.openFile(location);
					}					
				}
			}
			
			@Override
			public void changed(LocationEvent event) {

			}
		});
		fComposite.layout();
		fParentComposite.layout();
	}

	private void layoutComposite() {
		fComposite.dispose();
		fComposite = new Composite(fScrolledComposite, SWT.H_SCROLL);
		fComposite.setLayout(new FillLayout());
		fComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		fScrolledComposite.setContent(fComposite);
	}

	@Override
	public void setFocus() {

	}

	//==================================
	// Property changed
	
	@Override
	public void propertyChanged(Object source, int propId) {

		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				if (fComposite.isDisposed()) {
					return;
				}
				if (source instanceof MDEditor) {
					fEditor = (IEditorPart) source;
					changeMD((MDEditor) source, propId);
					fViewType = ViewType.MD;
				} else if (source instanceof MultiPageTableEditor) {
					changeTable((MultiPageTableEditor) source, propId);
					fViewType = ViewType.TABLE;
				}
			}
		});
	}

	private void changeMD(MDEditor mdEditor, int propId) {
		if (propId == MDEditor.SAVE_EVENT || propId == MDEditor.VISIBLE_EVENT) {
			FileEditorInput input = (FileEditorInput) mdEditor.getEditorInput();
			IFile file = input.getFile();
			String html = UmlUtilities.getHtmlFromMDText(file, mdEditor.getUnit());
			setHtmlComposite(html, file);
		}
	}
	
	private void changeTable(MultiPageTableEditor tableEditor, int propId) {
		if (propId == MultiPageTableEditor.SAVE_EVENT || propId == MultiPageTableEditor.VISIBLE_EVENT) {
			FileEditorInput input = (FileEditorInput) tableEditor.getEditorInput();
			IFile file = input.getFile();
			String html = FileUtilities.readFile(file, ""); //$NON-NLS-1$
			html = addBorderAttr(html);
			html = changeLinks(html, file);		
			setHtmlComposite(html, file);
		}
	}
	
	private String addBorderAttr(String original) {
		String[] lines = original.split("\n");			 //$NON-NLS-1$
		if (lines.length > 0) {
			String tag = lines[0];
			String newTag = XmlStringUtilities. changeValueAttribut("border", "1", tag); //$NON-NLS-1$ //$NON-NLS-2$
			lines[0] = newTag;
			StringBuilder builder = new StringBuilder();
			for (String str : lines) {
				builder.append(str);
				builder.append(TagStringUtilities.lineSeparator());
			}
			return builder.toString();
		}
		return original;
	}
	
	private String changeLinks(String original, IFile file) {
		DipProject dipProject = DipUtilities.findDipProject(file);
		if (original != null && !original.isEmpty() && dipProject != null) {
			return TextPresentation.prepareTextWithoutUnit(original, dipProject);
		}
		return original;
	}
	
	//=========================
	// ZOOM
	
	private class ZoomPlusAction extends Action {
		
		public ZoomPlusAction() {
			setToolTipText(Messages.DipView_ZoomPlusActionName);			
			setImageDescriptor(ImageDescriptor.createFromImage(ImageProvider.ZOOM_PLUS));
		}
		
		@Override
		public void run() {
			if (fIsImageComposite){
				fZoom = fZoom * 1.2;
				zoomPlus();
			}
		}
	}
	
	private class ZoomMinusAction extends Action {
		
		public ZoomMinusAction() {
			setToolTipText(Messages.DipView_ZoomMinusActionName);
			setImageDescriptor(ImageDescriptor.createFromImage(ImageProvider.ZOOM_MINUS));
		}
		
		@Override
		public void run() {
			if (fIsImageComposite){
				fZoom = fZoom / 1.2;
				zoomMinus();
			}
		}
	}
	
	private class OriginalSizeAction extends Action {
		
		public OriginalSizeAction(){
			setToolTipText(Messages.DipView_DefaultActionName);
			setImageDescriptor(ImageDescriptor.createFromImage(ImageProvider.DEFAULT_SIZE));
		}
		
		@Override
		public void run() {
			if (fIsImageComposite){
				fZoom = 1;
				if (fEditor instanceof MDEditor) {
					changeMD((MDEditor) fEditor, MDEditor.VISIBLE_EVENT);
				} else if (fEditor instanceof MultiPageTableEditor) {
					changeTable((MultiPageTableEditor) fEditor, MultiPageTableEditor.VISIBLE_EVENT);
				}
			}
		}
	}
	
	private class ViewSizeAction extends Action {
		
		public ViewSizeAction(){
			setToolTipText(Messages.DipView_ViewSizeActionName);
			setImageDescriptor(ImageDescriptor.createFromImage(ImageProvider.VIEW_SIZE));
		}
		
		@Override
		public void run() {
			if (fIsImageComposite){
				trimImage();
			}
		}		
	}
	
	private void zoomPlus(){
		Image image = fLabel.getImage();
		Image zoomImage = resizePlus(image);
		fLabel.setImage(zoomImage);
		fScrolledComposite.setMinSize(zoomImage.getBounds().width, zoomImage.getBounds().height);
		fComposite.layout();
		fParentComposite.layout();
		
	}
	
	private void zoomMinus(){
		Image image = fLabel.getImage();
		Image zoomImage = resizeMinus(image);
		fLabel.setImage(zoomImage);
		fScrolledComposite.setMinSize(zoomImage.getBounds().width, zoomImage.getBounds().height);
		fComposite.layout();
		fParentComposite.layout();
	}
	
	private Image resizePlus(Image image){		
		Rectangle rectangle = image.getBounds();
		int width = (int) (rectangle.width * 1.2);
		int height = (int) (rectangle.height * 1.2);
		return resize(image, width ,height);
	}
	
	private Image resizeMinus(Image image){
		Rectangle rectangle = image.getBounds();
		int width = (int) (rectangle.width / 1.2);
		int height = (int) (rectangle.height / 1.2);
		return resize(image, width ,height);
	}

	private Image resize(Image image, int width, int height) {
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0,image.getBounds().width, image.getBounds().height, 0, 0, width, height);
		gc.dispose();
		image.dispose(); // don't forget about me!
		return scaled;
	}
	
	private void trimImage(){		
		int width = fParentComposite.getBounds().width;
		int height = fParentComposite.getBounds().height;
		Image image = fLabel.getImage();		
		int imageWidth = image.getBoundsInPixels().width;
		int imageHeight = image.getBounds().height;
		double widthK = (double) width / imageWidth;
		double heightK = (double) height / imageHeight;		
		if (widthK > heightK){
			width = (int) (image.getBounds().width * heightK);
			fZoom = fZoom * heightK;
		} else {
			height = (int) (image.getBounds().height * widthK);
			fZoom = fZoom * widthK;
		}
		
		Image zoomImage = resize(image, width ,height);
		fLabel.setImage(zoomImage);
		fScrolledComposite.setMinSize(zoomImage.getBounds().width, zoomImage.getBounds().height);
		fComposite.layout();
		fParentComposite.layout();	
	}
	
}
