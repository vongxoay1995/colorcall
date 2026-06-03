package com.colorcall.callerscreen.paywall;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class AutoScrollRecyclerView extends RecyclerView {

    private static final int SPEED = 50;

    private final UniformSpeedInterpolator interpolator;
    private int speedDx;
    private int speedDy;
    private int currentSpeed = SPEED;
    private boolean loopEnabled;
    private boolean reverse;
    private boolean isOpenAuto;
    private boolean canTouch = true;
    private boolean pointTouch;
    private boolean ready;
    private boolean inflate;
    private boolean stopAutoScroll;

    public AutoScrollRecyclerView(Context context) {
        this(context, null);
    }

    public AutoScrollRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoScrollRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        interpolator = new UniformSpeedInterpolator();
    }

    public void startAutoScroll() {
        stopAutoScroll = false;
        openAutoScroll(currentSpeed, false);
    }

    public void openAutoScroll(int speed, boolean reverse) {
        this.reverse = reverse;
        currentSpeed = speed;
        isOpenAuto = true;
        notifyLayoutManager();
        startScroll();
    }

    public void setCanTouch(boolean canTouch) {
        this.canTouch = canTouch;
    }

    public boolean canTouch() {
        return canTouch;
    }

    public void setLoopEnabled(boolean loopEnabled) {
        this.loopEnabled = loopEnabled;

        if (getAdapter() != null) {
            getAdapter().notifyDataSetChanged();
            startScroll();
        }
    }

    public boolean isLoopEnabled() {
        return loopEnabled;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
        notifyLayoutManager();
        startScroll();
    }

    public void pauseAutoScroll(boolean stopAutoScroll) {
        this.stopAutoScroll = stopAutoScroll;
    }

    public boolean getReverse() {
        return reverse;
    }

    private void startScroll() {
        if (!isOpenAuto || getScrollState() == SCROLL_STATE_SETTLING) {
            return;
        }
        if (inflate && ready) {
            speedDx = 0;
            speedDy = 0;
            smoothScroll();
        }
    }

    private void smoothScroll() {
        if (stopAutoScroll) {
            return;
        }

        int absSpeed = Math.abs(currentSpeed);
        int distance = reverse ? -absSpeed : absSpeed;
        smoothScrollBy(distance, distance, interpolator);
    }

    private void notifyLayoutManager() {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            ((LinearLayoutManager) layoutManager).setReverseLayout(reverse);
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            ((StaggeredGridLayoutManager) layoutManager).setReverseLayout(reverse);
        }
    }

    @Override
    public void swapAdapter(Adapter adapter, boolean removeAndRecycleExistingViews) {
        super.swapAdapter(generateAdapter(adapter), removeAndRecycleExistingViews);
        ready = true;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(generateAdapter(adapter));
        ready = true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!canTouch) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pointTouch = true;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isOpenAuto) {
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!canTouch) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isOpenAuto) {
                    pointTouch = false;
                    smoothScroll();
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        startScroll();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        inflate = true;
    }

    @Override
    public void onScrolled(int dx, int dy) {
        if (pointTouch) {
            speedDx = 0;
            speedDy = 0;
            return;
        }

        if (dx == 0) {
            speedDy += dy;
            if (Math.abs(speedDy) >= Math.abs(currentSpeed)) {
                speedDy = 0;
                smoothScroll();
            }
        } else {
            speedDx += dx;
            if (Math.abs(speedDx) >= Math.abs(currentSpeed)) {
                speedDx = 0;
                smoothScroll();
            }
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private NestingRecyclerViewAdapter generateAdapter(Adapter adapter) {
        return new NestingRecyclerViewAdapter(this, adapter);
    }

    private static class UniformSpeedInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float input) {
            return input;
        }
    }

    private static class NestingRecyclerViewAdapter<VH extends ViewHolder> extends Adapter<VH> {

        private final AutoScrollRecyclerView recyclerView;
        private final Adapter<VH> adapter;

        NestingRecyclerViewAdapter(AutoScrollRecyclerView recyclerView, Adapter<VH> adapter) {
            this.recyclerView = recyclerView;
            this.adapter = adapter;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return adapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void registerAdapterDataObserver(@NonNull AdapterDataObserver observer) {
            super.registerAdapterDataObserver(observer);
            adapter.registerAdapterDataObserver(observer);
        }

        @Override
        public void unregisterAdapterDataObserver(@NonNull AdapterDataObserver observer) {
            super.unregisterAdapterDataObserver(observer);
            adapter.unregisterAdapterDataObserver(observer);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            adapter.onBindViewHolder(holder, generatePosition(position));
        }

        @Override
        public void setHasStableIds(boolean hasStableIds) {
            super.setHasStableIds(hasStableIds);
            adapter.setHasStableIds(hasStableIds);
        }

        @Override
        public int getItemCount() {
            return getLoopEnabled() ? Integer.MAX_VALUE : adapter.getItemCount();
        }

        @Override
        public int getItemViewType(int position) {
            return adapter.getItemViewType(generatePosition(position));
        }

        @Override
        public long getItemId(int position) {
            return adapter.getItemId(generatePosition(position));
        }

        private int generatePosition(int position) {
            return getLoopEnabled() ? getActualPosition(position) : position;
        }

        private int getActualPosition(int position) {
            int itemCount = adapter.getItemCount();
            if (itemCount == 0) {
                return 0;
            }
            return position >= itemCount ? position % itemCount : position;
        }

        private boolean getLoopEnabled() {
            return recyclerView.loopEnabled;
        }
    }
}
