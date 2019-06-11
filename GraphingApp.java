import javax.swing.JButton;

import javax.swing.JFrame;

import javax.swing.JLabel;

import javax.swing.JPanel;

import javax.swing.JTextField;

import java.awt.Color;

import java.awt.EventQueue;

import java.awt.Graphics;

import java.awt.GridLayout;

import java.awt.Image;

import java.awt.Toolkit;

import java.awt.geom.Point2D;

import java.util.ArrayList;



class GraphingApp {

    private JFrame frame;



    // members of control panel

    private JPanel controlPanel;

    private DrawingArea drawingArea;

    private JTextField fieldLengthX;

    private JTextField fieldLengthY;

    private JTextField fieldBegin;

    private JTextField fieldIncrement;

    private JButton button;



    private int cpWidth = 270;      // set control panel's width



    public GraphingApp() {

        // setup the frame

        frame = new JFrame("Graphing App");

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(null);

        frame.setVisible(true);



        // setup control panel itself

        controlPanel = new JPanel(new GridLayout(5, 2, 5, 0));

        createControlPanel(controlPanel);

        controlPanel.setBounds(0, 0, cpWidth, 300);

        frame.add(controlPanel);



        // setup drawing area

        drawingArea = new DrawingArea();

        frame.add(drawingArea);

    }



    // add members to control panel

    private void createControlPanel(JPanel panel) {

        // the x-axis would be shown from -lengthX to lengthX

        JLabel labelLengthX = new JLabel("Length of X axis");

        controlPanel.add(labelLengthX);

        fieldLengthX = new JTextField(2);

        controlPanel.add(fieldLengthX);



        // the y-axis would be shown from -lengthY to lengthY

        JLabel labelLengthY = new JLabel("Length of Y axis");

        controlPanel.add(labelLengthY);

        fieldLengthY = new JTextField(2);

        controlPanel.add(fieldLengthY);



        // function calculation will start from startX

        JLabel labelBegin = new JLabel("Start point from X");

        controlPanel.add(labelBegin);

        fieldBegin = new JTextField(2);

        controlPanel.add(fieldBegin);



        // the lesser the number, the more detailed it will become

        JLabel labelIncrement = new JLabel("Incremental factor");

        controlPanel.add(labelIncrement);

        fieldIncrement = new JTextField(2);

        controlPanel.add(fieldIncrement);



        // clicking the button will start the animation

        button = new JButton("Begin graphing");

        button.addActionListener(e -> sendValuesToDrawer());

        controlPanel.add(button);

    }



    // get values from text fields

    public void sendValuesToDrawer() {

        double lengthX = Double.parseDouble(fieldLengthX.getText());

        double lengthY = Double.parseDouble(fieldLengthY.getText());

        double begin = Double.parseDouble(fieldBegin.getText());

        double increment = Double.parseDouble(fieldIncrement.getText());

        drawingArea.beginDrawing(lengthX, lengthY, begin, increment);

    }



    class DrawingArea extends JPanel {

        boolean draw = false;

        int originX;        // the origin points (0, 0)

        int originY;

        double scaleX;      //scaling the canvas according to lengthX & lengthY

        double scaleY;

        double lengthX;     // how many numbers shown along absis and ordinate

        double lengthY;

        double currentX;    // current X-point, the Y is retrieved from

        double increment;   // controlling detail

        final int MAX_POINTS = 1000;    // in case the function is a loop, or the thread runs for far too long

        ArrayList<Point2D.Double> points1 = new ArrayList<Point2D.Double>();

        ArrayList<Point2D.Double> points2 = new ArrayList<Point2D.Double>();

        Image drawingArea;

        Thread animator;    // thread to draw the graph

        

        // setup the drawing area

        public DrawingArea() {

            super(null);

            setBounds(cpWidth, 0, frame.getWidth() - cpWidth, frame.getHeight());

            originX = frame.getWidth()/2;

            originY = frame.getHeight()/2;

            setBackground(Color.white);

            drawingArea = createImage(frame.getWidth() - controlPanel.getWidth(),

                    frame.getHeight());

        }

        

        // functions to draw on the screen

        public double function1(double x) {

            return 4*x*x-16;

        }



        public double function2(double x) {

            return 2*x+1;

        }



        public void beginDrawing(double lengthX, double lengthY, double startX, double increment) {

            // retrieve data

            this.lengthX = lengthX + 1;

            this.lengthY = lengthY + 1;

            this.scaleX = (double) (frame.getWidth() - originX) / lengthX;

            this.scaleY = (double) (frame.getHeight() - originY) / lengthY;

            this.currentX = startX;

            this.increment = increment;

            

            // trigger drawing process

            draw = true;

            drawingArea = createImage(frame.getWidth() - controlPanel.getWidth(),

                    frame.getHeight());

            animator = new Thread(this::eventLoop);

            animator.start();

        }



        void eventLoop() {

            while(draw) {

                update();

                render();

                printScreen();

                try {

                    animator.sleep(10);

                } catch (InterruptedException ex) {

                    ex.printStackTrace();

                }

            }

            // clean up the thread

            System.out.println("stopping");

            draw = false;

            try {

                animator.join();

            } catch (InterruptedException ex) {

                ex.printStackTrace();

            }

        }



        // add points to be drawn

        void update()

        {

            // while there is still a need to draw

            if(currentX < lengthX && currentX > -lengthX && points1.size() < MAX_POINTS)

            {

                System.out.println("checking");

                currentX = currentX + increment;

                points1.add(new Point2D.Double(currentX, function1(currentX)));

                points2.add(new Point2D.Double(currentX, function2(currentX)));

            }

            else {      // cleanup for the next thread

                draw = false;

                points1.clear();

                points2.clear();

            }

        }



        void render() {

            if (drawingArea != null) {

                //get graphics of the image where coordinate and function will be drawn

                Graphics g = drawingArea.getGraphics();

                g.setColor(Color.BLACK);



                //draw the x-axis and y-axis

                g.drawLine(0, originY, getWidth(), originY);

                g.drawLine(originX, 0, originX, getHeight());



                //print numbers on the x-axis and y-axis, based on the scale

                for (int i = 0; i < lengthX; i++) {

                    g.drawString(Integer.toString(i), (int) (originX + (i * scaleX)), originY);

                    g.drawString(Integer.toString(-1 * i), (int) (originX + (-i * scaleX)), originY);

                }

                for (int i = 0; i < lengthY; i++) {

                    g.drawString(Integer.toString(-1 * i), originX, (int) (originY + (i * scaleY)));

                    g.drawString(Integer.toString(i), originX, (int) (originY + (-i * scaleY)));

                }

                

                // draw the lines

                for (int i = 0; i < points1.size() - 1; i++) {

                    g.drawLine((int) (originX + points1.get(i).x * scaleX), (int) (originY - points1.get(i).y * scaleY),

                            (int) (originX + points1.get(i + 1).x * scaleX), (int) (originY - points1.get(i + 1).y * scaleY));

                    g.drawLine((int) (originX + points2.get(i).x * scaleX), (int) (originY - points2.get(i).y * scaleY),

                            (int) (originX + points2.get(i + 1).x * scaleX), (int) (originY - points2.get(i + 1).y * scaleY));

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

                System.out.println("Graphics error: " + ex);

            }

        }

    }



    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {

            @Override

            public void run() {

                GraphingApp ga = new GraphingApp();

            }

        });

    }

}