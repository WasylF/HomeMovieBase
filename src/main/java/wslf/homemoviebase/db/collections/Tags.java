package wslf.homemoviebase.db.collections;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 *
 * @author Wsl_F
 */
public class Tags extends Collection {

    /*
    Mongo collection
     Tags {
        caption : String
     }
     */
    public Tags(MongoDatabase database) {
        super(database);
    }

    private static final String CAPTION_FIELD = "caption";

    /**
     *
     * @param caption caption of some tag
     * @return existence
     */
    public boolean exists(String caption) {
        return collection.count(new Document(CAPTION_FIELD, caption)) != 0;
    }

    /**
     * creates new tag in Tags collection
     *
     * @param caption tag caption
     * @return successfulness
     */
    private boolean addNew(String caption) {
        try {
            Document tag = new Document(CAPTION_FIELD, caption);
            collection.insertOne(tag);
        } catch (Exception ex) {
            System.err.println("db error!\n" + ex.toString());
            return false;
        }

        return true;
    }

    /**
     * creates if need new tag in Tags collection
     *
     * @param caption tag caption
     */
    public void ensureExistence(String caption) {
        if (!exists(caption)) {
            addNew(caption);
        }
    }

    /**
     * get tag with specified caption
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
