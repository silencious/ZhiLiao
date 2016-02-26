package com.zhiliao.client.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.websocket.CloseReason;

import org.glassfish.tyrus.client.auth.AuthenticationException;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.zhiliao.client.activity.AccountActivity.AccountHandler;
import com.zhiliao.client.activity.ChatActivity.ChatHandler;
import com.zhiliao.client.activity.FollowersActivity.FollowersHandler;
import com.zhiliao.client.activity.FollowingsActivity.FollowingsHandler;
import com.zhiliao.client.activity.MainActivity.MainHandler;
import com.zhiliao.client.activity.MyInfoActivity.MyInfoHandler;
import com.zhiliao.client.activity.RegisterActivity.RegisterHandler;
import com.zhiliao.client.activity.UserActivity.UserHandler;
import com.zhiliao.client.entity.MessageEntity;
import com.zhiliao.client.entity.TopicEntity;
import com.zhiliao.client.entity.UserInfo;
import com.zhiliao.client.entity.UserInfo.PublicInfo;
import com.zhiliao.client.util.Constant;
import com.zhiliao.client.websocket.BasicAuthConfigurator;
import com.zhiliao.client.websocket.MessageApi;
import com.zhiliao.client.websocket.MessageApi.OnCloseHandler;
import com.zhiliao.client.websocket.MessageApi.OnErrorHandler;
import com.zhiliao.client.websocket.MessageApi.OnMessageHandler;
import com.zhiliao.client.websocket.MessageApi.OnOpenHandler;
import com.zhiliao.client.websocket.rest.RestClient;
import com.zhiliao.client.websocket.rest.RestClient.Callback.Delete;
import com.zhiliao.client.websocket.rest.RestClient.Callback.Get;
import com.zhiliao.client.websocket.rest.RestClient.Callback.IteratorInit;
import com.zhiliao.client.websocket.rest.RestClient.Callback.IteratorNext;
import com.zhiliao.client.websocket.rest.RestClient.Callback.List;
import com.zhiliao.client.websocket.rest.RestClient.Callback.Post;
import com.zhiliao.message.client.IteratorInitRequest.ByBranchIteratorSpecification;
import com.zhiliao.message.client.IteratorInitRequest.ByFollowIteratorSpecification;
import com.zhiliao.message.client.IteratorInitRequest.ByTagIteratorSpecification;
import com.zhiliao.message.client.Subscribe;
import com.zhiliao.message.server.Push;
import com.zhiliao.server.exception.ErrorCode;
import com.zhiliao.server.model.rest.BranchModel;
import com.zhiliao.server.model.rest.CommitModel;
import com.zhiliao.server.model.rest.FollowModel;
import com.zhiliao.server.model.rest.ForwardModel;
import com.zhiliao.server.model.rest.Resource;
import com.zhiliao.server.model.rest.UserModel;

public class SocketService extends Service {
	// logged user
	private volatile UserInfo myInfo = null;
	// iterator service
	private volatile IteratorService iteratorService = new IteratorService();
	// message and topic service
	private volatile MessageService messageService = new MessageService();
	// use Users class to save users' information
	private volatile UserService userService = new UserService();
	// followService class is used to manage follow relationships
	private volatile FollowService followService = new FollowService();
	// map from tag to usernames
	private HashMap<String, TreeSet<String>> tags = new HashMap<String, TreeSet<String>>();

	// SocketService's own binder
	private SocketBinder socketBinder = new SocketBinder();
	// handler of MainActivity
	private MainHandler mainHandler = null;
	// handler of ChatActivity
	private ChatHandler chatHandler = null;
	// handler of UserActivity
	private UserHandler userHandler = null;
	// handler of FollowersActivity
	private FollowersHandler followersHandler = null;
	// handler of FollowingsActivity
	private FollowingsHandler followingsHandler = null;
	// handler of AddFriendActivity
	// private AddFriendHandler addFriendHandler = null;
	// handler of RegisterActivity
	private RegisterHandler registerHandler = null;
	// account handler
	private AccountHandler accountHandler = null;
	// user info handler
	private MyInfoHandler myInfoHandler = null;

	// indicate the status of websocket connection with server
	private volatile boolean connected = false;
	// stored credentials
	private BasicAuthConfigurator auth = null;
	// use api to send message to server
	private MessageApi messageApi = new MessageApi();
	// rest client is used to bind methods to response
	private RestClient restClient = new RestClient(messageApi);

