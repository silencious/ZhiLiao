package com.zhiliao.client.activity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.method.ScrollingMovementMethod;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhiliao.R;
import com.zhiliao.client.adapter.ChatMsgViewAdapter;
import com.zhiliao.client.entity.MessageEntity;
import com.zhiliao.client.entity.TopicEntity;
import com.zhiliao.client.service.SocketService;
import com.zhiliao.client.service.SocketService.SocketBinder;
import com.zhiliao.client.util.Constant;

public class ChatActivity extends Activity implements OnClickListener,
		OnGestureListener, SwipeRefreshLayout.OnRefreshListener {
	final static int FOLLOW = 0;
	final static int UNFOLLOW = 1;
	private Long topicId = null;
	private String myname;
	private String createmsg = null;
	private String forkmsg;
	private Long referId;
	private String toname;

	private LinkedList<LinkedList<MessageEntity>> loading = new LinkedList<LinkedList<MessageEntity>>();

	public SocketBinder socketBinder;
	private ChatHandler handler = new ChatHandler();

	private GestureDetector mGestureDetector;
	private Button mBtnSend, btnDelRefer, btnMenu, menuBtn1, menuBtn2,
			menuBtn3, menuBtn4, menuBtn5;
	private boolean forkable = true;
	private EditText mEditTextContent;
	private ExpandableListView mListView;
	private TextView tvToName;
	private RelativeLayout rlRefer;
	private ChatMsgViewAdapter mAdapter;

	private ProgressDialog pd;

	private LinkedList<MessageEntity> mDataArrays = new LinkedList<MessageEntity>();
	private LinkedList<LinkedList<MessageEntity>> referArrays = new LinkedList<LinkedList<MessageEntity>>();
	public static Context context;

	private float xDown;
	private float yDown;
	private float xMove;
	private float yMove;
	private SwipeRefreshLayout mSwipeLayout;
	private Bundle bundle;
	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			socketBinder = (SocketBinder) binder;
			socketBinder.setChatHandler(topicId, handler);
			if (topicId == null || topicId <= 0) {
				socketBinder.createTopic(createmsg);
			}
		}
	};

	public class ChatHandler extends Handler {

		public Long getTopicId() {
			return topicId;
		}

		public void handleMessage(android.os.Message msg) {
			// update ui according to msg
			switch (msg.what) {
			case Constant.LOAD_DATA:// updatedata asynchronously
				updateData();
				break;
			case Constant.PUSH:// receive chat massage
				System.out.println("Receiver push");
				if (msg != null) {
					MessageEntity m = (MessageEntity) msg.obj;
					if (mDataArrays.isEmpty()) {
						mDataArrays.add(m);
						referArrays.add(new LinkedList<MessageEntity>());
					} else {
						ListIterator<MessageEntity> it = mDataArrays
								.listIterator();
						ListIterator<LinkedList<MessageEntity>> itRefer = referArrays
								.listIterator();
						while (it.hasNext() && itRefer.hasNext()) {
							MessageEntity item = it.next();
							LinkedList<MessageEntity> referItem = itRefer
									.next();
							if (m.getId().equals(item.getId())) {
								return;
							}
							if (m.getId() < item.getId()) {
								it.previous();
								itRefer.previous();
								it.add(m);
								itRefer.add(new LinkedList<MessageEntity>());
								break;
							}
						}

						if (!it.hasNext()) {
							it.add(m);
							itRefer.add(new LinkedList<MessageEntity>());
							mListView
									.setSelection(mAdapter.getGroupCount() - 1);
						}
					}
					mAdapter.notifyDataSetChanged();
					// mListView.setSelection(mAdapter.getCount() - 1);
				}
				mSwipeLayout.setRefreshing(false);
				break;
			case Constant.SEND_SUCCESS:// send message feedback:success
			case Constant.FORWARD_SUCCESS:
				if (pd != null) {
					pd.dismiss();
				}
				mAdapter.notifyDataSetChanged();
				break;
			case Constant.SEND_FAIL:
			case Constant.FORWARD_FAIL: { // send message feedback:fail
				if (pd != null) {
					pd.dismiss();
				}
				Toast.makeText(context, Constant.MSG + Constant.SEND_FAILED,
						Toast.LENGTH_SHORT).show();
				break;
			}
			case Constant.CREATE_SUCCESS:// create topic feedback:success
				System.out.println("Create topic success");
				topicId = (Long) msg.obj;
				// pd.dismiss(); // FIXME why is this null?
				break;
			case Constant.CREATE_FAIL:// create topic feedback:fail
				Toast.makeText(
						context,
						Constant.CREATE_TOPIC_FAILED + Constant.COMMA
								+ Constant.CHECK_NETWORK, Toast.LENGTH_SHORT)
						.show();
				pd.dismiss();
				finish();
				break;
			case Constant.FORK_SUCCESS: {// fork feedback:success
				pd.dismiss();
				Bundle bundle = msg.getData();
				Long branch = bundle.getLong("branch");

				if (branch == null) {
					return;
				}
				topicId = branch;

				Toast.makeText(context, Constant.CURRENT_TOPIC + forkmsg,
						Toast.LENGTH_SHORT).show();
				// NOTE:
				// (Button)chatFork在setClickable(false)后用户能否辨别，
				// 如何让用户了解规则：在话题分支一次只能创建一个，不能同时创建
				// 页面能否美化
				forking(false);
				break;
			}
			case Constant.FORK_FAIL: {// fork feedback:fail
				// NOTE：
				// 标记话题分支创建成功的方法能否改进？原方法为：在msg上强行加上string。
				pd.dismiss();
				Long mark = (Long) msg.obj;
				if (mark == null) {
					return;
				}
				forking(false);
				Toast.makeText(context, Constant.CREATE_TOPIC_BRANCH_FAILED,
						Toast.LENGTH_SHORT).show();
				break;
			}
			case Constant.REACH_END:
				mSwipeLayout.setRefreshing(false);
				break;
			case Constant.GET_REPLIED: {
				// TODO get the referred message, update ui
				ArrayList<MessageEntity> msgList = new ArrayList<MessageEntity>();
				MessageEntity msgRefer = socketBinder.getMsg((Long) msg.obj);
				msgList.add(msgRefer);
				MessageEntity msgTmp = msgRefer;
				boolean flag = true;
				while (msgTmp.getReplied() != null) {
					msgTmp = socketBinder.getMsg(msgTmp.getReplied());
					if (msgTmp == null) {// 加载未完成
						flag = false;
						break;
					}
				}
				for (int i = 0; i < loading.size(); i++) {
					if (loading.get(i).get(0).getReplied() == msgRefer.getId()) {
						loading.get(i).addAll(0, msgList);
						mAdapter.notifyDataSetChanged();
						if (flag) {
							loading.remove(i);
						}
					}
				}
				break;
			}
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("Create ChatActivity");
		bundle = this.getIntent().getExtras();
		if (bundle != null) {
			topicId = bundle.getLong("topicId");
			createmsg = bundle.getString("msg");
		}
		bindService(new Intent(ChatActivity.this, SocketService.class), conn,
				BIND_AUTO_CREATE);
		init();
		if (topicId == null || bundle == null) {
			System.out.println("Topic id is null, create");
			pd = ProgressDialog.show(ChatActivity.this, Constant.WAIT,
					Constant.LOADING, true, false);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (socketBinder != null && topicId != null) {
			if (socketBinder.getTopicById(topicId).isEmpty()) {
				socketBinder.loadMore(topicId);
			}
		}
		// Should do something to refresh
	};

	@Override
	public void onDestroy() {
		unbindService(conn);
		super.onDestroy();
	}

	public void init() {
		// init view
		setContentView(R.layout.activity_chat);
		context = getApplicationContext();
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		mListView = (ExpandableListView) findViewById(R.id.chat_listview);

		Button retButton = (Button) findViewById(R.id.chat_ret);
		retButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		btnMenu = (Button) findViewById(R.id.chat_btn_menu);
		btnMenu.setOnClickListener(this);

		mBtnSend = (Button) findViewById(R.id.chat_sendbutton);
		mBtnSend.setOnClickListener(this);

		btnDelRefer = (Button) findViewById(R.id.chat_receiver_del);
		btnDelRefer.setOnClickListener(this);

		tvToName = (TextView) findViewById(R.id.chat_tv_receiver);

		rlRefer = (RelativeLayout) findViewById(R.id.chat_refer);

		mGestureDetector = new GestureDetector(this, this);
		// mGestureDetector = new GestureDetector((OnGestureListener) this);

		mEditTextContent = (EditText) findViewById(R.id.chat_sendmessage);
		mEditTextContent.setMovementMethod(ScrollingMovementMethod
				.getInstance());

		// hide the keyboard

		myname = getSharedPreferences("auth", MODE_PRIVATE).getString("name",
				null);
		updateToName(null, null);
		mAdapter = new ChatMsgViewAdapter(ChatActivity.this, mDataArrays,
				referArrays, myname);
		mListView.setAdapter(mAdapter);

		mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.chat_layout);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorScheme(R.color.refresh_blue, R.color.refresh_blue,
				R.color.white, R.color.white);
		mListView.setOnScrollListener(new SwpipeListViewOnScrollListener(
				mSwipeLayout));
		mListView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				// TODO 获得refer的list
				MessageEntity msg = mDataArrays.get(groupPosition);
				if (msg.getReplied() != null
						&& referArrays.get(groupPosition).size() == 0) {
					while (msg.getReplied() != null) {
						msg = socketBinder.getMsg(msg.getReplied());
						if (msg == null) {
							// 未加载完的处理， 记录msgGroup的Id
							loading.add(referArrays.get(groupPosition));
							break;
						}
						referArrays.get(groupPosition).add(0, msg);
					}
					mAdapter.notifyDataSetChanged();
				}
				return false;
			}
		});
	}

	public void onRefresh() {
		socketBinder.loadMore(topicId);
	}

	public void updateData() {
		if (socketBinder != null) {
			TopicEntity topic = socketBinder.getTopicById(topicId);
			if (topic != null) {
				mDataArrays.clear();
				mDataArrays.addAll(topic.getMsgColl());
				referArrays.clear();
				for (int i = 0; i < mDataArrays.size(); i++) {
					referArrays.add(new LinkedList<MessageEntity>());
				}
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	public void updateToName(String name, Long Id) {
		toname = name;
		referId = Id;
		if (toname == null) {
			// if receiver is the user him/herself, hide it
			rlRefer.setVisibility(View.GONE);
		} else {
			// show receiver
			rlRefer.setVisibility(View.VISIBLE);
			tvToName.setText(toname);

			// show the keyboard
			mEditTextContent.setFocusable(true);
			mEditTextContent.setFocusableInTouchMode(true);
			mEditTextContent.requestFocus();
			InputMethodManager inputManager = (InputMethodManager) mEditTextContent
					.getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.showSoftInput(mEditTextContent, 0);
		}
	}

	@Override
	public void onClick(View v) {
		if (topicId == null) {
			Toast.makeText(context,
					Constant.WAIT + Constant.COMMA + Constant.LOADING,
					Toast.LENGTH_SHORT).show();
			return;
		}
		switch (v.getId()) {
		case R.id.chat_sendbutton:
			send();
			break;
		case R.id.chat_receiver_del:
			updateToName(null, null);
			break;
		case R.id.chat_btn_menu:
			// 显示下拉菜单
			View contentView = LayoutInflater.from(ChatActivity.this).inflate(
					R.layout.chat_popup_window, null);
			menuBtn1 = (Button) contentView.findViewById(R.id.chat_pop_follow);
			menuBtn1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					topicDialog(FOLLOW);
				}

			});
			menuBtn2 = (Button) contentView.findViewById(R.id.chat_pop_fork);
			menuBtn2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (forkable) {
						forkDialog();
					} else {
						Toast.makeText(context, Constant.FORK_WAIT,
								Toast.LENGTH_SHORT).show();
					}
				}

			});
			menuBtn3 = (Button) contentView.findViewById(R.id.chat_pop_home);
			menuBtn3.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					finish();
				}

			});
			menuBtn4 = (Button) contentView
					.findViewById(R.id.chat_pop_unfollow);
			menuBtn4.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					topicDialog(UNFOLLOW);
				}

			});

			menuBtn5 = (Button) contentView.findViewById(R.id.chat_pop_about);
			menuBtn5.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					aboutTopicDialog();
				}
			});

			if (forkable) {
				menuBtn2.setText(Constant.FORK_BRANCH);
			} else {
				menuBtn2.setText(Constant.CREATING);
			}

			// if(socketBinder.getFollowingTopics(topicId).size() == 0){
			if (true) {// TODO 判断是否已follow话题
				menuBtn4.setVisibility(View.GONE);
			} else {
				menuBtn4.setVisibility(View.VISIBLE);
			}
			final PopupWindow popupWindow = new PopupWindow(contentView,
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
			popupWindow.setTouchable(true);
			popupWindow.setTouchInterceptor(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return false;
				}
			});
			popupWindow.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.logo_new_small));
			popupWindow.showAsDropDown(btnMenu, 0, 0);
			break;
		}
	}

	private void aboutTopicDialog() {
		// TODO 获取topics
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dlg = builder.create();
		LayoutInflater inflater = (LayoutInflater) getApplicationContext()
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		RelativeLayout view = (RelativeLayout) inflater.inflate(
				R.layout.dialog_topic_about, null);
		builder.setView(view);
		dlg.show();

		Window window = dlg.getWindow();
		window.setContentView(R.layout.dialog_topic_about);

		LinearLayout topicAbout = (LinearLayout) window
				.findViewById(R.id.topic_about);
		TextView founder = (TextView) window
				.findViewById(R.id.topic_about_creator);
		String name = "创建人用户名";// socketBinder.getFounder(topicId);
		founder.setText(name);
		ArrayList<String> tags = new ArrayList();// socketBinder.getTag(topicId);
		if (tags.size() != 0) {
			((TextView) window.findViewById(R.id.topic_about_null))
					.setVisibility(View.GONE);
		}
		for (int i = 0; i < tags.size(); i++) {
			TextView tag = new TextView(this);
			tag.setText(tags.get(i));
			tag.setTextSize(21);
			tag.setTextColor(Color.parseColor("#0e5d47"));
			topicAbout.addView(tag);
		}

		Button btnCancel = (Button) window
				.findViewById(R.id.topic_about_cancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dlg.cancel();
			}
		});
	}

	public void singleTopicDialog(final MessageEntity msgEntity) {
		// TODO 获取topics
		final LinkedList<TopicEntity> topics = new LinkedList<TopicEntity>();
		topics.addAll(socketBinder.getTopics(Constant.SESSION));

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dlg = builder.create();
		LayoutInflater inflater = (LayoutInflater) getApplicationContext()
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		RelativeLayout view = (RelativeLayout) inflater.inflate(
				R.layout.dialog_radiogroup, null);
		builder.setView(view);
		dlg.show();

		Window window = dlg.getWindow();
		window.setContentView(R.layout.dialog_radiogroup);

		final RadioGroup topicsGroup = (RadioGroup) window
				.findViewById(R.id.radioGroup_topic);
		final CheckBox ifJump = (CheckBox) window.findViewById(R.id.cb_jump);

		for (int i = 0; i < topics.size(); i++) {
			RadioButton topic = new RadioButton(this);
			topic.setId(i);
			topic.setTextSize(20);
			topic.setText(topics.get(i).getPreview());
			topicsGroup.addView(topic);
		}

		Button btnOk = (Button) window.findViewById(R.id.radioGroup_ok);
		btnOk.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				forward(topics.get(topicsGroup.getCheckedRadioButtonId())
						.getTopicId(), ifJump.isChecked(), msgEntity);
				dlg.dismiss();
			}
		});
		Button btnCancel = (Button) window.findViewById(R.id.radioGroup_cancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dlg.cancel();
			}
		});
	}

	private void topicDialog(final int type) {
		LayoutInflater inflater = (LayoutInflater) getApplicationContext()
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_topic_checkboxgroup, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dlg = builder.create();
		builder.setView(view);
		dlg.show();

		Window window = dlg.getWindow();
		window.setContentView(R.layout.dialog_topic_checkboxgroup);

		final LinearLayout topicGroup = (LinearLayout) window
				.findViewById(R.id.topic_checkboxGroup);

		Button btnCancel = (Button) window
				.findViewById(R.id.checkboxGroup_cancel);
		Button btnOk = (Button) window.findViewById(R.id.checkboxGroup_ok);
		TextView tvTitle = (TextView) window
				.findViewById(R.id.checkboxGroup_title);
		final ArrayList<Long> multiSelectedId = new ArrayList<Long>();

		// 获取话题List
		final LinkedList<TopicEntity> topics = new LinkedList<TopicEntity>();
		topics.addAll(socketBinder.getTopics(Constant.SESSION));// TODO delete
																// it
		switch (type) {
		case FOLLOW:
			// topics.addAll(socketBinder.getToFollowTopics(topicId));
			tvTitle.setText(Constant.FOLLOW_TOPIC);
			break;
		case UNFOLLOW:
			// topics.addAll(socketBinder.getFollowingTopics(topicId));
			tvTitle.setText(Constant.UNFOLLOW_TOPIC);
			break;
		}
		// 生成checkbox列表
		final int topicNum = topics.size();
		for (int i = 0; i < topicNum; i++) {
			CheckBox topic = (CheckBox) inflater.inflate(R.layout.checkbox,
					null).findViewById(R.id.checkbox_item);
			topic.setId(i);
			topic.setTextSize(20);
			topic.setText(topics.get(i).getPreview());
			topicGroup.addView(topic);
		}

		btnOk.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				for (int i = 0; i < topicNum; i++) {
					if (((CheckBox) topicGroup.getChildAt(i)).isChecked()) {
						multiSelectedId.add(topics.get(i).getTopicId());
					}
				}
				switch (type) {
				case FOLLOW:
					follow(multiSelectedId);
					break;
				case UNFOLLOW:
					unfollow(multiSelectedId);
					break;
				}
				dlg.dismiss();
			}
		});

		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dlg.cancel();
			}
		});
	}

	private void forward(Long targetTopicId, boolean jumpFlag,
			MessageEntity msgEntity) {
		if (targetTopicId != null) {
			// TODO forward can add more information(like weibo or renren)
			socketBinder.forward(msgEntity.getMsg(), targetTopicId,
					msgEntity.getId(), topicId);
			pd = ProgressDialog.show(ChatActivity.this, Constant.WAIT,
					Constant.SENDING, true, false);
			// 跳转到topic targetTopicId
			if (jumpFlag) {
				// 更换topicId,更新数据
				setTopicId(targetTopicId);
				updateData();
				updateToName(null, null);
			}
		}
	}

	private void follow(ArrayList<Long> multiSelectedId) {
		for (int i = 0; i < multiSelectedId.size(); i++) {
			socketBinder.follow(topicId, multiSelectedId.get(i));
		}
	}

	private void unfollow(ArrayList<Long> multiSelectedId) {
		for (int i = 0; i < multiSelectedId.size(); i++) {
			// TODO: socketBinder.unfollow(topicId,multiSelectedId.get(i));
		}
	}

	private void forkDialog() {
		final AlertDialog dlg = new AlertDialog.Builder(this).create();
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
				String text = edit_msg.getText().toString().trim();
				if (text.length() == 0) {
					Toast.makeText(context, Constant.EMPTY_ERROR,
							Toast.LENGTH_SHORT).show();
				} else {
					fork(text);
					dlg.dismiss();
				}
			}
		});
		Button cancel = (Button) window.findViewById(R.id.btn_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dlg.cancel();
			}
		});

	}

	private void send() {
		String text = mEditTextContent.getText().toString().trim();
		if (text.length() > 0) {
			MessageEntity message = new MessageEntity();
			message.setTopicId(topicId);
			message.setFromName(myname);
			message.setToName(toname);
			message.setReplied(referId);
			message.setMsg(text);
			socketBinder.send(message);
			pd = ProgressDialog.show(ChatActivity.this, Constant.WAIT,
					Constant.SENDING, true, false);
			mEditTextContent.setText("");
			updateToName(null, null);
		} else {
			Toast.makeText(context, Constant.EMPTY_ERROR, Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void send(String msg) {
		String text = msg;
		if (text.length() > 0) {
			MessageEntity message = new MessageEntity();
			message.setTopicId(topicId);
			message.setFromName(myname);
			message.setToName(toname);
			message.setReplied(referId);
			message.setMsg(text);
			socketBinder.send(message);
			pd = ProgressDialog.show(ChatActivity.this, Constant.WAIT,
					Constant.SENDING, true, false);
			mEditTextContent.setText("");
		} else {
			Toast.makeText(context, Constant.EMPTY_ERROR, Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void fork(String text) {
		if (text.length() > 0) {
			forking(true);
			forkmsg = text;
			socketBinder.fork(forkmsg, topicId);
			pd = ProgressDialog.show(ChatActivity.this, Constant.WAIT,
					Constant.CREATING_TOPIC_BRANCH + Constant.ELLIPSIS, true,
					false);
		}
	}

	private void forking(boolean flag) {
		forkable = !flag;
	}

	// left slide support
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

	// left slide:switch to MainActivity
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e2.getX() - e1.getX() > 100
				&& Math.abs(e2.getY() - e1.getY()) < 100
				&& Math.abs(velocityX) > 100) {
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
		return false;
	}

	public ChatHandler getHandler() {
		return this.handler;
	}

	public Long getTopicId() {
		return topicId;
	}

	public void setTopicId(Long topicId) {
		this.topicId = topicId;
	}

	public void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	}

	// fix conflict between refresh and scroll
	public static class SwpipeListViewOnScrollListener implements
			AbsListView.OnScrollListener {

		private SwipeRefreshLayout mSwipeView;
		private AbsListView.OnScrollListener mOnScrollListener;

		public SwpipeListViewOnScrollListener(SwipeRefreshLayout swipeView) {
			mSwipeView = swipeView;
		}

		public SwpipeListViewOnScrollListener(SwipeRefreshLayout swipeView,
				OnScrollListener onScrollListener) {
			mSwipeView = swipeView;
			mOnScrollListener = onScrollListener;
		}

		@Override
		public void onScrollStateChanged(AbsListView absListView, int i) {
		}

		@Override
		public void onScroll(AbsListView absListView, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			View firstView = absListView.getChildAt(firstVisibleItem);

			// 当firstVisibleItem是第0位。如果firstView==null说明列表为空，需要刷新;或者top==0说明已经到达列表顶部,
			// 也需要刷新
			// if (firstVisibleItem == 0 && (firstView == null ||
			// firstView.getTop() == 0)) {
			// mSwipeView.setEnabled(true);
			// Log.v("ontouch", "top");
			// } else {
			// mSwipeView.setEnabled(false);
			// Log.v("ontouch", "down");
			// }
			if (null != mOnScrollListener) {
				mOnScrollListener.onScroll(absListView, firstVisibleItem,
						visibleItemCount, totalItemCount);
			}
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

}
