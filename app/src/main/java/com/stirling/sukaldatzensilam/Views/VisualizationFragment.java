package com.stirling.sukaldatzensilam.Views;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.libRG.CustomTextView;
import com.stirling.sukaldatzensilam.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VisualizationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VisualizationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VisualizationFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private int NUM_OF_COUNT;
    private boolean timelapseRunning = false;
    private float temp;

    private int position = -1;
    int[] imageArray = { R.drawable.vacio, R.drawable.frio, R.drawable.caliente };
    String [] tempArray = {"0ºC", "20ºC", "70ºC"};

    @BindView(R.id.bSetAlarm) TextView bSetTemperatureAlarm;
    @BindView(R.id.temperatureThreshold) TextView temperatureThreshold;
    @BindView(R.id.bSetTime) TextView bSetTimeAlarm;
    @BindView(R.id.alarmLayout)    LinearLayout llAlarm;
    @BindView(R.id.alarmTimeSeekbar) org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
            seekBarTime;
    @BindView(R.id.alarmTemperatureSeekbar) org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
            seekBarTemp;
    @BindView(R.id.timeAlarm)    TextView timeAlarm;
    @BindView(R.id.imgTupper2) ImageView MyImageView;
    @BindView(R.id.temperatureIndicator) CustomTextView tvTemperature;



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VisualizationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VisualizationFragment newInstance(String param1, String param2) {
        VisualizationFragment fragment = new VisualizationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /*@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }*/

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.visualization_fragment, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstancestate){
        super.onViewCreated(view, savedInstancestate);

        ButterKnife.bind(this, view);

        //Limpiar temperatura anterior
        tvTemperature.setText("-- ºC");

        //Set temperature alarm click listener
        bSetTemperatureAlarm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//              currentCazuela.setTemperatureAlarm(seekBarTemp.getProgress());
                temperatureThreshold.setText(Html.fromHtml("<b>Temperature limit: </b>" +
                        seekBarTemp.getProgress() + "ºC"));
            }
        });
        //Set temperature alarm click listener
        bSetTimeAlarm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//                currentCazuela.setTimeAlarm(seekBarTime.getProgress());
                timeAlarm.setText(Html.fromHtml("<b>Time remaining:</b> " +
                        seekBarTime.getProgress() + "min."));
            }
        });

    }
    //Actualiza el color del círculo en el que se muestra la temperatura
    public void actualizarColor(){

        int temp1 = getResources().getInteger(R.integer.tempVerdeMenorQue);
        int temp2 = getResources().getInteger(R.integer.tempAmarillaMayVerMenQue);
        int temp3 = getResources().getInteger(R.integer.tempRojaMayorQue);
        if(temp < temp1){
            tvTemperature.setBackgroundColor(getContext().getColor(R.color.tempVerde));
        }else if(temp1 < temp && temp < temp2){
            tvTemperature.setBackgroundColor(getContext().getColor(R.color.tempAmarillo));
        }else if(temp < temp3){
            tvTemperature.setBackgroundColor(getContext().getColor(R.color.tempRojo));
        }else{
            tvTemperature.setBackgroundColor(getContext().getColor(R.color.material_grey300));
        }
    }

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
