package com.zhiliao.client.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zhiliao.R;
import com.zhiliao.client.service.SocketService;
import com.zhiliao.client.service.SocketService.SocketBinder;
import com.zhiliao.client.util.Constant;
import com.zhiliao.server.model.rest.UserModel;

public class LoginActivity extends Activity {
	private SocketBinder chatBinder;
	private LoginHandler handler = new LoginHandler();
	private CustomProgressDialog pd;
	private LinearLayout loginLayout;

	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			chatBinder = (SocketBinder) binder;
		}
	};

	public class LoginHandler extends Handler {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Constant.REGISTERED: // success
				pd.dismiss();
				Intent intent = new Intent(LoginActivity.this,
						MainActivity.class);
				startActivity(intent);
				FrontActivity.instance.finish();
				finish();
				break;
			case Constant.FAIL: // wrong name or pwd
				Toast.makeText(
						LoginActivity.this,
						Constant.LOGIN_FAILED + Constant.COMMA
								+ Constant.CHECK_NAME_PWD, Toast.LENGTH_SHORT)
						.show();
				((EditText) findViewById(R.id.login_user)).setText("");
				((EditText) findViewById(R.id.login_pwd)).setText("");
				pd.dismiss();
				break;
			case Constant.ERROR: // network error
				Toast.makeText(
						LoginActivity.this,
						Constant.LOGIN_FAILED + Constant.COMMA
								+ Constant.CHECK_NETWORK, Toast.LENGTH_SHORT)
						.show();
				SharedPreferences sp = getSharedPreferences("auth",
						MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.putString("pwd", null);
				editor.commit();
				pd.dismiss();
				break;
			}
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectDiskReads().detectDiskWrites().detectNetwork()
				.penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects()
				// .detectLeakedClosableObjects()
				.penaltyLog().penaltyDeath().build());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		Button login_button = (Button) findViewById(R.id.login_button);
		login_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pd = new CustomProgressDialog(LoginActivity.this, Constant.LOADING,
						R.anim.frame);
				pd.show();
				login();
			}
		});
		
		Button retButton = (Button) findViewById(R.id.login_ret);
		retButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginActivity.this, FrontActivity.class));
				finish();
			}
		});
		
		loginLayout = (LinearLayout) findViewById(R.id.login_layout);
		loginLayout.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent arg1) {
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				return imm.hideSoftInputFromWindow(getCurrentFocus()
						.getWindowToken(), 0);
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		bindService(new Intent(LoginActivity.this, SocketService.class), conn,
				BIND_AUTO_CREATE);
		SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
		String name = sp.getString("name", null);
		if (name != null && !name.isEmpty()) {
			((EditText) findViewById(R.id.login_user)).setText(name);
		}
	};

	@Override
	public void onStop() {
		unbindService(conn);
		super.onStop();
		finish();
	}

	@Override
	public void onBackPressed() {
		startActivity(new Intent(LoginActivity.this, FrontActivity.class));
		finish();
	}
	
	private void login() {
		EditText etUserName = (EditText) findViewById(R.id.login_user);
		EditText etPwd = (EditText) findViewById(R.id.login_pwd);
		String userName = etUserName.getEditableText().toString().trim();
		String pwd = etPwd.getEditableText().toString().trim();

		if (userName.isEmpty() || pwd.isEmpty()) {
			Toast.makeText(LoginActivity.this, Constant.NAME_PWD_EMPTY_ERROR,
					Toast.LENGTH_SHORT).show();
			pd.dismiss();
		} else {
			UserModel user = new UserModel();
			user.setUsername(userName);
			user.setPassword(pwd);
			rememberMe(user);
			chatBinder.login(handler);
		}
	}

	// save user information, can be used for later verification
	private void rememberMe(UserModel user) {
		SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString("name", user.getUsername());
		editor.putString("pwd", user.getPassword());
		editor.commit();
	}
}
