// 1. Interface untuk pemain
interface Player {
    String getSymbol();
    String getName();
    void makeMove(Board board, int row, int col) throws InvalidMoveException;
}

// 2. Abstract class
abstract class AbstractPlayer implements Player {
    protected String symbol;
    protected String name;
    
    public AbstractPlayer(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }
    
    @Override
    public String getSymbol() {
        return symbol;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    // Template method pattern
    @Override
    public void makeMove(Board board, int row, int col) throws InvalidMoveException {
        validateMove(board, row, col);
        board.placeMark(row, col, this);
    }
    
    // Hook method to be implemented by subclasses
    protected abstract void validateMove(Board board, int row, int col) throws InvalidMoveException;
}

// 3. Concrete implementation (inheritance)
class HumanPlayer extends AbstractPlayer {
    public HumanPlayer(String name, String symbol) {
        super(name, symbol);
    }
    
    @Override
    protected void validateMove(Board board, int row, int col) throws InvalidMoveException {
        if (!board.isValidMove(row, col)) {
            throw new InvalidMoveException("Invalid move: Cell is already occupied or out of bounds");
        }
    }
}

class ComputerPlayer extends AbstractPlayer {
    public ComputerPlayer(String name, String symbol) {
        super(name, symbol);
    }
    
    @Override
    protected void validateMove(Board board, int row, int col) throws InvalidMoveException {
        // Computer can validate moves differently if needed
        if (!board.isValidMove(row, col)) {
            throw new InvalidMoveException("Invalid move by computer: Cell is already occupied or out of bounds");
        }
    }
    
    // Additional method specific to ComputerPlayer
    public void makeAutomaticMove(Board board) throws InvalidMoveException {
        // Simple implementation: find first available cell
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                if (board.isValidMove(i, j)) {
                    makeMove(board, i, j);
                    return;
                }
            }
        }
        throw new InvalidMoveException("No valid moves available");
    }
}

// 4. Custom Exception
class InvalidMoveException extends Exception {
    public InvalidMoveException(String message) {
        super(message);
    }
}

// 5. Model class for MVC pattern
class Board {
    private final int size;
    private final Cell[][] cells;
    
    public Board(int size) {
        this.size = size;
        cells = new Cell[size][size];
        initializeBoard();
    }
    
