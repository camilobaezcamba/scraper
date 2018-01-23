package py.gov.ocds.scraper; /**
 * Created by diego on 03/04/17.
 */

import org.json.JSONArray;

import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import py.gov.ocds.dao.interfaz.Dao;
import py.gov.ocds.dao.impl.ScraperDao;
import py.gov.ocds.service.impl.LicitacionesService;
import py.gov.ocds.service.impl.OCDSService;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
public class Scraper {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Scraper.class);
  static Logger root = (Logger) LoggerFactory.getLogger("org.mongodb.driver");
  static {
    root.setLevel(ch.qos.logback.classic.Level.ERROR);
  }

  public static void main(String[] args) throws InterruptedException {

    LicitacionesService licitaciones = new LicitacionesService();
    OCDSService ocds = new OCDSService();
    Dao dao = new ScraperDao();

    logger.warn("Recuperando licitaciones");
    JSONArray procesos = licitaciones.recuperarLicitaciones(Parametros.builder()
            .put("fecha_desde", "2016-01-01")
            .put("fecha_hasta", "2016-01-31")
            .put("tipo_fecha", "ENT")
            .put("tipo_licitacion", "tradicional")
            .put("offset", "0")
            .put("limit", "100"));

    for (int i = 0; i < procesos.length(); i++) {

      int finalI = i;
      Thread.sleep(500);
      new Thread(() -> {
        //Do whatever
        Long id_llamado = procesos.getJSONObject(finalI).getLong("id_llamado");
        String record = ocds.recordPackage(id_llamado.toString());
        JSONObject recordJson = new JSONObject(record);
        JSONArray recordsJson = recordJson.getJSONArray("records");
        boolean guardar = false;
        for (int j=0; j<recordsJson.length(); j++) {
          JSONObject item = recordsJson.getJSONObject(j);
          JSONObject compiledRelease = item.getJSONObject("compiledRelease");
          if(compiledRelease != null){
            guardar = true;
          }
        }
        if (guardar) {
          logger.debug("Guardando datos de {}", id_llamado);
          dao.guardar(id_llamado.toString(), record);
        } else {

          logger.error("No se pudo recuperar el registro {}", id_llamado.toString());
        }
      }).start();
    }
  }
}





