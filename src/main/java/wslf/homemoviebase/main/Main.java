package wslf.homemoviebase.main;

import wslf.homemoviebase.forms.MainForm;
import wslf.homemoviebase.logic.Worker;

/**
 *
 * @author Wsl_F
 */
public class Main {

    public static void main(String[] args) {

        java.awt.EventQueue.invokeLater(() -> {
            Worker worker = new Worker();
            MainForm mainForm = new MainForm(worker);
            mainForm.setVisible(true);
        });

    }
}
