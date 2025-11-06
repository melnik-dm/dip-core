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

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.egit.ui.internal.repository.RepositoriesViewContentProvider;
import org.eclipse.egit.ui.internal.repository.RepositoryTreeNodeLabelProvider;
import org.eclipse.egit.ui.internal.repository.tree.RepositoryNode;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import ru.dip.core.utilities.GITUtilities;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.ui.BrowseFolderComposite;

public class SelectRepoPage extends WizardPage {

	private BrowseFolderComposite fBrowseComposite; 
	
	protected SelectRepoPage() {
		super("Select Repo");
		setTitle(Messages.ImportDipProjectPage_Title);
		setDescription(Messages.SelectRepoPage_Description);
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite composite = CompositeBuilder.instance(parent).full().build();	
		browseComposie(composite);
		createRepoViewer(composite);		
		setControl(composite);
	}
	
	private void browseComposie(Composite parent) {
		fBrowseComposite = new BrowseFolderComposite(parent, "Репозиторий: ");
		fBrowseComposite.getTextControl().addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				setPageComplete(isPageComplete());
			}
		});
		fBrowseComposite.setStartDirectory(GITUtilities.defaultRepositoryDir());
	}
	
	
	private void createRepoViewer(Composite parent) {
		TreeViewer fViewer = new TreeViewer(parent);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);	
		RepositoriesContentProvider contentProvider = new RepositoriesContentProvider();
		RepositoryTreeNodeLabelProvider labelProvider = new RepositoryTreeNodeLabelProvider();
		
		fViewer.setContentProvider(contentProvider);
		fViewer.setLabelProvider(labelProvider);
		fViewer.setInput("");
		
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
				@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Object obj = event.getStructuredSelection().getFirstElement();
				if (obj instanceof RepositoryNode) {
					RepositoryNode node = (RepositoryNode) obj;
					String path = node.getRepository().getDirectory().getParent().toString();
					fBrowseComposite.setValue(path);				
				}
			}
		});
		
		Point size = fViewer.getTree().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		fViewer.getTree().setSize(SWT.DEFAULT, 200);

		
		if (size.y > 300) {
			gd.heightHint = 300;
		}
		
		fViewer.getTree().setLayoutData(gd);	
	}
	
	private class RepositoriesContentProvider implements ITreeContentProvider {
		
		private final RepositoriesViewContentProvider gitContentProvider = new RepositoriesViewContentProvider();

		@Override
		public Object[] getElements(Object inputElement) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			return gitContentProvider.getElements(root);
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return null;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return false;
		}
	}

	@Override
	public boolean isPageComplete() {
		String repoPath = fBrowseComposite.getValue();
		if (repoPath.isBlank()) {
			return false;
		}
		return GITUtilities.isGitRepo(repoPath);
	}

	public String getRepoFolder() {
		return fBrowseComposite.getValue();
	}

}
