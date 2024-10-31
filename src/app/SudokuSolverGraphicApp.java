package app;

import com.google.gson.JsonArray;
import gui.LogPanel;
import gui.ResultsPanel;
import gui.SetupPanel;
import javax.swing.*;
import java.awt.*;

public class SudokuSolverGraphicApp extends JFrame {
    private SetupPanel setupPanel;
    private LogPanel logPanel;
    private ResultsPanel resultsPanel;

    public SudokuSolverGraphicApp() {
        setTitle("Sudoku Solver Graphic App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Add the SetupPanel to the frame
        setupPanel = new SetupPanel(this);
        add(setupPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SudokuSolverGraphicApp::new);
    }

    public void startExecution(
            boolean enableSequential,
            int bfsDepthS,
            boolean enableEliminationS,
            boolean enableNakedS,
            boolean enableBacktrackingS,
            boolean enableConcurrent,
            int bfsDepthC,
            boolean enableEliminationC,
            boolean enableNakedC,
            boolean enableBacktrackingC,
            boolean isSingleTest,
            int numberTests,
            String test
    ) {
        setupPanel.setVisible(false);
        remove(setupPanel);

        logPanel = new LogPanel(
                this,
                enableSequential,
                bfsDepthS,
                enableEliminationS,
                enableNakedS,
                enableBacktrackingS,
                enableConcurrent,
                bfsDepthC,
                enableEliminationC,
                enableNakedC,
                enableBacktrackingC,
                isSingleTest,
                numberTests,
                test);

        setSize(new Dimension(600, 300));
        add(logPanel);
        setLocationRelativeTo(null);
        logPanel.run();
    }

    public void showResults(JsonArray sequentialResults, JsonArray concurrentResults) {
        logPanel.setVisible(false);
        resultsPanel = new ResultsPanel(this, sequentialResults, concurrentResults);
        setSize(new Dimension(1600, 525));
        add(resultsPanel);
    }

    public void restart() {
        remove(logPanel);
        remove(resultsPanel);
        setLayout(new BorderLayout());
        setupPanel = new SetupPanel(this);
        add(setupPanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
