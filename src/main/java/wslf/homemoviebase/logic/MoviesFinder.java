package wslf.homemoviebase.logic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 *
 * @author Wsl_F
 */
public class MoviesFinder {

    private final Set<Path> finishedFolders;
    private LinkedList<String> results;
    private final Set<Path> allResults;
    private int amount;

    public MoviesFinder() {
        finishedFolders = new TreeSet<>();
        allResults = new TreeSet<>();
    }

    public LinkedList<String> findFolders(String rootPath, int amount) {
        this.results = new LinkedList<>();
        this.amount = amount;

        findFolders(Paths.get(rootPath));

        return results;
    }

    private boolean findFolders(Path path) {
        if (finishedFolders.contains(path)) {
            return true;
        }
        if (results.size() >= amount) {
            return false;
        }
        if (containsMovies(path.toString())) {
            if (!allResults.contains(path)) {
                allResults.add(path);
                results.add(path.toString());
            }
        }
        List<Path> subFolders = getSubFolders(path);
        boolean flag = true;
        for (Path subFolder : subFolders) {
            flag &= findFolders(subFolder);
        }

        if (flag) {
            for (Path subFolder : subFolders) {
                finishedFolders.remove(subFolder);
            }
            finishedFolders.add(path);
        }
        return flag;
    }

    static boolean containsMovies(String folderPath) {
        return !FileChecker.getMovies(folderPath).isEmpty();
    }

    static List<Path> getSubFolders(Path folderPath) {
        try {
            List<Path> filesInFolder = Files.walk(folderPath, 1)
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());
            filesInFolder.remove(0);
            return filesInFolder;
        } catch (IOException ex) {
            System.err.println("gettingSubFoldersError!\n"
                    + ex.getMessage() + "\n" + ex.toString() + "\n");
        }

        return new LinkedList<>();
    }
}
