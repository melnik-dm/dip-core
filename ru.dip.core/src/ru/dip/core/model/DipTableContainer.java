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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import ru.dip.core.manager.DipTableContainerComputer;
import ru.dip.core.model.finder.FindSettings;
import ru.dip.core.model.finder.FinderIdManager;
import ru.dip.core.model.finder.IContent;
import ru.dip.core.model.finder.IFindPoints;
import ru.dip.core.model.finder.IFindedIdPoints;
import ru.dip.core.model.finder.TextFinderManager;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IFindable;
import ru.dip.core.model.interfaces.IGlossarySupport;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.table.TableWriter;
import ru.dip.core.utilities.DipTableUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.text.Terms;

public abstract class DipTableContainer extends DipContainer implements IDipParent, IFindable, IFindPoints, IFindedIdPoints, IGlossarySupport, IContent {

	private final DipTableContainerProperties fProperties;
	
	//  элементы для отображения в таблице (только DipFolder и DipUnit) в определенном порядке
	private List<IDipDocumentElement> fDipDocumentChildren;
	protected DnfoTable fTable;
	protected boolean fActiveNumeration = true;	
	protected String fFileStepNumeration = null;
	protected String fFolderStepNumeration = null;
	
	protected String fPageBreak;
	
	protected boolean fNeedUpdate = false;  // есть ли необходимость, обновить
	
	private List<DipUnit> fAppendixTableNumbers = new ArrayList<>();
	private List<DipUnit> fAppendixImageNumbers = new ArrayList<>();
	
	// find
	private final TextFinderManager fFinderManager;
	private final FinderIdManager fFinderIdManager;

	public DipTableContainer(IContainer container, IParent parent) {
		super(container, parent);
		fProperties = new DipTableContainerProperties(this);
		fFinderManager = new TextFinderManager(this);
		fFinderIdManager = new FinderIdManager(this);
	}

	protected IDipElement createTable() {
		IFile file = ((IContainer) resource()).getFile(new Path(DnfoTable.TABLE_FILE_NAME));
		if (!file.exists()) {
			java.nio.file.Path filePath = Paths.get(file.getLocation().toOSString());
			if (Files.exists(filePath)){
				// error						
				boolean exists = handleNotTableFile(file);
				if (!exists){					
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					MessageDialog.openError(shell, "Read table error", "Ошибка чтения таблицы. Просьба сообщить об этом сообщении разработчикам.");						
					return null;
				}
				
			} else {
				createNewEmptyTable(file);
			}
		}
		fTable = DnfoTable.instance(file, this);
		fChildren.add(fTable);
		return fTable;
	}

	private boolean handleNotTableFile(IFile file){
		/*Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		MessageDialog.openInformation(shell, "Debug message", "В этом месте пропадало описание директорий. "
				+ "Если вы читаете это сообщение, значит баг обнаружен. "
				+ "Сообщите пожалуйста разработчикам, что увидели это сообщение.\n"
				+ "Желательно обратить внимание, после чего появилось данное сообщение (смена Workspace, операции с файлами и т.п.)");
		*/
		ResourcesUtilities.updateProject(resource());
		WorkbenchUtitlities.updateProjectExplorer();
		return file.exists();
	}
		
	private void createNewEmptyTable(IFile file){
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		try {
			ResourcesUtilities.createFile(file, shell);
			TableWriter.saveEmptyModel(file);
		} catch (CoreException | ParserConfigurationException | IOException | TransformerException e) {
			e.printStackTrace();
		}
	}
	
	protected List<IDipDocumentElement> computeDipChildren(){
		return this.computeDipChildren(false);
	}
	
	/**
	 * Если withBrokenFolderss = true, то добавляеются директории, которых не перечислены в .dnfo
	 */
	protected List<IDipDocumentElement> computeDipChildren(boolean withBrokenFolders){
		DipTableContainerComputer computer = new DipTableContainerComputer(this);
		return computer.computeDipChildren(withBrokenFolders);
	}
	
	@Override
	public void addNewChild(IDipDocumentElement dipDocumentElement, int dipIndex) {
		fChildren.add(dipDocumentElement);
		getDipDocumentChildren().add(dipIndex, dipDocumentElement);
	}
	
