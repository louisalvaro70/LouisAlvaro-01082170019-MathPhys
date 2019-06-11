import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/*
    MatFis pertemuan 2
    Note that every dimension-related are measured in pixel
    Except for angle, which is measured in radian
    Explain how parabolic motion of projectile works.
    What is the difference between mapping code in Cartesian coordinates and pixel coordinates?
    
    TODO:
     1. Add a text field to adjust bullet's velocity
     2. Make cannon able to shoot more than one bullet
     3. Limit the amount of bullet in the cannon
     4. Add wind force, with its direction (which impacts acceleration on x-axis and y-axis; use Newton's second law)
     5. Make a shooter game with simple moving target (yes, over-achievers, I need SIMPLE)
	 
    Extra:
    Q: Does this mean I can make a bullet hell game for my final project?
    A: Yes, but since the concept is already explained in class, you won't get Liv's extra brownie point.
 */


class Shooter {
    private JFrame frame;
    private JTextField bulletVelocity;

    // game area
    private Bullet bullet = null;
    double time = 0;
    double timeIncrement = 0.05;
    private Cannon cannon;

    private static final String INSTRUCTION = "Welcome to Cannon Simulation!\n" + 
                    "\nMove cannon's position = W A S D\n" +
                    "Move shooting direction = Left | Right \n" +
                    "Launch bullet = Space\n" +
                    "\nThere can only one bullet at a time";

    private int cpSize = 230;      // set control panel's width

