package com.colorcall.callerscreen.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.widget.VideoView;

import com.colorcall.callerscreen.R;

public class FullScreenVideoView extends VideoView {

    public FullScreenVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
       // setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(VideoView.getDefaultSize(0, widthMeasureSpec), VideoView.getDefaultSize(0, heightMeasureSpec));
    }
   /*public boolean a = true;

    public class a implements MediaPlayer.OnPreparedListener {
        public final *//* synthetic *//* d a;

        public a(FullScreenVideoView fullscreenVideoView, d dVar) {
            this.a = dVar;
        }

        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.setLooping(true);
            d dVar = this.a;
            if (dVar != null) {
                mediaPlayer.setVolume(0.0f, 0.0f);
            }
        }
    }

    public class b implements MediaPlayer.OnInfoListener {
        public final *//* synthetic *//* d a;

        public b(FullScreenVideoView fullscreenVideoView, d dVar) {
            this.a = dVar;
        }

        public boolean onInfo(MediaPlayer mediaPlayer, int i, int i2) {
            if (i != 3) {
                return false;
            }
            d dVar = this.a;
            if (dVar == null) {
                return true;
            }
         //   ThemeView.a aVar = (ThemeView.a) dVar;
            *//*ThemeView.this.mVideoView.*//*setAlpha(1.0f);
           // ThemeView.this.e();
            return true;
        }
    }

    public class c implements MediaPlayer.OnErrorListener {
        public c() {
        }

        public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
            FullScreenVideoView.this.stopPlayback();
            return true;
        }
    }

    public interface d {
    }

    public FullScreenVideoView(Context context) {
        super(context);
    }

    public FullScreenVideoView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        int[] FullscreenVideoView = {R.attr.fullscreen};
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet,FullscreenVideoView);
        this.a = obtainStyledAttributes.getBoolean(0, false);
        obtainStyledAttributes.recycle();
    }

    public void a(String str, d dVar) {
        setVideoPath(str);
        start();
        setOnPreparedListener(new a(this, dVar));
        setOnInfoListener(new b(this, dVar));
        setOnErrorListener(new c());
    }

    public void onMeasure(int i, int i2) {
        if (this.a) {
            setMeasuredDimension(VideoView.getDefaultSize(0, i), VideoView.getDefaultSize(0, i2));
        } else {
            super.onMeasure(i, i2);
        }
    }*/
}
