package com.xieyao.ruler;

import android.widget.CompoundButton;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.xieyao.ruler.widget.SimpleRulerView;

public class MainViewModel extends ViewModel {

    public MutableLiveData<Boolean> mIsInches = new MutableLiveData<>();
    private SimpleRulerView mRulerView;

    public void setup(SimpleRulerView rulerView) {
        mIsInches.setValue(false);
        mRulerView = rulerView;
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

}
