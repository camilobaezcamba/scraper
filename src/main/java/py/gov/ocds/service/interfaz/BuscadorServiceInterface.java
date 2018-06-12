package py.gov.ocds.service.interfaz;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

/**
 * Created by diego on 01/05/17.
 * Updated by cbaez on 09/05/18.
 */
public interface BuscadorServiceInterface {

  @GET("datos/api/v2/doc/buscadores/{servicio}")
  Call<String> buscar(@Path(value = "servicio", encoded = true) String servicio, @Header("Authorization") String token, @QueryMap Map<String, String> opciones);
}
