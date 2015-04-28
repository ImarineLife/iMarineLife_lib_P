package nl.imarinelife.lib.divinglog.db.dive;

import java.text.SimpleDateFormat;
import java.util.Date;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.TextView;

public class DiveSimpleCursorAdapter extends SimpleCursorAdapter {

	private final static String				TAG			= "DiveSimpleCursorAdapter";
	public static String					ID			= "id";

	/*
	 * private FilterCursorWrapper selection = null; boolean isShowingSelection
	 * = false; String selectionConstraint = "";
	 */
	public static final SimpleDateFormat	dateformat	= new SimpleDateFormat("dd-MM-yyyy");
	public static final SimpleDateFormat	timeformat	= new SimpleDateFormat("HH:mm");

	public DiveSimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flag) {
		super(context, layout, c, from, to, flag);
		
		ViewBinder binder = new ViewBinder() {
			@SuppressWarnings("deprecation")
			@Override
			public boolean setViewValue(android.view.View view, Cursor cursor, int columnIndex) {
				Log.d(TAG,
					"setViewValue for columnIndex[" + columnIndex + "]");
				String catalog = cursor.getString(DiveDbHelper.KEY_CATNAME_CURSORLOC);
				
				boolean belongsToCurrentCatalog = cursor.getString(DiveDbHelper.KEY_CATNAME_CURSORLOC).equals(LibApp.getCurrentCatalogName());
				switch (columnIndex) {
					case DiveDbHelper.KEY_DIVENR_CURSORLOC:
						Log.d(TAG,
							"setViewValue for columnIndex[" + columnIndex + "] divenr");
						TextView diveNrView = (TextView) view;
						if (!belongsToCurrentCatalog) {
							diveNrView.setTextColor(Color.GRAY);
							diveNrView.setText(cursor.getInt(columnIndex) + ":");
						} else {
							SpannableString spanString = new SpannableString(cursor.getInt(columnIndex) + ":");
							spanString.setSpan(new StyleSpan(Typeface.BOLD),
								0,
								spanString.length(),
								0);
							diveNrView.setText(spanString);
							diveNrView.setTextColor(Color.BLACK);
						}
						return true;
					case DiveDbHelper.KEY_LOCATIONNAME_CURSORLOC:
						TextView locationView = (TextView) view;
						// TODO: remove suffixes!!
						String showValue = cursor.getString(columnIndex);
						
						if (!belongsToCurrentCatalog) {
							String catalogName = "";
							String packagename = "nl.imarinelife." + catalog.toLowerCase();
							try {
								Context otherContext = MainActivity.me.createPackageContext(packagename,
									0);
								AssetManager manager = otherContext.getAssets();
								String[] files = manager.list("catalog");
								if (files != null && files.length > 0) {
									catalogName = " (" + files[0].replace("_",
										" ") + ")";
								}else{
									catalogName = " ("+catalog+")";
								}
							} catch (Exception e) {
								// too bad.. just ignore
								Log.e(TAG,
									"problem getting asset from package ["+packagename+"] (ignored)");
							}
							locationView.setTextColor(Color.GRAY);
							locationView.setText(showValue + catalogName);
						} else {
							
							SpannableString spanString = new SpannableString(showValue);
							spanString.setSpan(new StyleSpan(Typeface.BOLD),
								0,
								spanString.length(),
								0);
							locationView.setText(spanString);
							locationView.setTextColor(Color.BLACK);
						}
						return true;
					case DiveDbHelper.KEY_DATE_CURSORLOC:
						TextView dateView = (TextView) view;
						Date date = new Date(cursor.getLong(columnIndex));
						if (!belongsToCurrentCatalog) {
							dateView.setTextColor(Color.GRAY);
							dateView.setText(dateformat.format(date));
						} else {
							SpannableString spanString = new SpannableString(dateformat.format(date));
							spanString.setSpan(new StyleSpan(Typeface.BOLD),
								0,
								spanString.length(),
								0);
							dateView.setText(spanString);
							dateView.setTextColor(Color.BLACK);
						}

						Log.d(TAG,
							"setViewValue for columnIndex[" + columnIndex + "] date [" + cursor.getLong(columnIndex)
									+ ":" + date.toGMTString() + "] formatted [" + dateformat.format(date) + "]");
						return true;
					case DiveDbHelper.KEY_TIME_CURSORLOC:
						TextView timeView = (TextView) view;
						Date time = new Date(cursor.getLong(columnIndex));
						if (!belongsToCurrentCatalog) {
							timeView.setTextColor(Color.GRAY);
							timeView.setText(timeformat.format(time));
						} else {
							SpannableString spanString = new SpannableString(timeformat.format(time));
							spanString.setSpan(new StyleSpan(Typeface.BOLD),
								0,
								spanString.length(),
								0);
							timeView.setText(spanString);
							timeView.setTextColor(Color.BLACK);
						}

						Log.d(TAG,
							"setViewValue for columnIndex[" + columnIndex + "] time [" + cursor.getLong(columnIndex)
									+ ":" + time.toGMTString() + "] formatted [" + timeformat.format(time) + "]");
						return true;
				}
				return false;
			};
		};
		setViewBinder(binder);

	}
	
	
}
