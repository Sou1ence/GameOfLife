/**
 * @author: Kostiantyn Feniuk
 *
 */

package com.apokalist.gameoflife2;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Objects;

public class GameOfLife extends Application {

    // Board config
    private static final int BOARD_WIDTH = 60;    // Config was taken fromm the book
    private static final int BOARD_HEIGHT = 40;
    private static final int CELL_SIZE = 12;

    // State variables
    private boolean[][] board = new boolean[BOARD_HEIGHT][BOARD_WIDTH];
    private Board gameBoard;
    private int generation = 0;
    private Timeline timeline;
    private boolean isRunning = false;

    // Game rules (default Conways Game of Life: 23/3)
    private String survivalRules = "23";  // neighbors for survival
    private String birthRules = "3";      // neighbors for birth

    // JFX UI
    private Label generationLabel;
    private TextField rulesField;
    private Button startStopButton;
    private Button resetButton;
    private Button randomButton;
    private Button clearButton;
    private Slider speedSlider;
    private Label speedLabel;

    @Override
    public void start(Stage primaryStage) {
        initializeBoard();
        setupUI();

        BorderPane root = new BorderPane();
        root.setCenter(gameBoard);
        root.setTop(createTopControls());
        root.setBottom(createBottomControls());

        // Apply dark theme styling from external CSS
        applyDarkTheme(root);

        Scene scene = new Scene(root, BOARD_WIDTH * CELL_SIZE + 20, BOARD_HEIGHT * CELL_SIZE + 120);
        scene.setFill(Color.web("#2c2c2a"));
        primaryStage.setTitle("Cellular Automaton - Game of Life");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        setupTimeline();
    }

