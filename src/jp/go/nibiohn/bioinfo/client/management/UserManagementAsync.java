package jp.go.nibiohn.bioinfo.client.management;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GutFloraService</code>.
 */
public interface UserManagementAsync {

  void getLoginUserRole(AsyncCallback<String> callback);

}