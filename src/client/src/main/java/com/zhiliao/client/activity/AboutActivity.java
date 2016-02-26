package com.zhiliao.client.activity;

import com.zhiliao.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;

public class AboutActivity extends Activity implements OnTouchListener, OnGestureListener{

	private GestureDetector mGestureDetector;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mGestureDetector = new GestureDetector(this, this);
		setContentView(R.layout.activity_about);
		
		Button buttonBack = (Button) findViewById(R.id.about_ret);
		buttonBack.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					finish();
					overridePendingTransition(R.anim.in_from_left,
							R.anim.out_to_right);
				}
		});
	}

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
	// slide:switch to MainActivity
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e2.getX() - e1.getX() > 100 && Math.abs(velocityX) > 100) {
			//Intent intent = new Intent(ChatActivity.this, MainActivity.class);
			//startActivity(intent);
			//Toast.makeText(this, "向左手势", Toast.LENGTH_SHORT).show();
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);			
		}
		return false;
	}
	@Override
	public boolean onDown(MotionEvent arg0) {
		return false;
	}
	@Override
	public void onLongPress(MotionEvent arg0) {
		}
	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		return false;
	}
	@Override
	public void onShowPress(MotionEvent arg0) {
		}
	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		return false;
	}
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

}
