package wslf.homemoviebase.db.collections;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import java.util.LinkedList;
import java.util.List;
import org.bson.Document;

/**
 *
 * @author Wsl_F
 */
public class Events extends Collection {

    public Events(MongoDatabase database) {
        super(database);
    }

    /*
    Mongo collection
    Events {
        caption : String
        path : String
        people : [People]
        place : Places
        year  : int
        mounth : int or null
        day     : int or null
        accuracy : int 
        category : Categories
        tags : [Tags]
    }
     */
    private static final String CAPTION_FIELD = "caption";
    private static final String PATH_FIELD = "path";
    private static final String PEOPLE_FIELD = "people";
    private static final String PLACE_FIELD = "place";
    private static final String YESR_FIELD = "year";
    private static final String MOUNTH_FIELD = "mounth";
    private static final String DAY_FIELD = "day";
    private static final String ACCURACY_FIELD = "accuracy";
    private static final String CATEGORY_FIELD = "category";
    private static final String TAGS_FIELD = "tags";

    public boolean addNew(String caption, String path, List<Document> people,
            Document place, int year, int mounth, int day, int accuracy,
            Document category, List<Document> tags) {
        try {
            Document event = new Document(CAPTION_FIELD, caption);
            event.append(PATH_FIELD, path);
            event.append(PEOPLE_FIELD, people);
            event.append(PLACE_FIELD, place);
            event.append(YESR_FIELD, year);
            if (mounth >= 1 && mounth <= 12) {
                event.append(MOUNTH_FIELD, mounth);
            }
            if (day >= 1 && day <= 31) {
                event.append(DAY_FIELD, day);
            }
            event.append(ACCURACY_FIELD, accuracy);
            event.append(CATEGORY_FIELD, category);
            event.append(TAGS_FIELD, tags);

            collection.insertOne(event);
        } catch (Exception ex) {
            System.err.println("db error!\n" + ex.toString());
            return false;
        }

        return true;
    }

    public LinkedList<Document> find(String caption_part, int year_begin, int year_end) {
        LinkedList<Document> result = new LinkedList<>();
        BasicDBObject query = new BasicDBObject();
        query.put(CAPTION_FIELD,
                new BasicDBObject("$regex", "*" + caption_part + "*")
                .append("$options", "i"));
        
        query.append(YESR_FIELD, new BasicDBObject("$gte",
                year_begin).append("$lt", year_end));
     
        FindIterable<Document> events = collection.find(query);
        
        for (Document event : events) {
            result.addLast(event);
        }
        
        return result;
    }
}
