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
package ru.dip.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import ru.dip.core.manager.DipNatureManager;
import ru.dip.core.manager.DipProjectResourceCreator;
import ru.dip.core.model.glossary.ProjectGlossaryFolder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.ISchemaContainer;
import ru.dip.core.model.properties.DipProjectProperties;
import ru.dip.core.model.reports.IMainReportContainer;
import ru.dip.core.model.reports.IReportContainer;
import ru.dip.core.model.reports.MainReportContainer;
import ru.dip.core.model.reports.ProjectReportFolder;
import ru.dip.core.model.reports.Report;
import ru.dip.core.model.vars.ProjectVarContainer;
import ru.dip.core.schema.Schema;
import ru.dip.core.unit.UnitType;
import ru.dip.core.utilities.DipTableUtilities;
import ru.dip.core.utilities.GITUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;

public class DipProject extends DipTableContainer implements IDipParent, IProjectNature, ISchemaContainer {

	public static final String DIP_START_FILE_NAME = "project.dip";
	public static final String DIP_START_PATH = "project_content" + TagStringUtilities.PATH_SEPARATOR +
			"dip_start" +  TagStringUtilities.PATH_SEPARATOR+ DIP_START_FILE_NAME;
	
	public static interface ProjectImagesListener {
		void imagesChanged();		
	}
	
	public static interface ProjectTablesListener {
		void tablesChanged();		
	}
	
	private DipSchemaFolder fSchemaFolder;
	private IMainReportContainer fMainReportFolder;

	private ProjectGlossaryFolder fGlossFolder;
	private ProjectVarContainer fVariablesContainer;
	private DipProjectSchemaModel fSchemaModel;
	private List<IncludeFolder> fIncludeFolders = new ArrayList<>();
	private List<DipUnit> fTables = new ArrayList<>();
	private List<DipUnit> fImages = new ArrayList<>();
	private HashMap<String, ArrayList<DipUnit>> fDipNumbers = new HashMap<>();
	private List<ProjectImagesListener> fImagesListeners = new ArrayList<>();
	private List<ProjectTablesListener> fTablesListeners = new ArrayList<>();
	
	private DipProjectProperties fProjectProperties = new DipProjectProperties(this);
	private Repository fGitRepo;
	
	public static DipProject instance(IProject container) {
		IDipElement element = DipRoot.getInstance().getElement(container, null, DipElementType.RPOJECT);
		if (element == null) {
			DipProject project = new DipProject(container);
			DipRoot.getInstance().putElement(project);
			return project;
		} else {
			return (DipProject) element;
		}	
	}
	
	/**
	 * Нужен
	 * Иначе ошибка при имопрте, не добавляет nature
	 */
	public DipProject() {
		super(null, null);
	}
	
	private DipProject(IProject project) {
		super(project, null);
		DipProjectResourceCreator.checkDipFile(this);
		fGitRepo = GITUtilities.findRepo(project);
	}
	
	@Override
	public void refresh() {
		super.refresh();
		DipTableUtilities.saveModel(this);
	}
		
	public String projectName() {
		return resource().getLocation().lastSegment();		
	}
	
	public String decorateName() {
		String name = name();
		String locationName = projectName();
		if (name.equals(locationName)) {
			return name;
		} else {
			StringBuilder builder = new StringBuilder();
			builder.append(name);
			builder.append(" (");
			builder.append(locationName);
			builder.append(")");
			return builder.toString();
		}
	}
	
	//=======================
	// compute children model
	
