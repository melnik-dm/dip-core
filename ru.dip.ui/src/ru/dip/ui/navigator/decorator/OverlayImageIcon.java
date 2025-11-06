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

public class OverlayImageIcon extends CompositeImageDescriptor {

	private Image fBaseImage;
	private Point fImageSize;
	private boolean fReadOnly;
	
	public OverlayImageIcon(Image baseImage, boolean readOnly){
		fBaseImage = baseImage;
		fImageSize = new Point(baseImage.getBounds().width, baseImage.getBounds().height);
		fReadOnly = readOnly;
		
	}

	@Override
	protected void drawCompositeImage(int width, int height) {
		drawImage(createCachedImageDataProvider(fBaseImage), 0, 0);				
		CachedImageDataProvider imageProvider = imageData();		
		drawImage(imageProvider, 0, fImageSize.y - imageProvider.getHeight());
	}
	
	private CachedImageDataProvider imageData() {
		if (fReadOnly) {
			return createCachedImageDataProvider(ImageProvider.LOCK_OVR_DESCRIPTOR);
		} else {
			return createCachedImageDataProvider(ImageProvider.LINK_OVR_DESCRIPTOR);
		}
	}

	@Override
	protected Point getSize() {
		return fImageSize;
	}

	public Image getImage() {
		return createImage();
	}

}
