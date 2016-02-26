package com.zhiliao.client.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zhiliao.R;

public class FollowListAdapter extends BaseAdapter{
	private LayoutInflater mInflater;
	private Context context;
	private ArrayList<String> list = new ArrayList<String>();
	public FollowListAdapter(Context context) {
		this.context = context;
		this.mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int position, View convertView,
			ViewGroup parent) {

		ViewHolder holder = null;
		if (convertView == null) {

			holder = new ViewHolder();

			convertView = mInflater.inflate(R.layout.add_friend_item, null);
			holder.name = (TextView) convertView
					.findViewById(R.id.add_friend_item_name);
			holder.btnEnter = (ImageButton) convertView
					.findViewById(R.id.add_friend_item_add);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.name.setText((String) list.get(position).toString());
//		holder.btnEnter.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent();
//				intent.setClass(context, UserActivity.class);
//				Bundle b = new Bundle();
//				b.putString("friend", list.get(position).toString());
//				intent.putExtras(b);
//				startActivity(intent);
//			}
//		});

		return convertView;
	}
	
	public final class ViewHolder {
		public TextView name;
		public ImageButton btnEnter;
	}
}
