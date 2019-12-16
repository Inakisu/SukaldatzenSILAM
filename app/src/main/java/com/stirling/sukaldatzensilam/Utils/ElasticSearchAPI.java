package com.stirling.sukaldatzensilam.Utils;

import com.stirling.sukaldatzensilam.Models.HitsObjects.HitsObject;
import com.stirling.sukaldatzensilam.Models.HitsObjects.HitsObjectC;
import com.stirling.sukaldatzensilam.Models.HitsObjects.HitsObjectM;
import com.stirling.sukaldatzensilam.Models.POJOs.RespuestaU;
import com.stirling.sukaldatzensilam.Models.gson2pojo.Example;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

//import com.stirling.sukaldatzensilam.Models.gson2pojo.Example;

public interface ElasticSearchAPI {

    //Llamada para buscar usuario. Headermap para autenticacion y body para query json
    @POST("usuarios_sukaldatzen/_search")
    Call<HitsObject> searchUsuario(@HeaderMap Map<String, String> headers,
                                   @Body RequestBody params);

    //Llamada para introducir un usuario nuevo en la base de datos
    @POST("/usuarios_sukaldatzen/_doc")
    Call<RespuestaU> postUserReg(@HeaderMap Map<String, String> headers,
                                 @Body RequestBody params);

    //Prueba: LLamada para eliminar la entrada de un usuario
    //usado a la hora de actualizar la ubicación de un usuario
    @POST("/usuarios_sukaldatzen/_delete_by_query")
    Call<RequestBody> deleteUserByQuery(@HeaderMap Map<String, String> headers,
                                        @Body RequestBody params);

    //Llamada para obtener información sobre una cazuela
    @POST("/cazuelas_sukaldatzen/_search")
    Call<HitsObjectC> searchCazuela(@HeaderMap Map<String, String> headers,
                                    @Body RequestBody params);

    //Llamada para introducir una cazuela nueva en la base de datos
    @POST("/cazuelas_sukaldatzen/_doc")
    Call<RequestBody> postCazuela();

    //Llamada para obtener información acerca de una medición. En hits
    @POST("/mediciones_sukaldatzen/_search")
    Call<HitsObjectM> searchMedicion(@HeaderMap Map<String, String> headers,
                                     @Body RequestBody params);

    //Llamada para obtener información acerca de una medición. Con aggregations
    @POST("mediciones_sukaldatzen/_search?filter_path=aggregations.myAgg.hits.hits._source*")
    Call<Example> searchHitsAgg(@HeaderMap Map<String, String> headers,
                                @Body RequestBody params);

    //Llamada para introducir una medición nueva en la base de datos
    @POST("/mediciones_sukaldatzen/_doc")
    Call<RequestBody> postMedicion();
}
