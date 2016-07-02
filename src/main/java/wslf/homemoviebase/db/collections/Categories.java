package wslf.homemoviebase.db.collections;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 *
 * @author Wsl_F
 */
public class Categories extends Collection {

    /*
    Mongo collection
     Categories {
        caption : String
     }
     */
    public Categories(MongoDatabase database) {
        super(database);
    }

    private static final String CAPTION_FIELD = "caption";

    /**
     *
     * @param caption caption of some category
     * @return existence
     */
    public boolean exists(String caption) {
        return collection.count(new Document(CAPTION_FIELD, caption)) != 0;
    }

    /**
     * creates new category in Categories collection
     *
     * @param caption category caption
     * @return successfulness
     */
    private boolean addNew(String caption) {
        try {
            Document category = new Document(CAPTION_FIELD, caption);
            collection.insertOne(category);
        } catch (Exception ex) {
            System.err.println("db error!\n" + ex.toString());
            return false;
        }

        return true;
    }

    /**
     * creates if need new category in Categories collection
     *
     * @param caption category caption
     */
    public void ensureExistence(String caption) {
        if (!exists(caption)) {
            addNew(caption);
        }
    }

    /**
     * get category with specified caption
     *
     * @param caption caption
     * @return document
     */
    public Document get(String caption) {
        ensureExistence(caption);

        FindIterable<Document> iterable = collection.find(new Document(CAPTION_FIELD, caption));
        return iterable.first();
    }

}
