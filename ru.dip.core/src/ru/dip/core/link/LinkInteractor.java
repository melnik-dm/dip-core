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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.unit.UnitType;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.ui.swt.ColorProvider;

public class LinkInteractor {

	public static final String LINS_VIEW_ID = "ru.dip.editors.incorrectlinks";
	
	public static final String LINK_REGEX = "\\[[^]\\[]*\\]\\([^)]*\\)";
	public static final Pattern LINK_PATTERN = Pattern.compile(LINK_REGEX);

	private static LinkInteractor instance;

	public static LinkInteractor instance() {
		if (instance == null) {
			instance = new LinkInteractor();
		}
		return instance;
	}
	
	public static IDipElement findElement(IFile file, String link) {
		IDipElement element = DipUtilities.findElement(file);
		return DipUtilities.findElement(element, link);
	}
		
	private HashSet<IncorrectLink> fIncorrectLinks = new HashSet<>();
	private ArrayList<CorrectLink> fCorrectLinks = new ArrayList<>();
	private IncorrectLinkChangeListener fListener;
	
	// =================================
	// changeLinks

	public String changeLinks(String original, IDipUnit source) {
		if (original == null) {
			return original;
		}
		Matcher matcher = LINK_PATTERN.matcher(original);
		while (matcher.find()) {
			String group = matcher.group();
			int index = group.indexOf("](");
			String title = group.substring(1, index);
			String link = group.substring(index + 2, group.length() - 1);
			String result = getRef(title, link, source);
			original = matcher.replaceFirst(result);
			matcher = LINK_PATTERN.matcher(original);
		}
		return original;
	}

	public String changeLinks(DipProject dipProject, String original) {
		Matcher matcher = LINK_PATTERN.matcher(original);
		while (matcher.find()) {
			String group = matcher.group();
			int index = group.indexOf("](");
			String title = group.substring(1, index);
			String link = group.substring(index + 2, group.length() - 1);
			String result = getRef(dipProject, title, link);
			original = matcher.replaceFirst(result);
			matcher = LINK_PATTERN.matcher(original);
		}
		return original;
	}

	/**
	 * Заменяет ссылки для представления в DIPRender В отличие от предыдущих
	 * методов, не убирает саму ссылку, просто меняет значение для +,-,#,№ и т.д.
	 */
	public String changeLinksForRender(String original, IDipUnit source) {
		if (original == null) {
			return original;
		}
		Matcher matcher = LINK_PATTERN.matcher(original);
		int findStartIndex = 0;
		while (matcher.find(findStartIndex)) {
			String group = matcher.group();			
			int index = group.indexOf("](");
			String title = group.substring(1, index);
			String link = group.substring(index + 2, group.length() - 1);
			String result = getRef(title, link, source);
			findStartIndex = matcher.end();
			String newLink = "[" + result + "](" + link + ")";
			original = original.substring(0, matcher.start()) + newLink + original.substring(matcher.end());
			matcher = LINK_PATTERN.matcher(original);
		}
		return original;
	}

	public String getRef(String title, String destination, IDipUnit source) {
		DipProject project = source.dipProject();
		IDipElement element = DipUtilities.findElement(source, destination);
		if (element == null) {
			fIncorrectLinks.add(new IncorrectLink(title, destination, source));
			return destination;
		}
		return getTitle(title, destination, project, element);
	}

	public String getRef(DipProject project, String title, String destination) {
		IDipElement element = DipUtilities.findElement(project, destination);
		if (element == null) {
			return destination;
		}
		return getTitle(title, destination, project, element);
	}

	private String getTitle(String title, String destination, DipProject project, IDipElement element) {
		if (element instanceof IDipDocumentElement && ((IDipDocumentElement) element).isDisabledInDocument()) {
			return DipUtilities.relativeProjectID(element);
		}
		
		switch (title) {
		case "№": {
			return getNumberRef(project, element, destination);
		}
		case "#": {
			return getNumberRef(project, element, destination);
		}
		case "*": {
			return getFullRef(project, element, destination);
		}
		case "-": {
			return getShortRef(project, element, destination);
		}
		case "+": {
			return getLongRef(project, element, destination);
		}
		case "": {
			return getEmptyRef(element, destination);
		}
		default: {
			return title;
		}
		}
	}

	public String getNumberRef(DipProject project, IDipElement element, String destination) {
		if (element != null) {
			String number = getNumberRef(element);
			if (number != null) {
				return number;
			}
		}
		return destination;
	}

