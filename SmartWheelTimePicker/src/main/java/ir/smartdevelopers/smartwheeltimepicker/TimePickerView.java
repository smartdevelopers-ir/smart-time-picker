package ir.smartdevelopers.smartwheeltimepicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class TimePickerView extends ConstraintLayout {
    private View mTopCoverView,mBottomCoverView;
    private WheelEditText edtHour,edtMinute;
    private WheelRecyclerView mHourRecyclerView,mMinuteRecyclerView,mAmPmRecyclerView;
    private WheelAdapter mHourAdapter,mMinuteAdapter;
    private AmPmAdapter mAmPmAdapter;
    private WheelLayoutManager mHourLayoutManager,mMinuteLayoutManager,mAmPmLayoutManager;
    private OnItemClickListener mMinuteOnItemClickListener;
    private OnItemClickListener mHourOnItemClickListener;
    private OnItemClickListener mAmPmOnItemClickListener;
   private ArrayList<String> mHours;
   private ArrayList<String> mMinutes;
   private boolean canChangeAmPm=true;
   private int mActiveColor;
   private WheelEditText.OnBackListener mOnBackListener;
   private OnTimeChangeListener mOnTimeChangeListener;
   private boolean mustScrollToDefaultPosition=true;
   private boolean mFirstSmoothScroll=true;
    public TimePickerView(@NonNull Context context) {
        this(context,null);
    }

    public TimePickerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TimePickerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }
    private void init(Context context,AttributeSet attrs){
        inflate(context,R.layout.whv_main_layout,this);
        findViews();
        initListeners();

        if (attrs !=null){
            TypedArray typedArray=context.obtainStyledAttributes(attrs,R.styleable.TimePickerView);
            TypedValue typedValue=new TypedValue();
            context.getTheme().resolveAttribute(R.attr.colorPrimary,typedValue,true);
            int defaultActiveColor=typedValue.data;
            mActiveColor=typedArray.getColor(R.styleable.TimePickerView_activeColor, defaultActiveColor);
            mHourRecyclerView.setCenterColor(mActiveColor);
            mMinuteRecyclerView.setCenterColor(mActiveColor);
            mAmPmRecyclerView.setCenterColor(mActiveColor);

            int inactivateColor=typedArray.getColor(R.styleable.TimePickerView_inactivateColor, Color.BLACK);
            mHourRecyclerView.setInactivateColor(inactivateColor);
            mMinuteRecyclerView.setInactivateColor(inactivateColor);
            mAmPmRecyclerView.setInactivateColor(inactivateColor);

            typedArray.recycle();
        }
        initViews();
         mHours=new ArrayList<>();
         mMinutes=new ArrayList<>();
        for (int i=0;i<60;i++){
            if (i>0 && i<=12){
                mHours.add(String.valueOf(i));
            }
            mMinutes.add(String.format(new Locale("en"),"%02d",i));
        }
        mHourAdapter=new WheelAdapter(mHours);
        mMinuteAdapter=new WheelAdapter(mMinutes);
        mAmPmAdapter=new AmPmAdapter(context);
        mHourAdapter.setOnItemClickListener(mHourOnItemClickListener);
        mMinuteAdapter.setOnItemClickListener(mMinuteOnItemClickListener);
        mAmPmAdapter.setOnItemClickListener(mAmPmOnItemClickListener);
        mHourLayoutManager=new WheelLayoutManager(context);
        mMinuteLayoutManager=new WheelLayoutManager(context);
        mAmPmLayoutManager=new WheelLayoutManager(context);
        mHourRecyclerView.setLayoutManager(mHourLayoutManager);
        mMinuteRecyclerView.setLayoutManager(mMinuteLayoutManager);
        mAmPmRecyclerView.setLayoutManager(mAmPmLayoutManager);
        mHourRecyclerView.setAdapter(mHourAdapter);
        mMinuteRecyclerView.setAdapter(mMinuteAdapter);
        mAmPmRecyclerView.setCenterScale(1.6f);
        mAmPmRecyclerView.setFromScale(1f);
        mAmPmRecyclerView.setToScale(1.4f);
        mAmPmRecyclerView.setAdapter(mAmPmAdapter);

        mMinuteLayoutManager.scrollToPosition(Integer.MAX_VALUE/2+20);
        mHourLayoutManager.scrollToPosition(Integer.MAX_VALUE/2-8);



        Runnable runnable=()->{
            if (mustScrollToDefaultPosition){

                if (!Objects.equals(mMinuteRecyclerView.getTag(),"called")){
                    mMinuteRecyclerView.setTag("called");
                    scrollToNumber(mMinuteRecyclerView,mMinuteLayoutManager,"00",mFirstSmoothScroll,mMinutes);

                }
                if (!Objects.equals(mHourRecyclerView.getTag(),"called")) {
                    mHourRecyclerView.setTag("called");
                    scrollToNumber(mHourRecyclerView, mHourLayoutManager, "6", mFirstSmoothScroll, mHours);
                }
            }
        };
        post(()->{
            if (mustScrollToDefaultPosition){
                if (mMinuteRecyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE ||
                        mHourRecyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE ){
                    mMinuteRecyclerView.stopScroll();
                    mHourRecyclerView.stopScroll();
                    post(runnable);
                }else {
                    mMinuteRecyclerView.stopScroll();
                    mHourRecyclerView.stopScroll();
                    runnable.run();
                }
            }
        });


    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle=new Bundle();
        bundle.putParcelable("superState",super.onSaveInstanceState());
        bundle.putBoolean("mustScrollToDefaultPosition",false);
        Time time=getTime();
        bundle.putInt("hour",time.get24FormatHour());
        bundle.putInt("minute",time.minute);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle){
            Bundle bundle= (Bundle) state;
            int hour=bundle.getInt("hour");
            int minute=bundle.getInt("minute");
            mustScrollToDefaultPosition=bundle.getBoolean("mustScrollToDefaultPosition");
            state=bundle.getParcelable("superState");
            setTime(hour,minute,false);
        }
        super.onRestoreInstanceState(state);
    }

    public void reset(boolean smoothScroll){
        mMinuteRecyclerView.stopScroll();
        mHourRecyclerView.stopScroll();
        scrollToNumber(mMinuteRecyclerView,mMinuteLayoutManager,"00",smoothScroll,mMinutes);
        scrollToNumber(mHourRecyclerView, mHourLayoutManager, "6", smoothScroll, mHours);
        goToAm();
    }
    private void findViews() {
        mMinuteRecyclerView=findViewById(R.id.whv_minuteRecyclerView);
        mHourRecyclerView=findViewById(R.id.whv_hourRecyclerView);
        mAmPmRecyclerView=findViewById(R.id.whv_am_pm_recyclerView);
        mTopCoverView=findViewById(R.id.whv_coverTop);
        mBottomCoverView=findViewById(R.id.whv_coverBottom);
        edtHour=findViewById(R.id.whv_edtHour);
        edtMinute=findViewById(R.id.whv_edtMinute);
    }
    private void initListeners() {

        mHourOnItemClickListener=new OnItemClickListener() {
            @Override
            public void onItemClicked(View itemView, int position, String text) {
                View centerView=mHourRecyclerView.getCenterView();
                if (centerView==itemView){
                    showCoversAndEditTexts();

                    edtHour.requestFocus();
                    showKeyboard(edtHour);
                    return;
                }
                scrollToPos(mHourRecyclerView,mHourLayoutManager,centerView,position);

            }
        };
        mMinuteOnItemClickListener=new OnItemClickListener() {
            @Override
            public void onItemClicked(View itemView, int position, String text) {
                View centerView=mMinuteRecyclerView.getCenterView();
                if (centerView==itemView){
                    showCoversAndEditTexts();

                    edtMinute.requestFocus();
                    showKeyboard(edtMinute);
                    return;
                }

                scrollToPos(mMinuteRecyclerView,mMinuteLayoutManager,centerView,position);
            }
        };
        mAmPmOnItemClickListener=new OnItemClickListener() {
            @Override
            public void onItemClicked(View itemView, int position, String text) {
                if (mAmPmRecyclerView.getSnapViewPosition() != position ){
                    if (position == 1){
                        goToAm();
                    }else if (position == 2){
                        goToPm();
                    }
                }
            }
        };
        mOnBackListener=new WheelEditText.OnBackListener() {
            @Override
            public void onBackPressed(WheelEditText editText) {
                saveChanges();
            }
        };
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {


        edtMinute.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){

                    saveChanges();

                    return true;
                }
                return false;
            }
        });
        edtHour.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    if (Objects.equals(edtHour.getText().toString(),"0") ){
                        edtHour.setText("12");
                    }
                }
            }
        });

        edtHour.setOnClickListener(v->{
            edtHour.selectAll();
        });
        edtMinute.setOnClickListener(v->{
            edtMinute.selectAll();
        });

        edtHour.setScaleX(mHourRecyclerView.centerScale);
        edtHour.setScaleY(mHourRecyclerView.centerScale);
        edtMinute.setScaleX(mMinuteRecyclerView.centerScale);
        edtMinute.setScaleY(mMinuteRecyclerView.centerScale);
        edtMinute.setTextColor(mActiveColor);
        edtHour.setTextColor(mActiveColor);
        edtMinute.setOnBackListener(mOnBackListener);
        edtHour.setOnBackListener(mOnBackListener);

        edtMinute.addTextChangedListener(new NumberTextChangeListener(0,59, edtMinute,true));
        edtHour.addTextChangedListener(new NumberTextChangeListener(1,12, edtHour,false));


        mHourRecyclerView.setSnapViewChangeListener(new WheelRecyclerView.SnapViewChangeListener() {
            @Override
            public void onSnapViewChanged(View lastSnapView, View newSnapView) {
                if (lastSnapView !=null && newSnapView !=null){
                    TextView txtLastNumber=lastSnapView.findViewById(R.id.whv_txtWheelChild);
                    TextView txtNewNumber=newSnapView.findViewById(R.id.whv_txtWheelChild);
                    try {
                        int lastNumber=Integer.parseInt(txtLastNumber.getText().toString());
                        int newNumber=Integer.parseInt(txtNewNumber.getText().toString());
                        if ((lastNumber==11 && newNumber == 12) || (lastNumber == 12 && newNumber == 11)){
                            // go to pm
                            manageAmPm();
                        }
                    }catch (Exception ignore){}
                }
                if (mOnTimeChangeListener != null) {
                    mOnTimeChangeListener.onTimeChanged(getTime());
                }
            }
        });
        mMinuteRecyclerView.setSnapViewChangeListener(new WheelRecyclerView.SnapViewChangeListener() {
            @Override
            public void onSnapViewChanged(View lastSnapView, View newSnapView) {
                if (mOnTimeChangeListener != null) {
                    mOnTimeChangeListener.onTimeChanged(getTime());
                }
            }
        });
        mAmPmRecyclerView.setSnapViewChangeListener(new WheelRecyclerView.SnapViewChangeListener() {
            @Override
            public void onSnapViewChanged(View lastSnapView, View newSnapView) {
                if (mOnTimeChangeListener != null) {
                    mOnTimeChangeListener.onTimeChanged(getTime());
                }
            }
        });

        mHourRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    canChangeAmPm=true;
                }
            }
        });


    }

    public void preventFirstSmoothScroll(){
        mFirstSmoothScroll=false;
    }
    public Time getTime(){
        View minuteCenterView=mMinuteRecyclerView.getCenterView();
        View hourCenterView=mHourRecyclerView.getCenterView();
        View amPmCenterView =mAmPmRecyclerView.getCenterView();
        Time time=new Time();
        if (hourCenterView !=null && minuteCenterView !=null && amPmCenterView!=null){
            TextView txtMinute=minuteCenterView.findViewById(R.id.whv_txtWheelChild);
            TextView txtHour=hourCenterView.findViewById(R.id.whv_txtWheelChild);
            if (txtHour !=null && txtMinute !=null){
                try {
                    int minute=Integer.parseInt(txtMinute.getText().toString());
                    int hour=Integer.parseInt(txtHour.getText().toString());
                    int amPmCenterPos=mAmPmLayoutManager.getPosition(amPmCenterView);
                    time.minute=minute;
                    time.hour=hour;
                    time.am_pm = amPmCenterPos == 1 ? Time.AM : Time.PM;

                }catch (Exception ignore){}
            }
        }
        return time;
    }
    /**24 hour format*/
    public void setTime(@IntRange(from = 0,to = 24) int hour,@IntRange(from = 0,to = 59)int minute,boolean smoothScroll){
        int hour24;
        boolean am;
        if (hour==0 || hour==24){
            am=true;
            hour24=12;
        }else if (hour <12){
            am =true;
            hour24=hour;
        }else if (hour == 12){
            am=false;
            hour24=hour;
        }else {
            am=false;
            hour24 = hour - 12;
        }
        setTime(hour24,minute,am,smoothScroll);
    }
    /**12 hour format*/
    public void setTime(@IntRange(from = 1,to = 12) int hour,@IntRange(from = 0,to = 59) int minute, boolean am,boolean smoothScroll){
        mustScrollToDefaultPosition=false;
        String hourSt=String.valueOf(hour);
        String minuteSt=String.format(new Locale("en"),"%02d",minute);
        scrollToNumber(mHourRecyclerView,mHourLayoutManager,hourSt,smoothScroll,mHours);
        scrollToNumber(mMinuteRecyclerView,mMinuteLayoutManager,minuteSt,smoothScroll,mMinutes);
        if (am){
            goToAm();
        }else {
            goToPm();
        }
    }

    /** Save changed numbers by edittext and hide covers and scroll to that numbers*/
    private void saveChanges() {
        hideKeyboard(edtMinute);
        canChangeAmPm=false;
        scrollToEditedNumbers();
        hideCovers();
    }

    private void manageAmPm() {
        if (!canChangeAmPm){
            return;
        }
        if (isAm()){
            goToPm();
        }else {
            goToAm();
        }
    }

    private boolean isAm() {
        View amPmCenterView=mAmPmRecyclerView.getCenterView();
        if (amPmCenterView!=null){
            int centerPos=mAmPmLayoutManager.getPosition(amPmCenterView);
            return centerPos == 1;
        }
        return false;
    }

    private void goToAm() {
        mAmPmLayoutManager.smoothScrollToPos(mAmPmRecyclerView,1);
    }

    private void goToPm() {
        mAmPmLayoutManager.smoothScrollToPos(mAmPmRecyclerView,2);

    }

    /** Scroll to the edited number by user from editTexts*/
    private void scrollToEditedNumbers() {
        String minuteNumber=edtMinute.getText().toString();
        String hourNumber=edtHour.getText().toString();
        View minuteCenterView=mMinuteRecyclerView.getCenterView();
        View hourCenterView=mHourRecyclerView.getCenterView();
        TextView minuteTextView=minuteCenterView.findViewById(R.id.whv_txtWheelChild);
        TextView hourTextView=hourCenterView.findViewById(R.id.whv_txtWheelChild);
        if (!Objects.equals(minuteNumber,minuteTextView.getText().toString())){
            scrollToNumber(mMinuteRecyclerView,mMinuteLayoutManager,minuteNumber,false,mMinutes);
        }
        if (!Objects.equals(hourNumber,hourTextView.getText().toString())){
            scrollToNumber(mHourRecyclerView,mHourLayoutManager,hourNumber,false,mHours);
        }
        if (mOnTimeChangeListener != null) {
            try{
                Time time=new Time();
                int amPmCenterPos=mAmPmRecyclerView.getSnapViewPosition();
                time.am_pm=amPmCenterPos==1 ? Time.AM : Time.PM;
                time.hour=Integer.parseInt(hourNumber);
                time.minute=Integer.parseInt(minuteNumber);
                mOnTimeChangeListener.onTimeChanged(time);
            }catch (Exception e){
                Log.e(getClass().getName(),"Exception while getting time",e);
            }
        }
    }




    private void scrollToPos(WheelRecyclerView recyclerView, WheelLayoutManager layoutManager, View centerView, int pos){
        if (centerView!=null){
            int centerPos=layoutManager.getPosition(centerView);
            if (centerPos > 3){
                layoutManager.smoothScrollToPos(recyclerView,pos );
            }
        }
    }
    /** Scrolls to the position of the given number as text*/
    private void scrollToNumber(WheelRecyclerView recyclerView, WheelLayoutManager layoutManager,
                                String targetNumberText, boolean smoothScroll, ArrayList<String> numbers){
        View centerView=recyclerView.getCenterView();
        if (centerView==null){
            return;
        }
        int centerPos=layoutManager.getPosition(centerView);
        if (centerPos ==-1){
            return;
        }
        TextView centerTextView=centerView.findViewById(R.id.whv_txtWheelChild);
        if (centerTextView==null){
            return;
        }
//        Log.v("TTT","center view text ="+centerTextView.getText().toString());
        try {
            String textViewNumber=centerTextView.getText().toString();
            int centerIndex=numbers.indexOf(textViewNumber);
            int targetIndex=numbers.indexOf(targetNumberText);
            if (targetIndex == -1){
                return;
            }

            int shift=targetIndex - centerIndex;
            if (smoothScroll){
                int targetPos = centerPos + (targetIndex - centerIndex);

                scrollToPos(recyclerView,layoutManager,centerView,targetPos);
            }else {
                if (recyclerView.getAdapter() instanceof WheelAdapter){
                    ((WheelAdapter) recyclerView.getAdapter()).updateList(centerPos-5,shift);
                }
            }




        }catch (Exception ignore){}
    }
    /** When click on numbers the white cover shows up and showing editTexts to edit times*/
    private void showCoversAndEditTexts(){
        mBottomCoverView.setVisibility(View.VISIBLE);
        mTopCoverView.setVisibility(View.VISIBLE);
        edtMinute.setVisibility(View.VISIBLE);
        edtHour.setVisibility(View.VISIBLE);
        View minuteCenterView=mMinuteRecyclerView.getCenterView();
        View hourCenterView = mHourRecyclerView.getCenterView();
        if (minuteCenterView !=null && hourCenterView !=null){
            TextView txtMinute=minuteCenterView.findViewById(R.id.whv_txtWheelChild);
            TextView txtHour=hourCenterView.findViewById(R.id.whv_txtWheelChild);
            edtMinute.setText(txtMinute.getText().toString());
            edtHour.setText(txtHour.getText().toString());
        }
    }

    private void hideCovers(){
        mBottomCoverView.setVisibility(View.GONE);
        mTopCoverView.setVisibility(View.GONE);
        edtMinute.clearFocus();
        edtHour.clearFocus();
        edtMinute.setVisibility(View.GONE);
        edtHour.setVisibility(View.GONE);
    }

    private void showKeyboard(EditText view){
        InputMethodManager inputMethodManager= (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view,InputMethodManager.SHOW_FORCED);
    }
    private  void hideKeyboard(EditText view){
        view.clearFocus();
        InputMethodManager inputMethodManager= (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(getResources().getDimensionPixelSize(R.dimen.whv_main_height),MeasureSpec.EXACTLY));
    }

    public void setOnTimeChangeListener(OnTimeChangeListener onTimeChangeListener) {
        mOnTimeChangeListener = onTimeChangeListener;
    }

    static class NumberTextChangeListener implements TextWatcher{

        private final int startNumber;
        private final int endNumber;
        private final EditText mEditText;
        private final boolean mTwoDigit;

        NumberTextChangeListener(int startNumber, int endNumber, EditText editText, boolean twoDigit) {
            this.startNumber = startNumber;
            this.endNumber = endNumber;
            mEditText = editText;
            mTwoDigit=twoDigit;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s)){
                return;
            }
            String text;
            boolean mustSelectAll=false;
            mEditText.removeTextChangedListener(this);
            try {
                int number=Integer.parseInt(s.toString());
                if (number < startNumber || number > endNumber){
                    text=s.subSequence(s.length()-1,s.length()).toString();
                    if (Integer.parseInt(text) >= 6){
                        mustSelectAll=true;
                    }
                }else {
                    text=s.toString();
                }
                int finalNumber=Integer.parseInt(text);
                if (mTwoDigit){
                    text=String.format(new Locale("en"),"%02d",finalNumber);
                }else {
                    text=String.valueOf(finalNumber);
                }
                mEditText.setText(text);
                if (finalNumber >= 10 || mustSelectAll){
                    mEditText.selectAll();
                }else {
                    mEditText.setSelection(mEditText.length());
                }
            }catch (Exception ignore){}
            mEditText.addTextChangedListener(this);
        }
    }
}
