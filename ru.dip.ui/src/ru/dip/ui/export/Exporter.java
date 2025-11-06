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
package ru.dip.ui.export;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.exception.ExportCommandException;
import ru.dip.core.exception.IExportException;
import ru.dip.core.model.DipProject;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.ui.Messages;
import ru.dip.ui.dialog.ExportFinishDialog;
import ru.dip.ui.export.error.IExportError;

public class Exporter {
	
	private final Shell fShell;
	private final DipProject fDipProject;
		
	private boolean fDoCancel = false;
	private boolean fExportSuccess = false;
	private String fOutput;
	private List<IExportError> fExportErrors;  // не критичные ошибки (для вывода в сообщение)  
	private ExportExternalCommand fExportCommand;
	private IExportException fException;

	private long fAllUnits = 0;
	
	public Exporter(DipProject project, Shell shell) {
		fDipProject = project;
		fShell = shell;
	}
	
	public void doExport() {
		ExportDialog dialog = new ExportDialog(fShell, fDipProject);
		if (dialog.open() == Dialog.OK) {
			doExport(dialog.getTargetPath(), dialog.getConfigName(), dialog.getExportVersion());
		}
	}
	
	public void doExport(String targetPath, String config, ExportVersion exportVersion) {	
		udpateExportProperties(targetPath, config);  // добавить обновление верси в Properties
		runExportCommand(targetPath, config, exportVersion);
	}
	
	private void udpateExportProperties(String targetPath, String config) {
		fDipProject.getProjectProperties().setExportOut(targetPath);
		fDipProject.getProjectProperties().setExportConfig(config);
	}
	
	private void runExportCommand(String targetPath, String config, ExportVersion exportVersion) {	
		ProgressMonitorDialog progressDialog = createProgressMonitorDialog();
		runExport(progressDialog, targetPath, config, exportVersion);		
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
	
	private ProgressMonitorDialog createProgressMonitorDialog() {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(fShell){
	
			@Override
			protected void cancelPressed() {
				super.cancelPressed();
				fDoCancel = true;
				if (getOpenOnRun()){
					finishedRun();
					if (fExportCommand != null) {
						fExportCommand.stop();
					}
				}
			}
		};
				
		return dialog;
	}
	
	private void runExport(ProgressMonitorDialog progressDialog, String targetPath, String config, ExportVersion version) {		

		switch (version) {
		case JAVA:{
			Path javaPath = Paths.get(targetPath);
			javaVersion(progressDialog, javaPath, config);
			break;
		}
		case PYTHON:{
			pythonVersion(progressDialog, targetPath, config);
			break;
		}
		case BOTH:{
			Path javaPath = Paths.get(targetPath,"java");
			if (!Files.exists(javaPath)) {
				try {
					Files.createDirectories(javaPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			javaVersion(progressDialog, javaPath, config);
			
			
			
			Path pythonPath = Paths.get(targetPath,"python");
			if (!Files.exists(pythonPath)) {
				try {
					Files.createDirectories(pythonPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			pythonVersion(progressDialog, pythonPath.toString(), config);
			break;
		}
		}
	}
	
	

	
	
	private void javaVersion(ProgressMonitorDialog progressDialog, Path targetPath, String config) {
		try {
			progressDialog.run(true, true, new IRunnableWithProgress() {
				
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {										
					try {
						//fDipProject.computeChildren();						
						fDipProject.setUpdateImageListeners(false);
						fAllUnits = DipUtilities.countUnits(fDipProject);
                        monitor.beginTask("Preprocessing", (int) fAllUnits);		
                        ExportPreprocessor exportPreprocessor = new ExportPreprocessor(fDipProject, targetPath, Paths.get(config), monitor,  (int) fAllUnits);
                        fOutput = exportPreprocessor.export();
                        fExportErrors = exportPreprocessor.getExportErrors();
						fExportSuccess = true;						
					} catch (Exception e) {						
						e.printStackTrace();
						if (e instanceof IExportException) {
							DipCorePlugin.logInfo("Export Error " + e.getMessage());					
							fException = (IExportException) e;
						} else {
							DipCorePlugin.logInfo("Export Error " + e.getMessage());
							fException = new ExportCommandException("Java Version Exception", e.getMessage(), e);
						}
					}
                    finally  {
                        monitor.done();
						fDipProject.setUpdateImageListeners(true);
                    }
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
			fException = new ExportCommandException("Java Version Exception", e.getMessage(), e);
		} 	
	}
	
	private void pythonVersion(ProgressMonitorDialog progressDialog, String targetPath, String config) {
		try {
			progressDialog.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {										
					fExportCommand = new ExportExternalCommand(fDipProject, true, targetPath, config);
					try {
						fExportSuccess = fExportCommand.runCommand();
					} catch (ExportCommandException e) {
						e.printStackTrace();
						fException = e;
					}
				}								
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		} 		
	}
	
	private void openResultDialog(String targetPath) {
		String resultPath = fOutput != null ? fOutput : getResultPath(targetPath);
		ExportFinishDialog dialog = new ExportFinishDialog(fShell, resultPath, Messages.ExportDialog_ProjectExported, fExportErrors);
		dialog.open();
	}
	
	private String getResultPath(String path){
		String extension = Messages.ExportDialog_DocxExtension; 		
		Path outPath = Paths.get(path, fDipProject.name() + extension);		
		return outPath.toString();
	}
}
