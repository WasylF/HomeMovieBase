package wslf.homemoviebase.db.collections;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.LinkedList;
import org.bson.Document;

/**
 *
 * @author Wsl_F
 */
public abstract class Collection {

    /**
     * get collection caption in database by java cllection class
     *
     * @param collectionClass java cllection class
     * @return collection caption in database
     */
    public static final String getCollectionName(Class<? extends Collection> collectionClass) {
        if (collectionClass.equals(Categories.class)) {
            return "Categories";
        }
        if (collectionClass.equals(Events.class)) {
            return "Events";
        }
        if (collectionClass.equals(Files.class)) {
            return "Files";
        }
        if (collectionClass.equals(People.class)) {
            return "People";
        }
        if (collectionClass.equals(Places.class)) {
            return "Places";
        }
        if (collectionClass.equals(Tags.class)) {
            return "Tags";
        }
        return "Error";
    }

    MongoDatabase database;
    MongoCollection collection;

    public Collection(MongoDatabase database) {
        this.database = database;
        connect();
    }

    /**
     * connects to collection
     *
     * @return successfulness
     */
    final boolean connect() {
        try {
            Class<? extends Collection> className = this.getClass();
            String collectionName = getCollectionName(className);
            collection = database.getCollection(collectionName);
        } catch (Exception ex) {
            System.err.println("db error!\n" + ex.toString());
            return false;
        }

        return true;
    }

    /**
     *
     * @return all documents from collection
     */
    public LinkedList<Document> getAll() {
        FindIterable<Document> allDocuments = collection.find();
        LinkedList<Document> result = new LinkedList<>();

        for (Document document : allDocuments) {
            result.add(document);
        }

        return result;
    }

    public void printAll() {
        LinkedList<Document> all = getAll();
        int number = 0;
        for (Document doc : all) {
            number++;
            System.out.println(number + ") ");
            System.out.println(doc.toJson());
        }
    }
}
