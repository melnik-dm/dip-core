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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import ru.dip.core.exception.ExternalCommandException;

public class ExternalProcessUtilities {

	private static long TIMEOUT = 180;
	
	public static String runCommand(String[] command, File workDirectory) throws ExternalCommandException {
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(workDirectory);
		return runProcess(builder, command);
	}
	
	private  static String runProcess(ProcessBuilder builder, String[] command) throws ExternalCommandException  {
		try {
			Process process = builder.start();
            Gobbler outGobbler = new Gobbler(process.getInputStream());
            Thread outThread = new Thread(outGobbler);
            outThread.start();
        		
			try {
			    outThread.join();
				@SuppressWarnings("unused")
				boolean result = process.waitFor(TIMEOUT, TimeUnit.SECONDS);
				int n = process.exitValue();
				if (n != 0) {

					String errorMessage = ExternalProcessUtilities.getErrorMessage(process);					
					if (errorMessage == null || errorMessage.isEmpty()) {
						errorMessage = outGobbler.getOutput();
					}				
					String commandStr = String.join(" ", command);
					ExternalCommandException commandException = new ExternalCommandException(commandStr, errorMessage);
					throw commandException;
				}
				String output = outGobbler.getOutput();		
				process.destroy();
				if (process.isAlive()) {
				    process.destroyForcibly();
				}
				
				return output;
			} catch (InterruptedException e) {
				String errorMessage = e.getMessage();				
				String commandStr = String.join(" ", command);
				ExternalCommandException commandException = new ExternalCommandException(commandStr, errorMessage);
				throw commandException;

			}
		} catch (IOException e) {
			String commandStr = String.join(" ", command);
			String errorMessage = e.getMessage();
			ExternalCommandException commandException = new ExternalCommandException(commandStr, errorMessage);
			throw commandException;
		}
	}
	
	public static String getErrorMessage(Process process){
		InputStream stream = process.getErrorStream();
		if (stream != null){
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))){
				StringBuilder builder = new StringBuilder();
				String str = reader.readLine();
				while(str != null){
					builder.append(str);
					builder.append("\n");
					str = reader.readLine();				
				}
				return builder.toString();
			} catch (IOException e) {
				return null;
			} 
		} 
		return null;
	}
	

    public static class Gobbler implements Runnable {
        private BufferedReader reader;
        private StringBuilder builder = new StringBuilder();

        public Gobbler(InputStream inputStream) {
            this.reader = new BufferedReader(new InputStreamReader(inputStream));
        }

        public void run() {
            String line;
            builder = new StringBuilder();
            try {
                while((line = this.reader.readLine()) != null) {
                	builder.append(line);
                	builder.append("\n");
                }
                this.reader.close();
            }
            catch (IOException e) {
                System.err.println("ERROR: " + e.getMessage());
            }
        }

        public String getOutput() {
        	return builder.toString();
        }
    }
	
	public static String getOutputMessage(Process process){
		InputStream stream = process.getInputStream();
		if (stream != null){
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))){
				StringBuilder builder = new StringBuilder();
				String str = reader.readLine();
				while(str != null){
					builder.append(str);
					builder.append("\n");
					str = reader.readLine();				
				}
				return builder.toString();
			} catch (IOException e) {
				return null;
			} 
		} 
		return null;
	}

}
