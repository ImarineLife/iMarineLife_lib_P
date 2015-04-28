package nl.imarinelife.lib;

import android.content.Context;
import android.database.SQLException;

public interface DbHelper {
	
	public abstract DbHelper open(Context mCtx) throws SQLException;

	public abstract void finalize() throws Throwable;

	public abstract void close();
}