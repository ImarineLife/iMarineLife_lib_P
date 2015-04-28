package nl.imarinelife.lib.divinglog.db.dive;

import nl.imarinelife.lib.divinglog.db.res.ProfilePart;

public class DiveProfilePart {
	public int			diveNr				= 0;
	public int			stayValueInMeters	= 0;
	public ProfilePart	profilePart			= null;

	public DiveProfilePart(int diveNr, int stayValueInMeters, ProfilePart profilePart) {
		super();
		this.diveNr = diveNr;
		this.stayValueInMeters = stayValueInMeters;
		this.profilePart = profilePart;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("profilePart["+profilePart+"]");
		builder.append("stayValueInMeters["+stayValueInMeters+"]");
		return builder.toString();
	}
}
