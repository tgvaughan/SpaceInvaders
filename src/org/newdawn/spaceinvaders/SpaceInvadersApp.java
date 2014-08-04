package org.newdawn.spaceinvaders;

import java.awt.Dimension;
import java.awt.HeadlessException;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author Tim Vaughan <tgvaughan@gmail.com>
 */
public class SpaceInvadersApp extends JFrame {
    
    private GameCanvas game;

    /**
     * Create new Space Invaders app.
     * @throws HeadlessException 
     */
    public SpaceInvadersApp() throws HeadlessException {
        setTitle("Space Invaders");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);        
        
        game = new GameCanvas(800, 600);
        add(game);
        
        pack();
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
        
    }
    
}
