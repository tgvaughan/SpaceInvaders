package spaceinvaders;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * The main hook of our game. This class with both act as a manager for the
 * display and central mediator for the game logic.
 *
 * Display management will consist of a loop that cycles round all entities in
 * the game asking them to move and then drawing them in the appropriate place.
 * With the help of an inner class it will also allow the player to control the
 * main ship.
 *
 * As a mediator it will be informed when entities within our game detect events
 * (e.g. alien killed, played died) and will take appropriate game actions.
 *
 * @author Kevin Glass
 */
public class GamePanel extends JPanel {
    
    /**
     * The list of all the entities that exist in our game
     */
    private final ArrayList<Entity> entities = new ArrayList<Entity>();

    /**
     * The list of entities that need to be removed from the game this loop
     */
    private final ArrayList removeList = new ArrayList();

    /**
     * The entity representing the player
     */
    private Entity ship;

    /**
     * The speed at which the player's ship should move (pixels/sec)
     */
    private final double moveSpeed = 300;

    /**
     * The time at which last fired a shot
     */
    private long lastFire = 0;

    /**
     * The interval between our players shot (ms)
     */
    private final long firingInterval = 500;
    
    /**
     * The number of aliens left on the screen
     */
    private int alienCount;
    
    /**
     * Set to true if an alien reaches the bottom of the screen or
     * collides with the player's ship.
     */
    private boolean humansDead = false;
    
    /**
     * Set to true if a game is in progress.
     */
    private boolean gameInProgress = false;

    /**
     * True if the left cursor key is currently pressed
     */
    private boolean leftPressed = false;

    /**
     * True if the right cursor key is currently pressed
     */
    private boolean rightPressed = false;

    /**
     * True if we are firing
     */
    private boolean firePressed = false;

    /**
     * True if game logic needs to be applied this loop, normally as a result of
     * a game event
     */
    private boolean logicRequiredThisLoop = false;
    
    /**
     * Timer used to run game.
     */
    private final Timer timer;
    
    /**
     * Keeps track of score, which is defined as the number of game
     * iterations required to kill all of the aliens.
     */
    private int iterations;
    
    /**
     * Fonts used on game panel.
     */
    private final Font titleFont1, titleFont2, bigFont, scoreFont;
    
    /**
     * App this panel belongs to.
     */
    private final SpaceInvadersApp app;
    
