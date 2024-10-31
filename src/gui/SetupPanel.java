package gui;

import app.SudokuSolverGraphicApp;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class SetupPanel extends JPanel {
    private JCheckBox sequentialCheckbox, concurrentCheckbox;
    private JCheckBox eliminationStrategyCheckbox, nakedSingleStrategyCheckbox, backtrackingStrategyCheckbox;
    private JTextField bfsDepthField;
    private JCheckBox eliminationConcurrent, nakedSingleConcurrent, backtrackingConcurrent;
    private JTextField bfsDepthConcurrent;
    private JRadioButton benchmarkMode, singleSolveMode;
    private JTextField numTestsField;
    private SudokuPanel sudokuPanel;
    private JButton startButton;
    private JPanel numberTestsPanel;
    private SudokuSolverGraphicApp context;

    public SetupPanel(SudokuSolverGraphicApp context) {
        this.context = context;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(1200, 610));

        JPanel formPanel = new JPanel(new GridLayout(1, 3, 30, 10));

        formPanel.add(createSequentialSolverPanel());
        formPanel.add(createConcurrentSolverPanel());
        formPanel.add(createModeSelectionPanel());

        add(formPanel, BorderLayout.CENTER);
    }

    // Sequential Solver Panel
    private JPanel createSequentialSolverPanel() {
        JPanel sequentialPanel = new JPanel(new GridLayout(3, 1));
        sequentialCheckbox = new JCheckBox("Sequential Solver");
        sequentialCheckbox.addActionListener(e -> toggleSequentialOptions(sequentialCheckbox.isSelected()));
        sequentialCheckbox.setSelected(true);
        sequentialPanel.add(sequentialCheckbox);

        JPanel sequentialOptions = new JPanel(new GridLayout(4, 1, 5, 5));
        sequentialOptions.setBorder(new EmptyBorder(10, 10, 10, 10));  // Add padding inside the panel

        eliminationStrategyCheckbox = new JCheckBox("Enable Elimination Strategy");
        eliminationStrategyCheckbox.setSelected(true);
        nakedSingleStrategyCheckbox = new JCheckBox("Enable Naked Single Strategy");
        nakedSingleStrategyCheckbox.setSelected(true);
        backtrackingStrategyCheckbox = new JCheckBox("Enable Backtracking Sequential Strategy");
        backtrackingStrategyCheckbox.setSelected(true);  // Default enabled

        JPanel bfsDepthPanel = new JPanel(new GridLayout(1, 2));
        bfsDepthField = new JTextField();
        bfsDepthField.setText("5");
        bfsDepthPanel.add(new JLabel("BFS Depth (1-10)"));
        bfsDepthPanel.add(bfsDepthField);

        sequentialOptions.add(eliminationStrategyCheckbox);
        sequentialOptions.add(nakedSingleStrategyCheckbox);
        sequentialOptions.add(backtrackingStrategyCheckbox);
        sequentialOptions.add(bfsDepthPanel);
        sequentialPanel.add(sequentialOptions);

        JPanel startPanel = new JPanel(new BorderLayout());
        startButton = new JButton("Start");
        startButton.setPreferredSize(new Dimension(120, 40));
        startButton.addActionListener(e -> validateAndStart());
        startPanel.add(startButton, BorderLayout.SOUTH);
        sequentialPanel.add(startPanel);

        toggleSequentialOptions(true);  // Initially disabled
        return sequentialPanel;
    }

    // Concurrent Solver Panel
    private JPanel createConcurrentSolverPanel() {
        JPanel concurrentPanel = new JPanel(new GridLayout(3, 1));
        concurrentCheckbox = new JCheckBox("Concurrent Solver");
        concurrentCheckbox.addActionListener(e -> toggleConcurrentOptions(concurrentCheckbox.isSelected()));
        concurrentCheckbox.setSelected(true);
        concurrentPanel.add(concurrentCheckbox);

        JPanel concurrentOptions = new JPanel(new GridLayout(4, 1, 5, 5));
        concurrentOptions.setBorder(new EmptyBorder(10, 10, 10, 10));  // Add padding inside the panel

        eliminationConcurrent = new JCheckBox("Enable Elimination Strategy");
        eliminationConcurrent.setSelected(true);
        nakedSingleConcurrent = new JCheckBox("Enable Naked Single Strategy");
        backtrackingConcurrent = new JCheckBox("Enable Backtracking Sequential Strategy");
        backtrackingConcurrent.setSelected(true);  // Default enabled

        JPanel bfsDepthPanel = new JPanel(new GridLayout(1, 2));
        bfsDepthConcurrent = new JTextField();
        bfsDepthConcurrent.setText("5");
        bfsDepthPanel.add(new JLabel("BFS Depth (1-10)"));
        bfsDepthPanel.add(bfsDepthConcurrent);

        concurrentOptions.add(eliminationConcurrent);
        concurrentOptions.add(nakedSingleConcurrent);
        concurrentOptions.add(backtrackingConcurrent);
        concurrentOptions.add(bfsDepthPanel);
        concurrentPanel.add(concurrentOptions);

        toggleConcurrentOptions(true);  // Initially disabled
        return concurrentPanel;
    }

    // Mode Selection Panel
    private JPanel createModeSelectionPanel() {
        JPanel modeSelectionPanel = new JPanel(new BorderLayout());

        JPanel configPanel = new JPanel(new GridLayout(2, 1));
        numberTestsPanel = new JPanel(new GridLayout(1, 2));
        numTestsField = new JTextField();
        numTestsField.setText("1000");
        numberTestsPanel.add(new JLabel("Number of Tests (1-10000):"));
        numberTestsPanel.add(numTestsField);

        // Radio buttons for mode selection
        benchmarkMode = new JRadioButton("Benchmark Mode");
        singleSolveMode = new JRadioButton("Single Solve Mode");
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(benchmarkMode);
        modeGroup.add(singleSolveMode);
        modeGroup.setSelected(benchmarkMode.getModel(), true);

        JPanel radioPanel = new JPanel(new GridLayout(2, 1));
        radioPanel.setBorder(new EmptyBorder(10, 10, 10, 10));  // Add padding inside the panel
        radioPanel.add(benchmarkMode);
        radioPanel.add(singleSolveMode);

        configPanel.add(radioPanel);
        configPanel.add(numberTestsPanel);

        benchmarkMode.addActionListener(e -> switchMode("Benchmark"));
        singleSolveMode.addActionListener(e -> switchMode("SingleSolve"));
        sudokuPanel = new SudokuPanel();

        modeSelectionPanel.add(configPanel, BorderLayout.NORTH);
        modeSelectionPanel.add(sudokuPanel, BorderLayout.CENTER);
        switchMode("Benchmark");

        return modeSelectionPanel;
    }

    // Switch between Benchmark and Single Solve modes
    private void switchMode(String mode) {
        sudokuPanel.setVisible(mode == "SingleSolve");
        numberTestsPanel.setVisible(mode == "Benchmark");
    }

    // Enable/Disable Sequential Solver options
    private void toggleSequentialOptions(boolean enabled) {
        eliminationStrategyCheckbox.setEnabled(enabled);
        nakedSingleStrategyCheckbox.setEnabled(enabled);
        backtrackingStrategyCheckbox.setEnabled(enabled);
        bfsDepthField.setEnabled(enabled);
    }

    // Enable/Disable Concurrent Solver options
    private void toggleConcurrentOptions(boolean enabled) {
        eliminationConcurrent.setEnabled(enabled);
        nakedSingleConcurrent.setEnabled(enabled);
        backtrackingConcurrent.setEnabled(enabled);
        bfsDepthConcurrent.setEnabled(enabled);
    }

    // Validate inputs and start the process
    private void validateAndStart() {
        try {
            int bfsDepthS = 5;
            int bfsDepthC = 5;
            int numTests = 1000;

            if (sequentialCheckbox.isSelected()) {
                bfsDepthS = Integer.parseInt(bfsDepthField.getText());
                if (bfsDepthS < 1 || bfsDepthS > 10) {
                    throw new NumberFormatException("Sequential BFS Depth must be between 1 and 10.");
                }
            }
            if (concurrentCheckbox.isSelected()) {
                bfsDepthC = Integer.parseInt(bfsDepthConcurrent.getText());
                if (bfsDepthC < 1 || bfsDepthC > 10) {
                    throw new NumberFormatException("Concurrent BFS Depth must be between 1 and 10.");
                }
            }
            if (benchmarkMode.isSelected()) {
                numTests = Integer.parseInt(numTestsField.getText());
                if (numTests < 1 || numTests > 10000) {
                    throw new NumberFormatException("Number of tests must be between 1 and 10000.");
                }
            }

            // Start execution here
            context.startExecution(
                    sequentialCheckbox.isSelected(),
                    bfsDepthS,
                    eliminationStrategyCheckbox.isSelected(),
                    nakedSingleStrategyCheckbox.isSelected(),
                    backtrackingStrategyCheckbox.isSelected(),
                    concurrentCheckbox.isSelected(),
                    bfsDepthC,
                    eliminationConcurrent.isSelected(),
                    nakedSingleConcurrent.isSelected(),
                    backtrackingConcurrent.isSelected(),
                    singleSolveMode.isSelected(),
                    numTests,
                    sudokuPanel.generateSudokuString()
            );

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
