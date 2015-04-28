package nl.imarinelife.lib.utility;

import android.widget.Filterable;

public interface FilterEnabledAdapter extends Filterable  {
	public boolean isShowingSelection();

	public String getSelectionConstraint();

	public void invalidateSelection();
	
	
}
