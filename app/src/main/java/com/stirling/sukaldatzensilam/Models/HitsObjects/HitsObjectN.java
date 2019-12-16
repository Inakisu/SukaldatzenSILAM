package com.stirling.sukaldatzensilam.Models.HitsObjects;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.stirling.sukaldatzensilam.Models.HitsLists.HitsListN;

@IgnoreExtraProperties
public class HitsObjectN {

    @SerializedName("_source")
    @Expose
    private HitsListN hits;

    public HitsListN getHits() { return hits;}

    public void setHits(HitsListN hits){ this.hits = hits; }
}
