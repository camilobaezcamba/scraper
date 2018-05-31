package py.gov.ocds.dao.impl;

import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.bson.conversions.Bson;
import py.gov.ocds.dao.interfaz.Dao;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cbaez on 11/05/18.
 */
public class ProveedoresDao implements Dao {

    private static String DATABASE = "opendata";
    private static String COLLECTION = "proveedores";
    private MongoManager mongoManager = new MongoManager(DATABASE, COLLECTION);

    public void guardar(String id, String record) {
        String KEY = "proveedor";
        Document doc = mongoManager.createDocument(id, KEY, record);
        mongoManager.update(doc);
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

    public Document get(String id){
        return mongoManager.get(id);
    }

    public List<Document> getPaginado(int offset, int limit, Bson filter){
        return getPaginado(null, offset, limit, filter);
    }

    public List<Document> getPaginado(MongoManager mongoManager, int offset, int limit, Bson filter){
        MongoManager manager = mongoManager != null? mongoManager: this.mongoManager;
        return manager.getPaginado(offset, limit, filter).into(new ArrayList<>());
    }

    public void agregarTDB(String id, String record) {
        String KEY = "proveedor";
        Document doc = mongoManager.createDocumentTDB(id, KEY, record);
        mongoManager.update(doc);
    }



}
