package com.stirling.sukaldatzensilam.Utils;

import com.stirling.sukaldatzensilam.Models.POJOs.RespuestaU;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface ElasticSearchAPI {

/*    //Llamada para buscar usuario. Headermap para autenticacion y body para query json
    @POST("silam_usuarios/_search")
    Call<HitsObject> searchUsuario(@HeaderMap Map<String, String> headers,
                                   @Body RequestBody params);*/

    //Llamada para introducir un usuario nuevo en la base de datos
    @POST("/silam_usuarios/_doc")
    Call<RespuestaU> postUserReg(@HeaderMap Map<String, String> headers,
                                 @Body RequestBody params);

    //Llamada para introducir una cazuela nueva en la base de datos
    @POST("/silam_dispositivos/_doc")
    Call<RespuestaU> postTupper(@HeaderMap Map<String, String> headers,
                                  @Body RequestBody params);

}
