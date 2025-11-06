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
package ru.dip.core.utilities;

import java.io.File;
import java.io.IOException;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.exception.DiaConvertCommandException;

public class DiaUtilities {
	
	public static File toPng(String filepath, String unitID) throws DiaConvertCommandException {
		File outputFile = null;
		try {
			outputFile = File.createTempFile(unitID, "");
		} catch (IOException e) {
			String errorMesage = e.getMessage();
			String commandStr = filepath + " for " + unitID;
			DiaConvertCommandException commandException = new DiaConvertCommandException(commandStr, errorMesage);
			throw commandException;	
		}

		String[] command = getCommand(filepath, outputFile.getAbsolutePath());
		ProcessBuilder builder = new ProcessBuilder(command);
		
		try {
			Process process = builder.start();
			int n = process.waitFor();
			if (n != 0) {				
				String errorMessage = ExternalProcessUtilities.getErrorMessage(process);
				if (errorMessage == null || errorMessage.isEmpty()){
					errorMessage = ExternalProcessUtilities.getOutputMessage(process);		
				}
				String commandStr = String.join(" ", command);
				DiaConvertCommandException commandException = new DiaConvertCommandException(commandStr, errorMessage);
				throw commandException;
			}	
			return outputFile;
		} catch (IOException | InterruptedException e) {
			String errorMesage = e.getMessage();
			String commandStr = String.join(" ", command);
			DiaConvertCommandException commandException = new DiaConvertCommandException(commandStr, errorMesage);
			throw commandException;		
		}
	}

	private static String[] getCommand(String input,  String output) {						
		String[] result = {DipCorePlugin.getDiaPath(), "-e", output, "-t", "png", input};		
		return result;
	}
	
}
