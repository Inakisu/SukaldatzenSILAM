package com.stirling.sukaldatzensilam.Models.Sources;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.stirling.sukaldatzensilam.Models.POJOs.Notificacion;

@IgnoreExtraProperties
public class NotificacionSource {

    @SerializedName("_source")
    @Expose
    private Notificacion notificacion;

    public Notificacion getNotificacion(){ return notificacion;}

    public void setNotificacion(Notificacion notificacion){ this.notificacion = notificacion;}
}
