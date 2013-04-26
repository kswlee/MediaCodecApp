package com.test.colorconverter;

import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

public class ColorConvertUtil {
	
	public Bitmap convertQCOMVideoBufferAsRGB565(byte [] src, int width, int height) {
		byte [] out = new byte[width*height*2];
        convert(width, height, src, out);
        
        Bitmap bmp = Bitmap.createBitmap(width, height, Config.RGB_565);
        ByteBuffer buffer = ByteBuffer.allocateDirect(out.length);
        buffer.put(out);
        buffer.position(0);
        bmp.copyPixelsFromBuffer(buffer);
        out = null;
        buffer.clear();
		
		return bmp;
	}
	
	public Bitmap convertBufferAsRGB565(byte [] src, int srcFormat, int width, int height) {			
		byte [] out = new byte[width*height*2];
		convertColor(srcFormat, width, height, src, out);
        
        Bitmap bmp = Bitmap.createBitmap(width, height, Config.RGB_565);
        ByteBuffer buffer = ByteBuffer.allocateDirect(out.length);
        buffer.put(out);
        buffer.position(0);
        bmp.copyPixelsFromBuffer(buffer);
        out = null;
        buffer.clear();
		
		return bmp;
	}	

    public native String  convert(int width, int height, byte[] buffer, byte[] outbuffer);
    
    public native String  convertColor(int srcFormat, int width, int height, byte[] buffer, byte[] outbuffer);

    static {
        System.loadLibrary("colorconvert-jni");
    }
}
