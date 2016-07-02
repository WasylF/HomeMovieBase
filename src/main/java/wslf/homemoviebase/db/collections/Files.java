package wslf.homemoviebase.db.collections;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import java.util.LinkedList;
import org.bson.Document;

/**
 *
 * @author Wsl_F
 */
public class Files extends Collection {

    /*
    Mongo collection
     Files {
        path : String
        hash : String
     }
     */
    public Files(MongoDatabase database) {
        super(database);
    }

    private static final String PATH_FIELD = "path";
    private static final String HASH_FIELD = "hash";

    /**
     * adds new file and hash without any checkup
     *
     * @param path file path
     * @param hash file hash
     * @return successfulness
     */
    public boolean addNew(String path, String hash) {
        try {
            Document human = new Document(PATH_FIELD, path)
                    .append(HASH_FIELD, hash);
            collection.insertOne(human);
        } catch (Exception ex) {
            System.err.println("db error!\n" + ex.toString());
            return false;
        }

        return true;
    }

    /**
     * find all files with specified hash
     *
     * @param hash hash
     * @return list of all files with requested hash
     */
    public LinkedList<String> getWithHash(String hash) {
        FindIterable<Document> files = collection.find(new Document(HASH_FIELD, hash));
        LinkedList<String> pathes = new LinkedList<>();
        for (Document file : files) {
            pathes.add(file.getString(PATH_FIELD));
        }

        return pathes;
    }

}
