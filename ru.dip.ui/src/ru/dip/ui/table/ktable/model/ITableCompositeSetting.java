package ru.dip.ui.table.ktable.model;

public interface ITableCompositeSetting extends ITableFormSettings {

	boolean isOneListMode();

	boolean isHideDisableObj();

	void setFindMode(boolean value);

	boolean isShowFormVersion();

	boolean isShowMdComment();

	boolean isShowStrictMdComment();

	boolean isShowNumeration();

	boolean isHighlightGloss();

	boolean isFixedContent();

	boolean isCheckSpellingEnable();

	boolean isFindMode();

}
