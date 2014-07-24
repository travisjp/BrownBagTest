/* 
 * File: Breakout.java 
 * ------------------- 
 * This file will eventually implement the game of Breakout. 
 */ 

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.Random;

import acm.graphics.GLabel;
import acm.graphics.GObject;
import acm.graphics.GOval;
import acm.graphics.GRect;
import acm.program.GraphicsProgram;

public class Breakout extends GraphicsProgram 
{ 
	/** Serializable UID */
	private static final long serialVersionUID = 5589145633014973459L;
	
	/** Width and height of application window in pixels */ 
	public static final int APPLICATION_WIDTH = 400; 
	public static final int APPLICATION_HEIGHT = 600;
	
	/** Dimensions of game board (usually the same) */ 
	private static final int WIDTH = APPLICATION_WIDTH; 
	private static final int HEIGHT = APPLICATION_HEIGHT; 

	/** Dimensions of the paddle */ 
	private static final int PADDLE_WIDTH = 60; 
	private static final int PADDLE_HEIGHT = 10; 

	/** Offset of the paddle up from the bottom */ 
	private static final int PADDLE_Y_OFFSET = 30; 

	/** Number of bricks per row */ 
	private static final int NBRICKS_PER_ROW = 10; 

	/** Number of rows of bricks */ 
	private static final int NBRICK_ROWS = 10; 

	/** Separation between bricks */ 
	private static final int BRICK_SEP = 4; 

