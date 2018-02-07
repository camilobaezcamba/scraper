package py.gov.ocds.aplicacion;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;

import java.io.ByteArrayInputStream;

public class Translator {
    public void translate(String jsonString){
        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(jsonString.getBytes()), null, "JSON-LD");
        model.write(System.out, "TURTLE");
    }
}
