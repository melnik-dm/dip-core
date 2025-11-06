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

public class ErrorDecorator implements ILabelDecorator {
	
	public static final String ID = "ru.dip.error.decorator"; //$NON-NLS-1$
	
	public static ErrorDecorator getErrorDecorator() {
		IDecoratorManager decoratorManager = PlatformUI.getWorkbench().getDecoratorManager();
		if (decoratorManager.getEnabled(ID)) {
			return (ErrorDecorator) decoratorManager.getLabelDecorator(ID);
		}
		return null;
	}

	@Override
	public Image decorateImage(Image image, Object element) {
		return drawIconImage(image);
	}

	private Image drawIconImage(Image baseImage) {
		ErrorImageIcon overlayIcon = new ErrorImageIcon(baseImage);
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