    /**
     * Initializes the game board with default state and demo patterns
     */
    private void initializeBoard() {
        // Initial state - all cells dead
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                board[row][col] = false;
            }
        }

        // demonstration example
        createGlider(5, 5);
        createGlider(15, 15);
        createGlider(25, 25);
    }

    /**
     * Creates a glider pattern at specified coordinates
     * @param startRow Starting row position
     * @param startCol Starting column position
     */
    private void createGlider(int startRow, int startCol) {
        // "Glider" pattern - a moving figure
        int[][] glider = {
                {0, 1, 0},
                {0, 0, 1},
                {1, 1, 1}
        };

        // SKOŃCZONA PLANSZA Z CYKLICZNYMI WARUNKAMI BRZEGOWYMI
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int row = (startRow + i) % BOARD_HEIGHT;
                int col = (startCol + j) % BOARD_WIDTH;
                board[row][col] = (glider[i][j] == 1);
            }
        }
    }

    /**
     * Sets up the user interface components
     */
    private void setupUI() {
        gameBoard = new Board(BOARD_WIDTH * CELL_SIZE, BOARD_HEIGHT * CELL_SIZE);
        gameBoard.drawBoard(board);

        // Add mouse event handlers for cell editing
        gameBoard.setOnMouseClicked(this::handleMouseClick);
        gameBoard.setOnMouseDragged(this::handleMouseDrag);

        generationLabel = new Label("Generation: " + generation);
        generationLabel.getStyleClass().add("generation-label");

        // MOŻLIWOŚĆ ZADANIA REGUŁ GRY
        rulesField = new TextField("23/3");
        rulesField.setPromptText("Format: survival/birth");
        rulesField.setPrefWidth(100);
        rulesField.getStyleClass().add("rules-field");

        startStopButton = new Button("START");
        startStopButton.getStyleClass().add("primary-button");

        resetButton = new Button("RESET");
        resetButton.getStyleClass().add("secondary-button");

        randomButton = new Button("RANDOM");
        randomButton.getStyleClass().add("secondary-button");

        clearButton = new Button("CLEAR");
        clearButton.getStyleClass().add("secondary-button");

        speedSlider = new Slider(0.1, 5.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(1);
        speedSlider.setBlockIncrement(0.1);
        speedSlider.setPrefWidth(150);
        speedSlider.getStyleClass().add("speed-slider");

        speedLabel = new Label("Speed: 1.0x");
        speedLabel.getStyleClass().add("speed-label");

        setupEventHandlers();
    }

    /**
     * Sets up event handlers for UI components
     */
    private void setupEventHandlers() {
        startStopButton.setOnAction(e -> toggleSimulation());
        resetButton.setOnAction(e -> resetSimulation());
        randomButton.setOnAction(e -> randomizeBoard());
        clearButton.setOnAction(e -> clearBoard());

        rulesField.setOnAction(e -> updateRules());

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (timeline != null) {
                timeline.setRate(newVal.doubleValue());
            }
            speedLabel.setText("Speed: " + String.format("%.1fx", newVal.doubleValue()));
        });
    }

    /**
     * Creates the top control panel with rules configuration
     */
    private VBox createTopControls() {
        Label rulesLabel = new Label("Game Rules:");
        rulesLabel.getStyleClass().add("section-label");

        Label rulesExplain = new Label("Format: [survival_numbers]/[birth_numbers] (e.g.: 23/3, 34/34)");
        rulesExplain.getStyleClass().add("help-text");

        HBox rulesBox = new HBox(10);
        rulesBox.getChildren().addAll(rulesLabel, rulesField);

        VBox topBox = new VBox(5);
        topBox.setPadding(new Insets(10));
        topBox.getStyleClass().add("control-panel");
        topBox.getChildren().addAll(rulesBox, rulesExplain);

        return topBox;
    }

    /**
     * Creates the bottom control panel with simulation controls
     */
    private HBox createBottomControls() {
        HBox bottomControls = new HBox(15);
        bottomControls.setPadding(new Insets(10));
        bottomControls.getStyleClass().add("control-panel");
        bottomControls.getChildren().addAll(
                generationLabel,
                new Separator(),
                startStopButton,
                resetButton,
                randomButton,
                clearButton,
                new Separator(),
                speedLabel,
                speedSlider
        );

        return bottomControls;
    }

    /**
     * Sets up the animation timeline for automatic generation progression
     */
    private void setupTimeline() {
        timeline = new Timeline(new KeyFrame(Duration.millis(200), event -> {
            if (isRunning) {
                board = nextGeneration(board);
                generation++;
                generationLabel.setText("Generation: " + generation);
                gameBoard.drawBoard(board);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    /**
     * Handles mouse click events on the game board
     */
    private void handleMouseClick(MouseEvent event) {
        if (!isRunning) {
            toggleCell(event.getX(), event.getY());
        }
    }

    /**
     * Handles mouse drag events on the game board
     */
    private void handleMouseDrag(MouseEvent event) {
        if (!isRunning) {
            toggleCell(event.getX(), event.getY());
        }
    }

    /**
     * Toggles the state of a cell at the given coordinates
     */
    private void toggleCell(double x, double y) {
        int col = (int) (x / CELL_SIZE);
        int row = (int) (y / CELL_SIZE);

        if (row >= 0 && row < BOARD_HEIGHT && col >= 0 && col < BOARD_WIDTH) {
            board[row][col] = !board[row][col];
            gameBoard.drawBoard(board);
        }
    }

    /**
     * Toggles the simulation between running and paused states
     */
    private void toggleSimulation() {
        if (!isRunning) {
            updateRules(); // Updating rules before starting
            isRunning = true;
            startStopButton.setText("STOP");
            timeline.play();
        } else {
            isRunning = false;
            startStopButton.setText("START");
            timeline.pause();
        }
    }

    /**
     * Resets the simulation to initial state
     *
     */
    private void resetSimulation() {
        isRunning = false;
        startStopButton.setText("START");
        timeline.pause();
        generation = 0;
        generationLabel.setText("Generation: " + generation);
        initializeBoard();
        gameBoard.drawBoard(board);
    }

    /**
     * Randomizes the board with approximately 30% live cells
     */
    private void randomizeBoard() {
        if (!isRunning) {
            for (int row = 0; row < BOARD_HEIGHT; row++) {
                for (int col = 0; col < BOARD_WIDTH; col++) {
                    board[row][col] = Math.random() < 0.3;
                }
            }
            gameBoard.drawBoard(board);
        }
    }

    /**
     * Clears the board, setting all cells to dead state
     */
    private void clearBoard() {
        if (!isRunning) {
            // Complete clear without patterns
            for (int row = 0; row < BOARD_HEIGHT; row++) {
                for (int col = 0; col < BOARD_WIDTH; col++) {
                    board[row][col] = false;
                }
            }
            gameBoard.drawBoard(board);
        }
    }

    /**
     * Updates game rules from user input
     */
    private void updateRules() {
        String rules = rulesField.getText().trim();
        if (parseRules(rules)) {
            // Rules successfully updated
            rulesField.setStyle("-fx-border-color: #ca7b5d;");
        } else {
            // Error in rules format
            rulesField.setStyle("-fx-border-color: #ff6b6b;");
            showAlert("Error", "Invalid rules format!\nUse format: survival_numbers/birth_numbers\nExample: 23/3 or 34/34");
        }
    }

    /**
     * Parses and validates rule string format
     * @param rules Rule string in format "survival/birth"
     * @return true if rules are valid, false otherwise
     */
    private boolean parseRules(String rules) {
        try {
            if (!rules.contains("/")) {
                return false;
            }

            String[] parts = rules.split("/");
            if (parts.length != 2) {
                return false;
            }

            // Check that both parts contain only digits
            String survival = parts[0].trim();
            String birth = parts[1].trim();

            if (!survival.matches("\\d*") || !birth.matches("\\d*")) {
                return false;
            }

            // Check that digits are in range 0-8 (maximum 8 neighbors)
            for (char c : survival.toCharArray()) {
                if (c < '0' || c > '8') return false;
            }
            for (char c : birth.toCharArray()) {
                if (c < '0' || c > '8') return false;
            }

            survivalRules = survival;
            birthRules = birth;
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Shows an error alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Calculates the next generation based on current board state and rules
     * @param currentBoard Current state of the board
     * @return New board state representing next generation
     */
    private boolean[][] nextGeneration(boolean[][] currentBoard) {
        boolean[][] nextBoard = new boolean[BOARD_HEIGHT][BOARD_WIDTH];

        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                int liveNeighbors = countLiveNeighbors(currentBoard, row, col);

                if (currentBoard[row][col]) {
                    // Living cell
                    nextBoard[row][col] = survivalRules.contains(String.valueOf(liveNeighbors));
                } else {
                    // Dead cell
                    nextBoard[row][col] = birthRules.contains(String.valueOf(liveNeighbors));
                }
            }
        }
        return nextBoard;
    }

    /**
     * Counts live neighbors using toroidal boundary conditions (wrap-around edges)
     * @param board Current board state
     * @param row Cell row position
     * @param col Cell column position
     * @return Number of live neighboring cells
     */
    private int countLiveNeighbors(boolean[][] board, int row, int col) {
        int count = 0;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue; // Skip the cell itself

                // Toroidal boundary conditions (wrap-around)
                int neighborRow = (row + i + BOARD_HEIGHT) % BOARD_HEIGHT;
                int neighborCol = (col + j + BOARD_WIDTH) % BOARD_WIDTH;

                if (board[neighborRow][neighborCol]) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Applies dark theme styling to the application
     */
    private void applyDarkTheme(BorderPane root) {
        // Load external CSS file
        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
    }

    public static void main(String[] args) {
        launch(args);
    }
}