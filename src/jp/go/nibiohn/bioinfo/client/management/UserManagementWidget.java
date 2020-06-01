package jp.go.nibiohn.bioinfo.client.management;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.go.nibiohn.bioinfo.client.generic.DisableableCheckboxCell;
import jp.go.nibiohn.bioinfo.client.generic.ModifiedSimplePager;
import jp.go.nibiohn.bioinfo.shared.GutFloraConstant;
import jp.go.nibiohn.bioinfo.shared.GutFloraLanguagePack;
import jp.go.nibiohn.bioinfo.shared.SampleEntry;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Composite;

import jp.go.nibiohn.bioinfo.client.management.UserManagementAsync;
import jp.go.nibiohn.bioinfo.client.management.UserManagement;
import jp.go.nibiohn.bioinfo.client.GutFloraServiceAsync;
import jp.go.nibiohn.bioinfo.client.GutFloraService;

import jp.go.nibiohn.bioinfo.client.BaseWidget;

public class UserManagementWidget extends BaseWidget {

  private TextBox userIdTb = new TextBox();


  public UserManagementWidget(String name, String link) {
    super(name, link);
  }

  public void getUserInfo() {
    service.getCurrentUser(new AsyncCallback<String>() {
      @Override
      public void onSuccess(String currentUser) {
        System.out.println("############################ 3 ############################");
        GWT.log("############################ 1 ############################");

        RootPanel.get("signUp").clear();
        RootPanel userInfo = RootPanel.get("userInfo");
        userInfo.clear(true);
        HTMLPanel userPanel = new HTMLPanel("");
        userInfo.add(userPanel);
        userPanel.add(new Label(currentUser));
      }

      @Override
      public void onFailure(Throwable caught) {
        warnMessage(SERVER_ERROR);
      }
    });
    createAuthButton();
    createManagementButton();
  }

