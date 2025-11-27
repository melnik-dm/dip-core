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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ru.dip.core.external.editors.IDipHtmlRenderExtension;
import ru.dip.core.external.editors.IDipImageRenderExtension;
import ru.dip.core.utilities.EditorUtils;
import ru.dip.editors.Messages;
import ru.dip.editors.utilities.image.ImageProvider;
import ru.dip.ui.action.hyperlink.ReqLink;

public class DipView extends ViewPart implements /*ISelectionListener,*/ IPropertyListener {

	public static final String ID = Messages.DipView_ID;

	
	// model
	@SuppressWarnings("unused")
	private double fZoom = 1;
	private IEditorPart fEditor;	
	// control
	private Composite fParentComposite;
	private ScrolledComposite fScrolledComposite;
	private Composite fComposite;
	private Label fLabel;
	private boolean fIsImageComposite = false;
	private Image fImage;
	
	public DipView() {
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
		createToolbar(toolbar);
		
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		page.getWorkbenchWindow().getActivePage().addPartListener(new DipViewListener(this));

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
		fImage = null;
		fIsImageComposite = false;
		layoutComposite();
		fComposite.layout();
		fParentComposite.layout();
	}

	private void setImageComposite(Image image) {
		fImage = image;
		fIsImageComposite = true;
		layoutComposite();
		fScrolledComposite.setMinSize(image.getBounds().width, image.getBounds().height);
		fLabel = new Label(fComposite, SWT.NULL);
		fLabel.setImage(image);
		fComposite.layout();
		fParentComposite.layout();
	}

	private void setHtmlComposite(String html, IFile file) {
		fImage = null;
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
				if (source instanceof IDipImageRenderExtension) {
					fEditor = (IEditorPart) source;
					changeImageRenderEditor((IDipImageRenderExtension) source, propId);
				} else if (source instanceof IDipHtmlRenderExtension) {
					fEditor = (IEditorPart) source;
					changeHtmlRenderEditor((IDipHtmlRenderExtension) source, propId);
				} 
			}
		});
	}
	

	private void changeImageRenderEditor(IDipImageRenderExtension imageRenderEditor, int propId) {
		if (propId == EditorUtils.SAVE_EVENT || propId == EditorUtils.VISIBLE_EVENT) {
			fImage = imageRenderEditor.renderImage();
			
			if (propId == EditorUtils.SAVE_EVENT){
				fImage = resize(fImage);
			} else {
				fZoom = 1;
			}
			if (propId == EditorUtils.VISIBLE_EVENT) {
				imageRenderEditor.addPropertyListener(this);
			}		
			setImageComposite(fImage);
		} else if (propId == EditorUtils.HIDE_EVENT) {
			setNullComposite();
			imageRenderEditor.removePropertyListener(this);		
		}
	}


	private void changeHtmlRenderEditor(IDipHtmlRenderExtension htmlRenderEditor, int propId) {
		if (propId == EditorUtils.SAVE_EVENT || propId == EditorUtils.VISIBLE_EVENT) {
			String html = htmlRenderEditor.getHtmlPresentation();
			setHtmlComposite(html, htmlRenderEditor.getFile());
			if (propId == EditorUtils.VISIBLE_EVENT) {
				htmlRenderEditor.addPropertyListener(this);
			}
		} else if (propId == EditorUtils.HIDE_EVENT) {
			setNullComposite();
			htmlRenderEditor.removePropertyListener(this);		
		}
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
				if (fEditor instanceof IDipImageRenderExtension) {
					changeImageRenderEditor((IDipImageRenderExtension) fEditor, EditorUtils.VISIBLE_EVENT);
				} else if (fEditor instanceof IDipHtmlRenderExtension) {
					changeHtmlRenderEditor((IDipHtmlRenderExtension) fEditor, EditorUtils.VISIBLE_EVENT);
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
	
	private Image resize(Image image){	
		Rectangle rectangle = image.getBounds();
		int width = (int) (rectangle.width * fZoom);
		int height = (int) (rectangle.height * fZoom);
		return resize(image, width ,height);
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
		int imageWidth = image.getBounds().width;
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
