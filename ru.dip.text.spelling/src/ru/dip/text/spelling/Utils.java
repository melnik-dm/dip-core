package ru.dip.text.spelling;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;

public class Utils {
	
	public static boolean isWindows = false;
	
	public static Path getPathFromPlugin(Plugin plugin, String path) throws IOException {
		URL url = plugin.getBundle().getResource(path);
		if (url == null) {
			return null;
		}
		String source = FileLocator.toFileURL(url).getPath();
		source = checkWindowsPath(source);
		return Paths.get(source).normalize();
	}
	
	static {
		String os = System.getProperty("os.name");
		if (os != null && os.startsWith("Windows")){
			isWindows = true;
		}
	}
	
	public static String checkWindowsPath(String path) {
		if (isWindows && path.startsWith("/")) {
			return path.substring(1);
		}
		return path;
	}
	
	public static Path metadataPluginPath(Plugin plugin, String folder) {
		Bundle bundle = plugin.getBundle();
		IPath pathMetaData = Platform.getStateLocation(bundle);
		String metaData = pathMetaData.toOSString();
		if (folder == null) {
			return Paths.get(metaData);
		} else {
			return Paths.get(metaData, folder);
		}
	}
	
	public static void copyFolder(Path source, Path destination) throws IOException {
		if (!Files.exists(destination)) {
			Files.copy(source, destination);
		}
		Files.list(source).forEach((path) -> {
			Path name = path.getFileName();
			Path dest = destination.resolve(name);
			if (Files.isDirectory(path)) {
				try {
					copyFolder(path, dest);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void unzip(Path zip, Path targetDir) throws IOException {
		InputStream input = Files.newInputStream(zip);
		unzip(input,targetDir);
	}
	
	private static void unzip(InputStream is, Path targetDir) throws IOException {
	    targetDir = targetDir.toAbsolutePath();
	    try (ZipInputStream zipIn = new ZipInputStream(is)) {
	        for (ZipEntry ze; (ze = zipIn.getNextEntry()) != null; ) {
	            Path resolvedPath = targetDir.resolve(ze.getName()).normalize();
	            if (!resolvedPath.startsWith(targetDir)) {
	                throw new RuntimeException("Entry with an illegal path: " 
	                        + ze.getName());
	            }
	            if (ze.isDirectory()) {
	                Files.createDirectories(resolvedPath);
	            } else {
	                Files.createDirectories(resolvedPath.getParent());
	                Files.copy(zipIn, resolvedPath);
	            }
	        }
	    }
	}

}
