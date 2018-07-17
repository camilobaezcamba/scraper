package py.gov.ocds.aplicacion;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.bson.Document;
import py.gov.ocds.context.Context;
import py.gov.ocds.dao.impl.CompiledReleaseDao;
import py.gov.ocds.dao.impl.MongoManager;
import py.gov.ocds.dao.impl.ProveedoresDao;
import py.gov.ocds.scraper.ProveedoresScraper;
import py.gov.ocds.scraper.Scraper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.mongodb.client.model.Filters.ne;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        //translatToTDB("licitaciones");

        Translator translator = new Translator();
        //translator.count("proveedores");
        //translator.query("licitaciones", "SELECT * { ?s ?p ?o } limit 1");
        //translator.borrarModelo("licitaciones");
        //translator.borrarModelo("licitaciones");
        //setTDBFalse();

        System.out.println("1) Generar tdb licitaciones");
        System.out.println("2) Generar tdb proveedores");
        System.out.println("3) Contar licitaciones");
        System.out.println("4) Contar proveedores");
        System.out.println("5) Setear tdb = false en mongo licitaciones");

        Scanner in = new Scanner(System.in);
        int opcion = in.nextInt();
        switch (opcion){
            case 1:
                System.out.println("Generando/Actualizando tdb de licitaciones...");
                translatToTDB("licitaciones");
                break;
            case 2:
                System.out.println("Generando/Actualizando tdb de proveedores...");
                translatToTDB("proveedores");
                break;
            case 3:
                translator.count("licitaciones");
                break;
            case 4:
                translator.count("proveedores");
                break;
            case 5:
                setTDBFalse();
                break;
            default:
                System.out.println("Terminado");
        }
    }

    public void scrapping() throws InterruptedException {
        Scraper scraper = new Scraper();
        scraper.scrap();
        ProveedoresScraper proveedoresScraper = new ProveedoresScraper();
        proveedoresScraper.scrap();
        scraper.scrapProveedores();
    }

    public void agregarContextos() {
        Context context = new Context();
        CompiledReleaseDao dao = new CompiledReleaseDao();
        List<Document> documentos = dao.getPaginado(0, 200, null);
        MongoManager mongoManager = new MongoManager("opendata", "ocdsContext");

        System.out.println("Agregando contexto");
        if(!documentos.isEmpty() && documentos.get(0).get("compiledRelease") != null){
            for(Document doc : documentos){
                Document compiledRelease = (Document) doc.get("compiledRelease");
                String json = compiledRelease.toJson();

                context.addContext(compiledRelease, "compiledRelease");
                dao.guardar(mongoManager, doc.getString("_id"), compiledRelease.toJson());
                dao.agregarContexto(doc.getString("_id"), json);
            }

            System.out.println("Cantidad de Instancias: " + context.getCantInstancias());
            System.out.println("Cantidad de Blank Nodes: " + context.getCantBlankNodes());
        }
    }

    public static void translatToTDB(String entidad) throws JsonProcessingException {
        Translator translator = new Translator();

        if(entidad.equals("licitaciones")){
            CompiledReleaseDao dao = new CompiledReleaseDao();

            MongoManager mongoManager = new MongoManager("opendata", "ocdsContext");
            long total = dao.getCount(mongoManager, ne("tdb", true));
            int limit = 1000;

            while(total > 0){
                System.out.println("------- " + total);
                total = total - limit;

                List<Document> documentos = dao.getPaginado(mongoManager, 0,limit, ne("tdb", true));
                List<Document> documentosFinales = new ArrayList<>();

                for(Document doc : documentos){
                    Document compiledRelease = (Document) doc.get("compiledRelease");
                    documentosFinales.add(compiledRelease);
                }
                translator.translateToTDB(documentosFinales, entidad);
                //translator.load("licitaciones");
                for(Document doc : documentos){
                    Document compiledRelease = (Document) doc.get("compiledRelease");
                    dao.agregarTDB(mongoManager, doc.getString("_id"), compiledRelease.toJson());
                }
            }

        } else {
            ProveedoresDao proveedoresDao = new ProveedoresDao();
            List<Document> proveedores = proveedoresDao.getPaginado(0,26252, null);
            List<Document> proveedoresFinales = new ArrayList<>();

            for(Document doc : proveedores){
                Document compiledRelease = (Document) doc.get("proveedor");
                proveedoresFinales.add(compiledRelease);
            }
            for(Document proveedor: proveedoresFinales) {
                proveedor.put("@context", "http://girolabs.com.py/ocds/proveedor.json");
            }
            translator.translateToTDB(proveedoresFinales, entidad);
            //translator.count("proveedores");

            for(Document doc : proveedores){
                Document proveedor = (Document) doc.get("proveedor");
                proveedoresDao.agregarTDB(doc.getString("_id"), proveedor.toJson());
            }
        }
    }

    private static void setTDBFalse(){
        MongoManager mongoManager = new MongoManager("opendata", "ocdsContext");
        mongoManager.setTDBFalse();
    }
}
