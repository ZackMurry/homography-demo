import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
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

public class Display extends Canvas implements Runnable {

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
    private int i;
    private int j;

    public Display(BufferedImage buf, int i, int j) {
        bufferedImage = buf;
        this.i = i;
        this.j = j;
        this.frame = new JFrame();
        Dimension size = new Dimension(WIDTH, HEIGHT);
        this.setPreferredSize(size);
        addMouseListener(new ClickDetector());
    }

    class ClickDetector implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent mouseEvent) {
            i = mouseEvent.getX();
            j = mouseEvent.getY();
            DoublePoint pos = Homography.positionFromPoint(new org.opencv.core.Point(i, j));
            System.out.printf("(%.2f, %.2f)", pos.getX(), pos.getY());
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
//        g.fillRect((int) Math.round(j * (WIDTH / 4032d)), (int) Math.round(i * (HEIGHT / 3024d)), 5, 5);
    }


    public static void main(String[] args) throws IOException {
        Mat img = Imgcodecs.imread("/home/zack/homography-demo/sample_checkerboard.jpg");
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", img, matOfByte);
        byte[] bytes = matOfByte.toArray();
        InputStream in = new ByteArrayInputStream(bytes);
        BufferedImage buf = ImageIO.read(in);
        Display display = new Display(buf, 0, 0);
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
