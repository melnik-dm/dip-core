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
package ru.dip.ui.imageview;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.unit.CsvUnitPresentation;
import ru.dip.core.unit.HtmlImagePresentation;
import ru.dip.core.unit.TablePresentation;
import ru.dip.core.utilities.ui.image.ImageUtilities;
import ru.dip.ui.table.table.TableSettings;

/**
 * Кэш для картинок (миниатюр)
 */
public class ImagesCacheProvider {

	private class Key {
		
		private final IResource fResource;
		private final long fTimeStamp;
		private final int fWidth;
		
		public Key(IDipUnit unit, int width) {
			fResource = unit.resource();
			fTimeStamp = fResource.getLocalTimeStamp();
			fWidth = width;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + ((fResource == null) ? 0 : fResource.hashCode());
			result = prime * result + (int) (fTimeStamp ^ (fTimeStamp >>> 32));
			result = prime * result + fWidth;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			if (fResource == null) {
				if (other.fResource != null)
					return false;
			} else if (!fResource.equals(other.fResource))
				return false;
			if (fTimeStamp != other.fTimeStamp)
				return false;
			if (fWidth != other.fWidth)
				return false;
			return true;
		}

		private ImagesCacheProvider getEnclosingInstance() {
			return ImagesCacheProvider.this;
		}
	
	}
	
	private final Map<Key, Image> fImages = new HashMap<>();
	
	public Image getImage(IDipUnit unit, int width) {
		Key key = new Key(unit, width);
		return fImages.get(key);
	}
	
	public Image getShowedImage(IDipUnit unit, int width) {
		Image image = getImage(unit, width);
		if (image != null) {
			return image;
		}		
		image = getImageFromUnit(unit, width);
		if (image == null) {
			return null;
		}		
		if (image.getBounds().height > ImagesView.IMAGE_WIDTH) {
			image = ImageUtilities.getResizedImage(image, image.getBounds().width, ImagesView.IMAGE_WIDTH);
		}			
		put(unit, image, width);;
		return image;
	}
	
	private Image getImageFromUnit(IDipUnit unit, int width) {
		TablePresentation presentation = unit.getUnitPresentation().getPresentation();
		if (presentation instanceof HtmlImagePresentation) {
			return ((HtmlImagePresentation) presentation).getExternalImage(width);	
		} else if (presentation instanceof CsvUnitPresentation) {
			boolean wrapTable = TableSettings.isCsvColumnWidthByContent();
			return  ((CsvUnitPresentation) presentation).getExternalImage(width, wrapTable);
		} else {
			Image image = presentation.getImage();
			if (image == null) {
				return null;
			}			
			return getImageIcon(image, width);
		}				
	}
	
	private Image getImageIcon(Image original, int width) {
		Rectangle originalBounds = original.getBounds();
		if (originalBounds.width <= width 
				&& originalBounds.height <= width) {
			return new Image(null,original.getImageData());
		}				 	
		return ImageUtilities.trimImage(width, original);	
	}
	
	public void put(IDipUnit unit, Image image, int width) {
		Key key = new Key(unit, width);
		fImages.put(key, image);
	}

	public void dispose() {
		fImages.values().forEach(Image::dispose);
		fImages.clear();
	}
	
}