	public String getNumberRef(IDipElement element) {
		if (element instanceof DipUnit) {
			DipUnit unit = (DipUnit) element;
			UnitType type = unit.getUnitType();
			if (type.isRefType()) {
				return String.valueOf(unit.getNumer());
			} else {
				return getNumberRef(unit.parent());
			}
		} else if (element instanceof DipFolder) {
			DipFolder folder = (DipFolder) element;
			if (folder.isActiveNumeration()) {
				return folder.number();
			}
		}
		return null;
	}

	public String getShortRef(DipProject project, String destination) {
		IDipElement element = DipUtilities.findElement(project, destination);
		return getShortRef(project, element, destination);
	}

	// from markdown (empty ref)
	public String getEmptyRef(DipUnit source, String destination) {
		DipProject project = source.dipProject();
		IDipElement element = DipUtilities.findElement(project, destination);
		if (element == null) {
			fIncorrectLinks.add(new IncorrectLink("", destination, source));
			return destination;
		}
		return getTitle("", destination, project, element);
	}

	public String getShortRef(DipProject project, IDipElement element, String destination) {
		if (element != null) {
			String number = getShortRef(element);
			if (number != null) {
				return number;
			}
		}
		return destination;
	}

	public String getShortRef(IDipElement element) {
		if (element instanceof DipUnit) {
			DipUnit unit = (DipUnit) element;
			UnitType type = unit.getUnitType();
			if (type.isRefType()) {
				return unit.getUnitDescription().shortLinkTitle();
			} else {
				return getShortRef(unit.parent());
			}
		} else if (element instanceof DipFolder) {
			DipFolder folder = (DipFolder) element;
			if (folder.isActiveNumeration()) {
				return "п. " + folder.number();
			}
		}
		return null;
	}

	public String getLongRef(DipProject project, IDipElement element, String destination) {
		if (element != null) {
			String number = getLongRef(element);
			if (number != null) {
				return number;
			}
		}
		return destination;
	}

	public String getLongRef(IDipElement element) {
		if (element instanceof DipUnit) {
			DipUnit unit = (DipUnit) element;
			UnitType type = unit.getUnitType();
			if (type.isRefType()) {
				return unit.getUnitDescription().longLinkTitle();
			} else {
				return getLongRef(unit.parent());
			}
		} else if (element instanceof DipFolder) {
			DipFolder folder = (DipFolder) element;
			if (folder.isActiveNumeration()) {
				StringBuilder builder = new StringBuilder();
				builder.append(folder.number());
				if (folder.description() != null && !folder.description().isEmpty()) {
					builder.append(" ");
					builder.append(folder.description());
				}
				return builder.toString();
			} else if (folder.description() != null && !folder.description().isEmpty()) {
				return folder.description();
			}
		}
		return null;
	}

	public String getFullRef(DipProject project, IDipElement element, String destination) {
		if (element != null) {
			String number = getFullRef(element);
			if (number != null) {
				return number;
			}
		}
		return destination;
	}

	public String getFullRef(IDipElement element) {
		if (element instanceof DipUnit) {
			DipUnit unit = (DipUnit) element;
			UnitType type = unit.getUnitType();
			if (type.isRefType()) {
				return unit.getUnitDescription().fullLinkTitle();
			} else {
				return getLongRef(unit.parent());
			}
		} else if (element instanceof DipFolder) {
			DipFolder folder = (DipFolder) element;
			if (folder.isActiveNumeration()) {
				StringBuilder builder = new StringBuilder();
				builder.append("Раздел ");
				builder.append(folder.number());
				builder.append(".");
				if (folder.description() != null && !folder.description().isEmpty()) {
					builder.append(" ");
					builder.append(folder.description());
				}
				return builder.toString();
			} else if (folder.description() != null && !folder.description().isEmpty()) {
				return folder.description();
			}
		}
		return null;
	}

	public String getEmptyRef(IDipElement element, String destination) {
		if (element instanceof DipUnit) {
			DipUnit unit = (DipUnit) element;
			UnitType type = unit.getUnitType();
			if (!type.isRefType()) {
				return DipUtilities.relativeProjectID(unit.parent());
			}
		}
		return destination;
	}

	// ===================================
	// incorrect links

	public Object[] getIncorrectLinks(DipProject project) {
		String projectName = project.name();
		return fIncorrectLinks.stream().filter((link) -> link.isLocatedProject(projectName))
				.sorted(Comparator.comparing(IncorrectLink::getSourceId)).toArray();
	}

	public void clearIncorrectLinks() {
		fIncorrectLinks.clear();
	}

