package py.gov.ocds.factory;

import com.mongodb.*;

/**
 * Created by diego on 06/05/17.
 */
public class MongoClientFactory {

  public static MongoClient getMongoClient()
  {
      MongoClient mongo = new MongoClient();
      return mongo;
  }

}
