package org.blockframe.blocks;

import java.util.List;

import org.blockframe.core.Layout;

public class LedgerColumnConfig { 
	
	final String columnTitle ; 
	
	final boolean isMultiline ; 
	
	final Layout.Justification justification ; 
	
	private Layout.Alignment alignment = Layout.TOP ; 
	
	final boolean isRubberWidth ; 
	
	public LedgerColumnConfig(String columnTitle, boolean isMultiline, Layout.Justification justification, boolean isRubberWidth) { 
		this.columnTitle = columnTitle ; 
		this.isMultiline = isMultiline ; 
		this.justification = justification ; 
		this.isRubberWidth = isRubberWidth ; 
	}
	
	public String getColumnTitle(int ledgerPageNumber) { 
		return columnTitle ; 
	}
	
	public static LedgerColumnConfig [] toArray(List<LedgerColumnConfig> list) { 
		LedgerColumnConfig [] array = new LedgerColumnConfig[list.size()] ; 
		list.toArray(array); 
		return array ; 
	}

	public Layout.Alignment getAlignment() {
		return alignment;
	}

	public LedgerColumnConfig setAlignment(Layout.Alignment alignment) {
		this.alignment = alignment ; 
		return this ; 
	}
}