package py.gov.ocds.scraper;

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

/**
 * Created by diego on 03/04/17.
 */
public class Scraper {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Scraper.class);
  private static Logger root = (Logger) LoggerFactory.getLogger("org.mongodb.driver");
  static {
    root.setLevel(ch.qos.logback.classic.Level.ERROR);
  }
  private static List<Thread> hilos = new ArrayList<>();

  private static final boolean asynkMode = true;
  private static final int sleep = 0;
  private static final int sleepReintento = 3000;
  private static ExecutorService es;
  private static final String cantidadLicitaciones = "100";
  private static final String fechaDesde = "2016-01-01";
  private static final String fechaHasta = "2016-01-31";

  /**
   * Método asincrono para obtener un compiledRelease. Solamente se crea el hilo y es agregado al listado de hilos, deben ser iniciados manualmente
   * @param finalI
   * @param procesos
   * @param dao
   * @param ocds
   * @throws InterruptedException
   */
  private void asynkSaveRecord(int finalI, JSONArray procesos, Dao dao, OCDSService ocds) throws InterruptedException {
    Thread hilo = new Thread(() -> saveRecord(finalI, procesos, dao, ocds));
    hilos.add(hilo);
  }

  /**
   * Metodo sincrono para obtener un compiledRelease.
   * @param finalI
   * @param procesos
   * @param dao
   * @param ocds
   * @throws InterruptedException
   */
  private void normalSaveRecord(int finalI, JSONArray procesos, Dao dao, OCDSService ocds) throws InterruptedException {
    saveRecord(finalI, procesos, dao, ocds);
    if(sleep > 0)
      Thread.sleep(sleep);
  }

  //Obtiene un compiledRelease y lo almacen en la base de datos
  private void saveRecord(int finalI, JSONArray procesos, Dao dao, OCDSService ocds){
      Long id_llamado = procesos.getJSONObject(finalI).getLong("id_llamado");
      saveRecordOne(id_llamado.toString(), dao, ocds);
  }

  private void saveRecordOne(String id_llamado, Dao dao, OCDSService ocds){
    String record = ocds.recordPackage(id_llamado);
    if(record == null){
      logger.error("No se pudo recuperar el registro {}", id_llamado);
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
    if (guardar && compiledRelease != null) {
      logger.debug("Datos guardados: {}", id_llamado);
      dao.guardar(id_llamado, compiledRelease.toString());
    }
  }

  public void scrap(String idLlamado) throws InterruptedException {

    LicitacionesService licitaciones = new LicitacionesService();
    OCDSService ocds = new OCDSService();
    Dao dao = new ScraperDao();

    if(idLlamado != null){
      logger.warn("Recuperando recordPackage: " + idLlamado);
      saveRecordOne(idLlamado, dao, ocds);
    }else{
      logger.warn("Recuperando licitaciones");
      JSONArray procesos = licitaciones.recuperarLicitaciones(Parametros.builder()
              .put("fecha_desde", fechaDesde)
              .put("fecha_hasta", fechaHasta)
              .put("tipo_fecha", "ENT")
              .put("tipo_licitacion", "tradicional")
              .put("offset", "0")
              .put("show_pagination", "false")
              .put("limit", cantidadLicitaciones));

      ocds.setSleep(sleepReintento);
      long inicio = System.currentTimeMillis();
      long fin;
      logger.warn("Recuperadas: " + procesos.length());

      if(asynkMode){
        for (int i = 0; i < procesos.length(); i++) {
          asynkSaveRecord(i, procesos, dao, ocds);
        }
        int cant = 100;
        int start = 0;
        int end = cant;

        //Lanzar 'cant' hilos y esperar la respuesta de todos para volver a lanzar los siguientes
        while(hilos.size() >= end){
          System.out.println("probando del " + (start + 1) + " al " + end);
          List<Thread> subLista = hilos.subList(start, end);

          //Se lanzan los hilos
          for (Thread aSubLista : subLista) {
            aSubLista.start();
          }

          //Se espera la finalizacion de los hilos
          for (Thread aSubLista : subLista) {
            aSubLista.join();
          }

          start += cant;
          end += cant;
        }
        fin = System.currentTimeMillis();;
      } else{
        for (int i = 0; i < procesos.length(); i++) {
          normalSaveRecord(i, procesos, dao, ocds);
        }
        fin = System.currentTimeMillis();;
      }
      System.out.println("Tiempo de ejecución: " + (fin - inicio) / 1000 + " seg.");
    }
  }

  public void scrap() throws InterruptedException {
    scrap(null);
  }
}





