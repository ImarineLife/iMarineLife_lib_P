package nl.imarinelife.lib.divinglog.db.dive;


import nl.imarinelife.lib.R;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

public class BuddyCursorAdapter extends CursorAdapter  {

	private DiveDbHelper			diveDbHelper;
	@SuppressWarnings("unused")
	private AutoCompleteTextView	autoview;

	/**
	 * Constructor. Note that no cursor is needed when we create the adapter.
	 * Instead, cursors are created on demand when completions are needed for
	 * the field. (see
	 *
	 * @param dbHelper
	 *            The AutoCompleteDbAdapter in use by the outer class object.
	 */

	public BuddyCursorAdapter(Activity activity, DiveDbHelper dbHelper, AutoCompleteTextView autoview) {

		// Call the CursorAdapter constructor with a null Cursor.
		super(activity, null, FLAG_REGISTER_CONTENT_OBSERVER);
		diveDbHelper = dbHelper;
		this.autoview = autoview;

	}

	/**
	 * Invoked by the AutoCompleteTextView field to get completions for the
	 * current input. NOTE: If this method either throws an exception or returns
	 * null, the Filter class that invokes it will log an error with the
	 * traceback, but otherwise ignore the problem. No choice list will be
	 * displayed. Watch those error logs!
	 * 
	 * @param constraint
	 *            The input entered thus far. The resulting query will search
	 *            for states whose name begins with this string.
	 * @return A Cursor that is positioned to the first row (if one exists) and
	 *         managed by the activity.
	 */

	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		if (getFilterQueryProvider() != null) {
			return getFilterQueryProvider().runQuery(constraint);
		}

		Cursor cursor = diveDbHelper.fetchCursorForMatchingBuddies((constraint != null
				? constraint.toString()
				: null));

		return cursor;

	}

	/**
	 * Called by the AutoCompleteTextView field to get the text that will be
	 * entered in the field after a choice has been made.
	 * 
	 * @param cursor
	 *            The cursor, positioned to a particular row in the list.
	 * @return A String representing the row's text value. (Note that this
	 *         specializes the base class return value for this method, which is
	 *         {@link CharSequence}.)
	 */

	@Override
	public String convertToString(Cursor cursor) {
		final int columnIndex = cursor.getColumnIndexOrThrow(DiveDbHelper.KEY_BUDDYNAME);
		final String str = cursor.getString(columnIndex);
		return str;
	}

	/**
	 * Called by the ListView for the AutoCompleteTextView field to display the
	 * text for a particular choice in the list.
	 * 
	 * @param view
	 *            The TextView used by the ListView to display a particular
	 *            choice.
	 * @param context
	 *            The context (Activity) to which this form belongs; equivalent
	 *            to {@code SelectState.this}.
	 * @param cursor
	 *            The cursor for the list of choices, positioned to a particular
	 *            row.
	 */

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		final String text = convertToString(cursor);

		((TextView) view).setText(text);

	}

	/**
	 * Called by the AutoCompleteTextView field to display the text for a
	 * particular choice in the list.
	 * 
	 * @param context
	 *            The context (Activity) to which this form belongs; equivalent
	 *            to {@code SelectState.this}.
	 * @param cursor
	 *            The cursor for the list of choices, positioned to a particular
	 *            row.
	 * @param parent
	 *            The ListView that contains the list of choices.
	 * @return A new View (really, a TextView) to hold a particular choice.
	 */

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		final LayoutInflater inflater = LayoutInflater.from(context);

		final View view =

		inflater.inflate(R.layout.general_listitem_layout,

			parent,
			false);

		return view;

	}

	/**
	 * Called by the AutoCompleteTextView field when a choice has been made by
	 * the user.
	 * 
	 * @param listView
	 *            The ListView containing the choices that were displayed to the
	 *            user.
	 * @param view
	 *            The field representing the selected choice
	 * @param position
	 *            The position of the choice within the list (0-based)
	 * @param id
	 *            The id of the row that was chosen (as provided by the _id
	 *            column in the cursor.)
	 */


}
