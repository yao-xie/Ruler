package com.xieyao.ruler.adapter;

import android.widget.CompoundButton;

import androidx.databinding.BindingAdapter;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.xieyao.ruler.widget.SimpleRulerView;

public class BindAdapter {

    @BindingAdapter("app:onCheckedChangeListener")
    public static void setOnCheckedChangeListener(SwitchMaterial view, CompoundButton.OnCheckedChangeListener listener) {
        view.setOnCheckedChangeListener(listener);
    }

    @BindingAdapter("app:rulerCallback")
    public static void setOnCheckedChangeListener(SimpleRulerView view, SimpleRulerView.Callback callback) {
        view.setCallback(callback);
    }

}