	@Override
	public IDipElement createNewUnit(IFile file) {
		return createNewUnit(file, 0);
	}

	@Override
	public IDipDocumentElement createNewUnit(IFile file, int dipIndex) {
		DipUnit newUnit = DipUnit.instance(file, this);
		fChildren.add(newUnit);
		getDipDocumentChildren().add(dipIndex, newUnit);
		return newUnit;
	}

	@Override
	public IDipParent createNewFolder(IFolder folder) {
		if (DipUtilities.isServedFolder(folder)) {
			return null;
		}
		DipFolder newFolder = DipFolder.instance(folder, this); 
		fChildren.add(newFolder);
		int index = DipTableUtilities.getFirstParentIndex(this);
		getDipDocumentChildren().add(index, newFolder);		
		newFolder.computeChildren();
		return newFolder;
	}
	
	@Override
	public IDipParent createNewFolder(IFolder folder, int dipIndex) {
		if (DipUtilities.isServedFolder(folder)) {
			return null;
		}	
		DipFolder newFolder = DipFolder.instance(folder, this); 
		fChildren.add(newFolder);
		getDipDocumentChildren().add(dipIndex, newFolder);
		newFolder.computeChildren();
		return newFolder;
	}
	
	@Override
	public IDipParent includeFolder(IFolder folder, String name, String description, boolean readOnly) {
		IncludeFolder includeFolder = IncludeFolder.instance(folder, this); 
		includeFolder.setReadOnly(readOnly);
		includeFolder.setLinkName(name);
		includeFolder.setLinkDescription(description);
		fChildren.add(includeFolder);
		int index = DipTableUtilities.getFirstParentIndex(this);
		getDipDocumentChildren().add(index, includeFolder);		
		includeFolder.computeChildren();
		return includeFolder;
	}
	
	@Override
	public IDipParent includeFolder(IFolder folder, int dipIndex, String name, String description, boolean readOnly) {
		IncludeFolder includeFolder = IncludeFolder.instance(folder, this); 
		includeFolder.setReadOnly(readOnly);
		includeFolder.setLinkName(name);
		includeFolder.setLinkDescription(description);
		fChildren.add(includeFolder);
		getDipDocumentChildren().add(dipIndex, includeFolder);		
		includeFolder.computeChildren();
		return includeFolder;
	}
	
	@Override
	public void removeChild(IDipElement child) {
		if (child instanceof DipFolder) {
			((DipFolder)child).clearWhenDeleting();
		}
		super.removeChild(child);
		getDipDocumentChildren().remove(child);
	}
	
	@Override
	public IDipParent parent() {
		return (IDipParent) super.parent();
	}
		
	@Override
	public IContainer resource() {
		return (IContainer) super.resource();
	}
	
	//=================================
	// get children
	
	@Override
	public IDipDocumentElement[] getOneListChildren(){
		ArrayList<IDipDocumentElement> result = new ArrayList<>();
		for (IDipDocumentElement child: getDipDocChildrenList()){
			if (child instanceof DipTableContainer){
				result.add(child);
				Collections.addAll(result, ((IDipParent) child).getOneListChildren());
			} else if (child instanceof IDipUnit) {
				IDipUnit dipUnit = (IDipUnit) child;
				result.addAll(dipUnit.getUnionExtensions());
			}
		}
		return result.stream().toArray(IDipDocumentElement[]::new);
	}
	
	@Override
	public IDipDocumentElement[] getDipChildren() {
		ArrayList<IDipDocumentElement> extendedChildren = new ArrayList<>();		
		for (IDipDocumentElement element: getDipDocChildrenList()){
			if (element instanceof IDipParent){
				extendedChildren.add((IDipParent) element);
			} else if (element instanceof DipUnit){
				IDipUnit dipUnit = (IDipUnit) element;
				extendedChildren.addAll(dipUnit.getUnionExtensions());			
			}			
		}
		return extendedChildren.stream().toArray(IDipDocumentElement[]::new);
	}
	
	
	@Override
	public List<IDipDocumentElement> getDipDocChildrenList() {
		if (fDipDocumentChildren == null){
			getChildren();
		}
		return getDipDocumentChildren();
	}
		
