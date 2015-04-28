package nl.imarinelife.lib.utility;

import android.content.ContentValues;

public class SQLUpdateObject {
	
	private ContentValues updateValues;
	private String selection;
	
	public SQLUpdateObject(ContentValues updateValues, String selection){
		this.updateValues=updateValues;
		this.selection=selection;
	}
	
	public ContentValues getUpdateValues() {
		return updateValues;
	}

	public String getSelection() {
		return selection;
	}

}
