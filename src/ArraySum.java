import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.Random;

public class ArraySum {

    // Serial summing of the array
    private static long sumSerial(int[] array) {
        long sum = 0;
        for (int value : array) {
            sum += value;
        }
        return sum;
    }

    // Parallel task for summing an array
    static class ArraySumParallel extends RecursiveTask<Long> {
        private int[] array;
        private int start, end;

        ArraySumParallel(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            if (end - start <= 100) { // This threshold can be adjusted for performance tuning
                long sum = 0;
                for (int i = start; i < end; i++) {
                    sum += array[i];
                }
                return sum;
            } else {
                int mid = (start + end) / 2;
                ArraySumParallel left = new ArraySumParallel(array, start, mid);
                ArraySumParallel right = new ArraySumParallel(array, mid, end);
                left.fork();
                long rightResult = right.compute();
                long leftResult = left.join();
                return leftResult + rightResult;
            }
        }
    }

    public static void main(String[] args) {
        int[] array = new int[1000000]; // Array of 10,000 elements
        Random random = new Random();
        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextInt(100000000); // Fill the array with random numbers up to 100
        }

        // Serial Summation
        long startTime = System.currentTimeMillis();
        long serialSum = sumSerial(array);
        long endTime = System.currentTimeMillis();
        System.out.println("Sum (Serial): " + serialSum);
        System.out.println("Serial time: " + (endTime - startTime) + " ms");

        // Parallel Summation
        ForkJoinPool pool = new ForkJoinPool();
        ArraySumParallel task = new ArraySumParallel(array, 0, array.length);
        startTime = System.currentTimeMillis();
        long parallelSum = pool.invoke(task);
        endTime = System.currentTimeMillis();
        System.out.println("Sum (Parallel): " + parallelSum);
        System.out.println("Parallel time: " + (endTime - startTime) + " ms");
    }
}