	public DnfoTable getTable(){
		if (fTable == null) {		
			computeChildren();
		}
		return fTable;
	}

	
	protected List<IDipDocumentElement> getDipDocumentChildren(){ 
		checkUpdate(fDipDocumentChildren);
		return fDipDocumentChildren;
	}
	
	protected void setDipDocElementsChildren(List<IDipDocumentElement> newChildren) {
		fDipDocumentChildren = newChildren;
	}
	
	//=============================
	// update
	
	protected void updateChildren() {
		fNeedUpdate = true;
	}
	
	public void setNeedUpdate() {
		fNeedUpdate = true;
	}
	
	protected void checkUpdate(List<IDipDocumentElement> dipDocumentChildren) {};
		
	//===================================
	// numeration
	
	@Override
	public String getLocalNumber() {
		IDipParent parent = parent();
		if (parent != null){
			if (parent instanceof Appendix) {
				return getAppendixLocalNumber(parent);
			} else {
				return getStandardLocalNumber(parent);
			}
		}		
		return null;
	}
	
	private String getStandardLocalNumber(IDipParent parent) {
		if (isDisabledInDocument()){
			return "X";
		}
		
		List<IDipDocumentElement> brothers = parent.getDipDocChildrenList();
		int index = brothers.indexOf(this);
		if (index >= 0){
			int result = 0;
			for (int i = 0; i <= index; i++){
				IDipDocumentElement dipDocumentElement = brothers.get(i);
				if (dipDocumentElement instanceof IDipParent 
						&& ((IDipParent) dipDocumentElement).isActiveNumeration()
						&& !dipDocumentElement.isDisabledInDocument()){
					result++;
				}					
			}	
			return String.valueOf(result);
		}	
		return null;
	}
	
	private String getAppendixLocalNumber(IDipParent parent) {
		if (isDisabledInDocument()){
			return "X";
		}
		
		List<IDipDocumentElement> brothers = parent.getDipDocChildrenList();
		int index = brothers.indexOf(this);
		if (index >= 0){
			char result = 'А' - 1;
			for (int i = 0; i <= index; i++){
				IDipDocumentElement dipDocumentElement = brothers.get(i);
				if (dipDocumentElement instanceof IDipParent) {
					result++;
				}				
			}
			return String.valueOf(result);
		}
		return null;
	}
	
	@Override
	public boolean isActiveNumeration() {
		IDipParent parent = parent();
		if (Appendix.isAppendixPartition(this)) {
			return true;
		}
		if (parent != null && !parent.isActiveNumeration()){
			return false;
		}
		return fActiveNumeration;
	}
	
	@Override
	public void setActiveNumeration(boolean active) {
		fActiveNumeration = active;
	}
	
	@Override
	public String getParentNumber() {
		IDipParent parent = parent();
		if (parent != null && 
				parent.isActiveNumeration()
				&& parent.parent() != null){
			return parent.number();
		}		
		return null;
	}
	
	@Override
	public String number() {
		StringBuilder builder = new StringBuilder();		
		String parentNumber = getParentNumber();
		if (parentNumber != null){
			builder.append(parentNumber);
			builder.append(".");
		}
		builder.append(getLocalNumber());	
		return builder.toString();
	}
	
	//==================================
	// appendix image/table numeration  (обновление нумерации происходит по всему проекту, 
	// данные методы вызывать только для директории appendix)
	
	public void updateTableNumbers(){
		for (IDipDocumentElement dipDocumentElement: getDipDocChildrenList()) {
			if (dipDocumentElement instanceof DipTableContainer) {
				DipTableContainer appendixPartition = (DipTableContainer) dipDocumentElement;
				appendixPartition.fAppendixTableNumbers = new ArrayList<>();
				String appendixNumber = appendixPartition.number() + ".";
				appendixPartition.updateTableNumbers(appendixPartition, appendixNumber, appendixPartition);
			}						
		}
	}
	
	private void updateTableNumbers(IDipParent parent, String appendixNumber, DipTableContainer appendixPartition){		
		for (IDipDocumentElement dipDocumentElement : parent.getDipDocChildrenList()) {
			if (dipDocumentElement instanceof IDipParent) {
				IDipParent dipParent = (IDipParent) dipDocumentElement;		
				updateTableNumbers(dipParent, appendixNumber, appendixPartition);
			} else if (dipDocumentElement instanceof DipUnit) {
				DipUnit unit = (DipUnit) dipDocumentElement;
				if ((unit.getUnitType().isTableDescription())) {
					int nextNumber = appendixPartition.fAppendixTableNumbers.size() + 1;
					StringBuilder builder = new StringBuilder();
					builder.append(appendixNumber);
					builder.append(nextNumber);
					unit.setNumber(builder.toString());
					appendixPartition.fAppendixTableNumbers.add(unit);
					dipProject().tables().add(unit);
				}
			}			
		}
	}
	
