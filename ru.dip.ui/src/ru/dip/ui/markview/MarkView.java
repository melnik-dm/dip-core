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
package ru.dip.ui.markview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IMarkable;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.core.utilities.ui.ControlFactory;
import ru.dip.core.utilities.ui.GridDataFactory;
import ru.dip.core.utilities.ui.MouseDoubleClickListener;
import ru.dip.core.utilities.ui.WSelectionListener;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.action.hyperlink.ReqLink;
import ru.dip.ui.table.editor.DipTableEditor;
import ru.dip.ui.utilities.image.ImageProvider;

public class MarkView extends ViewPart implements IMarksUpdateListener {

	public static final String VIEW_ID = "ru.dip.ui.view.marks";
		
	// control
	private Composite fParentComposite;
	private Composite fMainComposite;
	private ScrolledComposite fScroll;
	// model
	private DipProject fCurrentProject;
	private MarkViewModel fModel;
	private boolean[] fFilter = new boolean[IMarkable.MARKS_SIZE];
	
	public MarkView() {
		Arrays.fill(fFilter, true);
	}
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		addActions(site);
	}
	
	//===========================
	// actions
	
	private void addActions(IViewSite site) {
		IToolBarManager toolbarManager = site.getActionBars().getToolBarManager();
		addFilterAction(toolbarManager);
	}
	
	private void addFilterAction(IToolBarManager toolbarManager) {
		Action filterAction = new Action(null, SWT.DROP_DOWN) {};
		filterAction.setImageDescriptor(ImageProvider.FILTER_DESC);
		filterAction.setMenuCreator(new FilterMenuCreator());
		toolbarManager.add(filterAction);
	}
	
	class FilterMenuCreator implements IMenuCreator{
		
		private MenuItem[] fItems = new MenuItem[IMarkable.MARKS_SIZE];
		
		@Override
		public void dispose() {}

		@Override
		public Menu getMenu(Menu parent) {
			Menu menu = new Menu(parent);
			fillMenu(menu);
			return menu;
		}

		@Override
		public Menu getMenu(Control parent) {
			Menu menu = new Menu(parent);
			fillMenu(menu);
			return menu;
		}
		
		private void fillMenu(Menu menu) {
			IMarkable.markNumberSteam().forEach(markNumber -> addMenuItem(menu, markNumber));						
		}
		
		
		private void addMenuItem(Menu menu, int markNumber) {
			MenuItem item = new MenuItem(menu, SWT.CHECK);
			item.setImage(ImageProvider.BOOKMARKS[markNumber]);
			item.setSelection(fFilter[markNumber]);
			fItems[markNumber] = item;
			item.addSelectionListener(new WSelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					fFilter[markNumber] = !fFilter[markNumber];
					updateMainComposite();
				}
			});
		}
	}
	
	//=============================
	// create control

	@Override
	public void createPartControl(Composite parent) {
		fParentComposite = CompositeBuilder.instance(parent).full().notIndetns().build();
		fParentComposite.setLayout(new FillLayout());
		fScroll = new ScrolledComposite(fParentComposite, SWT.V_SCROLL);
		fMainComposite = CompositeBuilder.instance(fScroll).full().notIndetns().build();
	}
	
	private void checkMarks() {
		Display.getCurrent().asyncExec(() -> {
			fCurrentProject = WorkbenchUtitlities.getDipProjectFromOpenedEditor();			
			fModel = fCurrentProject != null ? new MarkViewModel(fCurrentProject) : null;
			updateMainComposite();
			
		});		
	}
	
	private void updateMainComposite() {
		disposeComposites();
		createComposites();
		createMarkableControls();
	}
	
	private void disposeComposites() {
		if (fMainComposite != null && !fMainComposite.isDisposed()) {
			fMainComposite.dispose();
		}
		if (fScroll != null && !fScroll.isDisposed()) {
			fScroll.dispose();
		}
	}
	
	private void createComposites() {
		fScroll = new ScrolledComposite(fParentComposite, SWT.V_SCROLL);
		fMainComposite = CompositeBuilder.instance(fScroll)				
				.background(ColorProvider.DEFAULT_COLOR)
				.verticallSpacing(0)
				.notIndetns()
				.full()
				.build();
	}
	
	private void createMarkableControls() {
		if (fCurrentProject != null) {						
			List<Integer> filter = getFilter();
			List<IMarkable> markables = fModel.getMarkables(filter);
			if (!markables.isEmpty()) {
				markables.forEach(markable -> createMarkableComposite(markable, filter));			
				setScrollSize();
			}			
		}		
	}
	
	/**
	 * Преобразует фильтры из boolean в список номеров меток, которые нужно отображать 
	 */
	private List<Integer> getFilter(){
		List<Integer> filter = new ArrayList<>();
		for (int i = 0; i < fFilter.length; i++) {
			if (fFilter[i]) {
				filter.add(i);
			}
		}
		return filter;
	}
	
	
	private void createMarkableComposite(IMarkable markable, List<Integer> filter) {
		MouseDoubleClickListener doubleClickListener = doubleClickListener(markable);
		int labels = getMarkCount(markable, filter) + 1;
		Composite composite = CompositeBuilder.instance(fMainComposite)
				.notIndetns()
				.background(ColorProvider.DEFAULT_COLOR)
				.horizontalSpacing(0)
				.border()
				.horizontal()
				.marginHeights(4)
				.columns(labels, false).build();
		// mark label
		IMarkable.markNumberSteam()
			.filter(filter::contains)
			.filter(markable::isMark)
			.forEach(markNumber -> {
				Label label = ControlFactory.imageLabel(composite, ImageProvider.BOOKMARKS[markNumber]);
				label.addMouseListener(doubleClickListener);
			});
		// unit label
		Label label = ControlFactory.label(composite, DipUtilities.relativeProjectID((IDipElement) markable));
		GridDataFactory.applyFillHorizontal(label);
		label.addMouseListener(doubleClickListener);
	}
	
	private MouseDoubleClickListener doubleClickListener(IMarkable markable) {
		return e -> ReqLink.openElementInTable((IDipDocumentElement) markable);
	}
	
	private int getMarkCount(IMarkable markable, List<Integer> filter) {
		return (int) IMarkable.markNumberSteam()
				.filter(filter::contains)
				.filter(markable::isMark)
				.count();
	}
	
	private void setScrollSize() {
		fMainComposite.pack();
		fScroll.setExpandVertical(true);
		fScroll.setExpandHorizontal(true);
		fScroll.setMinSize(fMainComposite.getClientArea().width,fMainComposite.getClientArea().height);
		fScroll.setContent(fMainComposite);		
		fParentComposite.layout();
	}
	
	//=========================
	// run update job

	@Override
	public void updateMarks() {
		Job job = new Job("Update Marks") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Display.getDefault().asyncExec(MarkView.this::doUpdate);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
	
	private void doUpdate() {
		if (isVisible()) {
			checkMarks();
		}
	}

	private boolean isVisible() {
		return fParentComposite != null && fParentComposite.isVisible();
	}
	
	//==========================
	// add listener to editor
	
	public void addListenerToEditor() {
		IEditorPart editor = WorkbenchUtitlities.getActiveEditor();
		if (editor != null &&  editor instanceof DipTableEditor) {
			((DipTableEditor) editor).addMarksListener(this);
		}
	}
	
	public void addListenerToEditor(DipTableEditor editor) {
		editor.addMarksListener(this);
	}
	
	//===========================
	// other

	@Override
	public void setFocus() {

	}
}
