package com.xieyao.ruler;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.xieyao.ruler.widget.SimpleRulerView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        MainViewModel model = ViewModelProviders.of(this).get(MainViewModel.class);
        model.setup(this);
        binding.setViewModel(model);
    }
}
