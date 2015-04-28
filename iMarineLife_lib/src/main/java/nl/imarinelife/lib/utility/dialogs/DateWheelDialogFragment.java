package nl.imarinelife.lib.utility.dialogs;

import java.io.Serializable;
import java.util.Calendar;

import nl.imarinelife.lib.utility.wheel.DateWheel;
import nl.imarinelife.lib.utility.wheel.DateWheel.IDateChangedListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class DateWheelDialogFragment extends DialogFragment implements IDateChangedListener {
	private static final String	TAG					= "DateWheelDialogFragment";

	public static final String	KEY_SER_CURRENTDATE	= "currentdate";
	public static final String	KEY_INT_MINYEAR		= "minyear";
	public static final String	KEY_INT_MAXYEAR		= "maxyear";
	public static final String	KEY_INT_NRSSHOWN	= "nrsshown";
	public static final String	KEY_INT_LAYOUT		= "layoutId";
	public static final String	KEY_INT_DATEPICKER	= "datepickerId";
	public static final String	KEY_INT_CHOOSE		= "choosebuttonId";
	public static final String	KEY_INT_CANCEL		= "cancelbuttonId";
	public static final String	KEY_INT_CURRENT		= "currentbuttonId";
	public static final String	KEY_INT_STYLE		= "style";
	public static final String	KEY_INT_THEME		= "theme";

	DateWheel					datewheel			= null;
	Button						choice				= null;
	Button						cancel				= null;
	Button						current				= null;
	Calendar					currentDate			= null;
	Integer						minYear				= null;
	Integer						maxYear				= null;
	Integer						nrShown				= 3;
	int							layoutId			= 0;
	int							choosebuttonId		= 0;
	int							cancelbuttonId		= 0;
	int							currentbuttonId		= 0;
	int							datepickerId		= 0;
	int							style				= 0;
	int							theme				= 0;
	OnDateCompleteListener		listener			= null;

	public static DateWheelDialogFragment newInstance(Calendar currentDate, int minyear, int maxyear, int nrsshown,
			int layoutId, int datepickerId, int choosebuttonId, int cancelbuttonId, int currentbuttonId, int style,
			int theme) {

		DateWheelDialogFragment f = new DateWheelDialogFragment();

		Bundle args = new Bundle();
		args.putSerializable(KEY_SER_CURRENTDATE,
			currentDate);
		args.putInt(KEY_INT_MINYEAR,
			minyear);
		args.putInt(KEY_INT_MAXYEAR,
			maxyear);
		args.putInt(KEY_INT_NRSSHOWN,
			nrsshown);
		args.putInt(KEY_INT_LAYOUT,
			layoutId);
		args.putInt(KEY_INT_DATEPICKER,
			datepickerId);
		args.putInt(KEY_INT_CHOOSE,
			choosebuttonId);
		args.putInt(KEY_INT_CANCEL,
			cancelbuttonId);
		args.putInt(KEY_INT_CURRENT,
			currentbuttonId);
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
		currentDate = (Calendar) bundle.getSerializable(KEY_SER_CURRENTDATE);
		minYear = bundle.getInt(KEY_INT_MINYEAR);
		maxYear = bundle.getInt(KEY_INT_MAXYEAR);
		nrShown = bundle.getInt(KEY_INT_NRSSHOWN);
		layoutId = bundle.getInt(KEY_INT_LAYOUT);
		datepickerId = bundle.getInt(KEY_INT_DATEPICKER);
		choosebuttonId = bundle.getInt(KEY_INT_CHOOSE);
		cancelbuttonId = bundle.getInt(KEY_INT_CANCEL);
		currentbuttonId = bundle.getInt(KEY_INT_CURRENT);
		style = bundle.getInt(KEY_INT_STYLE);
		theme = bundle.getInt(KEY_INT_THEME);

		setStyle(style,
			theme);
		
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		
		outState.putSerializable(KEY_SER_CURRENTDATE,
			currentDate);
		outState.putInt(KEY_INT_MINYEAR,
			minYear);
		outState.putInt(KEY_INT_MAXYEAR,
			maxYear);
		outState.putInt(KEY_INT_NRSSHOWN,
			nrShown);
		outState.putInt(KEY_INT_LAYOUT,
			layoutId);
		outState.putInt(KEY_INT_DATEPICKER,
			datepickerId);
		outState.putInt(KEY_INT_CHOOSE,
			choosebuttonId);
		outState.putInt(KEY_INT_CANCEL,
			cancelbuttonId);
		outState.putInt(KEY_INT_CURRENT,
			currentbuttonId);
		outState.putInt(KEY_INT_STYLE,
			style);
		outState.putInt(KEY_INT_THEME,
			theme);
		Log.d(TAG, "onSaveInstanceState["+outState.toString()+"]");

		super.onSaveInstanceState(outState);
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
		current = (Button) view.findViewById(currentbuttonId);

		datewheel = (DateWheel) view.findViewById(datepickerId);
		datewheel.setDay(currentDate.get(Calendar.DAY_OF_MONTH));
		datewheel.setMonth(currentDate.get(Calendar.MONTH) + 1);
		datewheel.setYear(currentDate.get(Calendar.YEAR));
		datewheel.setMinMaxYears(minYear,
			maxYear);
		datewheel.setVisibleItems(nrShown);
		datewheel.addDateChangedListener(this);
		
		choice.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				choose();
			}
		});
		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		current.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Calendar cal = Calendar.getInstance();
				datewheel.setYear(cal.get(Calendar.YEAR));
				datewheel.setMonth(cal.get(Calendar.MONTH)+1);
				datewheel.setDay(cal.get(Calendar.DAY_OF_MONTH));
				currentDate=cal;
			}
		});

		return view;
	}

	protected void choose() {
		if (listener != null) {
			listener.onCompleteDatePicker(currentDate.get(Calendar.YEAR),
				currentDate.get(Calendar.MONTH),
				currentDate.get(Calendar.DAY_OF_MONTH));
		}
		dismiss();
	}

	public interface OnDateCompleteListener extends Serializable {
		public void onCompleteDatePicker(int year, int month, int day);
	}

	public void setOnDateCompleteListener(OnDateCompleteListener listener) {
		this.listener = listener;
	}

	@Override
	public void onChanged(DateWheel datewheel, int oldDay, int oldMonth, int oldYear, int day, int month, int year) {
		currentDate.set(datewheel.getYear(),
			datewheel.getMonth() - 1,
			datewheel.getDay());
	}

}
