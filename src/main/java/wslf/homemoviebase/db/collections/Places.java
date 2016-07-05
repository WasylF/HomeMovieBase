package wslf.homemoviebase.db.collections;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 *
 * @author Wsl_F
 */
public class Places extends Collection {

    /*
    Mongo collection
     Places {
        caption : String
     }
     */
    public Places(MongoDatabase database) {
        super(database);
    }

    private static final String CAPTION_FIELD = "caption";

    /**
     *
     * @param caption caption of some place
     * @return existence
     */
    public boolean exists(String caption) {
        caption = caption.trim();
        return collection.count(new Document(CAPTION_FIELD, caption)) != 0;
    }

    /**
     * creates new place in Places collection
     *
     * @param caption place caption
     * @return successfulness
     */
    private boolean addNew(String caption) {
        try {
            caption = caption.trim();
            Document place = new Document(CAPTION_FIELD, caption);
            collection.insertOne(place);
        } catch (Exception ex) {
            System.err.println("db error!\n" + ex.toString());
            return false;
        }

        return true;
    }

    /**
     * creates if need new place in Places collection
     *
     * @param caption place caption
     */
    public void ensureExistence(String caption) {
        caption = caption.trim();
        if (!exists(caption)) {
            addNew(caption);
        }
    }

    /**
     * get place with specified caption
     *
     * @param caption caption
     * @return document
     */
    public Document get(String caption) {
        caption = caption.trim();
        ensureExistence(caption);

        FindIterable<Document> iterable = collection.find(new Document(CAPTION_FIELD, caption));
        return iterable.first();
    }
}
