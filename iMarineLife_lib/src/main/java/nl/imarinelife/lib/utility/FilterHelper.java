package nl.imarinelife.lib.utility;

import android.app.ActionBar;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter.FilterListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class FilterHelper implements OnEditorActionListener, TextWatcher {
    private static final String TAG = "FilterHelper";

    Fragment fragment;
    MenuItem menuItem;
    EditText editText;
    ActionBar actionbar;
    FilterEnabledAdapter adapter;

    public FilterHelper(Fragment fragment, EditText pEditText, ActionBar actionbar,
                        FilterEnabledAdapter pAdapter, MenuItem pMenuItem) {
        super();
        Log.d(TAG, fragment.getClass().getName() + " " + editText + " " + menuItem + " " + actionbar + " " + adapter);
        this.fragment = fragment;
        this.editText = pEditText;
        this.menuItem = pMenuItem;
        this.actionbar = actionbar;
        this.adapter = pAdapter;

        this.editText.addTextChangedListener(this);
        this.editText.setOnEditorActionListener(this);

        if (this.adapter.isShowingSelection()) {
            Log.d(TAG, "Filter setupSearchBox - expanding ActionView because selection is showing");
            this.editText.setText(this.adapter.getSelectionConstraint());
            menuItem.expandActionView();
            ((BaseAdapter) (this.adapter)).notifyDataSetChanged();
        }

        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return false;
            }
        });

        menuItem.setOnActionExpandListener( new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Log.d(TAG, "Filter OnActionCollapseListener");
                adapter.invalidateSelection();
                editText.setText(null);
                hideKeyboard();
                return true; // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Log.d(TAG, "Filter OnActionExpandListener");
                editText.clearFocus();
                showKeyboard();
                editText.requestFocus();

                return true; // Return true to expand action view
            }
        });
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        Log.d(TAG, "Filter onEditorAction [" + event.getKeyCode() + "]");
        if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            hideKeyboard();
            menuItem.collapseActionView();
            return true;
        }
        return false;
    }

    private void showKeyboard() {
        Utils.showKeyboard(fragment, editText);
    }

    public void hideKeyboard() {
        Utils.hideKeyboard(fragment);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.d(TAG, "Filter onTextChanged [" + s + "]");
        if (adapter != null) {
            adapter.getFilter().filter(s,
                    (FilterListener) fragment);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // method stub

    }

    @Override
    public void afterTextChanged(Editable s) {
        // method stub

    }

}
