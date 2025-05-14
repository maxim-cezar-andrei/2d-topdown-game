import javax.swing.*;
import java.awt.*;

public class PauseMenuPanel extends JPanel {
    public PauseMenuPanel(Runnable onResume, Runnable onMainMenu, Runnable onSave, Runnable onLoad, Runnable onOptions, Runnable onExit) {
        setOpaque(false);

        setLayout(new GridBagLayout());  // Centrează conținutul

        JPanel menuBox = new JPanel();
        menuBox.setLayout(new GridLayout(4, 1, 15, 15));  // 4 butoane, spațiate
        menuBox.setBackground(new Color(30, 30, 30));
        menuBox.setBorder(BorderFactory.createLineBorder(Color.CYAN, 3));

        // Creăm butoanele
        JButton mainMenuButton = createStyledButton("Main Menu", onMainMenu);
        JButton resumeButton = createStyledButton("Resume", onResume);
        JButton saveButton = createStyledButton("Save", onSave);
        JButton loadButton     = createStyledButton("Load", onLoad);
        JButton optionsButton  = createStyledButton("Options", onOptions);
        JButton exitButton     = createStyledButton("Exit", onExit);

        // Adăugăm butoanele în panou
        menuBox.add(resumeButton);
        menuBox.add(saveButton);
        menuBox.add(loadButton);
        menuBox.add(optionsButton);
        menuBox.add(mainMenuButton);
        menuBox.add(exitButton);

        add(menuBox);
    }

    private JButton createStyledButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(Color.DARK_GRAY);
        button.setForeground(Color.CYAN);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBorder(BorderFactory.createLineBorder(Color.MAGENTA, 2));

        button.addActionListener(e -> action.run());

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(50, 50, 50));
                button.setForeground(Color.MAGENTA);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.DARK_GRAY);
                button.setForeground(Color.CYAN);
            }
        });


        return button;
    }
    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
        super.paintComponent(g);
    }
}

