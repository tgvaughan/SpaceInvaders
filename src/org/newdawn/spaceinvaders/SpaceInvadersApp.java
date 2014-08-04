package org.newdawn.spaceinvaders;

import java.awt.HeadlessException;
import javax.swing.JFrame;

/**
 * @author Tim Vaughan <tgvaughan@gmail.com>
 */
public class SpaceInvadersApp extends JFrame {
    
    private final GameCanvas game;

    /**
     * Create new Space Invaders app.
     * @throws HeadlessException 
     */
    public SpaceInvadersApp() throws HeadlessException {
        setTitle("Space Invaders");

        game = new GameCanvas();
        add(game);
        
        pack();
        setResizable(false);
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
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
        
        SpaceInvadersApp spaceInvaders = new SpaceInvadersApp();

        spaceInvaders.setVisible(true);
        spaceInvaders.start();
        System.exit(0);
        
    }
    
}
