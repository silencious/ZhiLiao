package com.zhiliao.client.activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
//import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.view.ViewHelper;
import com.zhiliao.R;
import com.zhiliao.client.controller.FilterController;
import com.zhiliao.client.entity.TopicEntity;
import com.zhiliao.client.service.SocketService;
import com.zhiliao.client.service.SocketService.SocketBinder;
import com.zhiliao.client.swipemenu.SwipeMenu;
import com.zhiliao.client.swipemenu.SwipeMenuCreator;
import com.zhiliao.client.swipemenu.SwipeMenuItem;
import com.zhiliao.client.swipemenu.SwipeMenuListView;
import com.zhiliao.client.swipemenu.SwipeMenuListView.OnMenuItemClickListener;
import com.zhiliao.client.swipemenu.SwipeMenuListView.OnSwipeListener;
import com.zhiliao.client.util.Constant;

/**
 * 滑动菜单Demo主Activity
 * 
 */

public class MainActivity extends FragmentActivity implements
		SwipeRefreshLayout.OnRefreshListener {
	private int btnFlag = Constant.SESSION;
	private boolean reach_end = false;
	private SocketBinder socketBinder;
	private MainHandler handler = new MainHandler();
	// sliding menu & refreshable view
	// private SwipeRefreshLayout mSwipeLayout;
	private DrawerLayout mDrawerLayout;
	/**
	 * 在内容布局上显示的ListView
	 */

	private RelativeLayout headBar;
	private RelativeLayout searchBar;
	private ImageButton createButton;
	private ImageView mainLine;
	private Button btnSearch;
	private EditText etKey;
	private Button bFollower, bFollowing, bMyinfo, bAccount, bSet, bUpload,
			bDiscover, bTimeline;

	private SwipeMenuListView topicItemsList;
	/**
	 * ListView的适配器
	 */
	private MainAdapter topicItemsAdapter;
	private TimelineAdapter timelineAdapter;

	/**
	 * 用于填充contentListAdapter的数据源。
	 */
	private LinkedList<TopicEntity> topicContents = new LinkedList<TopicEntity>();

	/*
	 * 待更改 private ArrayList<String> rightToAdd = new ArrayList<String>();
	 * private ArrayList<String> rightAdded = new ArrayList<String>();
	 */

	FilterController filterController;

	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			System.out.println("Main connect with socket");
			socketBinder = (SocketBinder) binder;
			socketBinder.setMainHandler(handler);
			socketBinder.login(handler);
		}
	};

	public class MainHandler extends Handler {
		public void handleMessage(android.os.Message msg) {
			// update ui according to msg
			switch (msg.what) {
			case Constant.LOAD_DATA: // service notify to update topics on main
										// activity
				reach_end = true;
				loadTopics();
				break;
			case Constant.FAIL: // wrong username or password
				Toast.makeText(MainActivity.this,
						Constant.LOGIN_FAILED + "，" + Constant.CHECK_NAME_PWD,
						Toast.LENGTH_SHORT).show();
				startActivity(new Intent(MainActivity.this, FrontActivity.class));
				break;
			case Constant.ERROR: // TODO network problem, should tell user
				Toast.makeText(MainActivity.this,
						Constant.LOGIN_FAILED + "，" + Constant.CHECK_NETWORK,
						Toast.LENGTH_SHORT).show();
				startActivity(new Intent(MainActivity.this, LoginActivity.class));
				break;
			case Constant.IGNORE:
				int p = Integer.parseInt(msg.obj.toString());
				TopicEntity item = topicContents.get(p);
				socketBinder.ignore(item.getTopicId());
				topicContents.set(p, item);
				topicItemsAdapter.notifyDataSetChanged();
				break;
			case Constant.REACH_END:
				reach_end = true;
				break;
			case Constant.CLEAR:
				topicContents.clear();
				topicItemsAdapter.notifyDataSetChanged();
				timelineAdapter.notifyDataSetChanged();
				break;
			}
		}
	}

	public MainHandler getHandler() {
		return this.handler;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("Create MainActivity");
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.permitAll().build());
		bindService(new Intent(MainActivity.this, SocketService.class), conn,
				BIND_AUTO_CREATE);
		initMainView();

		initLeftView();
		setListener();
	}

	@Override
	public void onStart() {
		super.onStart();
		System.out.println("Start MainActivity");
		SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
		String username = sp.getString("name", null);
		String pwd = sp.getString("pwd", null);
		if (pwd == null || username == null) {
			System.out.println("No auth, enter FrontActivity");
			startActivity(new Intent(MainActivity.this, FrontActivity.class));
		}
	}

	@Override
	public void onRestart() {
		super.onRestart();
		if (socketBinder != null) {
			System.out.println("Reconnect from MainActivity");
			socketBinder.login(handler);
		}
		switch(btnFlag){
		case Constant.SESSION:
		case Constant.DISCOVER:
			topicItemsList.setAdapter(topicItemsAdapter);
			topicItemsAdapter.notifyDataSetChanged();
			break;
		case Constant.TIMELINE:
		case Constant.TREND:
			topicItemsList.setAdapter(timelineAdapter);
			timelineAdapter.notifyDataSetChanged();
			break;
		}		
	}

	@Override
	public void onStop() {
		super.onStop();
		if (socketBinder != null) {
			socketBinder.setMyInfo();
		}
	}

	@Override
	public void onDestroy() {
		unbindService(conn);
		super.onDestroy();
	}

	// press back button : don't destroy the activity, hide current activity
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
	}

	private void initMainView() {
		setContentView(R.layout.activity_main);

		createButton = (ImageButton) findViewById(R.id.main_create);
		mainLine = (ImageView) findViewById(R.id.main_line);
		mainLine.setVisibility(View.GONE);

		headBar = (RelativeLayout) findViewById(R.id.main_head);
		bUpload = (Button) findViewById(R.id.main_upload);
		bDiscover = (Button) findViewById(R.id.main_discover);
		bTimeline = (Button) findViewById(R.id.main_timeline);

		searchBar = (RelativeLayout) findViewById(R.id.main_search);
		btnSearch = (Button) findViewById(R.id.main_btn_search);
		etKey = (EditText) findViewById(R.id.main_et_key);
		searchBar.setVisibility(View.GONE);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.id_drawerLayout);
		mDrawerLayout.setScrimColor(Color.TRANSPARENT);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
				Gravity.RIGHT);// 关闭右侧菜单的滑动出现效果
		// mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.id_swipe_ly);

		// mSwipeLayout.setOnRefreshListener(this);
		// mSwipeLayout.setColorScheme(android.R.color.holo_green_dark,
		// android.R.color.holo_green_light,
		// android.R.color.holo_orange_light,
		// android.R.color.holo_red_light);
		topicItemsList = (SwipeMenuListView) findViewById(R.id.main_content);
		topicItemsAdapter = new MainAdapter(topicContents);
		timelineAdapter = new TimelineAdapter(topicContents);
		topicItemsList.setAdapter(topicItemsAdapter);

		WindowManager wm = (WindowManager) getBaseContext().getSystemService(
				Context.WINDOW_SERVICE);
		final int windowwidth = wm.getDefaultDisplay().getWidth();
		SwipeMenuCreator creator = new SwipeMenuCreator() {
			@Override
			public void create(SwipeMenu menu) {
				SwipeMenuItem deleteItem = new SwipeMenuItem(
						getApplicationContext());
				// set item background
				// deleteItem.setBackground(new ColorDrawable(Color.rgb(0xff,
				// 0xff, 0xff)));
				// set item width
				deleteItem.setWidth(windowwidth);
				deleteItem.setBackground(new ColorDrawable(Color.RED));
				// set a icon
				// deleteItem.setIcon(R.drawable.block);
				// add to menu
				menu.addMenuItem(deleteItem);
			}
		};
		topicItemsList.setMenuCreator(creator);
		initEvents();
		topicItemsList.setMainActivity(this);

	}

	private void initLeftView() {
		bFollower = (Button) findViewById(R.id.main_left_follower);
		bFollowing = (Button) findViewById(R.id.main_left_following);
		bMyinfo = (Button) findViewById(R.id.main_left_myinfo);
		bAccount = (Button) findViewById(R.id.main_left_account);
		bSet = (Button) findViewById(R.id.main_left_set);
	}

	private void setListener() {
		// main comtent listener
		headBar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (topicItemsList != null && topicContents.size() > 0) {
					topicItemsList.smoothScrollBy(3, 30);
					if (topicItemsList.getFirstVisiblePosition() > 18) {
						topicItemsList.setSelection(18);
					}
					topicItemsList.smoothScrollToPosition(0);
				}
			}
		});

		btnSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				String key = etKey.getText().toString().trim();
				if (key.length() == 0) {
					Toast.makeText(MainActivity.this, Constant.EMPTY_ERROR,
							Toast.LENGTH_SHORT).show();
				} else {
					socketBinder.search(key);
				}
			}
		});

		createButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// InputMethodManager imm = (InputMethodManager)
				// getSystemService(Context.INPUT_METHOD_SERVICE);
				// imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				// final EditText inputmsg = new EditText(context);
				// inputmsg.setBackgroundColor(Color.RED);
				final AlertDialog dlg = new AlertDialog.Builder(
						MainActivity.this).create();
				LayoutInflater inflater = (LayoutInflater) getApplicationContext()
						.getSystemService(LAYOUT_INFLATER_SERVICE);
				RelativeLayout layout = (RelativeLayout) inflater.inflate(
						R.layout.msgdialog, null);
				dlg.setView(layout);

				dlg.show();
				Window window = dlg.getWindow();
				window.setContentView(R.layout.msgdialog);

				final EditText edit_msg = (EditText) dlg
						.findViewById(R.id.txt_username);

				Button ok = (Button) window.findViewById(R.id.btn_ok);
				ok.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (edit_msg.getText() == null
								|| edit_msg.getText().toString().trim()
										.equals("")) {
							Toast.makeText(MainActivity.this,
									Constant.CREATE_MSG_ERROR,
									Toast.LENGTH_LONG).show();
							return;
						}
						String msg2 = edit_msg.getText().toString().trim();
						Intent intent = new Intent();
						intent.setClass(MainActivity.this, ChatActivity.class);
						Bundle b = new Bundle();
						b.putString("msg", msg2);
						intent.putExtras(b);
						startActivity(intent);
						dlg.dismiss();
					}
				});
				Button cancel = (Button) window.findViewById(R.id.btn_cancel);
				cancel.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						dlg.cancel();
					}
				});
			}

		});
		bUpload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (btnFlag == Constant.SESSION) {
					return;
				}
				btnFlag = Constant.SESSION;
				bTimeline.setBackgroundResource(R.drawable.timeline_button);
				bDiscover.setBackgroundResource(R.drawable.discover_button);
				bUpload.setBackgroundResource(R.drawable.chat_button_pressed);
				loadTopics();
				mainLine.setVisibility(View.GONE);
				searchBar.setVisibility(View.GONE);
				createButton.setVisibility(View.VISIBLE);
			}
		});
		bDiscover.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (btnFlag == Constant.DISCOVER) {
					return;
				}
				btnFlag = Constant.DISCOVER;
				bTimeline.setBackgroundResource(R.drawable.timeline_button);
				bDiscover
						.setBackgroundResource(R.drawable.discover_button_pressed);
				bUpload.setBackgroundResource(R.drawable.chat_button);
				loadTopics();
				mainLine.setVisibility(View.GONE);
				searchBar.setVisibility(View.VISIBLE);
				createButton.setVisibility(View.GONE);
			}
		});
		bTimeline.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (btnFlag == Constant.TREND) {
					return;
				}
				btnFlag = Constant.TREND;
				bTimeline
						.setBackgroundResource(R.drawable.timeline_button_pressed);
				bDiscover.setBackgroundResource(R.drawable.discover_button);
				bUpload.setBackgroundResource(R.drawable.chat_button);
				loadTopics();
				mainLine.setVisibility(View.VISIBLE);
				searchBar.setVisibility(View.GONE);
				createButton.setVisibility(View.GONE);
			}
		});
		topicItemsList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, ChatActivity.class);
				Bundle bundle = new Bundle();
				bundle.putLong("topicId", topicContents.get(position)
						.getTopicId());
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		topicItemsList
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public void onMenuItemClick(int position, SwipeMenu menu,
							int index) {
						TopicEntity item = topicContents.get(position);
						switch (index) {
						case 0:
							// topicContents_.remove(position);
							item.setIgnore(true);
							socketBinder.ignore(topicContents.get(position)
									.getTopicId());
							topicContents.set(position, item);
							topicItemsAdapter.notifyDataSetChanged();
							break;
						case 1:

							break;
						}
					}
				});

		// set SwipeListener
		topicItemsList.setOnSwipeListener(new OnSwipeListener() {
			@Override
			public void onSwipeStart(int position) {
				topicItemsList.setPosition(position);
			}

			@Override
			public void onSwipeEnd(int position) {
			}
		});
		topicItemsList
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						return false;
					}
				});
		// left menu listener
		bFollower.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, FollowersActivity.class);
				startActivity(intent);
			}
		});
		bFollowing.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, FollowingsActivity.class);
				startActivity(intent);
			}
		});
		bMyinfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, MyInfoActivity.class);
				startActivity(intent);
			}
		});
		bAccount.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent();
				// TODO: account activity
				intent.setClass(MainActivity.this, AccountActivity.class);
				startActivity(intent);
			}
		});
		bSet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, AboutActivity.class);
				startActivity(intent);
			}
		});
	}

	private void initEvents() {
		// 设置监听
		mDrawerLayout.setDrawerListener(new DrawerListener() {

			@Override
			public void onDrawerStateChanged(int newState) {
			}

			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				View mContent = mDrawerLayout.getChildAt(0);
				View mMenu = drawerView;
				float scale = 1 - slideOffset;
				if (drawerView.getTag().equals(
						getResources().getString(R.string.left_tag))) {// 展开左侧菜单
					float leftScale = 1 - 0.3f * scale;
					float rightScale = 0.8f + scale * 0.2f;
					// 设置左侧菜单缩放效果
					ViewHelper.setScaleX(mMenu, leftScale);
					ViewHelper.setScaleY(mMenu, leftScale);
					ViewHelper.setAlpha(mMenu, 0.6f + 0.4f * (1 - scale));

					// 设置中间View缩放效果
					ViewHelper.setTranslationX(mContent,
							mMenu.getMeasuredWidth() * (1 - scale));
					ViewHelper.setPivotX(mContent, 0);
					ViewHelper.setPivotY(mContent,
							mContent.getMeasuredHeight() / 2);
					mContent.invalidate();
					ViewHelper.setScaleX(mContent, rightScale);
					ViewHelper.setScaleY(mContent, rightScale);
				} else {// 展开右侧菜单
					// if (btnFlag == false) {
					// float leftScale = 1 - 0.3f * scale;
					// float rightScale = 0.8f + scale * 0.2f;
					// // 设置右侧菜单缩放效果
					// ViewHelper.setScaleX(mMenu, rightScale);
					// ViewHelper.setScaleY(mMenu, rightScale);
					// ViewHelper.setAlpha(mMenu, 0.6f + 0.4f * (1 - scale));
					// // 设置中间View缩放效果
					// ViewHelper.setTranslationX(mContent,
					// -mMenu.getMeasuredWidth() * slideOffset);
					// ViewHelper.setPivotX(mContent,
					// mContent.getMeasuredWidth());
					// ViewHelper.setPivotY(mContent,
					// mContent.getMeasuredHeight() / 2);
					// mContent.invalidate();
					// ViewHelper.setScaleX(mContent, rightScale);
					// ViewHelper.setScaleY(mContent, rightScale);
					// }
				}
			}

			// 菜单打开
			@Override
			public void onDrawerOpened(View drawerView) {
			}

			// 菜单关闭
			@Override
			public void onDrawerClosed(View drawerView) {
				// mDrawerLayout.setDrawerLockMode(
				// DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
			}
		});
	}

	public void onRefresh() {
		// Log.e("xxx", Thread.currentThread().getName());
		// UI Thread
		Toast.makeText(this, "refreshing", Toast.LENGTH_LONG).show();
		// mHandler.sendEmptyMessageDelayed(REFRESH_COMPLETE, 2000);

	}

	private void loadTopics() {
		if (socketBinder != null) {
			synchronized (topicContents) {
				topicContents.clear();
				topicContents.addAll(socketBinder.getTopics(btnFlag));
			}
			switch (btnFlag) {
			case Constant.SESSION:
			case Constant.DISCOVER:
				topicItemsList.setAdapter(topicItemsAdapter);
				topicItemsAdapter.notifyDataSetChanged();
				break;
			case Constant.TIMELINE:
			case Constant.TREND:
				topicItemsList.setAdapter(timelineAdapter);
				timelineAdapter.notifyDataSetChanged();
				break;
			}
			System.out.println("Load " + topicContents.size()
					+ " topics, type=" + btnFlag);
		}
	}

	class MainAdapter extends BaseAdapter {

		private LinkedList<TopicEntity> topicContents;

		public MainAdapter(LinkedList<TopicEntity> topicContents_) {
			this.topicContents = topicContents_;
		}

		@Override
		public int getCount() {
			return topicContents.size();
		}

		@Override
		public TopicEntity getItem(int position) {
			return topicContents.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("NewApi")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final TopicEntity entity = topicContents.get(position);
			ViewHolder viewHolder = null;

			if (convertView == null) {
				convertView = View.inflate(getApplicationContext(),
						R.layout.item_list_app, null);
				viewHolder = new ViewHolder();
				viewHolder.tv_name = (TextView) convertView
						.findViewById(R.id.tv_name);
				viewHolder.tv_date = (TextView) convertView
						.findViewById(R.id.date);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			TopicEntity temp = topicContents.get(position);
			viewHolder.tv_name.setText(temp.getPreview());

			viewHolder.tv_date.setText(getDateView(temp.getLatest().getDate()));
			viewHolder.tv_name.setTextColor(Color.rgb(0x77, 0x77, 0x77));
			viewHolder.tv_name.setTextSize(20);

			if (entity.isIgnore()) {
				convertView.setBackgroundColor(Color.rgb(0xCC, 0xCC, 0xCC));
			} else {
				convertView.setBackgroundResource(R.drawable.list_item_bg);
				convertView.getBackground().setAlpha(100);

			}
			return convertView;
		}

		class ViewHolder {
			TextView tv_name;
			TextView tv_date;
		}
	}

	class TimelineAdapter extends BaseAdapter {

		private LinkedList<TopicEntity> topicContents;

		public TimelineAdapter(LinkedList<TopicEntity> topicContents_) {
			this.topicContents = topicContents_;
		}

		@Override
		public int getCount() {
			return topicContents.size();
		}

		@Override
		public TopicEntity getItem(int position) {
			return topicContents.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("NewApi")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final TopicEntity entity = topicContents.get(position);
			ViewHolder viewHolder = null;
			if (convertView == null) {
				convertView = View.inflate(getApplicationContext(),
						R.layout.item_list_app_timeline, null);
				viewHolder = new ViewHolder();
				viewHolder.tv_name = (TextView) convertView
						.findViewById(R.id.tv_name);
				viewHolder.tv_date = (TextView) convertView
						.findViewById(R.id.date);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			TopicEntity temp = topicContents.get(position);
			viewHolder.tv_name.setText(temp.getPreview());

			// test: 1403931367000l
			viewHolder.tv_date.setText(getDateView(temp.getLatest().getDate()));
			viewHolder.tv_name.setTextColor(Color.rgb(0x77, 0x77, 0x77));
			viewHolder.tv_name.setTextSize(20);
			if (entity.isIgnore()) {
				convertView.setBackgroundColor(Color.rgb(0xCC, 0xCC, 0xCC));
			} else {
				convertView.setBackgroundResource(R.drawable.list_item_bg);
				convertView.getBackground().setAlpha(100);

			}
			return convertView;
		}

		class ViewHolder {
			TextView tv_name;
			TextView tv_date;
		}
	}

	private String getDateView(long date) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date sentDate = new Date(date);
		String sentStr = sdf.format(date);
		String sentDay = sentStr.substring(0, 10);
		String sentYear = sentStr.substring(6, 10);
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String curStr = sdf.format(curDate);
		String curDay = curStr.substring(0, 10);
		String curYear = curStr.substring(6, 10);
		if (curDay.equals(sentDay)) {
			return sentStr.substring(11, 16);
		} else if (sentYear.equals(curYear)) {
			return sentStr.substring(0, 5);
		} else {
			return sentYear;
		}
	}

	public void OpenRightMenu(View view) {
		mDrawerLayout.openDrawer(Gravity.RIGHT);// 展开侧边的菜单
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,
				Gravity.RIGHT);// 打开手势滑动
	}

	public void OpenLeftMenu(View view) {
		mDrawerLayout.openDrawer(Gravity.LEFT);// 展开侧边的菜单
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,
				Gravity.RIGHT);// 打开手势滑动
	}
}
