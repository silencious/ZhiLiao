package com.zhiliao.client.util;

public class Constant {
	// Dialog
	public final static String ADD_MEMBER = "添加成员";
	public final static String ADD_TAG = "添加标签";
	public final static String REMOVE_MEMBER = "移除成员";
	public final static String REMOVE_TAG = "移除标签";
	public final static String DELETE_TAG = "删除标签";
	public final static String IF_SURE_TO_LOGOUT = "确定退出登录吗？";
	public final static String OK = "确定";
	public final static String CANCEL = "取消";
	public final static String LOGOUT = "退出";
	public static final String UNFOLLOW_TOPIC = "取消引用";
	public static final String FOLLOW_TOPIC = "话题引用";
	public static final String IF_SURE_TO_UNFOLLOW_TOPIC = "确定取消引用话题吗?";
	public final static String IF_SURE_TO_DELETE_TAG_0 = "确定删除标签\"";
	public final static String IF_SURE_TO_DELETE_TAG_1 = "\"吗?";
	public final static String IF_SURE_TO_REMOVE_TAG_0 = "确定移除标签\"";
	public final static String IF_SURE_TO_REMOVE_TAG_1 = "\"吗?";
	public final static String FORK_CREATE = "创建话题分支";
	public final static String FORK = "话题分支";
	public final static String CHOOSE_TOPIC = "选择话题";
	
	//Toast
	public final static String WRONG_PWD = "密码错误";
	public final static String MODIFY_SUCCESS = "修改成功";
	public final static String INCONSISTENT_PWD = "密码不一致";
	public final static String CHECK_NAME_PWD = "请检查用户名及密码";
	public final static String CHECK_NETWORK = "请检查网络状况";
	public final static String NAME_PWD_EMPTY_ERROR = "用户名及密码不能为空";
	public final static String EMAIL_EMPTY_ERROR = "邮箱不能为空";
	public final static String EMPTY_ERROR = "输入不能为空";
	public final static String MSG = "信息";
	public final static String MODIFY_FAILED = "修改失败";
	public final static String SEND_FAILED = "发送失败";
	public final static String CREATE_TOPIC_FAILED = "创建话题失败";
	public final static String CREATE_TOPIC_BRANCH_FAILED = "创建话题分支失败";
	public final static String CREATING_TOPIC_BRANCH = "正在创建话题分支";
	public final static String CURRENT_TOPIC = "当前话题:";
	public final static String ADD_FAILED = "添加失败";
	public final static String REMOVE_FAILED = "移除失败";
	public final static String DELETE_FAILED = "删除失败";
	public final static String LOGIN_FAILED = "登录失败";
	public final static String SAVE_SUCCESSED = "保存成功";
	public final static String REGISTER_FAILED = "注册失败";
	public final static String REGISTER_SUCCESSED = "注册成功";
	public final static String FOCUS_FAILED = "关注失败";
	public final static String CANCEL_FOCUS_FAILED = "取消关注失败";
	public static final String FORK_WAIT = "正在创建中，请稍候重试";
	public final static String CREATE_MSG_ERROR = "内容不能为空";
	public final static String LOAD_ERROR = "加载信息出错";
	
	// ProgressDialog
	public final static String WAIT = "请稍候";
	public final static String MODIFYING = "正在修改...";
	public final static String SEARCHING = "正在搜索...";
	public final static String LOADING = "正在加载...";
	public final static String ADDING = "正在添加...";
	public final static String REMOVING = "正在移除...";
	public final static String DELETING = "正在删除...";
	public final static String LONGING_IN = "正在登录...";
	public final static String REGISTERING = "正在注册...";
	public final static String SAVING = "正在保存...";
	public final static String WAITING = "等待响应...";
	public static final String SENDING = "正在发送...";


	// OTHERS
	public final static String SELECT_ALL = "全选";
	public final static String FOCUS = "关注";
	public final static String CANCEL_FOCUS = "取消关注";
	public static final String RESPONSE = "回复：";
	public static final String CREATING = "正在创建";
	public static final String FORK_BRANCH = "创建分支";
	public static final String CLOSE = "收起";
	public static final String OPEN = "展开";
	public static final String MALE = "男";
	public static final String FEMALE = "女";	
	
	//Token
	public final static String ELLIPSIS = "...";
	public final static String COMMA = ",";
	public final static String QUOTES = "\"" ;
	public final static String COLON = ":" ;

	// main menu topic type
	public final static int SESSION = 0x00;
	public final static int DISCOVER = 0x01;
	public final static int TIMELINE = 0x02;
	public final static int TREND = 0x03;
	
	// Message.what
	// general
	public final static int SUCCESS = 0x00;
	public final static int FAIL = 0x01;
	public final static int ERROR = 0x02;
	public final static int LOAD_DATA = 0x03;
	public final static int REACH_END = 0x04;
	public final static int LATER = 0x05;
	public final static int REGISTERED = 0x06;
	public final static int GUEST = 0x07;
	public final static int CLEAR= 0x08;
	// login & main
	public final static int IGNORE = 0x10;
	public final static int SEARCH_FAIL = 0x11;	
	// chat
	public final static int SEND_SUCCESS = 0x20;
	public final static int SEND_FAIL = 0x21;
	public final static int CREATE_SUCCESS = 0x22;
	public final static int CREATE_FAIL = 0x23;
	public final static int FORK_SUCCESS = 0x24;
	public final static int FORK_FAIL = 0x25;
	public final static int PUSH = 0x26;
	public final static int CHANGE_REPLIED = 0x27;
	public final static int GET_REPLIED = 0x28;
	public final static int FORWARD_SUCCESS = 0x29;
	public final static int FORWARD_FAIL = 0x2A;
	// follow part
	public final static int FOLLOW_SUCCESS = 0x30;
	public final static int FOLLOW_FAIL = 0x31;
	public final static int UNFOLLOW_SUCCESS = 0x32;
	public final static int UNFOLLOW_FAIL = 0x33;
	public final static int ADD_TAG_SUCCESS = 0x34;
	public final static int ADD_TAG_FAIL = 0x35;
	public final static int DELETE_TAG_SUCCESS = 0x36;
	public final static int DELETE_TAG_FAIL = 0x37;
	public final static int ADD_MEMBER_SUCCESS = 0x38;
	public final static int ADD_MEMBER_FAIL = 0x39;
	public final static int DELETE_MEMBER_SUCCESS = 0x3A;
	public final static int DELETE_MEMBER_FAIL = 0x3B;
	
	public final static int ITERATOR_COUNT = 16;
	// iterator types
	public final static long BY_BRANCH = 0x90;
	public final static long BY_TAG = 0x91;
	public final static long BY_FOLLOWED = 0x92;
	public final static long BY_FOLLOWER = 0x93;
}