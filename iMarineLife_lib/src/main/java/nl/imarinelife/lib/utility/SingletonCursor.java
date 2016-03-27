package nl.imarinelife.lib.utility;

import java.util.ArrayList;
import java.util.List;

import nl.imarinelife.lib.MainActivity;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class SingletonCursor implements Cursor {

	private static final String				TAG			= "SingletonCursor";
	private Cursor							myCursor	= null;
	private static SingletonCursor			me;

	private static List<DataSetObserver>	observers	= new ArrayList<DataSetObserver>();

	private SingletonCursor(Cursor cursor) {
		myCursor = cursor;
	}

	public synchronized static Cursor swapCursor(Cursor newCursor) {
		return swapCursor(newCursor,
			true);
	}

	public synchronized static Cursor swapCursor(Cursor newCursor, boolean closeOldCursor) {
		Log.d(TAG,
			"swapCursor me[" + getCursorStatus(me) + "] newCursor[" + getCursorStatus(newCursor) + "]");
		if (newCursor == null) {
			me = null;
		} else if (me != null && me != newCursor && me.myCursor != newCursor && !newCursor.isClosed()) {
			Cursor oldCursor = me.myCursor;
			me.myCursor = newCursor;
			MainActivity.me.cursors.put(TAG,
				newCursor);
			Log.d(TAG,
				"swapCursor replaceing cursor[" + getCursorStatus(newCursor) + "]");
			if (oldCursor != null && !oldCursor.isClosed() && closeOldCursor) {
				oldCursor.close();
				Log.d(TAG,
					"swapCursor - closed old Cursor");
			}
		}
		if (me == null) {
			me = new SingletonCursor(newCursor);
		}
		return me;
	}

	/*
	 * private static void notifyObservers() { for (DataSetObserver observer :
	 * observers) { observer.onChanged(); } }
	 */

	public static String getCursorStatus(Cursor cursor) {
		if (cursor == null)
			return "null";
		if (cursor.isClosed())
			return "closed";
		return "" + cursor.getCount();
	}

	public static SingletonCursor getCursor() {
		return me;
	}

	@Override
	public void close() {
		if (myCursor != null && !myCursor.isClosed()) {
			myCursor.close();
		}

	}

	@Override
	public void copyStringToBuffer(int arg0, CharArrayBuffer arg1) {
		if (myCursor != null && !myCursor.isClosed()) {
			myCursor.copyStringToBuffer(arg0,
				arg1);
		}
	}

	@Override
	@Deprecated
	public void deactivate() {
		if (myCursor != null && !myCursor.isClosed()) {
			myCursor.deactivate();
		}

	}

	@Override
	public byte[] getBlob(int arg0) {
		if (myCursor == null || myCursor.isClosed())
			return null;
		return myCursor.getBlob(arg0);

	}

	@Override
	public int getColumnCount() {
		if (myCursor == null || myCursor.isClosed())
			return 0;
		return myCursor.getColumnCount();
	}

	@Override
	public int getColumnIndex(String arg0) {
		if (myCursor == null || myCursor.isClosed())
			return 0;
		return myCursor.getColumnIndex(arg0);
	}

	@Override
	public int getColumnIndexOrThrow(String arg0) throws IllegalArgumentException {
		if (myCursor == null || myCursor.isClosed())
			throw new IllegalArgumentException("cursor is invalid");
		return myCursor.getColumnIndexOrThrow(arg0);
	}

	@Override
	public String getColumnName(int columnIndex) {
		if (myCursor == null || myCursor.isClosed())
			return "";
		return myCursor.getColumnName(columnIndex);
	}

	@Override
	public String[] getColumnNames() {
		if (myCursor == null || myCursor.isClosed())
			return null;
		return myCursor.getColumnNames();
	}

	@Override
	public int getCount() {
		if (myCursor == null || myCursor.isClosed())
			return 0;
		return myCursor.getCount();
	}

	@Override
	public double getDouble(int columnIndex) {
		if (myCursor == null || myCursor.isClosed())
			return 0;
		return myCursor.getDouble(columnIndex);
	}

	@Override
	public Bundle getExtras() {
		if (myCursor == null || myCursor.isClosed())
			return null;
		return myCursor.getExtras();
	}

	@Override
	public float getFloat(int columnIndex) {
		if (myCursor == null || myCursor.isClosed())
			return 0;
		return myCursor.getFloat(columnIndex);
	}

	@Override
	public int getInt(int columnIndex) {
		if (myCursor == null || myCursor.isClosed())
			return 0;
		return myCursor.getInt(columnIndex);
	}

	@Override
	public long getLong(int columnIndex) {
		if (myCursor == null || myCursor.isClosed())
			return 0;
		return myCursor.getLong(columnIndex);
	}

	@Override
	public int getPosition() {
		if (myCursor == null || myCursor.isClosed())
			return 0;
		return myCursor.getPosition();
	}

	@Override
	public short getShort(int columnIndex) {
		if (myCursor == null || myCursor.isClosed())
			return 0;
		return myCursor.getShort(columnIndex);
	}

	@Override
	public String getString(int columnIndex) {
		if (myCursor == null || myCursor.isClosed())
			return "";
		return myCursor.getString(columnIndex);
	}

	@SuppressLint("NewApi")
	@Override
	public int getType(int columnIndex) {
		if (myCursor == null || myCursor.isClosed())
			return Cursor.FIELD_TYPE_NULL;
		return myCursor.getType(columnIndex);
	}

	@Override
	public boolean getWantsAllOnMoveCalls() {
		if (myCursor == null || myCursor.isClosed())
			return false;
		return myCursor.getWantsAllOnMoveCalls();

	}

	@Override
	public boolean isAfterLast() {
		if (myCursor == null || myCursor.isClosed())
			return true;
		return myCursor.isAfterLast();
	}

	@Override
	public boolean isBeforeFirst() {
		if (myCursor == null || myCursor.isClosed())
			return true;
		return myCursor.isBeforeFirst();
	}

	@Override
	public boolean isClosed() {
		if (myCursor == null || myCursor.isClosed())
			return true;
		return myCursor.isClosed();
	}

	@Override
	public boolean isFirst() {
		if (myCursor == null || myCursor.isClosed())
			return false;
		return myCursor.isFirst();
	}

	@Override
	public boolean isLast() {
		if (myCursor == null || myCursor.isClosed())
			return false;
		return myCursor.isLast();
	}

	@Override
	public boolean isNull(int columnIndex) {
		if (myCursor == null || myCursor.isClosed())
			return true;
		return myCursor.isNull(columnIndex);
	}

	@Override
	public boolean move(int offset) {
		if (myCursor == null || myCursor.isClosed())
			return false;
		return myCursor.move(offset);
	}

	@Override
	public boolean moveToFirst() {
		if (myCursor == null || myCursor.isClosed())
			return false;
		return myCursor.moveToFirst();
	}

	@Override
	public boolean moveToLast() {
		if (myCursor == null || myCursor.isClosed())
			return false;
		return myCursor.moveToLast();
	}

	@Override
	public boolean moveToNext() {
		if (myCursor == null || myCursor.isClosed())
			return false;
		return myCursor.moveToNext();
	}

	@Override
	public boolean moveToPosition(int position) {
		if (myCursor == null || myCursor.isClosed())
			return false;
		return myCursor.moveToPosition(position);
	}

	@Override
	public boolean moveToPrevious() {
		if (myCursor == null || myCursor.isClosed())
			return false;
		return myCursor.moveToPrevious();
	}

	@Override
	public void registerContentObserver(ContentObserver observer) {
		if (myCursor != null && !myCursor.isClosed()) {
			myCursor.registerContentObserver(observer);
		}

	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		if (myCursor != null && !myCursor.isClosed()) {
			myCursor.registerDataSetObserver(observer);
		}
		if (!observers.contains(observer)) {
			observers.add(observer);
		}

	}

	@Override
	@Deprecated
	public boolean requery() {
		Log.w(TAG,
			"requery called and ignored");
		return true;
	}

	@Override
	public Bundle respond(Bundle extras) {
		if (myCursor == null || myCursor.isClosed())
			return null;
		return myCursor.respond(extras);
	}

	@Override
	public void setNotificationUri(ContentResolver cr, Uri uri) {
		if (myCursor != null && !myCursor.isClosed()) {
			myCursor.setNotificationUri(cr,
				uri);
		}

	}

    @Override
    public Uri getNotificationUri() {
        // not implemented because not used and possible from sdk version 19
		return null;
		//return myCursor.getNotificationUri();
    }

    @Override
	public void unregisterContentObserver(ContentObserver observer) {
		if (myCursor != null && !myCursor.isClosed()) {
			myCursor.unregisterContentObserver(observer);
		}

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		if (myCursor != null && !myCursor.isClosed()) {
			myCursor.unregisterDataSetObserver(observer);
			observers.remove(observer);
		}
	}

}