	public void reconnect() {
		connected = false;
		iteratorService.disconnect();
		new Thread() {
			public void run() {
				int i = 1 << 8;
				int ret = connect();
				while (ret == Constant.ERROR) {
					try {
						wait(i);
						i = Math.min(i << 1, 1 << 20);
						ret = connect();
					} catch (InterruptedException e) {
						System.out.println("Reconnect Error");
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return socketBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		messageApi.getOnOpenHandlers().add(new OnOpenHandler() {
			@Override
			public void handle(MessageApi api) {
				System.out.println("Connected");
				connected = true;
			}
		});
		messageApi.getOnCloseHandlers().add(new OnCloseHandler() {
			@Override
			public void handle(MessageApi api, CloseReason reason) {
				System.out.println("Closed " + reason);
				reconnect();
			}
		});
		messageApi.getOnErrorHandlers().add(new OnErrorHandler() {
			@Override
			public void handle(MessageApi api, Throwable thr) {
				System.out.println("On Error");
				thr.printStackTrace();
				reconnect();
			}
		});
		messageApi.getOnMessageHandlers().put(Push.class,
				new OnMessageHandler<Push>() {
					public void handle(MessageApi api, final Push push) {
						pushMessage((CommitModel) push.getResource());
					}
				});
	}

	private void pushMessage(final CommitModel message) {
		new Thread() {
			public void run() {
				Long id = message.getId();
				Long topicId = message.getBranch();
				Long author = message.getAuthor();
				if (topicId == null || id == null || author == null) {
					System.out.println("Bad push");
					return;
				}
				MessageEntity entity = null;
				Long replied = message.getReplied();
				synchronized (messageService) {
					entity = messageService.getMessage(id);
					if (entity != null) {
						System.out.println("Push duplicate message");
						return;
					}
					entity = new MessageEntity();
					entity.setId(id);
					entity.setTopicId(topicId);
					entity.setAuthor(author);
					entity.setReplied(replied);
					entity.setDate(message.getDate());
					entity.setMsg(message.getMsg());
					messageService.addMessage(entity);
				}
				entity.setFromName(getUsername(author));
				if (replied != null) {
					synchronized (messageService) {
						MessageEntity rMsg = socketBinder.getMsg(replied);
						while (rMsg == null) {
							try {
								System.out
										.println("Waiting for replied message");
								messageService.wait();
								rMsg = messageService.getMessage(replied);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						String toName = rMsg.getFromName();
						entity.setToName(toName);
					}
				}
				if (chatHandler != null) {
					chatHandler.obtainMessage(Constant.LOAD_DATA, entity)
							.sendToTarget();
				}
				System.out.println("Tell MainActivity to refresh");
				mainHandler.obtainMessage(Constant.LOAD_DATA).sendToTarget();
			}
		}.start();
	}

	private void getCommit(Long id) {
		if (id == null) {
			return;
		}
		CommitModel commit = new CommitModel();
		commit.setId(id);
		restClient.get(commit, new Get<CommitModel>() {
			@Override
			public void onError(ErrorCode error) {
				System.out.println("Get commit error");
			}

			@Override
			public void onResponse(final CommitModel res) {
				System.out.println("Get commit success");
				new Thread() {
					public void run() {
						Long id = res.getId();
						synchronized (messageService) {
							if (messageService.getMessage(id) == null) {
								Long author = res.getAuthor();
								Long replied = res.getReplied();
								MessageEntity msg = new MessageEntity();
								msg.setId(id);
								msg.setTopicId(res.getBranch());
								msg.setReplied(replied);
								msg.setAuthor(author);
								msg.setFromName(getUsername(author));
								MessageEntity rMsg = messageService
										.getMessage(replied);
								if (rMsg != null) {
									msg.setToName(rMsg.getFromName());
								}
								msg.setDate(res.getDate());
								msg.setMsg(res.getMsg());
								messageService.addMessage(msg);
								System.out.println("Get refered message");
								messageService.notifyAll();
							}
						}
						if (chatHandler != null) {
							chatHandler.obtainMessage(Constant.GET_REPLIED, id)
									.sendToTarget();
						}
					}
				}.start();
			}
		});
	}

	private void getTopicFromRemote(final TopicEntity topic) {
		BranchModel branch = new BranchModel();
		branch.setId(topic.getTopicId());
		restClient.get(branch, new Get<BranchModel>() {
			@Override
			public void onError(ErrorCode error) {
				System.out.println("Error with get topic");
			}

			@Override
			public void onResponse(BranchModel res) {
				topic.setFounder(res.getFounder());
				topic.setTags(res.getTags());
			}
		});
	}

	// get a user's name by id. NOTE: it may stall here
	private String getUsername(Long id) {
		if (id == null) {
			return null;
		}
		synchronized (userService) {
			if (userService.get(id) == null) {
				UserModel user = new UserModel();
				user.setId(id);
				try {
					System.out.println("Get user info:" + id);
					restClient.get(user, new Get<UserModel>() {
						@Override
						public void onError(ErrorCode error) {
							System.out.println("Get user error");
						}

						@Override
						public void onResponse(UserModel user) {
							synchronized (userService) {
								userService.add(user);
								userService.notifyAll();
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
				while (userService.get(id) == null) {
					try {
						userService.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return userService.get(id).getUsername();
	}

	private void init() {
		System.out.println("Init, get user info");
		restClient.get(new UserModel(), new Get<UserModel>() {
			@Override
			public void onError(ErrorCode error) {
				// TODO get user error not implemented
			}

			@Override
			public synchronized void onResponse(UserModel user) {
				if (myInfo != null) {
					return;
				}
				myInfo = new UserInfo(user);
				System.out.println("Got my user info");
				BranchModel branch = new BranchModel();
				branch.setFounder(myInfo.getPrivateInfo().getId());
				if (myInfo.getPrivateInfo().getFollowerBranch() == null) {
					System.out.println("Create my FollowerBranch");
					restClient.post(branch, new Post<BranchModel>() {
						@Override
						public void onError(ErrorCode error) {
							System.out.println("Create followerBranch error");
						}

						@Override
						public void onResponse(long id) {
							System.out.println("Create followerBranch success");
							synchronized (myInfo) {
								myInfo.getPrivateInfo().setFollowerBranch(id);
								if (myInfo.getPublicInfo().getFollowedBranch() != null) {
									socketBinder.follow(id, myInfo
											.getPublicInfo()
											.getFollowedBranch());
								}
								socketBinder.setMyInfo();
								TopicEntity topic = new TopicEntity();
								topic.setTopicId(id);
								messageService.setFollowerTopic(topic);
								subscribe(id);
							}
						}
					});
				} else {
					TopicEntity topic = new TopicEntity();
					topic.setTopicId(myInfo.getPrivateInfo()
							.getFollowerBranch());
					messageService.setFollowerTopic(topic);
					getTopicFromRemote(topic);
				}
				if (myInfo.getPublicInfo().getFollowedBranch() == null) {
					System.out.println("Create my FollowedBranch");
					restClient.post(branch, new Post<BranchModel>() {
						@Override
						public void onError(ErrorCode error) {
							System.out.println("Create followedBranch error");
						}

						@Override
						public void onResponse(long id) {
							System.out.println("Create followedBranch success");
							synchronized (myInfo) {
								myInfo.getPublicInfo().setFollowedBranch(id);
								if (myInfo.getPrivateInfo().getFollowerBranch() != null) {
									socketBinder.follow(myInfo.getPrivateInfo()
											.getFollowerBranch(), id);
								}
								socketBinder.setMyInfo();
								TopicEntity topic = new TopicEntity();
								topic.setTopicId(id);
								messageService.setFollowedTopic(topic);
								subscribe(id);
							}
						}
					});
				} else {
					TopicEntity topic = new TopicEntity();
					topic.setTopicId(myInfo.getPublicInfo().getFollowedBranch());
					messageService.setFollowedTopic(topic);
					getTopicFromRemote(topic);
				}
				userService.add(user);
				java.util.List<Long> sessions = myInfo.getPrivateInfo()
						.getSessions();
				if (sessions != null) {
					synchronized (messageService) {
						System.out.println("User has " + sessions.size()
								+ " sessions");
						for (Long topicId : sessions) {
							if (topicId.equals(myInfo.getPublicInfo()
									.getFollowedBranch())
									|| topicId.equals(myInfo.getPrivateInfo()
											.getFollowerBranch())) {
								continue;
							}
							TopicEntity topic = messageService
									.getTopic(topicId);
							if (topic == null) {
								topic = new TopicEntity();
								topic.setTopicId(topicId);
								getTopicFromRemote(topic);
							}
							messageService.addTopic(topic, Constant.SESSION);
							subscribe(topicId);
							socketBinder.loadMore(topicId);
						}
					}
				}
				getDiscover();
			}
		});
	}

	private void getDiscover() {
		restClient.list(new BranchModel(), new List<BranchModel>() {
			@Override
			public void onError(ErrorCode error) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onResponse(java.util.List<BranchModel> list) {
				System.out.println("Get discover");
				LinkedList<TopicEntity> discover = messageService
						.getTopics(Constant.DISCOVER);
				discover.clear();
				for (Resource branch : list) {
					if (!(branch instanceof BranchModel)) {
						continue;
					}
					Long id = branch.getId();
					TopicEntity topic = messageService.getTopic(id);
					if (topic == null) {
						topic = new TopicEntity();
						topic.setTopicId(id);
						topic.setFounder(((BranchModel) branch).getFounder());
						topic.setTags(((BranchModel) branch).getTags());
						discover.add(topic);
						socketBinder.loadMore(id);
					}
					mainHandler.obtainMessage(Constant.LOAD_DATA)
							.sendToTarget();
				}
			}
		});
	}

	// connect with server
	private int connect() {
		SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
		String name = sp.getString("name", null);
		String pwd = sp.getString("pwd", null);
		if (!connected) {
			if (pwd != null) {
				auth = new BasicAuthConfigurator(name, pwd);
			}
			try {
				messageApi.connect(auth);
				for (TopicEntity topic : messageService
						.getTopics(Constant.SESSION)) {
					subscribe(topic.getTopicId());
				}
				if (myInfo != null) {
					subscribe(myInfo.getPrivateInfo().getFollowerBranch());
					subscribe(myInfo.getPublicInfo().getFollowedBranch());
				}
			} catch (Exception e) {
				System.out.println("Connect Error");
				e.printStackTrace();
				Throwable e0 = e.getCause(); // get the root cause
				if (e0 != null) {
					while (e0.getCause() != null) {
						e0 = e0.getCause();
					}
					if (e0 instanceof AuthenticationException) {
						System.out.println("Authentication Error");
						// if the exception is caused by wrong username or
						// password,
						SharedPreferences.Editor editor = sp.edit();
						editor.putString("pwd", null);
						editor.commit();
						return Constant.FAIL;
					}
				}
				System.out.println("Other error");
				// other problems, mostly due to network
				return Constant.ERROR;
			}
		}
		if (pwd == null) {
			return Constant.GUEST;
		} else {
			return Constant.REGISTERED;
		}
	}

	// subscribe a certain topic
	private void subscribe(Long topicId) {
		if (topicId != null && topicId > 0) {
			Subscribe sub = new Subscribe();
			sub.setBranch(topicId);
			while (true) {
				try {
					messageApi.send(sub);
					break;
				} catch (Exception e) {
					e.printStackTrace();
					reconnect();
				}
			}
		}
	}

	// Binded with various activities, handle user requests
	public class SocketBinder extends Binder {
		public void setMainHandler(MainHandler handler) {
			mainHandler = handler;
		}

		public void setChatHandler(Long topicId, ChatHandler handler) {
			chatHandler = handler;
			if (topicId != null && topicId > 0) {
				synchronized (iteratorService) {
					TopicEntity topic = messageService.getTopic(topicId);
					if (topic == null) {
						topic = new TopicEntity();
						topic.setTopicId(topicId);
						getTopicFromRemote(topic);
					}
					topic.setIgnore(false);
					if (topic.isEmpty()) {
						System.out.println("Open an empty topic");
						iteratorService.reset(topicId);
						loadMore(topicId);
					}
				}
				chatHandler.obtainMessage(Constant.LOAD_DATA).sendToTarget();
			}
		}

		public void setUserHandler(UserHandler handler) {
			userHandler = handler;
		}

		public void setFollowersHandler(FollowersHandler handler) {
			followersHandler = handler; // handler of FollowersActivity
		}

		public void setFollowingsHandler(FollowingsHandler handler) {
			followingsHandler = handler; // handler of FollowingActivity
		}

		// public void setAddFriendHandler(AddFriendHandler handler) {
		// addFriendHandler = handler; // handler of AddFriendActivity
		// }

		public void setRegisterHandler(RegisterHandler handler) {
			registerHandler = handler;
		}

		public void setAccountHandler(AccountHandler handler) {
			accountHandler = handler;
		}

		public void setMyInfoHandler(MyInfoHandler handler) {
			myInfoHandler = handler;
		}

		public void register(final UserModel user) {
			new Thread() {
				public void run() {
					int ret = connect();
					if (ret == Constant.GUEST) {
						try {
							restClient.post(user, new Post<UserModel>() {
								@Override
								public void onError(ErrorCode error) {
									if (registerHandler != null) {
										registerHandler.obtainMessage(
												Constant.FAIL).sendToTarget();
									}
								}

								@Override
								public void onResponse(long id) {
									if (registerHandler != null) {
										registerHandler.obtainMessage(
												Constant.SUCCESS)
												.sendToTarget();
									}
								}
							});
						} catch (Exception e) {
							e.printStackTrace();
							registerHandler.obtainMessage(Constant.FAIL)
									.sendToTarget();
						}
					} else if (ret == Constant.REGISTERED) {
						logout();
					}
				}
			}.start();
		}

		public void login(final android.os.Handler handler) {
			SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
			String username = sp.getString("name", null);
			String pwd = sp.getString("pwd", null);
			if (pwd == null || username == null) {
				return;
			}
			System.out.println("Login");
			new Thread() {
				public void run() {
					connected = false;
					int ret = connect();
					if (ret == Constant.REGISTERED && myInfo == null
							&& handler instanceof MainHandler) {
						init();
					}
					handler.obtainMessage(ret).sendToTarget();
				}
			}.start();
		}

		public void logout() {
			SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
			SharedPreferences.Editor editor = sp.edit();
			editor.putString("pwd", null);
			editor.commit();
			myInfo = null;
			iteratorService.clear();
			messageService.clearTopics();
			if (mainHandler != null) {
				mainHandler.obtainMessage(Constant.CLEAR).sendToTarget();
			}
		}

		public MessageEntity getMsg(Long id) {
			MessageEntity entity = messageService.getMessage(id);
			if (entity == null) {
				// get the message from server
				getCommit(id);
				return null;
			}
			if (entity.getReplied() != null && entity.getToName() == null) {
				getCommit(entity.getReplied());
				return null;
			}
			return entity;
		}

		// send a message to server
		public void send(MessageEntity message) {
			final CommitModel cMsg = new CommitModel();
			cMsg.setBranch(message.getTopicId());
			cMsg.setAuthor(myInfo.getPrivateInfo().getId());
			cMsg.setMsg(message.getMsg());
			cMsg.setReplied(message.getReplied());
			restClient.post(cMsg, new Post<CommitModel>() {
				@Override
				public void onError(ErrorCode error) {
					if (chatHandler != null) {
						chatHandler.obtainMessage(Constant.SEND_FAIL)
								.sendToTarget();
					}
				}

				@Override
				public void onResponse(long id) {
					System.out.println("Send message reply");
					cMsg.setId(id);
					cMsg.setDate(System.currentTimeMillis());
					pushMessage(cMsg);
					if (!messageService.exist(cMsg.getBranch(),
							Constant.SESSION)) {
						messageService.topTopic(
								messageService.getTopic(cMsg.getBranch()),
								Constant.SESSION);
					}
					if (chatHandler != null) {
						chatHandler.obtainMessage(Constant.SEND_SUCCESS)
								.sendToTarget();
					}
					forward(cMsg.getMsg(), myInfo.getPublicInfo()
							.getFollowedBranch(), id, cMsg.getBranch());
					iteratorService.reset(myInfo.getPublicInfo()
							.getFollowedBranch());
					iteratorService.reset(myInfo.getPrivateInfo()
							.getFollowerBranch());
				}
			});
		}

		public void loadMore(final Long topicId) {
			if (topicId == null || topicId <= 0) {
				System.out.println("Error: illegal topicId");
				return;
			}
			if (messageService.getTopic(topicId) == null) {
				System.out.println("Error: trying to load non-existed topic");
				if (chatHandler != null) {
					chatHandler.obtainMessage(Constant.REACH_END)
							.sendToTarget();
				}
				return;
			}
			try {
				synchronized (iteratorService) {
					if (!iteratorService.contains(topicId)) {
						ByBranchIteratorSpecification spec = new ByBranchIteratorSpecification();
						spec.setBranch(topicId);
						System.out.println("Iterator init");
						restClient
								.iteratorInit(
										topicId,
										spec,
										new IteratorInit<ByBranchIteratorSpecification>() {
											@Override
											public void onError(ErrorCode error) {
												System.out
														.println("By branch iterator error");
												if (chatHandler != null) {
													chatHandler.obtainMessage(
															Constant.REACH_END)
															.sendToTarget();
												}
											}

											@Override
											public void onResponse() {
												iteratorService.init(topicId);
											}
										});
					} else if (!iteratorService.isActive(topicId)) {
						if (chatHandler != null) {
							chatHandler.obtainMessage(Constant.REACH_END)
									.sendToTarget();
						}
					}
					System.out.println("Iterator next");
					restClient.iteratorNext(topicId, Constant.ITERATOR_COUNT,
							false,
							new IteratorNext<ByBranchIteratorSpecification>() {
								@Override
								public void onError(ErrorCode error) {
									System.out
											.println("By branch iterator error");
									iteratorService.reset(topicId);
									if (chatHandler != null) {
										chatHandler.obtainMessage(
												Constant.REACH_END)
												.sendToTarget();
									}
								}

								@Override
								public void onResponse(
										java.util.List<Resource> list) {
									if (list.size() < Constant.ITERATOR_COUNT) {
										iteratorService.end(topicId);
										if (chatHandler != null) {
											chatHandler.obtainMessage(
													Constant.REACH_END)
													.sendToTarget();
										}
									}
									for (Resource res : list) {
										if (!(res instanceof CommitModel)) {
											continue;
										}
										pushMessage((CommitModel) res);
									}
								}
							});
				}
			} catch (IllegalStateException e) {
				if (chatHandler != null) {
					chatHandler.obtainMessage(Constant.REACH_END)
							.sendToTarget();
				}
				e.printStackTrace();
			}
		}

		// transfer data to main
		public LinkedList<TopicEntity> getTopics(final int type) {
			LinkedList<TopicEntity> topics = new LinkedList<TopicEntity>();
			synchronized (messageService) {
				for (TopicEntity topic : messageService.getTopics(type)) {
					if (!topic.isEmpty()) {
						topics.add(topic);
					}
				}
			}
			switch (type) {
			case Constant.SESSION:
			case Constant.DISCOVER:
				break;
			case Constant.TIMELINE:
			case Constant.TREND: {
				final Long topicId = (type == Constant.TIMELINE) ? myInfo
						.getPublicInfo().getFollowedBranch() : myInfo
						.getPrivateInfo().getFollowerBranch();
				if (!iteratorService.contains(topicId)) {
					ByBranchIteratorSpecification spec = new ByBranchIteratorSpecification();
					spec.setBranch(topicId);
					System.out.println("Iterator init");
					restClient.iteratorInit(topicId, spec,
							new IteratorInit<ByBranchIteratorSpecification>() {
								@Override
								public void onError(ErrorCode error) {
									System.out
											.println("Iterator timeline or trend error");
								}

								@Override
								public void onResponse() {
									iteratorService.init(topicId);
									messageService.getTopics(type).clear();
									getTopics(type);
								}
							});
				} else if (iteratorService.isActive(topicId)) {
					System.out.println("Iterator next");
					restClient.iteratorNext(topicId, Constant.ITERATOR_COUNT,
							false,
							new IteratorNext<ByBranchIteratorSpecification>() {
								@Override
								public void onError(ErrorCode error) {
									System.out
											.println("Iterator timeline or trend error");
									iteratorService.reset(topicId);
								}

								@Override
								public void onResponse(
										java.util.List<Resource> list) {
									if (list.size() < Constant.ITERATOR_COUNT) {
										iteratorService.end(topicId);
										mainHandler.obtainMessage(
												Constant.REACH_END)
												.sendToTarget();
									}
									synchronized (messageService) {
										for (Resource msg : list) {
											if (!(msg instanceof ForwardModel)) {
												continue;
											}
											Long origin = ((ForwardModel) msg)
													.getOriginBranch();
											TopicEntity topic = messageService
													.getTopic(origin);
											if (topic == null) {
												topic = new TopicEntity();
												topic.setTopicId(origin);
												getTopicFromRemote(topic);
												topic.addMessage(messageService
														.getMessage(msg.getId()));
											}
											messageService.addTopic(topic,
													type);
											pushMessage((CommitModel) msg);
										}
									}
									if (mainHandler != null) {
										mainHandler.obtainMessage(
												Constant.LOAD_DATA)
												.sendToTarget();
									}
								}
							});
				}
			}
			}
			return topics;
		}

		// called by chat activity, get history message
		public TopicEntity getTopicById(Long topicId) {
			// do something with conn to get msgs for topicId
			return messageService.getTopic(topicId);
		}

		// create a topic asynchronously
		public void createTopic(final String msg) {
			System.out.println("Create topic ");
			if (msg == null || msg.isEmpty()) {
				System.out.println("Sending empty msg while creating topic");
				return;
			}
			final HashSet<String> tags = new HashSet<String>(Arrays.asList(msg
					.split("\\s+")));
			System.out.println("Has " + tags.size() + " tags");
			BranchModel branch = new BranchModel();
			branch.setFounder(myInfo.getPrivateInfo().getId());
			branch.setTags(tags);
			restClient.post(branch, new Post<BranchModel>() {
				@Override
				public void onError(ErrorCode error) {
					System.out.println("Create topic fail");
					if (chatHandler != null) {
						chatHandler.obtainMessage(Constant.CREATE_FAIL)
								.sendToTarget();
					}
				}

				@Override
				public void onResponse(long branch) {
					subscribe(branch);
					TopicEntity topic = new TopicEntity();
					topic.setTopicId(branch);
					topic.setFounder(myInfo.getPrivateInfo().getId());
					topic.setTags(tags);
					messageService.topTopic(topic, Constant.SESSION);
					MessageEntity message = new MessageEntity();
					message.setMsg(msg);
					message.setTopicId(branch);
					send(message);
					setMyInfo();
					System.out
							.println("Create new topic success, id=" + branch);
					if (chatHandler != null) {
						chatHandler.obtainMessage(Constant.CREATE_SUCCESS,
								branch).sendToTarget();
					}
					if (mainHandler != null) {
						mainHandler.obtainMessage(Constant.LOAD_DATA)
								.sendToTarget();
					}
				}
			});
		}

		public void fork(final String msg, final Long parentId) {
			BranchModel branch = new BranchModel();
			final HashSet<String> tags = new HashSet<String>(Arrays.asList(msg
					.split("\\s+")));
			branch.setTags(tags);

			final MessageEntity head = messageService.getTopic(parentId)
					.getLatest();
			if (head.getId() != null) {
				branch.setHead(head.getId());
				restClient.post(branch, new Post<BranchModel>() {
					@Override
					public void onError(ErrorCode error) {
						if (chatHandler != null) {
							chatHandler.obtainMessage(Constant.FORK_FAIL)
									.sendToTarget();
						}
					}

					@Override
					public void onResponse(long branch) {
						subscribe(branch);
						TopicEntity topic = new TopicEntity();
						topic.setTopicId(branch);
						topic.setFounder(myInfo.getPrivateInfo().getId());
						topic.setTags(tags);
						messageService.topTopic(topic, Constant.SESSION);
						setMyInfo();
						System.out.println("Create new topic " + branch);
						if (chatHandler != null) {
							System.out.println("Fork");
							android.os.Message msg = chatHandler
									.obtainMessage(Constant.FORK_SUCCESS);
							Bundle bundle = new Bundle();
							bundle.putLong("branch", branch);
							msg.setData(bundle);
							msg.sendToTarget();
						}

						MessageEntity forkMsg = new MessageEntity();
						forkMsg.setMsg(Constant.FORK_CREATE + Constant.COLON
								+ msg);
						forkMsg.setTopicId(parentId);
						send(forkMsg);
						forkMsg.setMsg(msg);
						forkMsg.setTopicId(branch);
						send(forkMsg);
					}
				});
			}
		}

		public void ignore(Long topicId) {
			// ignore a topic, no longer push it to top
			messageService.getTopic(topicId).setIgnore(true);
		}

		public void forward(String msg, Long topicId, Long refered, Long origin) {
			final ForwardModel forward = new ForwardModel();
			forward.setAuthor(myInfo.getPrivateInfo().getId());
			forward.setBranch(topicId);
			forward.setRefered(refered);
			forward.setMsg(msg);
			forward.setOriginBranch(origin);
			restClient.post(forward, new Post<ForwardModel>() {

				@Override
				public void onError(ErrorCode error) {
					// TODO forward error
					chatHandler.obtainMessage(Constant.FORWARD_FAIL)
							.sendToTarget();
				}

				@Override
				public void onResponse(long id) {
					forward.setId(id);
					forward.setDate(System.currentTimeMillis());
					pushMessage(forward);
					if (chatHandler != null) {
						chatHandler.obtainMessage(Constant.FORWARD_SUCCESS, id)
								.sendToTarget();
					}
					if (forward.getBranch() != myInfo.getPublicInfo()
							.getFollowedBranch()) {
						forward(forward.getMsg(), myInfo.getPublicInfo()
								.getFollowedBranch(), id, forward.getBranch());
					}
					iteratorService.reset(myInfo.getPublicInfo()
							.getFollowedBranch());
					iteratorService.reset(myInfo.getPrivateInfo()
							.getFollowerBranch());
				}
			});
		}

		// topic following
		public void follow(Long follower, Long followed) {
			final FollowModel flw = new FollowModel();
			flw.setFollower(follower);
			flw.setFollowed(followed);
			restClient.post(flw, new Post<FollowModel>() {
				@Override
				public void onError(ErrorCode error) {
					if (chatHandler != null) {
						chatHandler.obtainMessage(Constant.FOLLOW_FAIL,
								flw.getFollowed()).sendToTarget();
					}
				}

				@Override
				public void onResponse(long id) {
					System.out.println("Follow topic response");
					flw.setId(id);
					chatHandler.obtainMessage(Constant.FOLLOW_SUCCESS, flw)
							.sendToTarget();
				}
			});
		}

		// follow a certain user's exclusive topic
		public void follow(Long followed) {
			final FollowModel flw = new FollowModel();
			flw.setFollower(myInfo.getPrivateInfo().getFollowerBranch());
			flw.setFollowed(followed);
			restClient.post(flw, new Post<FollowModel>() {
				@Override
				public void onError(ErrorCode error) {
					if (userHandler != null) {
						userHandler.obtainMessage(Constant.FOLLOW_FAIL,
								flw.getFollowed()).sendToTarget();
					}
				}

				@Override
				public void onResponse(long id) {
					System.out.println("Follow user response");
					flw.setId(id);
					userHandler.obtainMessage(Constant.FOLLOW_SUCCESS, flw)
							.sendToTarget();
				}

			});
		}

		public void unfollow(final FollowModel followModel) {
			if (followModel != null) {
				restClient.delete(followModel, new Delete<FollowModel>() {
					@Override
					public void onError(ErrorCode error) {
						if (userHandler != null) {
							userHandler.obtainMessage(Constant.UNFOLLOW_FAIL,
									followModel.getFollowed()).sendToTarget();
						}
					}

					@Override
					public void onResponse() {
						System.out.println("Unfollow response");
						Long id = followModel.getFollowed();
						userHandler
								.obtainMessage(Constant.UNFOLLOW_SUCCESS, id)
								.sendToTarget();
					}
				});
			}
		}

		public void ifFollowing(String name) {
			UserInfo user = new UserInfo(userService.get(name));
			final Long followed = user.getPublicInfo().getFollowedBranch();
			final Long follower = myInfo.getPrivateInfo().getFollowerBranch();
			ByFollowIteratorSpecification spec = new ByFollowIteratorSpecification();
			spec.setFollowed(followed);
			spec.setFollower(follower);
			restClient.iteratorInit(0, spec,
					new IteratorInit<ByFollowIteratorSpecification>() {
						@Override
						public void onError(ErrorCode error) {
							System.out.println("Iffollowing error");
							userHandler.obtainMessage(Constant.ERROR)
									.sendToTarget();
						}

						@Override
						public void onResponse() {
							restClient
									.iteratorNext(
											0,
											1,
											false,
											new IteratorNext<ByFollowIteratorSpecification>() {
												@Override
												public void onError(
														ErrorCode error) {
													System.out
															.println("Iffollowing error");
													userHandler.obtainMessage(
															Constant.ERROR)
															.sendToTarget();
												}

												@Override
												public void onResponse(
														java.util.List<Resource> list) {
													System.out
															.println("Check if follow response");
													if (userHandler != null) {
														if (list.isEmpty()) {
															FollowModel flw = new FollowModel();
															flw.setFollowed(followed);
															flw.setFollower(follower);
															userHandler
																	.obtainMessage(
																			Constant.LOAD_DATA,
																			0,
																			0,
																			flw)
																	.sendToTarget();
														} else {
															userHandler
																	.obtainMessage(
																			Constant.LOAD_DATA,
																			1,
																			0,
																			list.get(0))
																	.sendToTarget();
														}
													}
												}

											});
						}
					});
		}

		// send keywords to server to discover topics
		public void search(String keystring) {
			System.out.println("Search keys:" + keystring);
			HashSet<String> keywords = new HashSet<String>(
					Arrays.asList(keystring.split("\\s+")));
			ByTagIteratorSpecification spec = new ByTagIteratorSpecification();
			spec.setTags(keywords);
			restClient.iteratorInit(myInfo.getPrivateInfo().getId(), spec,
					new IteratorInit<ByTagIteratorSpecification>() {
						@Override
						public void onError(ErrorCode error) {
							if (mainHandler != null) {
								mainHandler.obtainMessage(Constant.SEARCH_FAIL)
										.sendToTarget();
							}
						}

						@Override
						public void onResponse() {
							messageService.getTopics(Constant.DISCOVER).clear();
							searchMore();
						}
					});
		}

		public void searchMore() {
			final Long searchId = myInfo.getPrivateInfo().getId();
			restClient.iteratorNext(searchId, Constant.ITERATOR_COUNT, false,
					new IteratorNext<ByTagIteratorSpecification>() {
						@Override
						public void onError(ErrorCode error) {
							iteratorService.reset(searchId);
							if (mainHandler != null) {
								mainHandler.obtainMessage(Constant.SEARCH_FAIL)
										.sendToTarget();
							}
						}

						@Override
						public void onResponse(java.util.List<Resource> list) {
							if (list.size() < Constant.ITERATOR_COUNT) {
								iteratorService.end(searchId);
							}
							for (Resource branch : list) {
								if (!(branch instanceof BranchModel)) {
									continue;
								}
								Long id = branch.getId();
								TopicEntity topic = messageService.getTopic(id);
								if (topic == null) {
									topic = new TopicEntity();
									topic.setTopicId(id);
									topic.setFounder(((BranchModel) branch)
											.getFounder());
									topic.setTags(((BranchModel) branch)
											.getTags());
								}
								messageService.addTopic(topic,
										Constant.DISCOVER);
								if (topic.isEmpty()) {
									loadMore(id);
								}
							}
							if (mainHandler != null) {
								mainHandler.obtainMessage(Constant.LOAD_DATA)
										.sendToTarget();
							}
						}
					});
		}

		public ArrayList<String> getFollowerNames() {
			System.out.println("Get followers");
			final ArrayList<UserModel> followers = followService
					.getMyFollowers();
			final Long followed = myInfo.getPublicInfo().getFollowedBranch();
			if (!iteratorService.contains(followed)) { // uninitialized
				ByFollowIteratorSpecification spec = new ByFollowIteratorSpecification();
				spec.setFollowed(followed);
				restClient.iteratorInit(followed, spec,
						new IteratorInit<ByFollowIteratorSpecification>() {
							@Override
							public void onError(ErrorCode error) {
								System.out.println("Get follower names error");
							}

							@Override
							public void onResponse() {
								iteratorService.init(followed);
								getFollowerNames();
							}
						});
			}
			// if iterator is still active
			else if (iteratorService.isActive(followed)) {
				restClient.iteratorNext(followed, Constant.ITERATOR_COUNT,
						false,
						new IteratorNext<ByFollowIteratorSpecification>() {
							@Override
							public void onError(ErrorCode error) {
								iteratorService.reset(followed);
								System.out.println("Get follower names error");
							}

							@Override
							public void onResponse(
									final java.util.List<Resource> list) {
								final ArrayList<Long> toGet = new ArrayList<Long>();
								synchronized (followers) {
									for (Resource res : list) {
										if (!(res instanceof BranchModel)) {
											continue;
										}
										Long userid = ((BranchModel) res)
												.getFounder();
										if (userService.get(userid) != null) {
											followers.add(userService
													.get(userid));
										} else {
											toGet.add(userid);
										}
									}
								}
								if (toGet.isEmpty()) {
									if (followersHandler != null) {
										followersHandler.obtainMessage(
												Constant.LOAD_DATA)
												.sendToTarget();
									}
									if (list.size() < Constant.ITERATOR_COUNT) {
										iteratorService.end(followed);
										if (followersHandler != null) {
											followersHandler.obtainMessage(
													Constant.REACH_END)
													.sendToTarget();
										}
									}
								} else {
									new Thread() {
										public void run() {
											synchronized (followers) {
												for (Long id : toGet) {
													getUsername(id);
													followers.add(userService
															.get(id));
												}
												if (followersHandler != null) {
													followersHandler
															.obtainMessage(
																	Constant.LOAD_DATA)
															.sendToTarget();
												}
												if (list.size() < Constant.ITERATOR_COUNT) {
													iteratorService
															.end(followed);
													if (followersHandler != null) {
														followersHandler
																.obtainMessage(
																		Constant.REACH_END)
																.sendToTarget();
													}
												}
											}
										}
									}.start();
								}
							}
						});
			}
			TreeSet<String> names = new TreeSet<String>();
			for (UserModel user : followers) {
				names.add(user.getUsername());
			}
			return new ArrayList<String>(names);
		}

		public ArrayList<String> getFollowingNames() {
			System.out.println("Get folloings");
			final ArrayList<UserModel> followings = followService
					.getMyFollowings();
			final Long follower = myInfo.getPrivateInfo().getFollowerBranch();
			if (!iteratorService.contains(follower)) { // uninitialized=
				ByFollowIteratorSpecification spec = new ByFollowIteratorSpecification();
				spec.setFollower(follower);
				restClient.iteratorInit(follower, spec,
						new IteratorInit<ByFollowIteratorSpecification>() {
							@Override
							public void onError(ErrorCode error) {
								System.out.println("Get following names error");
							}

							@Override
							public void onResponse() {
								iteratorService.init(follower);
								getFollowingNames();
							}
						});
			} else if (iteratorService.isActive(follower)) {
				restClient.iteratorNext(follower, Constant.ITERATOR_COUNT,
						false,
						new IteratorNext<ByFollowIteratorSpecification>() {
							@Override
							public void onError(ErrorCode error) {
								iteratorService.reset(follower);
								System.out.println("Get following names error");
							}

							@Override
							public void onResponse(
									final java.util.List<Resource> list) {
								final ArrayList<Long> toGet = new ArrayList<Long>();
								synchronized (followService) {
									for (Resource res : list) {
										if (!(res instanceof BranchModel)) {
											continue;
										}
										Long userid = ((BranchModel) res)
												.getFounder();
										if (userService.get(userid) != null) {
											followings.add(userService
													.get(userid));
										} else {
											toGet.add(userid);
										}
									}
								}
								if (toGet.isEmpty()) {
									if (followingsHandler != null) {
										followingsHandler.obtainMessage(
												Constant.LOAD_DATA)
												.sendToTarget();
									}
									if (list.size() < Constant.ITERATOR_COUNT) {
										iteratorService.end(follower);
										if (followingsHandler != null) {
											followingsHandler.obtainMessage(
													Constant.REACH_END)
													.sendToTarget();
										}
									}
								} else {
									new Thread() {
										public void run() {
											synchronized (followings) {
												for (Long id : toGet) {
													getUsername(id);
													followings.add(userService
															.get(id));
												}
												if (followingsHandler != null) {
													followingsHandler
															.obtainMessage(
																	Constant.LOAD_DATA)
															.sendToTarget();
												}
												if (list.size() < Constant.ITERATOR_COUNT) {
													iteratorService
															.end(follower);
													if (followingsHandler != null) {
														followingsHandler
																.obtainMessage(
																		Constant.REACH_END)
																.sendToTarget();
													}
												}
											}
										}
									}.start();
								}
							}
						});
			}
			TreeSet<String> names = new TreeSet<String>();
			for (UserModel user : followings) {
				names.add(user.getUsername());
			}
			return new ArrayList<String>(names);
		}

		public ArrayList<TopicEntity> getToFollowTopics(Long topicId) {

			return null;
		}

		public ArrayList<TopicEntity> getFollowingTopics(Long topicId) {

			return null;
		}

		public void addTag(String tag, TreeSet<String> names) {
			if (tags.containsKey(tag)) {
				tags.get(tag).addAll(names);
			} else {
				tags.put(tag, names);
			}
		}

		public void removeTag(String tag, TreeSet<String> names) {
			if (tags.containsKey(tag)) {
				tags.get(tag).removeAll(names);
			}
		}

		// get a user's public info
		public PublicInfo getUserInfo(String name) {
			UserInfo userInfo = new UserInfo(userService.get(name));
			return userInfo.getPublicInfo();
		}

		// get MyInfo
		public UserInfo getMyInfo() {
			return myInfo;
		}

		// send MyInfo to server
		public void setMyInfo() {
			if (myInfo != null) {
				myInfo.saveSessions(messageService.getTopics(Constant.SESSION));
				restClient.post(myInfo.toUserModel(), new Post<UserModel>() {
					@Override
					public void onError(ErrorCode error) {
						if (accountHandler != null) {
							accountHandler.obtainMessage(Constant.ERROR)
									.sendToTarget();
						}
						if (myInfoHandler != null) {
							myInfoHandler.obtainMessage(Constant.FAIL)
									.sendToTarget();
						}
					}

					@Override
					public void onResponse(long id) {
						if (accountHandler != null) {
							accountHandler.obtainMessage(Constant.SUCCESS)
									.sendToTarget();
						}
						if (myInfoHandler != null) {
							myInfoHandler.obtainMessage(Constant.SUCCESS)
									.sendToTarget();
						}
					}
				});
			}
		}
	}
}
