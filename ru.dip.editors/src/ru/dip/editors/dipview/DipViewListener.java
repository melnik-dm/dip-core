package ru.dip.editors.dipview;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import ru.dip.core.utilities.EditorUtils;

public class DipViewListener implements IPartListener2 {
	
	private DipView fDipView;
	
	public DipViewListener(DipView dipView) {
		fDipView = dipView;
	}
	
	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof IEditorPart) {
			fDipView.propertyChanged(part, EditorUtils.VISIBLE_EVENT);
		}
	}
	
	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof IEditorPart) {
			fDipView.propertyChanged(part, EditorUtils.HIDE_EVENT);
		};
	}

}
