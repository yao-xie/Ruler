package com.xieyao.ruler.adapter;

import android.widget.CompoundButton;

import androidx.databinding.BindingAdapter;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class BindAdapter {

    @BindingAdapter("app:onCheckedChangeListener")
    public static void setOnCheckedChangeListener(SwitchMaterial view, CompoundButton.OnCheckedChangeListener listener) {
        view.setOnCheckedChangeListener(listener);
    }

}
