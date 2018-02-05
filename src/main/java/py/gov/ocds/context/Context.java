package py.gov.ocds.context;

import org.json.JSONArray;
import org.json.JSONObject;
import py.gov.ocds.dao.impl.ScraperDao;
import java.util.HashMap;

public class Context {

    HashMap<String, String> map = new HashMap<>();

    public static void main(String[] args) throws InterruptedException {
        //Scraper scraper = new Scraper();
        //scraper.scrap();
        Context context = new Context();
        context.map.put("compiledRelease", "release");
        context.map.put("contactPoint", "contactPoint");
        ScraperDao dao = new ScraperDao();
        JSONArray compiledReleases = new JSONArray(dao.getAllDocuments());
        JSONObject compiledRelease = (JSONObject) compiledReleases.get(0);
        context.addContext(compiledRelease.getJSONObject("compiledRelease"), "compiledRelease");
        System.out.println(compiledRelease.toString(2));
    }

    private void addContext(JSONObject ocdsObject, String nombre){
        String contextName = map.get(nombre);
        if(contextName != null){
            ocdsObject.put("@context", "http://girolabs.com.py/ocds/context-" + contextName + ".json");
            //ocdsObject.put("@type", "http://girolabs.com.py/ocds/context-" + contextName + ".json");
            //ocdsObject.put("@id", "http://girolabs.com.py/ocds/context-" + contextName + ".json");
        }

        for(String property: ocdsObject.keySet()){
            //por el momento no manejar arrays
            if(ocdsObject.get(property) instanceof JSONObject)
                addContext(ocdsObject.getJSONObject(property), property);
        }
    }
}