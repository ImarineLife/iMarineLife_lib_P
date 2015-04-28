package nl.imarinelife.lib.fieldguide.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.MarineLifeContentProvider;
import nl.imarinelife.lib.catalog.Catalog;
import nl.imarinelife.lib.utility.ExpansionFileAccessHelper;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

public class FieldGuideEntry {

	public static String TAG = "FieldGuideEntry";
	public int id = 0;
	public String catalog = null;
	public String latinName = null;
	private String commonName = null;
	private String groupName = null;
	private String description = null;
	private String checkValues = null;
	// showvalues - only put in here so that removing an app does not hinder the
	// showing of good names.
	// the values have to be changed whenever there is a change of locality
	private String showCommonName = null;
	private String showGroupName = null;
	private String showCheckValues = null;
	public int ordernr = 0;

	public static final String ID = "FieldGuideEntryId";
	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ MarineLifeContentProvider.getAuthority() + "/"
			+ MarineLifeContentProvider.NAME_FIELDGUIDE);
	public static final Uri CONTENT_URI_FILTER = Uri.parse("content://"
			+ MarineLifeContentProvider.getAuthority() + "/"
			+ MarineLifeContentProvider.NAME_FIELDGUIDE_FILTERED);

	public FieldGuideEntry() {
	};

	public FieldGuideEntry(int id, String catalog, String latinName,
			String groupName, String commonName, String description,
			String checkValues, int ordernr) {
		super();
		this.id = id;
		this.catalog = catalog;
		this.latinName = latinName;
		this.setCommonName(commonName);
		this.setGroupName(groupName);
		this.setDescription(description);
		this.setCheckValues(checkValues);
		this.setShowGroupName(getResourcedGroupName(catalog, groupName));
		this.setShowCommonName(getResourcedCommonName(catalog, commonName));
		this.setShowCheckValues(getResourcedCheckValues());
		this.ordernr = ordernr;
	}

	public FieldGuideEntry(int id, String catalog, String latinName,
			String groupName, String commonName, String description,
			String checkValues, int ordernr, String showGroupName,
			String showCommonName, String showCheckValues) {
		super();
		this.id = id;
		this.catalog = catalog;
		this.latinName = latinName;
		this.setCommonName(commonName);
		this.setGroupName(groupName);
		this.setDescription(description);
		this.setCheckValues(checkValues);
		this.setShowGroupName(showGroupName);
		this.setShowCommonName(showCommonName);
		this.setShowCheckValues(showCheckValues);
		this.ordernr = ordernr;
	}

	public long getId() {
		return (long) id;
	}

	public List<String> getCheckValuesAsList() {
		String resourcedCheckValues = getShowCheckValues();
		if (resourcedCheckValues != null) {
			String[] array = resourcedCheckValues.split(",");
			return Arrays.asList(array);
		} else {
			return null;
		}

	}

	public static String getSpicRefAsAssetFromFieldGuideId(int id) {
		return "smallpics/s" + id + ".png";
	}

	public static String getSpicRefAsAssetFromFileName(String fileName) {
		return "smallpics/" + fileName;
	}

	public String getSpicRefAsAsset() {
		return "smallpics/s" + id + ".png";
	}

	public static Bitmap getSpicBitmapAsAssetFromFieldGuideId(int id) {
		AssetManager manager = MainActivity.me.getAssets();
		return getSpicBitmapAsAssetFromFieldGuideId(id, manager);
	}

	public static Bitmap getSpicBitmapAsAssetFromFieldGuideId(int id,
			AssetManager manager) {
		try {
			InputStream is = manager
					.open(getSpicRefAsAssetFromFieldGuideId(id));
			return BitmapFactory.decodeStream(is);
		} catch (IOException e) {
			Log.d(TAG, "getSpicBitmapAsAssetFromFieldGuideId[" + id
					+ "] not found");
		}
		return null;

	}

	public static Bitmap getSpicBitmapAsAssetFromFileName(String fileName) {
		AssetManager manager = MainActivity.me.getAssets();
		return getSpicBitmapAsAssetFromFileName(fileName, manager);
	}

	public static Bitmap getSpicBitmapAsAssetFromFileName(String fileName,
			AssetManager manager) {
		try {
			InputStream is = manager
					.open(getSpicRefAsAssetFromFileName(fileName));
			return BitmapFactory.decodeStream(is);
		} catch (IOException e) {
			Log.d(TAG, "getSpicBitmapAsAssetFromFileName[" + fileName + "]", e);
		}
		return null;

	}

	public Bitmap getSpicBitmapAsAsset() {
		return getSpicBitmapAsAssetFromFieldGuideId(id);
	}

	public static String getLpicRefFromFieldGuideId(int id) {
		return "largepictures/l" + id + ".png";
	}

	public String getLpicRef() {
		return "largepictures/l" + id + ".png";
	}

	public static Bitmap getLpicBitmapAsExpansionFromFieldGuideId(int id) {
		return ExpansionFileAccessHelper
				.getBitMap(getLpicRefFromFieldGuideId(id));
	}

	public Bitmap getLpicBitmapAsExpansion() {
		return getLpicBitmapAsExpansionFromFieldGuideId(id);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.catalog + "");
		buffer.append(" ");
		buffer.append(id + "");
		buffer.append(" ");
		buffer.append(getShowCommonName());
		buffer.append(" ");
		buffer.append(getShowGroupName());
		return buffer.toString();
	}

	public String getResourcedCheckValues() {
		return getResourcedCheckValuesForCurrentCatalog(catalog, getCheckValues());
	}

	public static String getResourcedCheckValuesForCurrentCatalog(String entryCatalog,
			String checkValues) {
		if (checkValues != null) {
			String[] array = checkValues.split(",");
			StringBuffer result = new StringBuffer();
			boolean first = true;
			for (int i = 0; i < array.length; i++) {
				String value = null;

				Catalog cat = LibApp.getInstance().getCurrentCatalog();
				value = cat.getResourcedValue(entryCatalog,
						cat.getValuesMapping(), array[i], null);

				if (!first) {
					result.append(",");
				}
				result.append(value);
			}
			return result.toString();
		} else {
			return null;
		}
	}

	

	public static List<String> getResourcedCheckValuesForCurrentCatalog(String entryCatalog,
			List<String> checkValues) {
		Log.d(TAG,"getResourcedCheckValuesForCurrentCatalog in "+checkValues);
		if (checkValues != null && checkValues.size() > 0) {
			List<String> result = new ArrayList<String>();
			for (String checkValue : checkValues) {
				Catalog cat = LibApp.getInstance().getCurrentCatalog();
				String value = cat.getResourcedValue(entryCatalog,
						cat.getValuesMapping(), checkValue, null);
				result.add(value);
			}
			Log.d(TAG,"getResourcedCheckValuesForCurrentCatalog out "+result);
			return result;
		} else {
			Log.d(TAG,"getResourcedCheckValuesForCurrentCatalog out null");
			return null;
		}
	}

	public String getCheckValues() {
		return checkValues;
	}

	public void setCheckValues(String checkValues) {
		this.checkValues = checkValues;
	}

	public String getCommonName() {
		return commonName;
	}

	public static String getResourcedCommonName(String catalog, String commonName) {
		Catalog cat = LibApp.getInstance().getCurrentCatalog();
		return cat.getResourcedValue(catalog, cat.getCommonIdMapping(),
				commonName, null);
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getGroupName() {
		return groupName;
	}

	public static String getResourcedGroupName(String catalog, String groupName) {
		Catalog cat = LibApp.getInstance().getCurrentCatalog();
		return cat.getResourcedValue(catalog, cat.getGroupIdMapping(),
				groupName, null);
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getDescription() {
		return description;
	}

	public String getResourcedDescription() {
		Catalog cat = LibApp.getInstance().getCurrentCatalog();
		return cat.getResourcedValue(catalog, cat.getDescriptionIdMapping(),
				description, null);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getShowCommonName() {
		return showCommonName;
	}

	public void setShowCommonName(String showCommonName) {
		this.showCommonName = showCommonName;
	}

	public String getShowGroupName() {
		return showGroupName;
	}

	public void setShowGroupName(String showGroupName) {
		this.showGroupName = showGroupName;
	}

	public String getShowCheckValues() {
		return showCheckValues;
	}

	public void setShowCheckValues(String showCheckValues) {
		this.showCheckValues = showCheckValues;
	}

}
