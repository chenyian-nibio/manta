package jp.go.nibiohn.bioinfo.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DbUser implements IsSerializable {
	
	private String username;

	private String displayName;
	
	private boolean r16s;
	
	private boolean shotgun;

	public DbUser() {
	}

	public DbUser(String username, String displayName, boolean r16s, boolean shotgun) {
		super();
		this.username = username;
		this.displayName = displayName;
		this.r16s = r16s;
		this.shotgun = shotgun;
	}

	public String getUsername() {
		return username;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean canSee16sData() {
		return r16s;
	}

	public boolean canSeeShotgunData() {
		return shotgun;
	}

}
