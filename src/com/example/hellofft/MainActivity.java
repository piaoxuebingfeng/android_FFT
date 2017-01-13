package com.example.hellofft;
import android.app.Activity;  
import android.content.Context;  
import android.graphics.Canvas;  
import android.graphics.Color;  
import android.graphics.Paint;  
import android.graphics.Rect;  
import android.media.AudioManager;  
import android.media.MediaPlayer;  
import android.media.audiofx.Equalizer;  
import android.media.audiofx.Visualizer;  
import android.os.Bundle;  
import android.util.Log;  
import android.view.Gravity;  
import android.view.View;  
import android.view.ViewGroup;  
import android.view.WindowManager;  
import android.widget.LinearLayout;  
import android.widget.SeekBar;  
import android.widget.TextView;  
 
public class MainActivity extends Activity  
{  
   private static final String TAG = "AudioFxActivity";  
 
   private static final float VISUALIZER_HEIGHT_DIP = 160f;  
 
   private MediaPlayer mMediaPlayer;  
   private Visualizer mVisualizer;  
   private Equalizer mEqualizer;  
 
   private LinearLayout mLinearLayout;  
   private VisualizerView mVisualizerView;  
   private TextView mStatusTextView;  
   private TextView mInfoView;  
 
   @Override  
   public void onCreate(Bundle icicle)  
   {  
       super.onCreate(icicle);  
         
       mStatusTextView = new TextView(this);  
 
       mLinearLayout = new LinearLayout(this);  
       mLinearLayout.setOrientation(LinearLayout.VERTICAL);  
       mLinearLayout.addView(mStatusTextView);  
 
       setContentView(mLinearLayout);  
 
       // Create the MediaPlayer  
       mMediaPlayer = MediaPlayer.create(this, R.raw.my_life);  
       Log.d(TAG,  
               "MediaPlayer audio session ID: "  
                       + mMediaPlayer.getAudioSessionId());  
 
       setupVisualizerFxAndUI();  
       setupEqualizerFxAndUI();  
 
       // Make sure the visualizer is enabled only when you actually want to  
       // receive data, and  
       // when it makes sense to receive data. 
       //这个方法的主要作用是为了控制何时去采集频谱数据，你应该只是愿意采集你所关心的音乐数据，而不关心声音输出器中任何的声音。
       //而且对mVisualizer的许多设置必须在setEnable之前完成。并且结束功能后，要记得setEnable(false)
       mVisualizer.setEnabled(true);  
 
       // When the stream ends, we don't need to collect any more data. We  
       // don't do this in  
       // setupVisualizerFxAndUI because we likely want to have more,  
       // non-Visualizer related code  
       // in this callback.  
       mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()  
               {  
                   public void onCompletion(MediaPlayer mediaPlayer)  
                   {  
                       mVisualizer.setEnabled(false);  
                       getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  
                       setVolumeControlStream(AudioManager.STREAM_SYSTEM);  
                       mStatusTextView.setText("音乐播放完毕");  
                   }  
               });  
 
       getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  
       setVolumeControlStream(AudioManager.STREAM_MUSIC);  
       mMediaPlayer.start();  
       mStatusTextView.setText("播放音乐中....");  
   }  
 
   private void setupEqualizerFxAndUI()  
   {  
       // Create the Equalizer object (an AudioEffect subclass) and attach it  
       // to our media player,  
       // with a default priority (0).  
       mEqualizer = new Equalizer(0, mMediaPlayer.getAudioSessionId());  
       mEqualizer.setEnabled(true);  
 
       TextView eqTextView = new TextView(this);  
       eqTextView.setText("均衡器:");  
       mLinearLayout.addView(eqTextView);  
 
       short bands = mEqualizer.getNumberOfBands();  
 
       final short minEQLevel = mEqualizer.getBandLevelRange()[0];  
       final short maxEQLevel = mEqualizer.getBandLevelRange()[1];  
 
       for (short i = 0; i < bands; i++)  
       {  
           final short band = i;  
 
           TextView freqTextView = new TextView(this);  
           freqTextView.setLayoutParams(new ViewGroup.LayoutParams(  
                   ViewGroup.LayoutParams.FILL_PARENT,  
                   ViewGroup.LayoutParams.WRAP_CONTENT));  
           freqTextView.setGravity(Gravity.CENTER_HORIZONTAL);  
           freqTextView.setText((mEqualizer.getCenterFreq(band) / 1000)  
                   + " Hz");  
           mLinearLayout.addView(freqTextView);  
 
           LinearLayout row = new LinearLayout(this);  
           row.setOrientation(LinearLayout.HORIZONTAL);  
 
           TextView minDbTextView = new TextView(this);  
           minDbTextView.setLayoutParams(new ViewGroup.LayoutParams(  
                   ViewGroup.LayoutParams.WRAP_CONTENT,  
                   ViewGroup.LayoutParams.WRAP_CONTENT));  
           minDbTextView.setText((minEQLevel / 100) + " dB");  
 
           TextView maxDbTextView = new TextView(this);  
           maxDbTextView.setLayoutParams(new ViewGroup.LayoutParams(  
                   ViewGroup.LayoutParams.WRAP_CONTENT,  
                   ViewGroup.LayoutParams.WRAP_CONTENT));  
           maxDbTextView.setText((maxEQLevel / 100) + " dB");  
 
           LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(  
                   ViewGroup.LayoutParams.FILL_PARENT,  
                   ViewGroup.LayoutParams.WRAP_CONTENT);  
           layoutParams.weight = 1;  
           SeekBar bar = new SeekBar(this);  
           bar.setLayoutParams(layoutParams);  
           bar.setMax(maxEQLevel - minEQLevel);  
           bar.setProgress(mEqualizer.getBandLevel(band));  
 
           bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()  
           {  
               public void onProgressChanged(SeekBar seekBar, int progress,  
                       boolean fromUser)  
               {  
                   mEqualizer.setBandLevel(band, (short) (progress + minEQLevel));  
               }  
 
               public void onStartTrackingTouch(SeekBar seekBar)  
               {  
               }  
 
               public void onStopTrackingTouch(SeekBar seekBar)  
               {  
               }  
           });  
 
           row.addView(minDbTextView);  
           row.addView(bar);  
           row.addView(maxDbTextView);  
 
           mLinearLayout.addView(row);  
       }  
   }  
 
   private void setupVisualizerFxAndUI()  
   {  
       mVisualizerView = new VisualizerView(this);  
       mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(  
               ViewGroup.LayoutParams.FILL_PARENT,  
               (int) (VISUALIZER_HEIGHT_DIP * getResources()  
                       .getDisplayMetrics().density)));  
       mLinearLayout.addView(mVisualizerView);  
 
       mInfoView = new TextView(this);  
       String infoStr = "";  
         
       int[] csr = Visualizer.getCaptureSizeRange();  
       if(csr != null)  
       {  
           String csrStr = "CaptureSizeRange: ";  
           for(int i = 0; i < csr.length; i ++)  
           {  
               csrStr += csr[i];  
               csrStr +=" ";  
           }  
           infoStr += csrStr;  
       }  
         
       final int maxCR = Visualizer.getMaxCaptureRate();  
         
       infoStr = infoStr + "\nMaxCaptureRate: " + maxCR;  
         
       mInfoView.setText(infoStr);  
       mLinearLayout.addView(mInfoView);  
         
       mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId()); //使用音乐的sessionId来实例化这个类 
       
       //设置每次捕获频谱的大小，
       //音乐在播放中的时候采集的数据的大小或者说是采集的精度吧，
       //我的理解，而且getCaptureSizeRange()所返回的数组里面就两个值 .文档里说数组[0]是最小值（128），数组[1]是最大值（1024）
       // mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
       mVisualizer.setCaptureSize(256);  
       
       //接下来就好理解了设置一个监听器来监听不断而来的所采集的数据。
       //一共有4个参数，第一个是监听者，第二个单位是毫赫兹，表示的是采集的频率，第三个是是否采集波形，第四个是是否采集频率
       mVisualizer.setDataCaptureListener(  
               new Visualizer.OnDataCaptureListener()  
               {  
                   public void onWaveFormDataCapture(Visualizer visualizer,  
                           byte[] bytes, int samplingRate)  
                   {  
                       mVisualizerView.updateVisualizer(bytes);  
                   }  
 
                   public void onFftDataCapture(Visualizer visualizer,  
                           byte[] fft, int samplingRate)  
                   {  
                       mVisualizerView.updateVisualizer(fft);  
                   }  
               }, maxCR / 2, false, true);  
   }  
 
   @Override  
   protected void onPause()  
   {  
       super.onPause();  
 
       if (isFinishing() && mMediaPlayer != null)  
       {  
           mVisualizer.release();  
           mEqualizer.release();  
           mMediaPlayer.release();  
           mMediaPlayer = null;  
       }  
   }  
     
   /** 
    * A simple class that draws waveform data received from a 
    * {@link Visualizer.OnDataCaptureListener#onWaveFormDataCapture } 
    */  
   class VisualizerView extends View  
   {  
       private byte[] mBytes;  
       private float[] mPoints;  
       private Rect mRect = new Rect();  
 
       private Paint mForePaint = new Paint();  
       //频谱宽度
       private int mSpectrumNum = 48;  
       private boolean mFirst = true;  
 
       public VisualizerView(Context context)  
       {  
           super(context);  
           init();  
       }  
 
       private void init()  
       {  
           mBytes = null;  
 
           mForePaint.setStrokeWidth(8f);  
           mForePaint.setAntiAlias(true);  
           mForePaint.setColor(Color.rgb(0, 128, 255));  
       }  
 
       public void updateVisualizer(byte[] fft)  
       {  
           if(mFirst )  
           {  
               mInfoView.setText(mInfoView.getText().toString() + "\nCaptureSize: " + fft.length);  
               mFirst = false;  
           }  
             
             
           byte[] model = new byte[fft.length / 2 + 1];  
 
           model[0] = (byte) Math.abs(fft[0]);  
           for (int i = 2, j = 1; j < mSpectrumNum;)  
           {  
               model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);  
               i += 2;  
               j++;  
           }  
           mBytes = model;  
           invalidate();  
       }  
 
       @Override  
       protected void onDraw(Canvas canvas)  
       {  
           super.onDraw(canvas);  
           //mBytes就是采集来的数据 这里是个大小为1024的数组，里面的数据都是byts类型，所以大小为-127到128
           if (mBytes == null)  
           {  
               return;  
           }  
 
           if (mPoints == null || mPoints.length < mBytes.length * 4)  
           {  
        	 //mPoints主要用来存储要画直线的4个坐标（每个点两个坐标，所以一条直线需要两个点，也就是4个坐标）
               mPoints = new float[mBytes.length * 4];  
           }  
 
           mRect.set(0, 0, getWidth(), getHeight());  
 
           
           
         //xOrdinate是x轴的总刻度，因为一次会传输过来1024个数据，每两个数据要画成一条直线，所以x轴我们分成1023段。你要是觉的太多了，也可以像我一样除以2，看自己需求了。
          // int xOrdinate = (mBytes.length - 1)/2;
           
           
           //绘制波形  
           // for (int i = 0; i < mBytes.length - 1; i++) {  
           // mPoints[i * 4] = mRect.width() * i / (mBytes.length - 1);  
           // mPoints[i * 4 + 1] = mRect.height() / 2  
           // + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;  
           // mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length - 1);  
           // mPoints[i * 4 + 3] = mRect.height() / 2  
           // + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 128;  
           // }  
             
           //绘制频谱  
           final int baseX = mRect.width()/mSpectrumNum;  
           final int height = mRect.height();  
 
           for (int i = 0; i < mSpectrumNum ; i++)  
           {  
               if (mBytes[i] < 0)  
               {  
                   mBytes[i] = 127;  
               }  
                 
               final int xi = baseX*i + baseX/2;  
                 
               mPoints[i * 4] = xi;  
               mPoints[i * 4 + 1] = height;  
                 
               mPoints[i * 4 + 2] = xi;  
               mPoints[i * 4 + 3] = height - mBytes[i];  
           }  
 
           canvas.drawLines(mPoints, mForePaint);  
       }  
   }  
}  
