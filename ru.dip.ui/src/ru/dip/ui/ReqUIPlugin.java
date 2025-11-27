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
package ru.dip.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ru.dip.core.model.DipContainer;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.ui.action.duplicate.DuplicateDialog;
import ru.dip.ui.action.duplicate.ServiceResDuplicateDialog;
import ru.dip.ui.glossary.GlossaryHover;
import ru.dip.ui.glossary.GlossaryWorkbenchListener;
import ru.dip.ui.imageview.DipImageWorkbenchListener;
import ru.dip.ui.imageview.ImageViewPreferences;
import ru.dip.ui.markview.MarkViewPartListener;
import ru.dip.ui.preferences.MdPreferences;
import ru.dip.ui.preferences.OtherPreferences;
import ru.dip.ui.preferences.ReqEditorSettings;
import ru.dip.ui.table.editor.DipTableEditor;
import ru.dip.ui.table.table.TableSettings;
import ru.dip.ui.toc.DipTocWorkbenchListener;
import ru.dip.ui.utilities.GlobalKeyListener;
import ru.dip.ui.variable.VarHover;
import ru.dip.ui.variable.view.VariableWorkbenchListener;

/**
 * The activator class controls the plug-in life cycle
 */
public class ReqUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ru.dip.ui"; //$NON-NLS-1$
	// preferences
	public static final String MARKDOWN_MAX_LINE_WIDTH = "_markdown_max_line_width";
	public static final int DEFAULT_MD_MAX_LINE_WIDTH = 80;
	public static final String PROJECT_TD_REPO = "_project_td_repo";
	public static final String LAST_EXTENSION = "_last_extension";
	
	// The shared instance
	private static ReqUIPlugin plugin;
	
	private boolean fShowReservedObjects = false;
	private Set<IProject> fCheckedDuplicatesProjects = new HashSet<> ();
	
	/**
	 * The constructor
	 */
	public ReqUIPlugin() {
	}

	public void start(BundleContext context) throws Exception {		
		super.start(context);
		plugin = this;
		if (!PlatformUI.isWorkbenchRunning()) {
			return;
		}
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				addWorkBenchListener();
				GlobalKeyListener.instance().addListeners();
				checkDuplicateNames();
			}
		});
	}

	private void addWorkBenchListener() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService()
				.addSelectionListener(GlossaryHover.getInstance());
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService()
				.addSelectionListener(VarHover.getInstance());
		
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.addPartListener(new DipTocWorkbenchListener());
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.addPartListener(new DipImageWorkbenchListener());
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.addPartListener(new GlossaryWorkbenchListener());
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
			.addPartListener(new VariableWorkbenchListener());		
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.addPartListener(new MarkViewPartListener());
	}
	
	//===========================
	// check duplicate names (перенести в отдельный класс)

	private void checkDuplicateNames() {		
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPartListener(new IPartListener2() {
			
			@Override
			public void partVisible(IWorkbenchPartReference partRef) {
				if (DipTableEditor.EDITOR_ID.equals(partRef.getId())) {
					DipTableEditor editor = (DipTableEditor) partRef.getPart(false);
					DipProject dipProject = editor.getDipProject();
					checkProjectDuplicateNames(dipProject);
				}
			}
		});
	}
		
	private void checkProjectDuplicateNames(DipProject dipProject) {
		if (!fCheckedDuplicatesProjects.contains(dipProject.getProject())) {
			checkDuplicateNames(dipProject);
			fCheckedDuplicatesProjects.add(dipProject.getProject());
		}
	}

	private void checkDuplicateNames(DipContainer container) {		
		IContainer resource = container.resource();	
		try {
			IResource[] resources = resource.members();
			String[] names = ResourcesUtilities.getChildrenNames(resource); 	
			checkDuplicateNames(container, resources, names);
			checkChildren(container);			
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	private void checkDuplicateNames(DipContainer container, 
			IResource[] resources, String[] names) {
		for(int i = 0; i < names.length; i++){
		    for(int j = i + 1; j < names.length; j++){
		        if (names[i].equals(names[j])){			        	
		        	openDuplicateDialog(container, resources[i], resources[j]);
		        }
		    }
		} 
	}
	
	private void openDuplicateDialog(DipContainer container, IResource first, IResource second) {
    	// если совпадение со служебным ресурсом
    	if (DipUtilities.isServiceResource(first)) {
    		openDuplicateServResDialog(container, second);
    		return;
    	} 
    	if (DipUtilities.isServiceResource(second)) {
    		openDuplicateServResDialog(container, first);
    		return;
    	} 
    	
    	Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    	DuplicateDialog dialog = new DuplicateDialog(
    			shell, 
    			container, 
    			first, 
    			second);
    	if (dialog.open() != Dialog.OK) {
    		dialog.forceRename();
    	} 
	}
	
	private void openDuplicateServResDialog(DipContainer container, IResource resource) {
    	Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    	ServiceResDuplicateDialog dialog = new ServiceResDuplicateDialog(
    			shell, 
    			container, 
    			resource);
    	if (dialog.open() != Dialog.OK) {
    		dialog.forceRename();
    	} 
	}
	
	private void checkChildren(DipContainer container) {
		if (container.hasChildren()) {
			for (IDipElement element: container.getChildren()) {
				if (element instanceof DipContainer) {
					checkDuplicateNames((DipContainer) element);
				}
			}
		}
	}
	
	//=============================
	// preferences
	
	@Override
	protected void initializeDefaultPluginPreferences() {		
		getPreferenceStore().setDefault(MARKDOWN_MAX_LINE_WIDTH, DEFAULT_MD_MAX_LINE_WIDTH);
		getPreferenceStore().setDefault(LAST_EXTENSION, "");
		ImageViewPreferences.setDefaultValues(getPreferenceStore());
		TableSettings.setDefaultValues();		
		TableSettings.instance(); 
		MdPreferences.setDefaultValues(getPreferenceStore());
		MdPreferences.instance().setPreferences();
		ReqEditorSettings.setDefaultValues(getPreferenceStore());
		OtherPreferences.setDefaultValues(getPreferenceStore());
		OtherPreferences.init();
	}

	public static int getMarkdownMaxLine() {
		return plugin.getPreferenceStore().getInt(MARKDOWN_MAX_LINE_WIDTH);
	}
	
	public static void setMarkdownMaxLine(int newValue) {
		plugin.getPreferenceStore().setValue(MARKDOWN_MAX_LINE_WIDTH, newValue);
	}
	
	public static String getLastExtension() {
		return plugin.getPreferenceStore().getString(LAST_EXTENSION);
	}
	
	public static void setLastExtension(String value) {
		plugin.getPreferenceStore().setValue(LAST_EXTENSION, value);
	}
		
	//====================
	
	public void stop(BundleContext context) throws Exception {
		TableSettings.instance().disposeResources();
		MdPreferences.instance().disposeResources();
		plugin = null;
		super.stop(context);
	}

	//====================
	// getters & setters
	
	public static ReqUIPlugin getDefault() {
		return plugin;
	}
	
	public boolean isShowReservedObjects(){
		return fShowReservedObjects; 
	}
	
	public void setShowReservedObjects(boolean newValue){
		fShowReservedObjects = newValue;
	}
	
}
