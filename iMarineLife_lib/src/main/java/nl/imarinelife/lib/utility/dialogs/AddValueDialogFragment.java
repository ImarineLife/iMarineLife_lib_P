package nl.imarinelife.lib.utility.dialogs;

import java.io.Serializable;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class AddValueDialogFragment extends DialogFragment {
	@SuppressWarnings("unused")
	private static final String	TAG						= "AddValueDialogFragment";

	public static final String	KEY_STR_CURRENTVALUE	= "currentValue";
	public static final String	KEY_INT_LAYOUT			= "layoutId";
	public static final String	KEY_INT_EDITTEXT		= "editTextId";
	public static final String	KEY_INT_OK				= "okbuttonId";
	public static final String	KEY_INT_CANCEL			= "cancelbuttonId";
	public static final String	KEY_INT_STYLE			= "style";
	public static final String	KEY_INT_THEME			= "theme";

	EditText					editText				= null;
	Button						ok						= null;
	Button						cancel					= null;
	String						currentValue			= null;
	int							layoutId				= 0;
	int							okbuttonId				= 0;
	int							cancelbuttonId			= 0;
	int							edittextId				= 0;
	int							style					= 0;
	int							theme					= 0;

	OnOkListener				listener				= null;

	public static AddValueDialogFragment newInstance(String currentValue, int layoutId, int editTextId,
			int okbuttonId, int cancelbuttonId, int style, int theme) {

		AddValueDialogFragment f = new AddValueDialogFragment();

		Bundle args = new Bundle();
		args.putString(KEY_STR_CURRENTVALUE,
			currentValue);
		args.putInt(KEY_INT_LAYOUT,
			layoutId);
		args.putInt(KEY_INT_EDITTEXT,
			editTextId);
		args.putInt(KEY_INT_OK,
			okbuttonId);
		args.putInt(KEY_INT_CANCEL,
			cancelbuttonId);
		args.putInt(KEY_INT_STYLE,
			style);
		args.putInt(KEY_INT_THEME,
			theme);
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
		currentValue = bundle.getString(KEY_STR_CURRENTVALUE);
		layoutId = bundle.getInt(KEY_INT_LAYOUT);
		edittextId = bundle.getInt(KEY_INT_EDITTEXT);
		okbuttonId = bundle.getInt(KEY_INT_OK);
		cancelbuttonId = bundle.getInt(KEY_INT_CANCEL);
		style = bundle.getInt(KEY_INT_STYLE);
		theme = bundle.getInt(KEY_INT_THEME);

		setStyle(style,
			theme);
	}

	@Override
	public void onSaveInstanceState(Bundle args) {
		args.putString(KEY_STR_CURRENTVALUE,
			currentValue);
		args.putInt(KEY_INT_LAYOUT,
			layoutId);
		args.putInt(KEY_INT_EDITTEXT,
			edittextId);
		args.putInt(KEY_INT_OK,
			okbuttonId);
		args.putInt(KEY_INT_CANCEL,
			cancelbuttonId);
		args.putInt(KEY_INT_STYLE,
			style);
		args.putInt(KEY_INT_THEME,
			theme);
		super.onSaveInstanceState(args);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			initializeBundle(savedInstanceState);
		}
		View view = inflater.inflate(layoutId,
			container,
			false);
		ok = (Button) view.findViewById(okbuttonId);
		cancel = (Button) view.findViewById(cancelbuttonId);

		editText = (EditText) view.findViewById(edittextId);
		editText.setText(currentValue);

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
		newFragment.show(ft,
			"adddialog");
		//ft.commit(); // ??
	}

}
