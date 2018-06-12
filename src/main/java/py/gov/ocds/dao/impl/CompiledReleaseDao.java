package py.gov.ocds.dao.impl;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import py.gov.ocds.dao.interfaz.Dao;
import py.gov.ocds.factory.MongoClientFactory;
import javax.annotation.PreDestroy;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by diego on 29/04/17.
 */
public class CompiledReleaseDao implements Dao {

    private static String DATABASE = "opendata";
    private static String COLLECTION = "ocds";

    private MongoManager mongoManager = new MongoManager(DATABASE, COLLECTION);

    public void guardar(String id, String record) {
        guardar(null, id, record);
    }

    public void agregarContexto(String id, String record) {
        String KEY = "compiledRelease";
        Document doc = mongoManager.createDocumentContext(id, KEY, record);
        mongoManager.update(doc);
    }

    public void agregarTDB(MongoManager mongoManager, String id, String record) {
        String KEY = "compiledRelease";
        Document doc = mongoManager.createDocumentTDB(id, KEY, record);
        mongoManager.update(doc);
    }

    public void guardar(MongoManager mongoManager, String id, String record) {
        MongoManager manager = mongoManager != null? mongoManager: this.mongoManager;
        String KEY = "compiledRelease";
        Document doc = manager.createDocument(id, KEY, record);
        manager.update(doc);
    }

    public void saveFile(String id, String record) {
        try (FileWriter file = new FileWriter("json/"+id+".json")) {
            file.write(record);
            System.out.println("Successfully Copied JSON Object to File...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FindIterable<Document> getAll(){
        return mongoManager.getAll();
    }

    public List<Document> getAllDocuments(){
        return getAll().into(new ArrayList<>());
    }

    public List<Document> getPaginado(int offset, int limit, Bson filter){
        return getPaginado(null, offset, limit, filter);
    }

    public List<Document> getPaginado(MongoManager mongoManager, int offset, int limit, Bson filter){
        MongoManager manager = mongoManager != null? mongoManager: this.mongoManager;
        return manager.getPaginado(offset, limit, filter).into(new ArrayList<>());
    }

    public Document get(String id){
        return mongoManager.get(id);
    }

}
