package nl.imarinelife.lib.utility;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TextViewPreference extends DialogPreference {

	private static final String	TAG	= "TextViewPreference";
	/**
	 * The edit text shown in the dialog.
	 */
	private TextView			mtextView;

	public TextViewPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Log.d(TAG,
			"constructor");

		mtextView = new TextView(context, attrs);

		// Give it an ID so it can be saved/restored
		mtextView.setId(1);

		/*
		 * The preference framework and view framework both have an 'enabled'
		 * attribute. Most likely, the 'enabled' specified in this XML is for
		 * the preference framework, but it was also given to the view
		 * framework. We reset the enabled state.
		 */
		mtextView.setEnabled(true);
	}

	public TextViewPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 2);
	}

	public TextViewPreference(Context context) {
		this(context, null);
	}

	/**
	 * 
	 * @return The text.
	 */
	public String getText() {
		Log.d(TAG,
			"getText");
		return LibApp.getCurrentCatalogPreferenceText(getKey());
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		Log.d(TAG,
			"onBindView");

		setDialogLayoutResource(R.layout.about);
		setDialogTitle(null);
		setDialogMessage(getText());
	}

	/**
	 * Adds the text widget of this preference to the dialog's view.
	 * 
	 * @param dialogView
	 *            The dialog view.
	 */
	protected void onAddtextViewToDialogView(View dialogView, TextView text) {
		ViewGroup container = (ViewGroup) dialogView.findViewById(3);
		if (container != null) {
			container.addView(text,
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getString(index);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
	}

	@Override
	public boolean shouldDisableDependents() {
		return true;
	}

	/**
	 * Returns the {@link TextView} widget that will be shown in the dialog.
	 * 
	 * @return The {@link TextView} widget that will be shown in the dialog.
	 */
	public TextView getTextView() {
		return mtextView;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		return super.onSaveInstanceState();
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
		return;
	}


}