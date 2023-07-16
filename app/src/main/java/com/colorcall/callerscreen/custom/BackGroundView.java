/*
package com.colorcall.callerscreen.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.colorcall.callerscreen.database.Contact;

import butterknife.BindView;

public class BackGroundView extends RelativeLayout {
    public static final int[] d = {Color.parseColor("#333333"), Color.parseColor("#2F2F41")};
    public static final int[] e = {R.drawable.img_portrait_1, R.drawable.img_portrait_2, R.drawable.img_portrait_3, R.drawable.img_portrait_4, R.drawable.img_portrait_5, R.drawable.img_portrait_6, R.drawable.img_portrait_7, R.drawable.img_portrait_8, R.drawable.img_portrait_9};
    public static final String[] f = {"Tony", "Daisy", "Mike", "Kobe", "Kardashian", "James"};
    public static final String[] g = {"201-8747-5424", "351-1458-9875", "302-7589-1456", "405-7589-7896", "011-5725-7893", "503-8957-9812"};
    public Contact contact;
    public MicroBean b;
    public int c;
    @BindView
    public ViewGroup mContainerImage;
    @BindView
    public ViewGroup mContainerLottie;
    @BindView
    public ImageView mIvGif;
    @BindView
    public ImageView btnAccept;
    @BindView
    public ImageView btnReject;
    @BindView
    public ImageView mIvPortrait;
    @BindView
    public ImageView mIvThumb;
    @BindView
    public LottieAnimationView mLottieMicroAccept;
    @BindView
    public TextView mTvContact;
    @Nullable
    @BindView
    public TextView mTvLoading;
    @BindView
    public TextView mTvMicroAccept;
    @BindView
    public TextView mTvMicroDecline;
    @BindView
    public TextView mTvNumber;
    @BindView
    public FullscreenVideoView mVideoView;

    public class a implements FullscreenVideoView.a {
        public a() {
        }
    }

    public class b extends AnimatorListenerAdapter {
        public b() {
        }

        public void onAnimationStart(Animator animator) {
            super.onAnimationStart(animator);
            ThemeView.this.mIvMicroAccept.setVisibility(4);
            ThemeView.this.mLottieMicroAccept.setVisibility(0);
        }
    }

    public BackGroundView(@NonNull Context context) {
        super(context);
    }

    public BackGroundView(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public BackGroundView(@NonNull Context context, @Nullable AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    private void setMicroAnime(String str) {
        try {
            k1.a(str, "json");
            this.mLottieMicroAccept.a(str, "json");
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public final void a(boolean z) {
        if (z) {
            TransitionManager.beginDelayedTransition(this.mContainerImage, new ChangeBounds().setDuration(500));
            TransitionManager.beginDelayedTransition(this.mContainerLottie, new ChangeBounds().setDuration(500));
        }
        int i = this.mContainerImage.getLayoutDirection() == 0 ? 1 : 0;
        this.mContainerImage.setLayoutDirection(i);
        this.mContainerLottie.setLayoutDirection(i);
    }

    public boolean a() {
        ThemeBean themeBean = this.contact;
        return themeBean != null && ((themeBean instanceof LocalThemeBean) || (new File(this.contact.thumbPath).exists() && new File(this.b.declineMicroPath).exists() && new File(this.b.acceptMicroPath).exists() && new File(this.b.acceptLottiePath).exists()));
    }

    public void b(boolean z) {
        if (!z || a()) {
            ax i = e.i(getContext());
            int[] iArr = e;
            ((zw) i.c().a(Integer.valueOf(iArr[this.c % iArr.length]))).a(this.mIvPortrait);
            TextView textView = this.mTvContact;
            String[] strArr = f;
            textView.setText(strArr[this.c % strArr.length]);
            TextView textView2 = this.mTvNumber;
            String[] strArr2 = g;
            textView2.setText(strArr2[this.c % strArr2.length]);
        }
    }

    public boolean b() {
        ThemeBean themeBean = this.contact;
        return themeBean != null && ((themeBean instanceof LocalThemeBean) || new File(this.contact.videoPath).exists());
    }

    public boolean c() {
        return true;
    }

    @y11(threadMode = ThreadMode.MAIN)
    public synchronized void changeMicroOri(MsgBean msgBean) {
        if (msgBean.msg.equals("CHANGE_MICRO_ORI")) {
            a(true);
        }
    }

    public void d() {
        b(true);
    }

    public final void e() {
        try {
            LottieAnimationView lottieAnimationView = this.mLottieMicroAccept;
            lottieAnimationView.e.c.b.add(new b());
            this.mLottieMicroAccept.setFrame(0);
            this.mLottieMicroAccept.d();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public void f() {
        if (!b()) {
            return;
        }
        if (this.contact instanceof LocalThemeBean) {
            ax i = e.i(getContext());
            if (i != null) {
                zw zwVar = (zw) i.a(GifDrawable.class).a((jf<?>) u7.m);
                zwVar.F = this.contact.videoPath;
                zwVar.I = true;
                zwVar.a(this.mIvGif);
                this.mIvGif.setVisibility(0);
                this.mVideoView.setVisibility(8);
                e();
                return;
            }
            throw null;
        }
        this.mIvGif.setImageDrawable(null);
        this.mIvGif.setVisibility(8);
        this.mVideoView.setVisibility(0);
        FullscreenVideoView fullscreenVideoView = this.mVideoView;
        String str = this.contact.videoPath;
        a aVar = new a();
        fullscreenVideoView.setVideoPath(str);
        fullscreenVideoView.start();
        fullscreenVideoView.setOnPreparedListener(new xx(fullscreenVideoView, aVar));
        fullscreenVideoView.setOnInfoListener(new yx(fullscreenVideoView, aVar));
        fullscreenVideoView.setOnErrorListener(new zx(fullscreenVideoView));
    }

    public void g() {
        this.mIvGif.setImageDrawable(null);
        this.mIvGif.setVisibility(8);
        this.mVideoView.setAlpha(0.0f);
        this.mVideoView.stopPlayback();
        this.mVideoView.setVisibility(8);
        try {
            this.btnAccept.setVisibility(0);
            this.mLottieMicroAccept.setVisibility(4);
            this.mLottieMicroAccept.c();
            this.mLottieMicroAccept.e.c.b.clear();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public int getLottieSize() {
        return e.a(getContext(), (float) this.b.acceptLottieSmallSize);
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        o11.b().b(this);
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        o11.b().c(this);
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.a(this, this);
        this.c = new Random().nextInt(10);
    }

    public void setSimulateIndex(int i) {
        this.c = i;
        int[] iArr = d;
        this.mIvThumb.setBackgroundColor(iArr[i % iArr.length]);
    }

    public void setThemeBean(ThemeBean themeBean) {
        String str;
        this.contact = themeBean;
        this.b = themeBean.microBean;
        this.mIvThumb.setImageDrawable(null);
        this.btnReject.setImageDrawable(null);
        this.btnAccept.setImageDrawable(null);
        int i = 8;
        this.mTvMicroDecline.setVisibility(a() ? 0 : 8);
        this.mTvMicroAccept.setVisibility(a() ? 0 : 8);
        TextView textView = this.mTvLoading;
        if (textView != null) {
            if (!a()) {
                i = 0;
            }
            textView.setVisibility(i);
        }
        if (a()) {
            try {
                str = o70.a((InputStream) new FileInputStream(new File(this.b.acceptLottiePath)));
            } catch (FileNotFoundException e2) {
                e2.printStackTrace();
                str = "";
            }
            setMicroAnime(str);
            post(new dy(this));
            e.i(getContext()).a(this.contact.thumbPath).a(this.mIvThumb);
            e.i(getContext()).a(this.b.declineMicroPath).a(this.btnReject);
            e.i(getContext()).a(this.b.acceptMicroPath).a(this.btnAccept);
        }
        if (xw.b.getInt("themeMicroOri", 0) != this.mContainerImage.getLayoutDirection()) {
            a(false);
        }
    }

}
*/
