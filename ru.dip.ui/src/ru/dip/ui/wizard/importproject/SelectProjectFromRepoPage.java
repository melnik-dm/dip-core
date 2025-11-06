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
package ru.dip.ui.wizard.importproject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.image.ImageProvider;

public class SelectProjectFromRepoPage extends WizardPage {

	private final int MAX_NESTED_LEVEL = 4;
	
	private Composite fMainComposite;
	private CheckboxTreeViewer fViewer;
	
	private List<Path> fDipProjects;
	private List<Path> fCanImportProjects;
	
	protected SelectProjectFromRepoPage() {
		super("Select Project");
		setTitle(Messages.ImportDipProjectPage_Title);
		setDescription(Messages.SelectProjectFromRepoPage_Description);
	}

	@Override
	public void createControl(Composite parent) {
		fMainComposite = CompositeBuilder.instance(parent).full().build();	
		
		fViewer = new CheckboxTreeViewer(fMainComposite);
		fViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		fViewer.setContentProvider(new ITreeContentProvider() {
						
			@Override
			public Object[] getElements(Object inputElement) {
				return fDipProjects.toArray();
			}
			
			@Override
			public boolean hasChildren(Object element) {
				return false;
			}
			
			@Override
			public Object getParent(Object element) {
				return null;
			}

			
			@Override
			public Object[] getChildren(Object parentElement) {
				return null;
			}
		});
			
		fViewer.setLabelProvider(new CellLabelProvider() {
					
			@Override
			public void update(ViewerCell cell) {
				Path path = (Path) cell.getElement();
				cell.setImage(ImageProvider.PROJECT_FOLDER);
				cell.setText(path.getFileName().toString());
				
				if (!fCanImportProjects.contains(path)) {
					cell.setForeground(ColorProvider.GRAY);
				}				
			}				
		});
		
		fViewer.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (!fCanImportProjects.contains(event.getElement())) {
					fViewer.setChecked(event.getElement(), false);
				}				
				setPageComplete(isPageComplete());
			}
		});
				
		setControl(fMainComposite);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			updateContent();
		}
		super.setVisible(visible);
	}
	
	private void updateContent() {
		SelectRepoPage selectRepoPage = (SelectRepoPage) getPreviousPage();
		String folder = selectRepoPage.getRepoFolder();		
		setTitle("Import DIP Project from: " + folder);
		
		Path path = Paths.get(folder);
		fDipProjects = new ArrayList<Path>();
		findDipProjects(path, fDipProjects, 0);
				
		fCanImportProjects = fDipProjects.stream()
			.filter(p -> !ResourcesPlugin.getWorkspace().getRoot().getProject(p.getFileName().toString()).exists())
			.collect(Collectors.toList());
		fViewer.setInput("");		
	}
	
	private void findDipProjects(Path path, List<Path> result, int level){				
		if (DipUtilities.isDipProject(path)) {
			result.add(path);
		} else if (level < MAX_NESTED_LEVEL) {
			try {
				Files.list(path).filter(Files::isDirectory).forEach(p -> findDipProjects(p, result, level + 1));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean isPageComplete() {
		return fViewer.getCheckedElements().length > 0;
	}
	
	public List<Path> getSelectedProjects(){
		return Stream.of(fViewer.getCheckedElements())
				.map(Path.class::cast)
				.collect(Collectors.toList());		
	}

}
