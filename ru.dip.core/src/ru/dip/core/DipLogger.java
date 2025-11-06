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
package ru.dip.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class DipLogger implements ILogListener {

	private File logFile;

	public DipLogger() {
		logFile = getFile();
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (logFile.length() > 2 * 1024 * 1024) {
			try {
				logFile.delete();
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void logging(IStatus status, String plugin) {
		StringBuffer str = new StringBuffer(plugin);
		str.append(": ");
		str.append(getStatusType(status.getSeverity()));
		str.append(": ");
		str.append(LocalDate.now());
		str.append(": ");
		str.append(LocalTime.now());
		str.append(": ");
		str.append(status.getMessage());
		str.append(": ");
		if (status.getException() != null) {
			Throwable exception = status.getException();
			str.append(exception.getClass());
			str.append(": ");
			str.append(exception.getMessage());
			String stackTrace = Arrays.toString(exception.getStackTrace());
			str.append(stackTrace);
		}
		str.append("====================================================");
		str.append("====================================================");
		str.append('\n');
		appendUsingFileWriter(logFile, str.toString());
	}

	public File getFile() {
		Bundle bundle = DipCorePlugin.getDefault().getBundle();
		IPath pathMetaData = Platform.getStateLocation(bundle);
		Path erroLogPath = Paths.get(pathMetaData.toOSString(), "errorlog");
		return erroLogPath.toFile();
	}

	private String getStatusType(int st) {
		switch (st) {
		case IStatus.INFO: {
			return "INFO";
		}
		case IStatus.WARNING: {
			return "WARNING";
		}
		case IStatus.ERROR: {
			return "ERROR";
		}
		}
		return "";
	}

	private void appendUsingFileWriter(File file, String text) {
		FileWriter fr = null;
		try {
			fr = new FileWriter(file, true);
			fr.write(text);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
