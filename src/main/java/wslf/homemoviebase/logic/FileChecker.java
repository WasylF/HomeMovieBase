package wslf.homemoviebase.logic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import javax.xml.bind.DatatypeConverter;
import wslf.homemoviebase.db.MongoDB;

/**
 *
 * @author Wsl_F
 */
public class FileChecker {

    /**
     * object to work with db
     */
    private final MongoDB mongoDB;
    /**
     * Maximum files size to load whole it to the RAM. in bytes
     */
    private static final long MAX_SIZE_IN_RAM = 256 * 1024 * 1024; // 256 MB

    public FileChecker(MongoDB mongoDB) {
        this.mongoDB = mongoDB;
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
     * calculate file hash: "size_MD5FileHash"
     *
     * @param path path to the file
     * @return hash
     */
    private String getFileHash(String path) {
        try {
            long fileSize = Files.size(Paths.get(path));
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(Files.readAllBytes(Paths.get(path)));
            byte[] digest = md.digest();
            String fileHash = DatatypeConverter.printHexBinary(digest);
            return fileSize + "_" + fileHash;
        } catch (IOException | NoSuchAlgorithmException ex) {
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
