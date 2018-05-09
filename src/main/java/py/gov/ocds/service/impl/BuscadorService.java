package py.gov.ocds.service.impl;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.gov.ocds.scraper.Parametros;
import py.gov.ocds.service.interfaz.BuscadorServiceInterface;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by diego on 01/05/17.
 */
public class BuscadorService extends BaseService{

  private static final Logger logger = LoggerFactory.getLogger(BuscadorService.class);

  BuscadorServiceInterface service = retrofit.create(BuscadorServiceInterface.class);

  private AutenticacionService authService = new AutenticacionService();
  private String token = authService.accessToken();

  public JSONArray recuperar(String servicio, Parametros criterios) {

    try {

      Call<String> resultados = service.buscar(servicio, token, criterios.build());

      Response res = resultados.execute();

      if (res.body() != null) {
        return listarResultados(res.body().toString());
      }

    } catch(Exception e) {
      logger.error("Error al consultar el servicio de " + servicio);
    }
    return null;
  }

  public JSONArray listarResultados(String jsonld)
  {
    JSONObject obj = new JSONObject(jsonld);

    JSONArray graph = obj.getJSONArray("@graph");
    JSONObject list = graph.getJSONObject(0);
    JSONArray licitaciones = list.getJSONArray("list");

    return licitaciones;
  }
}
