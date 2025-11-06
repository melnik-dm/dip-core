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
package ru.dip.ui.table.ktable;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;

import ru.dip.core.model.DipProject;
import ru.dip.ui.table.ktable.model.DipTableModel;

public class TableSizeInteractor {

	private KTableComposite fTableComposite;
	private boolean fStart = true;  // флаг для первичного изменения ширины 

	// restore
	private double fRatioRestoreID = 0;
	private double fRatioRestorePresent = 0;
	private double fRatioRestoreComment = 0;
	// maximize
	private double fRatioMaximizeID = 0;
	private double fRatioMaximizePresent = 0;
	private double fRatioMaximizeComment = 0;

	public TableSizeInteractor(KTableComposite tableComposite) {
		fTableComposite = tableComposite;
		readSavedMaxMinSizes();
	}

	public CompositeControlListener getCompositeControlListener() {
		return new CompositeControlListener();
	}

	class CompositeControlListener implements ControlListener {

		@Override
		public void controlMoved(ControlEvent e) {
		}

		@Override
		public void controlResized(ControlEvent e) {
			if (tableWidth() == idWidth() + presentationWidth() + commentWidth()) {
				return;
			}
			compositeCotrolResized();
		}
	}

	public void compositeCotrolResized() {						
		// обычные сохраненные размеры (при старте)	
		if (fStart) {
			setSavedWidths(); 
			fStart  = false; 
			fTableComposite.asyncRefreshTree();
			return; 
		}

		if (isMaximize()) {
			applyMaximize();
			fTableComposite.asyncRefreshTree();
		} else if (isRestore()) {
			applyRestore();
			fTableComposite.asyncRefreshTree();
		}
	}

	private boolean isMaximize() {
		return fTableComposite.editor().isMaximize();
	}
	
	private void applyMaximize() {
		if (checkMaximizeSize()) {
			setIDWidth(fRatioMaximizeID);
			setPresentationWidth(fRatioMaximizePresent);
			setCommentWidth(fRatioMaximizeComment);
			fTableComposite.asyncRefreshTree();
		}		
	}
	
	/**
	 * Проверка если добавился один из столбцов в режиме restore
	 */
	private boolean checkMaximizeSize() {
		if (!validRatioMaximize()) {
			return false;
		}		
		if (fRatioMaximizeID <= 0 && isVisibleID()) {
			return false;
		}
		if (fRatioMaximizePresent <= 0) {
			return false;
		}
		if (fRatioMaximizeComment <= 0 && isVisibleComment()) {
			return false;
		}
		return true;		
	}
	
	private boolean validRatioMaximize() {
		return fRatioMaximizeID + fRatioMaximizePresent + fRatioMaximizeComment < 1.1;
	}

	private boolean isRestore() {
		return fTableComposite.editor().isRestore();
	}
	
	private void applyRestore() {
		if (checkRestoreSize()) {	
			setIDWidth(fRatioRestoreID);
			setPresentationWidth(fRatioRestorePresent);
			setCommentWidth(fRatioRestoreComment);
			fTableComposite.asyncRefreshTree();
		} 
	}
	
	/**
	 * Проверка если добавился один из столбцов в режиме maximize
	 */
	private boolean checkRestoreSize() {
		if (!validRatioRestore()) {
			return false;
		}	
		if (fRatioRestoreID == 0 && isVisibleID()) {
			return false;
		}
		if (fRatioRestorePresent == 0) {
			return false;
		}
		if (fRatioRestoreComment == 0 && isVisibleComment()) {
			return false;
		}
		return true;		
	}
	
	private boolean validRatioRestore() {
		return fRatioRestoreID + fRatioRestorePresent + fRatioRestoreComment < 1.1;
	}
	
	/**
	 * Устанавливает текущие значение (перед сменой режима)
	 */
	public void setCurrentColumnsWidth(boolean maximize) {
		int idWidth = idWidth();
		int presentationWidth = presentationWidth(); 
		int commentWidth = commentWidth();
		int compositeWidth = tableWidth();			
		if (!maximize) {
			fRatioMaximizeID = (double)idWidth / compositeWidth;
			fRatioMaximizePresent = (double)presentationWidth / compositeWidth;
			fRatioMaximizeComment = (double)commentWidth / compositeWidth;
		} else {
			fRatioRestoreID = (double)idWidth / compositeWidth;
			fRatioRestorePresent = (double)presentationWidth / compositeWidth;
			fRatioRestoreComment = (double)commentWidth / compositeWidth;
		}		
	}

