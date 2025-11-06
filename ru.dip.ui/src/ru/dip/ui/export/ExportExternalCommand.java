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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.exception.ExportCommandException;
import ru.dip.core.model.DipProject;
import ru.dip.core.utilities.ExportScriptProvider;


public class ExportExternalCommand {

	private Process fProcess;
	private boolean fDocx = true;
	private DipProject fDipProject;
	private String fOut;
	private String fConfig;

	public ExportExternalCommand(DipProject project, boolean docx, String out, String config) {
		fDipProject = project;
		fDocx = docx;
		fOut = out;
		fConfig = config;
		if (fOut == null) {
			fOut = fDipProject.getProject().getLocation().toOSString();
		}
	}

	/**
	 * Стандартный вызов внешнего процесса не может переварить большой вывод по этому используется класс StreamGobbler
	 */
	public boolean runCommand() throws ExportCommandException {
		DipCorePlugin.logInfo("Run export external command");
		String[] command = getCommand();
		File workDirectory = getWorkDirectory();
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(workDirectory);

		try {
			fProcess = builder.start();
			StreamGobbler errorGobbler = new StreamGobbler(fProcess.getErrorStream(), "ERROR");
			StreamGobbler outputGobbler = new StreamGobbler(fProcess.getInputStream(), "OUTPUT");
			// kick them off
			errorGobbler.start();
			outputGobbler.start();
			try {
				int n = fProcess.waitFor();
				if (n != 0) {
					String outputMessage = outputGobbler.getMessage();
					String errorMessage = errorGobbler.getMessage();
					if (!errorMessage.isEmpty()) {
						outputMessage += "\n";
						outputMessage += errorMessage;
					}
					throwExportException(command, outputMessage, null);
				}
				return true;
			} catch (InterruptedException e) {
				DipCorePlugin.logInfo("Run export external command - error1  " + e.getMessage());
				throwExportException(command, e.getMessage(), e);
			}
		} catch (IOException e) {
			DipCorePlugin.logInfo("Run export external command - error2  " + e.getMessage());
			throwExportException(command, e.getMessage(), e);
		}
		return false;
	}
	
	private void throwExportException(String[] command, String errorMessage, Throwable e) throws ExportCommandException {
		String commandLine = String.join(" ", command); //$NON-NLS-1$
		throw new ExportCommandException(commandLine, errorMessage, e);
	}

	private File getWorkDirectory() {
		return new File(fDipProject.getProject().getLocation().toOSString());
	}

	private String[] getCommand() {
		return new String[] { ExportScriptProvider.getInstance().getExec(fDocx),
				fDipProject.getProject().getFile(fConfig).getLocation().toOSString(), "--out=" + fOut //$NON-NLS-1$
		};
	}

	public void stop() {
		if (fProcess != null && fProcess.isAlive()) {
			fProcess.destroy();
		}
	}

	class StreamGobbler extends Thread {
		
		private InputStream fInputStream;
		@SuppressWarnings("unused")
		private String fType;

		StringBuilder fBuilder = new StringBuilder();
		StreamGobbler(InputStream is, String type) {
			fInputStream = is;
			fType = type;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(fInputStream);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					fBuilder.append(line);
					fBuilder.append("\n");
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		public String getMessage() {
			return fBuilder.toString();
		}

	}
}
