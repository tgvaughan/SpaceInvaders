package org.newdawn.spaceinvaders;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

        // Set up menu hierarchy:
        
        JMenuBar menuBar = new JMenuBar();
        
        JMenu menuGame = new JMenu("Game");
        menuGame.setMnemonic(KeyEvent.VK_G);
        
        JMenuItem menuItemGameNew = new JMenuItem("New", KeyEvent.VK_N);
        menuItemGameNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        menuItemGameNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                start();
            }
        });
        menuGame.add(menuItemGameNew);
        
        JMenuItem menuItemGamePause = new JMenuItem("Pause", KeyEvent.VK_P);
        menuItemGamePause.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        menuItemGamePause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                game.gameStop();
            }
        });
        menuGame.add(menuItemGamePause);
        
        menuGame.addSeparator();
        
        JMenuItem menuItemGameExit = new JMenuItem("Exit", KeyEvent.VK_X);
        menuItemGameExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        menuItemGameExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        menuGame.add(menuItemGameExit);
        
        menuBar.add(menuGame);
        
        JMenu menuHelp = new JMenu("Help");
        menuHelp.setMnemonic(KeyEvent.VK_H);
        
        JMenuItem menuItemHelpAbout = new JMenuItem("About", KeyEvent.VK_A);
        menuHelp.add(menuItemHelpAbout);
        
        menuBar.add(menuHelp);
        
        setJMenuBar(menuBar);
        
        // Set up game canvas:
        game = new GameCanvas();
        add(game);
        
        // Cause outer components to adjust to the the size of the canvas:
        pack();
        
        // Initialise canvas buffer - must be done following pack.
        game.initBuffer();
        
        // Our game canvas has a fixed size - don't let user's change
        // the window size:
        setResizable(false);
        
        // Define what happens when a user closes the game window:
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Start the game loop.
     */
    public void start() {
        game.requestFocus();
        game.gameStart();
    }
    
    /**
     * Main entry point for app.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        
        SpaceInvadersApp spaceInvaders = new SpaceInvadersApp();

        spaceInvaders.setVisible(true);
//        spaceInvaders.start();
//        System.exit(0);
        
    }
    
}