	public void computeChildren(){
		clearChildren();
		try {
			for (IResource resource: resource().members()){
				DipElementType type = DipRoot.getType(resource);
				switch (type){
				case SCHEMA_FOLDER:{
					createSchemaFolder((IFolder) resource);
					break;
				}
				case INCLUDE_FOLDER:{
					createIncludeFolder((IFolder)resource);
					break;
				}				
				case REPORT_FOLDER:{
					createReportFolder((IFolder) resource);
					break;
				}
				case REPORT:{
					createReport((IFile) resource);
					break;
				}				
				case RESERVED_FOLDER:{
					createReservedFolder((IFolder) resource);
					break;
				}
				case FOLDER:{
					createFolder((IFolder) resource);
					break;
				}
				case SERV_FOLDER:{
					//createServFolder((IFolder) resource);
					break;
				}
				case BROKEN_FOLDER:{
					break;
				}
				case RESERVED_MARKER:{
					createReservedMarker((IFile) resource);
					break;
				}
				case RESERVED_UNIT:{
					createReservedUnit((IFile) resource);
					break;
				}				
				case UNIT:{
					createUnit((IFile) resource);
					break;
				}
				case GLOSS_REF:{
					createGlossRef((IFile) resource);
					break;
				}		
				case TOC_REF:{
					createTocRef((IFile) resource);
					break;
				}	
				case CHANGE_LOG:{
					createChangeLog((IFile) resource);
					break;
				}								
				case COMMENT:{
					createDipComment((IFile) resource);
					break;
				}	
				case DESCRIPTION:{
					createDipDescription((IFile) resource);
					break;
				}
				case FOLDER_COMMENT:{
					createFolderDipComment((IFile) resource);
					break;
				}
				case GLOSSARY_FOLDER:{
					createMainGlossaryFolder((IFile) resource);
					break;
				}
				case VARIABLES_CONTAINER:{
					createMainVariablesContainer((IFile) resource);
					break;
				}
				case EXPORT_CONFIG:{
					createExportConfig((IFile) resource);
					break;
				}
				default:
					break;			
				}

			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		createTable();
		setDipDocElementsChildren(computeDipChildren());

		if (fGlossFolder == null){
			Shell shell =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			createNewGlossaryFolder(shell);
		}
		if (fVariablesContainer == null){
			Shell shell =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			createNewVariablesContainer(shell);
		}
		
		// если есть папка Reports (объект ProjectReportFolder) - то ее обработать
		if (fMainReportFolder instanceof ProjectReportFolder) {
			((ProjectReportFolder) fMainReportFolder).computeChildren();		
		}
	}
	
	private void clearChildren() {
		fSchemaFolder = null;
		fGlossFolder = null;
		fMainReportFolder = null;
		fChildren = new ArrayList<>();
		setDipDocElementsChildren(new ArrayList<>());
		fIncludeFolders = new ArrayList<>();
	}
		
	//=======================
	
	@Override
 	public IDipParent parent() {
		return null;
	}
		
	@Override
	public DipProject dipProject() {
		return this;
	}
	
	@Override
	public IDipElement createNewUnit(IFile file) {
		if (ExportConfig.EXTENSION.equals(file.getFileExtension())){
			return createExportConfig(file);
		} else {		
			return super.createNewUnit(file);
		}
	}
	
	public String[] getPossibleExtensions() {
		ArrayList<String > extensions = new ArrayList<>();
		extensions.addAll(getSchemaModel().getAllExtensions());
		Collections.addAll(extensions, UnitType.EXTENSION_NEW);				
		return extensions.stream()
				.map((extension) ->  !extension.isEmpty() ? "." + extension : "")
				.toArray(String[]::new);
	}
	
	@Override
	public boolean isRoot() {
		return true;
	}
	
	//=========================
	// numbering
	
	public void updateNumeration() {
		updateTableNumbers();
		updateImageNumbers();
		updateFormNumbers();
	}
	
	@Override
	public void updateTableNumbers(){
		fTables = new ArrayList<>();
		updateTableNumbers(this);
		fireTablesListeners();
	}
		
	private void updateTableNumbers(IDipParent parent){		
		for (IDipDocumentElement dipDocumentElement : parent.getDipDocChildrenList()) {
			if (dipDocumentElement instanceof DipTableContainer) {
				DipTableContainer dipParent = (DipTableContainer) dipDocumentElement;
				if (dipParent instanceof Appendix) {
					dipParent.updateTableNumbers();
				} else {		
					updateTableNumbers(dipParent);
				}
			} else if (dipDocumentElement instanceof DipUnit) {
				DipUnit unit = (DipUnit) dipDocumentElement;
				if ((unit.getUnitType().isTableDescription())) {
					if (unit.isDisabledInDocument()) {
						unit.setNumber("X");
					} else {
						int nextNumber = fTables.size() + 1;
						unit.setNumber(String.valueOf(nextNumber));
						fTables.add(unit);
					}
				}
			}			
		}
	}
	
	// костыль
	//  не обновлять слушатели (ImageView в частности)
	boolean fUpdateImageListeners = true;
	
	public void setUpdateImageListeners(boolean updateImageListeners) {
		fUpdateImageListeners = updateImageListeners;
	}
	
	@Override
	public void updateImageNumbers() {
		fImages = new ArrayList<>();
		updateImageNumbers(this);
		if (fUpdateImageListeners) {
			fireImagesListeners();
		}
	}
	
	private void updateImageNumbers(IDipParent parent){		
		for (IDipDocumentElement dipDocumentElement : parent.getDipDocChildrenList()) {
			if (dipDocumentElement instanceof DipTableContainer) {
				DipTableContainer dipParent = (DipTableContainer) dipDocumentElement;
				if (dipParent instanceof Appendix) {
					dipParent.updateImageNumbers();									
				} else {
					updateImageNumbers(dipParent);
				}
			} else if (dipDocumentElement instanceof DipUnit) {
				DipUnit unit = (DipUnit) dipDocumentElement;
				if (unit.getUnitType().isImageType()) {
					if (unit.isDisabledInDocument()) {
						unit.setNumber("X");
					} else {
						int nextNumber = fImages.size() + 1;
						unit.setNumber(String.valueOf(nextNumber));
						fImages.add(unit);
					}
				}
			}			
		}
	}
	
	private void updateFormNumbers(){
		fDipNumbers = new HashMap<>();
		clearFormNumbers();
		updateFormNumbers(this);
	}
	
	private void clearFormNumbers() {
		fDipNumbers = new HashMap<>();
		getSchemaModel().getAllExtensions().forEach((ext) -> fDipNumbers.put(ext, new ArrayList<>()));
	}
	
	private void updateFormNumbers(IDipParent parent){		
		for (IDipDocumentElement dipDocumentElement : parent.getDipDocChildrenList()) {
			if (dipDocumentElement instanceof IDipParent) {
				updateFormNumbers((IDipParent) dipDocumentElement);
			} else if (dipDocumentElement instanceof DipUnit) {
				DipUnit unit = (DipUnit) dipDocumentElement;
				if ((unit.getUnitType().isForm())) {
					String extension = unit.resource().getFileExtension();
					List<DipUnit> numbers = fDipNumbers.get(extension);
					if (numbers != null) {		
						if (unit.isDisabledInDocument()) {
							unit.setNumber("X");
						} else {						
							int nextNumber = numbers.size() + 1;
							unit.setNumber(String.valueOf(nextNumber));
							numbers.add(unit);
						}
					}
				}
			}			
		}
	}	
	
	//===============================
	// images-tables listeners
	
	public void addImagesListener(ProjectImagesListener listener) {
		fImagesListeners.add(listener);
	}
	
	public void removeImagesListener(ProjectImagesListener listener) {
		fImagesListeners.remove(listener);
	}
	
	private void fireImagesListeners() {
		for (int i = fImagesListeners.size() - 1; i >= 0; i--) {
			ProjectImagesListener listener = fImagesListeners.get(i);
			if (listener != null) {
				listener.imagesChanged();
			}
		}
	}
	
	public void addTablesListener(ProjectTablesListener listener) {
		fTablesListeners.add(listener);
	}
	
	public void removeTablesListener(ProjectTablesListener listener) {
		fTablesListeners.remove(listener);
	}
	
	private void fireTablesListeners() {
		for (int i = fTablesListeners.size() - 1; i >= 0; i--) {
			ProjectTablesListener listener = fTablesListeners.get(i);
			if (listener != null) {
				listener.tablesChanged();
			}
		}
	}
	
	//===============================
	// schema
	
	private void createSchemaFolder(IFolder folder){
		fSchemaFolder = DipSchemaFolder.instance(folder, this);
		fChildren.add(0, fSchemaFolder);	
	}
	
	public DipProjectSchemaModel getSchemaModel(){
		if (fSchemaModel == null){
			fSchemaModel = createtSchemaModel();
		}
		return fSchemaModel;
	}
	
	public DipProjectSchemaModel createtSchemaModel(){
		if (fSchemaFolder == null){
			getChildren();
		}
		if (fSchemaFolder == null){
			Shell shell =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			IFolder folder = DipProjectResourceCreator.createDefaultSchemaFolder(shell, this);
			createSchemaFolder(folder);
		}
		return new DipProjectSchemaModel(fSchemaFolder);
	}
	
	@Override
	public boolean containsSchema(String extension) {
		DipProjectSchemaModel schemaModel = getSchemaModel();
		return extension != null && schemaModel.containsFileExtension(extension);
	}
	
	@Override
	public Schema getSchema(String extension) {
		return getSchemaModel().getSchema(extension);
	}
	
	//==============================
	// report
	
	private void createReportFolder(IFolder folder){
		fMainReportFolder = ProjectReportFolder.instance(folder, this);
		if (fSchemaFolder != null && fChildren.size() > 0){
			fChildren.add(1, fMainReportFolder);
		} else {
			fChildren.add(0, fMainReportFolder);
		}
	}
	
	private void createReport(IFile file) {
		IReportContainer container = getOrCreateReportContainer();
		container.addReportChild(file);
	}
	
	/**
	 * Возвращает контейнер для отчетов (создает новый при необходимости)
	 * Если есть физическая папка Reports в корне проекта (как в старых версиях), 
	 * то контейнер создается на ее базе (для обратной совместимости)  (ProjectReportFolder)
	 * Если нет, то папку будет только виртуальная (MainReportContainer)
	 */
	public IMainReportContainer getOrCreateReportContainer() {
		if (fMainReportFolder != null) {
			return fMainReportFolder;
		}
		fMainReportFolder = new MainReportContainer(this);
		fChildren.add(0, fMainReportFolder);		
		return fMainReportFolder;
	}
	
	public IMainReportContainer getReportFolder(){
		return fMainReportFolder;
	}
	
	public Report getReport(String reportName){
		if (fMainReportFolder == null){
			return null;
		}
		for (IDipElement report: fMainReportFolder.getChildren()){
			if (report instanceof Report && report.name().equals(reportName)){
				return (Report) report;
			}
		}
		return null;
	}
		
	@Override
	public IReportContainer getReportContainer() {
		throw new RuntimeException("Operation not support");
	}

	//============================
	// glossary
	
	private void createMainGlossaryFolder(IFile file){
		fGlossFolder =  new ProjectGlossaryFolder(file, this);
		addGlossFodlerToChildren();
	}
	
	private void addGlossFodlerToChildren(){
		int index = 0;
		if (fSchemaFolder != null){
			index ++;
		}
		if (fMainReportFolder != null){
			index ++;
		}
		if (fChildren.size() > index - 1){
			fChildren.add(index, fGlossFolder);
		} 
	}
		
	private void createNewGlossaryFolder(Shell shell){
		IFile file = DipProjectResourceCreator.createGlossaryFile(shell, this);
		if (file != null){
			fGlossFolder = new ProjectGlossaryFolder(file, this);
			if (fGlossFolder != null){
				addGlossFodlerToChildren();
			}
			ResourcesUtilities.updateDipElement(this);
			WorkbenchUtitlities.updateProjectExplorer();			
		} 
	}
	
	public ProjectGlossaryFolder getGlossaryFolder(){
		return fGlossFolder;
	}
	
	//===========================
	// vars
	
	private void createMainVariablesContainer(IFile file){		
		fVariablesContainer = new ProjectVarContainer(file, this);
		addVariblesContainerToChildren();
	}
	
	private void addVariblesContainerToChildren(){
		int index = 0;
		if (fSchemaFolder != null){
			index ++;
		}
		if (fMainReportFolder != null){
			index ++;
		}
		if (fGlossFolder != null){
			index ++;
		}
		if (fChildren.size() > index - 1){
			fChildren.add(index, fVariablesContainer);
		} 
	}
		
	private void createNewVariablesContainer(Shell shell){
		IFile file = DipProjectResourceCreator.createVariablesFile(shell, this);
		if (file != null){
			fVariablesContainer = new ProjectVarContainer(file, this);
			if (fVariablesContainer != null){
				addVariblesContainerToChildren();
			}
			ResourcesUtilities.updateDipElement(this);
			WorkbenchUtitlities.updateProjectExplorer();			
		} 
	}
	
	public ProjectVarContainer getVariablesContainer(){
		return fVariablesContainer;
	}
	
	@Override
	public void deleteVarContainer() {
		throw new RuntimeException("Operation not support");
	}
	
	//============================
	// include
	
	public void addIncludeFolder(IncludeFolder includeFolder) {
		fIncludeFolders.add(includeFolder);
	}
	
	public void removeIncludeFolder(IncludeFolder includeFolder) {
		fIncludeFolders.remove(includeFolder);
	}
	
	public boolean containtsIncludeFolder(String name) {
		for (IncludeFolder include: fIncludeFolders) {
			if (name.equals(include.name())) {
				return true;
			}
		}
		return false;
	}
	
	public IncludeFolder getIncludeFolder(String name) {
		for (IncludeFolder include: fIncludeFolders) {
			if (name.equals(include.name())) {
				return include;
			}
		}
		return null;
	}
	
	//========================
	// export
	
	private IDipElement createExportConfig(IFile file){
		ExportConfig config = ExportConfig.instance(file, this);
		fChildren.add(config);
		return config;
	}
	
	public String[] getExportConfigs(){
		return fChildren.stream()
			.filter((child) -> child.type() == DipElementType.EXPORT_CONFIG)
			.map(IDipElement::name)
			.toArray(String[]::new);		
	}
	
	//==========================
	// properties

	public DipProjectProperties getProjectProperties() {
		return fProjectProperties;
	}
	
	
	//===========================
	// other
	
	@Override
	public void configure() throws CoreException {}

	@Override
	public void deconfigure() throws CoreException {}
	
	//===========================
	// getters & setters

	@Override
	public IProject getProject() {
		return resource();
	}
		
	@Override
	public void setProject(IProject project) {
	}

	@Override
	public IProject resource() {
		return (IProject) super.resource();
	}

	@Override
	public DipElementType type() {
		return DipElementType.RPOJECT;
	}
	
	public List<DipUnit> images(){
		return fImages;
	}
	
	public List<DipUnit> tables(){
		return fTables;
	}

	public List<IncludeFolder> getIncludeFolders(){
		return fIncludeFolders;
	}

	@Override
	public IDipDocumentElement strong() {
		return this;
	}
	
	public Repository getGitRepo() {
		return fGitRepo;
	}
	
}
