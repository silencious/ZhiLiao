package com.zhiliao.client.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zhiliao.R;
import com.zhiliao.client.entity.UserInfo;
import com.zhiliao.client.entity.UserInfo.PrivateInfo;
import com.zhiliao.client.entity.UserInfo.PublicInfo;
import com.zhiliao.client.service.SocketService;
import com.zhiliao.client.service.SocketService.SocketBinder;
import com.zhiliao.client.util.Constant;
import com.zhiliao.server.model.rest.UserModel;

public class MyInfoActivity extends Activity implements OnTouchListener,
		OnGestureListener {
	private UserInfo user;
	private TextView tvUserName;
	private String gender;
	RadioGroup radiogroup;
	private RadioButton mGenderMale, mGenderFemale;
	private EditText etEmail;
	private Button btnSave,btnReturn;

	private GestureDetector mGestureDetector;
	private ProgressDialog pd;
	private SocketBinder socketBinder;
	private MyInfoHandler handler = new MyInfoHandler();
	ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			socketBinder = (SocketBinder) binder;
			initData();
			socketBinder.setMyInfoHandler(handler);
		}

	};

	public class MyInfoHandler extends Handler {
		public void handleMessage(android.os.Message msg) {
			if (pd != null) {
				pd.dismiss();
				switch (msg.what) {
				// success
				case Constant.SUCCESS:
					Toast.makeText(MyInfoActivity.this,
							Constant.SAVE_SUCCESSED, Toast.LENGTH_SHORT).show();
					break;
				// fail
				case Constant.FAIL:
					Toast.makeText(MyInfoActivity.this, Constant.MODIFY_FAILED,
							Toast.LENGTH_SHORT).show();
					initData();
					break;
				}
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_myinfo);

		mGestureDetector = new GestureDetector(this, this);
		initView();
	}

	private void initView() {
		tvUserName = (TextView) findViewById(R.id.myInfo_username);
		etEmail = (EditText) findViewById(R.id.myInfo_email);
		radiogroup = (RadioGroup) findViewById(R.id.myInfo_gender);
		mGenderMale = (RadioButton) radiogroup.findViewById(R.id.myInfo_male);
		mGenderFemale = (RadioButton) radiogroup
				.findViewById(R.id.myInfo_female);
		btnSave = (Button) findViewById(R.id.myInfo_btn_save);
		btnReturn = (Button) findViewById(R.id.myinfo_ret);

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
		btnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pd = ProgressDialog.show(MyInfoActivity.this, Constant.WAIT,
						Constant.SAVING, true, false);
				saveChanges();
			}
		});
		
		btnReturn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void initData() {
		user = socketBinder.getMyInfo();
		tvUserName.setText(user.getPublicInfo().getUsername());
		etEmail.setText(user.getPrivateInfo().getEmail());
		gender = user.getPublicInfo().getGender();
		if (gender.equals(Constant.MALE)) {
			mGenderMale.setChecked(true);
		} else {
			mGenderFemale.setChecked(true);
		}
	}

	private void saveChanges() {
		String email = etEmail.getEditableText().toString().trim();
		user.getPrivateInfo().setEmail(email);
		user.getPublicInfo().setGender(gender);
		socketBinder.setMyInfo();
	}

	@Override
	public void onStart() {
		super.onStart();
		bindService(new Intent(MyInfoActivity.this, SocketService.class), conn,
				BIND_AUTO_CREATE);
	};

	@Override
	public void onStop() {
		unbindService(conn);
		super.onStop();
		finish();
	}

	// slide support
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TouchEvent dispatcher.
		if (mGestureDetector != null) {
			if (mGestureDetector.onTouchEvent(ev))
				// If the gestureDetector handles the event, a swipe has been
				// executed and no more needs to be done.
				return true;
		}
		return super.dispatchTouchEvent(ev);
	}

	// slide:switch to MainActivity
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e2.getX() - e1.getX() > 100 && Math.abs(velocityX) > 100) {
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);

		}
		return false;
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		return false;
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		return false;
	}
}
