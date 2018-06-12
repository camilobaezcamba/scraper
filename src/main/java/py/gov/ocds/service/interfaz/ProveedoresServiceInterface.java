package py.gov.ocds.service.interfaz;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import java.util.Map;

/**
 * Created by cbaez on 09/05/18.
 */
public interface ProveedoresServiceInterface {

  @GET("datos/api/v2/doc/proveedores/{id}")
  Call<String> get(@Path("id") String id, @Header("Authorization") String token);
}