  private void createSignUpButton() {
    System.out.println("############################ 3-1 ############################");
    GWT.log("############################ 1-1 ############################");
//    rootLogger.log(Level.INFO, "############################ 4-1 ############################");
    final Button signUpBtn = new Button("Sign Up", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        DialogBox dialogBox = createAuthDialogBox("Sign Up");
        dialogBox.setGlassEnabled(true);
        dialogBox.setAnimationEnabled(true);
        dialogBox.setAutoHideEnabled(false);
        dialogBox.center();
        userIdTb.setFocus(true);
      }
    });
    RootPanel.get("signUp").add(signUpBtn);
  }

  private void createAuthButton() {
    service.getCurrentUser(new AsyncCallback<String>() {
      @Override
      public void onSuccess(String currentUser) {
        RootPanel.get("Auth").clear();
        if (currentUser != "Guest") {
          final Button LogoutBtn = new Button("Logout", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              DialogBox dialogBox = createLogoutDialogBox();
              dialogBox.setGlassEnabled(true);
              dialogBox.setAnimationEnabled(true);
              dialogBox.setAutoHideEnabled(false);
              dialogBox.center();
            }
          });
          RootPanel.get("Auth").add(LogoutBtn);
        } else {
          final Button LoginBtn = new Button("Login", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              DialogBox dialogBox = createAuthDialogBox("Login");
              dialogBox.setGlassEnabled(true);
              dialogBox.setAnimationEnabled(true);
              dialogBox.setAutoHideEnabled(false);
              dialogBox.center();
              userIdTb.setFocus(true);
            }
          });
          RootPanel.get("Auth").add(LoginBtn);
        }
      }

      @Override
      public void onFailure(Throwable caught) {
        warnMessage(SERVER_ERROR);
      }
    });
  }

  private void onSuccessOperation(String result, DialogBox dialogBox, Label infoLabel) {
    if (result.equals("success")) {
      getUserInfo();
      dialogBox.hide();
      History.newItem(currentLang + GutFloraConstant.NAVI_LINK_SAMPLE);
      History.fireCurrentHistoryState();
    } else {
      infoLabel.setText(result);
      infoLabel.setStyleName("authError");
    }
  }

  private void onFailureOperation(Label infoLabel, Throwable caught) {
    infoLabel.setText(SERVER_ERROR);
    infoLabel.setStyleName("authError");
  }

  private void createManagementButton() {
    // user_roleを取得
    // user_roleがadminであれば、ユーザ管理画面のダイアログを表示させる
    management.getLoginUserRole(new AsyncCallback<String>() {

      @Override
      public void onSuccess(String result) {
        if (result.equals("admin")) {
          final Button managementBtn = new Button("Management", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              DialogBox dialogBox = createManagementDialogBox();
              dialogBox.setGlassEnabled(true);
              dialogBox.setAnimationEnabled(true);
              dialogBox.setAutoHideEnabled(false);
              dialogBox.center();
            }
          });
          RootPanel.get("management").add(managementBtn);
        }
      }

      @Override
      public void onFailure(Throwable caught) {
        warnMessage("error");
      }
    });
  }

  private DialogBox createManagementDialogBox() {
    // Create a dialog box and set the caption text
    final DialogBox dialogBox = new DialogBox(true);
    dialogBox.setText("Management");


    VerticalPanel vp = new VerticalPanel();
    vp.setSpacing(16);
    vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

    final Label infoLabel = new Label("Logout current user?");
    infoLabel.setStyleName("authInfo");
    vp.add(infoLabel);

    Button logoutBtn = new Button("Logout", new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        service.logoutCurrentUser(new AsyncCallback<Void>() {

          @Override
          public void onSuccess(Void result) {
            onSuccessOperation("success", dialogBox, infoLabel);
            RootPanel.get("management").clear();
          }

          @Override
          public void onFailure(Throwable caught) {
            onFailureOperation(infoLabel, caught);
          }
        });
      }
    });
    Button cancelBtn = new Button("Cancel", new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
      }
    });
    logoutBtn.setWidth("80px");
    cancelBtn.setWidth("80px");

    HorizontalPanel hp = new HorizontalPanel();
    hp.setSpacing(6);
    hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    hp.add(logoutBtn);
    hp.add(cancelBtn);

    vp.add(hp);

    dialogBox.add(vp);
    // Return the dialog box
    return dialogBox;
  }






  private DialogBox createAuthDialogBox(final String type) {
    // Create a dialog box and set the caption text
    final DialogBox dialogBox = new DialogBox(true);
    dialogBox.setText(type);
    VerticalPanel vp = new VerticalPanel();
    vp.setSpacing(6);
    vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

    final Label infoLabel = new Label("Input your ID & password:");
    infoLabel.setStyleName("authInfo");
    vp.add(infoLabel);

    userIdTb.setText("");
    userIdTb.setSize("150px", "18px");
    final TextBox passwordTb = new PasswordTextBox();
    passwordTb.setSize("150px", "18px");
    final TextBox passwordConfirmTb = new PasswordTextBox();
    passwordConfirmTb.setSize("150px", "18px");

    Button authBtn = new Button(type, new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        String username = userIdTb.getText();
        String password = passwordTb.getText();
        String passwordConfirm = passwordConfirmTb.getText();

        if (username == "" || password == "") {
          infoLabel.setText("Please fill both your username and password.");
          infoLabel.setStyleName("authError");
        } else if (password.length() < 8) {
          infoLabel.setText("The password requires at least 8 characters.");
          infoLabel.setStyleName("authError");
        } else if (type == "Sign Up" && !password.equals(passwordConfirm)) {
          infoLabel.setText("The confirmation does not match the password.");
          infoLabel.setStyleName("authError");
        } else {
          if (type == "Sign Up") {
            service.createUser(username, password, passwordConfirm, new AsyncCallback<String>() {
              @Override
              public void onSuccess(String result) {
                onSuccessOperation(result, dialogBox, infoLabel);
              }

              @Override
              public void onFailure(Throwable caught) {
                onFailureOperation(infoLabel, caught);
              }
            });
          } else {
            service.loginUser(username, password, new AsyncCallback<String>() {
              @Override
              public void onSuccess(String result) {
                onSuccessOperation(result, dialogBox, infoLabel);
              }

              @Override
              public void onFailure(Throwable caught) {
                onFailureOperation(infoLabel, caught);
              }
            });
          }
        }
      }
    });
    Button cancelBtn = new Button("Cancel", new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
      }
    });
    authBtn.setWidth("80px");
    cancelBtn.setWidth("80px");

    int row_number = type == "Sign Up" ? 3 : 2;
    Grid grid = new Grid(row_number, 2);
    Label idLabel = new Label("User ID:");
    String labelWidth = type == "Sign Up" ? "120px" : "80px";
    idLabel.setWidth(labelWidth);
    idLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    grid.setWidget(0, 0, idLabel);
    grid.setWidget(0, 1, userIdTb);
    Label pwLabel = new Label("Password:");
    pwLabel.setWidth(labelWidth);
    pwLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    grid.setWidget(1, 0, pwLabel);
    grid.setWidget(1, 1, passwordTb);
    if (type == "Sign Up") {
      Label pwConfirmLabel = new Label("Password Confirm:");
      pwConfirmLabel.setWidth(labelWidth);
      pwConfirmLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
      grid.setWidget(2, 0, pwConfirmLabel);
      grid.setWidget(2, 1, passwordConfirmTb);
    }

    HorizontalPanel hp = new HorizontalPanel();
    hp.setSpacing(6);
    hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    hp.add(authBtn);
    hp.add(cancelBtn);

    vp.add(grid);
    vp.add(hp);

    dialogBox.add(vp);
    // Return the dialog box
    return dialogBox;
  }

  private DialogBox createLogoutDialogBox() {
    // Create a dialog box and set the caption text
    final DialogBox dialogBox = new DialogBox(true);
    dialogBox.setText("Logout");
    VerticalPanel vp = new VerticalPanel();
    vp.setSpacing(16);
    vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

    final Label infoLabel = new Label("Logout current user?");
    infoLabel.setStyleName("authInfo");
    vp.add(infoLabel);

    Button logoutBtn = new Button("Logout", new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        service.logoutCurrentUser(new AsyncCallback<Void>() {

          @Override
          public void onSuccess(Void result) {
            onSuccessOperation("success", dialogBox, infoLabel);
            RootPanel.get("management").clear();
          }

          @Override
          public void onFailure(Throwable caught) {
            onFailureOperation(infoLabel, caught);
          }
        });
      }
    });
    Button cancelBtn = new Button("Cancel", new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
      }
    });
    logoutBtn.setWidth("80px");
    cancelBtn.setWidth("80px");

    HorizontalPanel hp = new HorizontalPanel();
    hp.setSpacing(6);
    hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    hp.add(logoutBtn);
    hp.add(cancelBtn);

    vp.add(hp);

    dialogBox.add(vp);
    // Return the dialog box
    return dialogBox;
  }

}
