package nl.imarinelife.lib.utility.dialogs;

import java.io.Serializable;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class AreYouSureDialogFragment extends DialogFragment {
    @SuppressWarnings("unused")
    private static final String TAG = "AreYouSureDialogFragment";

    public static final String KEY_STR_DISPLAYTXT = "displayTxt";
    public static final String KEY_INT_LAYOUT = "layoutId";
    public static final String KEY_INT_YES = "yesbuttonId";
    public static final String KEY_INT_NO = "nobuttonId";
    public static final String KEY_INT_TEXTVIEW = "textviewId";
    public static final String KEY_INT_STYLE = "style";
    public static final String KEY_INT_THEME = "theme";
    public static final String KEY_OBJ_YES_LISTENER = "yesListener";
    public static final String KEY_OBJ_NO_LISTENER = "noListener";

    String displayTxt = null;
    Button yes = null;
    Button no = null;
    TextView textview = null;
    int layoutId = 0;
    int yesbuttonId = 0;
    int nobuttonId = 0;
    int textviewId = 0;
    int style = 0;
    int theme = 0;

    OnYesListener yesListener = null;
    OnNoListener noListener = null;

    public static AreYouSureDialogFragment newInstance(String displayText, int layoutId, int textviewId,
                                                       int yesbuttonId, int nobuttonId, int style, int theme) {

        AreYouSureDialogFragment f = new AreYouSureDialogFragment();

        Bundle args = new Bundle();
        args.putString(KEY_STR_DISPLAYTXT,
                displayText);
        args.putInt(KEY_INT_LAYOUT,
                layoutId);
        args.putInt(KEY_INT_TEXTVIEW,
                textviewId);
        args.putInt(KEY_INT_YES,
                yesbuttonId);
        args.putInt(KEY_INT_NO,
                nobuttonId);
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
        layoutId = bundle.getInt(KEY_INT_LAYOUT);
        displayTxt = bundle.getString(KEY_STR_DISPLAYTXT);
        textviewId = bundle.getInt(KEY_INT_TEXTVIEW);
        yesbuttonId = bundle.getInt(KEY_INT_YES);
        nobuttonId = bundle.getInt(KEY_INT_NO);
        style = bundle.getInt(KEY_INT_STYLE);
        theme = bundle.getInt(KEY_INT_THEME);
        if (bundle.getSerializable(KEY_OBJ_YES_LISTENER) != null) {
            yesListener = (OnYesListener) bundle.getSerializable(KEY_OBJ_YES_LISTENER);
        }
        if (bundle.getSerializable(KEY_OBJ_NO_LISTENER) != null) {
            noListener = (OnNoListener) bundle.getSerializable(KEY_OBJ_NO_LISTENER);
        }
        setStyle(style,
                theme);
    }

    @Override
    public void onSaveInstanceState(Bundle args) {
        args.putInt(KEY_INT_LAYOUT,
                layoutId);
        args.putString(KEY_STR_DISPLAYTXT,
                displayTxt);
        args.putInt(KEY_INT_TEXTVIEW,
                textviewId);
        args.putInt(KEY_INT_YES,
                yesbuttonId);
        args.putInt(KEY_INT_NO,
                nobuttonId);
        args.putInt(KEY_INT_STYLE,
                style);
        args.putInt(KEY_INT_THEME,
                theme);
        if (yesListener != null) {
            args.putSerializable(KEY_OBJ_YES_LISTENER, yesListener);
        }
        if (noListener != null) {
            args.putSerializable(KEY_OBJ_NO_LISTENER, noListener);
        }

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
        yes = (Button) view.findViewById(yesbuttonId);
        no = (Button) view.findViewById(nobuttonId);

        textview = (TextView) view.findViewById(textviewId);
        textview.setText(displayTxt);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (yesListener != null) {
                    yesListener.onYes();
                }
                dismiss();
            }
        });
        no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (noListener != null) {
                    noListener.onNo();
                }
                dismiss();
            }
        });

        return view;
    }

    public interface OnYesListener extends Serializable {
        public void onYes();
    }

    public void setOnYesListener(OnYesListener listener) {
        this.yesListener = listener;
    }

    public interface OnNoListener extends Serializable {
        public void onNo();
    }

    public void setOnNoListener(OnNoListener listener) {
        this.noListener = listener;
    }

    void showDialog(DialogFragment newFragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        newFragment.show(ft,
                "suredialog");
        // ft.commit(); // ??
    }

}
