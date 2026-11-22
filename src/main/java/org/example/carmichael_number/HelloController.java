package org.example.carmichael_number;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class HelloController {
    @FXML private TextField upperLimitField;
    @FXML private TextField threadCountField;
    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private Label errorLabel;
    @FXML private Label statusLabel;
    @FXML private FlowPane workerPane;
    @FXML private TextArea resultsArea;
    @FXML private Label carmichaelLabel;
    @FXML private Label nonCarmichaelLabel;
    @FXML private Label totalLabel;

    private SearchRun activeRun;

    @FXML
    private void startSearch() {
        if (activeRun != null) {
            return;
        }
        int limit = positiveInteger(upperLimitField);
        int count = positiveInteger(threadCountField);
        if (limit == 0) {
            setError("Upper limit must be a positive 32-bit integer.");
            return;
        }
        if (count == 0) {
            setError("Thread count must be a positive 32-bit integer.");
            return;
        }
        if (count > Math.min(limit, 64)) {
            setError("Maximum thread count for this upper limit is " + Math.min(limit, 64) + ".");
            return;
        }

        setError("");
        clearResults();
        workerPane.getChildren().clear();
        SearchRun run = new SearchRun(limit);
        activeRun = run;
        setRunning(true);
        statusLabel.setText("Finished threads: 0 of " + count);
        for (var range : CarmichaelSearch.partition(limit, count)) {
            int number = run.workers.size() + 1;
            SearchTask task = new SearchTask(range);
            run.workers.add(task);
            addWorkerPanel(run, task, number);
            Thread thread = new Thread(task, "Carmichael worker " + number);
            thread.setDaemon(true);
            thread.start();
        }
    }

    private static int positiveInteger(TextField field) {
        try {
            return Math.max(0, Integer.parseInt(field.getText().strip()));
        } catch (NumberFormatException invalid) {
            return 0;
        }
    }

    private void setError(String text) {
        errorLabel.setText(text);
        errorLabel.setManaged(!text.isEmpty());
        errorLabel.setVisible(!text.isEmpty());
    }

    private void addWorkerPanel(SearchRun run, SearchTask task, int number) {
        var range = task.range;
        ProgressIndicator progress = new ProgressIndicator(0);
        progress.setPrefSize(40, 40);
        progress.setMaxSize(40, 40);
        Label state = new Label("Queued");
        Label interval = new Label(range.start() + " to " + range.end());
        interval.setMaxWidth(Double.MAX_VALUE);
        interval.setWrapText(true);
        VBox panel = new VBox(6, new Label("Thread " + number), progress, interval,
                new Label(range.size() + (range.size() == 1 ? " number" : " numbers")), state);
        panel.setPrefWidth(150);
        panel.setAlignment(Pos.TOP_CENTER);
        workerPane.getChildren().add(panel);

        task.progressProperty().addListener((observable, previous, value) ->
                progress.setProgress(value.doubleValue()));
        task.setOnRunning(event -> state.setText("Running"));
        task.setOnCancelled(event -> state.setText("Stopped"));
        task.setOnFailed(event -> {
            state.setText("Failed");
            if (activeRun == run) {
                stopSearch();
                statusLabel.setText("Search failed on thread " + number + ".");
            }
        });
        task.setOnSucceeded(event -> {
            if (activeRun == run) {
                progress.setProgress(1);
                state.setText("Done");
                workerFinished(run);
            }
        });
    }

    private void workerFinished(SearchRun run) {
        run.finished++;
        String finished = "Finished threads: " + run.finished + " of " + run.workers.size();
        if (run.finished < run.workers.size()) {
            statusLabel.setText(finished);
            return;
        }

        StringJoiner numbers = new StringJoiner(", ");
        int matches = 0;
        for (SearchTask worker : run.workers) {
            List<Integer> found = worker.getValue();
            matches += found.size();
            found.forEach(n -> numbers.add(n.toString()));
        }
        resultsArea.setText(numbers.toString());
        setTotals(matches, run.limit);
        statusLabel.setText("Complete. Checked all " + run.limit + " numbers from 1 to "
                + run.limit + ". " + finished + ".");
        activeRun = null;
        setRunning(false);
    }

    @FXML
    void stopSearch() {
        SearchRun run = activeRun;
        if (run == null) {
            return;
        }
        activeRun = null;
        run.workers.forEach(task -> task.cancel(true));
        clearResults();
        statusLabel.setText("Stopped. Finished threads: " + run.finished + " of " + run.workers.size());
        setRunning(false);
    }

    private void setRunning(boolean running) {
        upperLimitField.setDisable(running);
        threadCountField.setDisable(running);
        startButton.setDisable(running);
        stopButton.setDisable(!running);
    }

    private void clearResults() {
        resultsArea.clear();
        setTotals(0, 0);
    }

    private void setTotals(int matches, int checked) {
        carmichaelLabel.setText("Carmichael numbers: " + matches);
        nonCarmichaelLabel.setText("Non-Carmichael numbers: " + (checked - matches));
        totalLabel.setText("Total checked: " + checked);
    }

    private static final class SearchRun {
        final int limit;
        final List<SearchTask> workers = new ArrayList<>();
        int finished;

        SearchRun(int limit) {
            this.limit = limit;
        }
    }

    private static final class SearchTask extends Task<List<Integer>> {
        final CarmichaelSearch.Range range;

        SearchTask(CarmichaelSearch.Range range) {
            this.range = range;
        }

        @Override
        protected List<Integer> call() {
            List<Integer> matches = new ArrayList<>();
            int checked = 0;
            long lastUpdate = System.nanoTime();
            for (long n = range.start(); n <= range.end(); n++) {
                if (isCancelled()) {
                    break;
                }
                if (CarmichaelSearch.isCarmichael((int) n)) {
                    matches.add((int) n);
                }
                checked++;
                if ((checked & 255) == 0 && System.nanoTime() - lastUpdate >= 50_000_000L) {
                    updateProgress(checked, range.size());
                    lastUpdate = System.nanoTime();
                }
            }
            updateProgress(checked, range.size());
            return matches;
        }
    }
}
