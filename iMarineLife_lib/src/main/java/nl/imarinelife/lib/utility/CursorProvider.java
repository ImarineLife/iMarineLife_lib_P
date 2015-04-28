package nl.imarinelife.lib.utility;

import java.io.Serializable;

import android.content.Context;
import android.database.Cursor;

public interface CursorProvider extends Serializable {
	public Cursor getCursor();

	public String getValue(int index);

	public Object getObject(int index);

	public int getIndex(String value);

	public int getCount();

	public void refresh(Context ctx);

	public boolean alreadyExists(Context ctx, Object... values);

	public void insert(Context ctx, Object... values);

	public int remove(Context ctx, Object... values);

	public int checkRemoval(Context ctx, Object... values);

	public Object getMinimalObject(Context ctx, Object... values);
}
