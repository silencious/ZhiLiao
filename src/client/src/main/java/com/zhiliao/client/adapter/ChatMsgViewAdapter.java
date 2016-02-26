package com.zhiliao.client.adapter;

import java.util.LinkedList;

import com.zhiliao.R;

import com.zhiliao.client.activity.ChatActivity;
import com.zhiliao.client.activity.FollowingsActivity;
import com.zhiliao.client.activity.LoginActivity;
import com.zhiliao.client.activity.UserActivity;
import com.zhiliao.client.entity.MessageEntity;
import com.zhiliao.client.util.Constant;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
public class ChatMsgViewAdapter extends BaseExpandableListAdapter {
	ChatActivity chatActivity;
	private LinkedList<MessageEntity> chatMsgList;
	private LinkedList<LinkedList<MessageEntity>> referList;
	private String userName;
	
	public ChatMsgViewAdapter(ChatActivity chatActivity, LinkedList<MessageEntity> chatMsgList,
			LinkedList<LinkedList<MessageEntity>> referList, String username) {
		super();
		this.chatActivity = chatActivity;
		this.chatMsgList = chatMsgList;
		this.referList = referList;
		this.userName = username;
	}
	public View getGroupView(final int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) chatActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.chat_item, null);
		}
		RelativeLayout rlReceiverBar = (RelativeLayout) view.findViewById(R.id.chat_item_receiver_bar);
		TextView receiver = (TextView) view.findViewById(R.id.chat_item_receiver);
	    TextView sender = (TextView) view.findViewById(R.id.chat_item_sender);
		TextView originalSender = (TextView) view.findViewById(R.id.chat_item_original_sender);
		TextView comment = (TextView) view.findViewById(R.id.chat_item_comment);
		TextView forward = (TextView) view.findViewById(R.id.chat_item_forward);
		TextView refer = (TextView) view.findViewById(R.id.chat_item_response);
		TextView seeOriginal = (TextView) view.findViewById(R.id.chat_item_see_original);
		TextView more = (TextView) view.findViewById(R.id.chat_item_more);
		
		String senderName = chatMsgList.get(groupPosition).getFromName();		
		if(senderName.equals(userName)){
			refer.setVisibility(View.GONE);
		}
		if(senderName.length() > 16){
			senderName = senderName.substring(12)+"...";
		}
		sender.setText(senderName);
		sender.getPaint().setFakeBoldText(true);
		//originalSender.getPaint().setFakeBoldText(true);
		comment.setText(chatMsgList.get(groupPosition).getMsg());
		String toName = chatMsgList.get(groupPosition).getToName();
		if(toName != null){
			if(toName.length() > 16){
				toName = toName.substring(12)+"...";
			}
			rlReceiverBar.setVisibility(View.VISIBLE);
			receiver.setText(toName);
		}else{
			rlReceiverBar.setVisibility(View.GONE);
		}
		
		if(chatMsgList.get(groupPosition).getReplied() != null){
			more.setVisibility(View.VISIBLE);
			if(isExpanded){
				more.setText(Constant.CLOSE);
			}else{
				more.setText(Constant.OPEN);			
			}
		}else{
			more.setVisibility(View.GONE);
		}
		//setOnClickListener
		
		forward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				chatActivity.singleTopicDialog(chatMsgList.get(groupPosition));
			}
		});
		refer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO
				MessageEntity msg = chatMsgList.get(groupPosition);
				chatActivity.updateToName(msg.getFromName(), msg.getId());
			}
		});
		seeOriginal.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO
				//
			}
		});
		
		//TODO 是否是要跳转到userActivity？
		sender.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkUser(chatMsgList.get(groupPosition).getFromName());
			}
		});
		
		//TODO 是否是要跳转到userActivity？
		receiver.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkUser(chatMsgList.get(groupPosition).getToName());
			}
		});
		originalSender.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO
				//checkUser(chatMsgList.get(groupPosition).getOriginalName());
			}
		});
			
		return view;
	}


	public long getGroupId(int groupPosition) {
		return groupPosition;
	}
	
	public Object getGroup(int groupPosition) {
		return chatMsgList.get(groupPosition);
	}

	public int getGroupCount() {
		return chatMsgList.size();
	}
	//**************************************
	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) chatActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.refer_item, null);	
		}
		RelativeLayout rlReceiverBar = (RelativeLayout) view.findViewById(R.id.refer_item_receiver_bar);
		TextView receiver = (TextView) view.findViewById(R.id.refer_item_receiver);
		TextView sender = (TextView) view.findViewById(R.id.refer_item_sender);
		TextView originalSender = (TextView) view.findViewById(R.id.refer_item_original_sender);
		TextView comment = (TextView) view.findViewById(R.id.refer_item_comment);
		TextView forward = (TextView) view.findViewById(R.id.refer_item_forward);
		TextView refer = (TextView) view.findViewById(R.id.refer_item_response);
		TextView seeOriginal = (TextView) view.findViewById(R.id.refer_item_see_original);
		
		String senderName = referList.get(groupPosition).get(childPosition).getFromName();
		if(senderName.equals(userName)){
			refer.setVisibility(View.GONE);
		}
		if(senderName.length() > 16){
			senderName = senderName.substring(12)+"...";
		}
		sender.setText(senderName);
		sender.getPaint().setFakeBoldText(true);
		//originalSender.getPaint().setFakeBoldText(true);
		comment.setText(referList.get(groupPosition).get(childPosition).getMsg());
		String toName = referList.get(groupPosition).get(childPosition).getToName();
		if(toName != null){
			if(toName.length() > 16){
				toName = toName.substring(12)+"...";
			}
			rlReceiverBar.setVisibility(View.VISIBLE);
			receiver.setText(toName);
		}else{
			rlReceiverBar.setVisibility(View.GONE);
		}
		//originalSender.setText(referList.get(groupPosition).get(childPosition).getOriginalName());
		
		//setOnClickListener

		forward.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				//TODO
				chatActivity.singleTopicDialog(referList.get(groupPosition).get(childPosition));
			}
		});
		
		refer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MessageEntity msg = referList.get(groupPosition).get(childPosition);
				chatActivity.updateToName(msg.getFromName(), msg.getId());
			}
		});
		seeOriginal.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO
			}
		});
				
		//TODO 是否是要跳转到userActivity？
		sender.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkUser(referList.get(groupPosition).get(childPosition).getFromName());
			}
		});
		
		//TODO 是否是要跳转到userActivity？
		receiver.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkUser(referList.get(groupPosition).get(childPosition).getToName());
			}
		});
		
		originalSender.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//checkUser(referList.get(groupPosition).get(childPosition)..getOriginalName());
			}
		});
		return view;
	}

	private void checkUser(String name){
		Intent intent = new Intent();
		intent.setClass(chatActivity, UserActivity.class);
		Bundle b = new Bundle();
		b.putString("friend", name);
		intent.putExtras(b);
		chatActivity.startActivity(intent);
	}
	
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}
	
	public Object getChild(int groupPosition, int childPosition) {
		return referList.get(groupPosition).get(childPosition);
	}

	public int getChildrenCount(int groupPosition) {
		return referList.get(groupPosition).size();
	}
	//**************************************
	public boolean hasStableIds() {
		return true;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}

