package py.gov.ocds.dao.interfaz;

import com.mongodb.client.FindIterable;
import org.bson.Document;

/**
 * Created by diego on 06/05/17.
 */
public interface Dao {

  /**
   * Se encarga de persistir un record package en la base de datos especificando un id unico.
   * @param id
   * @param record
   */
  void guardar(String id, String record);
  void saveFile(String id, String record);
  FindIterable<Document> getAll();
}
