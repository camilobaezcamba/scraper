package py.gov.ocds.service.impl;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.gov.ocds.aplicacion.Aplicacion;
import py.gov.ocds.service.interfaz.AutenticacionServiceInterface;
import retrofit2.Call;

import java.io.IOException;

/**
 * Created by diego on 01/05/17.
 */
public class AutenticacionService extends BaseService {

  private static final Logger logger = LoggerFactory.getLogger(AutenticacionService.class);

  private AutenticacionServiceInterface service = retrofit.create(AutenticacionServiceInterface.class);

  private String accessToken = null;

  public String accessToken() {
    return accessToken(false);
  }

  public String accessToken(boolean forzar) {
    if(accessToken != null && !forzar){
      return accessToken;
    }
    try {

      Call<String> autenticacion = service.accesToken("Basic " + Aplicacion.REQUEST_TOKEN);
      JSONObject token = new JSONObject(autenticacion.execute().body());
      System.out.println(this);
      accessToken = "Bearer " + token.getString("access_token");
      return accessToken;

    } catch (IOException e) {
      logger.error("Ocurrio un error en el servicio de autorizacion",e);
    }
    return null;
  }


}
