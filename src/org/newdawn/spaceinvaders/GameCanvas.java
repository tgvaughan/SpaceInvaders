package org.newdawn.spaceinvaders;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

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
public class GameCanvas extends Canvas {

    /**
     * The strategy that allows us to use accelerate page flipping
     */
    private BufferStrategy strategy;

    /**
     * True if the game is currently "running", i.e. the game loop is looping
     */
    private final boolean gameRunning = true;

    /**
     * The list of all the entities that exist in our game
     */
    private final ArrayList<Entity> entities = new ArrayList<>();

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
    private boolean humansDead;

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
     * Construct our game and set it running.
     */
    public GameCanvas() {

        // setup our canvas size and put it into the content of the frame
        setBounds(0, 0, 800, 600);
        
		// Tell AWT not to bother repainting our canvas since we're
        // going to do that our self in accelerated mode
        setIgnoreRepaint(true);

        // add a key input system (defined below) to our canvas
        // so we can respond to key pressed
        addKeyListener(new KeyInputHandler());

		// initialise the entities in our game so there's something
        // to see at startup
        initEntities();
    }
    

    /**
     * Start a fresh game, this should clear out any old data and create a new
     * set.
     */
    private void startGame() {
        // clear out any existing entities and intialise a new set
        entities.clear();
        initEntities();

        // blank out any keyboard settings we might currently have
        leftPressed = false;
        rightPressed = false;
        firePressed = false;
    }

    /**
     * Initialise the starting state of the entities (ship and aliens). Each
     * entity will be added to the overall list of entities in the game.
     */
    private void initEntities() {
        // create the player ship and place it roughly in the center of the screen
        ship = new ShipEntity(this, "sprites/ship.gif", 370, 550);
        entities.add(ship);

        // create a block of aliens (5 rows, by 12 aliens, spaced evenly)
        alienCount = 0;
        for (int row = 0; row < 1; row++) {
            for (int x = 0; x < 12; x++) {
                Entity alien = new AlienEntity(this, "sprites/alien.gif", 100 + (x * 50), (50) + row * 30);
                entities.add(alien);
                alienCount++;
            }
        }
        
        // Initialise gameLosingFlag
        humansDead = false;
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
     * Returns true if game over condition is met.  In this case the condition
     * is that all invading aliens are deceased.
     * 
     * @return true if condition is met.
     */
    public boolean gameOverConditionMet() {
        return alienCount == 0 || humansDead;
    }
    
    public boolean gameWon() {
        return !humansDead;
    }

    /**
     * The main game loop. This loop is running during all game play as is
     * responsible for the following activities:
     * <p>
     * - Working out the speed of the game loop to update moves - Moving the
     * game entities - Drawing the screen contents (entities, text) - Updating
     * game events - Checking Input
     * <p>
     */
    public void gameLoop() {
        // create the buffering strategy which will allow AWT
        // to manage our accelerated graphics
        createBufferStrategy(2);
        strategy = getBufferStrategy();

        long lastLoopTime = System.currentTimeMillis();

        // keep looping round til the game ends
        while (gameRunning) {
			// work out how long its been since the last update, this
            // will be used to calculate how far the entities should
            // move this loop
            long delta = System.currentTimeMillis() - lastLoopTime;
            lastLoopTime = System.currentTimeMillis();

			// Get hold of a graphics context for the accelerated 
            // surface and blank it out
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            g.setColor(Color.black);
            g.fillRect(0, 0, 800, 600);

            // cycle round asking each entity to move itself
//            if (!waitingForKeyPress) {
                for (Entity entity : entities) {
                    entity.move(delta);
                }
//            }

            for (Entity entity : entities) {
                entity.draw(g);
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

			// finally, we've completed drawing so clear up the graphics
            // and flip the buffer over
            g.dispose();
            strategy.show();
            
            if (gameOverConditionMet())
                return;

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

			// finally pause for a bit. Note: this should run us at about
            // 100 fps but on windows this might vary each loop due to
            // a bad implementation of timer
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
    }

    /**
     * A class to handle keyboard input from the user. The class handles both
     * dynamic input during game play, i.e. left/right and shoot, and more
     * static type input (i.e. press any key to continue)
     *
     * This has been implemented as an inner class more through habbit then
     * anything else. Its perfectly normal to implement this as seperate class
     * if slight less convienient.
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

        /**
         * Notification from AWT that a key has been typed. Note that typing a
         * key means to both press and then release it.
         *
         * @param e The details of the key that was typed.
         */
//        @Override
//        public void keyTyped(KeyEvent e) {
//            // if we hit escape, then quit the game
//            if (e.getKeyChar() == 27) {
//                System.exit(0);
//            }
//        }
    }
}