    private void initializeBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j] = new Cell(i, j);
            }
        }
    }
    
    public int getSize() {
        return size;
    }
    
    public boolean isValidMove(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size && cells[row][col].isEmpty();
    }
    
    public void placeMark(int row, int col, Player player) {
        cells[row][col].setPlayer(player);
    }
    
    public Cell getCell(int row, int col) {
        return cells[row][col];
    }
    
    public void reset() {
        initializeBoard();
    }
    
    // Check for win conditions
    public boolean checkWin() {
        // Check rows
        for (int i = 0; i < size; i++) {
            if (!cells[i][0].isEmpty() && checkRowWin(i)) {
                return true;
            }
        }
        
        // Check columns
        for (int i = 0; i < size; i++) {
            if (!cells[0][i].isEmpty() && checkColumnWin(i)) {
                return true;
            }
        }
        
        // Check diagonals
        if (!cells[0][0].isEmpty() && checkDiagonalWin()) {
            return true;
        }
        
        return !cells[0][size - 1].isEmpty() && checkAntiDiagonalWin();
    }
    
    private boolean checkRowWin(int row) {
        Player player = cells[row][0].getPlayer();
        for (int i = 1; i < size; i++) {
            if (cells[row][i].isEmpty() || cells[row][i].getPlayer() != player) {
                return false;
            }
        }
        return true;
    }
    
    private boolean checkColumnWin(int col) {
        Player player = cells[0][col].getPlayer();
        for (int i = 1; i < size; i++) {
            if (cells[i][col].isEmpty() || cells[i][col].getPlayer() != player) {
                return false;
            }
        }
        return true;
    }
    
    private boolean checkDiagonalWin() {
        Player player = cells[0][0].getPlayer();
        for (int i = 1; i < size; i++) {
            if (cells[i][i].isEmpty() || cells[i][i].getPlayer() != player) {
                return false;
            }
        }
        return true;
    }
    
    private boolean checkAntiDiagonalWin() {
        Player player = cells[0][size - 1].getPlayer();
        for (int i = 1; i < size; i++) {
            if (cells[i][size - 1 - i].isEmpty() || cells[i][size - 1 - i].getPlayer() != player) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isFull() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (cells[i][j].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}

// 6. Model class for MVC pattern
class Cell {
    private final int row;
    private final int col;
    private Player player;
    
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.player = null;
    }
    
    public boolean isEmpty() {
        return player == null;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    public void clear() {
        player = null;
    }
    
    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }
    
    public String getSymbol() {
        return isEmpty() ? "" : player.getSymbol();
    }
}

// 7. Interface for observers (Observer pattern)
interface GameObserver {
    void onGameUpdated(GameState state);
    void onGameOver(Player winner);
    void onMoveMade(int row, int col, Player player);
}

// 8. Enum for game state
enum GameState {
    IN_PROGRESS, PLAYER_X_WIN, PLAYER_O_WIN, TIE
}

// 9. Controller class for MVC pattern
class GameController {
    private final Board board;
    private final java.util.List<Player> players;
    private final java.util.List<GameObserver> observers;
    private int currentPlayerIndex;
    private boolean gameOver;
    private Player winner;
    private final GameDataPersistence dataPersistence;
    
    public GameController(int boardSize) {
        board = new Board(boardSize);
        players = new java.util.ArrayList<>();
        observers = new java.util.ArrayList<>();
        currentPlayerIndex = 0;
        gameOver = false;
        winner = null;
        dataPersistence = new FileGameDataPersistence("game_data.txt");
    }
    
    public void addPlayer(Player player) {
        players.add(player);
    }
    
    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }
    
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }
    
    public void startNewGame() {
        board.reset();
        currentPlayerIndex = 0;
        gameOver = false;
        winner = null;
        notifyGameUpdated();
    }
    
    public void makeMove(int row, int col) {
        if (gameOver) {
            return;
        }
        
        try {
            Player currentPlayer = getCurrentPlayer();
            currentPlayer.makeMove(board, row, col);
            
            notifyMoveMade(row, col, currentPlayer);
            
            if (board.checkWin()) {
                gameOver = true;
                winner = currentPlayer;
                notifyGameOver(winner);
                saveGameResult();
            } else if (board.isFull()) {
                gameOver = true;
                notifyGameOver(null);
                saveGameResult();
            } else {
                nextPlayer();
                notifyGameUpdated();
            }
        } catch (InvalidMoveException e) {
            System.err.println(e.getMessage());
        }
    }
    
    private void saveGameResult() {
        GameResult result = new GameResult();
        result.setDate(new java.util.Date());
        if (winner != null) {
            result.setResult(winner.getName() + " won");
        } else {
            result.setResult("Tie");
        }
        try {
            dataPersistence.saveGameResult(result);
        } catch (PersistenceException e) {
            System.err.println("Failed to save game result: " + e.getMessage());
        }
    }
    
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
    
    private void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    public Player getWinner() {
        return winner;
    }
    
    private void notifyGameUpdated() {
        GameState state = GameState.IN_PROGRESS;
        for (GameObserver observer : observers) {
            observer.onGameUpdated(state);
        }
    }
    
    private void notifyGameOver(Player winner) {
        for (GameObserver observer : observers) {
            observer.onGameOver(winner);
        }
    }
    
    private void notifyMoveMade(int row, int col, Player player) {
        for (GameObserver observer : observers) {
            observer.onMoveMade(row, col, player);
        }
    }
    
    public Board getBoard() {
        return board;
    }
    
    public java.util.List<GameResult> loadGameHistory() throws PersistenceException {
        return dataPersistence.loadGameResults();
    }
}

