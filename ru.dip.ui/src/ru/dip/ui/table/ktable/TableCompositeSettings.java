package ru.dip.ui.table.ktable;

import ru.dip.core.model.DipProject;
import ru.dip.ui.table.ktable.model.ITableCompositeSetting;

public class TableCompositeSettings implements ITableCompositeSetting {

	private boolean fOneListMode = false; // отображение одним списком (без узлов)
	private boolean fHighlightGlossMode = false;
	private boolean fHideDisableObj = false;
	private boolean fCheckSpellingEnable = true;
	private boolean fShowMDComment = true;
	private boolean fShowStrictMDComment = true;
	private boolean fShowFormNumeration = true;
	private boolean fFormShowPreferenciesEnable = false;
	private boolean fFixedContent = false;
	private boolean fShowNumeration = true;
	private boolean fShowFormVersion = false;
	private boolean fFindMode = false;

	public TableCompositeSettings(DipProject dipProject) {
		fOneListMode = dipProject.getProjectProperties().isOneListMode();
		fFixedContent = dipProject.getProjectProperties().isFixedContentMode();
		fShowNumeration = dipProject.getProjectProperties().isNumeration();
		fShowFormNumeration = dipProject.getProjectProperties().isFormNumeration();
		fHighlightGlossMode = dipProject.getProjectProperties().isHighlightGlossMode();
		fShowMDComment = dipProject.getProjectProperties().isMdComment();
		fShowStrictMDComment = dipProject.getProjectProperties().isStrictMdComment();
		fFormShowPreferenciesEnable = dipProject.getProjectProperties().isFormShowPreferenciesEnable();
		fCheckSpellingEnable = dipProject.getProjectProperties().isCheckSpellingEnable();
		fHideDisableObj = dipProject.getProjectProperties().isHideDisableObjsEnable();
		fShowFormVersion = dipProject.getProjectProperties().isShowFormVersion();
	}

	@Override
	public boolean isOneListMode() {
		return fOneListMode;
	}

	@Override
	public boolean isHighlightGloss() {
		return fHighlightGlossMode;
	}

	@Override
	public boolean isHideDisableObj() {
		return fHideDisableObj;
	}

	@Override
	public boolean isCheckSpellingEnable() {
		return fCheckSpellingEnable;
	}

	@Override
	public boolean isShowMdComment() {
		return fShowMDComment;
	}

	@Override
	public boolean isShowStrictMdComment() {
		return fShowStrictMDComment;
	}

	@Override
	public boolean isShowFormNumeration() {
		return fShowFormNumeration;
	}

	@Override
	public boolean isFixedContent() {
		return fFixedContent;
	}

	@Override
	public boolean isShowNumeration() {
		return fShowNumeration;
	}

	@Override
	public boolean isShowFormVersion() {
		return fShowFormVersion;
	}

	public void setOneListMode(boolean oneListMode) {
		fOneListMode = oneListMode;
	}

	public void setHighlightGlossMode(boolean highlightGlossMode) {
		fHighlightGlossMode = highlightGlossMode;
	}

	public void setHideDisableObj(boolean hideDisableObjEnable) {
		fHideDisableObj = hideDisableObjEnable;
	}

	public void setCheckSpellingEnable(boolean checkSpellingEnable) {
		fCheckSpellingEnable = checkSpellingEnable;
	}

	public void setShowMDComment(boolean showMDComment) {
		fShowMDComment = showMDComment;
	}

	public void setShowStrictMDComment(boolean showStrictMDComment) {
		fShowStrictMDComment = showStrictMDComment;
	}

	public void setShowFormNumeration(boolean showFormNumeration) {
		fShowFormNumeration = showFormNumeration;
	}

	public void setFormShowPreferenciesEnable(boolean formShowPreferenciesEnable) {
		fFormShowPreferenciesEnable = formShowPreferenciesEnable;
	}

	public void setFixedContent(boolean fixedContent) {
		fFixedContent = fixedContent;
	}

	public void setShowNumeration(boolean showNumeration) {
		fShowNumeration = showNumeration;
	}

	public void setShowFormVersion(boolean showFormVersion) {
		fShowFormVersion = showFormVersion;
	}

	@Override
	public boolean isFindMode() {
		return fFindMode;
	}

	public void setFindMode(boolean findMode) {
		fFindMode = findMode;
	}

	@Override
	public boolean isFormShowPreferenciesEnable() {
		return fFormShowPreferenciesEnable;
	}
}
