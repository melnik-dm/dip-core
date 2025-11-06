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
package ru.dip.core.utilities.ui.swt;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import javax.swing.JEditorPane;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ImageUtilities {
	
	/**
	 * 
	 * Старое изображение не освобождается!!!
	 */
	public static Image getResizedImage(Image image, int width, int height) {
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0,image.getBounds().width, image.getBounds().height, 0, 0, width, height);
		gc.dispose();
		//image.dispose(); // don't forget about me!
		return scaled;
	}
	
	public static Image trimImage(int max, Image image) {
		if (image.getBounds().width > image.getBounds().height) {
			return trimImageByWidth(max, image);
		} else {
			return trimImageByHeight(max, image);
		}
	}
	
	public static Image trimImageByWidth(int width, Image image){	
		int imageWidth = image.getBoundsInPixels().width;
		double k = (double) width / imageWidth;
		int height = (int) (image.getBounds().height * k);
		return ImageUtilities.getResizedImage(image, width, height);		
	}
	
	public static Image trimImageByHeight(int height, Image image){	
		int imageHeight= image.getBoundsInPixels().height;
		double k = (double) height / imageHeight;
		int width = (int) (image.getBounds().width * k);
		return ImageUtilities.getResizedImage(image, width, height);		
	}
	
	public  static Image createImageFromHtml(String HTML, IFile ifile) throws Exception {
		/*		
		// old version
		String filename = ifile.getLocation().toOSString();
		JEditorPane jep = new JEditorPane("text/html", HTML);
		Dimension dim = jep.getPreferredSize();
		BufferedImage image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration().createCompatibleImage(dim.width, dim.height);					
		Graphics graphics = image.createGraphics();
		jep.setSize(dim.width, dim.height);
		jep.print(graphics);
		
		File imageFile = File.createTempFile(filename, "");
		ImageIO.write(image, "png", imageFile);
		if (imageFile != null) {
			Image result =  new Image(Display.getDefault(), imageFile.getAbsolutePath());
			return result;
		}
		return null;*/
		return createImageFromHtml(HTML);
	}
	
	public  static Image createImageFromHtml(String HTML) {
		JEditorPane jep = new JEditorPane("text/html", HTML);
		Dimension dim = jep.getPreferredSize();
		BufferedImage image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration().createCompatibleImage(dim.width, dim.height);					
		Graphics graphics = image.createGraphics();
		jep.setSize(dim.width, dim.height);
		jep.print(graphics);	
		ImageData imageData = convertToSWT(image);	
		try {
			return new Image(null, imageData);
		} catch (Exception e) {
			// 
			return null;
		}
	}
	
	private static ImageData convertToSWT(BufferedImage bufferedImage) {
	    if (bufferedImage.getColorModel() instanceof DirectColorModel) {
	        DirectColorModel colorModel = (DirectColorModel)bufferedImage.getColorModel();
	        PaletteData palette = new PaletteData(
	                colorModel.getRedMask(),
	                colorModel.getGreenMask(),
	                colorModel.getBlueMask());
	        ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
	                colorModel.getPixelSize(), palette);
	        for (int y = 0; y < data.height; y++) {
	            for (int x = 0; x < data.width; x++) {
	                int rgb = bufferedImage.getRGB(x, y);
	                int pixel = palette.getPixel(new RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
	                data.setPixel(x, y, pixel);
	                if (colorModel.hasAlpha()) {
	                    data.setAlpha(x, y, (rgb >> 24) & 0xFF);
	                }
	            }
	        }
	        return data;
	    }
	    else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
	        IndexColorModel colorModel = (IndexColorModel)bufferedImage.getColorModel();
	        int size = colorModel.getMapSize();
	        byte[] reds = new byte[size];
	        byte[] greens = new byte[size];
	        byte[] blues = new byte[size];
	        colorModel.getReds(reds);
	        colorModel.getGreens(greens);
	        colorModel.getBlues(blues);
	        RGB[] rgbs = new RGB[size];
	        for (int i = 0; i < rgbs.length; i++) {
	            rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
	        }
	        PaletteData palette = new PaletteData(rgbs);
	        ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
	                colorModel.getPixelSize(), palette);
	        data.transparentPixel = colorModel.getTransparentPixel();
	        WritableRaster raster = bufferedImage.getRaster();
	        int[] pixelArray = new int[1];
	        for (int y = 0; y < data.height; y++) {
	            for (int x = 0; x < data.width; x++) {
	                raster.getPixel(x, y, pixelArray);
	                data.setPixel(x, y, pixelArray[0]);
	            }
	        }
	        return data;
	    }
	    else if (bufferedImage.getColorModel() instanceof ComponentColorModel) {
	        ComponentColorModel colorModel = (ComponentColorModel)bufferedImage.getColorModel();
	        //ASSUMES: 3 BYTE BGR IMAGE TYPE
	        PaletteData palette = new PaletteData(0x0000FF, 0x00FF00,0xFF0000);
	        ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
	                colorModel.getPixelSize(), palette);
	        //This is valid because we are using a 3-byte Data model with no transparent pixels
	        data.transparentPixel = -1;
	        WritableRaster raster = bufferedImage.getRaster();
	        int[] pixelArray = new int[3];
	        for (int y = 0; y < data.height; y++) {
	            for (int x = 0; x < data.width; x++) {
	                raster.getPixel(x, y, pixelArray);
	                int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
	                data.setPixel(x, y, pixel);
	            }
	        }
	        return data;
	    }
	    return null;
	}

}
