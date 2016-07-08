package wslf.homemoviebase.logic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import wslf.homemoviebase.db.MongoDB;

import static wslf.homemoviebase.logic.Constants.*;

/**
 *
 * @author Wsl_F
 */
public class FileChecker {

    /**
     * object to work with db
     */
    private final MongoDB mongoDB;

    public FileChecker(MongoDB mongoDB) {
        this.mongoDB = mongoDB;
    }

    /**
     *
     * @param folderPath path to folder with files
     * @return YYYY-MM-DD
     */
    public static String getCreationDate(String folderPath) {
        try {
            String filePath = getMovies(folderPath).getFirst();
            Path path = Paths.get(filePath);
            BasicFileAttributes attr;
            attr = Files.readAttributes(path, BasicFileAttributes.class);
            System.out.println("creationTime: " + attr.creationTime());
            return attr.creationTime().toString().substring(0, 10);
        } catch (IOException ex) {
            Logger.getLogger(FileChecker.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "0000-00-00";
    }

    public static LinkedList<String> getMovies(String folderPath) {
        LinkedList<String> files = new LinkedList<>();

        try {
            Files.walk(Paths.get(folderPath), 1)
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        for (String dataType : DATA_TYPES) {
                            if (filePath.toString().endsWith(dataType)) {
                                files.add(filePath.toString());
                                break;
                            }
                        }
                    });
        } catch (IOException ex) {
            Logger.getLogger(FileChecker.class.getName()).log(Level.SEVERE, null, ex);
        }

        return files;
    }

    /**
     * Looking for the similar files that alredy in the db
     *
     * @param folderPath path to folder, that stored video files
     * @return dublicats: List of pairs<input file path, same file in db path>
     */
    public LinkedList<Pair<String, String>> check(String folderPath) {
        return check(getMovies(folderPath));
    }

    /**
     * Looking for the similar files that alredy in the db
     *
     * @param files list of pathes to files thats needs to be checked
     * @return dublicats: List of pairs<input file path, same file in db path>
     */
    public LinkedList<Pair<String, String>> check(List<String> files) {
        LinkedList<Pair<String, String>> dublicats = new LinkedList<>();
        for (String filePath : files) {
            String hashCode = getFileHash(filePath);
            LinkedList<String> sameHash = mongoDB.getWithHash(hashCode);
            if (!sameHash.isEmpty()) {
                Path curFile = Paths.get(filePath);
                for (String collisionPath : sameHash) {
                    if (isSameContent(curFile, Paths.get(collisionPath))) {
                        dublicats.add(new Pair(filePath, collisionPath));
                    }
                }
            }
        }
        return dublicats;
    }

    /**
     * calculate file hash: "size"
     *
     * @param path path to the file
     * @return hash
     */
    static String getFileHash(String path) {
        try {
            long fileSize = Files.size(Paths.get(path));
            return "" + fileSize;
        } catch (IOException ex) {
            Logger.getLogger(FileChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "_";
    }

    /**
     * chech do files identical
     *
     * @param file1 path to the first file
     * @param file2 path to the second file
     * @return true if files are similar
     */
    boolean isSameContent(Path file1, Path file2) {
        try {
            long size = Files.size(file1);
            if (size != Files.size(file2)) {
                return false;
            }

            if (size < MAX_SIZE_IN_RAM) {
                return Arrays.equals(Files.readAllBytes(file1), Files.readAllBytes(file2));
            }

            try (InputStream is1 = Files.newInputStream(file1);
                    InputStream is2 = Files.newInputStream(file2)) {
                int data;
                while ((data = is1.read()) != -1) {
                    if (data != is2.read()) {
                        return false;
                    }
                }
            }
        } catch (IOException ex) {
            System.err.println("File compare error!\n" + ex.toString());
            return false;
        }

        return true;
    }
}
