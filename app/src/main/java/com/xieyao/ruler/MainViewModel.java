package com.xieyao.ruler;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.CompoundButton;

import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.xieyao.ruler.update.FlexibleFlowActivity;
import com.xieyao.ruler.update.ImmediateFlowActivity;
import com.xieyao.ruler.widget.SimpleRulerView;

import java.lang.ref.WeakReference;

public class MainViewModel extends ViewModel {

    public MutableLiveData<Boolean> mIsInches = new MutableLiveData<>();

    private WeakReference<Activity> mAcitivyRef;
    private SimpleRulerView mRulerView;
    private AppCompatButton mImmediateUpdateBtn, mFlexibleUpdateBtn;
    private int mShowUpdateCount = 0;

    public void setup(Activity activity) {
        mAcitivyRef = new WeakReference<>(activity);
        mIsInches.setValue(false);
        mRulerView = activity.findViewById(R.id.ruler_view);
        mRulerView.setCallback(mCallback);
        mImmediateUpdateBtn = activity.findViewById(R.id.btn_update_immediate);
        mFlexibleUpdateBtn = activity.findViewById(R.id.btn_update_flexible);
    }

    public CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                mRulerView.showInches();
            } else {
                mRulerView.showMillimeters();
            }
        }
    };

    public View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_update_immediate: {
                    Intent intent = new Intent(mAcitivyRef.get(), ImmediateFlowActivity.class);
                    mAcitivyRef.get().startActivity(intent);
                    break;
                }
                case R.id.btn_update_flexible: {
                    Intent intent = new Intent(mAcitivyRef.get(), FlexibleFlowActivity.class);
                    mAcitivyRef.get().startActivity(intent);
                    break;
                }
            }
        }
    };

    public SimpleRulerView.Callback mCallback = new SimpleRulerView.Callback() {
        @Override
        public void onShowUpdateButton() {
            if (++mShowUpdateCount >= 4) {
                if (null != mImmediateUpdateBtn && mImmediateUpdateBtn.getVisibility() != View.VISIBLE) {
                    mImmediateUpdateBtn.setVisibility(View.VISIBLE);
                }
                if (null != mFlexibleUpdateBtn && mFlexibleUpdateBtn.getVisibility() != View.VISIBLE) {
                    mFlexibleUpdateBtn.setVisibility(View.VISIBLE);
                }
            }
        }
    };

}
