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
package ru.dip.table.editor.table;

import static ru.dip.table.editor.table.HtmlContent.DIRTY;
import static ru.dip.table.editor.table.HtmlContent.HTML_CODE;
import static ru.dip.table.editor.table.HtmlContent.INIT;
import static ru.dip.table.editor.table.HtmlContent.INIT_SCRIPT;
import static ru.dip.table.editor.table.HtmlContent.SAVE;
import static ru.dip.table.editor.table.HtmlContent.scriptPath;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.table.DipTablePlugin;
import ru.dip.table.editor.MultiPageTableEditor;

public class TablePage extends FormPage {
	
	public static String htmlPath = null;

	private MultiPageTableEditor fEditor;
	private String fContent;
	private Browser fBrowser;
	private boolean fStart = false;

	public TablePage(MultiPageTableEditor editor, String id, String title) {
		super(editor, id, title);
		fEditor = editor;
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		Composite composite = managedForm.getForm().getBody();
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));	
		createBrowser(composite);
	}
	
	private void createBrowser(Composite parent) {

		if (ResourcesUtilities.isWindows) {
			System.setProperty("org.eclipse.swt.browser.DefaultType", "edge");
		}
		fBrowser = new Browser(parent, SWT.NONE);
		fBrowser.setJavascriptEnabled(true);
		fBrowser.setUrl(getHtmlPath());
		fBrowser.setLayoutData(new GridData(GridData.FILL_BOTH));
		new BrowserCallbackFunction(fBrowser, "callBack");
		LocationListener listener = new LocationListener() {

			@Override
			public void changing(LocationEvent event) {
			}

			@Override
			public void changed(LocationEvent event) {
				startBrowser();
			}
		};

		fBrowser.addLocationListener(listener);
	}
		
	public static String getHtmlPath() {
		if (htmlPath == null || htmlPath.isEmpty()) {
			Path path = htmlPath();
			createHtmlPage(path);
			try {
				htmlPath = path.toUri().toURL().toString();
			} catch (MalformedURLException e) {
				htmlPath = "";
				e.printStackTrace();
			}
		} 
		return htmlPath;	
	}
	
	public static void createHtmlPage(Path htmlPath) {
		try {
			String scriptPath = ResourcesUtilities.getPathFromPlugin(DipTablePlugin.getDefault(), "js").toString();
			scriptPath = Paths.get(scriptPath, "tinymce.min.js").toUri().toURL().toString();
			String content = scriptPath(scriptPath) + INIT_SCRIPT + HTML_CODE;
			FileUtilities.writeFile(htmlPath, content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static  Path htmlPath() {
		return ResourcesUtilities.metadataPluginPath(DipCorePlugin.getDefault(), "table.html");
		
	}
	
	public class BrowserCallbackFunction extends BrowserFunction {

		public BrowserCallbackFunction(Browser browser, String name) {
			super(browser, name);
		}

		public BrowserCallbackFunction(Browser fBrowser, String string, boolean b, String[] frames) {
			super(fBrowser, string, b, frames);
		}

		public Object function(Object[] args) {
			if (args != null && args.length > 0) {
				String callback = (String) args[0];
				if (DIRTY.equals(callback)) {
					fEditor.setDirty();
				} else if (SAVE.equals(callback)) {
					fContent = (String) args[1];
				} else if (INIT.equals(callback)) {
					setContent();
				} else if ("SAVED".equals(callback)) {
					// ctrl+s
					fContent = (String) args[1];
					fEditor.textEditor().updateContent(fContent);
					fEditor.textEditor().doSave(null);
					fEditor.setDirty(false);
					fEditor.fireDirtyPropertyChange();
				}	
			}
			return "1";
		}
	}
		
	public void startBrowser() {
		if (!fStart) {
			fStart = true;
			updateBrowser();
		} 
	}
	
	public void updateBrowser() {
		new BrowserCallbackFunction(fBrowser, "callBack");
		setContent();
	}
	
	private void setContent() {
		fBrowser.execute("setcontent('" + fEditor.content() + "')");
	}
	
	private void updateContent() {
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				fBrowser.evaluate("dipsave()\n");
			}
		});
	}
	
	public String getContent() {
		updateContent();
		return fContent;
	}
}
