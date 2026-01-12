package ru.dip.editors.report.content.model;

import java.util.List;

import org.eclipse.swt.graphics.Color;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.ui.table.ktable.model.ContentContainer;
import ru.dip.ui.table.ktable.model.ContentId;
import ru.dip.ui.table.ktable.model.ContentType;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.IDipTableModel;
import ru.dip.ui.table.ktable.model.ITableNode;

public class RceNotDocumentElement implements IDipTableElement {

	private final ContentContainer fContent = new ContentContainer();
	private final ITableNode fParent;
	private final IDipElement fDipElement;
	
	public RceNotDocumentElement(IDipElement element, ITableNode parent) {
		fDipElement = element;
		fParent = parent;
	}
	
	public IDipElement getDipElement() {
		return fDipElement;
	}
	
	@Override
	public void prepare(int i, IDipTableModel dipTableModel) {}

	@Override
	public int height(IDipTableModel dipTableModel) {
		return getInt(ContentId.ID, ContentType.HEIGHT);
	}

	@Override
	public void setCommentMainHeight(int i) {
		
	}

	@Override
	public int getCommentMainHeight() {
		return 0;
	}

	@Override
	public Color idBackground() {
		return background();		
	}

	@Override
	public Color background() {
		return get(ContentId.PRESENTATION, ContentType.BACKGROUND, Color.class);
	}

	@Override
	public void setBackground(Color background) {
		put(ContentId.PRESENTATION, ContentType.BACKGROUND, background);		
	}

	
	//=======================
	// content

	@Override
	public void put(ContentId id, ContentType type, Object obj) {
		fContent.put(id, type, obj);				
	}
	
	@Override
	public <T> T get(ContentId id, ContentType type, Class<T> className){
		return fContent.get(id, type, className);
	}
	
	@Override
	public int getInt(ContentId id, ContentType type){
		return fContent.getInt(id, type);
	}

	//===================
	// getters
	
	@Override
	public IDipDocumentElement dipDocElement() {
		return null;
	}

	@Override
	public void setLinkedElements(List<IDipTableElement> linkedElements) {
		
	}

	@Override
	public List<IDipTableElement> getLinkedElements() {
		return null;
	}

	@Override
	public void setNumber(int number) {
	}

	@Override
	public int getNumber() {
		return 0;
	}

	@Override
	public IDipTableElement self() {
		return this;
	}

	@Override
	public ITableNode parent() {
		return fParent;
	}

	@Override
	public boolean isDiff() {
		return false;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

}
