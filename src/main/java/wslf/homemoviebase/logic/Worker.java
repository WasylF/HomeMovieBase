package wslf.homemoviebase.logic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import org.bson.Document;
import wslf.homemoviebase.db.MongoDB;

import static wslf.homemoviebase.logic.Constants.*;

/**
 *
 * @author Wsl_F
 */
public class Worker {

    MongoDB mongoDB;
    FileChecker fileChecker;
    String rootFolder;
    Random random;
    MoviesFinder moviesFinder;
    String workFolder;

    public void setRootFolder(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    public String getRootFolder() {
        return rootFolder;
    }

    public void setWorkFolder(String workFolder) {
        this.workFolder = workFolder;
    }

    public String getWorkFolder() {
        return workFolder;
    }

    public Worker() {
        mongoDB = new MongoDB("HMB", "mongodb://localhost:27017");
        mongoDB.ensureBasics();

        fileChecker = new FileChecker(mongoDB);
        moviesFinder = new MoviesFinder();
        workFolder = "C:\\";
        random = new Random(Double.doubleToLongBits(Math.random()) ^ System.nanoTime());
    }

    /**
     * Looking for the similar files that alredy in the db
     *
     * @param folderPath path to folder, that stored video files
     * @return dublicats: List of pairs[input file path, same file in db path]
     */
    public LinkedList<Pair<String, String>> checkEvent(String folderPath) {
        return fileChecker.check(folderPath, rootFolder);
    }

    /**
     * adds event to DB, moves files to a new directory, adds hashes to DB
     *
     * @param caption
     * @param path
     * @param peopleNames
     * @param place
     * @param year
     * @param mounth
     * @param day
     * @param accuracy
     * @param category
     * @param tags
     * @param addToBase
     * @param targetPath
     * @return error list
     */
    public String addEvent(String caption, String path, String peopleNames,
            String place, String year, String mounth, String day,
            String accuracy, String category, String tags, boolean addToBase,
            StringBuilder targetPath) {
        String errorMessage = "";

        if (caption.length() < MIN_CAPTION_SIZE) {
            errorMessage += "Слишком короткое название\n";
        }

        if (!Files.isDirectory(Paths.get(path))) {
            errorMessage += "Данный путь не является папкой!\n";
        }

        List<Document> people = new LinkedList<>();
        String msg = parsePeople(peopleNames, people);
        if (!msg.equals(SUCCESS_MESSAGE)) {
            errorMessage += msg + "\n";
        }

        Document placeDoc = mongoDB.getPlace(place);

        Integer yearInt = toInt(year);
        if (yearInt == null) {
            errorMessage += "Неправильный формат даты (год)";
        }
        Integer mounthInt = toInt(mounth);
        if (mounthInt == null) {
            if (mounth.isEmpty()) {
                mounthInt = -1;
            } else {
                errorMessage += "Неправильный формат даты (месяц)";
            }
        }
        Integer dayInt = toInt(day);
        if (dayInt == null) {
            if (day.isEmpty()) {
                dayInt = -1;
            } else {
                errorMessage += "Неправильный формат даты (день)";
            }
        }
        Integer accuracyInt = toInt(accuracy);
        if (accuracyInt == null) {
            if (accuracy.isEmpty()) {
                accuracyInt = -1;
            } else {
                errorMessage += "Неправильный формат даты (точность)";
            }
        }

        Document categoryDoc = mongoDB.getCategory(category);
        List<Document> tagsDoc = parseTags(tags);

        if (errorMessage.isEmpty()) {
            if (addToBase) {
                String relativePath = getRelativePath(targetPath.toString());
                if (mongoDB.addEvent(caption, relativePath, people, placeDoc, yearInt,
                        mounthInt, dayInt, accuracyInt, categoryDoc, tagsDoc)) {
                    if (!moveFiles(path, targetPath.toString())) {
                        errorMessage += "File moving error!\n";
                    }
                } else {
                    errorMessage += "DB error!\n";
                }
            } else {
                targetPath.append(getTargetPath(yearInt, caption, category));
            }
        }

        return errorMessage.isEmpty() ? SUCCESS_MESSAGE : errorMessage;
    }

    private Integer toInt(String n) {
        try {
            return Integer.valueOf(n);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public String parsePeople(String peopleNames, List<Document> result) {
        result.clear();
        if (peopleNames.isEmpty()) {
            return SUCCESS_MESSAGE;
        }
        String errorMessage = "";
        String[] names = peopleNames.split(",");
        for (String name : names) {
            name = name.trim();
            Document doc = mongoDB.getHuman(name);
            if (doc == null) {
                errorMessage += "Нераспознанное имя: " + name + "\n";
            } else {
                result.add(doc);
            }
        }

        return errorMessage.isEmpty() ? SUCCESS_MESSAGE : errorMessage;
    }

    private List<Document> parseTags(String tags) {
        List<Document> result = new LinkedList<>();
        if (tags.isEmpty()) {
            return result;
        }
        String[] tags_ = tags.split(",");
        for (String tag : tags_) {
            tag = tag.trim();
            result.add(mongoDB.getTag(tag));
        }

        return result;
    }

    private boolean moveFiles(String path, String targetPath) {
        List<String> files = FileChecker.getMovies(path);
        return moveFiles(files, targetPath);
    }

    String getTargetPath(int year, String caption,
            String category) {
        if (category.isEmpty()) {
            category = "Разное";
        }
        return rootFolder + "\\" + category + "\\" + year + "\\"
                + caption /*+ "_0" + random.nextInt(100)*/ + "\\";
    }

    private void insureDestination(String destination) {
        if (!Files.exists(Paths.get(destination))) {
            try {
                Files.createDirectories(Paths.get(destination));
            } catch (IOException ex) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private boolean moveFiles(List<String> source, String destinationFolder) {
        insureDestination(destinationFolder);
        try {
            for (String file : source) {
                Path path = Paths.get(file);
                if (Files.exists(path) && Files.isRegularFile(path)) {
                    Path target = Paths.get(destinationFolder + path.getFileName().toString());
                    Files.move(path, target, StandardCopyOption.REPLACE_EXISTING);
                    String shortNewPath = getRelativePath(target);
                    mongoDB.addFile(shortNewPath,
                            FileChecker.getFileHash(target.toString()));
                }
            }
        } catch (IOException ex) {
            return false;
        }

        return true;
    }

    public boolean findRootPath() {
        for (char drive = 'C'; drive <= 'Z'; drive++) {
            String path = drive + ":\\" + DEFAULT_ROOT_FOLDER;
            if (Files.exists(Paths.get(path))) {
                setRootFolder(path);
                return true;
            }
        }
        return false;
    }

    public LinkedList<Document> getAll(String collectionName) {
        return mongoDB.getCollection(collectionName);
    }

    public LinkedList<String> getAllCategories() {
        List<Document> categories = mongoDB.getCollection("Categories");
        LinkedList<String> result = new LinkedList<>();
        for (Document category : categories) {
            result.add(category.getString("caption"));
        }

        return result;
    }

    public LinkedList<String> getAllPlaces() {
        List<Document> places = mongoDB.getCollection("Places");
        LinkedList<String> result = new LinkedList<>();
        for (Document place : places) {
            result.add(place.getString("caption"));
        }

        return result;
    }

    public List<String> getMoviesFolders(int amount) {
        return moviesFinder.findFolders(workFolder, amount);
    }

    /**
     *
     * @param folderPath path to folder with files
     * @return YYYY-MM-DD
     */
    public String getCreationDate(String folderPath) {
        return FileChecker.getCreationDate(folderPath);
    }

    private String getRelativePath(Path path) {
        return getRelativePath(path.toAbsolutePath().toString());
    }

    private String getRelativePath(String path) {
        return path.substring(rootFolder.length());
    }
}
