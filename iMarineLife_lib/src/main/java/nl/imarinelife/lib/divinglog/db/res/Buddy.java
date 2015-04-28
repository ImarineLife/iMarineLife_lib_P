package nl.imarinelife.lib.divinglog.db.res;

import java.io.Serializable;
import java.util.HashMap;

import android.util.Log;

public class Buddy implements Serializable {
	/**
	 * 
	 */
	private static final String		TAG									= "Buddy";
	private static final long		serialVersionUID					= 1L;
	private String					name								= null;
	private String					email								= null;
	private HashMap<String, String>	codeForCatalog						= null;
	private String					buddyNameSelected					= "";
	private boolean					buddyNameMustbeChangedEveryWhere	= false;
	private boolean					buddyEmailChanged					= false;
	private boolean					buddyCodeChanged					= false;

	public Buddy() {
	}

	public Buddy(String name, String email, HashMap<String, String> codeForCatalog) {
		super();
		this.setName(name);
		this.setEmail(email);
		this.setCodeForCatalog(codeForCatalog);
	}

	public Buddy(String name, String email, String code, String catalog) {
		super();
		this.setName(name);
		this.setEmail(email);
		this.setCodeForCatalog(new HashMap<String, String>());
		this.getCodeForCatalog().put(catalog,
			code);
	}

	public Buddy(int id, String name, String email, String code, String catalog) {
		super();
		this.setName(name);
		this.setEmail(email);
		this.setCodeForCatalog(new HashMap<String, String>());
		this.getCodeForCatalog().put(catalog,
			code);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("name[" + getName() + "]");
		builder.append("email[" + getEmail() + "]");
		builder.append("codeForCatalog[" + getCodeForCatalog() + "]");
		return builder.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		Log.d(TAG,
			"setName: " + name);
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		Log.d(TAG,
			"setEmail: " + email);
		this.email = email;
	}

	public HashMap<String, String> getCodeForCatalog() {
		return codeForCatalog;
	}
	
	public void setCodeForCatalog(HashMap<String, String> codeForCatalog) {
		Log.d(TAG,
			"setCodeForCatalog: " + codeForCatalog);
		this.codeForCatalog = codeForCatalog;
	}

	public void setCodeForCatalog(String catalog, String code) {
		Log.d(TAG,
			"setCodeForCatalog: " + catalog + ":" + code);
		if (this.codeForCatalog == null) {
			codeForCatalog = new HashMap<String, String>();
		}
		codeForCatalog.put(catalog,
			code);
	}

	public String getBuddyNameSelected() {
		return buddyNameSelected;
	}

	public void setBuddyNameSelected(String buddyNameSelected) {
		Log.d(TAG,
			"setBuddyNameSelected: " + buddyNameSelected);
		this.buddyNameSelected = buddyNameSelected;
	}

	public boolean isBuddyNameMustbeChangedEveryWhere() {
		return buddyNameMustbeChangedEveryWhere;
	}

	public void setBuddyNameMustbeChangedEveryWhere(boolean buddyNameMustbeChangedEveryWhere) {
		Log.d(TAG,
			"setBuddyNameMustbeChangedEveryWhere: " + buddyNameMustbeChangedEveryWhere);
		this.buddyNameMustbeChangedEveryWhere = buddyNameMustbeChangedEveryWhere;
	}

	public boolean isBuddyEmailChanged() {
		return buddyEmailChanged;
	}

	public void setBuddyEmailChanged(boolean buddyEmailChanged) {
		Log.d(TAG,
			"setBuddyEmailChanged: " + buddyEmailChanged);
		this.buddyEmailChanged = buddyEmailChanged;
	}

	public boolean isBuddyCodeChanged() {
		return buddyCodeChanged;
	}

	public void setBuddyCodeChanged(boolean buddyCodeChanged) {
		Log.d(TAG,
			"setBuddyCodeChanged: " + buddyCodeChanged);
		this.buddyCodeChanged = buddyCodeChanged;
	}

	public void removeCodeForCatalog(String catalog) {
		if (codeForCatalog != null) {
			codeForCatalog.remove(catalog);
		}

	}

	public String getCodeForCatalog(String catalog) {
		if (codeForCatalog == null)
			return null;
		else {
			String code = codeForCatalog.get(catalog);
			if (code==null || code.trim().equals("")) {
				return null;
			} else {
				return code;
			}
		}
	}
}
