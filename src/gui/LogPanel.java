package gui;

import app.SudokuSolverGraphicApp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import solver.ConcurrentSudokuSolver;
import solver.SequentialSudokuSolver;
import solver.com.Board;
import solver.utils.SudokuParser;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LogPanel extends JPanel {
    protected SudokuSolverGraphicApp context;
    protected JTextArea logArea;
    protected boolean enableSequential;
    protected int bfsDepthS;
    protected boolean enableEliminationS;
    protected boolean enableNakedS;
    protected boolean enableBacktrackingS;
    protected boolean enableConcurrent;
    protected int bfsDepthC;
    protected boolean enableEliminationC;
    protected boolean enableNakedC;
    protected boolean enableBacktrackingC;
    protected boolean isSingleTest;
    protected int numberTests;
    protected String test;
    private JsonArray sequentialResults;
    private JsonArray concurrentResults;

    public LogPanel(
            SudokuSolverGraphicApp context,
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
            String test) {

        this.context = context;
        this.enableSequential = enableSequential;
        this.bfsDepthS = bfsDepthS;
        this.enableEliminationS = enableEliminationS;
        this.enableNakedS = enableNakedS;
        this.enableBacktrackingS = enableBacktrackingS;
        this.enableConcurrent = enableConcurrent;
        this.bfsDepthC = bfsDepthC;
        this.enableEliminationC = enableEliminationC;
        this.enableNakedC = enableNakedC;
        this.enableBacktrackingC = enableBacktrackingC;
        this.isSingleTest = isSingleTest;
        this.numberTests = numberTests;
        this.test = test;

        setLayout(new BorderLayout());
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 20));

        add(new JScrollPane(logArea), BorderLayout.CENTER);
        setVisible(true);
    }

    public void run() {
        String inputFile = "/home/buronsuave/IdeaProjects/SudokuSolver/res/benchmark.txt";
        List<String> puzzles;
        if (!isSingleTest) {
            puzzles = readPuzzlesFromFile(inputFile, numberTests);
        } else { // Single test
            puzzles = new ArrayList<>();
            puzzles.add(test);
        }

        new SolverWorker(
                enableSequential,
                enableConcurrent,
                puzzles,
                new ArrayList<>(),
                "/home/buronsuave/IdeaProjects/SudokuSolver/res/sequential_results.json",
                new ArrayList<>(),
                "/home/buronsuave/IdeaProjects/SudokuSolver/res/concurrent_results.json"
        ).execute();
    }

    private class SolverWorker extends SwingWorker<Void, String> {
        private boolean enableSequential, enableConcurrent;
        private List<String> puzzles;
        private List<Long> sequentialTimes, concurrentTimes;
        private String sequentialOutput, concurrentOutput;

        public SolverWorker(
                boolean enableSequential, boolean enableConcurrent,
                List<String> puzzles, List<Long> sequentialTimes, String sequentialOutput, List<Long> concurrentTimes, String concurrentOutput) {
            this.enableSequential = enableSequential;
            this.enableConcurrent = enableConcurrent;
            this.puzzles = puzzles;
            this.sequentialTimes = sequentialTimes;
            this.sequentialOutput = sequentialOutput;
            this.concurrentTimes = concurrentTimes;
            this.concurrentOutput = concurrentOutput;
        }

        @Override
        protected Void doInBackground() {
            if (enableSequential) {
                publish("Starting sequential solver...\n");
                long totalTime = solveSequentially(
                        puzzles,
                        sequentialTimes,
                        sequentialOutput
                );
                publish("Finished sequential solver in " + totalTime + " ms.\n");
            }

            if (enableConcurrent) {
                publish("Starting concurrent solver...\n");
                long totalTime = solveConcurrently(
                        puzzles,
                        concurrentTimes,
                        concurrentOutput
                );
                publish("Finished concurrent solver in " + totalTime + " ms.\n");
            }

            context.showResults(sequentialResults, concurrentResults);
            return null;
        }

        @Override
        protected void process(List<String> chunks) {
            for (String log : chunks) {
                logArea.append(log);
            }
        }

        @Override
        protected void done() {
            logArea.append("All puzzles solved.\n");
            context.repaint();
        }
    }

    private List<String> readPuzzlesFromFile(String fileName, int numberTests) {
        List<String> puzzles = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    puzzles.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading input file: " + e.getMessage());
            System.exit(1);
        }

        List<String> realList = new ArrayList<>();
        for (int i = 0; i < numberTests; ++i) {
            realList.add(puzzles.get(i % puzzles.size()));
        }

        return realList;
    }

    private long solveSequentially(
            List<String> puzzles,
            List<Long> times,
            String outputFile
            ) {
        SequentialSudokuSolver solver = new SequentialSudokuSolver(
                this.enableEliminationS,
                this.enableNakedS,
                this.enableBacktrackingS,
                this.bfsDepthS);

        long totalTime = 0;
        sequentialResults = new JsonArray();

        for (int i = 0; i < puzzles.size(); i++) {
            System.out.print("Solving sequential puzzle " + (i + 1) + " of " + puzzles.size() + "\n");
            JsonObject object = new JsonObject();

            Board board = SudokuParser.parseBoard(puzzles.get(i));
            long startTime = System.currentTimeMillis();
            boolean solved = solver.solve(board);
            long endTime = System.currentTimeMillis();

            long elapsedTime = endTime - startTime;
            times.add(elapsedTime);
            totalTime += elapsedTime;

            object.addProperty("index", i);
            object.addProperty("solved", solved);
            object.addProperty("puzzle", puzzles.get(i));
            object.addProperty("solution", SudokuParser.boardToString(board));
            object.addProperty("time", elapsedTime);
            sequentialResults.add(object);
        }

        writeResultToJsonFile(outputFile, sequentialResults);
        return totalTime;
    }

    private long solveConcurrently(
            List<String> puzzles,
            List<Long> times,
            String outputFile
            ) {

        long totalTime = 0;
        concurrentResults = new JsonArray();

        for (int i = 0; i < puzzles.size(); i++) {
            System.out.print("Solving concurrent puzzle " + (i + 1) + " of " + puzzles.size() + "\n");
            JsonObject object = new JsonObject();

            Board board = SudokuParser.parseBoard(puzzles.get(i));
            ConcurrentSudokuSolver solver = new ConcurrentSudokuSolver(
                    this.enableEliminationC,
                    this.enableNakedC,
                    this.enableBacktrackingC,
                    this.bfsDepthC);

            long startTime = System.currentTimeMillis();
            boolean solved = solver.solve(board);
            long endTime = System.currentTimeMillis();

            long elapsedTime = endTime - startTime;
            times.add(elapsedTime);
            totalTime += elapsedTime;

            object.addProperty("index", i);
            object.addProperty("solved", solved);
            object.addProperty("puzzle", puzzles.get(i));
            object.addProperty("solution", SudokuParser.boardToString(board));
            object.addProperty("time", elapsedTime);
            concurrentResults.add(object);
        }

        writeResultToJsonFile(outputFile, concurrentResults);
        return totalTime;
    }

    private void writeResultToJsonFile(String filename, JsonArray array) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(array, writer);
        } catch (IOException e) {
            System.err.println("Error writing to JSON file: " + e.getMessage());
        }
    }

}
