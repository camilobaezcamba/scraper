package py.gov.ocds.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.gov.ocds.service.interfaz.ProveedoresServiceInterface;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by cbaez on 11/05/18.
 */
public class ProveedoresService extends BaseService{

  private static final Logger logger = LoggerFactory.getLogger(ProveedoresService.class);

  private ProveedoresServiceInterface service = retrofit.create(ProveedoresServiceInterface.class);

  private int sleep = 0;

  private AutenticacionService authService = new AutenticacionService();
  private String token = authService.accessToken();

  public String get(String id) {
    String recordPackage = null;
    try {

      Call<String> recordPackageReq = null;
      Integer intentos = 0;
      do {
        if(intentos > 0){
          //Espera por cada reintento
          //logger.error("Reintentando {}. Intento: {}", id, intentos);
          if(getSleep() > 0){
            int reintentarEn = getSleep() + intentos * (100 + (int)(Math.random() * ((1000 - 100) + 1)));
            logger.error("Reintentando {}. En: {} seg.", id, reintentarEn);
            Thread.sleep(reintentarEn);
          }
        }

        intentos++;
        recordPackageReq = service.get(id, token);
        Response res = recordPackageReq.execute();
        if (res.body() != null) {
          recordPackage = res.body().toString();
        } else {
          logger.error(res.code()+"");
          recordPackage = null;
          if(res.code() == 401){
            token = authService.accessToken(true);
            System.out.println("Actualizando token: " + token);
          }
        }

      } while (recordPackage == null && intentos < 10);

      if (intentos >= 10) {
        logger.error("Error al intentar consultar el id {}. Intentos: {}", id, intentos);
      }

      return recordPackage;

    } catch (Exception e) {
      logger.error("Error al consultar el proveedor", e);
    }

    return recordPackage;
  }

  private int getSleep() {
    return sleep;
  }

  public void setSleep(int sleep) {
    this.sleep = sleep;
  }

}