	public void updateImageNumbers() {
		for (IDipDocumentElement dipDocumentElement: getDipDocChildrenList()) {
			if (dipDocumentElement instanceof DipTableContainer) {
				DipTableContainer appendixPartition = (DipTableContainer) dipDocumentElement;
				appendixPartition.fAppendixImageNumbers = new ArrayList<>();
				String appendixNumber = appendixPartition.number() + ".";
				appendixPartition.updateImageNumbers(appendixPartition, appendixNumber, appendixPartition);
			}						
		}
	}
	
	private void updateImageNumbers(IDipParent parent, String appendixNumber, DipTableContainer appendixPartition){		
		for (IDipDocumentElement dipDocumentElement : parent.getDipDocChildrenList()) {
			if (dipDocumentElement instanceof IDipParent) {
				IDipParent dipParent = (IDipParent) dipDocumentElement;		
				updateImageNumbers(dipParent, appendixNumber, appendixPartition);
			} else if (dipDocumentElement instanceof DipUnit) {
				DipUnit unit = (DipUnit) dipDocumentElement;
				if ((unit.getUnitType().isImageType())) {
					int nextNumber = appendixPartition.fAppendixImageNumbers.size() + 1;
					StringBuilder builder = new StringBuilder();
					builder.append(appendixNumber);
					builder.append(nextNumber);
					unit.setNumber(builder.toString());
					appendixPartition.fAppendixImageNumbers.add(unit);
					dipProject().images().add(unit);
				}
			}			
		}
	}
	
	//============================
	// children numeration
		
	@Override
	public boolean isFileNumeration(){
		if (fFileStepNumeration == null || fFileStepNumeration.isEmpty()){
			return false;
		}
		return true;
	}
		
	@Override
	public boolean isFolderNumeration(){
		if (fFolderStepNumeration == null || fFolderStepNumeration.isEmpty()){
			return false;
		}
		return true;	
	}
	
	@Override
	public void setFileStep(String step){
		fFileStepNumeration = step;
	}
	
	@Override
	public String getFileStep(){
		return fFileStepNumeration;
	}
	
	@Override
	public void setFolderStep(String step) {
		fFolderStepNumeration = step;	
	}
	
	@Override
	public String getFolderStep() {
		return fFolderStepNumeration;
	}
		
	//=============================
	// description
	
	@Override
	public void updateDescription(String newDescriptionContent) {
		setDescription(newDescriptionContent);
	}

	@Override
	public void removeDescription() {
		setDescription(null);
	}
	
	public String getNumberDescrition(boolean showNumeration) {
		StringBuilder builder = new StringBuilder();
		if (showNumeration && isActiveNumeration()) {
			String number = number();
			if (Appendix.isAppendixPartition(this)) {
				builder.append("Приложение ");
			}			
			builder.append(number);
			builder.append(" ");
		}
		String desc = description();
		if (desc == null || desc.isEmpty()) {
			desc = name();
		}
		builder.append(desc);
		return builder.toString();
	}
	
	//===============================
	// pagebreak
	
	@Override
	public String getPageBreak() {
		return fPageBreak;
	}
	
	@Override
	public void setPageBreak(String value) {
		fPageBreak = value;
	}
	
	//================================
	// sort
	
	public void sort() throws ParserConfigurationException, IOException {
		if (fDipDocumentChildren == null) {
			getChildren();
		}
		fDipDocumentChildren.sort(Comparator.comparing(IDipElement::id));
		TableWriter.saveModel(this);
	}
	
	public void sort(List<String> order) {
		if (fDipDocumentChildren == null) {
			getChildren();
		}
		fDipDocumentChildren.sort(Comparator.comparing(child -> order.indexOf(child.name())));
	}
	
	//=================================
	// comment
	
