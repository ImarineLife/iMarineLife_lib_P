package nl.imarinelife.lib.utility.dialogs;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import java.io.Serializable;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.utility.CursorProvider;
import nl.imarinelife.lib.utility.Utils;
import nl.imarinelife.lib.utility.dialogs.AddValueDialogFragment.OnOkListener;
import nl.imarinelife.lib.utility.dialogs.AreYouSureDialogFragment.OnYesListener;
import nl.imarinelife.lib.utility.wheel.CursorWheelAdapter;
import nl.imarinelife.lib.utility.wheel.OnWheelChangedListener;
import nl.imarinelife.lib.utility.wheel.WheelView;

public class SearchTextWheelDialogFragment extends DialogFragment implements
        OnWheelChangedListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    @SuppressWarnings("unused")
    private static final String TAG = "SearchTextWheelDFr";

    public static final String KEY_STR_CURRENTVALUE = "currentValue";
    public static final String KEY_STR_CONSTRAINT = "constraint";
    public static final String KEY_SER_CURSORPROVIDER = "cursorprovider";
    public static final String KEY_INT_NRSSHOWN = "nrsshown";
    public static final String KEY_INT_LAYOUT = "layoutId";
    public static final String KEY_INT_WHEEL = "wheelId";
    public static final String KEY_INT_SEARCHTEXT = "searchTextId";
    public static final String KEY_INT_NEXT = "nextbuttonId";
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
    // search bit
    SearchView searchText = null;
    ImageButton nextButton = null;
    // choose bit
    EditText editText = null;
    Button choice = null;
    Button cancel = null;
    Button add = null;
    Button minus = null;
    // wheel bit
    String currentValue = null;
    String constraint = null;
    Integer nrShown = 3;
    int layoutId = 0;
    int searchtextId = 0;
    int nextbuttonId = 0;
    int prevbuttonId = 0;
    int choosebuttonId = 0;
    int cancelbuttonId = 0;
    int addbuttonId = 0;
    int minusbuttonId = 0;
    int wheelId = 0;
    int edittextId = 0;
    int style = 0;
    int theme = 0;

    OnTextCompleteListener listener = null;

    public static SearchTextWheelDialogFragment newInstance(String currentLocation,
                                                      CursorProvider cursorprovider, int nrshown, int layoutId,
                                                      int wheelId, int searchttext, int nextbuttonId, int edittext, int choosebuttonId, int cancelbuttonId,
                                                      int addbuttonId, int minusbuttonId, int style, int theme) {

        SearchTextWheelDialogFragment f = new SearchTextWheelDialogFragment();

        Bundle args = new Bundle();
        args.putString(KEY_STR_CURRENTVALUE, currentLocation);
        args.putSerializable(KEY_SER_CURSORPROVIDER, cursorprovider);
        args.putInt(KEY_INT_NRSSHOWN, nrshown);
        args.putInt(KEY_INT_LAYOUT, layoutId);
        args.putInt(KEY_INT_WHEEL, wheelId);
        args.putInt(KEY_INT_SEARCHTEXT, searchttext);
        args.putInt(KEY_INT_NEXT, nextbuttonId);
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
        constraint = bundle.getString(KEY_STR_CONSTRAINT);
        cursorProvider = (CursorProvider) bundle
                .getSerializable(KEY_SER_CURSORPROVIDER);
        nrShown = bundle.getInt(KEY_INT_NRSSHOWN);
        layoutId = bundle.getInt(KEY_INT_LAYOUT);
        wheelId = bundle.getInt(KEY_INT_WHEEL);
        searchtextId = bundle.getInt(KEY_INT_SEARCHTEXT);
        nextbuttonId = bundle.getInt(KEY_INT_NEXT);
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
        args.putString(KEY_STR_CONSTRAINT, constraint);
        args.putSerializable(KEY_SER_CURSORPROVIDER, cursorProvider);
        args.putInt(KEY_INT_NRSSHOWN, nrShown);
        args.putInt(KEY_INT_LAYOUT, layoutId);
        args.putInt(KEY_INT_WHEEL, wheelId);
        args.putInt(KEY_INT_SEARCHTEXT, searchtextId);
        args.putInt(KEY_INT_NEXT, nextbuttonId);
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
        searchText = (SearchView) view.findViewById(searchtextId);
        searchText.setOnQueryTextListener(this);
        searchText.setOnCloseListener(this);
        Utils.setSearchTextColour(searchText, getActivity().getResources());
        if(constraint!=null) {
            searchText.setQuery(constraint, true);
        }

        nextButton = (ImageButton) view.findViewById(nextbuttonId);
        nextButton.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              Log.d(TAG, "constraint: " + constraint);
                                              if (constraint != null) {
                                                  scrollToNextPosition(cursorProvider.getIndex(currentValue), constraint, false);
                                              }
                                          }
                                      }
        );


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

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d(TAG, "onQueryTextSubmit [" + query + "]");
        searchText.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Called when the searchview text has changed.
        // find the next entry in the wheel that suits
        constraint = !TextUtils.isEmpty(newText) ? newText.toLowerCase() : null;
        if(constraint!=null){
            int currentPosition = cursorProvider.getIndex(currentValue);
            scrollToNextPosition(currentPosition, constraint,true);
        }
        return true;
    }

    private void scrollToNextPosition(int currentPosition, String constraint, boolean alsotryCurrent) {
        Log.d(TAG, "scrollToNextPosition [" + currentPosition +"]["+constraint + "]");
        int nextPosition = currentPosition;
        if(!alsotryCurrent) nextPosition++;
        int newPosition = -1;
        int cursorSize = cursorProvider.getCount();
        while(nextPosition<cursorSize){
            Log.d(TAG, "scrollToNextPosition while [" + nextPosition +"<"+cursorSize+"]["+ constraint + "]");
            String value = cursorProvider.getValue(nextPosition);
            if(value!=null && value.toLowerCase().contains(constraint)){
                Log.d(TAG, "scrollToNextPosition found newPosition [" + nextPosition + "]");
                newPosition = nextPosition;
                break;
            }
            nextPosition++;
        }
        if(newPosition==-1){
            nextPosition=0;
            while (nextPosition<currentPosition){
                Log.d(TAG, "scrollToNextPosition while [" + nextPosition +"<"+currentPosition+"]["+ constraint + "]");
                String value = cursorProvider.getValue(nextPosition);
                if(value!=null && value.toLowerCase().contains(constraint)){
                    Log.d(TAG, "scrollToNextPosition found newPosition [" + nextPosition + "]");
                    newPosition = nextPosition;
                    break;
                }
                nextPosition++;
            }
        }
        if(newPosition!=-1){
            Log.d(TAG, "scrollToNextPosition scrolling to newPosition [" + newPosition + "]reposition["+(newPosition-currentPosition)+"]");
            wheel.scroll(newPosition-currentPosition, 500);
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
