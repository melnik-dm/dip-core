package ru.dip.ui.table.ktable.render;

/**
 * Сделать на каждый тип Presentation
 * В PresentationPainter регистрировать для каждого типа
 */
public interface IElementRender {
	
	void udpate();
	
	void measure();
	
	void paint();

}
