package com.zhiliao.client.layout;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhiliao.R;

/**
 * 左侧菜单
 */
public class MenuLeftFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.layout_leftmenu, container, false);
	}
}
