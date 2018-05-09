package py.gov.ocds.aplicacion;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import py.gov.ocds.context.Context;
import py.gov.ocds.dao.impl.ScraperDao;
import py.gov.ocds.scraper.Parametros;
import py.gov.ocds.scraper.Scraper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        Scraper scraper = new Scraper();
        //scraper.scrap();
        scraper.scrapProveedores();
        /*Context context = new Context();
        ScraperDao dao = new ScraperDao();
        Translator translator = new Translator();

        Document documento = (Document)dao.get("193399").get("compiledRelease");
        context.addContext(documento, "compiledRelease");

        translator.translateToFile(documento);
        System.out.println("Cantidad de Instancias: " + context.getCantInstancias());
        System.out.println("Cantidad de Blank Nodes: " + context.getCantBlankNodes());*/
    }
}
