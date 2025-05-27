package main;

import javax.swing.*;
import java.awt.*;

public class FadePanel extends JPanel {
    private float opacity = 0f;

    public void setOpacity(float value) {
        this.opacity = value;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}