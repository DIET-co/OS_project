import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ImageBlur {

    // Task class for parallel processing
    static class BlurTask extends RecursiveAction {
        private BufferedImage src;
        private BufferedImage dest;
        private int start;
        private int end;
        private int radius;

        BlurTask(BufferedImage src, BufferedImage dest, int start, int end, int radius) {
            this.src = src;
            this.dest = dest;
            this.start = start;
            this.end = end;
            this.radius = radius;
        }

        @Override
        protected void compute() {
            if (end - start < 100) {
                for (int x = start; x < end; x++) {
                    for (int y = 0; y < src.getHeight(); y++) {
                        applyBlurToPixel(src, dest, x, y, radius);
                    }
                }
            } else {
                int mid = (start + end) / 2;
                invokeAll(new BlurTask(src, dest, start, mid, radius),
                        new BlurTask(src, dest, mid, end, radius));
            }
        }

        private void applyBlurToPixel(BufferedImage src, BufferedImage dest, int x, int y, int radius) {
            int pixelCount = 0;
            int red = 0;
            int green = 0;
            int blue = 0;
            for (int fx = -radius; fx <= radius; fx++) {
                for (int fy = -radius; fy <= radius; fy++) {
                    int nx = x + fx;
                    int ny = y + fy;
                    if (nx >= 0 && nx < src.getWidth() && ny >= 0 && ny < src.getHeight()) {
                        Color color = new Color(src.getRGB(nx, ny));
                        red += color.getRed();
                        green += color.getGreen();
                        blue += color.getBlue();
                        pixelCount++;
                    }
                }
            }
            Color newColor = new Color(red / pixelCount, green / pixelCount, blue / pixelCount);
            dest.setRGB(x, y, newColor.getRGB());
        }
    }

    // Serial method for blur
    private static void applyBlur(BufferedImage src, BufferedImage dest, int radius) {
        int width = src.getWidth();
        int height = src.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixelCount = 0;
                int red = 0;
                int green = 0;
                int blue = 0;
                for (int fx = -radius; fx <= radius; fx++) {
                    for (int fy = -radius; fy <= radius; fy++) {
                        int nx = x + fx;
                        int ny = y + fy;
                        if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                            Color color = new Color(src.getRGB(nx, ny));
                            red += color.getRed();
                            green += color.getGreen();
                            blue += color.getBlue();
                            pixelCount++;
                        }
                    }
                }
                Color newColor = new Color(red / pixelCount, green / pixelCount, blue / pixelCount);
                dest.setRGB(x, y, newColor.getRGB());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        File inputFile = new File("C:\\Users\\klike\\OneDrive\\Documents\\GitHub\\OS-\\Project\\untitled\\pexels-luisdalvan-1770809.jpg");
        BufferedImage inputImage = ImageIO.read(inputFile);
        BufferedImage outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), inputImage.getType());

        // Parallel processing
        long startTime = System.currentTimeMillis();
        BlurTask blurTask = new BlurTask(inputImage, outputImage, 0, inputImage.getWidth(), 2);
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(blurTask);
        long endTime = System.currentTimeMillis();
        System.out.println("Parallel blur processing time: " + (endTime - startTime) + " ms");

        // Save parallel output image
        File outputParallelFile = new File("C:\\Users\\klike\\OneDrive\\Documents\\GitHub\\OS-\\Project\\untitled\\parallel_output.jpg");
        ImageIO.write(outputImage, "jpg", outputParallelFile);

        // Serial processing
        BufferedImage serialOutputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), inputImage.getType());
        startTime = System.currentTimeMillis();
        applyBlur(inputImage, serialOutputImage, 6);
        endTime = System.currentTimeMillis();
        System.out.println("Serial blur processing time: " + (endTime - startTime) + " ms");

        // Save serial output image
        File outputSerialFile = new File("C:\\Users\\klike\\OneDrive\\Documents\\GitHub\\OS-\\Project\\untitled\\serial_output.jpg");
        ImageIO.write(serialOutputImage, "jpg", outputSerialFile);

        pool.shutdown(); // Properly shutdown the thread pool
    }
}
