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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.utilities.ResourcesUtilities;

public class FontManager {
	
	// шрифты для Document/Section
	public static final String UBUNTU_MONO_FONT = "Ubuntu Mono";
	public static final String PT_MONO = "PT Mono";
	
	public static final String[] FONT_NAMES = {"System", UBUNTU_MONO_FONT, PT_MONO};

	static {
		try {			
			Method method = Font.class.getMethod("setNonDisposeHandler", Consumer.class);
			method.invoke(null, new Object[] {null});
		} catch (NoSuchMethodException e)  {
			DipCorePlugin.logInfo("FONT OLD VERSION (No setNonDisposeHandler method)");
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	// загрузка шрифтов
	static {
		try {			
			// Загрузка шрифтов Ubuntu
			Path pathMonoB = ResourcesUtilities.getPathFromPlugin(DipCorePlugin.getDefault(), "project_content/fonts/UbuntuMono-B.ttf");
			Path pathMonoBI = ResourcesUtilities.getPathFromPlugin(DipCorePlugin.getDefault(), "project_content/fonts/UbuntuMono-BI.ttf");
			Path pathMonoR = ResourcesUtilities.getPathFromPlugin(DipCorePlugin.getDefault(), "project_content/fonts/UbuntuMono-R.ttf");
			Path pathMonoRI = ResourcesUtilities.getPathFromPlugin(DipCorePlugin.getDefault(), "project_content/fonts/UbuntuMono-RI.ttf");
			Display.getDefault().loadFont(pathMonoB.toString());
			Display.getDefault().loadFont(pathMonoBI.toString());
			Display.getDefault().loadFont(pathMonoR.toString());
			Display.getDefault().loadFont(pathMonoRI.toString());
			
			// загрузка шрифта RTM55
			Path pathRTM = ResourcesUtilities.getPathFromPlugin(DipCorePlugin.getDefault(), "project_content/fonts/PTM55FT.ttf");
			Display.getDefault().loadFont(pathRTM.toString());


		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// for debug
		//System.out.println("load result: " + loadResult);
		/*System.out.println(Display.getDefault().getFontList("", false).length);
		System.out.println(Display.getDefault().getFontList("", true).length);
		System.out.println(Display.getDefault().getFontList("PT Mono", false).length);
		System.out.println(Display.getDefault().getFontList("PT Mono", true).length);
		for (FontData fd: JFaceResources.getTextFont().getFontData()) {
			System.out.println(fd.getName() + "   "  + fd.getHeight() + "  "  + fd.getStyle() + "  "  + fd.getLocale());
		}
		for (FontData fd: JFaceResources.getDefaultFont().getFontData()) {
			System.out.println(fd.getName() + "   "  + fd.getHeight() + "  "  + fd.getStyle() + "  "  + fd.getLocale());
		}*/
	}
	
	
	public static Display display = Display.getDefault();
	
	// mono
	public static FontData mono = new FontData(getMonoFontName(), 10, SWT.NONE); 
	public static Font mono_font = new Font(display, mono);
	//public static Font mono_font = new Font(null, new FontData("Ubuntu Mono"));
	
	// жирный-12
	public static FontData boldTwelweFontData = new FontData(); 
	public static Font boldTwelweFont;
	static {
		boldTwelweFontData.setStyle(SWT.BOLD);
		boldTwelweFontData.setHeight(12);
		boldTwelweFont = new Font(null, boldTwelweFontData);
	}
	
	public static FontData bold14FontData = new FontData(); 
	public static Font bold14Font;
	static {
		bold14FontData.setStyle(SWT.BOLD);
		bold14FontData.setHeight(14);
		bold14Font = new Font(null, bold14FontData);
	}
	
	// жирный-11
	public static FontData boldElevenFontData = new FontData(); 
	public static Font boldEleventFont;
	static {
		boldElevenFontData.setStyle(SWT.BOLD);
		boldElevenFontData.setHeight(11);
		boldEleventFont = new Font(null, boldElevenFontData);
	}
	
	public static FontData font12Data = new FontData(); 
	public static Font font12;
	static {
		font12Data.setHeight(12);
		font12 = new Font(display, font12Data);
	}
	
	public static FontData boldMonoElevenFontData = new FontData(getMonoFontName(), 11, SWT.BOLD); 
	public static Font boldMonoEleventFont;
	static {
		boldMonoElevenFontData.setStyle(SWT.BOLD);
		boldMonoElevenFontData.setHeight(11);
		boldMonoEleventFont = new Font(display, boldMonoElevenFontData);
	}
	
	// моно-8
	public static FontData monoEightFontData = new FontData(getMonoFontName(), 8, SWT.NONE); 
	public static Font monoEightFont;
	static {
		//monoElevenFontData.setStyle(SWT.BOLD);
		monoEightFontData.setHeight(8);
		monoEightFont = new Font(display, monoEightFontData);
	}

	// жирный-10
	public static FontData boldTenFontData = new FontData();	 
	public static Font boldFont; 	
	static {
		boldTenFontData.setStyle(SWT.BOLD);
		boldTenFontData.setHeight(10);
		boldFont = new Font(display, boldTenFontData);
	}
	
	// жирный-10
	public static FontData itlicTenFontData = new FontData();	 
	public static Font italicFont; 	
	static {
		itlicTenFontData.setStyle(SWT.ITALIC);
		itlicTenFontData.setHeight(10);
		italicFont = new Font(display, itlicTenFontData);
	}
	
	// жирный-10
	public static FontData itlicBoldTenFontData = new FontData();	 
	public static Font italicBoldFont; 	
	static {
		itlicBoldTenFontData.setStyle(SWT.ITALIC /*| SWT.BOLD*/);
		itlicBoldTenFontData.setHeight(10);
		italicBoldFont = new Font(display, itlicBoldTenFontData);
	}
	
	public static FontData itlicMonoTenFontData = new FontData(getMonoFontName(), 10, SWT.ITALIC);	 
	public static Font italicMonoFont; 	
	static {
		itlicMonoTenFontData.setStyle(SWT.ITALIC);
		itlicMonoTenFontData.setHeight(10);
		italicMonoFont = new Font(display, itlicMonoTenFontData);
	}
	
	public static Font getMonoFont(int height){
		FontData fontData = new FontData(getMonoFontName(), height, SWT.NONE);
		fontData.setHeight(height);
		Font font = new Font(display, fontData);		
		return font;
	}
	
	public static Font getBoldMonoFont(int height){
		FontData fontData = new FontData(getMonoFontName(), height, SWT.BOLD);
		fontData.setStyle(SWT.BOLD);
		fontData.setHeight(height);
		Font font = new Font(display, fontData);
		return font;
	}
	
	public static Font getItalicMonoFont(int height){
		FontData fontData = new FontData(getMonoFontName(), height, SWT.ITALIC);
		fontData.setStyle(SWT.ITALIC);
		fontData.setHeight(height);
		Font font = new Font(display, fontData);
		return font;
	}
	
	public static Font getBoldItalicMonoFont(int height){
		FontData fontData = new FontData(getMonoFontName(), height, SWT.ITALIC | SWT.BOLD);
		fontData.setStyle(SWT.ITALIC | SWT.BOLD);	
		fontData.setHeight(height);
		Font font = new Font(display, fontData);
		
		//Display.getDefault().loadFont("UbuntuMono-R.ttf");
		//Display.getDefault().loadFont(path)
		return font;
	}
	
	
	private static String getMonoFontName() {
		String monoFont = DipCorePlugin.getMonoFont();
		if (UBUNTU_MONO_FONT.equals(monoFont)) {
			return "Ubuntu Mono";
		} else if (PT_MONO.equals(monoFont)) {
			return "PT Mono";
		} else {				
			Font terminalFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);
			return terminalFont.getFontData()[0].getName();
		}
		//return "Monospace";
	}
	
}
