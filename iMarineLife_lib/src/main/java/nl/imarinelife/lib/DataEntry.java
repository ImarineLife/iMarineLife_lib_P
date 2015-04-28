package nl.imarinelife.lib;

public class DataEntry {
	public String	latinName		= null;
	public String	commonName		= null;
	public String	groupName		= null;
	public int		description_ref	= 0;
	public int		lpic_ref		= 0;
	public int		spic_ref		= 0;	// doubling as id

	public long getId() {
		return (long) spic_ref;
	}
}