	@Override
	public void updateDipComment(String commentContent){		
		DipFolderComment dipComment = (DipFolderComment) comment();
		if (dipComment == null){
			if (commentContent != null && !commentContent.isEmpty()){
				setDipComment(DipFolderComment.createNewDipComment(this, commentContent));
			} 			
		} else {
			if (commentContent != null && !commentContent.isEmpty()){
				dipComment.updateCommentText(commentContent);
			} else {
				deleteDipComment();
			}
		}
	}
	
	@Override
	public void deleteDipComment(){
		DipFolderComment dipComment = (DipFolderComment) comment();
		if (dipComment != null){
			dipComment.delete();
			setDipComment(null);
		}
	}
	
	//================================
	// find
		
	@Override
	public String getContent() {
		return description();
	}
	
	@Override
	public boolean contains(String text, FindSettings findSettigns) {
		return fFinderManager.contains(text, findSettigns);
	}
	
	@Override
	public boolean findWord(String text, boolean caseSensitive) {
		return fFinderManager.findWord(text, caseSensitive);
	}

	@Override
	public int findText(String text, FindSettings findSettigns) {
		int findedId = fFinderIdManager.find(text, findSettigns);
		int findDescription = fFinderManager.findText(text, findSettigns);
		return findedId + findDescription;	
	}

	@Override
	public void updateFindedPoints(String content) {
		fFinderManager.updateFindedPoints(content);
	}
	
	@Override
	public boolean appendFind(String text, boolean caseSensitive) {
		return fFinderManager.appendFind(text, caseSensitive);
	}

	@Override
	public boolean appendWord(String text, boolean caseSensitive) {
		return fFinderManager.appendWord(text, caseSensitive);
	}

	@Override
	public void cleanFind() {
		fFinderManager.cleanFind();
		fFinderIdManager.cleanFind();
	}

	@Override
	public boolean hasFindResult() {
		return fFinderManager.hasFindResult();
	}

	@Override
	public List<Point> getFindedPoints() {
		return fFinderManager.getFindedPoints();
	}
	
	@Override
	public List<Point> getFindedIdPoints(){
		return fFinderIdManager.getFindedIdPoints();
	}
	
	//====================
	// glossary
	
	@Override
	public void removeIfFind(Collection<String> terms) {
		TagStringUtilities.removeIfContains(name(), terms);
	}
	
	@Override
	public void findTerms(Set<String> terms) {
		Terms.findTerms(name(), terms);
	}
	
	//=================================
	// table properties (columns' width)
	
	public double getRatioRestoreIdWidth() {
		return fProperties.getRatioRestoreIdWidth();
	}
	
	public double getRatioRestorePresentationWidth() {
		return fProperties.getRatioRestorePresentationWidth();
	}
	
	public double getRatioRestoreCommentWidth() {
		return fProperties.getRatioRestoreCommentWidth();
	}
	
	public void setRatioRestroreWidth(double idWidth, double presentationWidth, double commentWidth) {
		fProperties.setRatioRestroreWidth(idWidth, presentationWidth, commentWidth);
	}
	
	public double getRatioMaximizeIdWidth() {
		return fProperties.getRatioMaximizeIdWidth();
	}
	
	public double getRatioMaximizePresentationWidth() {
		return fProperties.getRatioMaximizePresentationWidth();
	}
	
	public double getRatioMaximizeCommentWidth() {
		return fProperties.getRatioMaximizeCommentWidth();
	}
	
	public void setRatioMaximizeWidth(double idWidth, double presentationWidth, double commentWidth) {
		fProperties.setRatioMaximizeWidth(idWidth, presentationWidth, commentWidth);
	}
	
	public int getIDColumnWidth(){
		return fProperties.getIDColumnWidth();
	}
	
	public void setIDColumnWidth(int id){
		fProperties.setIDColumnWidth(id);;
	}
	
	public int getPresentationColumnWidth(){
		return fProperties.getPresentationColumnWidth();
	}
	
	public void setPresentationColumnWidth(int id){
		fProperties.setPresentationColumnWidth(id);
	}
	
	public int getCommentColumnWidth(){
		return fProperties.getCommentColumnWidth();
	}
	
	public void setCommentColumnWidth(int id){
		fProperties.setCommentColumnWidth(id);
	}
	
}
