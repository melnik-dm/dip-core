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
package ru.dip.core.link;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkUpdater {
	
	private String fLastID;
	private String fNewID;
	private boolean fNeedUpdate = false;
	
	public LinkUpdater(String lastID, String newID) {
		fLastID = lastID;
		fNewID = newID;
	}
	
	public synchronized String updateLinks(String original, boolean checkFolder) {		
		fNeedUpdate = false;
		String result = updateSimpleLinks(original);
		if (checkFolder) {
			result = updateChildrenLinks(result);
		}
		return result;
	}
	
	private synchronized String updateSimpleLinks(String original) {
		String content = original;	
		String lastIDRregex = fLastID.replace(".", "\\.").replace("-", "\\-");	
		String regex = "\\[[^]]*\\]\\(" + lastIDRregex+"\\)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		while(matcher.find()){
			fNeedUpdate = true;
			String group = matcher.group();
			int index = group.indexOf("](");			
			StringBuilder builder = new StringBuilder();
			builder.append(group.substring(0, index + 2));
			builder.append(fNewID);
			builder.append(")");
			content = matcher.replaceFirst(builder.toString());
			matcher = pattern.matcher(content);
		}		
		return content;
	}
	
	private  synchronized String updateChildrenLinks(String original) {
		String content = original;	
		String lastIDRregex = fLastID.replace(".", "\\.").replace("-", "\\-");	
		String regex = "\\[[^]]*\\]\\(" + lastIDRregex + "/[^)]*" + "\\)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		while(matcher.find()){
			fNeedUpdate = true;
			String group = matcher.group();
			int index = group.indexOf("](");			
			StringBuilder builder = new StringBuilder();
			builder.append(group.substring(0, index + 2));
			
			String oldID = group.substring(index + 2 + fLastID.length(), group.length() - 1);												
			builder.append(fNewID);
			builder.append(oldID);			
			builder.append(")");
			content = matcher.replaceFirst(builder.toString());
			matcher = pattern.matcher(content);
		}		
		return content;
	}
	
	public synchronized boolean isNeedUpdate() {
		return fNeedUpdate;
	}

}
