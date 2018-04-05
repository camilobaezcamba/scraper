package py.gov.ocds.aplicacion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DocumentCodec;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Translator {
    private ObjectMapper mapper;
    private static Codec<Document> DOCUMENT_CODEC = new DocumentCodec();

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

}
