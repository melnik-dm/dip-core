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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IFile;

public class FileUtilities {
	
	public static String getNameWithoutExtension(IFile file){
		String extension = file.getFileExtension();
		String filename = file.getName();
		if (extension != null){
			return filename.substring(0, filename.length() - extension.length() - 1);
		}
		return filename;		
	}
	
	public static String getFileExtension(String fileName){
		int dotLastIndex = fileName.lastIndexOf('.');
		if (dotLastIndex > 0) {
		    return fileName.substring(dotLastIndex + 1);
		}
		return null;
	}
	
	public static String getFileExtension(Path path){
		return getFileExtension(path.toString());
	}
	
	public static String readFile(IFile file) throws IOException {
		if (!file.exists()) {
			return null;
		}
		Path path = Paths.get(file.getLocationURI());
		return readFile(path);
	}
	
	public static List<String> readLines(IFile file) throws IOException {
		if (!file.exists()) {
			return null;
		}
		Path path = Paths.get(file.getLocationURI());
		return Files.readAllLines(path, StandardCharsets.UTF_8);
	}
	
	/**
	 * Читает файл, в случае ошибки возвращает defaultString
	 */
	public static String readFile(IFile file, String defaultString) {
		Path path = Paths.get(file.getLocationURI());
		try {
			return readFile(path);
		} catch (IOException e) {
			e.printStackTrace();
			return defaultString;
		}
	}
	
	public static String readFile(Path path) throws IOException {
		List<String> strings = Files.readAllLines(path, StandardCharsets.UTF_8);
		StringBuilder builder = new StringBuilder();
		for (String str : strings) {
			builder.append(str);
			builder.append(TagStringUtilities.lineSeparator());
		}
		return builder.toString().trim();
	}
	
	/**
	 * Читает файл, в случае ошибки возвращает defaultString
	 */
	public static String readFile(Path path, String defaultStrng){
		try {			
			return readFile(path);
		} catch (IOException e) {
			e.printStackTrace();
			return defaultStrng;
		}
	}
	
	
	public static void writeFile(IFile file, String content) throws IOException{
		Path path = Paths.get(file.getLocationURI());
		writeFile(path, content);
	}
	
	public static void writeFile(Path path, String content) throws IOException{
		String[] lines = content.split("\n");
		ArrayList<String> list = new ArrayList<>();
		Collections.addAll(list, lines);
		Files.write(path, list, StandardCharsets.UTF_8);
	}
	
	//==========================================
	// Comparators
	
	public static Comparator<Path> getFirstFilesComparator(){
		return new Comparator<Path>() {

			@Override
			public int compare(Path path1, Path path2) {
				
				int n1 = 0;
				int n2 = 0;
				
				if (Files.isDirectory(path1)) {
					n1 = 1;									
				}
				if (Files.isDirectory(path2)) {
					n2 = 1;
				}
				if (n1 != n2) {
					return n1 - n2;
				}
				return path1.compareTo(path2);
			}
		};
	}

	//=================================
	// check sum
	
	public static boolean equalsFilesByCheckSum(Path path1, Path path2) {
		String hash1 = getHash(path1);
		String hash2 = getHash(path2);
		return Objects.equals(hash1, hash2);
	}
	
	private static String getHash(Path path) {
		try {
			byte[] b = Files.readAllBytes(path);
			byte[] hash = MessageDigest.getInstance("MD5").digest(b);
			return new String(hash, StandardCharsets.UTF_8);
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

}