	//==================================
	// save / load

	public void saveMaxMinSizes() {
		fTableComposite.dipProject().setRatioRestroreWidth(fRatioRestoreID, fRatioRestorePresent, fRatioRestoreComment);
		fTableComposite.dipProject().setRatioMaximizeWidth(fRatioMaximizeID, fRatioMaximizePresent,
				fRatioMaximizeComment);
	}

	public void readSavedMaxMinSizes() {
		fRatioRestoreID = fTableComposite.editor().model().dipProject().getRatioRestoreIdWidth();
		fRatioRestorePresent = fTableComposite.editor().model().dipProject().getRatioRestorePresentationWidth();
		fRatioRestoreComment = fTableComposite.editor().model().dipProject().getRatioRestoreCommentWidth();
		fRatioMaximizeID = fTableComposite.editor().model().dipProject().getRatioMaximizeIdWidth();
		fRatioMaximizePresent = fTableComposite.editor().model().dipProject().getRatioMaximizePresentationWidth();
		fRatioMaximizeComment = fTableComposite.editor().model().dipProject().getRatioMaximizeCommentWidth();
	}

	
	//================================
	// saved width
	
	// ============================
	// standard sizes

	private int fIdWidth;
	private int fPresentationWidth;
	private int fCommentWidth;

	public void saveColumnsWidth() {
				
		int idWidth = 0;
		if (isVisibleID()) {
			idWidth = idWidth();
		}		
		dipProject().setIDColumnWidth(idWidth);
		dipProject().setPresentationColumnWidth(presentationWidth());
		
		int commentWidth = 0;
		if (isVisibleComment()) {
			commentWidth = commentWidth();
		}
		dipProject().setCommentColumnWidth(commentWidth);
	}

	public void readSavedColumnsWidth() {
		fIdWidth = dipProject().getIDColumnWidth();
		fPresentationWidth = dipProject().getPresentationColumnWidth();
		fCommentWidth = dipProject().getCommentColumnWidth();
	}

	public void setSavedWidths() {
		if (fPresentationWidth + fIdWidth + fCommentWidth + 7 < tableWidth()) {
			model().setStandartWidth();
			return;
		} 
		
		if (fPresentationWidth <= 0) {
			model().setStandartWidth();
			return;
		}
		if (isVisibleID() && fIdWidth > 0) {	
			setIDWidth(fIdWidth);
		}		
		
		setPresentationWidth(fPresentationWidth);
		
		if (isVisibleComment() && fCommentWidth > 0) {
			setCommentWidth(fCommentWidth);
		}
	}
	
	//================================
	// getters, setters && additional
	
	private DipProject dipProject() {
		return fTableComposite.dipProject();
	}
	
	private DipTableModel model() {
		return fTableComposite.tableModel();
	}
	
	private void setIDWidth(double ratio) {
		if (model().isShowId()) {
			int width = (int) (ratio * tableWidth());
			model().setIdWidth(width);
		}
	}
		
	private void setPresentationWidth(double ratio) {
		int width = (int) (ratio * tableWidth());
		model().setPresentationWidth(width);
	}
	
	private void setCommentWidth(double ratio) {
		if (model().isShowComment()) {
			int width = (int) (ratio * tableWidth());
			model().setCommentWidth(width);
		}
	}
	
	private void setIDWidth(int width) {
		model().setIdWidth(width);
	}
	
	private void setPresentationWidth(int width) {
		model().setPresentationWidth(width);
	}
	
	private void setCommentWidth(int width) {
		model().setCommentWidth(width);
	}
	
	
	private int idWidth() {
		if (!model().isShowId()) {
			return 0;
		}
		return model().idWidth();
	}
	
	private int presentationWidth() {
		return model().presentationWidth();
	}
	
	private int commentWidth() {
		if (!model().isShowComment()) {
			return 0;
		}
		return model().commentWidth();
	}
	
	private int tableWidth() {
		return fTableComposite.getBounds().width;
	}
	
	private boolean isVisibleID() {
		return model().isShowId();
	}
	
	private boolean isVisibleComment() {
		return model().isShowComment();
	}
	
}
