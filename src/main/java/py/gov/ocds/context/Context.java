package py.gov.ocds.context;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;

public class Context {

    private HashMap<String, String> map;
    private int cantBlankNodes;
    private int cantInstancias;

    public int getCantBlankNodes() {
        return cantBlankNodes;
    }

    public int getCantInstancias() {
        return cantInstancias;
    }

    //HashMap<String, String> mapArrays = new HashMap<>();
    public Context(){
        cantInstancias = 0;
        cantBlankNodes = 0;
        configMapping();
    }

    public void addContext(JSONObject ocdsObject, String nombrePropiedad){
        String contextName = map.get(nombrePropiedad);
        if(contextName != null){
            ocdsObject.put("@context", "http://girolabs.com.py/ocds/context-" + contextName + ".json");
            ocdsObject.put("@type", capitalize(contextName));
            String id = getId(ocdsObject, nombrePropiedad);
            if(!id.isEmpty()){
                ocdsObject.put("@id", id);
                cantInstancias++;
            }else{
                cantBlankNodes++;
            }
        }

        for(String property: ocdsObject.keySet()){
            //por el momento no manejar arrays
            if(ocdsObject.get(property) instanceof JSONObject)
                addContext(ocdsObject.getJSONObject(property), property);
            else if(ocdsObject.get(property) instanceof JSONArray){
                JSONArray array = ocdsObject.getJSONArray(property);
                for (Object item : array) {
                    if (item instanceof JSONObject)
                        addContext((JSONObject) item, property);
                }
            }
        }
    }

    private String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    private String getId(JSONObject ocdsObject, String nombrePropiedad){
        if(nombrePropiedad.equals("suppliers")){
            return ocdsObject.getJSONObject("identifier").getString("id");
        }
        if(ocdsObject.has("uri"))
            return ocdsObject.getString("uri");
        if(ocdsObject.has("url"))
            return ocdsObject.getString("url");
        if(ocdsObject.has("id"))
            return ocdsObject.getString("id");

        return "";
    }

    private void configMapping(){
        map = new HashMap<>();
        //Se agrega un mappeo de nombre de atributo a identificador de contexto
        map.put("compiledRelease", "release");
        map.put("contactPoint", "contactPoint");
        map.put("award", "award");
        map.put("contract", "contract");
        map.put("budget", "budget");
        map.put("address", "address");
        map.put("classification", "classification");
        map.put("document", "document");
        map.put("identifier", "identifier");
        map.put("item", "item");
        map.put("period", "period");
        map.put("planning", "planning");
        map.put("tender", "tender");
        map.put("unit", "unit");
        map.put("value", "value");
        map.put("organization", "organization");

        map.put("suppliers", "organization");

        //para los listados se define
        map.put("awards", "award");
        map.put("documents", "document");
        map.put("items", "item");
        map.put("awardPeriod", "period");
        map.put("documents", "document");

        //listado para mapear el nombre del atributo (Array) al nombre del context
        /*mapArrays.put("awards", "award");
        mapArrays.put("documents", "document");*/
    }
}