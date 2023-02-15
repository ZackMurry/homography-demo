import Jama.Matrix;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class HomographyDisplay extends Canvas implements Runnable {



    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static int WIDTH = 1280;
    public static int HEIGHT = 720;

    private Thread thread;
    private static final String title = "Homography Demo";
    private static final Font font = new Font("Serif", Font.PLAIN, 24);

    private JFrame frame;
    private static boolean running = false;

    private final BufferedImage bufferedImage;

    static ArrayList<Point> points = new ArrayList<>();
    static ArrayList<Rect> rects = new ArrayList<>();
    static Rect largestRect = null;

    class ClickDetector implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent mouseEvent) {
        }

        @Override
        public void mousePressed(MouseEvent mouseEvent) {
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
        }

        @Override
        public void mouseEntered(MouseEvent mouseEvent) {
        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {
        }
    }


    public HomographyDisplay(BufferedImage buf) {
        bufferedImage = buf;
        this.frame = new JFrame();
        Dimension size = new Dimension(WIDTH, HEIGHT);
        this.setPreferredSize(size);
        addMouseListener(new ClickDetector());
    }

    public synchronized void start() {
        running = true;
        this.thread = new Thread(this, "Display");
        this.thread.start();
    }

    public synchronized void stop() {
        running = false;
        try{
            this.thread.join();
        } catch (Exception e){
            e.printStackTrace();
            this.thread.interrupt();
        }
    }

    @Override
    public void run() {
        try{
            while(running) {
                render();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }
        Toolkit.getDefaultToolkit().sync();

        Graphics g = bs.getDrawGraphics();

        g.setFont(font);

        g.setColor(Color.WHITE);
        g.fillRect(0,0,WIDTH,HEIGHT);

        draw(g);

        g.dispose();
        bs.show();
    }

    public void draw(Graphics g) {
        g.setColor(new Color(255, 255, 255));
        g.fillRect(-WIDTH, -HEIGHT, 2*WIDTH, 2*HEIGHT);
        g.drawImage(bufferedImage, 0, 0, WIDTH, HEIGHT, 0, 0, 1280,  720,null);
        g.setColor(new Color(255, 0, 0));
//        for (Point p : points) {
//            g.fillRect((int) p.x - 2, (int) p.y - 2, 4, 4);
//        }
//        for (Rect r : rects) {
//            g.fillRect(r.x, r.y, r.width, r.height);
//        }
        if (largestRect != null) {
            g.fillRect(largestRect.x, largestRect.y, largestRect.width, largestRect.height);
        }
    }


    public static void main(String[] args) throws IOException {
        Mat img = Imgcodecs.imread("/home/zack/homography-demo/pole_1.jpg");
        Mat output = new PoleDetectionPipeline().processFrame(img);
        Mat labeled = new Mat(output.size(), output.type());
        Mat rectComponents = Mat.zeros(new Size(0, 0), 0);
        Mat centComponents = Mat.zeros(new Size(0, 0), 0);

        Imgproc.connectedComponentsWithStats(output, labeled, rectComponents, centComponents);

        // Collect regions info
        int[] rectangleInfo = new int[5];
        double[] centroidInfo = new double[2];
        PoleDetectionPipeline.Region[] regions = new PoleDetectionPipeline.Region[rectComponents.rows() - 1];

        largestRect = null;
        for(int i = 1; i < rectComponents.rows(); i++) {

            // Extract bounding box
            rectComponents.row(i).get(0, 0, rectangleInfo);
            Rect rectangle = new Rect(rectangleInfo[0], rectangleInfo[1], rectangleInfo[2], rectangleInfo[3]);
            rects.add(rectangle);
            if (largestRect == null || rectangle.area() > largestRect.area()) {
                largestRect = rectangle;
            }

            // Extract centroids
            centComponents.row(i).get(0, 0, centroidInfo);
            Point centroid = new Point(centroidInfo[0], centroidInfo[1]);
            points.add(centroid);
            System.out.println(centroid);

            regions[i - 1] = new PoleDetectionPipeline.Region(rectangle, centroid);
        }
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", img, matOfByte);
        byte[] bytes = matOfByte.toArray();
        InputStream in = new ByteArrayInputStream(bytes);
        BufferedImage buf = ImageIO.read(in);
        HomographyDisplay display = new HomographyDisplay(buf);
        display.frame.setTitle(title);

        display.frame.setLayout(new GridBagLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.add(display);
        display.frame.add(mainPanel);
        SwingUtilities.updateComponentTreeUI(display.frame);

        display.frame.pack();
        display.frame.getContentPane().setBackground(Color.WHITE);
        display.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        display.frame.setLocationRelativeTo(null);
        display.frame.setBackground(Color.BLACK);
        display.frame.setResizable(false);
        display.frame.setVisible(true);

        display.start();
    }

}
