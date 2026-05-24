import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

// ============================================================
//  Tic-Tac-Toe  –  Rebuilt & Improved
//  Language : Java  |  UI : Swing only  |  Storage : .txt files
//  All classes are in one file for easy viva / submission.
// ============================================================


// ── PLAYER MODEL ────────────────────────────────────────────
/**
 * Simple Player class that holds one player's data.
 * No ArrayLists – we use a proper object instead.
 */
class Player {
    String name;
    String username;
    int wins;
    int losses;

    // Constructor used when registering a new player
    public Player(String name, String username) {
        this.name     = name;
        this.username = username;
        this.wins     = 0;
        this.losses   = 0;
    }

    // Constructor used when loading from file
    public Player(String name, String username, int wins, int losses) {
        this.name     = name;
        this.username = username;
        this.wins     = wins;
        this.losses   = losses;
    }

    @Override
    public String toString() {
        // One player per line: name,username,wins,losses
        return name + "," + username + "," + wins + "," + losses;
    }
}


// ── FILE STORAGE HELPER ─────────────────────────────────────
/**
 * Handles all reading and writing of players.txt.
 * Format of each line: name,username,wins,losses
 */
class PlayerStorage {

    // The file where all player data is saved
    private static final String FILE_NAME = "players.txt";

    /** Load every player from the file. Returns an empty list if file missing. */
    public static List<Player> loadAll() {
        List<Player> players = new ArrayList<>();
        File file = new File(FILE_NAME);
        if (!file.exists()) return players;   // no file yet – that's fine

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String name     = parts[0];
                    String username = parts[1];
                    int wins        = Integer.parseInt(parts[2]);
                    int losses      = Integer.parseInt(parts[3]);
                    players.add(new Player(name, username, wins, losses));
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                "Could not read players.txt:\n" + e.getMessage(), "File Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return players;
    }

    /** Save the full player list back to the file (overwrites). */
    public static void saveAll(List<Player> players) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Player p : players) {
                writer.write(p.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                "Could not write players.txt:\n" + e.getMessage(), "File Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Find a player by username. Returns null if not found.
     * Useful for login / duplicate checks.
     */
    public static Player findByUsername(String username) {
        for (Player p : loadAll()) {
            if (p.username.equalsIgnoreCase(username)) return p;
        }
        return null;
    }

    /**
     * Add a brand-new player and persist immediately.
     * Returns false if username already exists.
     */
    public static boolean addPlayer(Player newPlayer) {
        List<Player> players = loadAll();
        for (Player p : players) {
            if (p.username.equalsIgnoreCase(newPlayer.username)) return false;
        }
        players.add(newPlayer);
        saveAll(players);
        return true;
    }

    /**
     * Update an existing player's wins/losses in the file.
     * Matches by username (case-insensitive).
     */
    public static void updatePlayer(Player updated) {
        List<Player> players = loadAll();
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).username.equalsIgnoreCase(updated.username)) {
                players.set(i, updated);
                break;
            }
        }
        saveAll(players);
    }
}


// ── SHARED UI HELPER ─────────────────────────────────────────
/**
 * A small utility class with reusable UI methods.
 * Keeps button / header creation consistent across all screens.
 */
class UIHelper {

    // Colour palette used throughout the app
    static final Color DARK_BG    = new Color(30, 30, 45);
    static final Color ACCENT     = new Color(100, 180, 255);
    static final Color BTN_GREEN  = new Color(60, 180, 100);
    static final Color BTN_RED    = new Color(210, 70, 70);
    static final Color BTN_ORANGE = new Color(230, 140, 40);
    static final Color BTN_BLUE   = new Color(60, 130, 210);
    static final Color TEXT_LIGHT = new Color(230, 230, 240);
    static final Color BOARD_BG   = new Color(45, 45, 65);

    /** Creates a styled header label (dark background, light text). */
    public static JLabel makeHeader(String title) {
        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setOpaque(true);
        lbl.setBackground(DARK_BG);
        lbl.setForeground(ACCENT);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        lbl.setPreferredSize(new Dimension(0, 50));
        lbl.setBorder(new EmptyBorder(0, 0, 0, 0));
        return lbl;
    }

    /**
     * Creates a styled button with the given background colour.
     * All buttons share the same rounded, no-focus look.
     */
    public static JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(160, 45));
        return btn;
    }

    /**
     * Sets standard properties on any JFrame
     * (size, centre on screen, EXIT_ON_CLOSE).
     */
    public static void setupFrame(JFrame frame, String title) {
        frame.setTitle(title);
        frame.setSize(600, 520);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
    }
}


