package edu.iastate.cs472.proj1;

import java.util.ArrayList;

/**
 * An object of this class holds data about a game of checkers. It knows what
 * kind of piece is on each square of the checkerboard. Note that RED moves "up"
 * the board (i.e. row number decreases) while BLACK moves "down" the board
 * (i.e. row number increases). Methods are provided to return lists of
 * available legal moves.
 * 
 * @author Cole Patton
 */
public class CheckersData {

    /*
     * The following constants represent the possible contents of a square on the
     * board. The constants RED and BLACK also represent players in the game.
     */

    static final int EMPTY = 0, RED = 1, RED_KING = 2, BLACK = 3, BLACK_KING = 4;

    int[][] board; // board[r][c] is the contents of row r, column c.

    /**
     * Constructor. Create the board and set it up for a new game.
     */
    CheckersData() {
        board = new int[8][8];
        setUpGame();
    }

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < board.length; i++) {
            int[] row = board[i];
            sb.append(8 - i).append(" ");
            for (int n : row) {
                if (n == 0) {
                    sb.append(" ");
                } else if (n == 1) {
                    sb.append(ANSI_RED + "R" + ANSI_RESET);
                } else if (n == 2) {
                    sb.append(ANSI_RED + "K" + ANSI_RESET);
                } else if (n == 3) {
                    sb.append(ANSI_YELLOW + "B" + ANSI_RESET);
                } else if (n == 4) {
                    sb.append(ANSI_YELLOW + "K" + ANSI_RESET);
                }
                sb.append(" ");
            }
            sb.append(System.lineSeparator());
        }
        sb.append("  a b c d e f g h");

        return sb.toString();
    }

    /**
     * Set up the board with checkers in position for the beginning of a game. Note
     * that checkers can only be found in squares that satisfy row % 2 != col % 2.
     * At the start of the game, all such squares in the first three rows contain
     * black squares and all such squares in the last three rows contain red
     * squares.
     */
    void setUpGame() {
        int piece = BLACK;
        for (int i = 0; i < 8; i++) {
            if (i > 2)
                piece = EMPTY;
            if (i > 4)
                piece = RED;
            for (int j = 0; j < 8; j++) {
                if (i % 2 != j % 2) {
                    board[i][j] = piece;
                }
            }
        }
    }

    /**
     * Return the contents of the square in the specified row and column.
     */
    int pieceAt(int row, int col) {
        return board[row][col];
    }

    /**
     * Make the specified move. It is assumed that move is non-null and that the
     * move it represents is legal.
     * 
     * @return true if the piece becomes a king, otherwise false
     */
    boolean makeMove(CheckersMove move) {
        return makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol);
    }

    /**
     * Make the move from (fromRow,fromCol) to (toRow,toCol). It is assumed that
     * this move is legal. If the move is a jump, the jumped piece is removed from
     * the board. If a piece moves to the last row on the opponent's side of the
     * board, the piece becomes a king.
     *
     * @param fromRow row index of the from square
     * @param fromCol column index of the from square
     * @param toRow   row index of the to square
     * @param toCol   column index of the to square
     * @return true if the piece becomes a king, otherwise false
     */
    boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        // handle jump
        if (Math.abs(fromRow - toRow) > 1)
            board[(fromRow + toRow) / 2][(fromCol + toCol) / 2] = EMPTY;

        // move piece
        board[toRow][toCol] = pieceAt(fromRow, fromCol);
        board[fromRow][fromCol] = EMPTY;

        // set piece to king
        if (toRow == 7 && pieceAt(toRow, toCol) == BLACK) {
            board[toRow][toCol] = BLACK_KING;
            return true;
        } else if (toRow == 0 && pieceAt(toRow, toCol) == RED) {
            board[toRow][toCol] = RED_KING;
            return true;
        }
        return false;
    }

    /**
     * Return an array containing all the legal CheckersMoves for the specified
     * player on a given board. If the player has no legal moves, null is returned.
     * The value of player should be one of the constants RED or BLACK; if not, null
     * is returned. If the returned value is non-null, it consists entirely of jump
     * moves or entirely of regular moves, since if the player can jump, only jumps
     * are legal moves.
     *
     * @param gameState an 8x8 int array containing the pieces of a game at a
     *                  certain state
     * @param player    color of the player, RED or BLACK
     */
    CheckersMove[] getLegalMoves(int[][] gameState, int player) {
        // Use an arraylist to store legal moves because we don't know how many we'll
        // find.
        ArrayList<CheckersMove> legalMoves = new ArrayList<>();
        ArrayList<CheckersMove> legalJumpMoves = new ArrayList<>();
        int increment = 0;
        int KING;
        if (player == RED) {
            increment = -1;
            KING = RED_KING;
        } else if (player == BLACK) {
            increment = 1;
            KING = BLACK_KING;
        } else
            return null;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i % 2 != j % 2 && (gameState[i][j] == player || gameState[i][j] == KING)) {
                    // Once we find a piece belonging to the current player, check for possible jump
                    // moves. Only search for non-jump moves if there are no jumps available
                    CheckersMove[] legalJumpMovesArray = getLegalJumpsFrom(gameState, player, i, j);
                    if (legalJumpMovesArray != null) {
                        for (int k = 0; k < legalJumpMovesArray.length; k++) {
                            legalJumpMoves.add(legalJumpMovesArray[k]);
                        }
                    } else if (legalJumpMoves.size() == 0) {
                        // Check for moves going forward. ("Up" the board for red pieces, "down" the
                        // board for black pieces)
                        if (j - 1 >= 0 && i + increment >= 0 && i + increment <= 7
                                && gameState[i + increment][j - 1] == EMPTY)
                            legalMoves.add(new CheckersMove(i, j, i + increment, j - 1));
                        if (j + 1 <= 7 && i + increment >= 0 && i + increment <= 7
                                && gameState[i + increment][j + 1] == EMPTY)
                            legalMoves.add(new CheckersMove(i, j, i + increment, j + 1));
                        if (pieceAt(i, j) == KING) {
                            // If the piece is a king, check for moves going backwards
                            if (j - 1 >= 0 && i - increment >= 0 && i - increment <= 7
                                    && gameState[i - increment][j - 1] == EMPTY)
                                legalMoves.add(new CheckersMove(i, j, i - increment, j - 1));
                            if (j + 1 <= 7 && i - increment >= 0 && i - increment <= 7
                                    && gameState[i - increment][j + 1] == EMPTY)
                                legalMoves.add(new CheckersMove(i, j, i - increment, j + 1));
                        }
                    }
                }
            }
        }

        // Convert arraylist to an array once we've found all possible moves
        if (legalJumpMoves.size() > 0) {
            CheckersMove[] legalMovesArray = new CheckersMove[legalJumpMoves.size()];
            for (int i = 0; i < legalJumpMoves.size(); i++) {
                legalMovesArray[i] = legalJumpMoves.get(i);
            }
            return legalMovesArray;
        } else if (legalMoves.size() > 0) {
            CheckersMove[] legalMovesArray = new CheckersMove[legalMoves.size()];
            for (int i = 0; i < legalMoves.size(); i++) {
                legalMovesArray[i] = legalMoves.get(i);
            }
            return legalMovesArray;
        } else {
            return null;
        }

    }

    /**
     * Return a list of the legal jumps that the specified player can make starting
     * from the specified row and column. If no such jumps are possible, null is
     * returned. The logic is similar to the logic of the getLegalMoves() method.
     *
     * @param gameState An 8x8 int array containing the pieces of a game at a
     *                  certain state
     * @param player    The player of the current jump, either RED or BLACK.
     * @param row       row index of the start square.
     * @param col       col index of the start square.
     */
    CheckersMove[] getLegalJumpsFrom(int[][] gameState, int player, int row, int col) {
        // Use an arraylist to store legal moves because we don't know how many we'll
        // find.
        ArrayList<CheckersMove> legalMoves = new ArrayList<>();
        int increment;
        int opponent;
        int KING;
        int OPPONENT_KING;
        if (player == RED) {
            increment = -1;
            opponent = BLACK;
            KING = RED_KING;
            OPPONENT_KING = BLACK_KING;
        } else if (player == BLACK) {
            increment = 1;
            opponent = RED;
            KING = BLACK_KING;
            OPPONENT_KING = RED_KING;
        } else
            return null;

        // Check for an opponent piece on the left diagonal
        if (col - 1 >= 0 && row + increment >= 0 && row + increment <= 7
                && (gameState[row + increment][col - 1] == opponent
                        || gameState[row + increment][col - 1] == OPPONENT_KING)) {
            // If there is an opponent piece on the left diagonal, check if the space past
            // the opponent piece is empty (jump possible)
            if (col - 2 >= 0 && row + (increment * 2) >= 0 && row + (increment * 2) <= 7
                    && gameState[row + (increment * 2)][col - 2] == EMPTY)
                legalMoves.add(new CheckersMove(row, col, row + (increment * 2), col - 2));
        }
        // Check for an opponent piece on the right diagonal
        if (col + 1 <= 7 && row + increment >= 0 && row + increment <= 7
                && (gameState[row + increment][col + 1] == opponent
                        || gameState[row + increment][col + 1] == OPPONENT_KING)) {
            // If there is an opponent piece on the right diagonal, check if the space past
            // the opponent piece is empty (jump possible)
            if (col + 2 <= 7 && row + (increment * 2) >= 0 && row + (increment * 2) <= 7
                    && gameState[row + (increment * 2)][col + 2] == EMPTY)
                legalMoves.add(new CheckersMove(row, col, row + (increment * 2), col + 2));
        }

        // If the piece is a king, we need to check for jumps in both directions (jumps
        // going "up" the board for red kings and "down" the board for black kings)
        if (pieceAt(row, col) == KING) {
            if (col - 1 >= 0 && row - increment >= 0 && row - increment <= 7
                    && (gameState[row - increment][col - 1] == opponent
                            || gameState[row - increment][col - 1] == OPPONENT_KING)) {
                if (col - 2 >= 0 && row - (increment * 2) >= 0 && row - (increment * 2) <= 7
                        && gameState[row - (increment * 2)][col - 2] == EMPTY)
                    legalMoves.add(new CheckersMove(row, col, row - (increment * 2), col - 2));
            }
            if (col + 1 <= 7 && row - increment >= 0 && row - increment <= 7
                    && (gameState[row - increment][col + 1] == opponent
                            || gameState[row - increment][col + 1] == OPPONENT_KING)) {
                if (col + 2 <= 7 && row - (increment * 2) >= 0 && row - (increment * 2) <= 7
                        && gameState[row - (increment * 2)][col + 2] == EMPTY)
                    legalMoves.add(new CheckersMove(row, col, row - (increment * 2), col + 2));
            }
        }

        if (legalMoves.size() == 0)
            return null;

        // Convert arraylist to an array once we've found all possible moves
        CheckersMove[] legalMovesArray = new CheckersMove[legalMoves.size()];
        for (int i = 0; i < legalMoves.size(); i++) {
            legalMovesArray[i] = legalMoves.get(i);
        }
        return legalMovesArray;
    }

}