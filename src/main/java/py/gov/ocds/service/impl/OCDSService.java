package py.gov.ocds.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.gov.ocds.service.interfaz.OCDSServiceInterface;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by diego on 01/05/17.
 */
public class OCDSService extends BaseService{

  private static final Logger logger = LoggerFactory.getLogger(OCDSService.class);

  private OCDSServiceInterface service = retrofit.create(OCDSServiceInterface.class);

  private int sleep = 0;

  private AutenticacionService authService = new AutenticacionService();
  private String token = authService.accessToken();

  public String recordPackage(String id) {
    String recordPackage = null;
    try {

      Call<String> recordPackageReq = null;
      Integer intentos = 0;
      do {
        if(intentos > 0){
          //Espera por cada reintento
          logger.error("Reintentando {}. Intento: {}", id, intentos);
          if(getSleep() > 0){
            int reintentarEn = getSleep() + intentos * (100 + (int)(Math.random() * ((1000 - 100) + 1)));
            logger.error("Reintentando {}. En: {} seg.", id, reintentarEn);
            Thread.sleep(reintentarEn);
          }
        }

        intentos++;
        recordPackageReq = service.recordPackage(token,id);
        Response res = recordPackageReq.execute();
        //logger.error(res.message());
        if (res.body() != null) {
          recordPackage = res.body().toString();
        } else {
          recordPackage = null;
        }

      } while (recordPackage == null && intentos < 100);

      if (intentos >= 70) {
        logger.error("Error al intentar consultar el id {}. Intentos: {}", id, intentos);
      }

      return recordPackage;

    } catch (Exception e) {
      logger.error("Error al consultar el record package", e);
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
