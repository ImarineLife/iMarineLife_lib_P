package nl.imarinelife.lib.utility.dialogs;

import java.io.Serializable;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.utility.CursorProvider;
import nl.imarinelife.lib.utility.dialogs.AddValueDialogFragment.OnOkListener;
import nl.imarinelife.lib.utility.dialogs.AreYouSureDialogFragment.OnYesListener;
import nl.imarinelife.lib.utility.wheel.CursorWheelAdapter;
import nl.imarinelife.lib.utility.wheel.OnWheelChangedListener;
import nl.imarinelife.lib.utility.wheel.WheelView;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TextWheelDialogFragment extends DialogFragment implements
        OnWheelChangedListener {
    @SuppressWarnings("unused")
    private static final String TAG = "TextWheelDialogFragment";

    public static final String KEY_STR_CURRENTVALUE = "currentValue";
    public static final String KEY_SER_CURSORPROVIDER = "cursorprovider";
    public static final String KEY_INT_NRSSHOWN = "nrsshown";
    public static final String KEY_INT_LAYOUT = "layoutId";
    public static final String KEY_INT_WHEEL = "wheelId";
    public static final String KEY_INT_EDITTEXT = "editTextId";
    public static final String KEY_INT_CHOOSE = "choosebuttonId";
    public static final String KEY_INT_CANCEL = "cancelbuttonId";
    public static final String KEY_INT_ADD = "addbuttonId";
    public static final String KEY_INT_MINUS = "minusbuttonId";
    public static final String KEY_INT_STYLE = "style";
    public static final String KEY_INT_THEME = "theme";
    public static final String KEY_OBJ_LISTENER = "listener";

    WheelView wheel = null;
    CursorProvider cursorProvider = null;
    EditText editText = null;
    Button choice = null;
    Button cancel = null;
    Button add = null;
    Button minus = null;
    String currentValue = null;
    Integer nrShown = 3;
    int layoutId = 0;
    int choosebuttonId = 0;
    int cancelbuttonId = 0;
    int addbuttonId = 0;
    int minusbuttonId = 0;
    int wheelId = 0;
    int edittextId = 0;
    int style = 0;
    int theme = 0;

    OnTextCompleteListener listener = null;

    public static TextWheelDialogFragment newInstance(String currentLocation,
                                                      CursorProvider cursorprovider, int nrshown, int layoutId,
                                                      int wheelId, int edittext, int choosebuttonId, int cancelbuttonId,
                                                      int addbuttonId, int minusbuttonId, int style, int theme) {

        TextWheelDialogFragment f = new TextWheelDialogFragment();

        Bundle args = new Bundle();
        args.putString(KEY_STR_CURRENTVALUE, currentLocation);
        args.putSerializable(KEY_SER_CURSORPROVIDER, cursorprovider);
        args.putInt(KEY_INT_NRSSHOWN, nrshown);
        args.putInt(KEY_INT_LAYOUT, layoutId);
        args.putInt(KEY_INT_WHEEL, wheelId);
        args.putInt(KEY_INT_EDITTEXT, edittext);
        args.putInt(KEY_INT_CHOOSE, choosebuttonId);
        args.putInt(KEY_INT_CANCEL, cancelbuttonId);
        args.putInt(KEY_INT_ADD, addbuttonId);
        args.putInt(KEY_INT_MINUS, minusbuttonId);
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
        currentValue = bundle.getString(KEY_STR_CURRENTVALUE);
        cursorProvider = (CursorProvider) bundle
                .getSerializable(KEY_SER_CURSORPROVIDER);
        nrShown = bundle.getInt(KEY_INT_NRSSHOWN);
        layoutId = bundle.getInt(KEY_INT_LAYOUT);
        wheelId = bundle.getInt(KEY_INT_WHEEL);
        edittextId = bundle.getInt(KEY_INT_EDITTEXT);
        choosebuttonId = bundle.getInt(KEY_INT_CHOOSE);
        cancelbuttonId = bundle.getInt(KEY_INT_CANCEL);
        addbuttonId = bundle.getInt(KEY_INT_ADD);
        minusbuttonId = bundle.getInt(KEY_INT_MINUS);
        style = bundle.getInt(KEY_INT_STYLE);
        theme = bundle.getInt(KEY_INT_THEME);
        if (bundle.getSerializable(KEY_OBJ_LISTENER) != null) {
            listener = (OnTextCompleteListener) bundle.getSerializable(KEY_OBJ_LISTENER);
        }

        setStyle(style, theme);
    }

    @Override
    public void onSaveInstanceState(Bundle args) {
        args.putString(KEY_STR_CURRENTVALUE, currentValue);
        args.putSerializable(KEY_SER_CURSORPROVIDER, cursorProvider);
        args.putInt(KEY_INT_NRSSHOWN, nrShown);
        args.putInt(KEY_INT_LAYOUT, layoutId);
        args.putInt(KEY_INT_WHEEL, wheelId);
        args.putInt(KEY_INT_EDITTEXT, edittextId);
        args.putInt(KEY_INT_CHOOSE, choosebuttonId);
        args.putInt(KEY_INT_CANCEL, cancelbuttonId);
        args.putInt(KEY_INT_ADD, addbuttonId);
        args.putInt(KEY_INT_MINUS, minusbuttonId);
        args.putInt(KEY_INT_STYLE, style);
        args.putInt(KEY_INT_THEME, theme);
        if (listener != null) {
            args.putSerializable(KEY_OBJ_LISTENER, listener);
        }
        super.onSaveInstanceState(args);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Context ctx = this.getActivity();
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            initializeBundle(savedInstanceState);
        }
        View view = inflater.inflate(layoutId, container, false);
        choice = (Button) view.findViewById(choosebuttonId);
        cancel = (Button) view.findViewById(cancelbuttonId);
        add = (Button) view.findViewById(addbuttonId);
        minus = (Button) view.findViewById(minusbuttonId);

        CursorWheelAdapter adapter = new CursorWheelAdapter(getActivity(),
                cursorProvider);
        wheel = (WheelView) view.findViewById(wheelId);
        wheel.setViewAdapter(adapter);
        wheel.setCurrentItem(adapter.getItem(currentValue));
        wheel.setVisibleItems(nrShown);
        wheel.addChangingListener(this);

        if (edittextId != 0) {
            editText = (EditText) view.findViewById(edittextId);
            editText.setText(currentValue);
        }

        choice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    CursorWheelAdapter adapter = (CursorWheelAdapter) wheel
                            .getViewAdapter();
                    Object object = null;
                    CharSequence seq = adapter.getItemText(wheel
                            .getCurrentItem());
                    String item = seq != null ? seq.toString() : null;
                    if (editText == null
                            || editText.getText().toString().equals(item)) {
                        object = adapter.getObject(wheel.getCurrentItem());
                    } else {
                        object = adapter.getMinimalObject(ctx, editText
                                .getText().toString());
                    }
                    listener.onCompleteTextWheel(object, item);
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
        if (add != null) {
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (editText == null) {
                        AddValueDialogFragment fragment = getAddValueDialogFragmentForLocation(ctx);
                        showDialog(fragment);
                    } else {
                        handleOk(editText.getText().toString(), ctx);
                    }
                }
            });
        }
        if (minus != null) {
            minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int checkCount = cursorProvider.checkRemoval(ctx,
                            currentValue);
                    if (checkCount == 0) {
                        Toast.makeText(
                                getActivity(),
                                LibApp.getCurrentResources().getString(
                                        R.string.onlyRemovePersonal),
                                Toast.LENGTH_SHORT).show();
                    } else if (checkCount == -1) {
                        Toast.makeText(
                                getActivity(),
                                LibApp.getCurrentResources().getString(
                                        R.string.onlyRemoveNonUsedLocation),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        AreYouSureDialogFragment fragment = getAreYouSureDialogFragment(ctx);
                        showDialog(fragment);
                    }
                }
            });
        }

        return view;
    }

    protected AreYouSureDialogFragment getAreYouSureDialogFragment(
            final Context ctx) {
        final int layoutId;
        final int textViewId;
        final int yesbuttonId;
        final int nobuttonId;
        layoutId = R.layout.general_yoursuretext_dialog;
        textViewId = R.id.general_textview_id;
        yesbuttonId = R.id.general_yes_button_id;
        nobuttonId = R.id.general_no_button_id;

        @SuppressWarnings("serial")
        OnYesListener listener = new OnYesListener() {

            @Override
            public void onYes() {
                int index = cursorProvider.getIndex(currentValue);
                int removed = cursorProvider.remove(ctx, currentValue);
                if (removed == 0) {
                    Toast.makeText(
                            getActivity(),
                            LibApp.getCurrentResources().getString(
                                    R.string.onlyRemovePersonal),
                            Toast.LENGTH_SHORT).show();
                } else if (removed == -1) {
                    Toast.makeText(
                            getActivity(),
                            LibApp.getCurrentResources().getString(
                                    R.string.onlyRemoveNonUsedLocation),
                            Toast.LENGTH_SHORT).show();
                } else {
                    cursorProvider.refresh(ctx);
                    CursorWheelAdapter adapter = new CursorWheelAdapter(
                            getActivity(), cursorProvider);
                    wheel.setViewAdapter(adapter);
                    if (index == 0) {
                        currentValue = cursorProvider.getValue(0);
                        wheel.setCurrentItem(0);
                    } else {
                        currentValue = cursorProvider.getValue(index - 1);
                        wheel.setCurrentItem(index - 1);
                    }
                }
            }
        };

        String dialogtxt = LibApp.getCurrentResources().getString(
                R.string.areyousure_locationremoval)
                + " " + currentValue;
        AreYouSureDialogFragment frag = AreYouSureDialogFragment.newInstance(
                dialogtxt, layoutId, textViewId, yesbuttonId, nobuttonId,
                DialogFragment.STYLE_NO_TITLE, R.style.iMarineLifeDialogTheme);
        frag.setOnYesListener(listener);
        return frag;

    }

    protected AddValueDialogFragment getAddValueDialogFragmentForLocation(
            final Context ctx) {
        final int layoutId;
        final int editTextId;
        final int okbuttonId;
        final int cancelbuttonId;
        layoutId = R.layout.general_edittext_dialog;
        editTextId = R.id.general_edittext_id;
        okbuttonId = R.id.general_ok_button_id;
        cancelbuttonId = R.id.general_cancel_button_id;

        @SuppressWarnings("serial")
        OnOkListener listener = new OnOkListener() {

            @Override
            public void onOk(String value) {
                handleOk(value, ctx);
            }
        };

        AddValueDialogFragment frag = AddValueDialogFragment.newInstance(null,
                layoutId, editTextId, okbuttonId, cancelbuttonId,
                DialogFragment.STYLE_NO_TITLE, R.style.iMarineLifeDialogTheme);
        frag.setOnOkListener(listener);
        return frag;

    }

    protected void handleOk(String value, Context ctx) {
        if (value != null && value.trim().length() > 0) {
            if (!cursorProvider.alreadyExists(ctx, value)) {
                cursorProvider.insert(ctx, value);
                cursorProvider.refresh(ctx);
                wheel.setCurrentItem(((CursorWheelAdapter) wheel
                        .getViewAdapter()).getItem(value));
                currentValue = value;
            } else {
                Toast.makeText(
                        getActivity(),
                        LibApp.getCurrentResources().getString(
                                R.string.alreadyExists), Toast.LENGTH_SHORT)
                        .show();

            }
        }
    }

    public interface OnTextCompleteListener extends Serializable {
        public void onCompleteTextWheel(Object object, String displayName);
    }

    public void setOnCompleteListener(OnTextCompleteListener listener) {
        this.listener = listener;
    }

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        currentValue = (((CursorWheelAdapter) wheel.getViewAdapter())
                .getItemText(newValue)).toString();
    }

    void showDialog(DialogFragment newFragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        newFragment.show(ft, "adddialog");

    }

}
