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
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.zhiliao.R;
import com.zhiliao.client.service.SocketService;
import com.zhiliao.client.service.SocketService.SocketBinder;
import com.zhiliao.client.util.Constant;

public class FollowersActivity extends Activity implements OnTouchListener,
		OnGestureListener {
	private Button buttonBack;
	private ListView mylistview;
	private GestureDetector mGestureDetector;
	private boolean reach_end = false;
	private ArrayList<String> list = new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	private SocketBinder socketBinder;
	private FollowersHandler handler = new FollowersHandler();
	private ProgressDialog pd;

	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			socketBinder = (SocketBinder) binder;
			socketBinder.setFollowersHandler(handler);
			LoadFollowers();
		}
	};

	public class FollowersHandler extends Handler {
		public void handleMessage(android.os.Message msg) {
			// update ui according to msg
			switch (msg.what) {
			case Constant.LOAD_DATA:
				if (pd != null) {
					pd.dismiss();
					if (!reach_end) {
						LoadFollowers();
					}
				}
				break;
			case Constant.FAIL:

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
		setContentView(R.layout.activity_followers);
		bindService(new Intent(FollowersActivity.this, SocketService.class),
				conn, BIND_AUTO_CREATE);
		init();
	}

	@Override
	public void onDestroy() {
		unbindService(conn);
		super.onDestroy();
	}

	public void init() {
		buttonBack = (Button) findViewById(R.id.followers_back);
		buttonBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
				overridePendingTransition(R.anim.in_from_left,
						R.anim.out_to_right);
			}
		});

		mylistview = (ListView) findViewById(R.id.followers_list);
		mGestureDetector = new GestureDetector(this, this);
	}

	// get Followers' name
	private void LoadFollowers() {
		if (socketBinder != null) {
			pd = ProgressDialog.show(FollowersActivity.this, Constant.WAIT,
					Constant.LOADING, true, false);
			list.clear();
			list.addAll(socketBinder.getFollowerNames());
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
							intent.setClass(FollowersActivity.this,
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

	// slide:switch to MainActivity
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