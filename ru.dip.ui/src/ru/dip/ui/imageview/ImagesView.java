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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipProject.ProjectImagesListener;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;
import ru.dip.ui.imageview.interfaces.IImageComposite;
import ru.dip.ui.imageview.interfaces.IImageView;
import ru.dip.ui.imageview.interfaces.IImageViewPreferences;
import ru.dip.ui.imageview.ui.HorizontalImageComposite;
import ru.dip.ui.imageview.ui.VerticalImageComposite;
import ru.dip.ui.utilities.dip.DipUnitManager;

public class ImagesView extends ViewPart implements ProjectImagesListener, IImageView {

	public static final String ID = Messages.ImagesView_ID;

	public static final int IMAGE_WIDTH = 70;
	public static final int HORIZONTAL_IMAGE_WIDTH = 160;
	
	// control
	private Composite fParent;
	private Composite fMainComposite;
	private Table fTable;
	private IImageComposite fImageComposite;
	// model
	protected DipProject fProject;
	protected List<IDipUnit> fUnits = new ArrayList<>();	
	protected IFile fCurrentFile;

	private IImageViewPreferences fPreferences;	
	private ImageViewActionManager fActionManager;
	private ImageViewSelector fSelector;
	private ImagesFontProvider fFontProvider = new ImagesFontProvider();
	private DipUnitManager fUnitManager;
	private ImageViewMouseAdapter fMouseAdapter;
	private ImagesCacheProvider fImageProvider = new ImagesCacheProvider();
		
	public ImagesView() {
		fActionManager = new ImageViewActionManager(this);
		fPreferences = new ImageViewPreferences();
		fUnitManager = new DipUnitManager(this);
		fSelector = new ImageViewSelector(this);
		fMouseAdapter = new ImageViewMouseAdapter(this);
	}
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		fActionManager.init(site);
	}

	//=============================
	// control
	
	@Override
	public void createPartControl(Composite parent) {
		fParent = parent;
		fMainComposite = new Composite(fParent, SWT.NONE);
		fMainComposite.setLayout(new FillLayout());
		createComposite();		
	}
	
	@Override
	public void createComposite() {
		DipProject project =  WorkbenchUtitlities.getDipProjectFromOpenedEditor();;
		IFile file = WorkbenchUtitlities.getFileFromOpenedEditor();
		createComposite(project, file);
	}
	
	public void createComposite(DipProject project, IFile file) {
		if (fParent.isDisposed()) {
			return;
		}
		fCurrentFile = file;		
		prepareComposite(project);
		createImageComposite();
	}
		
	private void prepareComposite(DipProject project) {		
		clear();
		fProject = project;		
		fMainComposite = new Composite(fParent, SWT.BORDER);
		fMainComposite.setLayout(new FillLayout());
		fMainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		if (fProject == null) {
			return;
		}		
		fProject.addImagesListener(this);
		getUnitsFromProject();
	}
	
	private void clear() {
		fSelector.clearSelection();
		fUnits.clear();
		if (fProject != null) {
			fProject.removeImagesListener(this);
		}	
		fProject = null;		
		if (fMainComposite != null && !fMainComposite.isDisposed()) {			
			fMainComposite.dispose();		
		}
		fImageProvider.dispose();
	}
	
	protected void getUnitsFromProject() {
		if (fPreferences.isFolderMode()) {
			if (fCurrentFile != null) {
				IDipElement element = DipUtilities.findElement(fCurrentFile);
				if (element != null) {
					IParent parent = element.parent();
					if (parent instanceof IDipParent) {
						fUnits = fProject.images()
								.stream()
								.filter(im -> im.parent().equals(parent))
								.collect(Collectors.toList());
						return;
					}
				}
			}
		}
		fUnits = new ArrayList<>(fProject.images());
	}
		
	private void createImageComposite() {	
		if (fPreferences.isHorizontalMode()) {
			createHorizontalTable();
		} else {
			createVerticalTable();
		}
		createListeners();
		
		fMainComposite.layout();
		fParent.layout();	
		fMainComposite.addControlListener(new ControlAdapter() {
			
			@Override
			public void controlResized(ControlEvent e) {
				fImageComposite.updateTable();
			}

		});
	}
		
	private void createVerticalTable() {
		fImageComposite = new VerticalImageComposite(this);
		fTable = fImageComposite.createTable(fMainComposite);
	}
	
	private void createHorizontalTable() {
		fImageComposite = new HorizontalImageComposite(this);
		fTable = fImageComposite.createTable(fMainComposite);
	}

	//=============================
	// Listeners
	
	private void createListeners() {
		addMouseListener();
		fActionManager.addMenu();
		fActionManager.addDragListener();
		fActionManager.addHotKeyListener();
	}
		
 	private void addMouseListener() {
    	// если установлен как false, тогда не работает перетаскивание
 		// если включить перетаскивание, то есть проблемы в Windows на слушатель мыши down-up
 		// (down срабатывает только вместе с up)
 		if (ResourcesUtilities.isWindows) {
 			fTable.setDragDetect(false);
 		}
 		fTable.addMouseListener(fMouseAdapter);
	}
	
	//=================================
	// update
		
	@Override
	public void imagesChanged() {
		if (isVisible()) {
			updateIfNeed();
		}
	}
	
	private void updateIfNeed() {
		DipProject project =  WorkbenchUtitlities.getDipProjectFromOpenedEditor();;
		IFile file = WorkbenchUtitlities.getFileFromOpenedEditor();
		if (Objects.equals(project, fProject) && Objects.equals(file, fCurrentFile)) {
			getUnitsFromProject();
			fImageComposite.updateTable();
		} else {
			fullUpdate();
		}
	}
	
	private void fullUpdate() {
		clear();
		createComposite();
	}
	
	protected boolean isVisible() {
		// тут ошибка InvalidThreadAccsess в fParent.isVisible
		
		return fParent != null && !fParent.isDisposed() && fParent.isVisible();
	}
	
	@Override
	public void setFocus() {}
	
	@Override
	public void dispose() {
		super.dispose();
		fFontProvider.dispose();
		fImageProvider.dispose();
	}
		
	//===================
	// getters
	
	@Override
	public IDipUnit getDipUnit() {
		return fSelector.selectionUnit();
	}
	
	@Override
	public IImageViewPreferences getPreferences() {
		return fPreferences;
	}
	
	@Override
	public Table getTable() {
		return fTable;
	}
	
	@Override
	public DipUnitManager getDipUnitManager() {
		return fUnitManager;
	}

	@Override
	public ImageViewSelector getImageSelector() {
		return fSelector;
	}

	@Override
	public List<IDipUnit> getUnits() {
		return fUnits;
	}

	@Override
	public int getHeight() {
		 return fParent.getBounds().height;
	}

	@Override
	public int getWidth() {
		 return fParent.getBounds().width;
	}

	@Override
	public ImagesFontProvider getFontProvider() {
		return fFontProvider;
	}
	
	@Override
	public ImagesCacheProvider getImageProvider() {
		return fImageProvider;
	}

}
