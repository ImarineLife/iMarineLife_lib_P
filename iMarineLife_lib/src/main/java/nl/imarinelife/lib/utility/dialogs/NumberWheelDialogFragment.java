package nl.imarinelife.lib.utility.dialogs;

import java.io.Serializable;

import nl.imarinelife.lib.utility.wheel.NumericWheelAdapter;
import nl.imarinelife.lib.utility.wheel.OnWheelChangedListener;
import nl.imarinelife.lib.utility.wheel.WheelView;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class NumberWheelDialogFragment extends DialogFragment implements OnWheelChangedListener {
	private static final String	TAG						= "NumberWheelDialogFr";

	public static final String	KEY_INT_CURRENTNUBMER	= "currentNumber";
	public static final String	KEY_INT_MINVALUE		= "minvalue";
	public static final String	KEY_INT_MAXVALUE		= "maxvalue";
	public static final String	KEY_INT_NRSSHOWN		= "nrsshown";
	public static final String	KEY_INT_FORMAT			= "format";
	public static final String	KEY_INT_LAYOUT			= "layoutId";
	public static final String	KEY_INT_WHEEL			= "wheelId";
	public static final String	KEY_INT_CHOOSE			= "choosebuttonId";
	public static final String	KEY_INT_CANCEL			= "cancelbuttonId";
	public static final String	KEY_INT_STYLE			= "style";
	public static final String	KEY_INT_THEME			= "theme";

	WheelView					numberwheel				= null;
	Button						choice					= null;
	Button						cancel					= null;
	Integer						currentNumber			= null;
	Integer						minValue				= null;
	Integer						maxValue				= null;
	Integer						nrShown					= 3;
	String						format					= null;
	int							layoutId				= 0;
	int							choosebuttonId			= 0;
	int							cancelbuttonId			= 0;
	int							wheelId					= 0;
	int							style					= 0;
	int							theme					= 0;
	OnCompleteListener			listener				= null;

	public static NumberWheelDialogFragment newInstance(int currentNumber, int minValue, int maxValue, int nrshown,
			String format, int layoutId, int wheelId, int choosebuttonId, int cancelbuttonId, int style, int theme) {

		NumberWheelDialogFragment f = new NumberWheelDialogFragment();

		Bundle args = new Bundle();
		args.putInt(KEY_INT_CURRENTNUBMER,
			currentNumber);
		args.putInt(KEY_INT_MINVALUE,
			minValue);
		args.putInt(KEY_INT_MAXVALUE,
			maxValue);
		args.putInt(KEY_INT_NRSSHOWN,
			nrshown);
		args.putString(KEY_INT_FORMAT,
			format);
		args.putInt(KEY_INT_LAYOUT,
			layoutId);
		args.putInt(KEY_INT_WHEEL,
			wheelId);
		args.putInt(KEY_INT_CHOOSE,
			choosebuttonId);
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
		if(savedInstanceState!=null && !savedInstanceState.isEmpty()){
			initializeBundle(savedInstanceState);
		}else{
			initializeBundle(getArguments());
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if(savedInstanceState!=null && !savedInstanceState.isEmpty()){
			initializeBundle(savedInstanceState);
		}
		super.onActivityCreated(savedInstanceState);
	}
	
	private void initializeBundle(Bundle bundle) {
		currentNumber = bundle.getInt(KEY_INT_CURRENTNUBMER);
		Log.d(TAG, "initializeBundle["+currentNumber+"]");
		minValue = bundle.getInt(KEY_INT_MINVALUE);
		maxValue = bundle.getInt(KEY_INT_MAXVALUE);
		nrShown = bundle.getInt(KEY_INT_NRSSHOWN);
		format = bundle.getString(KEY_INT_FORMAT);
		layoutId = bundle.getInt(KEY_INT_LAYOUT);
		wheelId = bundle.getInt(KEY_INT_WHEEL);
		choosebuttonId = bundle.getInt(KEY_INT_CHOOSE);
		cancelbuttonId = bundle.getInt(KEY_INT_CANCEL);
		style = bundle.getInt(KEY_INT_STYLE);
		theme = bundle.getInt(KEY_INT_THEME);

		setStyle(style,
			theme);		
	}

	@Override
	public void onSaveInstanceState(Bundle args) {
		args.putInt(KEY_INT_CURRENTNUBMER,
			currentNumber);
		args.putInt(KEY_INT_MINVALUE,
			minValue);
		args.putInt(KEY_INT_MAXVALUE,
			maxValue);
		args.putInt(KEY_INT_NRSSHOWN,
			nrShown);
		args.putString(KEY_INT_FORMAT,
			format);
		args.putInt(KEY_INT_LAYOUT,
			layoutId);
		args.putInt(KEY_INT_WHEEL,
			wheelId);
		args.putInt(KEY_INT_CHOOSE,
			choosebuttonId);
		args.putInt(KEY_INT_CANCEL,
			cancelbuttonId);
		args.putInt(KEY_INT_STYLE,
			style);
		args.putInt(KEY_INT_THEME,
			theme);
		int nr = args.getInt(KEY_INT_CURRENTNUBMER);
		Log.d(TAG, "onSaveInstanceState["+nr+"]");
		
		super.onSaveInstanceState(args);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(savedInstanceState!=null && !savedInstanceState.isEmpty()){
			initializeBundle(savedInstanceState);
		}
		View view = inflater.inflate(layoutId,
			container,
			false);
		choice = (Button) view.findViewById(choosebuttonId);
		cancel = (Button) view.findViewById(cancelbuttonId);
		final NumericWheelAdapter adapter = new NumericWheelAdapter(getActivity(), minValue, maxValue, format);
		Log.d(TAG,
			"Current Number = [" + currentNumber + "]");
		numberwheel = (WheelView) view.findViewById(wheelId);
		numberwheel.setViewAdapter(adapter);
		numberwheel.setCurrentItem(currentNumber);
		String maxValueString = Integer.toString(maxValue);
		int length = maxValueString.length();
		numberwheel.setMinimumWidth(length * 30);
		numberwheel.setVisibleItems(nrShown);
		numberwheel.addChangingListener(this);

		choice.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onCompleteNumberWheel(Integer.parseInt(adapter.getItemText(numberwheel.getCurrentItem()).toString()));
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

	public interface OnCompleteListener extends Serializable {
		public void onCompleteNumberWheel(int value);
	}

	public void setOnCompleteListener(OnCompleteListener listener) {
		this.listener = listener;
	}

	@Override
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
		currentNumber=newValue;
		Log.d(TAG,"onChanged ["+currentNumber+"]");
		
	}

}
