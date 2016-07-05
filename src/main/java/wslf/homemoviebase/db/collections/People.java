package wslf.homemoviebase.db.collections;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import java.util.regex.Pattern;
import org.bson.Document;

/**
 *
 * @author Wsl_F
 */
public class People extends Collection {

    /*
    Mongo collection
    People {
        name : String
        full name : String
    }
     */
    private static final String NAME_FIELD = "name";
    private static final String FULL_NAME_FIELD = "full name";

    public People(MongoDatabase database) {
        super(database);
    }

    /**
     * adds new human without any checkup
     *
     * @param name human's name
     * @param fullName human's full name
     * @return successfulness
     */
    private boolean addNew(String name, String fullName) {
        try {
            name = name.trim();
            fullName = fullName.trim();
            Document human = new Document(NAME_FIELD, "#" + name + "#")
                    .append(FULL_NAME_FIELD, "#" + fullName + "#");
            collection.insertOne(human);
        } catch (Exception ex) {
            System.err.println("db error!\n" + ex.toString());
            return false;
        }

        return true;
    }

    /**
     * print to console all existings names
     */
    public void printAll() {
        FindIterable<Document> people = collection.find();
        for (Document human : people) {
            System.out.println("name: " + human.getString(NAME_FIELD));
        }
    }

    /**
     * check does collection contains any human with specific name
     *
     * @param fullName human's fullname
     * @return existence
     */
    public boolean exists(String fullName) {
        fullName = fullName.trim();
        Document query = getFullNameQuery(fullName);
        return collection.count(query) > 0;
    }

    /**
     * calculate number of short names
     *
     * @param name human's name
     * @return numver of humans with this name
     */
    public int count(String name) {
        name = name.trim();
        return (int) collection.count(getNameQuery(name));
    }

    /**
     * create human if need
     *
     * @param name name
     * @param fullName full name
     * @return document that contains requested human
     */
    public Document get(String name, String fullName) {
        name = name.trim();
        fullName = fullName.trim();
        if (!exists(fullName)) {
            addNew(name, fullName);
        }

        FindIterable<Document> iterable = collection.find(getFullNameQuery(fullName));
        return iterable.first();
    }

    /**
     * getting human with specified name, if possible
     *
     * @param name short or full name
     * @return document that contains requested human or nill if doesn't exist
     * human with specified name or exists more than 1 human with requested
     * short name
     */
    public Document get(String name) {
        name = name.trim();
        if (exists(name)) {
            FindIterable<Document> iterable = collection.find(getFullNameQuery(name));
            return iterable.first();
        }

        if (count(name) == 1) {
            FindIterable<Document> iterable = collection.find(getNameQuery(name));
            return iterable.first();
        }

        return null;
    }

    private Document getNameQuery(String name) {
        Document query = new Document(NAME_FIELD, Pattern.compile("#" + name + "#", Pattern.CASE_INSENSITIVE));
        //Document query = new Document(NAME_FIELD, new Document("$regex", name));
        //query.append("$options", "i");
        return query;
    }

    private Document getFullNameQuery(String fullName) {
        Document query = new Document(FULL_NAME_FIELD, Pattern.compile("#" + fullName + "#", Pattern.CASE_INSENSITIVE));
        //query.append("$options", "i");
        return query;
    }
}
