package py.gov.ocds.context;

import org.apache.commons.text.WordUtils;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
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

    public void addContext(Document ocdsObject, String nombrePropiedad){
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
            //Arreglar los codelist
            if((nombrePropiedad.equals("tender")
                    || nombrePropiedad.matches("contracts?")
                    || nombrePropiedad.matches("awards?"))
                    && property.equals("status")){
                String valor = ocdsObject.getString("status");
                ocdsObject.put("status", "http://purl.org/onto-ocds/ocds#"+map.get(nombrePropiedad)+"Status" + WordUtils.capitalizeFully(valor));
            }

            if(ocdsObject.get(property) instanceof Document) {
                addContext((Document) ocdsObject.get(property), property);
            } else if(ocdsObject.get(property) instanceof ArrayList) {
                ArrayList array = (ArrayList) ocdsObject.get(property);
                for (Object item : array) {
                    if (item instanceof Document)
                        addContext((Document) item, property);
                }
            }
        }
    }

    private String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    private String getId(JSONObject ocdsObject, String nombrePropiedad){
        if(nombrePropiedad.equals("suppliers")){
            return "http://www.contrataciones.gov.py/datos/api/v2/doc/proveedores/ruc/"+ocdsObject.getJSONObject("identifier").getString("id");
        }
        if(ocdsObject.get("uri") != null && isUri(ocdsObject.getString("uri")))
            return ocdsObject.getString("uri");
        if(ocdsObject.get("url") != null && isUri(ocdsObject.getString("url")))
            return ocdsObject.getString("url");
        if(ocdsObject.get("id") != null && isUri(ocdsObject.getString("id")))
            return ocdsObject.getString("id");

        return "";
    }

    private String getId(Document ocdsObject, String nombrePropiedad){
        if(nombrePropiedad.equals("suppliers")){
            return "http://www.contrataciones.gov.py/datos/api/v2/doc/proveedores/ruc/"+ ((Document)ocdsObject.get("identifier")).getString("id");
        }
        if(ocdsObject.get("uri") != null && isUri(ocdsObject.getString("uri")))
            return ocdsObject.getString("uri");
        if(ocdsObject.get("url") != null && isUri(ocdsObject.getString("url")))
            return ocdsObject.getString("url");
        if(ocdsObject.get("id") != null && isUri(ocdsObject.getString("id")))
            return ocdsObject.getString("id");

        return "";
    }

    private boolean isUri(String url){
        return url.matches("^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
        //return url.matches("^(https?://)?([\da-z.-]+).([a-z.]{2,6})([/\w \.-]*)*\/?$");

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

        map.put("buyer", "organization");
        map.put("procuringEntity", "organization");
        map.put("amount", "value");
        map.put("minValue", "value");
        map.put("additionalClassifications", "classification");
        map.put("tenderPeriod", "period");
        map.put("enquiryPeriod", "period");
        map.put("awardPeriod", "period");
        map.put("contractPeriod", "period");
        map.put("additionalItendifiers", "identifier");

        //para los listados se define
        map.put("suppliers", "organization");
        map.put("documents", "document");
        map.put("items", "item");
        map.put("awards", "award");
        map.put("contracts", "contract");
        map.put("tenderers", "organization");

        //listado para mapear el nombre del atributo (Array) al nombre del context
        /*mapArrays.put("awards", "award");
        mapArrays.put("documents", "document");*/
    }
}