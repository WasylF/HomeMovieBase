package wslf.homemoviebase.logic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
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
    private String rootFolder;

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
            String changeDate = attr.lastModifiedTime().toString().substring(0, 10);
            String createDate = attr.creationTime().toString().substring(0, 10);
            return (changeDate.compareTo(createDate) <= 0) ? changeDate : createDate;
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
                            if (filePath.toString().toLowerCase().endsWith(dataType)) {
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
     * @param rootFolder root folder, that contain all movies from the DB
     * @return dublicats: List of pairs [input file path, same file in db path]
     */
    public LinkedList<Pair<String, String>> check(String folderPath,
            String rootFolder) {
        this.rootFolder = rootFolder;
        return check(getMovies(folderPath));
    }

    /**
     * Looking for the similar files that alredy in the db
     *
     * @param files list of pathes to files thats needs to be checked
     * @return dublicats: List of pairs[input file path, same file in db path]
     */
    private LinkedList<Pair<String, String>> check(List<String> files) {
        LinkedList<Pair<String, String>> dublicats = new LinkedList<>();
        for (String filePath : files) {
            String hashCode = getFileHash(filePath);
            LinkedList<String> sameHash = mongoDB.getWithHash(hashCode);
            if (!sameHash.isEmpty()) {
                Path curFile = Paths.get(filePath);
                for (String collisionPath : sameHash) {
                    Path path = Paths.get(rootFolder + "\\" + collisionPath);
                    if (isSameContent(curFile, path)) {
                        dublicats.add(new Pair(filePath,
                                path.toAbsolutePath().toString()));
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
     * check are files identical
     *
     * @param file1 path to the first file
     * @param file2 path to the second file
     * @return true if files are the same
     */
    public static boolean isSameContent(Path file1, Path file2) {
        try {
            long size = Files.size(file1);
            if (size != Files.size(file2)) {
                return false;
            }

            if (size < MAX_SIZE_IN_RAM) {
                return Arrays.equals(Files.readAllBytes(file1), Files.readAllBytes(file2));
            }

            return compareLarge(file1, file2, MY_BUFFER_SIZE);
        } catch (IOException ex) {
            System.err.println("File compare error!\n" + ex.toString());
            return false;
        }
    }

    static boolean compareLarge(Path firstPath, Path secondPath, final int BUFFER_SIZE) throws IOException {
        SeekableByteChannel firstIn = null, secondIn = null;
        try {
            firstIn = Files.newByteChannel(firstPath);
            secondIn = Files.newByteChannel(secondPath);
            if (firstIn.size() != secondIn.size()) {
                return false;
            }
            ByteBuffer firstBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
            ByteBuffer secondBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
            int firstRead, secondRead;
            while (firstIn.position() < firstIn.size()) {
                firstRead = firstIn.read(firstBuffer);
                secondRead = secondIn.read(secondBuffer);
                if (firstRead != secondRead) {
                    return false;
                }
                if (!buffersEqual(firstBuffer, secondBuffer, firstRead)) {
                    return false;
                }
            }
            return true;
        } finally {
            if (firstIn != null) {
                firstIn.close();
            }
            if (secondIn != null) {
                secondIn.close();
            }
        }
    }

    private static boolean buffersEqual(ByteBuffer first, ByteBuffer second, final int length) {
        if (first.limit() != second.limit() || length > first.limit()) {
            return false;
        }
        first.rewind();
        second.rewind();
        int i;
        for (i = 0; i < length - 7; i += 8) {
            if (first.getLong() != second.getLong()) {
                return false;
            }
        }
        for (; i < length; i++) {
            if (first.get() != second.get()) {
                return false;
            }
        }
        return true;
    }
}
