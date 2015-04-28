package nl.imarinelife.lib.divinglog.db.res;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.catalog.Catalog;

public class ProfilePart {
	public String catName = null;
	public String stayId = null;
	public String showName = null;
	public int orderNumber = 0;
	public boolean addForTotalDiveTime = false;

	private static final String TAG = "ProfilePart";

	public ProfilePart(String catName, String stayId, String showName,
			boolean addForTotalDiveTime, int orderNumber) {
		super();
		this.catName = catName;
		this.stayId = stayId;
		this.showName = showName;
		this.addForTotalDiveTime = addForTotalDiveTime;
		this.orderNumber = orderNumber;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("catName[" + catName + "]");
		builder.append("orderNumber[" + orderNumber + "]");
		builder.append("stayId[" + stayId + "]");
		builder.append("showName[" + showName + "]");
		builder.append("add[" + addForTotalDiveTime + "]");
		return builder.toString();
	}

	public String getShowName() {
		if(showName==null){
			Catalog catalog = LibApp.getInstance().getCurrentCatalog();
			showName = catalog.getResourcedValue(catalog.getProfilePartsMapping(), stayId, showName);
		}
		
		return showName;
	}
}