// ── SCREEN 1 : INTRO / SPLASH ────────────────────────────────
/**
 * First screen the user sees.
 * Shows the game title and a "Get Started" button.
 */
class IntroScreen {

    public IntroScreen() {
        JFrame frame = new JFrame();
        UIHelper.setupFrame(frame, "Tic-Tac-Toe");

        // Main panel with dark background
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIHelper.DARK_BG);
        frame.setContentPane(main);

        // ── Top header ──
        main.add(UIHelper.makeHeader("TIC  TAC  TOE"), BorderLayout.NORTH);

        // ── Centre logo area ──
        JPanel centre = new JPanel(new GridBagLayout());
        centre.setBackground(UIHelper.DARK_BG);

        JLabel logo = new JLabel("✕ ○ ✕", SwingConstants.CENTER);
        logo.setFont(new Font("SansSerif", Font.BOLD, 64));
        logo.setForeground(UIHelper.ACCENT);

        JLabel sub = new JLabel("A classic two-player game", SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 16));
        sub.setForeground(UIHelper.TEXT_LIGHT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0, 0, 10, 0);
        centre.add(logo, gbc);
        gbc.gridy = 1;
        centre.add(sub, gbc);
        main.add(centre, BorderLayout.CENTER);

        // ── Bottom button ──
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        bottom.setBackground(UIHelper.DARK_BG);
        JButton start = UIHelper.makeButton("Get Started  →", UIHelper.BTN_GREEN);
        start.setPreferredSize(new Dimension(200, 50));
        start.addActionListener(e -> {
            new MenuScreen();
            frame.dispose();
        });
        bottom.add(start);
        main.add(bottom, BorderLayout.SOUTH);

        frame.setVisible(true);
    }
}


// ── SCREEN 2 : MAIN MENU ─────────────────────────────────────
/**
 * Hub screen – player can go to Login/Register, start a game,
 * or view the scoreboard.
 */
class MenuScreen {

    public MenuScreen() {
        JFrame frame = new JFrame();
        UIHelper.setupFrame(frame, "Main Menu");

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIHelper.DARK_BG);
        frame.setContentPane(main);

        main.add(UIHelper.makeHeader("MAIN  MENU"), BorderLayout.NORTH);

        // ── Centre button grid ──
        JPanel btnPanel = new JPanel(new GridBagLayout());
        btnPanel.setBackground(UIHelper.DARK_BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(12, 12, 12, 12);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.gridx   = 0;
        gbc.gridy   = 0;

        JButton btnLogin = UIHelper.makeButton("Login / Register", UIHelper.BTN_BLUE);
        JButton btnPlay  = UIHelper.makeButton("Play Game",        UIHelper.BTN_GREEN);
        JButton btnStats = UIHelper.makeButton("Scoreboard",       UIHelper.BTN_ORANGE);
        JButton btnExit  = UIHelper.makeButton("Exit",             UIHelper.BTN_RED);

        btnPanel.add(btnLogin, gbc); gbc.gridy++;
        btnPanel.add(btnPlay,  gbc); gbc.gridy++;
        btnPanel.add(btnStats, gbc); gbc.gridy++;
        btnPanel.add(btnExit,  gbc);
        main.add(btnPanel, BorderLayout.CENTER);

        // ── Hint label ──
        JLabel hint = new JLabel(
            "Tip: login first so wins & losses are saved!", SwingConstants.CENTER);
        hint.setFont(new Font("SansSerif", Font.ITALIC, 12));
        hint.setForeground(new Color(160, 160, 180));
        hint.setBorder(new EmptyBorder(0, 0, 10, 0));
        main.add(hint, BorderLayout.SOUTH);

        // ── Wire up buttons ──
        btnLogin.addActionListener(e -> { new LoginScreen(frame); });
        btnPlay.addActionListener( e -> { new GameScreen(frame);  });
        btnStats.addActionListener(e -> { new StatsScreen(frame); });
        btnExit.addActionListener( e -> System.exit(0));

        frame.setVisible(true);
    }
}


// ── SCREEN 3 : LOGIN / REGISTER ──────────────────────────────
/**
 * Lets a player register (new account) or log in (existing).
 * Data is saved to / loaded from players.txt automatically.
 *
 * After a successful login the player's username is stored so
 * the game can update their stats when a match finishes.
 */
class LoginScreen {

    // The last logged-in player – shared across the app via static field
    static Player currentPlayer = null;

