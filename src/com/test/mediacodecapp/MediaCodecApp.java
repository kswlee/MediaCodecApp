package com.test.mediacodecapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MediaCodecApp extends Activity{
	@SuppressLint("SdCardPath")
	private final static String VIDEO_CONTENT = "/sdcard/frameCount.mp4";	
	private SurfaceView mSurfaceView = null;
	private Surface mSurface = null;

	@Override
	public void onCreate(Bundle icicle) {
	    super.onCreate(icicle);
	    setContentView(R.layout.activity_main);
	      	    
	    mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);	    
	    mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {			
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				mSurface = null;
			}
			
			@Override
			public void surfaceCreated(SurfaceHolder holder) {				
			}
			
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width,
					int height) {		
				mSurface = holder.getSurface();
			}
		});
	    
	    Button extractButton = (Button) findViewById(R.id.button_snapshot);
	    extractButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				decodeFrames(mSurface);
			}	    	
	    });
	}
	
	private MediaDecoder mDecoder = null;
	private void decodeFrames(Surface surface) {
		File videoFile = new File(VIDEO_CONTENT);
		if (false == videoFile.exists()) {
			copyAssets();
		}
		
		mDecoder = new MediaDecoder(VIDEO_CONTENT, surface, MediaCodecApp.this, new MediaDecoder.OnFrameAvailabkeListener() {			
			@Override
			public void onFrameAvailable(long timestamp, int index, boolean EOS) {
				Log.i("TAG", "frame available " + index);
				mDecoder.render(index);
				
				if (EOS) {
					mDecoder.release();					
					MediaCodecApp.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(MediaCodecApp.this, "End of stream", Toast.LENGTH_LONG).show();
						}
						
					});
				}
			}
		});		
		mDecoder.decode();
	}
	
	private void copyAssets() {
	    AssetManager assetManager = getAssets();
	    String[] files = null;
	    try {
	        files = assetManager.list("");
	    } catch (IOException e) {
	        Log.e("tag", "Failed to get asset file list.", e);
	    }
	    for(String filename : files) {
	        InputStream in = null;
	        OutputStream out = null;
	        try {
	          in = assetManager.open(filename);
	          File outFile = new File(VIDEO_CONTENT);
	          out = new FileOutputStream(outFile);
	          copyFile(in, out);
	          in.close();
	          in = null;
	          out.flush();
	          out.close();
	          out = null;
	        } catch(IOException e) {
	            Log.e("tag", "Failed to copy asset file: " + filename, e);
	        }       
	    }
	}
	
	private void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}	
}