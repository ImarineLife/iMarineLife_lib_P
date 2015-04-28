package nl.imarinelife.lib.divinglog.db.res;

import java.io.Serializable;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.catalog.Catalog;
import android.content.res.Resources;
import android.util.Log;

public class Location implements Serializable {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private String catName = null;    // MOO3
	private String catCode = null;	  // 103 (from loc103) - for sorting purposes
	private String locationId = null;   // loc103 // moet nog vertaald worden naar resourceid waardoor de naam taalspecifiek kan zijn
	private String showLocationName = null;  // since version 2 also in db, but will be updated on change of locality
	
	public static String TAG = "Location";
	
	public Location(String catNaam, String catCode, String locationId, String showLocationName) {
		super();
		this.setCatName(catNaam);
		this.setCatCode(catCode);
		this.setLocationId(locationId);
		this.setShowLocationName(showLocationName);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("catName["+getCatName()+"]");
		builder.append("catCode["+getCatCode()+"]");
		builder.append("locationId["+getLocationId()+"]");
		builder.append("showLocationName["+getShowLocationName()+"]");
		return builder.toString();
	}

	public String getLocationId() {
		return locationId;
	}

	public String getShowLocationName() {
		/*Catalog catalog = LibApp.getInstance().getCurrentCatalog();
		String locationName = catalog.getResourcedValue(catName, catalog.getLocationNamesMapping(), locationId);
		*/
		return showLocationName;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	public void setShowLocationName(String showLocationName) {
		this.showLocationName = showLocationName;
	}
	
	public String getCatCode() {
		return catCode;
	}

	public void setCatCode(String catCode) {
		this.catCode = catCode;
	}

	public String getCatName() {
		return catName;
	}

	public void setCatName(String catName) {
		this.catName = catName;
	}
}
