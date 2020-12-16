package edu.iastate.cs472.proj1;

public class AlphaBetaSearch {
    private CheckersData board;

    /**
     * SearchDepth limits how far the algorithm will search. The greater the depth,
     * the longer the algorthim will take but the better the AI's moves will be.
     * Recommended values are between 6 and 10 (inclusive)
     */
    private int SearchDepth = 12;

    // An instance of this class will be created in the Checkers.Board
    // It would be better to keep the default constructor.

    public void setCheckersData(CheckersData board) {
        this.board = board;
    }

    /**
     * Find the best move at current stage using the Alpha-Beta pruning algorithm
     * "search". The input parameter legalMoves contains all the possible moves. It
     * contains four integers: fromRow, fromCol, toRow, toCol which represents a
     * move from (fromRow, fromCol) to (toRow, toCol). It also provides a utility
     * method `isJump` to see whether this move is a jump or a simple move.
     *
     * @param legalMoves All the legal moves for the agent at current step.
     */
    public CheckersMove makeMove(CheckersMove[] legalMoves) {
        // The checker board state can be obtained from this.board,
        // which is a int 2D array. The numbers in the `board` are
        // defined as
        // 0 - empty square,
        // 1 - red man
        // 2 - red king
        // 3 - black man
        // 4 - black king
        System.out.println(board);
        System.out.println();

        // Make a copy of the current board so we can test and evaluate moves without
        // changing the actual game. We only change the game board once the best move
        // has been determined and made.
        int[][] currentGameState = new int[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                currentGameState[i][j] = this.board.board[i][j];
            }
        }

        long startTime = System.currentTimeMillis();

        // Find the best move and then return it
        CheckersMove bestMove = search(currentGameState, legalMoves, 3, 0);

        System.out.println("Move took: " + (System.currentTimeMillis() - startTime) + "ms");

