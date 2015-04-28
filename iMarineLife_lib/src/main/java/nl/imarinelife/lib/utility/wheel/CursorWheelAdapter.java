package nl.imarinelife.lib.utility.wheel;

import nl.imarinelife.lib.R;
import nl.imarinelife.lib.utility.CursorProvider;
import android.content.Context;

/**
 * The simple Array wheel adapter
 * 
 */
public class CursorWheelAdapter extends AbstractWheelTextAdapter {

	private CursorProvider	cursorProvider;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the current context
	 * @param cursorProvider
	 *            the cursorProvider
	 */
	public CursorWheelAdapter(Context context, CursorProvider cursorProvider) {
		super(context, R.layout.wheel_text_items);
		this.cursorProvider = cursorProvider;
	}

	@Override
	public CharSequence getItemText(int index) {
		return cursorProvider.getValue(index);
	}

	@Override
	public int getItemsCount() {
		return cursorProvider.getCount();
	}

	public int getItem(String value) {
		return cursorProvider.getIndex(value);
		
	}
	
	public Object getObject(int index){
		return cursorProvider.getObject(index);
	}
	
	public Object getMinimalObject(Context ctx, Object... values){
		return cursorProvider.getMinimalObject(ctx, values);
	}
}
