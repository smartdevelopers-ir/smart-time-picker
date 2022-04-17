package ir.smartdevelopers.smartwheeltimepicker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

public class WheelEditText extends AppCompatEditText {
    private OnBackListener mOnBackListener;
    public WheelEditText(@NonNull Context context) {
        super(context);
    }

    public WheelEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WheelEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            if (mOnBackListener != null) {
                mOnBackListener.onBackPressed(this);
                return true;
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setOnBackListener(OnBackListener onBackListener) {
        mOnBackListener = onBackListener;
    }

    interface OnBackListener{
        void onBackPressed(WheelEditText editText);
    }
}
