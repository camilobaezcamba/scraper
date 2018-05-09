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
public class ScraperDao implements Dao {

    MongoClient mongo = MongoClientFactory.getMongoClient();
    MongoDatabase dbManager = mongo.getDatabase("opendata");
    MongoCollection<Document> collection = dbManager.getCollection("ocds");

    public void guardar(String id, String record) {
        Document doc = crearDocumento(id, record);
        mongoUpdate(doc);
    }

    private Document crearDocumento(String id, String record) {

        JSONObject recordPackage = new JSONObject(record);

        JSONObject documento = new JSONObject();
        documento.put("_id", id);
        documento.put("compiledRelease", recordPackage);

        return Document.parse(documento.toString());
    }

    private void mongoInsert(Document doc) {
        collection.insertOne(doc);
        mongo.close();
    }

    private void mongoUpdate(Document doc) {
        MongoClient mongo = MongoClientFactory.getMongoClient();

        String id = doc.getString("_id");
        Bson filter = eq("_id", id);

        Bson update = new Document("$set", doc);
        UpdateOptions options = new UpdateOptions().upsert(true);
        collection.updateOne(filter, update, options);
        mongo.close();
    }

    public void saveFile(String id, String record) {
        try (FileWriter file = new FileWriter("json/"+id+".json")) {
            JSONObject recordPackage = new JSONObject(record);
            file.write(record);
            System.out.println("Successfully Copied JSON Object to File...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FindIterable<Document> getAll(){
        FindIterable<Document> documents = collection.find();
        //mongo.close();
        return documents;
    }

    public List<Document> getAllDocuments(){
        return getAll().into(new ArrayList<>());
    }

    public Document get(String id){
        return collection.find(eq("_id", id)).first();
    }

    @PreDestroy
    public void destroy(){
        System.out.println("cerrando conexion");
        mongo.close();
    }
}
