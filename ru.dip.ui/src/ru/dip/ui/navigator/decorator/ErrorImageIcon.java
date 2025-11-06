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

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import ru.dip.ui.utilities.image.ImageProvider;

public class ErrorImageIcon extends CompositeImageDescriptor {

	private Image fBaseImage;
	private Point fImageSize;
	
	public ErrorImageIcon(Image baseImage){
		fBaseImage = baseImage;
		fImageSize = new Point(baseImage.getBounds().width, baseImage.getBounds().height);
	}

	@Override
	protected void drawCompositeImage(int width, int height) {
		drawImage(createCachedImageDataProvider(fBaseImage), 0, 0);
		CachedImageDataProvider error = createCachedImageDataProvider(ImageProvider.ERRPR_OVR_DESCRIPTOR);
		drawImage(error, fImageSize.x - error.getWidth(), fImageSize.y - error.getHeight());		
	}
	
	@Override
	protected Point getSize() {
		return fImageSize;
	}

	public Image getImage() {
		return createImage();
	}

}
