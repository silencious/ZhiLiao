package com.zhiliao.client.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.zhiliao.R;
import com.zhiliao.client.service.SocketService;
import com.zhiliao.client.service.SocketService.SocketBinder;
import com.zhiliao.client.util.Constant;

public class FollowingsActivity extends Activity implements OnGestureListener {
	private Button buttonBack;
	private ListView mylistview;
	private GestureDetector mGestureDetector;
	private boolean reach_end = false;
	private ArrayList<String> list = new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	private SocketBinder socketBinder;
	private FollowingsHandler handler = new FollowingsHandler();
	private ProgressDialog pd;

	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			socketBinder = (SocketBinder) binder;
			socketBinder.setFollowingsHandler(handler);
			LoadFollowings();
		}
	};

	public class FollowingsHandler extends Handler {
		public void handleMessage(android.os.Message msg) {
			// update ui according to msg
			switch (msg.what) {
			case Constant.ADD_MEMBER_SUCCESS:// Add a member successfully
				pd.dismiss();
				break;
			case Constant.ADD_MEMBER_FAIL:// failed to add a member
				pd.dismiss();
				Toast.makeText(FollowingsActivity.this, Constant.ADD_FAILED,
						Toast.LENGTH_SHORT).show();
				break;
			case Constant.DELETE_MEMBER_SUCCESS:// delete a member successfully
				pd.dismiss();
				break;
			case Constant.DELETE_MEMBER_FAIL:// failed to delete a member
				pd.dismiss();
				Toast.makeText(FollowingsActivity.this, Constant.REMOVE_FAILED,
						Toast.LENGTH_SHORT).show();
				break;
			case Constant.LOAD_DATA:
				if (pd != null) {
					pd.dismiss();
					if (!reach_end) {
						LoadFollowings();
					}
				}
				break;
			case Constant.REACH_END:
				pd.dismiss();
				reach_end = true;
				break;
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bindService(new Intent(FollowingsActivity.this, SocketService.class),
				conn, BIND_AUTO_CREATE);
		setContentView(R.layout.activity_followings);
		init();
	}

	@Override
	public void onDestroy() {
		unbindService(conn);
		super.onDestroy();
	}

	private void init() {
		buttonBack = (Button) findViewById(R.id.followings_back);
		if (buttonBack != null) {
			buttonBack.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					finish();
					overridePendingTransition(R.anim.in_from_left,
							R.anim.out_to_right);
				}
			});
		}
		mylistview = (ListView) findViewById(R.id.followings_list);
		mGestureDetector = new GestureDetector(this, this);
	}

	// get Followings' name
	private void LoadFollowings() {
		if (socketBinder != null) {
			pd = ProgressDialog.show(FollowingsActivity.this, Constant.WAIT,
					Constant.LOADING, true, false);
			list.clear();
			list.addAll(socketBinder.getFollowingNames());
			System.out.println("load " + list.size() + " followers");
			if (adapter != null) {
				adapter.notifyDataSetChanged();
			}
		}
		if (list != null && adapter == null) {
			// adapter need to be improved next time
			adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, list);
			mylistview.setAdapter(adapter);
			mylistview
					.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int position, long arg3) {
							Intent intent = new Intent();
							intent.setClass(FollowingsActivity.this,
									UserActivity.class);
							Bundle b = new Bundle();
							b.putString("friend", list.get(position).toString());
							intent.putExtras(b);
							startActivity(intent);

						}
					});
		}
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

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e2.getX() - e1.getX() > 100 && Math.abs(velocityX) > 100) {
			// Intent intent = new Intent(ChatActivity.this,
			// MainActivity.class);
			// startActivity(intent);
			// Toast.makeText(this, "向左手势", Toast.LENGTH_SHORT).show();
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
}