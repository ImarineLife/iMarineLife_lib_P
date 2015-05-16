package nl.imarinelife.lib.utility;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SearchView;

public class Utils {
	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes,
					0,
					buffer_size);
				if (count == -1)
					break;
				os.write(bytes,
					0,
					count);
			}
		} catch (Exception ex) {
		}
	}

	public static void hideKeyboard(Fragment fragment) {
		InputMethodManager mInputManager = (InputMethodManager) fragment.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (android.os.Build.VERSION.SDK_INT < 11) {
			if (mInputManager != null) {
				if (fragment != null && fragment.getActivity() != null && fragment.getActivity().getWindow() != null
						&& fragment.getActivity().getWindow().getCurrentFocus() != null
						&& fragment.getActivity().getWindow().getCurrentFocus().getWindowToken() != null) {
					mInputManager.hideSoftInputFromWindow(fragment.getActivity().getWindow().getCurrentFocus().getWindowToken(),
						0);
				}
			}

		} else {
			if (mInputManager != null) {
				if (fragment != null && fragment.getActivity() != null
						&& fragment.getActivity().getCurrentFocus() != null
						&& fragment.getActivity().getCurrentFocus().getWindowToken() != null) {
					mInputManager.hideSoftInputFromWindow(fragment.getActivity().getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}

		}
	}

	public static void showKeyboard(Fragment fragment, EditText editText) {
		InputMethodManager mInputManager = (InputMethodManager) fragment.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (android.os.Build.VERSION.SDK_INT < 11) {
			mInputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,
				InputMethodManager.HIDE_IMPLICIT_ONLY);
		} else {
			mInputManager.showSoftInput(editText,
				InputMethodManager.SHOW_IMPLICIT);
		}
	}

	public static int getPixels(Resources resources, int dp) {
		final float scale = resources.getDisplayMetrics().density;
		int px = (int) (dp * scale + 0.5f);
		return px;
	}

	public static void setSearchTextColour(SearchView searchView, Resources resources) {
		int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
		EditText searchPlate = (EditText) searchView.findViewById(searchPlateId);
		searchPlate.setTextColor(resources.getColor(android.R.color.secondary_text_dark));
		//searchPlate.setBackgroundResource(R.drawable.edit_text_holo_light);
		//searchPlate.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
	}
}
