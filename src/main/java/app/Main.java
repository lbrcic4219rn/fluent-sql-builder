package app;

import database.DatabaseConfig;
import gui.IGui;
import gui.swingimp.SwingGui;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private Main() {
    }

    public static void main(String[] args) {
        DatabaseConfig config;
        try {
            config = DatabaseConfig.fromEnvironment();
        } catch (IllegalStateException e) {
            LOGGER.log(Level.SEVERE, "Configuration error: {0}", e.getMessage());
            JOptionPane.showMessageDialog(
                null, e.getMessage(), "Configuration error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        AppCore core = new AppCore(config);
        Runtime.getRuntime().addShutdownHook(new Thread(core::shutdown));

        SwingUtilities.invokeLater(
            () -> {
                IGui gui = new SwingGui(core);
                gui.start();
            });
    }
}
