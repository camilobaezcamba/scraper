package py.gov.ocds.dao.impl;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import py.gov.ocds.dao.interfaz.Dao;
import py.gov.ocds.factory.MongoClientFactory;

/**
 * Created by diego on 29/04/17.
 */
public class ScraperDao implements Dao {

    public void guardar(String id, String record) {
        Document doc = crearDocumento(id, record);
        mongoUpdate(doc);
    }

    private Document crearDocumento(String id, String record) {

        JSONObject recordPackage = new JSONObject(record);

        JSONObject documento = new JSONObject();
        documento.put("_id", id);
        documento.put("record_package", recordPackage);

        return Document.parse(documento.toString());
    }

    private void mongoInsert(Document doc) {
        MongoClient mongo = MongoClientFactory.getMongoClient();
        MongoDatabase dbManager = mongo.getDatabase("opendata");
        MongoCollection<Document> colllection = dbManager.getCollection("ocds");
        colllection.insertOne(doc);
        mongo.close();
    }

    private void mongoUpdate(Document doc) {
        MongoClient mongo = MongoClientFactory.getMongoClient();
        MongoDatabase dbManager = mongo.getDatabase("opendata");
        MongoCollection<Document> colllection = dbManager.getCollection("ocds");
        String id = doc.getString("_id");
        Bson filter = Filters.eq("_id", id);

        Bson update = new Document("$set", doc);
        UpdateOptions options = new UpdateOptions().upsert(true);
        colllection.updateOne(filter, update, options);
        mongo.close();
    }
}