    public LoginScreen(JFrame parentFrame) {
        JFrame frame = new JFrame();
        UIHelper.setupFrame(frame, "Login / Register");

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIHelper.DARK_BG);
        frame.setContentPane(main);

        main.add(UIHelper.makeHeader("LOGIN  /  REGISTER"), BorderLayout.NORTH);

        // ── Form panel ──
        JPanel form = new JPanel(null);   // absolute layout keeps form tidy
        form.setBackground(new Color(40, 40, 60));
        form.setPreferredSize(new Dimension(600, 380));
        main.add(form, BorderLayout.CENTER);

        // Helper: create a styled label for the form
        int labelX = 130, fieldX = 290, fieldW = 180;

        JLabel lblName  = formLabel("Full Name:");
        JLabel lblUser  = formLabel("Username:");
        JLabel lblNote  = formLabel("(login only needs username)");
        lblNote.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblNote.setForeground(new Color(140, 140, 160));

        lblName.setBounds(labelX, 80,  140, 25);
        lblUser.setBounds(labelX, 130, 140, 25);
        lblNote.setBounds(labelX, 155, 300, 20);
        form.add(lblName);
        form.add(lblUser);
        form.add(lblNote);

        JTextField tfName = styledField();
        JTextField tfUser = styledField();
        tfName.setBounds(fieldX, 80,  fieldW, 30);
        tfUser.setBounds(fieldX, 130, fieldW, 30);
        form.add(tfName);
        form.add(tfUser);

        // Status label (shows success / error messages)
        JLabel lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setBounds(100, 200, 400, 25);
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStatus.setForeground(UIHelper.BTN_GREEN);
        form.add(lblStatus);

        // ── Register button ──
        JButton btnRegister = UIHelper.makeButton("Register", UIHelper.BTN_GREEN);
        btnRegister.setBounds(100, 260, 160, 45);
        btnRegister.addActionListener(e -> {
            String name = tfName.getText().trim();
            String user = tfUser.getText().trim();

            if (name.isEmpty() || user.isEmpty()) {
                showStatus(lblStatus, "Please fill both fields.", UIHelper.BTN_RED);
                return;
            }
            Player p = new Player(name, user);
            boolean ok = PlayerStorage.addPlayer(p);
            if (ok) {
                currentPlayer = p;
                showStatus(lblStatus,
                    "Registered & logged in as: " + user, UIHelper.BTN_GREEN);
            } else {
                showStatus(lblStatus,
                    "Username already exists – try logging in.", UIHelper.BTN_ORANGE);
            }
        });
        form.add(btnRegister);

        // ── Login button ──
        JButton btnLogin = UIHelper.makeButton("Login", UIHelper.BTN_BLUE);
        btnLogin.setBounds(330, 260, 160, 45);
        btnLogin.addActionListener(e -> {
            String user = tfUser.getText().trim();
            if (user.isEmpty()) {
                showStatus(lblStatus, "Enter your username to log in.", UIHelper.BTN_RED);
                return;
            }
            Player found = PlayerStorage.findByUsername(user);
            if (found != null) {
                currentPlayer = found;
                showStatus(lblStatus,
                    "Welcome back, " + found.name + "!", UIHelper.BTN_GREEN);
            } else {
                showStatus(lblStatus,
                    "Username not found – register first.", UIHelper.BTN_RED);
            }
        });
        form.add(btnLogin);

        // ── Back button ──
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(UIHelper.DARK_BG);
        JButton btnBack = UIHelper.makeButton("← Back", UIHelper.BTN_RED);
        btnBack.setPreferredSize(new Dimension(110, 38));
        btnBack.addActionListener(e -> frame.dispose());
        south.add(btnBack);
        main.add(south, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    // ── Private helpers ──

    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(UIHelper.TEXT_LIGHT);
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return l;
    }

    private JTextField styledField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tf.setBackground(new Color(60, 60, 85));
        tf.setForeground(UIHelper.TEXT_LIGHT);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIHelper.ACCENT, 1),
            new EmptyBorder(4, 6, 4, 6)));
        return tf;
    }

    private void showStatus(JLabel lbl, String msg, Color colour) {
        lbl.setText(msg);
        lbl.setForeground(colour);
    }
}


// ── SCREEN 4 : GAME ──────────────────────────────────────────
/**
 * The Tic-Tac-Toe game board.
 * - Player X is the logged-in player (or "Player X" if no login).
 * - Player O is the second player.
 * - Win / draw is detected automatically.
 * - Winning cells are highlighted in green.
 * - Stats (wins / losses) are saved to file when the game ends.
 */
