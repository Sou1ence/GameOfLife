package com.apokalist.gameoflife2;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Board extends Canvas {

    // CSS style colors (ignore it)
    private static final Color LIVE_CELL_COLOR = Color.web("#ca7b5d");
    private static final Color DEAD_CELL_COLOR = Color.web("#2c2c2a");
    private static final Color GRID_COLOR = Color.web("#4f4f4c");
    private static final Color BOARD_BACKGROUND = Color.web("#2c2c2a");

    public Board(double width, double height) {
        super(width, height);
        getStyleClass().add("board-canvas");
    }

    public void drawBoard(boolean[][] board) {
        GraphicsContext gc = this.getGraphicsContext2D();
        double cellWidth = getWidth() / board[0].length;
        double cellHeight = getHeight() / board.length;

        // Clear canvas with dark background
        gc.setFill(BOARD_BACKGROUND);
        gc.fillRect(0, 0, getWidth(), getHeight());

        // Draw cells
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                double x = col * cellWidth;
                double y = row * cellHeight;

                // Fill cell based on state
                if (board[row][col]) {
                    // Live cell - accent color
                    gc.setFill(LIVE_CELL_COLOR);
                    gc.fillRect(x, y, cellWidth, cellHeight);

                    gc.setFill(Color.web("#d4855f", 0.3));
                    gc.fillRect(x + 1, y + 1, cellWidth - 2, cellHeight - 2);
                } else {
                    gc.setFill(DEAD_CELL_COLOR);
                    gc.fillRect(x, y, cellWidth, cellHeight);
                }

                // Grid lines drowing
                gc.setStroke(GRID_COLOR);
                gc.setLineWidth(0.3);
                gc.strokeRect(x, y, cellWidth, cellHeight);
            }
        }
    }
}