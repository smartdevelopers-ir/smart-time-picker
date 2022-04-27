package ir.smartdevelopers.smartwheeltimepicker;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class WheelRecyclerView extends RecyclerView {
    private LinearSnapHelper mSnapHelper;
    float fromScale=0.6f;
    float toScale=1.4f;
    float centerScale=1.8f;
    private View lastSnapView,mSnapView;
    private SnapViewChangeListener mSnapViewChangeListener;
    private int mCenterColor,mInactivateColor;
    public WheelRecyclerView(@NonNull Context context) {
        this(context,null);
    }

    public WheelRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public WheelRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mSnapHelper=new LinearSnapHelper();
        mSnapHelper.attachToRecyclerView(this);

        mCenterColor=Color.BLUE;
        mInactivateColor=Color.BLACK;
        setOverScrollMode(OVER_SCROLL_NEVER);

    }

    View getCenterView(){
        return mSnapView;
    }
    int getSnapViewPosition(){
        if (mSnapView !=null && getLayoutManager() != null){
            return getLayoutManager().getPosition(mSnapView);
        }
        return -1;
    }
    public void setFromScale(float fromScale) {
        this.fromScale = fromScale;
    }

    public void setToScale(float toScale) {
        this.toScale = toScale;
    }

    public void setCenterScale(float centerScale) {
        this.centerScale = centerScale;
    }

    public float getCenterScale() {
        return centerScale;
    }

    public void setCenterColor(int centerColor) {
        mCenterColor = centerColor;
    }

    public void setInactivateColor(int inactivateColor) {
        mInactivateColor = inactivateColor;
    }

    public void setSnapViewChangeListener(SnapViewChangeListener snapViewChangeListener) {
        mSnapViewChangeListener = snapViewChangeListener;
    }

    @Override
    public void onScrolled(int dx, int dy) {
        if (!(getLayoutManager() instanceof LinearLayoutManager)){
            return;
        }
        LinearLayoutManager layoutManager= (LinearLayoutManager) getLayoutManager();

        int firstViewPos=layoutManager.findFirstVisibleItemPosition();
        int lastViewPos=layoutManager.findLastVisibleItemPosition();
        View snapView=mSnapHelper.findSnapView(layoutManager);
        int space=20;
        if (snapView!=null){
            mSnapView=snapView;
            space=snapView.getMeasuredHeight()/2;
            if (mSnapViewChangeListener != null && lastSnapView != snapView) {
                mSnapViewChangeListener.onSnapViewChanged(lastSnapView,snapView);
            }
            lastSnapView=snapView;
        }
        int lineUp=getMeasuredHeight()/2-space;
        int lineDown=getMeasuredHeight()/2+space;
        int d=lineUp;

        for (int i=firstViewPos;i<=lastViewPos;i++){
            if (i!=-1){
                View view=layoutManager.findViewByPosition(i);
                if (view!=null){
                    int viewCenterPivot=view.getTop()+(view.getMeasuredHeight()/2);
                    int d_y=0;
                    float scale;
                    if (viewCenterPivot < lineUp){//going up
                        d_y=Math.abs(viewCenterPivot-lineUp);
                        scale=((toScale*(d-d_y))+(d_y*fromScale))/d;
                    }else if (viewCenterPivot > lineDown){//going down
                        d_y=Math.abs(viewCenterPivot-lineDown);
                        scale=((toScale*(d-d_y))+(d_y*fromScale))/d;
                    }else {
                        scale=centerScale;
                    }
                    view.setScaleY(scale);
                    view.setScaleX(scale);
                    TextView txtNumber=view.findViewById(R.id.whv_txtWheelChild);
                    if (txtNumber!=null){
                        if (snapView==view){
                            txtNumber.setTextColor(mCenterColor);
//                                    txtNumber.setTypeface(txtNumber.getTypeface(), Typeface.BOLD);
                        }else {
                            txtNumber.setTextColor(mInactivateColor);
//                                    txtNumber.setTypeface(txtNumber.getTypeface(), Typeface.NORMAL);

                        }
                    }
                }
            }
        }

    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
    }

    public interface SnapViewChangeListener{
        void onSnapViewChanged(View lastSnapView,View newSnapView);
    }
}