	/** Width of a brick */ 
	private static final int BRICK_WIDTH = (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW; 

	/** Height of a brick */ 
	private static final int BRICK_HEIGHT = 8; 

	/** Radius of the ball in pixels */ 
	private static final int BALL_RADIUS = 10; 

	/** Offset of the top brick row from the top */ 
	private static final int BRICK_Y_OFFSET = 70; 

	/** Number of turns */ 
	private static final int NTURNS = 3;

	private static final double START_BALL_MAX_DX = 3;
	private static final double START_BALL_DY = 3;

	/** Framerate delay */
	private static final double DELAY_MIN = 5;
	private static final double DELAY_MAX = 25;
	private static final double BRICK_SPEED_INC = 1;
	private static final double PADDLE_SPEED_INC = 0.5;
	
	private int lives;
	private int liveBlocks;
	private double ballDx, ballDy;	// ball speed components
	private double lastX, lastY;	// ball previous position
	
	private GRect block[][];		// two dimensional array of GRect block objects
	private GRect paddle;
	private GOval ball;				
	private Random random;			// random number generator
	private double delay;
	
	public void run() 
	{ 
		initializeGame();
		
		boolean result = runGame();

		// erase ball
		remove(ball);
		ball = null;
		
		// make paddle invisible
		paddle.setVisible(false);
		
		String str;
		
		if (result == true)
			str = new String("You win!! CONGRATULATIONS!!!");
		else
			str = new String("You lose! Oh the humanity!!");

		GLabel label = new GLabel(str);
		label.setLocation((APPLICATION_WIDTH -label.getWidth()) / 2, APPLICATION_HEIGHT /2);
		add(label);
		
	}	// end run 
	
	/**
	 *  Initializes all in-game variables
	 */
	public void initializeGame()
	{
		int x, y;			// temp variables for drawing objects
		int i, j;			
		
		lives = NTURNS;
		liveBlocks = NBRICKS_PER_ROW * NBRICK_ROWS;
		delay = DELAY_MAX;
		
		setSize(APPLICATION_WIDTH + BRICK_SEP, APPLICATION_HEIGHT);
		
		random = new Random();

		// create the ball
		ball = new GOval((APPLICATION_WIDTH - BALL_RADIUS) / 2, (APPLICATION_HEIGHT - BALL_RADIUS) / 2, BALL_RADIUS, BALL_RADIUS);
		ball.setColor(Color.black);
		ball.setFilled(true);
		
		resetBall();
		
		add(ball);
		
		// create the block object array
		block = new GRect[NBRICKS_PER_ROW][NBRICK_ROWS];
		Color c = new Color(0,0,0);
		
		for (i = 0, y = BRICK_Y_OFFSET; i < NBRICK_ROWS; i++)
		{
			// sets the color of the brick rows
			if(i < 2)
				c = Color.red;
			else if (i < 4)
				c = Color.orange;
			else if (i < 6)
				c = Color.yellow;
			else if (i < 8)
				c = Color.green;
			else
				c = Color.cyan;
			
			for (j = 0, x = BRICK_SEP; j < NBRICKS_PER_ROW; j++)
			{
				// Create the array of blocks
				block[j][i] = new GRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
				block[j][i].setColor(c);
				block[j][i].setFilled(true);
				
				// adds the block to the cavnas
				add(block[j][i]);
				
				x += BRICK_WIDTH + BRICK_SEP;
				
			}	// end for j
			
			y += BRICK_HEIGHT + BRICK_SEP;
			
		}	// end for i
		
		// create the paddle
		paddle = new GRect((APPLICATION_WIDTH - PADDLE_WIDTH) / 2, (APPLICATION_HEIGHT - PADDLE_Y_OFFSET), PADDLE_WIDTH, PADDLE_HEIGHT);
		paddle.setColor(Color.black);
		paddle.setFilled(true);
		
		add(paddle);
		
		// enable mouse input
		addMouseListeners();
		
		return;
		
	}	// end initalizeGame

	/**
	 *  This is the main loop of the game and returns a boolean value depending on whether
	 *  the player won or lost.
	 *  
	 * @return	- true or false depending on whether the player won or lost
	 */
	public boolean runGame()
	{
		do
		{
			moveBall();				// move the ball

			checkForCollision();	// check for collision with bricks, walls, and paddle
			
			if(lives <= 0)
				return(false);
			
			pause(delay);

		} while (liveBlocks > 0);
		
		return (true);
		
	}	// end runGame


	private void checkForCollision() 
	{
		double x = ball.getX() + BALL_RADIUS;
		double y = ball.getY() + BALL_RADIUS;
		
		// ball can collide with walls
		// OOB left
		if(x < 0)
		{
			ballDx = -ballDx;	// invert velocity

			ball.move(-lastX, 0);
		}	// end if OOB LEFT
		else if(x > APPLICATION_WIDTH)
		{
			ballDx = -ballDx;
			
			ball.move(ballDx - (APPLICATION_WIDTH - lastX), 0);
		}	// end if OOB RIGHT
		
		if(y < 0)
		{	
			ballDy = -ballDy;
			
			ball.move(0, -lastY);
		}	// end OOB TOP
		else if(y > APPLICATION_HEIGHT)			// death!
		{
			lives -= 1;			// deduct life
			resetBall();
			
		}	// end OOB BOTTOM

		GObject obj = getElementAt(x, y);

		// no collision
		if (obj == ball || obj == null)
			return;

		// ball can collide with paddle
		if (obj == paddle)
		{
			ballDy = -ballDy;
			
			delay -= PADDLE_SPEED_INC;
			
			if(delay < DELAY_MIN)
				delay = DELAY_MIN;
			
			return;
		}	// end if paddle

		
		// ball can collide with brick--time consuming
		for (int i = 0; i < NBRICK_ROWS; i++)
		{
			for (int j = 0; j < NBRICKS_PER_ROW; j++)
			{
				if(obj == block[i][j])
				{
					remove(block[i][j]);
					block[i][j] = null;
					liveBlocks--;
					
					ballDy = -ballDy;
					
					delay -= BRICK_SPEED_INC;
					if(delay < DELAY_MIN)
						delay = DELAY_MIN;
					
					return;
				}	// end if obj is block
			}	// end for j			
		}	// end for i
		
		return;
	}	// end checkForCollision

	private void resetBall() 
	{
		// reset speed
		delay = DELAY_MAX;
		
		ball.setLocation((APPLICATION_WIDTH - BALL_RADIUS) / 2, (APPLICATION_HEIGHT - BALL_RADIUS) / 2);
		
		// Starts ballDx at some random value between + and - START_BALL_MAX_DX
		ballDx = (START_BALL_MAX_DX * random.nextDouble()) * (random.nextDouble() > 0.5 ? (1) : (-1));
		ballDy = START_BALL_DY;

		lastX = ball.getX() + BALL_RADIUS;
		lastY = ball.getY() + BALL_RADIUS;
		
		return;		
	}	// end resetBall

	private void moveBall() 
	{
		lastX = ball.getX() + BALL_RADIUS;
		lastY = ball.getY() + BALL_RADIUS;
		ball.move(ballDx, ballDy);		
		
		return;
	}	// end moveBall

	public void mouseMoved(MouseEvent e)
	{
		int x = e.getX() - (PADDLE_WIDTH / 2);
		
		// do not let ball go past edge of screen
		if(x > APPLICATION_WIDTH - PADDLE_WIDTH)
			x = APPLICATION_WIDTH - PADDLE_WIDTH;
		else if (x < 0)
			x = 0;
		
		paddle.setLocation(x, paddle.getY());
	}	// end mouseMoved
	
}	// end class Breakout