        return bestMove;
    }

    /**
     * Finds the best possible move from a given game state using Alpha-Beta
     * pruning.
     * 
     * @param gameState     An 8x8 int array containing the pieces of a game at a
     *                      certain state
     * @param currentMoves  Array of all possible moves for the current player from
     *                      the given gameState
     * @param currentPlayer Keeps track of the current player and whether to min or
     *                      max the evaluation. Value is 3 for AI and 1 for human
     *                      player
     * @param depth         Starting depth of the alpha-beta search. Should always
     *                      be 0 UNLESS being called from the "tryMove" function, in
     *                      which case we're exclusively looking for double jumps
     *                      and the depth will have already incremented a certain
     *                      amount before the jump move was found.
     */
    public CheckersMove search(int[][] gameState, CheckersMove[] currentMoves, int currentPlayer, int depth) {
        int bestScore = currentPlayer == 3 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        if (currentMoves == null || depth >= SearchDepth) {
            bestScore = evaluateBoard(gameState);
        }
        int currentScore;
        CheckersMove bestMove = currentMoves[0];

        for (CheckersMove checkersMove : currentMoves) {
            currentScore = minValue(tryMove(gameState, checkersMove, depth), 1, Integer.MIN_VALUE, Integer.MAX_VALUE,
                    depth);
            if (currentScore > bestScore) {
                bestScore = currentScore;
                bestMove = checkersMove;
            }
        }

        return bestMove;
    }

    /**
     * 
     * @param gameState An 8x8 int array containing the pieces of a game at a
     *                  certain state
     * @param player    Value of the current player. Should always be 1 since we're
     *                  looking for the min value of the human player
     * @param alpha     The current highest value found by the search algorithm. If
     *                  a lower value is found, we can prune this branch
     * @param beta      The current lowest value found by the search algorithm.
     * @param depth     The current depth of the algorithm. The search stops if the
     *                  depth reaches the searchDepth
     */
    public int minValue(int[][] gameState, int player, int alpha, int beta, int depth) {
        depth++;
        CheckersMove[] legalMoves = this.board.getLegalMoves(gameState, player);

        if (legalMoves == null || depth >= SearchDepth || isGameOver(gameState)) {
            return evaluateBoard(gameState);
        }
        int currentScore = Integer.MAX_VALUE;
        for (CheckersMove checkersMove : legalMoves) {
            currentScore = Math.min(currentScore,
                    maxValue(tryMove(gameState, checkersMove, depth), 3, alpha, beta, depth));
            if (currentScore <= alpha) {
                return currentScore;
            }
            beta = Math.min(currentScore, beta);
        }
        return currentScore;
    }

    /**
     * 
     * @param gameState An 8x8 int array containing the pieces of a game at a
     *                  certain state
     * @param player    Value of the current player. Should always be 1 since we're
     *                  looking for the min value of the human player
     * @param alpha     The current highest value found by the search algorithm
     * @param beta      The current lowest value found by the search algorithm. If a
     *                  higher value is found, we can prune this branch
     * @param depth     The current depth of the algorithm. The search stops if the
     *                  depth reaches the searchDepth
     */
    public int maxValue(int[][] gameState, int player, int alpha, int beta, int depth) {
        depth++;
        CheckersMove[] legalMoves = this.board.getLegalMoves(gameState, player);

        if (legalMoves == null || depth >= SearchDepth || isGameOver(gameState)) {
            return evaluateBoard(gameState);
        }
        int currentScore = Integer.MIN_VALUE;
        for (CheckersMove checkersMove : legalMoves) {
            currentScore = Math.max(currentScore,
                    minValue(tryMove(gameState, checkersMove, depth), 1, alpha, beta, depth));
            if (currentScore >= beta) {
                return currentScore;
            }
            alpha = Math.max(currentScore, alpha);
        }
        return currentScore;
    }

    /**
     * Try the move on a copy of the gameState. It is assumed that this move is
     * legal. If the move is a jump, the jumped piece is removed from the board and
     * we check for possible jumps from the piece that just jumped (look for double
     * jumps). If a piece moves to the last row on the opponent's side of the board,
     * the piece becomes a king.
     * 
     * @param gameState An 8x8 int array containing the pieces of a game at a
     *                  certain state
     * @param move      A move possible given the gameState
     * @param depth     Current depth of the search. In the case of a possible
     *                  double jump: after the first jump, we run the search
     *                  alorithm starting from the current depth instead of 0.
     */
    int[][] tryMove(int[][] gameState, CheckersMove move, int depth) {
        int[][] updatedGameState = new int[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                updatedGameState[i][j] = gameState[i][j];
            }
        }

        // Move current piece
        updatedGameState[move.toRow][move.toCol] = updatedGameState[move.fromRow][move.fromCol];
        updatedGameState[move.fromRow][move.fromCol] = 0;

        // Convert piece to a king
        if (move.toRow == 7 && updatedGameState[move.toRow][move.toCol] == 3) {
            updatedGameState[move.toRow][move.toCol] = 4;
        } else if (move.toRow == 0 && updatedGameState[move.toRow][move.toCol] == 1) {
            updatedGameState[move.toRow][move.toCol] = 2;
        }

        // Handle a jump move and search for possible double jumps. If there are more
        // than one double jump, use the alpha-beta search to find the best possible
        // double jump
        if (move.isJump()) {
            updatedGameState[(move.fromRow + move.toRow) / 2][(move.fromCol + move.toCol) / 2] = 0;
            CheckersMove[] doubleJumps = this.board.getLegalJumpsFrom(updatedGameState,
                    updatedGameState[move.toRow][move.toCol], move.toRow, move.toCol);
            if (doubleJumps != null)
                if (doubleJumps.length == 1)
                    return tryMove(updatedGameState, doubleJumps[0], depth);
                else
                    return tryMove(updatedGameState,
                            search(updatedGameState, doubleJumps, updatedGameState[move.toRow][move.toCol], depth),
                            depth);
        }

        return updatedGameState;
    }

    /**
     * Evaluation function that takes a game state and returns an evaluation value.
     * Normal pieces (non-kings) are given a value of plus two for the AI's pieces
     * and negative two for the opponents pieces. Kings are given a value of plus 50
     * for the AI's pieces and negative 50 for the opponents pieces. Pieces along
     * the sides of the board are weighted slightly more since they can not be
     * jumped over. Pieces closer to becoming king are not weighted higher since:
     * the AI will most likely run through bratches where pieces close to becoming
     * kings do in-fact become kings. And since kings are weighted so heavily,
     * pieces close to becoming kings are still treated as threatening
     */
    public int evaluateBoard(int[][] gameState) {
        int value = 0;
        boolean foundRed = false;
        boolean foundBlack = false;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (gameState[i][j] == 1) {
                    foundRed = true;
                    if (j == 0 || j == 7)
                        value -= 2;
                    value -= 5;
                } else if (gameState[i][j] == 2) {
                    foundRed = true;
                    if (j == 0 || j == 7)
                        value -= 4;
                    value -= 50;
                } else if (gameState[i][j] == 3) {
                    foundBlack = true;
                    if (j == 0 || j == 7)
                        value += 2;
                    value += 5;
                } else if (gameState[i][j] == 4) {
                    foundBlack = true;
                    if (j == 0 || j == 7)
                        value += 4;
                    value += 80;
                }
            }
        }
        // Game Over states
        if (!foundBlack)
            value -= 1000;
        if (!foundRed)
            value += 1000;

        return value;
    }

    // Helper method to check if a player no longer has peices remaining
    public boolean isGameOver(int[][] gameState) {
        boolean foundRed = false;
        boolean foundBlack = false;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (gameState[i][j] == 1 || gameState[i][j] == 2)
                    foundRed = true;
                else if (gameState[i][j] == 3 || gameState[i][j] == 4)
                    foundBlack = true;
                if (foundRed && foundBlack)
                    return false;
            }
        }
        return true;
    }
}
