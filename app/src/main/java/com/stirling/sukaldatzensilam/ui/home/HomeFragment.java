package com.stirling.sukaldatzensilam.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.libRG.CustomTextView;
import com.stirling.sukaldatzensilam.R;

import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {

    private int position = -1;
    private HomeViewModel homeViewModel;
    ImageView MyImageView;
    CustomTextView mostrarTemp;
    int[] imageArray = { R.drawable.vacio, R.drawable.frio, R.drawable.caliente };
    String [] tempArray = {"0ºC", "20ºC", "70ºC"};


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        MyImageView = (ImageView) root.findViewById(R.id.imgTupper2);
        mostrarTemp = (CustomTextView) root.findViewById(R.id.temperatureIndicator);

        cambiarImagen();
        return root;
    }

    public void cambiarImagen(){
        /**
         * This timer will call each of the seconds.
         */
        Timer mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // As timer is not a Main/UI thread need to do all UI task on runOnUiThread
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // increase your position so new image will show
                        position++;
                        // check whether position increased to length then set it to 0
                        // so it will show images in circuler
                        if (position >= imageArray.length)
                            position = 0;
                        // Set Image
                        MyImageView.setImageResource(imageArray[position]);
                        mostrarTemp.setText(tempArray[position]);
                    }
                });
            }
        }, 0, 3000);
// where 0 is for start now and 3000 (3 second) is for interval to change image
    }
}