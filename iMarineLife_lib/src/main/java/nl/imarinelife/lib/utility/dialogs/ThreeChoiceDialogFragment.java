package nl.imarinelife.lib.utility.dialogs;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.Serializable;

import nl.imarinelife.lib.R;

public class ThreeChoiceDialogFragment extends DialogFragment {
    @SuppressWarnings("unused")
    private static final String TAG = "ThreeChoiceDialogFr";

    public static final String KEY_STR_DISPLAYTXT = "displayTxt";
    public static final String KEY_INT_LAYOUT = "layoutId";
    public static final String KEY_INT_ONE = "choiceOneButtonId";
    public static final String KEY_INT_TWO = "choiceTwoButtonId";
    public static final String KEY_INT_THREE = "choiceThreeButtonId";
    public static final String KEY_INT_TEXTVIEW = "textviewId";
    public static final String KEY_INT_STYLE = "style";
    public static final String KEY_INT_THEME = "theme";
    public static final String KEY_INT_PROGRESS_SECTION = "progressSection";
    public static final String KEY_INT_PROGRESS_TEXT = "progressText";
    public static final String KEY_INT_PROGRESSBAR_LOCATIONS = "progressBarLocations";
    public static final String KEY_INT_PROGRESSBAR_FIELDGUIDE = "progressBarFieldguide";
    public static final String KEY_BOOL_SHOULDSHOWSTATUS = "shouldShowStatus";
    public static final String KEY_OBJ_ONE_LISTENER = "oneListener";
    public static final String KEY_OBJ_TWO_LISTENER = "twoListener";
    public static final String KEY_OBJ_THREE_LISTENER = "threeListener";

    String displayTxt = null;
    Button one = null;
    Button two = null;
    Button three = null;
    TextView textview = null;
    TextView progressText = null;
    ProgressBar progressbarLocations = null;
    ProgressBar progressbarFieldGuide = null;

    int layoutId = 0;
    int onebuttonId = 0;
    int twobuttonId = 0;
    int threebuttonId = 0;
    int textviewId = 0;
    int style = 0;
    int theme = 0;

    static int progressSectionId = 0;
    static int progressTextId = 0;
    static int progressbarLocationsId = 0;
    static int progressbarFieldGuideId = 0;

    OnOneListener oneListener = null;
    OnTwoListener twoListener = null;
    OnThreeListener threeListener = null;

    static boolean shouldShowStatus = false;

    public static ThreeChoiceDialogFragment newInstance(String displayText,
                                                        int layoutId, int textviewId, int onebuttonId, int twobuttonId,
                                                        int threebuttonId, int style, int theme) {

        ThreeChoiceDialogFragment f = new ThreeChoiceDialogFragment();

        Bundle args = new Bundle();
        args.putString(KEY_STR_DISPLAYTXT, displayText);
        args.putInt(KEY_INT_LAYOUT, layoutId);
        args.putInt(KEY_INT_TEXTVIEW, textviewId);
        args.putInt(KEY_INT_ONE, onebuttonId);
        args.putInt(KEY_INT_TWO, twobuttonId);
        args.putInt(KEY_INT_THREE, threebuttonId);
        args.putInt(KEY_INT_STYLE, style);
        args.putInt(KEY_INT_THEME, theme);
        args.putBoolean(KEY_BOOL_SHOULDSHOWSTATUS, shouldShowStatus);
        args.putInt(KEY_INT_PROGRESS_SECTION, progressSectionId);
        args.putInt(KEY_INT_PROGRESS_TEXT, progressTextId);
        args.putInt(KEY_INT_PROGRESSBAR_LOCATIONS, progressbarLocationsId);
        args.putInt(KEY_INT_PROGRESSBAR_FIELDGUIDE, progressbarFieldGuideId);
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

        View section = (View) getView().findViewById(R.id.progress_section_localechange);
        if (shouldShowStatus) {
            section.setVisibility(View.VISIBLE);
        } else {
            section.setVisibility(View.GONE);
        }

        super.onActivityCreated(savedInstanceState);
    }

    private void initializeBundle(Bundle bundle) {
        layoutId = bundle.getInt(KEY_INT_LAYOUT);
        displayTxt = bundle.getString(KEY_STR_DISPLAYTXT);
        textviewId = bundle.getInt(KEY_INT_TEXTVIEW);
        onebuttonId = bundle.getInt(KEY_INT_ONE);
        twobuttonId = bundle.getInt(KEY_INT_TWO);
        threebuttonId = bundle.getInt(KEY_INT_THREE);
        style = bundle.getInt(KEY_INT_STYLE);
        theme = bundle.getInt(KEY_INT_THEME);
        progressSectionId = bundle.getInt(KEY_INT_PROGRESS_SECTION);
        progressTextId = bundle.getInt(KEY_INT_PROGRESS_TEXT);
        progressbarFieldGuideId = bundle.getInt(KEY_INT_PROGRESSBAR_FIELDGUIDE);
        progressbarLocationsId = bundle.getInt(KEY_INT_PROGRESSBAR_LOCATIONS);
        shouldShowStatus = bundle.getBoolean(KEY_BOOL_SHOULDSHOWSTATUS);
        if (bundle.getSerializable(KEY_OBJ_ONE_LISTENER) != null) {
            oneListener = (OnOneListener) bundle.getSerializable(KEY_OBJ_ONE_LISTENER);
        }
        if (bundle.getSerializable(KEY_OBJ_TWO_LISTENER) != null) {
            twoListener = (OnTwoListener) bundle.getSerializable(KEY_OBJ_TWO_LISTENER);

        }
        if (bundle.getSerializable(KEY_OBJ_THREE_LISTENER) != null) {
            threeListener = (OnThreeListener) bundle.getSerializable(KEY_OBJ_THREE_LISTENER);
        }
        setStyle(style, theme);
    }

