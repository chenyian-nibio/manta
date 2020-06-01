package jp.go.nibiohn.bioinfo.client.management;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;


@RemoteServiceRelativePath("management")
public interface UserManagement extends RemoteService {

  String getLoginUserRole();

}
