package com.stirling.sukaldatzensilam.Models.Sources;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.stirling.sukaldatzensilam.Models.POJOs.Usuario;

@IgnoreExtraProperties
public class UsuarioSource {

    @SerializedName("_source")
    @Expose
    private Usuario usuario;

    public Usuario getUsuario() { return usuario;}

    public void setUsuario(Usuario usuario){ this.usuario = usuario;}
}
