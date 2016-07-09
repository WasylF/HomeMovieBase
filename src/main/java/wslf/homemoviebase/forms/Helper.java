package wslf.homemoviebase.forms;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Wsl_F
 */
public class Helper {

    /**
     * open path in explorer or return JOptionPane with Error message
     *
     * @param path path that should be opened
     * @param jFrame form that requested this action
     */
    static void openPathInExplorer(Path path, JFrame jFrame) {
        if (!path.toString().isEmpty()
                && Files.exists(path) && !Files.isDirectory(path)) {
            path = path.getParent();
        }
        openPath(path, jFrame);
    }

    /**
     * open directory or file (if possible) or return JOptionPane with Error
     * message
     *
     * @param path path that should be opened
     * @param jFrame form that requested this action
     */
    static void openPath(Path path, JFrame jFrame) {
        if (!path.toString().isEmpty() && Files.exists(path)) {
            try {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(path.toFile());
            } catch (IOException ex) {
                Logger.getLogger(AddEventForm.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            JOptionPane.showMessageDialog(jFrame, "Данный путь не существует!",
                    "Ошибка!", JOptionPane.ERROR_MESSAGE);
        }

    }

    /**
     * open JFileChooser to allow user select folder
     *
     * @param caption caption for JFileChooser
     * @return path for selected folder or null
     */
    static String chooseDirectory(String caption) {
        return chooseDirectory(caption, new java.io.File(".").toString());
    }

    /**
     * open JFileChooser to allow user select folder
     *
     * @param caption caption for JFileChooser
     * @param curDirectory start directory for JFileChooser
     * @return path for selected folder or null
     */
    static String chooseDirectory(String caption, String curDirectory) {
        try {
            JFileChooser chooser = new JFileChooser();
            Path startDirectory = Paths.get(curDirectory);
            chooser.setCurrentDirectory(startDirectory.toFile());
            chooser.setDialogTitle(caption);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().toString();
                return path;
            }
        } catch (Exception ex) {
            System.err.println("Error while choosing directory\n"
                    + ex.getMessage() + "\n" + ex.toString());
        }
        return null;
    }

}