// 10. View class for MVC pattern
class TicTacToeView extends javax.swing.JFrame implements GameObserver {
    private final int boardWidth = 600;
    private final int boardHeight = 650;
    private final GameController controller;
    
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel boardPanel;
    private javax.swing.JButton[][] buttons;
    private javax.swing.JButton newGameButton;
    private javax.swing.JButton historyButton;
    
    public TicTacToeView(GameController controller) {
        this.controller = controller;
        controller.addObserver(this);
        
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Tic-Tac-Toe Game");
        setSize(boardWidth, boardHeight);
        setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new java.awt.BorderLayout());
        
        // Top panel with status label
        javax.swing.JPanel topPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        topPanel.setBackground(java.awt.Color.darkGray);
        
        statusLabel = new javax.swing.JLabel("Tic-Tac-Toe");
        statusLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
        statusLabel.setForeground(java.awt.Color.WHITE);
        statusLabel.setHorizontalAlignment(javax.swing.JLabel.CENTER);
        statusLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(statusLabel, java.awt.BorderLayout.CENTER);
        
        // Button panel
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel(new java.awt.FlowLayout());
        buttonPanel.setBackground(java.awt.Color.darkGray);
        
        newGameButton = new javax.swing.JButton("New Game");
        newGameButton.addActionListener(e -> controller.startNewGame());
        buttonPanel.add(newGameButton);
        
        historyButton = new javax.swing.JButton("Game History");
        historyButton.addActionListener(e -> showGameHistory());
        buttonPanel.add(historyButton);
        
        topPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);
        add(topPanel, java.awt.BorderLayout.NORTH);
        
        // Board panel
        boardPanel = new javax.swing.JPanel();
        int size = controller.getBoard().getSize();
        boardPanel.setLayout(new java.awt.GridLayout(size, size));
        buttons = new javax.swing.JButton[size][size];
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                final int row = i;
                final int col = j;
                buttons[i][j] = new javax.swing.JButton("");
                buttons[i][j].setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 60));
                buttons[i][j].setFocusPainted(false);
                buttons[i][j].addActionListener(e -> controller.makeMove(row, col));
                boardPanel.add(buttons[i][j]);
            }
        }
        
        add(boardPanel, java.awt.BorderLayout.CENTER);
        
        updateStatus();
    }
    
    private void showGameHistory() {
        try {
            java.util.List<GameResult> history = controller.loadGameHistory();
            
            if (history.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this, 
                    "No game history available.", 
                    "Game History", 
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            StringBuilder historyText = new StringBuilder("Game History:\n\n");
            
            for (int i = 0; i < history.size(); i++) {
                GameResult result = history.get(i);
                historyText.append(i + 1).append(". ")
                          .append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(result.getDate()))
                          .append(" - ")
                          .append(result.getResult())
                          .append("\n");
            }
            
            javax.swing.JTextArea textArea = new javax.swing.JTextArea(historyText.toString());
            textArea.setEditable(false);
            textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));
            
            javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(textArea);
            scrollPane.setPreferredSize(new java.awt.Dimension(400, 300));
            
            javax.swing.JOptionPane.showMessageDialog(this, scrollPane, 
                "Game History", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            
        } catch (PersistenceException e) {
            javax.swing.JOptionPane.showMessageDialog(this, 
                "Error loading game history: " + e.getMessage(), 
                "Error", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateStatus() {
        if (controller.isGameOver()) {
            Player winner = controller.getWinner();
            if (winner != null) {
                statusLabel.setText(winner.getName() + " (" + winner.getSymbol() + ") wins!");
            } else {
                statusLabel.setText("Game ended in a tie!");
            }
        } else {
            Player currentPlayer = controller.getCurrentPlayer();
            statusLabel.setText(currentPlayer.getName() + " (" + currentPlayer.getSymbol() + ")'s turn");
        }
    }
    
    @Override
    public void onGameUpdated(GameState state) {
        updateStatus();
        updateBoard();
    }
    
    @Override
    public void onGameOver(Player winner) {
        updateStatus();
        
        String message;
        if (winner != null) {
            message = winner.getName() + " (" + winner.getSymbol() + ") wins!";
            highlightWinningCells();
        } else {
            message = "Game ended in a tie!";
        }
        
        // Show message after a short delay
        javax.swing.Timer timer = new javax.swing.Timer(500, e -> {
            javax.swing.JOptionPane.showMessageDialog(this, message);
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void highlightWinningCells() {
        // For simplicity, highlight all cells for now
        // In a real implementation, you would only highlight the winning line
        Board board = controller.getBoard();
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                if (!board.getCell(i, j).isEmpty()) {
                    buttons[i][j].setBackground(java.awt.Color.lightGray);
                }
            }
        }
    }
    
    @Override
    public void onMoveMade(int row, int col, Player player) {
        buttons[row][col].setText(player.getSymbol());
    }
    
    private void updateBoard() {
        Board board = controller.getBoard();
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                buttons[i][j].setText(board.getCell(i, j).getSymbol());
                buttons[i][j].setBackground(null);
            }
        }
    }
}

