import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.Random;

public class MinElementFinder {

    // Serial method to find the minimum element
    private static int findMinSerial(int[] array) {
        int min = Integer.MAX_VALUE;
        for (int value : array) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    // Parallel task for finding the minimum element
    static class MinFinderParallel extends RecursiveTask<Integer> {
        private int[] array;
        private int start, end;

        MinFinderParallel(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Integer compute() {
            if (end - start <= 100) { // Base case threshold
                int min = Integer.MAX_VALUE;
                for (int i = start; i < end; i++) {
                    if (array[i] < min) {
                        min = array[i];
                    }
                }
                return min;
            } else {
                int mid = (start + end) / 2;
                MinFinderParallel left = new MinFinderParallel(array, start, mid);
                MinFinderParallel right = new MinFinderParallel(array, mid, end);
                left.fork();
                int rightResult = right.compute();
                int leftResult = left.join();
                return Math.min(leftResult, rightResult);
            }
        }
    }

    public static void main(String[] args) {
        int[] array = new int[100000]; // Large array of 10,000 elements
        Random random = new Random();
        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextInt(10000000); // Random integers up to 100,000
        }

        // Serial computation
        long startTime = System.currentTimeMillis();
        int minSerial = findMinSerial(array);
        long endTime = System.currentTimeMillis();
        System.out.println("Minimum element (Serial): " + minSerial);
        System.out.println("Serial time: " + (endTime - startTime) + " ms");

        // Parallel computation
        ForkJoinPool pool = new ForkJoinPool();
        MinFinderParallel task = new MinFinderParallel(array, 0, array.length);
        startTime = System.currentTimeMillis();
        int minParallel = pool.invoke(task);
        endTime = System.currentTimeMillis();
        System.out.println("Minimum element (Parallel): " + minParallel);
        System.out.println("Parallel time: " + (endTime - startTime) + " ms");
    }
}