	public void checkIncorrectLinks() {
		ArrayList<IncorrectLink> removeLinks = new ArrayList<>();
		for (IncorrectLink link : fIncorrectLinks) {
			if (isCorrectLink(link)) {
				removeLinks.add(link);
			}
		}
		fIncorrectLinks.removeAll(removeLinks);
	}

	private boolean isCorrectLink(IncorrectLink link) {
		IFile file = link.getSource().resource();
		if (!file.exists()) {
			return false;
		}
		if (checkLinks(link.getLink(), link.getSource())) {
			return false;
		}
		IDipElement element = null;
		if (link.getSource().isIncluded()) {
			IncludeFolder includeFolder = DipUtilities.findIncludeFolder(link.getSource());
			element = DipUtilities.findElement(includeFolder, link.getLink());
		} else {
			element = DipUtilities.findElement(link.getSource().dipProject(), link.getLink());
			if (element == null) {
				return false;
			}
		}
		if (element == null) {
			return false;
		}
		return true;
	}

	// ===================================
	// update links

	public void updateLinks(IDipElement lastElem, IDipElement newElem) {
		if (lastElem == null || newElem == null) {
			return;
		}
		boolean childrenLinks = newElem instanceof IDipParent;
		String oldID = DipUtilities.relativeProjectID(lastElem);
		String newID = DipUtilities.relativeProjectID(newElem);
		LinkInteractor.instance().updateLinks(oldID, newID, newElem.dipProject(), childrenLinks);
	}

	/*
	 * childrenLinks - проверить ссылки на дочерние элементы
	 */
	public void updateLinks(String lastID, String newID, IDipParent parent, boolean childrenLinks) {		
		LinkUpdater updater = new LinkUpdater(lastID, newID);
		for (IDipDocumentElement dipDocElement : parent.getDipDocChildrenList()) {
			if (dipDocElement instanceof DipUnit) {
				updateLinks(updater, (DipUnit) dipDocElement, childrenLinks);
			} else if (dipDocElement instanceof IDipParent) {
				updateLinks(lastID, newID, (IDipParent) dipDocElement, childrenLinks);
			}
		}
	}

	private void updateLinks(LinkUpdater updater, DipUnit unit, boolean childrenLinks) {
		UnitType type = unit.getUnitType();
		if (type.isLinkSupport()) {
			try {
				IFile file = unit.resource();
				String content = FileUtilities.readFile(file);
				if (content == null) {
					return;
				}
				String newContent = updater.updateLinks(content, childrenLinks);
				if (updater.isNeedUpdate()) {
					FileUtilities.writeFile(file, newContent);
				}
			} catch (IOException e) {
				e.printStackTrace();
				DipCorePlugin.logError(e, "Ошибка обновления ссылок");
				WorkbenchUtitlities.openError("Update link error", "Ошибка при обновлении ссылки для " + unit.name());
			}
		}
	}

	// =====================================
	// check links

	public void checkLinksAfterDelete(DipProject project) {
		if (fListener == null) {
			return;
		}
		Display.getDefault().asyncExec(() -> {
			boolean valid = checkLinks(project);
			if (!valid) {
				fListener.linksChanged();
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				FindIncorrectLinksDialog dialog = new FindIncorrectLinksDialog(shell);
				dialog.open();
			}
		});
	}

	public boolean checkLinks(IDipParent parent) {
		boolean result = true;
		if (parent == null) {
			return true;
		}
		for (IDipDocumentElement dipDocumentElement : parent.getDipDocChildrenList()) {
			if (dipDocumentElement instanceof DipUnit) {
				boolean checkUnit = checkLinks((DipUnit) dipDocumentElement);
				if (!checkUnit) {
					result = false;
				}
			} else if (dipDocumentElement instanceof IDipParent) {
				boolean checkParent = checkLinks((IDipParent) dipDocumentElement);
				if (!checkParent) {
					result = false;
				}
			}
		}
		return result;
	}

	private boolean checkLinks(DipUnit unit) {
		UnitType type = unit.getUnitType();
		if (type.isLinkSupport()) {
			try {
				IFile file = unit.resource();
				if (!file.exists()) {
					return true;
				}
				String content = FileUtilities.readFile(file);
				return checkLinksAfterDelete(content, unit);
			} catch (IOException e) {
				e.printStackTrace();
				DipCorePlugin.logError(e, "Ошибка проверки ссылок");
			}
		}
		return true;
	}

