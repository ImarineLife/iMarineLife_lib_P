package nl.imarinelife.lib.divinglog.sightings;

import java.util.ArrayList;
import java.util.List;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MarineLifeContentProvider;
import nl.imarinelife.lib.catalog.Catalog;
import nl.imarinelife.lib.fieldguide.db.FieldGuideEntry;
import android.net.Uri;

public class Sighting {

	public static class SightingData implements Cloneable {
		public String sightingValue;
		public List<String> csCheckedValues;
		public String remarks;
		// showvalues - only put in here so that removing an app does not hinder
		// the
		// showing of good names.
		// the values have to be changed whenever there is a change of locality
		public String showSightingValue = null;
		public List<String> showCsCheckedValues = null;

		public SightingData(String sightingValue, List<String> csCheckedValues,
				String remarks) {
			Catalog currentCatalog = LibApp.getInstance().getCurrentCatalog();
			this.sightingValue = sightingValue;
			this.showSightingValue = currentCatalog.getResourcedValue(currentCatalog.getValuesMapping(), sightingValue, null);
			this.csCheckedValues = csCheckedValues;
			this.showCsCheckedValues = getResourcedCheckedValues(currentCatalog.getName(), csCheckedValues);
			this.remarks = remarks;
		}

		public void setSightingValue(String sightingValue) {
			this.sightingValue = sightingValue;
			Catalog currentCatalog = LibApp.getInstance().getCurrentCatalog();
			this.showSightingValue = currentCatalog.getResourcedValue(currentCatalog.getValuesMapping(), sightingValue, null);
			
		}

		public void setCsCheckedValues(List<String> csCheckedValues) {
			this.csCheckedValues = csCheckedValues;
			Catalog currentCatalog = LibApp.getInstance().getCurrentCatalog();
			this.showCsCheckedValues = getResourcedCheckedValues(currentCatalog.getName(), csCheckedValues);
		}

				@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("sightingValue["+sightingValue+"]");
			builder.append("showSightingValue["+showSightingValue+"]");
			builder.append("csCheckedValues["+csCheckedValues+"]");
			builder.append("showCsCheckedValues["+showCsCheckedValues+"]");
			builder.append("remarks["+remarks+"]");
			
