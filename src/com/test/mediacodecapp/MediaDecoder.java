package com.test.mediacodecapp;

import java.nio.ByteBuffer;
import java.util.Locale;

import com.test.colorconverter.ColorConvertUtil;

import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.Surface;

public class MediaDecoder {
	public interface OnFrameAvailabkeListener {
		public void onFrameAvailable(Bitmap bmp, long timestamp, int index);
	};
	
	private MediaCodec mCodec;
	private MediaExtractor mExtractor;
	private String mFilePath;
	private MediaFormat mFormat;
	MediaCodecApp mContext;
	MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
	OnFrameAvailabkeListener mFrameListener = null;
	
	public MediaDecoder(String path, Surface surface, MediaCodecApp activity, OnFrameAvailabkeListener l) {
		mFilePath = path;
		
		mExtractor = new MediaExtractor( );
		mExtractor.setDataSource( mFilePath );
				
		int nCount = mExtractor.getTrackCount();
		for (int i = 0; i < nCount; ++i) {
			MediaFormat mf = mExtractor.getTrackFormat(i);			
			if (mf.getString(MediaFormat.KEY_MIME).toLowerCase(Locale.UK).contains("video")) {
				mFormat = mExtractor.getTrackFormat(i);
				break;
			}
		}		
		
		mContext = activity;
		mFrameListener = l;

		mRetriever.setDataSource( mFilePath );				
	}
	