	private boolean checkLinksAfterDelete(String content, DipUnit unit) {
		Matcher matcher = LINK_PATTERN.matcher(content);
		boolean result = true;
		while (matcher.find()) {
			String group = matcher.group();
			int index = group.indexOf("](");
			String title = group.substring(1, index);
			String link = group.substring(index + 2, group.length() - 1);
			IDipElement element = DipUtilities.findElement(unit, link);
			if (element == null) {
				if (fIncorrectLinks.add(new IncorrectLink(title, link, unit))) {
					result = false;
				}
			} else {
				fCorrectLinks.add(new CorrectLink(title, link, unit, element));
			}
		}
		return result;
	}

	private boolean checkLinks(String id, IDipUnit unit) {
		UnitType type = unit.getUnitType();
		if (type.isLinkSupport()) {
			try {
				IFile file = unit.resource();
				String content = FileUtilities.readFile(file);
				return findInccorrectLinks(id, content, unit);
			} catch (IOException e) {
				e.printStackTrace();
				DipCorePlugin.logError(e, "Ошибка проверки ссылок");
			}
		}
		return true;
	}

	private boolean findInccorrectLinks(String id, String content, IDipUnit unit) {
		String idRregex = id.replace(".", "\\.").replace("-", "\\-").replace("[", "\\[").replace("]", "\\]")
				.replace("+", "\\+").replace("(", "\\(").replace(")", "\\)");
		String regex = "\\[[^]]*\\]\\(" + idRregex + "\\)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		return !matcher.find();
	}

	
	/**
	 * Список всех ссылок для DipUnit (используется при перепроцессинге)
	 */
	public List<Link> findAllLinks(IDipUnit unit){
		try {
			IFile file = unit.resource();
			String content = FileUtilities.readFile(file);
			return findAllLinks(unit, content);
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return new ArrayList<>();
	}
	
	public static List<Link> findAllLinks(IDipUnit unit, String content){		
		List<Link> links = new ArrayList<>();	
		Matcher matcher = LINK_PATTERN.matcher(content);
		while (matcher.find()) {
			String group = matcher.group();
			int index = group.indexOf("](");
			String title = group.substring(1, index);
			String link = group.substring(index + 2, group.length() - 1);
			IDipElement element = DipUtilities.findElement(unit, link);
			if (element == null) {
				links.add(new IncorrectLink(title, link, unit));
			} else {
				links.add(new CorrectLink(title, link, unit, element));
			}
		}
		return links;		
	}
	
	public static List<Point> findAllLinks(String content){		
		List<Point> links = new ArrayList<>();	
		Matcher matcher = LINK_PATTERN.matcher(content);
		while (matcher.find()) {
			links.add(new Point(matcher.start(), matcher.end()));
		}
		return links;		
	}
	
	public static class FindIncorrectLinksDialog extends Dialog {

		protected FindIncorrectLinksDialog(Shell parentShell) {
			super(parentShell);
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Incorrect links");
		}

		@Override
		protected Point getInitialSize() {
			Point p = super.getInitialSize();
			if (p.x < 500) {
				p.x = 500;
			}
			return p;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout(3, false));
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			Label imageLabel = new Label(composite, SWT.NONE);
			Image questionImage = Display.getDefault().getSystemImage(SWT.ICON_ERROR);
			imageLabel.setImage(questionImage);
			Label textLabel = new Label(composite, SWT.WRAP);
			textLabel.setText("Найдены некорректные ссылки. ");

			Hyperlink link = new Hyperlink(composite, SWT.NONE);
			link.setText("Открыть Incorrect Links.  ");
			link.setForeground(ColorProvider.SELECT);
			link.setUnderlined(true);
			link.addHyperlinkListener(new IHyperlinkListener() {

				@Override
				public void linkExited(HyperlinkEvent e) {}

				@Override
				public void linkEntered(HyperlinkEvent e) {}

				@Override
				public void linkActivated(HyperlinkEvent e) {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						page.showView(LINS_VIEW_ID);
						FindIncorrectLinksDialog.this.close();
					} catch (PartInitException e1) {
						e1.printStackTrace();
					}
				}
			});
			return composite;
		}
	}

	// =============================

	public List<Object> allLinks(DipProject project) {
		String projectName = project.name();
		return Stream.concat(
					fIncorrectLinks.stream().sorted(Comparator.comparing(Link::getSourceId)),
					fCorrectLinks.stream().sorted(Comparator.comparing(Link::getSourceId)))
				.filter((link) -> link.isLocatedProject(projectName))
				.collect(Collectors.toList());
	}

	public void setIncorrectChangeListener(IncorrectLinkChangeListener listener) {
		fListener = listener;
	}

	public void clearLinks() {
		fCorrectLinks = new ArrayList<>();
	}
	
	public void clearAllLinks() {
		fIncorrectLinks = new HashSet<>();
		fCorrectLinks = new ArrayList<>();
	}
}
