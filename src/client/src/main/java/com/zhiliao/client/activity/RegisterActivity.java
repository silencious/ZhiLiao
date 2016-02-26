package com.zhiliao.client.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.zhiliao.R;
import com.zhiliao.client.entity.UserInfo;
import com.zhiliao.client.service.SocketService;
import com.zhiliao.client.service.SocketService.SocketBinder;
import com.zhiliao.client.util.Constant;
import com.zhiliao.server.model.rest.UserModel;

public class RegisterActivity extends Activity {
	private SocketBinder accountBinder;
	private RegisterHandler handler = new RegisterHandler();
	private String gender;
	private RadioButton mGenderMale, mGenderFemale;
	private LinearLayout registerLayout;
	ProgressDialog pd;

	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			accountBinder = (SocketBinder) binder;
			accountBinder.setRegisterHandler(handler);
		}
	};

	public class RegisterHandler extends Handler {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			// success
			case Constant.SUCCESS:
				pd.dismiss();
				Toast.makeText(RegisterActivity.this,
						Constant.REGISTER_SUCCESSED, Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(RegisterActivity.this,
						LoginActivity.class);
				startActivity(intent);
				break;
			// fail
			case Constant.FAIL:
				Toast.makeText(RegisterActivity.this, Constant.REGISTER_FAILED,
						Toast.LENGTH_LONG).show();
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
		setContentView(R.layout.activity_register);

		RadioGroup radiogroup = (RadioGroup) findViewById(R.id.register_gender);
		mGenderMale = (RadioButton) radiogroup.findViewById(R.id.register_male);
		mGenderFemale = (RadioButton) radiogroup
				.findViewById(R.id.register_female);
		gender = Constant.MALE;

		radiogroup
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (checkedId == mGenderMale.getId()) {
							gender = Constant.MALE;
						} else if (checkedId == mGenderFemale.getId()) {
							gender = Constant.FEMALE;
						}
					}
				});

		Button registerButton = (Button) findViewById(R.id.register_btn_register);
		registerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pd = ProgressDialog.show(RegisterActivity.this, Constant.WAIT,
						Constant.REGISTERING, true, false);
				register();
			}
		});

		Button retButton = (Button) findViewById(R.id.register_ret);
		retButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		registerLayout = (LinearLayout) findViewById(R.id.register_layout);
		registerLayout.setOnTouchListener(new OnTouchListener() {
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
		bindService(new Intent(RegisterActivity.this, SocketService.class),
				conn, BIND_AUTO_CREATE);
	};

	@Override
	public void onStop() {
		unbindService(conn);
		super.onStop();
		finish();
	}

	public void register() {
		EditText etUserName = (EditText) findViewById(R.id.register_username);
		EditText etPwd = (EditText) findViewById(R.id.register_pwd);
		EditText etCPwd = (EditText) findViewById(R.id.register_cpwd);
		EditText etEmail = (EditText) findViewById(R.id.register_email);
		String userName = etUserName.getEditableText().toString().trim();
		String pwd = etPwd.getEditableText().toString().trim();
		String cPwd = etCPwd.getEditableText().toString().trim();
		String email = etEmail.getEditableText().toString().trim();

		if (userName.isEmpty() || pwd.isEmpty() || cPwd.isEmpty()) {
			Toast.makeText(RegisterActivity.this,
					Constant.NAME_PWD_EMPTY_ERROR, Toast.LENGTH_SHORT).show();
			pd.dismiss();
		} else if (!pwd.equals(cPwd)) {
			Toast.makeText(RegisterActivity.this, Constant.INCONSISTENT_PWD,
					Toast.LENGTH_SHORT).show();
			pd.dismiss();
		} else if (email.isEmpty()) {
			Toast.makeText(RegisterActivity.this,
					Constant.EMAIL_EMPTY_ERROR, Toast.LENGTH_SHORT).show();
			pd.dismiss();
		} else {
			UserInfo info = new UserInfo();
			info.getPublicInfo().setUsername(userName);
			info.getPrivateInfo().setPassword(pwd);
			info.getPublicInfo().setGender(gender);
			info.getPrivateInfo().setEmail(email);
			accountBinder.register(info.toUserModel());
		}
	}

}
