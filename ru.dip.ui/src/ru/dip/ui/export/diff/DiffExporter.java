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
package ru.dip.ui.export.diff;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.exception.ExportCommandException;
import ru.dip.core.exception.IExportException;
import ru.dip.core.model.DipProject;
import ru.dip.ui.Messages;
import ru.dip.ui.dialog.ExportFinishDialog;
import ru.dip.ui.export.ExportErrorDialog;

public class DiffExporter {
	
	private final Shell fShell;
	private final DipProject fDipProject;
	private final Repository fRepository;
	private final RevCommit fRevCommit1;
	private final RevCommit fRevCommit2;
		
	private boolean fDoCancel = false;
	private boolean fExportSuccess = false;
	private String fOutput;
	private IExportException fException;
	
	public DiffExporter(Repository repository, 
			DipProject project, 
			RevCommit revCommit1,
			RevCommit revCommit2,
			Shell shell) {
		fDipProject = project;
		fRepository = repository;
		fRevCommit1 = revCommit1;
		fRevCommit2 = revCommit2;
		fShell = shell;
	}
	
	public void doExport() {
		DiffExportDialog dialog = new DiffExportDialog(fShell, fDipProject, fRevCommit1.getName(), fRevCommit2.getName());
		if (dialog.open() == Dialog.OK) {
			String targetPath = dialog.getTargetPath();
			String config = dialog.getConfigName();			
			udpateExportProperties(targetPath, config);			
			ProgressMonitorDialog progressDialog = createProgressMonitorDialog();						
			doDiffExport(progressDialog, targetPath, config);				
			if (fDoCancel){
				return;
			}
						
			if (fException != null) {
				new ExportErrorDialog(fShell, fException.getFullMessage()).open();
			}
			if (fExportSuccess) {
				openResultDialog(targetPath);
			}
		}
	}

	private void udpateExportProperties(String targetPath, String config) {
		if (fDipProject != null) {
			fDipProject.getProjectProperties().setExportOut(targetPath);
			fDipProject.getProjectProperties().setExportConfig(config);
		}
	}
	
	private ProgressMonitorDialog createProgressMonitorDialog() {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(fShell){
	
			@Override
			protected void cancelPressed() {
				super.cancelPressed();
				fDoCancel = true;
				if (getOpenOnRun()){
					finishedRun();
				}
			}
		};
				
		return dialog;
	}
		
	private void doDiffExport(ProgressMonitorDialog progressDialog, String targetPath, String config) {		
		try {
			progressDialog.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						DiffExportPreprocessor diffPreprocessor = new DiffExportPreprocessor(
								fDipProject,
								fRepository,
								Paths.get(targetPath),
								Paths.get(config),
								fRevCommit1,
								fRevCommit2);
						diffPreprocessor.doDiffExport();
						fExportSuccess = true;
					} catch (Exception e) {
						DipCorePlugin.logInfo("Export Error " + e.getMessage());
						fException = new ExportCommandException("Diff Export Exception", e.getMessage(), e);
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
		}
	}
			
	private void openResultDialog(String targetPath) {
		String resultPath = fOutput != null ? fOutput : getResultPath(targetPath);
		ExportFinishDialog dialog = new ExportFinishDialog(fShell, resultPath, Messages.ExportDialog_ProjectExported, null);
		dialog.open();
	}
	
	private String getResultPath(String path){
		String extension = Messages.ExportDialog_DocxExtension; 		
		Path outPath = Paths.get(path, fDipProject.name() + extension);		
		return outPath.toString();
	}
}
