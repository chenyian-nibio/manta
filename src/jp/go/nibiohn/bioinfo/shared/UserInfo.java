package jp.go.nibiohn.bioinfo.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class UserInfo implements IsSerializable {
	
	private String userName;

	private String displayName;
	
	private boolean admin;
	
	private boolean login;

	public UserInfo() {
	}

	public UserInfo(String userName, String displayName, boolean admin, boolean login) {
		this.userName = userName;
		this.displayName = displayName;
		this.admin = admin;
		this.login = login;
	}
	
	public String getUserName() {
		return userName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean isAdmin() {
		return admin;
	}

	public boolean isLogin() {
		return login;
	}

	public static UserInfo getGuestUser() {
		return new UserInfo(GUEST_USER_NAME, GUEST_DISPLAY_NAME, false, false);
	}
	
	public static String GUEST_USER_NAME = "demo";
	public static String GUEST_DISPLAY_NAME = "Guest";

	public static String ADMIN_ROLE = "admin";
	
}
