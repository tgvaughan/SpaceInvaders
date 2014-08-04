package org.newdawn.spaceinvaders;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * Main application class for the Space Invaders game.
 * 
 * @author Tim Vaughan <tgvaughan@gmail.com>
 */
public class SpaceInvadersApp extends JFrame {
    
    private final GameCanvas game;

    /**
     * Create new Space Invaders application.
     * @throws HeadlessException 
     */
    public SpaceInvadersApp() throws HeadlessException {
        setTitle("Space Invaders");

        JMenuBar menuBar = new JMenuBar();
        
        JMenu menuGame = new JMenu("Game");
        menuGame.setMnemonic(KeyEvent.VK_G);
        
        JMenuItem menuItemGameNew = new JMenuItem("New", KeyEvent.VK_N);
        menuItemGameNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        menuGame.add(menuItemGameNew);
        
        JMenuItem menuItemGamePause = new JMenuItem("Pause", KeyEvent.VK_P);
        menuItemGamePause.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        menuGame.add(menuItemGamePause);
        
        menuGame.addSeparator();
        
        JMenuItem menuItemGameExit = new JMenuItem("Exit", KeyEvent.VK_X);
        menuItemGameExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        menuGame.add(menuItemGameExit);
        
        menuBar.add(menuGame);
        
        JMenu menuHelp = new JMenu("Help");
        menuHelp.setMnemonic(KeyEvent.VK_H);
        
        JMenuItem menuItemHelpAbout = new JMenuItem("About", KeyEvent.VK_A);
        menuHelp.add(menuItemHelpAbout);
        
        menuBar.add(menuHelp);
        
        setJMenuBar(menuBar);
        
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
