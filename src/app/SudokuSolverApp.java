package app;

import solver.ConcurrentSudokuSolver;
import solver.com.*;
import solver.utils.*;
import solver.SequentialSudokuSolver;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SudokuSolverApp {

    public static void main(String[] args) {
        String inputFile = "/home/buronsuave/IdeaProjects/SudokuSolver/res/benchmark.txt";
        List<String> puzzles = readPuzzlesFromFile(inputFile);

        String sequentialOutput = "/home/buronsuave/IdeaProjects/SudokuSolver/res/sequential_results.txt";
        String concurrentOutput = "/home/buronsuave/IdeaProjects/SudokuSolver/res/concurrent_results.txt";

        List<Long> sequentialTimes = new ArrayList<>();
        List<Long> concurrentTimes = new ArrayList<>();


        // Solve sequentially and write results
        System.out.println("Starting sequential solver");
        long sequentialTotalTime = solveSequentially(puzzles, sequentialTimes, sequentialOutput);
        System.out.println("Finished sequential solver");

        // Solve concurrently and write results
        System.out.println("Starting concurrent solvers");
        long concurrentTotalTime = solveConcurrently(puzzles, concurrentTimes, concurrentOutput);
        System.out.println("Finished concurrent solvers");

        // Print performance summary
        System.out.println("All puzzles solved. Results written to files.");
        System.out.println("Sequential performance: " + (1_000 * puzzles.size() / sequentialTotalTime) + " sudokus/second");
        System.out.println("Concurrent performance: " + (1_000 * puzzles.size() / concurrentTotalTime) + " sudokus/second");

        writeTimesToFile("/home/buronsuave/IdeaProjects/SudokuSolver/res/sequential_times.txt", sequentialTimes);
        writeTimesToFile("/home/buronsuave/IdeaProjects/SudokuSolver/res/concurrent_times.txt", concurrentTimes);

        System.out.println("All puzzles solved. Times written to sequential_times.txt and concurrent_times.txt.");
    }

    private static List<String> readPuzzlesFromFile(String fileName) {
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
        return puzzles;
    }

    private static void writeTimesToFile(String fileName, List<Long> times) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Long time : times) {
                writer.write(time.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to file " + fileName + ": " + e.getMessage());
        }
    }

    private static long solveSequentially(List<String> puzzles, List<Long> times, String outputFile) {
        SequentialSudokuSolver solver = new SequentialSudokuSolver(
                true,
                true,
                true,
                7);

        long totalTime = 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("index\tsolved?\tsolution\ttime\n");

            for (int i = 0; i < puzzles.size(); i++) {
                System.out.println("Solving sequential puzzle " + (i + 1) + " of " + puzzles.size());

                Board board = SudokuParser.parseBoard(puzzles.get(i));
                long startTime = System.currentTimeMillis();
                boolean solved = solver.solve(board);
                long endTime = System.currentTimeMillis();

                long elapsedTime = endTime - startTime;
                times.add(elapsedTime);
                totalTime += elapsedTime;

                // Write result to file
                writer.write(String.format("%d\t%s\t%s\t%d\n", i, solved, SudokuParser.boardToString(board), elapsedTime));
            }
        } catch (IOException e) {
            System.err.println("Error writing to sequential results file: " + e.getMessage());
        }
        return totalTime;
    }

    private static long solveConcurrently(List<String> puzzles, List<Long> times, String outputFile) {
        long totalTime = 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("index\tsolved?\tsolution\ttime\n");

            for (int i = 0; i < puzzles.size(); i++) {
                System.out.println("Solving concurrent puzzle " + (i + 1) + " of " + puzzles.size());

                Board board = SudokuParser.parseBoard(puzzles.get(i));
                ConcurrentSudokuSolver solver = new ConcurrentSudokuSolver(
                        true,
                        false,
                        true,
                        7);

                long startTime = System.currentTimeMillis();
                boolean solved = solver.solve(board);
                long endTime = System.currentTimeMillis();

                long elapsedTime = endTime - startTime;
                times.add(elapsedTime);
                totalTime += elapsedTime;

                // Write result to file
                writer.write(String.format("%d\t%s\t%s\t%d\n", i, solved, SudokuParser.boardToString(board), elapsedTime));
            }
        } catch (IOException e) {
            System.err.println("Error writing to sequential results file: " + e.getMessage());
        }
        return totalTime;
    }

    static class SolverResult {
        final int index;
        final boolean solved;
        final String solution;
        final long time;

        SolverResult(int index, boolean solved, String solution, long time) {
            this.index = index;
            this.solved = solved;
            this.solution = solution;
            this.time = time;
        }
    }
}
