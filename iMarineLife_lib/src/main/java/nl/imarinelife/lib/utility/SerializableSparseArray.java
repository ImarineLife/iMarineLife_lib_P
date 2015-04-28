package nl.imarinelife.lib.utility;

import java.io.Serializable;

import android.util.SparseArray;

public class SerializableSparseArray<E> extends SparseArray<E> implements Serializable {

	private static final long	serialVersionUID	= 1L;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(int i=0;i<this.size();i++){
			builder.append("["+this.keyAt(i)+"]["+this.get(this.keyAt(i))+"]");
		}
		return builder.toString();
	}
	

}
