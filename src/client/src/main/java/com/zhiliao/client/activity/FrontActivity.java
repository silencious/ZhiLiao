package com.zhiliao.client.activity;

//import android.net.Uri;
import android.app.Activity;
//import android.app.AlertDialog;
import android.content.Intent;
//import android.view.View;
//import android.widget.EditText;
import android.os.Bundle;
import android.view.View;

import com.zhiliao.R;

public class FrontActivity extends Activity {
	public static FrontActivity instance = null;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_front);
		instance = this;
	}

	public void goto_login(View v) {
		Intent intent = new Intent();
		intent.setClass(FrontActivity.this, LoginActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.anim_enter, 
				R.anim.anim_exit);
	}

	public void goto_register(View v) {
		Intent intent = new Intent();
		intent.setClass(FrontActivity.this, RegisterActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.anim_enter, 
				R.anim.anim_exit);
	}
	
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
	}
}
