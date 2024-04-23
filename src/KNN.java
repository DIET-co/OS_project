import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.Random;

class Point {
    double x, y;
    int label; // Class label

    Point(double x, double y, int label) {
        this.x = x;
        this.y = y;
        this.label = label;
    }

    // Computes the Euclidean distance between two points
    static double distance(Point a, Point b) {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }
}

public class KNN {

    // Serial k-Nearest Neighbors
    private static int classify(Point[] trainingSet, Point testPoint, int k) {
        Arrays.sort(trainingSet, Comparator.comparingDouble(o -> Point.distance(testPoint, o)));
        int[] votes = new int[10]; // Assuming labels are from 0 to 9
        for (int i = 0; i < k; i++) {
            votes[trainingSet[i].label]++;
        }
        return getMaxIndex(votes);
    }

    // Helper method to find the index with the maximum value
    private static int getMaxIndex(int[] array) {
        int maxIndex = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    // Parallel k-Nearest Neighbors using ForkJoin
    static class KNNParallel extends RecursiveTask<Integer> {
        private Point[] trainingSet;
        private Point testPoint;
        private int k, start, end;

        KNNParallel(Point[] trainingSet, Point testPoint, int k, int start, int end) {
            this.trainingSet = trainingSet;
            this.testPoint = testPoint;
            this.k = k;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Integer compute() {
            if (end - start <= 500) { // Base case threshold
                return classify(Arrays.copyOfRange(trainingSet, start, end), testPoint, k);
            } else {
                int mid = (start + end) / 2;
                KNNParallel left = new KNNParallel(trainingSet, testPoint, k, start, mid);
                KNNParallel right = new KNNParallel(trainingSet, testPoint, k, mid, end);
                left.fork();
                int rightResult = right.compute();
                int leftResult = left.join();
                // Combine results from left and right
                int[] combined = new int[10];
                combined[leftResult]++;
                combined[rightResult]++;
                return getMaxIndex(combined);
            }
        }
    }

    public static void main(String[] args) {
        Point[] trainingSet = new Point[1000]; // Example training set
        Random random = new Random();
        for (int i = 0; i < trainingSet.length; i++) {
            trainingSet[i] = new Point(random.nextDouble() * 100, random.nextDouble() * 100, random.nextInt(10));
        }
        Point testPoint = new Point(50, 50, 0); // Example test point

        // Serial Classification
        long startTime = System.currentTimeMillis();
        int label = classify(trainingSet, testPoint, 5);
        long endTime = System.currentTimeMillis();
        System.out.println("Predicted label (Serial): " + label);
        System.out.println("Serial time: " + (endTime - startTime) + " ms");

        // Parallel Classification
        ForkJoinPool pool = new ForkJoinPool();
        KNNParallel task = new KNNParallel(trainingSet, testPoint, 5, 0, trainingSet.length);
        startTime = System.currentTimeMillis();
        int parallelLabel = pool.invoke(task);
        endTime = System.currentTimeMillis();
        System.out.println("Predicted label (Parallel): " + parallelLabel);
        System.out.println("Parallel time: " + (endTime - startTime) + " ms");
    }
}
