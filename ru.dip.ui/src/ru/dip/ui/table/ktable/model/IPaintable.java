package ru.dip.ui.table.ktable.model;

import org.eclipse.swt.graphics.Color;

public interface IPaintable {
	
	void prepare(int i, IDipTableModel dipTableModel);
	
	int height(IDipTableModel dipTableModel);
	
	void setCommentMainHeight(int i);
	
	int getCommentMainHeight();

	Color idBackground();

}
