package com.stirling.sukaldatzensilam.Models.HitsLists;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.stirling.sukaldatzensilam.Models.Sources.MedicionSource;

import java.util.List;

@IgnoreExtraProperties
public class HitsListM {

    @SerializedName("hits")
    @Expose
    private List<MedicionSource> medicionIndex;

    public List<MedicionSource> getMedicionIndex(){ return medicionIndex;}

    public void setMedicionIndex (List<MedicionSource> medicionIndex) {
        this.medicionIndex = medicionIndex;
    }
}
