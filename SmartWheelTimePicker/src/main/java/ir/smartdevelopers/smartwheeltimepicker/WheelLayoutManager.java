package ir.smartdevelopers.smartwheeltimepicker;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

public class WheelLayoutManager extends LinearLayoutManager {
    private OnLayoutCompleteListener mOnLayoutCompleteListener;
    public WheelLayoutManager(Context context) {
        super(context);
    }

    public WheelLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public WheelLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void scrollToPosition(int position) {
        super.scrollToPosition(position);

    }

    public void smoothScrollToPos(RecyclerView recyclerView, int pos){
        CenterScroller scroller=new CenterScroller(recyclerView.getContext());
        scroller.setTargetPosition(pos);
        startSmoothScroll(scroller);
    }
    public void scrollImmediateToPos(RecyclerView recyclerView, int pos){
        FastScroller scroller=new FastScroller(recyclerView.getContext());
        scroller.setTargetPosition(pos);
        startSmoothScroll(scroller);

    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        if (mOnLayoutCompleteListener != null) {
            mOnLayoutCompleteListener.onLayoutCompleted();
        }
    }

    public void setOnLayoutCompleteListener(OnLayoutCompleteListener onLayoutCompleteListener) {
        mOnLayoutCompleteListener = onLayoutCompleteListener;
    }

    static class FastScroller extends LinearSmoothScroller {
        public FastScroller(Context context) {
            super(context);
        }
        @Override
        protected int calculateTimeForScrolling(int dx) {
            return 0;
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
        }

        @Override
        protected int calculateTimeForDeceleration(int dx) {
            return 1;
        }


    }

    static class CenterScroller extends LinearSmoothScroller {
        public CenterScroller(Context context) {
            super(context);
        }

        @Override
        protected int calculateTimeForScrolling(int dx) {
            return 120;
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
        }

        @Override
        protected int calculateTimeForDeceleration(int dx) {
            return super.calculateTimeForDeceleration(dx);
        }
    }
    public interface OnLayoutCompleteListener{
        void onLayoutCompleted();
    }
}
