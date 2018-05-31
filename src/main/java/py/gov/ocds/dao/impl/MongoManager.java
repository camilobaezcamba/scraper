package py.gov.ocds.dao.impl;

import ch.qos.logback.classic.Logger;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import py.gov.ocds.factory.MongoClientFactory;

import javax.annotation.PreDestroy;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.ne;

/**
 * Created by diego on 06/08/17.
 */
public class MongoManager {
    private MongoClient mongo;
    private MongoDatabase dbManager;
    private MongoCollection<Document> collection;

    private static Logger root = (Logger) LoggerFactory.getLogger("org.mongodb.driver");
    static {
        root.setLevel(ch.qos.logback.classic.Level.ERROR);
    }

    public MongoManager(String dbname, String collectionName){
        mongo = MongoClientFactory.getMongoClient();
        dbManager = mongo.getDatabase(dbname);
        collection = dbManager.getCollection(collectionName);
    }

    public Document createDocument(String id, String key, String data) {
        JSONObject documento = new JSONObject();
        documento.put("_id", id);
        documento.put(key, new JSONObject(data));
        return Document.parse(documento.toString());
    }

    public Document createDocumentContext(String id, String key, String data) {
        JSONObject documento = new JSONObject();
        documento.put("_id", id);
        documento.put("contextoAgregado", true);
        documento.put(key, new JSONObject(data));
        return Document.parse(documento.toString());
    }

    public Document createDocumentTDB(String id, String key, String data) {
        JSONObject documento = new JSONObject();
        documento.put("_id", id);
        documento.put("tdb", true);
        documento.put(key, new JSONObject(data));
        return Document.parse(documento.toString());
    }

    public void insert(Document doc) {
        collection.insertOne(doc);
    }

    public void update(Document doc) {
        String id = doc.getString("_id");
        Bson filter = eq("_id", id);
        Bson update = new Document("$set", doc);
        UpdateOptions options = new UpdateOptions().upsert(true);
        collection.updateOne(filter, update, options);
    }

    public FindIterable<Document> getAll(){
        return collection.find(ne("contextoAgregado", true));
    }

    public FindIterable<Document> getPaginado(int offset, int limit, Bson filter){
        if(filter != null)
            return collection.find(filter).skip(offset).limit(limit);
        else
            return collection.find().skip(offset).limit(limit);
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