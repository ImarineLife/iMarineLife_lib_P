package nl.imarinelife.lib.utility.dialogs;

import java.io.Serializable;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EditTextDialogFragment extends DialogFragment {
	private static final String TAG = "EditTextDialogFragment";

	public static final String KEY_TEXT_CURRENTTEXT = "currentText";
	public static final String KEY_TEXT_LABELTEXT = "labelText";
	public static final String KEY_INT_LAYOUT = "layoutId";
	public static final String KEY_INT_LABEL = "labelId";
	public static final String KEY_INT_EDITTEXT = "editTextId";

	public static final String KEY_INT_OK = "okbuttonId";
	public static final String KEY_INT_CANCEL = "cancelbuttonId";
	public static final String KEY_INT_MINIMUMHEIGHT = "minimumHeight";
	public static final String KEY_INT_STYLE = "style";
	public static final String KEY_INT_THEME = "theme";

	Button ok = null;
	Button cancel = null;
	String currentText = null;
	String labelText = null;
	EditText editText = null;
	TextView label = null;
	int minimumHeight = 10;

	int layoutId = 0;
	int labelId = 0;
	int editTextId = 0;
	int okbuttonId = 0;
	int cancelbuttonId = 0;
	int style = 0;
	int theme = 0;

	OnOkListener listener = null;

	public static EditTextDialogFragment newInstance(String labelText,
			String currentText, int layoutId, int labelId, int editTextId,
			int okbuttonId, int cancelbuttonId, int minimumHeight, int style,
			int theme, boolean showLabelText) {

		Log.d(TAG,"labelText ["+labelText+"]["+showLabelText+"] ");
		EditTextDialogFragment f = new EditTextDialogFragment();
		if (!showLabelText)
			labelText = null;

		Bundle args = new Bundle();
		args.putString(KEY_TEXT_CURRENTTEXT, currentText);
		args.putString(KEY_TEXT_LABELTEXT, labelText);
		args.putInt(KEY_INT_LAYOUT, layoutId);
		args.putInt(KEY_INT_LABEL, labelId);
		args.putInt(KEY_INT_EDITTEXT, editTextId);
		args.putInt(KEY_INT_OK, okbuttonId);
		args.putInt(KEY_INT_CANCEL, cancelbuttonId);
		args.putInt(KEY_INT_MINIMUMHEIGHT, minimumHeight);
		args.putInt(KEY_INT_STYLE, style);
		args.putInt(KEY_INT_THEME, theme);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			initializeBundle(savedInstanceState);
		} else {
			initializeBundle(getArguments());
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			initializeBundle(savedInstanceState);
		}
		super.onActivityCreated(savedInstanceState);
	}

	private void initializeBundle(Bundle bundle) {
		currentText = bundle.getString(KEY_TEXT_CURRENTTEXT);
		Log.d(TAG, "initializeBundle[" + currentText + "]");
		layoutId = bundle.getInt(KEY_INT_LAYOUT);
		labelId = bundle.getInt(KEY_INT_LABEL);
		labelText = bundle.getString(KEY_TEXT_LABELTEXT);
		editTextId = bundle.getInt(KEY_INT_EDITTEXT);
		okbuttonId = bundle.getInt(KEY_INT_OK);
		cancelbuttonId = bundle.getInt(KEY_INT_CANCEL);
		minimumHeight = bundle.getInt(KEY_INT_MINIMUMHEIGHT);
		style = bundle.getInt(KEY_INT_STYLE);
		theme = bundle.getInt(KEY_INT_THEME);

		setStyle(style, theme);
	}

	@Override
	public void onSaveInstanceState(Bundle args) {
		args.putString(KEY_TEXT_CURRENTTEXT, currentText);
		args.putString(KEY_TEXT_LABELTEXT, labelText);
		args.putInt(KEY_INT_LAYOUT, layoutId);
		args.putInt(KEY_INT_LABEL, labelId);
		args.putInt(KEY_INT_EDITTEXT, editTextId);
		args.putInt(KEY_INT_OK, okbuttonId);
		args.putInt(KEY_INT_CANCEL, cancelbuttonId);
		args.putInt(KEY_INT_MINIMUMHEIGHT, minimumHeight);
		args.putInt(KEY_INT_STYLE, style);
		args.putInt(KEY_INT_THEME, theme);
		Log.d(TAG, "onSaveInstanceState[" + currentText + "]");

		super.onSaveInstanceState(args);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			initializeBundle(savedInstanceState);
		}
		View view = inflater.inflate(layoutId, container, false);
		ok = (Button) view.findViewById(okbuttonId);
		cancel = (Button) view.findViewById(cancelbuttonId);

		if (labelText != null) {
			Log.d(TAG,"setting label ["+labelText+"]");
			label = (TextView) view.findViewById(labelId);
			label.setText(labelText);
			label.setVisibility(TextView.VISIBLE);
		}else{
			Log.d(TAG,"no setting label");
		}

		editText = (EditText) view.findViewById(editTextId);
		editText.setText(currentText);
		editText.setMinimumHeight(minimumHeight);

		ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onOk(editText.getText().toString());
				}
				dismiss();
			}
		});
		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		return view;

	}

	public interface OnOkListener extends Serializable {
		public void onOk(String value);
	}

	public void setOnOkListener(OnOkListener listener) {
		this.listener = listener;
	}

	void showDialog(DialogFragment newFragment) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		newFragment.show(ft, "adddialog");
		// ft.commit(); // ??
	}
}