class GameScreen {

    // ── State ──
    private final JButton[][] cells = new JButton[3][3];
    private boolean xTurn = true;          // X always starts
    private boolean gameOver = false;

    private JLabel lblStatus;
    private JFrame frame;

    // Names shown on the board
    private final String nameX;
    private final String nameO = "Player O";

    public GameScreen(JFrame parentFrame) {
        // Decide display name for X
        if (LoginScreen.currentPlayer != null) {
            nameX = LoginScreen.currentPlayer.name + " (X)";
        } else {
            nameX = "Player X";
        }

        frame = new JFrame();
        UIHelper.setupFrame(frame, "Game");

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIHelper.DARK_BG);
        frame.setContentPane(main);

        main.add(UIHelper.makeHeader("TIC  TAC  TOE"), BorderLayout.NORTH);

        // ── Centre: board + status ──
        JPanel centre = new JPanel(new BorderLayout(0, 10));
        centre.setBackground(UIHelper.DARK_BG);
        centre.setBorder(new EmptyBorder(10, 60, 10, 60));

        // Status label (shows whose turn it is / result)
        lblStatus = new JLabel(nameX + "'s turn", SwingConstants.CENTER);
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblStatus.setForeground(UIHelper.ACCENT);
        centre.add(lblStatus, BorderLayout.NORTH);

        // 3×3 grid
        JPanel board = new JPanel(new GridLayout(3, 3, 5, 5));
        board.setBackground(UIHelper.DARK_BG);
        board.setPreferredSize(new Dimension(320, 320));

        Font cellFont = new Font("SansSerif", Font.BOLD, 56);
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                final int row = r, col = c;
                JButton btn = new JButton();
                btn.setFont(cellFont);
                btn.setFocusable(false);
                btn.setBackground(UIHelper.BOARD_BG);
                btn.setForeground(UIHelper.TEXT_LIGHT);
                btn.setBorder(BorderFactory.createLineBorder(
                    new Color(70, 70, 100), 2));
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btn.addActionListener(e -> handleMove(row, col));
                cells[r][c] = btn;
                board.add(btn);
            }
        }
        centre.add(board, BorderLayout.CENTER);
        main.add(centre, BorderLayout.CENTER);

        // ── Bottom controls ──
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 12));
        controls.setBackground(UIHelper.DARK_BG);

        JButton btnRestart = UIHelper.makeButton("Restart",   UIHelper.BTN_ORANGE);
        JButton btnMenu    = UIHelper.makeButton("Main Menu", UIHelper.BTN_RED);

        btnRestart.setPreferredSize(new Dimension(130, 40));
        btnMenu.setPreferredSize(new Dimension(130, 40));

        btnRestart.addActionListener(e -> resetBoard());
        btnMenu.addActionListener(e -> {
            frame.dispose();
            new MenuScreen();
        });

        controls.add(btnRestart);
        controls.add(btnMenu);
        main.add(controls, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    // ── Called when a cell button is clicked ──
    private void handleMove(int row, int col) {
        if (gameOver) return;
        if (!cells[row][col].getText().isEmpty()) return; // cell taken

        String symbol = xTurn ? "X" : "O";
        cells[row][col].setText(symbol);
        // Colour-code the symbol
        cells[row][col].setForeground(
            xTurn ? new Color(100, 200, 255) : new Color(255, 160, 80));

        // Check results
        int[] winLine = findWinLine(symbol);
        if (winLine != null) {
            highlightWin(winLine);
            String winner = xTurn ? nameX : nameO;
            lblStatus.setText("🎉  " + winner + " wins!");
            lblStatus.setForeground(UIHelper.BTN_GREEN);
            gameOver = true;
            // Save stats if a logged-in player won or lost
            saveStats(xTurn);
        } else if (isBoardFull()) {
            lblStatus.setText("It's a draw! Well played both.");
            lblStatus.setForeground(UIHelper.BTN_ORANGE);
            gameOver = true;
        } else {
            xTurn = !xTurn;
            String next = xTurn ? nameX : nameO;
            lblStatus.setText(next + "'s turn");
            lblStatus.setForeground(UIHelper.ACCENT);
        }
    }

    // ── Reset board for a new round ──
    private void resetBoard() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                cells[r][c].setText("");
                cells[r][c].setBackground(UIHelper.BOARD_BG);
                cells[r][c].setEnabled(true);
            }
        }
        xTurn    = true;
        gameOver = false;
        lblStatus.setText(nameX + "'s turn");
        lblStatus.setForeground(UIHelper.ACCENT);
    }

    // ── Win detection ──
    /**
     * Returns an int[] of 6 values {r0,c0, r1,c1, r2,c2} if 'symbol' has
     * three in a row, or null if no win.
     */
    private int[] findWinLine(String sym) {
        // Rows
        for (int r = 0; r < 3; r++) {
            if (check(sym, r,0, r,1, r,2))
                return new int[]{r,0, r,1, r,2};
        }
        // Columns
        for (int c = 0; c < 3; c++) {
            if (check(sym, 0,c, 1,c, 2,c))
                return new int[]{0,c, 1,c, 2,c};
        }
        // Diagonals
        if (check(sym, 0,0, 1,1, 2,2)) return new int[]{0,0, 1,1, 2,2};
        if (check(sym, 0,2, 1,1, 2,0)) return new int[]{0,2, 1,1, 2,0};
        return null;
    }

    private boolean check(String s, int r0,int c0, int r1,int c1, int r2,int c2) {
        return cells[r0][c0].getText().equals(s)
            && cells[r1][c1].getText().equals(s)
            && cells[r2][c2].getText().equals(s);
    }

    /** Highlights the three winning cells in green and disables all cells. */
    private void highlightWin(int[] line) {
        // Disable entire board
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                cells[r][c].setEnabled(false);

        // Green highlight on winning cells
        for (int i = 0; i < 6; i += 2) {
            cells[line[i]][line[i+1]].setBackground(new Color(50, 160, 80));
        }
    }

    private boolean isBoardFull() {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (cells[r][c].getText().isEmpty()) return false;
        return true;
    }

    /**
     * Update the logged-in player's wins/losses in the file.
     * xWon = true  → X won  → update logged-in player's wins.
     * xWon = false → O won  → update logged-in player's losses.
     */
    private void saveStats(boolean xWon) {
        if (LoginScreen.currentPlayer == null) return; // no login – skip

        Player p = LoginScreen.currentPlayer;
        // X is always the logged-in player in this setup
        if (xWon) {
            p.wins++;
        } else {
            p.losses++;
        }
        PlayerStorage.updatePlayer(p);
        // Refresh currentPlayer from file to stay in sync
        LoginScreen.currentPlayer = PlayerStorage.findByUsername(p.username);
    }
}


