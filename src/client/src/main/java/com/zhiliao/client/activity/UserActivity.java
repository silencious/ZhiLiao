package com.zhiliao.client.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zhiliao.R;
import com.zhiliao.client.entity.UserInfo.PublicInfo;
import com.zhiliao.client.service.SocketService;
import com.zhiliao.client.service.SocketService.SocketBinder;
import com.zhiliao.client.util.Constant;
import com.zhiliao.server.model.rest.FollowModel;

public class UserActivity extends Activity {
	private PublicInfo userInfo;
	private String userName;
	private FollowModel followModel;
	private SocketBinder socketBinder;
	private UserHandler handler = new UserHandler();

	private Context context;

	private Button button;
	private TextView tvName, tvGender;

	private ProgressDialog pd;
	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			socketBinder = (SocketBinder) binder;
			socketBinder.setUserHandler(handler);
			loadUser(userName);
		}
	};

	public class UserHandler extends Handler {
		public void handleMessage(android.os.Message msg) {
			// update ui according to msg
			if (pd != null) {
				pd.dismiss();
			}
			switch (msg.what) {
			case Constant.FOLLOW_SUCCESS: {// friend added succeed
				System.out.println("Follow success");
				followModel = (FollowModel) msg.obj;
				if (userInfo.getFollowedBranch().equals(
						followModel.getFollowed())) {
					button.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.unfollow));
				} else {
					followModel = null;
				}
				break;
			}
			case Constant.FOLLOW_FAIL: {// friend added failed
				System.out.println("Follow fail");
				if (userInfo.getFollowedBranch().equals(msg.obj)) {
					Toast.makeText(context, Constant.FOCUS_FAILED,
							Toast.LENGTH_SHORT).show();
				}
				break;
			}
			case Constant.UNFOLLOW_SUCCESS: {// friend deleted succeed
				System.out.println("Unfollow success");
				if (userInfo.getFollowedBranch().equals(msg.obj)) {
					button.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.follow));
					followModel = null;
				}
				break;
			}
			case Constant.UNFOLLOW_FAIL: {// friend deleted failed
				System.out.println("Unfollow fail");
				if (userInfo.getFollowedBranch().equals(msg.obj)) {
					Toast.makeText(context, Constant.CANCEL_FOCUS_FAILED,
							Toast.LENGTH_SHORT).show();
				}
				break;
			}
			case Constant.LOAD_DATA: {	// get user's follow status
				System.out.println("IfFollow has got its result");
				followModel = (FollowModel) msg.obj;
				if (userInfo.getFollowedBranch().equals(
						followModel.getFollowed())) {
					if (msg.arg1 == 0) {	// not following
						followModel = null;
					}
					initAction();
				}
				break;
			}
			case Constant.ERROR: {
				Toast.makeText(context, Constant.LOAD_ERROR, Toast.LENGTH_SHORT)
						.show();
				break;
			}
			}
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bindService(new Intent(UserActivity.this, SocketService.class), conn,
				BIND_AUTO_CREATE);
		userName = getIntent().getExtras().getString("friend");
		initView();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onDestroy() {
		unbindService(conn);
		super.onDestroy();
	}

	public void initView() {
		setContentView(R.layout.activity_user);
		context = getApplicationContext();

		button = (Button) findViewById(R.id.friend_action);
		tvName = (TextView) findViewById(R.id.user_name);
		tvGender = (TextView) findViewById(R.id.user_gender);

		Button retButton = (Button) findViewById(R.id.user_ret);
		retButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	public void initAction() {
		if (followModel != null) {
			button.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.unfollow));
		} else {
			button.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.follow));
		}
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pd = ProgressDialog.show(UserActivity.this, Constant.WAIT,
						Constant.WAITING, true, false);
				if (followModel != null) {
					socketBinder.unfollow(followModel);
				} else {
					socketBinder.follow(userInfo.getFollowedBranch());
				}
			}
		});
	}

	private void loadUser(String name) {
		userInfo = socketBinder.getUserInfo(name);
		if (userInfo != null && tvName != null) {
			tvName.setText(userInfo.getUsername());
		}
		if (userInfo != null && tvGender != null) {
				tvGender.setText(userInfo.getGender());
		}
		if (userInfo == null || button.getText().toString().length() == 0) {
			pd = ProgressDialog.show(UserActivity.this, Constant.WAIT,
					Constant.LOADING, true, false);
		}
		socketBinder.ifFollowing(name);
	}
}