package py.gov.ocds.aplicacion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.TDBLoader;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DocumentCodec;
import org.json.JSONObject;

import javax.print.Doc;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Translator {
    private ObjectMapper mapper;
    private static Codec<Document> DOCUMENT_CODEC = new DocumentCodec();

    private static final String PREFIX = "http://example.org/";

    public Translator(){
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Double.class, new DoubleSerializer());
        mapper.registerModule(module);
    }

    public void translate(String jsonString){
        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(jsonString.getBytes()), null, "JSON-LD");
        model.write(System.out, "TURTLE");
    }

    public void translate(Document document) throws JsonProcessingException {
        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(documentToString(document).getBytes()), null, "JSON-LD");
        model.write(System.out, "TURTLE");
    }

    private String documentToString(Document documento) throws JsonProcessingException {
        return mapper.writeValueAsString(documento);
    }

    public String JSONObjectToString(JSONObject documento) throws JsonProcessingException {
        return mapper.writeValueAsString(documento);
    }

    public void translateToFile(Document document) throws JsonProcessingException {
        Model model = ModelFactory.createDefaultModel();
        System.out.println(documentToString(document));
        model.read(new ByteArrayInputStream(documentToString(document).getBytes()), null, "JSON-LD");
        FileOutputStream fop = null;
        File file;

        try {

            file = new File("/Users/admin/user.ttl");
            fop = new FileOutputStream(file);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            model.write(fop, "RDFXML");

            fop.flush();
            fop.close();

            System.out.println("Done");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void count(String modelo) throws JsonProcessingException {
        String directory = "/Users/admin/tdb_" + modelo ;
        Dataset ds = TDBFactory.createDataset(directory) ;
        Model model = ds.getNamedModel(PREFIX + modelo) ;
        System.err.printf("Model size is: %s\n", model.size());
        ds.close();
    }

    public void query(String modelo, String sparqlQueryString) throws JsonProcessingException {
        String directory = "/Users/admin/tdb_" + modelo ;
        Dataset ds = TDBFactory.createDataset(directory) ;
        Model model = ds.getNamedModel(PREFIX + modelo) ;
        //ds.addNamedModel("http://example.orgs/"+modelo, model);

        Query query = QueryFactory.create(sparqlQueryString) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, model);

        ResultSet results = qexec.execSelect() ;
        ResultSetFormatter.out(results) ;

        qexec.close();
        //System.err.printf("Model size is: %s\n", model.size());

        ds.close();
    }

    public void changeName(String modelo) throws JsonProcessingException {
        String directory = "/Users/admin/tdb_" + modelo ;
        Dataset ds = TDBFactory.createDataset(directory) ;
        Model model = ds.getNamedModel(modelo) ;
        ds.addNamedModel(PREFIX + modelo, model);
        ds.close();
        System.out.println("Hecho");
    }

    public void borrarModelo(String modelo) throws JsonProcessingException {
        String directory = "/Users/admin/tdb_" + modelo ;
        Dataset ds = TDBFactory.createDataset(directory) ;
        ds.removeNamedModel(modelo);
        ds.close();
        System.out.println("Hecho");
    }
    public void translateToTDB(List<Document> documents, String modelo) throws JsonProcessingException {
        if(documents == null || documents.isEmpty())
            return;

        System.out.println("translating");
        String directory = "/Users/admin/tdb_" + modelo ;
        Dataset ds = TDBFactory.createDataset(directory) ;

        Model model = ds.getNamedModel(PREFIX + modelo) ;

        int i = 0;
        for(Document document: documents){
            i++;
            if(i % 100 == 0){
                System.out.println(i);
            }
            model.read(new ByteArrayInputStream(documentToString(document).getBytes()), null, "JSON-LD");
        }
        ds.close();
    }

}
