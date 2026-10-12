package gui.swingimp;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.util.function.Consumer;

public class MainFrame extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String CONFIRM = "confirm";
    private static final String RUNNING = "running...";

    private final transient Consumer<String> onSubmit;

    private final JTextArea queryArea = new JTextArea(20, 40);
    private final JTable resultTable = new JTable();
    private final JButton confirmButton = new JButton(CONFIRM);

    public MainFrame(TableModel tableModel, Consumer<String> onSubmit) {
        this.onSubmit = onSubmit;
        initialise(tableModel);
    }

    public void setRunning(boolean running) {
        confirmButton.setEnabled(!running);
        confirmButton.setText(running ? RUNNING : CONFIRM);
    }

    private void initialise(TableModel tableModel) {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Database app");

        queryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, queryArea.getFont().getSize()));
        queryArea.setLineWrap(true);
        queryArea.setWrapStyleWord(true);

        resultTable.setModel(tableModel);
        resultTable.setAutoCreateRowSorter(true);
        resultTable.setPreferredScrollableViewportSize(new Dimension(500, 400));
        resultTable.setFillsViewportHeight(true);

        confirmButton.addActionListener(e -> onSubmit.accept(queryArea.getText()));

        JSplitPane splitPane =
            new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(queryArea),
                new JScrollPane(resultTable));
        splitPane.setResizeWeight(0.4);

        add(new JLabel("Enter your query: "), BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(confirmButton, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
