package com.test.mediacodecapp;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MediaCodecApp extends Activity{
	private final static String VIDEO_CONTENT = "/sdcard/DCIM/movie.mp4";	
	private ImageView mBitmapView;

	@Override
	public void onCreate(Bundle icicle) {
	    super.onCreate(icicle);
	    setContentView(R.layout.activity_main);
	      
	    LinearLayout layout = (LinearLayout) findViewById(R.id.textureView_wrap);
	    mBitmapView = new ImageView(this);
	    layout.addView(mBitmapView);
	    
	    Button extractButton = (Button) findViewById(R.id.button_extract);
	    extractButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				decodeFrames();
			}	    	
	    });
	}
	
	private void decodeFrames() {
		MediaDecoder decoder = new MediaDecoder(VIDEO_CONTENT, null, MediaCodecApp.this, new MediaDecoder.OnFrameAvailabkeListener() {
			
			@Override
			public void onFrameAvailable(Bitmap bmp, long timestamp, int index) {
				Log.i("TAG", "frame available " + index);
				final Bitmap fBmp = bmp;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mBitmapView.setImageBitmap(fBmp);
					}
				});
			}
		});
		
		decoder.decode();
	}
}