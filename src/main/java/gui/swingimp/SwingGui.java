package gui.swingimp;

import app.AppCore;
import app.QueryOutcome;
import database.QueryResult;
import gui.IGui;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SwingGui implements IGui {

    private static final Logger LOGGER = Logger.getLogger(SwingGui.class.getName());

    private final AppCore core;
    private final TableModel tableModel = new TableModel();

    private MainFrame mainFrame;

    public SwingGui(AppCore core) {
        this.core = core;
    }

    @Override
    public void start() {
        this.mainFrame = new MainFrame(tableModel, this::submit);
    }

    private void submit(String source) {
        mainFrame.setRunning(true);
        new SwingWorker<QueryOutcome, Void>() {

            @Override
            protected QueryOutcome doInBackground() {
                return core.runQuery(source);
            }

            @Override
            protected void done() {
                mainFrame.setRunning(false);
                try {
                    show(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    LOGGER.log(Level.SEVERE, "Query execution failed", e);
                    tableModel.clear();
                    showMessage(String.valueOf(e.getCause()));
                }
            }
        }.execute();
    }

    private void show(QueryOutcome outcome) {
        switch (outcome) {
            case QueryOutcome.Success(QueryResult result) -> tableModel.setResult(result);
            case QueryOutcome.Invalid(List<String> errors) -> {
                tableModel.clear();
                showMessage(String.join("\n", errors));
            }
            case QueryOutcome.Failed(String message) -> {
                tableModel.clear();
                showMessage(message);
            }
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this.mainFrame, message);
    }
}
