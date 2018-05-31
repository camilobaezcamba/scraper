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

import static com.mongodb.client.model.Filters.ne;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        translatToTDB("licitaciones");
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

            List<Document> documentos = dao.getPaginado(mongoManager, 0,491, ne("tdb", true));
            List<Document> documentosFinales = new ArrayList<>();

            for(Document doc : documentos){
                Document compiledRelease = (Document) doc.get("compiledRelease");
                documentosFinales.add(compiledRelease);
            }
            translator.translateToTDB(documentosFinales, "licitaciones");
            translator.load("licitaciones");
            for(Document doc : documentos){
                Document compiledRelease = (Document) doc.get("compiledRelease");
                dao.agregarTDB(mongoManager, doc.getString("_id"), compiledRelease.toJson());
            }

        } else {
            ProveedoresDao proveedoresDao = new ProveedoresDao();
            List<Document> proveedores = proveedoresDao.getPaginado(1,26252, null);
            List<Document> proveedoresFinales = new ArrayList<>();

            for(Document doc : proveedores){
                Document compiledRelease = (Document) doc.get("proveedor");
                proveedoresFinales.add(compiledRelease);
            }
            for(Document proveedor: proveedoresFinales) {
                proveedor.put("@context", "http://girolabs.com.py/ocds/proveedor.json");
            }
            translator.translateToTDB(proveedoresFinales, "proveedores");
            translator.load("proveedores");

            for(Document doc : proveedores){
                Document proveedor = (Document) doc.get("proveedor");
                proveedoresDao.agregarTDB(doc.getString("_id"), proveedor.toJson());
            }
        }
    }
}