    @Override
    public void onSaveInstanceState(Bundle args) {
        args.putInt(KEY_INT_LAYOUT, layoutId);
        args.putString(KEY_STR_DISPLAYTXT, displayTxt);
        args.putInt(KEY_INT_TEXTVIEW, textviewId);
        args.putInt(KEY_INT_ONE, onebuttonId);
        args.putInt(KEY_INT_TWO, twobuttonId);
        args.putInt(KEY_INT_THREE, threebuttonId);
        args.putInt(KEY_INT_STYLE, style);
        args.putInt(KEY_INT_THEME, theme);
        args.putInt(KEY_INT_PROGRESS_SECTION, progressSectionId);
        args.putInt(KEY_INT_PROGRESS_TEXT, progressTextId);
        args.putInt(KEY_INT_PROGRESSBAR_LOCATIONS, progressbarLocationsId);
        args.putInt(KEY_INT_PROGRESSBAR_FIELDGUIDE, progressbarFieldGuideId);
        args.putBoolean(KEY_BOOL_SHOULDSHOWSTATUS, shouldShowStatus);
        super.onSaveInstanceState(args);
        if (oneListener != null) {
            args.putSerializable(KEY_OBJ_ONE_LISTENER, oneListener);
        }
        if (twoListener != null) {
            args.putSerializable(KEY_OBJ_TWO_LISTENER, twoListener);
        }
        if (threeListener != null) {
            args.putSerializable(KEY_OBJ_THREE_LISTENER, threeListener);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            initializeBundle(savedInstanceState);
        }
        View view = inflater.inflate(layoutId, container, false);
        one = (Button) view.findViewById(onebuttonId);
        two = (Button) view.findViewById(twobuttonId);
        three = (Button) view.findViewById(threebuttonId);

        progressbarLocations = (ProgressBar) view
                .findViewById(R.id.localechange_progressbar1);
        progressbarFieldGuide = (ProgressBar) view
                .findViewById(R.id.localechange_progressbar2);
        progressText = (TextView) view.findViewById(R.id.localechange_progressbar_text);

        textview = (TextView) view.findViewById(textviewId);
        textview.setText(displayTxt);

        one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (oneListener != null) {
                    shouldShowStatus = true;
                    View section = (View) getView().findViewById(R.id.progress_section_localechange);
                    section.setVisibility(View.VISIBLE);
                    oneListener.onOne();
                }
                // dismiss();  // done on end of background threads
            }
        });
        two.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (twoListener != null) {
                    twoListener.onTwo();
                }
                dismiss();
            }
        });
        three.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (threeListener != null) {
                    threeListener.onThree();
                }
                dismiss();
            }
        });

        return view;
    }

    public interface OnOneListener extends Serializable {
        public void onOne();
    }

    public void setOnOneListener(OnOneListener listener) {
        this.oneListener = listener;
    }

    public interface OnTwoListener extends Serializable {
        public void onTwo();
    }

    public void setOnTwoListener(OnTwoListener listener) {
        this.twoListener = listener;
    }

    public interface OnThreeListener extends Serializable {
        public void onThree();
    }

    public void setOnThreeListener(OnThreeListener listener) {
        this.threeListener = listener;
    }

    void showDialog(DialogFragment newFragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        newFragment.show(ft, "threechoicedialog");
        // ft.commit(); // ??
    }

    public void refreshProgressLocations(int percentage) {
        Log.d(TAG, "Progress Locations [" + percentage + "]");
        if (!isDetachedOrVisible()) {
            progressbarLocations.setProgress(percentage);
            progressbarLocations.refreshDrawableState();
        }

    }

    public void refreshProgressFieldGuide(int percentage) {
        Log.d(TAG, "Progress FieldGuide [" + percentage + "]");
        if (!isDetachedOrVisible()) {
            progressbarFieldGuide.setProgress(percentage);
            progressbarFieldGuide.refreshDrawableState();
        }
    }

    @TargetApi(14)
    private boolean isDetachedOrVisible() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            return isDetached();
        } else {
            return isVisible();
        }
    }

}
