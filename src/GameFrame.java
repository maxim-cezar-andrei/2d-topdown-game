import javax.swing.*;
import java.awt.*;

class GameFrame extends JFrame {
    public GameFrame(boolean withFadeIn) {
        setTitle("Shadow Heist - Map");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GameMap gameMap = new GameMap(this);


        setContentPane(gameMap);

        setUndecorated(false);
        pack();                    // Ajustează fereastra după dimensiunea hărții
        setResizable(true);       //  Permitem redimensionarea
        setLocationRelativeTo(null); // Centrează pe ecran
        setVisible(true);
        setSize(getWidth(),getHeight());
        if (withFadeIn) {
            FadePanel fade = new FadePanel();
            fade.setBounds(0, 0, getWidth(), getHeight());
            fade.setOpaque(false);
            setGlassPane(fade);
            fade.setVisible(true);

            new Thread(() -> {
                for (int i = 20; i >= 0; i--) {
                    float alpha = i / 20f;
                    fade.setOpacity(alpha);
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                fade.setVisible(false);
            }).start();
        }
    }

    public GameFrame() {
        this(false);
    }
}
