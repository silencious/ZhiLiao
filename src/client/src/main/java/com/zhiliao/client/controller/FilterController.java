package com.zhiliao.client.controller;

import java.util.ArrayList;

import com.zhiliao.server.model.rest.UserModel;


public class FilterController {
	private UserModel user;

	public void setUser(UserModel user){
		this.user=user;
	}
	
	public void addFriendsTag(int position,ArrayList<String> ToAdd, ArrayList<String> Added){
		String tag=ToAdd.get(position);
		if(sendTag(tag,0)){
			ToAdd.remove(position);
			Added.add(tag);
		}
	}
	public void addCustomTag(String tag,ArrayList<String> ToAdd, ArrayList<String> Added){
		if(sendTag(tag,1)){
			Added.add(tag);
		}
	}
	public void removeTag(int position,ArrayList<String> ToAdd, ArrayList<String> Added){
		String tag=Added.get(position);
		if(sendTag(tag,2)){
			Added.remove(position);
			//待填写
			if(true){ //是朋友tag   FriendsTagList。indexOf(tag)>=0
				ToAdd.add(tag);
			}
		}
	}
	
	
	
	public boolean sendTag(String tag,int type){
		//???????
		
		//????msg????
		
		//???
		return true;
	}
	/**
	 * 初始化AddedList 和 ToAddList
	 */
	private void updateList(ArrayList<String> ToAdd, ArrayList<String> Added){
		
	}
}
