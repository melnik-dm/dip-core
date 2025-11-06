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
package ru.dip.ui.navigator.decorator;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;

import ru.dip.core.model.interfaces.IDipElement;

public class ReadOnlyDecorator implements ILabelDecorator {

	public static final String ID = "ru.dip.readonly.decorator"; //$NON-NLS-1$
	
	public static ReadOnlyDecorator getDemoDecorator() {
		IDecoratorManager decoratorManager = PlatformUI.getWorkbench().getDecoratorManager();
		if (decoratorManager.getEnabled(ID)) {
			return (ReadOnlyDecorator) decoratorManager.getLabelDecorator(ID);
		}
		return null;
	}

	@Override
	public Image decorateImage(Image image, Object element) {
		if (element instanceof IDipElement) {
			IDipElement dipElement = (IDipElement) element;
			Image decorateImage = drawIconImage(image, dipElement.isReadOnly());
			return decorateImage;
		}
		return null;
	}

	private Image drawIconImage(Image baseImage, boolean readOnly) {
		OverlayImageIcon overlayIcon = new OverlayImageIcon(baseImage, readOnly);
		Image image = overlayIcon.getImage();
		return image;
	}

	@Override
	public String decorateText(String text, Object element) {
		return null;
	}
	
	@Override
	public void addListener(ILabelProviderListener listener) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {

	}
}
