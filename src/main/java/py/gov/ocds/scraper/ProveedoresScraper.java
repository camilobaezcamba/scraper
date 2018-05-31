package py.gov.ocds.scraper;

import ch.qos.logback.classic.Logger;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import py.gov.ocds.dao.impl.CompiledReleaseDao;
import py.gov.ocds.dao.impl.ProveedoresDao;
import py.gov.ocds.dao.interfaz.Dao;
import py.gov.ocds.service.impl.BaseService;
import py.gov.ocds.service.impl.BuscadorService;
import py.gov.ocds.service.impl.OCDSService;
import py.gov.ocds.service.impl.ProveedoresService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by diego on 03/04/17.
 */
public class ProveedoresScraper {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ProveedoresScraper.class);

  private static List<Thread> hilos = new ArrayList<>();

  private static final boolean asynkMode = true;
  private static final int sleep = 0;
  private static final int sleepReintento = 500;
  private static ExecutorService es;
  private static int cantidadPorPagina = 1000; // por pagina
  private static final int cantidadTotal = 26254; // en total "12508";

  private static List<String> invalidos =  Arrays.asList(
          "chai-sociedad-anonima", "juan-manuel-battilana-pena-bibolini" //404
  );

  /**
   * Método asincrono para obtener un compiledRelease. Solamente se crea el hilo y es agregado al listado de hilos, deben ser iniciados manualmente
   * @param finalI
   * @param procesos
   * @param dao
   * @param ocds
   * @throws InterruptedException
   */
  private void asynkSaveRecord(int finalI, JSONArray procesos, ProveedoresDao dao, ProveedoresService ocds) throws InterruptedException {
    String id = "";
    try{
      id = procesos.getJSONObject(finalI).getString("id");
    }catch (JSONException ex){
      System.out.println("Excepcion " + finalI);
    }

    if(id.isEmpty()){
      return;
    }
    Document doc = dao.get(id.toString());
    if(doc != null){
      System.out.println(id + " ya existe en la BD");
      return;
    }

    if(invalidos.contains(id.toString())){
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
  private void normalSaveRecord(int finalI, JSONArray procesos, ProveedoresDao dao, ProveedoresService ocds) throws InterruptedException {
    String id = procesos.getJSONObject(finalI).getString("id");
    Document doc = dao.get(id.toString());
    if(doc != null){
      System.out.println(id + " ya existe en la BD");
      return;
    }

    saveRecord(finalI, procesos, dao, ocds);
    if(sleep > 0)
      Thread.sleep(sleep);
  }

  //Obtiene un compiledRelease y lo almacen en la base de datos
  private void saveRecord(int finalI, JSONArray procesos, ProveedoresDao dao, ProveedoresService ocds){
      String id_llamado = procesos.getJSONObject(finalI).getString("id");
      saveRecordOne(id_llamado, dao, ocds);
  }

  private void saveRecordOne(String id, ProveedoresDao dao, ProveedoresService ocds){
    String record = ocds.get(id);
    if(record == null){
      logger.error("No se pudo recuperar el registro {}", id);
      return;
    }
    dao.guardar(id, record);
    logger.debug("Datos guardados: {}", id);
  }

  public void scrap(String id) throws InterruptedException {
    ProveedoresService ocds = new ProveedoresService();
    ProveedoresDao dao = new ProveedoresDao();

    if(id != null){
      logger.warn("Recuperando recordPackage: " + id);
      saveRecordOne(id, dao, ocds);
    }else{
      JSONArray procesos = buscarPaginado("proveedores", Parametros.builder()
                      .put("offset", "0")
                      .put("limit", String.valueOf(cantidadPorPagina))
              , cantidadPorPagina, cantidadTotal
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

  private String servicio;
  private String id;
  private Parametros parametros;

  private Dao dao;
  private BaseService service;
  public void scrapProveedores(){
    this.dao = new ProveedoresDao();
    this.service = new ProveedoresService();
    this.servicio = "proveedores";
    this.id = "id";
    this.parametros = Parametros.builder()
            .put("offset", "0")
            .put("limit", "1000");
  }

  public void scrapLicitaciones(){
    this.dao = new CompiledReleaseDao();
    this.service = new OCDSService();
    this.servicio = "licitaciones";
    this.id = "id_llamado";
    this.parametros = Parametros.builder()
            .put("fecha_desde", "2016-01-01")
            .put("fecha_hasta", "2016-12-31")
            .put("tipo_fecha", "ENT")
            .put("tipo_licitacion", "tradicional")
            .put("offset", "0")
            .put("limit", "cantidadLicitaciones");
  }
}