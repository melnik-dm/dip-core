package ru.dip.editors.report.content;

import ru.dip.core.model.DipProject;
import ru.dip.ui.table.ktable.TableCompositeSettings;

public class ReportCompositeSettings extends TableCompositeSettings {

	public ReportCompositeSettings(DipProject dipProject) {
		super(dipProject);
	}
	
	@Override
	public boolean isOneListMode() {
		return true;
	}

}
