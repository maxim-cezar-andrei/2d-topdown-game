import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.List;

class MainMenu extends JFrame {
    private FadePanel overlay;
    private Font customFont;

    public MainMenu() {
        setTitle("Shadow Heist - Main Menu");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel backgroundLabel = new JLabel();
        backgroundLabel.setBounds(0, 0, 800, 600);
        try {
            BufferedImage originalImage = ImageIO.read(new File("assets/backgrounds/mainmenu.png"));
            Image scaledImage = originalImage.getScaledInstance(800, 600, Image.SCALE_SMOOTH);
            backgroundLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            System.out.println("Background image not found.");
        }
        setContentPane(backgroundLabel);
        backgroundLabel.setLayout(null);

        customFont = new Font("Arial", Font.BOLD, 28);
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("assets/fonts/orbitron.ttf")).deriveFont(28f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (IOException | FontFormatException e) {
            System.out.println("Font not found, using default.");
        }

        JPanel titlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, Color.CYAN, getWidth(), 0, Color.MAGENTA);
                g2.setPaint(gp);
                g2.setFont(customFont.deriveFont(42f));
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.drawString("SHADOW HEIST", 10, 45);
            }
        };
        titlePanel.setOpaque(false);
        titlePanel.setBounds(50, 30, 500, 60);
        backgroundLabel.add(titlePanel);

        JButton startButton = createStyledButton("Start", customFont);
        startButton.setBounds(50, 160, 200, 40);
        backgroundLabel.add(startButton);

        JButton loadButton = createStyledButton("Load", customFont);
        loadButton.setBounds(50, 220, 200, 40);
        backgroundLabel.add(loadButton);


        JButton highscoresButton = createStyledButton("Highscores", customFont);
        highscoresButton.setBounds(50, 280, 200, 40);
        backgroundLabel.add(highscoresButton);

        JButton exitButton = createStyledButton("Exit", customFont);
        exitButton.setBounds(50, 340, 200, 40);
        backgroundLabel.add(exitButton);

        overlay = new FadePanel();
        overlay.setOpaque(false);
        overlay.setBounds(0, 0, 800, 600);

        backgroundLabel.add(overlay);
        backgroundLabel.setComponentZOrder(overlay, 0);

        setVisible(true);

        startButton.addActionListener(e -> {
            new Thread(() -> {
                for (int i = 0; i <= 20; i++) {
                    float alpha = i / 20f;
                    overlay.setOpacity(alpha);
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                dispose();
                new GameFrame(true);
            }).start();
        });

        exitButton.addActionListener(e -> System.exit(0));

        loadButton.addActionListener(e -> {
            DataBaseManager db = new DataBaseManager();
            GameState state = db.loadLastGame();
            db.close();

            if (state != null) {
                int mapId = state.getMapId();
                System.out.println("Map ID loaded: " + mapId);

                JFrame gameFrame = new JFrame("Shadow Heist");
                gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                gameFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                gameFrame.setUndecorated(true);


                JPanel mapPanel = switch (mapId) {
                    case 0 -> new GameMap(gameFrame, state);
                    case 1 -> new GameMap1(gameFrame, state);
                    case 2 -> new GameMap2(gameFrame, state);
                    case 3 -> new GameMap3(gameFrame, state);
                    default -> null;
                };

                if (mapPanel != null) {
                    gameFrame.setContentPane(mapPanel);
                    gameFrame.setVisible(true); // 👈 IMPORTANT
                    dispose(); // închide meniul
                } else {
                    JOptionPane.showMessageDialog(null, "Map ID invalid.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "No saved game found.");
            }
        });

    }

    private JButton createStyledButton(String text, Font font) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setBackground(Color.BLACK);
        button.setForeground(Color.CYAN);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.MAGENTA));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(20, 20, 20));
                button.setForeground(Color.MAGENTA);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.BLACK);
                button.setForeground(Color.CYAN);
            }
        });

        return button;
    }
}