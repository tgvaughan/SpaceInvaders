package org.newdawn.spaceinvaders;

import java.awt.Dimension;
import java.awt.HeadlessException;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author Tim Vaughan <tgvaughan@gmail.com>
 */
public class SpaceInvadersApp extends JFrame {
    
    private Game game;

    /**
     * Create new Space Invaders app.
     * @throws HeadlessException 
     */
    public SpaceInvadersApp() throws HeadlessException {
        this.setTitle("Space Invaders");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        
		// get hold the content of the frame and set up the resolution of the game
//		JPanel panel = (JPanel) this.getContentPane();
//		panel.setPreferredSize(new Dimension(800, 600));
//		panel.setLayout(null);
        
        setPreferredSize(new Dimension(800, 600));
        
        pack();
        setResizable(false);
        
        game = new Game(800, 600);
        //panel.add(game);
        add(game);
    }

    /**
     * Start the game loop.
     */
    public void start() {
        game.requestFocus();
        game.gameLoop();
    }
    
    /**
     * Main entry point for app.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        
        SpaceInvadersApp app = new SpaceInvadersApp();

        app.setVisible(true);
        app.start();
        
    }
    
}
