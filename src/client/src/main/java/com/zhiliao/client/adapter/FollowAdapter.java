package com.zhiliao.client.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhiliao.R;
import com.zhiliao.server.model.rest.UserModel;


public class FollowAdapter extends BaseAdapter {

	private List<UserModel> user;
	private LayoutInflater mInflater;
	private Context context;
	public FollowAdapter(Context context, List<UserModel> user) {
		this.context = context;
		this.user = user;
		mInflater = LayoutInflater.from(context);
	}

	public Object getItem(int position) {
		return user.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		return 0;
	}

	public int getViewTypeCount() {
		return 1;
	}
	//setAdapter时，首先会执行getCount方法，此处必须返回非零，否则getView不执行
	public int getCount() {
		return user.size();
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final UserModel entity = user.get(position);
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.user_item, null);
			viewHolder = new ViewHolder();
			viewHolder.name = (TextView) convertView.findViewById(R.id.user_item_name);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.name.setText(entity.getUsername());
		//点击时    屏幕显示"onclick"
		viewHolder.name.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
		//长按时    屏幕显示"onlongclick"
		viewHolder.name.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				
				return true;
			}
		});
		return convertView;
	}

	static class ViewHolder {
		public TextView name;
	}
}