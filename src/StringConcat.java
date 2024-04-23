import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class StringConcat {
    private static String concatSerial(String[] strings) {
        StringBuilder result = new StringBuilder();
        for (String s : strings) {
            result.append(s);
        }
        return result.toString();
    }

    static class StringConcatParallel extends RecursiveTask<String> {
        private String[] strings;
        private int start, end;

        StringConcatParallel(String[] strings, int start, int end) {
            this.strings = strings;
            this.start = start;
            this.end = end;
        }

        @Override
        protected String compute() {
            if (end - start <= 2) {
                StringBuilder result = new StringBuilder();
                for (int i = start; i < end; i++) {
                    result.append(strings[i]);
                }
                return result.toString();
            } else {
                int mid = (start + end) / 2;
                StringConcatParallel left = new StringConcatParallel(strings, start, mid);
                StringConcatParallel right = new StringConcatParallel(strings, mid, end);
                left.fork();
                String rightResult = right.compute();
                String leftResult = left.join();
                return leftResult + rightResult;
            }
        }
    }

    public static void main(String[] args) {
        // Modify the size of this array to increase the data load
        String[] strings = new String[1000]; // Increase the size of the array as needed
        for (int i = 0; i < strings.length; i++) {
            strings[i] = "word" + i; // Simulated large data set
        }

        // Serial Concatenation
        long startTime = System.currentTimeMillis();
        String serialResult = concatSerial(strings);
        long endTime = System.currentTimeMillis();
        System.out.println("Concatenated result length (Serial): " + serialResult.length());
        System.out.println("Serial time: " + (endTime - startTime) + " ms");

        // Parallel Concatenation
        ForkJoinPool pool = new ForkJoinPool();
        StringConcatParallel task = new StringConcatParallel(strings, 0, strings.length);
        startTime = System.currentTimeMillis();
        String parallelResult = pool.invoke(task);
        endTime = System.currentTimeMillis();
        System.out.println("Concatenated result length (Parallel): " + parallelResult.length());
        System.out.println("Parallel time: " + (endTime - startTime) + " ms");
    }
}
