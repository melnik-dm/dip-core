package ru.dip.editors.editview;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.TextEditor;

import ru.dip.core.unit.UnitType;
import ru.dip.editors.csv.CsvMultiEditor;
import ru.dip.editors.dot.DotEditor;
import ru.dip.editors.formeditor.FormsEditor;
import ru.dip.editors.md.MDEditor;
import ru.dip.editors.report.ReportEditor;
import ru.dip.table.editor.MultiPageTableEditor;

public class EditorFactory {
	
	private static EditorFactory instance = new EditorFactory();
	
	private Map<UnitType, Supplier<IEditorPart>> fFactoryByType = new HashMap<>();
	
	private EditorFactory() {
		registerDefaultFactories();
	}
	
	private void registerDefaultFactories() {
		fFactoryByType.put(UnitType.FORM, FormsEditor::new);
		fFactoryByType.put(UnitType.TEXT, TextEditor::new);
		fFactoryByType.put(UnitType.MARKDOWN, MDEditor::new);
		fFactoryByType.put(UnitType.CSV, CsvMultiEditor::new);
		fFactoryByType.put(UnitType.TABLE, MultiPageTableEditor::new);
		fFactoryByType.put(UnitType.REPROT_REF, ReportEditor::new);				
		fFactoryByType.put(UnitType.DOT, DotEditor::new);
		
		fFactoryByType.put(UnitType.JSON, TextEditor::new);
		fFactoryByType.put(UnitType.UML, TextEditor::new);
	}		

	public static void registerEditor(UnitType type, Supplier<IEditorPart> supplier) {
		instance.fFactoryByType.put(type, supplier);
	}
	
	public static IEditorPart createEditor(UnitType type) {
		Supplier<IEditorPart> supplier = instance.fFactoryByType.get(type);
		if (supplier != null) {
			return supplier.get();
		}
		return null;
	}
	
}