			return builder.toString();
		}
		
		

	}

	
	
	
	protected Sighting clone() {
		return new Sighting(fieldguide_id, diveNr, catalog, group_name,
				common_name, orderNr, data.sightingValue, data.csCheckedValues,
				data.showSightingValue, data.showCsCheckedValues, data.remarks, fg_entry);
	}

	public int diveNr = 0;
	// this combination allows the selection of the FieldGuideEntry (for
	// obtaining description, l_pic_ref
	public int fieldguide_id = 0;
	public String catalog = null;
	// rest
	public int orderNr = 0;
	public SightingData data = new SightingData(null, null, null);
	// public String active = null;
	public String group_name = null;
	public String common_name = null;
	// FieldGuideEntry
	public FieldGuideEntry fg_entry = null;
	
	public static final String ID = "SightingId";
	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ MarineLifeContentProvider.getAuthority() + "/"
			+ MarineLifeContentProvider.NAME_SIGHTING);
	public static final Uri CONTENT_URI_DIVE_FLESHEDOUT = Uri
			.parse("content://" + MarineLifeContentProvider.getAuthority()
					+ "/" + MarineLifeContentProvider.NAME_SIGHTINGS_FLESHEDOUT);
	public static final Uri CONTENT_URI_DIVE_FLESHEDOUT_FILTERED = Uri
			.parse("content://"
					+ MarineLifeContentProvider.getAuthority()
					+ "/"
					+ MarineLifeContentProvider.NAME_SIGHTINGS_FLESHEDOUT_FILTERED);
	public static final Uri CONTENT_URI_DIVE_ASIS = Uri.parse("content://"
			+ MarineLifeContentProvider.getAuthority() + "/"
			+ MarineLifeContentProvider.NAME_SIGHTINGS_ASIS);
	public static final Uri CONTENT_URI_DIVE_ASIS_FILTERED = Uri
			.parse("content://" + MarineLifeContentProvider.getAuthority()
					+ "/"
					+ MarineLifeContentProvider.NAME_SIGHTINGS_ASIS_FILTERED);

	// for creating a new entry and storing it
	public Sighting(int fieldguide_id, int diveNr, String catalog,
			String group_name, String common_name, int orderNr,
			String sightingValue, List<String> csCheckedValues,
			String remarks) {
		super();
		this.fieldguide_id = fieldguide_id;
		this.diveNr = diveNr;
		this.catalog = catalog;
		this.orderNr = orderNr;
		this.data.sightingValue = sightingValue;
		this.data.csCheckedValues = csCheckedValues;
		this.data.remarks = remarks;
		this.group_name = group_name;
		this.common_name = common_name;
		// this.active = "Y";
	}

	// for getting a entry from the database
	public Sighting(int fieldguide_id, int diveNr, String catalog,
			String group_name, String common_name, int orderNr,
			String sightingValue, List<String> csCheckedValues,
			String showSightingValue, List<String> showCsCheckedValues,
			String remarks, FieldGuideEntry entry) {
		this(fieldguide_id, diveNr, catalog, group_name, common_name, orderNr,
				sightingValue, csCheckedValues, remarks);
		// this.active = active;
		this.data.showSightingValue = showSightingValue;
		this.data.showCsCheckedValues = showCsCheckedValues;
		this.fg_entry = entry;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("catalog[" + catalog + "]");
		builder.append("fieldguide_id[" + fieldguide_id + "]");
		builder.append("divenr[" + diveNr + "]");
		builder.append("orderNr[" + orderNr + "]");
		builder.append("sightingValue[" + data.sightingValue + "]");
		builder.append("showSightingValue[" + data.showSightingValue + "]");
		builder.append("csCheckedValue[" + data.csCheckedValues + "]");
		builder.append("showCsCheckedValue[" + data.showCsCheckedValues + "]");
		builder.append("remarks[" + data.remarks + "]");
		builder.append(fg_entry);
		
		// builder.append("active[" + active + "]");
		return builder.toString();
	}

	public String getCsCheckedValues() {
		StringBuilder builder = new StringBuilder();
		if (data.csCheckedValues == null) {
			return null;
		} else {
			boolean first = true;
			for (String value : data.csCheckedValues) {
				if (!first) {
					builder.append(",");
				}
				first = false;
				builder.append(value);
			}
		}
		return builder.toString();
	}
	
	public static String getCsStringFromList(List<String> values) {
		StringBuilder builder = new StringBuilder();
		if (values == null) {
			return null;
		} else {
			boolean first = true;
			for (String value : values) {
				if (!first) {
					builder.append(",");
				}
				first = false;
				builder.append(value);
			}
		}
		return builder.toString();
	}
	
	public String getCsShowCheckedValues() {
		StringBuilder builder = new StringBuilder();
		if (data.showCsCheckedValues == null) {
			return null;
		} else {
			boolean first = true;
			for (String value : data.showCsCheckedValues) {
				if (!first) {
					builder.append(",");
				}
				first = false;
				builder.append(value);
			}
		}
		return builder.toString();
	}

	
	public static ArrayList<String> getCheckedValuesFromCs(
			String csCheckedValues) {
		String[] checked = null;
		if (csCheckedValues != null && csCheckedValues.length() > 0) {
			checked = csCheckedValues.split(",");
		}
		if (checked != null && checked.length > 0) {
			ArrayList<String> checkedValues = new ArrayList<String>();
			for (String value : checked) {
				if (value.length() > 0) {
					checkedValues.add(value);
				}
			}
			return checkedValues;
		}
		return null;
	}
	
	public static String getResourcedSightingValue(String catalog, String sightingValue) {
		Catalog cat = LibApp.getInstance().getCurrentCatalog();
		return cat.getResourcedValue(catalog, cat.getValuesMapping(),
				sightingValue, null);
	}
	
	public static List<String> getResourcedCheckedValues(String catalog, List<String> checkedValues){
		ArrayList<String> showCheckedValues = new ArrayList<String>();
		if(checkedValues==null){
			return null;
		}
		for(String checkedValue:checkedValues){
			showCheckedValues.add(getResourcedSightingValue(catalog,checkedValue));
		}
		return showCheckedValues;
	}
}