    public Shooter() {
        // setup the frame
        frame = new JFrame("Graphing App");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setFocusable(true);
        frame.setVisible(true);

        // setup control panel itself
        JPanel controlPanel = new JPanel();
        createControlPanel(controlPanel);
        controlPanel.setBounds(0, 0, cpWidth, 300);
        frame.add(controlPanel);
        JTextArea instruction = new JTextArea(INSTRUCTION);
        instruction.setBounds(5, 5, cpSize - 5, frame.getHeight());
        frame.add(instruction);
        labelBulletVelocity = new JLabel ("Bullet Velocity");
        panelBulletVelocity.add(labelBulletVelocity);
        bulletVelocity = new JTextField(2);
        panelBulletVelocity.add(bulletVelocity);

        // setup drawing area
        DrawingArea drawingArea = new DrawingArea();
        cannon = new Cannon(drawingArea.graphScale / 2, drawingArea.originX, drawingArea.originY);
        frame.add(drawingArea);

        // Keyboard shortcuts
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_SPACE:
                        bullet = new Bullet(cannon.barrelWidth / 2, (int) cannon.getBarrelMouthX(), (int) cannon.getBarrelMouthY(), cannon.angle, time);
                        bullet.shoot();
                        break;
                    case KeyEvent.VK_LEFT:
                        cannon.rotateLeft();
                        break;
                    case KeyEvent.VK_RIGHT:
                        cannon.rotateRight();
                        break;
                    case KeyEvent.VK_W:
                        cannon.moveUp();
                        break;
                    case KeyEvent.VK_A:
                        cannon.moveLeft();
                        break;
                    case KeyEvent.VK_S:
                        cannon.moveDown();
                        break;
                    case KeyEvent.VK_D:
                        cannon.moveRight();
                        break;
                }
            }
        });
		
		drawingArea.animator.start();
    }

    class DrawingArea extends JPanel {
        final int graphScale = 30;
        int originX;        // the origin points (0, 0)
        int originY;
        int lengthX;        // how many numbers shown along absis and ordinate
        int lengthY;
        Image drawingArea;
        Thread animator;    // thread to draw the graph

        // setup the drawing area
        public DrawingArea() {
            super(null);
            setBounds(cpSize, 0, frame.getWidth() - cpSize, frame.getHeight());
            drawingArea = createImage(getWidth(), getHeight());

            originX = getWidth() / 4;
            originY = getHeight() / 4;
            lengthX = ((getWidth() - originX) / (graphScale));
            lengthY = ((getHeight() - originY) / (graphScale));

            // trigger drawing process
            drawingArea = createImage(frame.getWidth() - cpSize,
                    frame.getHeight());
            animator = new Thread(this::eventLoop);
        }

        void eventLoop() {
			drawingArea = createImage(frame.getWidth() - cpSize, frame.getHeight());
            while (true) {
                update();
                render();
                printScreen();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    break;
                }
            }
        }

        void update() {
            time += timeIncrement;
            if (bullet != null && bullet.isShot()) {
                bullet.move(time);
                if (bullet.positionY > getHeight()) {
                    bullet.stopShoot();
                }
            }
        }

        void render() {
			if (drawingArea != null) {
				//get graphics of the image where coordinate and function will be drawn
				Graphics g = drawingArea.getGraphics();

				// clear screen
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth(), getHeight());

				g.setColor(Color.black);
				//draw the x-axis and y-axis
				g.drawLine(0, originY, getWidth(), originY);
				g.drawLine(originX, 0, originX, getHeight());

				//print numbers on the x-axis and y-axis, based on the scale
				for (int i = 0; i < lengthX; i++) {
					g.drawString(Integer.toString(i), (originX + (i * graphScale)), originY);
					g.drawString(Integer.toString(-1 * i), (originX + (-i * graphScale)), originY);
				}
				for (int i = 0; i < lengthY; i++) {
					g.drawString(Integer.toString(-1 * i), originX, (originY + (i * graphScale)));
					g.drawString(Integer.toString(i), originX, (originY + (-i * graphScale)));
				}

				// draw cannon and bullet
				cannon.draw(g);
				if (bullet != null && bullet.isShot()) {
					bullet.draw(g);
				}
			}
        }

        void printScreen()
        {
            try
            {
                Graphics g = getGraphics();
                if(drawingArea != null && g != null)
                {
                    g.drawImage(drawingArea, 0, 0, null);
                }

                // Sync the display on some systems.
                // (on Linux, this fixes event queue problems)
                Toolkit.getDefaultToolkit().sync();
                g.dispose();
            }
            catch(Exception ex)
            {
                System.out.print("Graphics error: ");
                ex.printStackTrace();
            }
        }
    }

    class Cannon {
        // appearance
        final Color COLOR = Color.red;
        double radius;
        double barrelLength;
        double barrelWidth;

        // location and angle
        int positionX;
        int positionY;
        int centerX;
        int centerY;
        double angle = Math.PI/4;
        double angleIncrement = 0.2;
        int distanceIncrement = 3;

        // initialize and draw
        public Cannon(double radius, int positionX, int positionY) {
            this.radius = radius;
            this.positionX = positionX;
            this.positionY = positionY;
            centerX = (int) (positionX + radius);
            centerY = (int) (positionY + radius);

            barrelLength = radius * 1.5;
            barrelWidth = radius/2;
        }

        void draw(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            Color tempColor = g2.getColor();
            g2.setColor(COLOR);

            // draw circle
            int size = (int) (radius * 2);
            g.fillOval(positionX, positionY, size, size);

            // draw barrel
            g2.setStroke(new BasicStroke((int) barrelWidth));
            g2.drawLine(centerX, centerY, (int) getBarrelMouthX(), (int) getBarrelMouthY());

            g2.setColor(tempColor);
        }

        // get barrel mouth's position
        double getBarrelMouthX() {
            return centerX + barrelLength * Math.cos(angle);
        }

        // minus, because the difference in coordinate system
        double getBarrelMouthY() {
            return centerY - barrelLength * Math.sin(angle);
        }

        // movement methods
        void rotateLeft() {
            angle += angleIncrement;
        }

        void rotateRight() {
            angle -= angleIncrement;
        }

        void moveUp() {
            positionY -= distanceIncrement;
            centerY -= distanceIncrement;
        }

        void moveDown() {
            positionY += distanceIncrement;
            centerY += distanceIncrement;
        }

        void moveLeft() {
            positionX -= distanceIncrement;
            centerX -= distanceIncrement;
        }

        void moveRight() {
            positionX += distanceIncrement;
            centerX += distanceIncrement;
        }
    }

    class Bullet {
        int originX;
        int originY;
        int positionX;
        int positionY;
        double radius;
        final double baseVelocity = 50;
        double velocityX;
        double velocityY;
        double gravity = 9.8;
        double timeInitial;
        boolean shot = false;
        final Color color = Color.darkGray;

        public Bullet(double radius, int originX, int originY, double angle, double timeInitial) {
            this.radius = radius;
            this.originX = originX;
            this.originY = originY;
            this.timeInitial = timeInitial;
            this.velocityX = baseVelocity * Math.cos(angle);
            this.velocityY = baseVelocity * Math.sin(angle);
        }

        void shoot() {
            shot = true;
        }

        void stopShoot() {
            shot = false;
        }

        boolean isShot() {
            return shot;
        }
        /**
         counting distance based on time
         in x-axis, the velocity is constant
         in y-axis, the velocity is influenced by gravitational acceleration
         and we decrease the y-displacement because of different coordinate system
         */
        void move(double time) {
            double currentTime = time - timeInitial;
            positionX = (int) (originX + (velocityX * currentTime));
            positionY = (int) (originY - (velocityY * currentTime - gravity * currentTime * currentTime / 2));
        }

        // drawing function
        void draw(Graphics g) {
            int size = (int) (radius * 2);
            Graphics2D g2 = (Graphics2D) g;
            Color tempColor = g.getColor();
            g2.setColor(color);

            // draw the bullet
            g2.fillOval((int) (positionX + radius), (int) (positionY - radius), size, size);
            g2.setColor(tempColor);
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(Shooter::new);
    }
}