    /**
     * Construct our game and set it running.
     * @param app App this panel belongs to.
     */
    public GamePanel(SpaceInvadersApp app) {

        // Set preferred size of panel.
        setPreferredSize(new Dimension(800, 600));

        // setup our canvas size and put it into the content of the frame
        setBounds(0, 0, 800, 600);

        // add a key input system (defined below) to our canvas
        // so we can respond to key pressed
        addKeyListener(new KeyInputHandler());

		// initialise the entities in our game so there's something
        // to see at startup
        initEntities();
        
        // Set up timer that calls the game iteration method.
        timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameIterate(100);
            }
        });
        
        // Set up fonts:
        titleFont1 = new Font(Font.SANS_SERIF, Font.BOLD, 40);
        titleFont2 = new Font(Font.SANS_SERIF, Font.PLAIN, 40);
        bigFont = new Font(Font.SANS_SERIF, Font.BOLD, 100);
        scoreFont = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
        
        // Record app object:
        this.app = app;
    }

    /**
     * Paint the panel.  Called whenever the panel needs to be redrawn.
     * Displays relevant game messages when game is not running.
     * 
     * @param g 
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.setColor(Color.black);
        g.fillRect(0, 0, 800, 600);

        for (Entity entity : entities) {
            entity.draw(g);
        }
        
        if (gameInProgress) {
            if (!timer.isRunning()) {
                g.setFont(bigFont);
                g.setColor(Color.white);
                g.drawString("PAUSED", 200, 300);
            }
        } else {
            if (isGameOverConditionMet()) {
                g.setFont(bigFont);
                g.setColor(Color.yellow);
                g.drawString("GAME OVER", 50, 300);
            } else {
                g.setFont(titleFont1);
                g.setColor(Color.yellow);
                g.drawString("SPACE INVADERS", 200, 250);
                g.setFont(titleFont2);
                g.setColor(Color.white);
                g.drawString("Use Ctrl-N to begin new game.", 80, 350);
            }
        }
        
        g.setFont(scoreFont);
        g.setColor(Color.white);
        g.drawString("Score: " + getScore(), 600, 20);
    }
    

    /**
     * Initialise the starting state of the entities (ship and aliens). Each
     * entity will be added to the overall list of entities in the game.
     */
    private void initEntities() {
        // clear out any existing entities and intialise a new set
        entities.clear();
        
        // create the player ship and place it roughly in the center of the screen
        ship = new ShipEntity(this, "sprites/ship.gif", 370, 550);
        entities.add(ship);

        // create a block of aliens (5 rows, by 12 aliens, spaced evenly)
        alienCount = 0;
        for (int row = 0; row < 5; row++) {
            for (int x = 0; x < 12; x++) {
                Entity alien = new AlienEntity(this, "sprites/alien.gif", 100 + (x * 50), (50) + row * 30);
                entities.add(alien);
                alienCount++;
            }
        }

    }

    /**
     * Notification from a game entity that the logic of the game should be run
     * at the next opportunity (normally as a result of some game event)
     */
    public void updateLogic() {
        logicRequiredThisLoop = true;
    }

    /**
     * Remove an entity from the game. The entity removed will no longer move or
     * be drawn.
     *
     * @param entity The entity that should be removed
     */
    public void removeEntity(Entity entity) {
        removeList.add(entity);
    }

    /**
     * Notification that an alien has been killed
     */
    public void notifyAlienKilled() {
        // reduce the alient count, if there are none left, the player has won!
        alienCount--;

        if (alienCount == 0) {
            return;
        }

        for (Entity entity : entities) {
            if (entity instanceof AlienEntity) {
                // speed up by 2%
                entity.setHorizontalMovement(entity.getHorizontalMovement() * 1.02);
            }
        }
    }
    
    /**
     * Notification that an alien has landed
     */
    public void notifyHumansDead() {
        humansDead = true;
    }

    /**
     * Attempt to fire a shot from the player. Its called "try" since we must
     * first check that the player can fire at this point, i.e. has he/she
     * waited long enough between shots
     */
    public void tryToFire() {
        // check that we have waiting long enough to fire
        if (System.currentTimeMillis() - lastFire < firingInterval) {
            return;
        }

        // if we waited long enough, create the shot entity, and record the time.
        lastFire = System.currentTimeMillis();
        ShotEntity shot = new ShotEntity(this, "sprites/shot.gif", ship.getX() + 10, ship.getY() - 30);
        entities.add(shot);
    }
    
    /**
     * Start a fresh game.
     */
    public void startGame() {
        initEntities();

        // blank out any keyboard settings we might currently have
        leftPressed = false;
        rightPressed = false;
        firePressed = false;

        // Initialise game flags:
        gameInProgress = true;
        humansDead = false;
        
        // Reset score
        iterations = 0;
        
        // Start timer that iterates the game state
        timer.start();
    }
    
    /**
     * Stop/pause the game.
     */
    public void pauseGame() {
        if (timer.isRunning()) {
            timer.stop();
            
            repaint(); // Trigger repaint to display PAUSED message.
        } else
            timer.start();
    }
        
    /**
     * Display wining/loosing message then reset game.
     */
    private void endGame() {
        timer.stop();
        gameInProgress = false;
        

        app.gameEnded();
    }
        
    /**
     * Returns true if game over condition is met.  In this case the condition
     * is that all invading aliens are deceased.
     * 
     * @return true if condition is met.
     */
    public boolean isGameOverConditionMet() {
        return alienCount == 0 || humansDead;
    }
    
    public boolean isGameWon() {
        return !humansDead;
    }
    
    /**
     * Retrieve current game score (out of 1000) or score of last game.
     * @return game score
     */
    public int getScore() {
        return iterations > 500 ? 0 : (500 - iterations)*500;
    }

    
    /**
     * Iterate game state.  This method is responsible for:
     * <p>
     * - Moving the game entities
     * - Drawing the screen contents (entities, text)
     * - Updating game events
     * - Checking game Input
     * <p>
     * @param delta Number of milliseconds to increment state by.
     */
    public void gameIterate(long delta) {
        
        // Increment score:
        iterations += 1;

        // cycle round asking each entity to move itself
        for (Entity entity : entities) {
            entity.move(delta);
        }

        // brute force collisions, compare every entity against
        // every other entity. If any of them collide notify 
        // both entities that the collision has occured
        for (int p = 0; p < entities.size(); p++) {
            for (int s = p + 1; s < entities.size(); s++) {
                Entity me = (Entity) entities.get(p);
                Entity him = (Entity) entities.get(s);
                
                if (me.collidesWith(him)) {
                    me.collidedWith(him);
                    him.collidedWith(me);
                }
            }
        }

        // remove any entity that has been marked for clear up
        entities.removeAll(removeList);
        removeList.clear();
        
        // if a game event has indicated that game logic should
        // be resolved, cycle round every entity requesting that
        // their personal logic should be considered.
        if (logicRequiredThisLoop) {
            for (Entity entity : entities) {
                entity.doLogic();
            }
            
            logicRequiredThisLoop = false;
        }
        
        // Force component to repaint itself following entity movement.
        repaint();
        
        if (isGameOverConditionMet()) {
            endGame();
            return;
        }

        // resolve the movement of the ship. First assume the ship 
        // isn't moving. If either cursor key is pressed then
        // update the movement appropraitely
        ship.setHorizontalMovement(0);

        if ((leftPressed) && (!rightPressed)) {
            ship.setHorizontalMovement(-moveSpeed);
        } else if ((rightPressed) && (!leftPressed)) {
            ship.setHorizontalMovement(moveSpeed);
        }
        
        // if we're pressing fire, attempt to fire
        if (firePressed) {
            tryToFire();
        }
    }

    /**
     * A class to handle keyboard input from the user. The class handles both
     * dynamic input during game play, i.e. left/right and shoot, and more
     * static type input (i.e. press any key to continue)
     *
     * This has been implemented as an inner class more through habit than
     * anything else. Its perfectly normal to implement this as separate class
     * if slight less convenient.
     *
     * @author Kevin Glass
     */
    private class KeyInputHandler extends KeyAdapter {

        /**
         * Notification from AWT that a key has been pressed. Note that a key
         * being pressed is equal to being pushed down but *NOT* released. Thats
         * where keyTyped() comes in.
         *
         * @param e The details of the key that was pressed
         */
        @Override
        public void keyPressed(KeyEvent e) {
			// if we're waiting for an "any key" typed then we don't 
            // want to do anything with just a "press"

            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                leftPressed = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                rightPressed = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                firePressed = true;
            }
        }

        /**
         * Notification from AWT that a key has been released.
         *
         * @param e The details of the key that was released
         */
        @Override
        public void keyReleased(KeyEvent e) {
			// if we're waiting for an "any key" typed then we don't 
            // want to do anything with just a "released"

            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                leftPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                rightPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                firePressed = false;
            }
        }
    }
}
