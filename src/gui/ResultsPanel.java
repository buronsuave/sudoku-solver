package gui;

import app.SudokuSolverGraphicApp;
import com.google.gson.JsonArray;
import javax.swing.*;

import com.google.gson.JsonObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;

public class ResultsPanel extends JPanel {
    private JsonArray sequentialResults;
    private JsonArray concurrentResults;

    private ChartPanel graphPanel;
    private int currentGraph = 0;  // 0 for accumulative, 1 for index-wise comparison
    private JTextField indexField;
    private JLabel totalSequentialTimeLabel, totalConcurrentTimeLabel,
            sequentialRateLabel, concurrentRateLabel,
            sequentialTimeLabel, concurrentTimeLabel;
    private SudokuPanel puzzlePanel, solutionPanel;
    private SudokuSolverGraphicApp context;

    public ResultsPanel(SudokuSolverGraphicApp context, JsonArray sequentialResults, JsonArray concurrentResults) {
        this.sequentialResults = sequentialResults;
        this.concurrentResults = concurrentResults;
        this.context = context;

        setLayout(new GridLayout(1, 4)); // 4 columns
        // === Column 1: Graph and Button ===
        JPanel graphColumn = new JPanel(new BorderLayout());
        JButton switchGraphButton = new JButton("Switch Graph");
        switchGraphButton.addActionListener(e -> switchGraph());

        graphPanel = new ChartPanel(createAccumulativeGraph());
        graphColumn.add(graphPanel, BorderLayout.CENTER);
        graphColumn.add(switchGraphButton, BorderLayout.SOUTH);


        // === Column 2: Details and Puzzle Selection ===
        JPanel detailsColumn = new JPanel(new GridLayout(8, 2));
        totalSequentialTimeLabel = new JLabel();
        totalConcurrentTimeLabel = new JLabel();
        sequentialRateLabel = new JLabel();
        concurrentRateLabel = new JLabel();
        sequentialTimeLabel = new JLabel();
        concurrentTimeLabel = new JLabel();

        indexField = new JTextField();
        indexField.setText("0");
        JButton loadButton = new JButton("Load Puzzle");
        loadButton.addActionListener(e -> loadPuzzle());

        detailsColumn.add(new JLabel("Total Sequential Time:"));
        detailsColumn.add(totalSequentialTimeLabel);
        detailsColumn.add(new JLabel("Total Concurrent Time:"));
        detailsColumn.add(totalConcurrentTimeLabel);
        detailsColumn.add(new JLabel("Sudokus/sec Sequential:"));
        detailsColumn.add(sequentialRateLabel);
        detailsColumn.add(new JLabel("Sudokus/sec Concurrent:"));
        detailsColumn.add(concurrentRateLabel);
        detailsColumn.add(new JLabel("Sequential Time (Selected):"));
        detailsColumn.add(sequentialTimeLabel);
        detailsColumn.add(new JLabel("Concurrent Time (Selected):"));
        detailsColumn.add(concurrentTimeLabel);
        detailsColumn.add(new JLabel("Select Puzzle Index:"));
        detailsColumn.add(indexField);
        detailsColumn.add(loadButton);

        JButton restartButton = new JButton("New Experiment");
        restartButton.addActionListener(actionEvent -> {
            context.restart();
        });
        detailsColumn.add(restartButton);

        // === Column 3: Puzzle Panel ===
        puzzlePanel = new SudokuPanel();
        JPanel puzzleColumn = new JPanel(new BorderLayout());
        puzzleColumn.add(puzzlePanel, BorderLayout.CENTER);

        // === Column 4: Solution Panel ===
        solutionPanel = new SudokuPanel();
        JPanel solutionColumn = new JPanel(new BorderLayout());
        solutionColumn.add(solutionPanel, BorderLayout.CENTER);

        // Add all columns to the main panel
        add(graphColumn);
        add(detailsColumn);
        add(puzzleColumn);
        add(solutionColumn);

        // Load initial data

        updateDetails();
        loadPuzzle();

    }

    private void switchGraph() {
        currentGraph = 1 - currentGraph;  // Toggle between 0 and 1
        graphPanel.setChart(currentGraph == 0 ? createAccumulativeGraph() : createIndexGraph());
    }

    private JFreeChart createAccumulativeGraph() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        long sequentialSum = 0, concurrentSum = 0;

        for (int i = 0; i < sequentialResults.size(); ++i) {
            sequentialSum += sequentialResults.get(i).getAsJsonObject().get("time").getAsLong();
            concurrentSum += concurrentResults.get(i).getAsJsonObject().get("time").getAsLong();
            dataset.addValue(sequentialSum, "Sequential", String.valueOf(i));
            dataset.addValue(concurrentSum, "Concurrent", String.valueOf(i));
        }

        return ChartFactory.createLineChart(
                "Accumulative Times", "Index", "Time (ms)", dataset);
    }

    private JFreeChart createIndexGraph() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < sequentialResults.size(); ++i) {
            dataset.addValue(sequentialResults.get(i).getAsJsonObject().get("time").getAsLong(), "Sequential", String.valueOf(i));
            dataset.addValue(concurrentResults.get(i).getAsJsonObject().get("time").getAsLong(), "Concurrent", String.valueOf(i));
        }

        return ChartFactory.createBarChart(
                "Index-wise Comparison", "Index", "Time (ms)", dataset);
    }

    private void updateDetails() {
        long totalSequentialTime = 0;
        long totalConcurrentTime = 0;

        for (int i = 0; i < sequentialResults.size(); ++i) {
            totalSequentialTime += sequentialResults.get(i).getAsJsonObject().get("time").getAsLong();
            totalConcurrentTime += concurrentResults.get(i).getAsJsonObject().get("time").getAsLong();
        }

        totalSequentialTimeLabel.setText(totalSequentialTime + " ms");
        totalConcurrentTimeLabel.setText(totalConcurrentTime + " ms");
        sequentialRateLabel.setText(String.format("%.2f", sequentialResults.size() * 1000.0 / totalSequentialTime));
        concurrentRateLabel.setText(String.format("%.2f", concurrentResults.size() * 1000.0 / totalConcurrentTime));

    }

    private void loadPuzzle() {
        int index = Integer.parseInt(indexField.getText());

        if (index >= 0 && index < sequentialResults.size()) {
            JsonObject sequentialResult = sequentialResults.get(index).getAsJsonObject();
            JsonObject concurrentResult = concurrentResults.get(index).getAsJsonObject();

            sequentialTimeLabel.setText(sequentialResult.get("time") + " ms");
            concurrentTimeLabel.setText(concurrentResult.get("time") + " ms");

            puzzlePanel.loadSudoku(sequentialResult.get("puzzle").getAsString());
            solutionPanel.loadSudoku(sequentialResult.get("solution").getAsString());
        } else {
            JOptionPane.showMessageDialog(this, "Invalid index!", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }
}
