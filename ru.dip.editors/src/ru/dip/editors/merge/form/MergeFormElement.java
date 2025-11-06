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
package ru.dip.editors.merge.form;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.egit.core.internal.storage.CommitFileRevision;
import org.eclipse.egit.core.internal.storage.IndexFileRevision;
import org.eclipse.egit.ui.internal.revision.FileRevisionTypedElement;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.Saveable;

import ru.dip.core.form.FormReader;
import ru.dip.core.form.model.Field;
import ru.dip.core.model.DipProject;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;

public class MergeFormElement {
	
	private List<FieldControl> fControls = new ArrayList<>();
	private CompareEditorInput fCompareContainer;
	private MergeFormElement fCorrespondingElement;
	private ITypedElement fElement;
	private FormReader fFormReader;
	private boolean fReadOnly = true;
	private boolean fDirty = false;	

	public MergeFormElement(CompareEditorInput compareContainer,
			ITypedElement element, boolean readOnly) {
		fCompareContainer = compareContainer;
		fElement = element;
		fReadOnly = readOnly;
		read();
	}
	
	private void read() {
		InputStream schemaStream = getSchemaStream();
		if (schemaStream == null) {
			return;
		}
		try (InputStream formStream = getFormInputStream()) {
			if (formStream == null) {
				return;
			}
			fFormReader = new FormReader(null);
			fFormReader.read(formStream, schemaStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//==============================
	// read schema
	
	private InputStream getSchemaStream() {
		String name = fElement.getName();
		String extension = FileUtilities.getFileExtension(name);
		if (extension == null) {
			return null;
		}
		if (fElement instanceof FileRevisionTypedElement) {
			FileRevisionTypedElement l = (FileRevisionTypedElement) fElement;			
			IFileRevision fileRevision = l.getRevision();
			if (fileRevision instanceof CommitFileRevision) {
				try {
					return getSchemaStream((CommitFileRevision)fileRevision, extension);
				} catch (IOException | GitAPIException e) {
					e.printStackTrace();
				}		
			} else if (fileRevision instanceof IndexFileRevision) {
				IndexFileRevision indexFileRevision = (IndexFileRevision) fileRevision;
				return getSchemaStream(indexFileRevision, extension);
			}
		} else if (fElement instanceof ResourceNode){
			ResourceNode node = (ResourceNode) fElement;
			return getSchemaStream(node, extension);
		}
		return null;
	}
	
	/**
	 * Находит схему по расширениею + .xml
	 * Может быть другой файл с таким названием ??
	 */
	private InputStream getSchemaStream(CommitFileRevision commitFileRevision, String extension)
			throws IOException, NoHeadException, GitAPIException {
		Repository repo = commitFileRevision.getRepository();
		String hash = commitFileRevision.getRevCommit().getName();
		ObjectId objId = repo.resolve(hash);

		try (RevWalk revWalk = new RevWalk(repo)) {
			RevCommit commit = revWalk.parseCommit(objId);
			RevTree tree = commit.getTree();
			try (TreeWalk treeWalk = new TreeWalk(repo)) {
				treeWalk.addTree(tree);
				treeWalk.setRecursive(true);
				while (treeWalk.next()) {
					String name = treeWalk.getNameString();
					if (name.equals(extension + ".xml")) {
						ObjectId objectId = treeWalk.getObjectId(0);
						ObjectLoader loader = repo.open(objectId);
						InputStream stream = loader.openStream();
						return stream;
					}
				}
			}
		}
		return null;
	}
	
	private InputStream getSchemaStream(IndexFileRevision indexFileRevision, String extension) {
		Path directory = indexFileRevision.getRepository().getDirectory().getParentFile().toPath();
		Path fullPath = directory.resolve(indexFileRevision.getGitPath());		
		IFile file = ResourcesUtilities.findFile(fullPath.toString());
		if (file != null && file.exists()) {
			DipProject project = DipUtilities.findDipProject(file);
			if (project != null) {
				IFile schemaFile = project.getSchemaModel().getSchemafile(extension);
				if (schemaFile.exists()) {					
					try {
						return Files.newInputStream(Paths.get(schemaFile.getLocationURI()));
					} catch (IOException e) {
						e.printStackTrace();
					}								
				}	
			}
		}
		return null;
	}

	private InputStream getSchemaStream(ResourceNode node, String extension) {
		DipProject project = DipUtilities.findDipProject(node.getResource());
		if (project != null) {
			IFile file = project.getSchemaModel().getSchemafile(extension);
			if (file.exists()) {					
				try {
					return Files.newInputStream(Paths.get(file.getLocationURI()));
				} catch (IOException e) {
					e.printStackTrace();
				}							
			}				
		}
		return null;
	}
	
	//================================
	// read form
	
	private InputStream getFormInputStream() {
		if (fElement instanceof IStreamContentAccessor) {
			IStreamContentAccessor streamAccessor = (IStreamContentAccessor) fElement;
			try {
				InputStream inputStream = streamAccessor.getContents();
				return inputStream;
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
		
	//================================
	// save
	
	public void save() {
		if (fElement instanceof ResourceNode) {
			ResourceNode node = (ResourceNode) fElement;
			String content = fFormReader.getContent();	
			node.setContent(content.getBytes(StandardCharsets.UTF_8));		
			if (fCompareContainer instanceof ISaveablesSource) {
				ISaveablesSource saveablesSource = (ISaveablesSource) fCompareContainer;
				Saveable[] saveables = saveablesSource.getSaveables();
				Saveable[] activeSaveables = saveablesSource.getActiveSaveables();
				try {
					saveables[0].doSave(new NullProgressMonitor());
				} catch (CoreException e) {
					e.printStackTrace();
				}				
			}
		}	
	}
	
	//================================
	// controls
		
	public void createControls(Composite composite) {
		if (fFormReader != null) {
			for (Field field: fFormReader.getFields()) {					
				FieldStatus status = getStatus(field);
				FieldControl control = FieldControl.createFiledControl(composite, field, fReadOnly);
				control.setStatus(status);
				control.setValue();	
				fControls.add(control);
				if (!fReadOnly) {
					ModifyListener modifyListener = e -> {
						fCompareContainer.setDirty(true);
						fDirty = true;
						control.updateTagValue();
					};
					control.addModifyListener(modifyListener);
				}					
			}
		}
	}
	
	//================================
	// status
	
	public void updateAllControlStatus() {
		fControls.forEach(control -> {
			FieldStatus status = getStatus(control.getField());
			control.setStatus(status);
			control.updateStatus();
		});
	}
	
	private FieldStatus getStatus(Field field) {
		if (fCorrespondingElement.getFields() == null) {
			return FieldStatus.ADDED;
		}
		for (Field rightField: fCorrespondingElement.getFields()) {
			if (rightField.getName().equals(field.getName())) {
				if (Objects.equals(rightField.getValue(),field.getValue())){
					return FieldStatus.EQUALS;
				} else {
					return FieldStatus.EDIT;	
				}
			} 			
		}		
		return FieldStatus.ADDED;
	}
		
	//=================================
	// getters & setters
	
	public boolean isReadOnly() {
		return fReadOnly;
	}
	
	public boolean isDirty() {
		return fDirty;
	}
	
	public void setDirty(boolean dirty) {
		fDirty = dirty;
	}
	
	public List<Field> getFields(){
		if (fFormReader == null) {
			return null;
		}		
		return fFormReader.getFields();
	}
	
	public void setCorrespondingElement(MergeFormElement corresponding) {
		fCorrespondingElement = corresponding;
	}
		
}
