import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMultiplier {

    public static double[][] serialMultiplyMatrix(double[][] a, double[][] b) {
        int rowsA = a.length;
        int columnsA = a[0].length;
        int columnsB = b[0].length;

        double[][] result = new double[rowsA][columnsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < columnsB; j++) {
                for (int k = 0; k < columnsA; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }

        return result;
    }

    public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b) {
        int rowsA = a.length;
        int columnsB = b[0].length;
        double[][] result = new double[rowsA][columnsB];

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (int i = 0; i < rowsA; i++) {
            int finalI = i;
            executor.submit(() -> {
                for (int j = 0; j < columnsB; j++) {
                    for (int k = 0; k < a[0].length; k++) {
                        result[finalI][j] += a[finalI][k] * b[k][j];
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result;
    }

    public static void main(String[] args) {
        int size = 1000; // Modify size for testing
        double[][] matrix1 = MatrixGenerator.generateRandomMatrix(size, size);
        double[][] matrix2 = MatrixGenerator.generateRandomMatrix(size, size);

        long startSerial = System.currentTimeMillis();
        double[][] resultSerial = serialMultiplyMatrix(matrix1, matrix2);
        long endSerial = System.currentTimeMillis();

        long startParallel = System.currentTimeMillis();
        double[][] resultParallel = parallelMultiplyMatrix(matrix1, matrix2);
        long endParallel = System.currentTimeMillis();

        System.out.println("Time taken for serial matrix multiplication: " + (endSerial - startSerial) + " ms");
        System.out.println("Time taken for parallel matrix multiplication: " + (endParallel - startParallel) + " ms");
    }
}

class MatrixGenerator {
    public static double[][] generateRandomMatrix(int rows, int columns) {
        Random rand = new Random();
        double[][] matrix = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                matrix[i][j] = rand.nextDouble();
            }
        }
        return matrix;
    }
}