// ── SCREEN 5 : SCOREBOARD ────────────────────────────────────
/**
 * Shows all registered players with their wins and losses,
 * read fresh from players.txt every time this screen opens.
 */
class StatsScreen {

    public StatsScreen(JFrame parentFrame) {
        JFrame frame = new JFrame();
        UIHelper.setupFrame(frame, "Scoreboard");

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIHelper.DARK_BG);
        frame.setContentPane(main);

        main.add(UIHelper.makeHeader("SCOREBOARD"), BorderLayout.NORTH);

        // ── Table ──
        String[] columns = {"Name", "Username", "Wins", "Losses"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Player> players = PlayerStorage.loadAll();
        if (players.isEmpty()) {
            model.addRow(new Object[]{"—", "No players yet", "—", "—"});
        } else {
            // Sort by wins descending so top players appear first
            players.sort((a, b) -> b.wins - a.wins);
            for (Player p : players) {
                model.addRow(new Object[]{p.name, p.username, p.wins, p.losses});
            }
        }

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.setBackground(new Color(45, 45, 65));
        table.setForeground(UIHelper.TEXT_LIGHT);
        table.getTableHeader().setBackground(UIHelper.DARK_BG);
        table.getTableHeader().setForeground(UIHelper.ACCENT);
        table.setGridColor(new Color(70, 70, 100));
        table.setSelectionBackground(new Color(80, 120, 180));

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(45, 45, 65));
        scroll.setBorder(new EmptyBorder(10, 20, 10, 20));
        main.add(scroll, BorderLayout.CENTER);

        // ── Back button ──
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        south.setBackground(UIHelper.DARK_BG);
        JButton btnBack = UIHelper.makeButton("← Back to Menu", UIHelper.BTN_RED);
        btnBack.addActionListener(e -> frame.dispose());
        south.add(btnBack);
        main.add(south, BorderLayout.SOUTH);

        frame.setVisible(true);
    }
}


// ── ENTRY POINT ──────────────────────────────────────────────
/**
 * Main class – just launches the intro screen.
 * All navigation is handled inside each screen.
 */
public class Main {
    public static void main(String[] args) {
        // Run on the Event Dispatch Thread (good Swing practice)
        SwingUtilities.invokeLater(() -> new IntroScreen());
    }
}
