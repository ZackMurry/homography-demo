import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Display extends Canvas implements Runnable {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static int WIDTH = 800;
    public static int HEIGHT = 600;

    private Thread thread;
    private static final String title = "Homography Demo";
    private static final Font font = new Font("Serif", Font.PLAIN, 24);

    private JFrame frame;
    private static boolean running = false;

    private final BufferedImage bufferedImage;
    private final int i;
    private final int j;

    public Display(BufferedImage buf, int i, int j) {
        bufferedImage = buf;
        this.i = i;
        this.j = j;
        this.frame = new JFrame();
        Dimension size = new Dimension(WIDTH, HEIGHT);
        this.setPreferredSize(size);
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
        g.drawImage(bufferedImage, 0, 0, WIDTH, HEIGHT, 0, 0, 4032,  3024,null);
        g.setColor(new Color(255, 0, 0));
        g.fillRect((int) Math.round(j * (WIDTH / 4032d)), (int) Math.round(i * (HEIGHT / 3024d)), 5, 5);
    }


    public static void main(String[] args) throws IOException {
        Mat img = Imgcodecs.imread("/home/zack/dl/IMG_8032.JPG");
        Mat hsvMat = new Mat();
        Imgproc.cvtColor(img, hsvMat, Imgproc.COLOR_RGB2HSV);
        Mat output = new Mat();
        Core.inRange(hsvMat, new Scalar(80, 100, 0), new Scalar(100, 125, 999), output);
        int mi = -1, mj = -1, mc = 0;
        for (int i = 5; i < output.rows() - 5; i++) {
            for (int j = 5; j < output.cols() - 5; j++) {
                if (output.get(i, j)[0] == 0) {
                    continue;
                }
                int cc = 0;
                for (int k = -10; k <= 10; k++) {
                    for (int l = -10; l <= 10; l++) {
                        double[] pixel = output.get(i + k, j + l);
                        if (pixel[0] == 255) {
                            cc++;
                        }
                    }
                }
                if (cc > mc) {
                    mi = i;
                    mj = j;
                    mc = cc;
                }
            }
        }
        System.out.println("mi: " + mi + "; mj: " + mj + "; mc: " + mc);
        DoublePoint dp = Homography.convertCameraPointToWorldPoint(mj, mi);
        System.out.println("wy: " + dp.getY());
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", img, matOfByte);
        byte[] bytes = matOfByte.toArray();
        InputStream in = new ByteArrayInputStream(bytes);
        BufferedImage buf = ImageIO.read(in);
        Display display = new Display(buf, mi, mj);
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
