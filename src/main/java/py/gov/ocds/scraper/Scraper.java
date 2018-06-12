package py.gov.ocds.scraper;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import py.gov.ocds.dao.impl.CompiledReleaseDao;
import py.gov.ocds.service.impl.BuscadorService;
import py.gov.ocds.service.impl.OCDSService;
import ch.qos.logback.classic.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by diego on 03/04/17.
 */
public class Scraper {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Scraper.class);
  private static List<Thread> hilos = new ArrayList<>();

  private static final boolean asynkMode = true;
  private static final int sleep = 0;
  private static final int sleepReintento = 500;
  private static ExecutorService es;
  private static int cantidadLicitaciones = 1000; // por pagina
  private static int cantidadProveedores = 1000; // por pagina
  private static final int totalLicitaciones = 12508; // en total "12508";
  private static final String fechaDesde = "2016-01-01";
  private static final String fechaHasta = "2016-12-31";
private int countExists = 0;
  private static List<String> invalidos =  Arrays.asList(
          "301272", "302292", "317774", "318319", "314426", "305995", "306178",
          "307357", "311027", "311076", "311173", "316864", // 500
          "321272", "304990", "307649", // sin compiledRelease
          "316205"); //504

  /**
   * Método asincrono para obtener un compiledRelease. Solamente se crea el hilo y es agregado al listado de hilos, deben ser iniciados manualmente
   * @param finalI
   * @param procesos
   * @param dao
   * @param ocds
   * @throws InterruptedException
   */
  private void asynkSaveRecord(int finalI, JSONArray procesos, CompiledReleaseDao dao, OCDSService ocds) throws InterruptedException {
    Long id_llamado = procesos.getJSONObject(finalI).getLong("id_llamado");
    Document doc = dao.get(id_llamado.toString());
    if(doc != null){
      System.out.println(id_llamado + " ya existe en la BD");
      countExists++;
      return;
    }

    if(invalidos.contains(id_llamado.toString())){
      return;
    }

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
  private void normalSaveRecord(int finalI, JSONArray procesos, CompiledReleaseDao dao, OCDSService ocds) throws InterruptedException {
    Long id_llamado = procesos.getJSONObject(finalI).getLong("id_llamado");
    Document doc = dao.get(id_llamado.toString());
    if(doc != null){
      System.out.println(id_llamado + " ya existe en la BD");
      return;
    }

    saveRecord(finalI, procesos, dao, ocds);
    if(sleep > 0)
      Thread.sleep(sleep);
  }

  //Obtiene un compiledRelease y lo almacen en la base de datos
  private void saveRecord(int finalI, JSONArray procesos, CompiledReleaseDao dao, OCDSService ocds){
      Long id_llamado = procesos.getJSONObject(finalI).getLong("id_llamado");
      saveRecordOne(id_llamado.toString(), dao, ocds);
  }

  private void saveRecordOne(String id_llamado, CompiledReleaseDao dao, OCDSService ocds){
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
    OCDSService ocds = new OCDSService();
    CompiledReleaseDao dao = new CompiledReleaseDao();

    if(idLlamado != null){
      logger.warn("Recuperando recordPackage: " + idLlamado);
      saveRecordOne(idLlamado, dao, ocds);
    }else{
      JSONArray procesos = buscarPaginado("licitaciones", Parametros.builder()
                      .put("fecha_desde", fechaDesde)
                      .put("fecha_hasta", fechaHasta)
                      .put("tipo_fecha", "ENT")
                      .put("tipo_licitacion", "tradicional")
                      .put("offset", "0")
                      .put("limit", String.valueOf(cantidadLicitaciones))
              , cantidadLicitaciones, totalLicitaciones
      );

      ocds.setSleep(sleepReintento);
      long inicio = System.currentTimeMillis();
      long fin;

      if(asynkMode){
        for (int i = 0; i < procesos.length(); i++) {
          asynkSaveRecord(i, procesos, dao, ocds);
        }
        int cant = 100;
        int start = 0;
        int end = cant;

        if(hilos.size() > 0 && hilos.size() < end){
          end = hilos.size();
        }
        //Lanzar 'cant' hilos y esperar la respuesta de todos para volver a lanzar los siguientes
        while(hilos.size() >= end){
          System.out.println("probando del " + (start + 1) + " al " + end + " de " + hilos.size());
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
      System.out.println(countExists);

    }
  }

  public void scrap() throws InterruptedException {
    scrap(null);
  }

  public JSONArray buscarPaginado(String servicio, Parametros criterios, int cantPagina, int total){
    BuscadorService buscador = new BuscadorService();
    int offset = 0;
    JSONArray procesos = new JSONArray();
    while (total > offset){
      logger.warn("Recuperando " + servicio + " "
              + criterios.get("offset") + "-"
              + (Integer.valueOf((String)criterios.get("offset")) + cantPagina ));
      JSONArray procesosPaginados = buscador.recuperar(servicio, criterios);

      if(procesosPaginados != null){
        for (int i = 0; i < procesosPaginados.length(); i++) {
          procesos.put(procesosPaginados.getJSONObject(i));
        }

        offset += cantPagina;
        criterios.put("offset", String.valueOf(offset));

        if(offset + cantPagina > total){
          cantPagina =  total - offset;
        }
      } else {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    logger.warn("Recuperadas: " + procesos.length());

    return procesos;
  }

  public void scrapProveedores(){
    JSONArray procesos = buscarPaginado("proveedores", Parametros.builder()
                    //.put("fecha_desde", fechaDesde)
                    //.put("fecha_hasta", fechaHasta)
                    //.put("tipo_fecha", "ADJ")
                    .put("offset", "0")
                    .put("limit", "1000")
            , 1000, 26246
    );
  }
}