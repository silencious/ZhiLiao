package com.zhiliao.client.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.zhiliao.R;
import com.zhiliao.client.activity.AccountActivity;
import com.zhiliao.client.activity.FrontActivity;
import com.zhiliao.client.entity.UserInfo;
import com.zhiliao.client.service.SocketService;
import com.zhiliao.client.service.SocketService.SocketBinder;
import com.zhiliao.client.util.Constant;

public class AccountActivity extends Activity implements OnTouchListener,
		OnGestureListener {

	private EditText etOldpwd;
	private EditText etNewpwd;
	private EditText etCpwd;
	private Button btnOk, btnLogout;

	private ProgressDialog pd = null;
	private GestureDetector mGestureDetector;
	private SocketBinder socketBinder;
	private AccountHandler handler = new AccountHandler();

	public static enum Msg {
		SUCCESS, WRONG, FAIL
	};

	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			socketBinder = (SocketBinder) binder;
			socketBinder.setAccountHandler(handler);
		}
	};

	public class AccountHandler extends Handler {

		public void handleMessage(android.os.Message msg) {
			if (pd != null) {
				pd.dismiss();
				// update ui according to msg
				switch (msg.what) {// TODO receive msg
				case Constant.SUCCESS:// success
					Toast.makeText(AccountActivity.this,
							Constant.MODIFY_SUCCESS, Toast.LENGTH_SHORT).show();
					overridePendingTransition(R.anim.in_from_left,
							R.anim.out_to_right);
					startActivity(new Intent(AccountActivity.this, LoginActivity.class));
					break;
				case Constant.FAIL:// wrong pwd
					Toast.makeText(AccountActivity.this, Constant.WRONG_PWD,
							Toast.LENGTH_SHORT).show();
					cleanEditBox();
					break;
				case Constant.ERROR:// other problems
					Toast.makeText(AccountActivity.this,
							Constant.MODIFY_FAILED + Constant.CHECK_NETWORK,
							Toast.LENGTH_SHORT).show();
					cleanEditBox();
					break;
				}
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bindService(new Intent(AccountActivity.this, SocketService.class),
				conn, BIND_AUTO_CREATE);
		setContentView(R.layout.activity_account);

		initView();
	}

	private void initView() {
		etOldpwd = (EditText) findViewById(R.id.account_old_pwd);
		etNewpwd = (EditText) findViewById(R.id.account_new_pwd);
		etCpwd = (EditText) findViewById(R.id.account_c_pwd);
		btnOk = (Button) findViewById(R.id.account_btn_ok);
		btnLogout = (Button) findViewById(R.id.account_logout);

		mGestureDetector = new GestureDetector(this, this);

		Button retButton = (Button) findViewById(R.id.account_ret);
		retButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		btnOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				UserInfo myInfo = socketBinder.getMyInfo();
				String oldpwd = etOldpwd.getText().toString().trim();
				String newpwd = etNewpwd.getText().toString().trim();
				String cpwd = etCpwd.getText().toString().trim();
				if (oldpwd.length() == 0 | newpwd.length() == 0
						| cpwd.length() == 0) {
					Toast.makeText(AccountActivity.this, Constant.EMPTY_ERROR,
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (!newpwd.equals(cpwd)) {
					Toast.makeText(AccountActivity.this,
							Constant.INCONSISTENT_PWD, Toast.LENGTH_SHORT)
							.show();
					etNewpwd.setText("");
					etCpwd.setText("");
					return;
				}
				if (!oldpwd.equals(myInfo.getPrivateInfo().getPassword())) {
					// TODO toast to tell user the password is incorrect
					Toast.makeText(AccountActivity.this,
							Constant.INCONSISTENT_PWD, Toast.LENGTH_SHORT)
							.show();
					etOldpwd.setText("");
					return;
				}
				myInfo.getPrivateInfo().setPassword(newpwd);
				socketBinder.setMyInfo();
				pd = ProgressDialog.show(AccountActivity.this, Constant.WAIT,
						Constant.MODIFYING, true, false);
			}
		});
		btnLogout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(AccountActivity.this)
						.setIcon(R.drawable.logo_new_small)
						.setTitle(Constant.LOGOUT)
						.setMessage(Constant.IF_SURE_TO_LOGOUT)
						.setPositiveButton(Constant.OK,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										exitAccount();
									}
								}).setNegativeButton(Constant.CANCEL, null)
						.create().show();
			}
		});
	}

	private void cleanEditBox() {
		etOldpwd.setText("");
		etNewpwd.setText("");
		etCpwd.setText("");
	}

	private void exitAccount() {
		// let socketBinder clear all information
		socketBinder.logout();
		Intent intent = new Intent(AccountActivity.this, FrontActivity.class);
		startActivity(intent);
	}

	@Override
	public void onDestroy() {
		unbindService(conn);
		super.onDestroy();
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

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
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

}
