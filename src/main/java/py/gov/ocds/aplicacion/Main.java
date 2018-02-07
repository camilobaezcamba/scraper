package py.gov.ocds.aplicacion;

import org.json.JSONArray;
import org.json.JSONObject;
import py.gov.ocds.context.Context;
import py.gov.ocds.dao.impl.ScraperDao;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        //Scraper scraper = new Scraper();
        //scraper.scrap();
        Context context = new Context();
        ScraperDao dao = new ScraperDao();
        JSONArray compiledReleases = new JSONArray(dao.getAllDocuments());
        JSONObject compiledRelease = (JSONObject) compiledReleases.get(1);
        context.addContext(compiledRelease.getJSONObject("compiledRelease"), "compiledRelease");
        System.out.println(compiledRelease.toString(2));
        System.out.println("Cantidad de Instancias: " + context.getCantInstancias());
        System.out.println("Cantidad de Blank Nodes: " + context.getCantBlankNodes());

        Translator translator = new Translator();
        translator.translate(compiledRelease.getJSONObject("compiledRelease").toString());
    }
}
