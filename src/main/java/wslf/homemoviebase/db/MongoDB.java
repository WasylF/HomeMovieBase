package wslf.homemoviebase.db;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import java.util.LinkedList;
import java.util.List;
import org.bson.Document;
import wslf.homemoviebase.db.collections.*;

/**
 *
 * @author Wsl_F
 */
public class MongoDB {

    String baseName;
    String clientURI;
    MongoClient mongoClient;
    MongoDatabase database;
    Categories categories;
    Events events;
    Files files;
    People people;
    Places places;
    Tags tags;

    public MongoDB(String baseName, String baseURI) {
        this.baseName = baseName;
        this.clientURI = baseURI;
        connectToBase();
        connectToCollections();
    }

    /**
     * connecting to db
     *
     * @return successfulness
     */
    private boolean connectToBase() {
        try {
            this.mongoClient = new MongoClient(new MongoClientURI(clientURI));
            this.database = mongoClient.getDatabase(baseName);
        } catch (Exception ex) {
            System.err.println("db error!\n" + ex.toString());
            return false;
        }
        return true;
    }

    private boolean connectToCollections() {
        try {
            categories = new Categories(database);
            events = new Events(database);
            files = new Files(database);
            people = new People(database);
            places = new Places(database);
            tags = new Tags(database);
        } catch (Exception ex) {
            System.err.println("connection to collection error!\n" + ex.toString());
            return false;
        }

        return true;
    }

    public void ensureBasics() {
        ensureBasicsCategories();
        ensureBasicsPeople();
        ensureBasicsPlaces();
        ensureBasicsTags();
    }

    private void ensureBasicsCategories() {
        categories.ensureExistence("Домашнее видео");
        categories.ensureExistence("Тернополь");
        categories.ensureExistence("Рощино");
        categories.ensureExistence("Степашки");
        categories.ensureExistence("Школа");
        categories.ensureExistence("Дворец пионеров");
        categories.ensureExistence("МАН");
        categories.ensureExistence("Прогулки");
    }

    private void ensureBasicsPlaces() {
        places.ensureExistence("Киев");
        places.ensureExistence("Тернополь");
        places.ensureExistence("Гаи");
        places.ensureExistence("Степашки");
        places.ensureExistence("Санкт-Петербург");
        places.ensureExistence("Рощино");
    }

    private void ensureBasicsTags() {
        tags.ensureExistence("школа");
        tags.ensureExistence("танцы");
        tags.ensureExistence("концерт");
        tags.ensureExistence("день учителя");
        tags.ensureExistence("новый год");
        tags.ensureExistence("первое сентрября");
    }

    private void ensureBasicsPeople() {
        people.get("Ваня", "Франчук Иван Олегович");
        people.get("Вася", "Франчук Василий Олегович");
        people.get("Мама", "Ланцева Наталия Владимировна");
        people.get("Папа", "Франчук Олег Васильевич");
        people.get("Бабуня Лина", "Франчук Лина Максимовна");
        people.get("Бабуня Рая", "Ланцева Раиса Дмитреевна");
        people.get("Дедуня Вася", "Франчук Василий Ефимович");
        people.get("Дедуня Володя", "Ланцев Владимир Алексеевич");
        people.get("Дядя Валик", "Франчук Валентин Васильевич");
        people.get("Крестна", "Ланцева Елевна Владимировна");
    }

    public LinkedList<String> getWithHash(String hash) {
        return files.getWithHash(hash);
    }

    public Document getHuman(String name) {
        return people.get(name);
    }

    public Document getPlace(String place) {
        return places.get(place);
    }

    public Document getCategory(String category) {
        return categories.get(category);
    }

    public Document getTag(String tag) {
        return tags.get(tag);
    }

    public boolean addEvent(String caption, String path, List<Document> people,
            Document place, int year, int mounth, int day, int accuracy,
            Document category, List<Document> tags) {
        return events.addNew(caption, path, people, place, year, mounth, day, accuracy, category, tags);
    }

    public boolean addFile(String file, String hash) {
        return files.addNew(file, hash);
    }

    public LinkedList<Document> getCollection(String collectionName) {
        if (collectionName.equals("Categories")) {
            return categories.getAll();
        }
        if (collectionName.equals("Events")) {
            return events.getAll();
        }
        if (collectionName.equals("Files")) {
            return files.getAll();
        }
        if (collectionName.equals("People")) {
            return people.getAll();
        }
        if (collectionName.equals("Places")) {
            return places.getAll();
        }
        if (collectionName.equals("Tags")) {
            return tags.getAll();
        }
        return new LinkedList<>();
    }
}
