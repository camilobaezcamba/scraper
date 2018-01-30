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
import ch.qos.logback.classic.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Scraper {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Scraper.class);
  static Logger root = (Logger) LoggerFactory.getLogger("org.mongodb.driver");
  static {
    root.setLevel(ch.qos.logback.classic.Level.ERROR);
  }
  static List<Thread> hilos = new ArrayList<>();

  static boolean asynkMode = true;
  static int sleep = 0;
  static int sleepReintento = 3000;
  static ExecutorService es;
  static String cantidadLicitaciones = "10";

  public void asynkSaveRecord(int finalI, JSONArray procesos, Dao dao, OCDSService ocds) throws InterruptedException {
    Thread hilo = new Thread(() -> saveRecord(finalI, procesos, dao, ocds));
    hilos.add(hilo);
  }

  public void normalSaveRecord(int finalI, JSONArray procesos, Dao dao, OCDSService ocds) throws InterruptedException {
    saveRecord(finalI, procesos, dao, ocds);
    if(sleep > 0)
      Thread.sleep(sleep);
  }

  public void saveRecord(int finalI, JSONArray procesos, Dao dao, OCDSService ocds){
      Long id_llamado = procesos.getJSONObject(finalI).getLong("id_llamado");
    String record = ocds.recordPackage(id_llamado.toString());
      if(record == null){
        logger.error("No se pudo recuperar el registro {}", id_llamado.toString());
        return;
      }
      JSONObject recordJson = new JSONObject(record);
      JSONArray recordsJson = recordJson.getJSONArray("records");
      boolean guardar = false;
      JSONObject compiledRelease = null;
      for (int j=0; j<recordsJson.length(); j++) {
        JSONObject item = recordsJson.getJSONObject(j);
        compiledRelease = item.getJSONObject("compiledRelease");
        if(compiledRelease != null){
          guardar = true;
        }
      }
      if (guardar) {
        logger.debug("Datos guardados: {}", id_llamado);
        dao.guardar(id_llamado.toString(), compiledRelease.toString());
      }
  }

  public void scrap() throws InterruptedException {

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
            .put("limit", cantidadLicitaciones));

    ocds.setSleep(sleepReintento);
    long inicio = System.currentTimeMillis();
    if(asynkMode){
      for (int i = 0; i < procesos.length(); i++) {
        asynkSaveRecord(i, procesos, dao, ocds);
      }
    } else{
      for (int i = 0; i < procesos.length(); i++) {
        normalSaveRecord(i, procesos, dao, ocds);
      }
    }
    long fin = System.currentTimeMillis();;
    if(asynkMode){
      int cant = 10;
      int start = 0;
      int end = cant;

      while(hilos.size() >= end){
        System.out.println("probando del " + start + " al " + end);
        List<Thread> subLista = hilos.subList(start, end);
        for(int i = 0; subLista.size() > i; i++) {
          subLista.get(i).start();
        }

        for(int i = 0; subLista.size() > i; i++) {
          subLista.get(i).join();
        }

        start += cant;
        end += cant;
      }


      fin = System.currentTimeMillis();
    }

    System.out.println("Tiempo de ejecución: " + (fin - inicio) / 1000 + " seg.");
  }
}