	public void decode() {
		createDecoder();
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				doDecode();
			}			
		}); t.start();
	}
	
	private ByteBuffer [] mCodecInputBuffers;
	private ByteBuffer [] mCodecOutputBuffers;
	private void createDecoder( )
	{		
		mCodec = MediaCodec.createDecoderByType( "video/avc" );
		mCodec.configure(mFormat, null, null, 0);
		mCodec.start( );
		
		mExtractor.selectTrack(0);
	}
		
	private void doDecode() {
		mCodecInputBuffers = mCodec.getInputBuffers();
		mCodecOutputBuffers = mCodec.getOutputBuffers();
		
		boolean sawInputEOS = false;
		int frameIndex = 0;
		for (;;) {
			long presentationTimeUs = 0;
			int inputBufferIndex = mCodec.dequeueInputBuffer(-1);
			if (inputBufferIndex >= 0) {				
				ByteBuffer dstBuf = mCodecInputBuffers[ inputBufferIndex ];

				int sampleSize = mExtractor.readSampleData( dstBuf, 0 );
		        Log.d( "Sample Size", String.valueOf( sampleSize ) );
		       
		        if( sampleSize < 0 )
		        {
		            sawInputEOS = true;
		            sampleSize = 0;		            
		        }
		        else
		        {
		            presentationTimeUs = mExtractor.getSampleTime( );
		        }
		        Log.d( "", "Input Buffer" );
		        Log.d( "InputBufIndex:", String.valueOf( inputBufferIndex ) );
		        Log.d( "PresentationTimeUS", String.valueOf( presentationTimeUs ) );
		        		        
		        mCodec.queueInputBuffer( inputBufferIndex, 0, // offset
		                sampleSize, presentationTimeUs, sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0 );
		        
		        if( !sawInputEOS )
		        {
		            Log.d( "Extractor", " Advancing" );
		            mExtractor.advance( );

		        } else {
		        	break;
		        }
			}
					
			// 
			// handle output buffer 
			//
		    MediaCodec.BufferInfo  info = new MediaCodec.BufferInfo ();
		    final int res = mCodec.dequeueOutputBuffer(info, 1000);
		    if( res >= 0 )
		    {
		    	// boolean sawOutputEOS = false;
		        int outputBufIndex = res;
		        ByteBuffer buf = mCodecOutputBuffers[ outputBufIndex ];		                
		        		        
		        final byte[] chunk = new byte[ info.size ];
		        buf.get( chunk ); // Read the buffer all at once		        
		        buf.clear( ); // ** MUST DO!!! OTHERWISE THE NEXT TIME YOU GET THIS SAME BUFFER BAD THINGS WILL HAPPEN		        
		        if( chunk.length > 0 )
		        {
		            Log.d( "Chunk: ", String.valueOf( chunk.length ) );
		            final MediaFormat oformat = mCodec.getOutputFormat();
		            int width = oformat.getInteger(MediaFormat.KEY_WIDTH);
		            int height = oformat.getInteger(MediaFormat.KEY_HEIGHT);
		            int colorFormat = oformat.getInteger(MediaFormat.KEY_COLOR_FORMAT);
		            getColorFormat(oformat);
		            
		            ColorConvertUtil converter = new ColorConvertUtil();
		            Bitmap bmp = converter.convertBufferAsRGB565(chunk, colorFormat, width, height);
		            
		            if (mFrameListener != null) {
		            	mFrameListener.onFrameAvailable(bmp, presentationTimeUs, frameIndex++);
		            }
		        } 
		                 		            		      
		        mCodec.releaseOutputBuffer( outputBufIndex, true /* render */);

		        if( ( info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM ) != 0 )
		        {
		        }
		    }
		    else if( res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED )
		    {
		    	mCodecOutputBuffers = mCodec.getOutputBuffers( );
		    }
		    else if( res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED )
		    {
		        final MediaFormat oformat = mCodec.getOutputFormat( );
		        getColorFormat(oformat);
		    }	
		}		
	}
	
	private void getColorFormat(MediaFormat format) {
		int colorFormat = format.getInteger(MediaFormat.KEY_COLOR_FORMAT);
		
		int QOMX_COLOR_FormatYUV420PackedSemiPlanar64x32Tile2m8ka = 0x7FA30C03;
		
		String formatString = "";  
		if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_Format12bitRGB444) {
			formatString = "COLOR_Format12bitRGB444";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_Format16bitARGB1555) {
			formatString = "COLOR_Format16bitARGB1555";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_Format16bitARGB4444) {
			formatString = "COLOR_Format16bitARGB4444";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_Format16bitBGR565) {
			formatString = "COLOR_Format16bitBGR565";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_Format16bitRGB565) {
			formatString = "COLOR_Format16bitRGB565";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_Format18bitARGB1665) {
			formatString = "COLOR_Format18bitARGB1665";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_Format18BitBGR666) {
			formatString = "COLOR_Format18BitBGR666";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_Format18bitRGB666) {
			formatString = "COLOR_Format18bitRGB666";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_Format19bitARGB1666) {
			formatString = "COLOR_Format19bitARGB1666";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_Format24BitABGR6666) {
			formatString = "COLOR_Format24BitABGR6666";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_Format24bitARGB1887) {
			formatString = "COLOR_Format24bitARGB1887";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_Format24BitARGB6666) {
			formatString = "COLOR_Format24BitARGB6666";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_Format24bitBGR888) {
			formatString = "COLOR_Format24bitBGR888";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_Format24bitRGB888) {
			formatString = "COLOR_Format24bitRGB888";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_Format25bitARGB1888) {
			formatString = "COLOR_Format25bitARGB1888";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_Format32bitARGB8888) {
			formatString = "COLOR_Format32bitARGB8888";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_Format32bitBGRA8888) {
			formatString = "COLOR_Format32bitBGRA8888";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_Format8bitRGB332) {
			formatString = "COLOR_Format8bitRGB332";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatCbYCrY) {
			formatString = "COLOR_FormatCbYCrY";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatCrYCbY) {
			formatString = "COLOR_FormatCrYCbY";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatL16) {
			formatString = "COLOR_FormatL16";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatL2) {
			formatString = "COLOR_FormatL2";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatL24) {
			formatString = "COLOR_FormatL24";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatL32) {
			formatString = "COLOR_FormatL32";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatL4) {
			formatString = "COLOR_FormatL4";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatL8) {
			formatString = "COLOR_FormatL8";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatMonochrome) {
			formatString = "COLOR_FormatMonochrome";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatRawBayer10bit) {
			formatString = "COLOR_FormatRawBayer10bit";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatRawBayer8bit) {
			formatString = "COLOR_FormatRawBayer8bit";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatRawBayer8bitcompressed) {
			formatString = "COLOR_FormatRawBayer8bitcompressed";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYCbYCr) {
			formatString = "COLOR_FormatYCbYCr";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYCrYCb) {
			formatString = "COLOR_FormatYCrYCb";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV411PackedPlanar) {
			formatString = "COLOR_FormatYUV411PackedPlanar";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV411Planar) {
			formatString = "COLOR_FormatYUV411Planar";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar) {
			formatString = "COLOR_FormatYUV420PackedPlanar";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar) {
			formatString = "COLOR_FormatYUV420PackedSemiPlanar";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422PackedPlanar) {
			formatString = "COLOR_FormatYUV422PackedPlanar";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422PackedSemiPlanar) {
			formatString = "COLOR_FormatYUV422PackedSemiPlanar";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422Planar) {
			formatString = "COLOR_FormatYUV422Planar";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422PackedSemiPlanar) {
			formatString = "COLOR_FormatYUV422PackedSemiPlanar";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422Planar) {
			formatString = "COLOR_FormatYUV422Planar";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422SemiPlanar) {
			formatString = "COLOR_FormatYUV422SemiPlanar";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV444Interleaved) {
			formatString = "COLOR_FormatYUV444Interleaved";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar) {
			formatString = "COLOR_QCOM_FormatYUV420SemiPlanar";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar) {
			formatString = "COLOR_TI_FormatYUV420PackedSemiPlanar";
		} else if (colorFormat == QOMX_COLOR_FormatYUV420PackedSemiPlanar64x32Tile2m8ka) {
			formatString = "QOMX_COLOR_FormatYUV420PackedSemiPlanar64x32Tile2m8ka";
		} else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) {
			formatString = "COLOR_FormatYUV420Planar";
		}
		
		Log.i("TAG", formatString);
	}
}