// 11. Interface for data persistence
interface GameDataPersistence {
    void saveGameResult(GameResult result) throws PersistenceException;
    java.util.List<GameResult> loadGameResults() throws PersistenceException;
}

// 12. Custom Exception for persistence
class PersistenceException extends Exception {
    public PersistenceException(String message) {
        super(message);
    }
    
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}

// 13. Implementation of data persistence
class FileGameDataPersistence implements GameDataPersistence {
    private final String fileName;
    
    public FileGameDataPersistence(String fileName) {
        this.fileName = fileName;
    }
    
    @Override
    public void saveGameResult(GameResult result) throws PersistenceException {
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(
                new java.io.FileWriter(fileName, true))) {
            writer.write(result.getDate().getTime() + "," + result.getResult());
            writer.newLine();
        } catch (java.io.IOException e) {
            throw new PersistenceException("Failed to save game result", e);
        }
    }
    
    @Override
    public java.util.List<GameResult> loadGameResults() throws PersistenceException {
        java.util.List<GameResult> results = new java.util.ArrayList<>();
        
        java.io.File file = new java.io.File(fileName);
        if (!file.exists()) {
            return results;
        }
        
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    try {
                        GameResult result = new GameResult();
                        result.setDate(new java.util.Date(Long.parseLong(parts[0])));
                        result.setResult(parts[1]);
                        results.add(result);
                    } catch (NumberFormatException e) {
                        // Skip invalid entries
                    }
                }
            }
        } catch (java.io.IOException e) {
            throw new PersistenceException("Failed to load game results", e);
        }
        
        return results;
    }
}

// 14. Model class for game result (for persistence)
class GameResult {
    private java.util.Date date;
    private String result;
    
    public java.util.Date getDate() {
        return date;
    }
    
    public void setDate(java.util.Date date) {
        this.date = date;
    }
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
}

// 15. Factory for creating players (Factory pattern)
class PlayerFactory {
    public static Player createHumanPlayer(String name, String symbol) {
        return new HumanPlayer(name, symbol);
    }
    
    public static Player createComputerPlayer(String name, String symbol) {
        return new ComputerPlayer(name, symbol);
    }
}

// 16. Main Game class
public class TicTacToe {
    public static void main(String[] args) {
        // Create game controller (3x3 board)
        GameController controller = new GameController(3);
        
        // Create players using factory
        Player player1 = PlayerFactory.createHumanPlayer("Player X", "X");
        Player player2 = PlayerFactory.createHumanPlayer("Player O", "O");
        
        // Add players to controller
        controller.addPlayer(player1);
        controller.addPlayer(player2);
        
        // Create and show view
        TicTacToeView view = new TicTacToeView(controller);
        view.setVisible(true);
        
        // Start new game
        controller.startNewGame();
    }
}