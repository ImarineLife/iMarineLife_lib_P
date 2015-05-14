package nl.imarinelife.lib.utility.dialogs;

import java.io.Serializable;
import java.util.Calendar;

import nl.imarinelife.lib.utility.wheel.TimeWheel;
import nl.imarinelife.lib.utility.wheel.TimeWheel.ITimeChangedListener;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class TimeWheelDialogFragment extends DialogFragment implements ITimeChangedListener {
    private static final String TAG = "TimeWheelDialogFragment";

    public static final String KEY_SER_CURRENTTIME = "currenttime";
    public static final String KEY_INT_NRSSHOWN = "nrsshown";
    public static final String KEY_INT_LAYOUT = "layoutId";
    public static final String KEY_INT_TIMEPICKER = "timepickerId";
    public static final String KEY_INT_CHOOSE = "choosebuttonId";
    public static final String KEY_INT_CANCEL = "cancelbuttonId";
    public static final String KEY_INT_STYLE = "style";
    public static final String KEY_INT_THEME = "theme";
    public static final String KEY_OBJ_LISTENER = "listener";

    TimeWheel timewheel = null;
    Button choice = null;
    Button cancel = null;
    Calendar currentTime = null;
    Integer nrShown = 3;
    int layoutId = 0;
    int choosebuttonId = 0;
    int cancelbuttonId = 0;
    int timepickerId = 0;
    int style = 0;
    int theme = 0;
    OnTimeCompleteListener listener = null;

    public static TimeWheelDialogFragment newInstance(Calendar currentTime, int nrsshown, int layoutId,
                                                      int timepickerId, int choosebuttonId, int cancelbuttonId, int style, int theme) {

        TimeWheelDialogFragment f = new TimeWheelDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable(KEY_SER_CURRENTTIME,
                currentTime);
        args.putInt(KEY_INT_NRSSHOWN,
                nrsshown);
        args.putInt(KEY_INT_LAYOUT,
                layoutId);
        args.putInt(KEY_INT_TIMEPICKER,
                timepickerId);
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
    public void onSaveInstanceState(Bundle args) {
        args.putSerializable(KEY_SER_CURRENTTIME,
                currentTime);
        args.putInt(KEY_INT_NRSSHOWN,
                nrShown);
        args.putInt(KEY_INT_LAYOUT,
                layoutId);
        args.putInt(KEY_INT_TIMEPICKER,
                timepickerId);
        args.putInt(KEY_INT_CHOOSE,
                choosebuttonId);
        args.putInt(KEY_INT_CANCEL,
                cancelbuttonId);
        args.putInt(KEY_INT_STYLE,
                style);
        args.putInt(KEY_INT_THEME,
                theme);
        if (listener != null) {
            args.putSerializable(KEY_OBJ_LISTENER, listener);
        }
        Log.d(TAG,
                "onSaveInstanceState[" + args.toString() + "]");
        super.onSaveInstanceState(args);
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

    private void initializeBundle(Bundle bundle) {
        currentTime = (Calendar) bundle.getSerializable(KEY_SER_CURRENTTIME);
        nrShown = bundle.getInt(KEY_INT_NRSSHOWN);
        layoutId = bundle.getInt(KEY_INT_LAYOUT);
        timepickerId = bundle.getInt(KEY_INT_TIMEPICKER);
        choosebuttonId = bundle.getInt(KEY_INT_CHOOSE);
        cancelbuttonId = bundle.getInt(KEY_INT_CANCEL);
        style = bundle.getInt(KEY_INT_STYLE);
        theme = bundle.getInt(KEY_INT_THEME);
        if (bundle.getSerializable(KEY_OBJ_LISTENER) != null) {
            listener = (OnTimeCompleteListener) bundle.getSerializable(KEY_OBJ_LISTENER);
        }

        setStyle(style,
                theme);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            initializeBundle(savedInstanceState);
        }
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            initializeBundle(savedInstanceState);
        }
        View view = inflater.inflate(layoutId,
                container,
                false);
        choice = (Button) view.findViewById(choosebuttonId);
        cancel = (Button) view.findViewById(cancelbuttonId);

        timewheel = (TimeWheel) view.findViewById(timepickerId);
        timewheel.setMinute(currentTime.get(Calendar.MINUTE));
        timewheel.setHour(currentTime.get(Calendar.HOUR_OF_DAY));
        timewheel.setVisibleItems(nrShown);

        choice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onCompleteTimePicker(timewheel.getHour(),
                            timewheel.getMinute());
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

    public interface OnTimeCompleteListener extends Serializable {
        public void onCompleteTimePicker(int hour, int minute);
    }

    public void setOnTimeCompleteListener(OnTimeCompleteListener listener) {
        this.listener = listener;
    }

    @Override
    public void onChanged(TimeWheel sender, int oldMinute, int oldHour, int minute, int hour) {
        currentTime.set(Calendar.HOUR_OF_DAY,
                hour);
        currentTime.set(Calendar.MINUTE,
                minute);

    }

}
