import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class SortComparison {

    private static final Random random = new Random();

    // Generate an array of random integers
    public static int[] generateRandomArray(int size) {
        return random.ints(100000, 1, 100000000).toArray();
    }

    
    // Quick Sort (Serial)
    public static void quickSort(int[] array, int left, int right) {
        if (left < right) {
            int pivotIndex = partition(array, left, right);
            quickSort(array, left, pivotIndex - 1);
            quickSort(array, pivotIndex + 1, right);
        }
    }


    private static int partition(int[] array, int left, int right) {
        int pivot = array[right];
        int i = (left - 1);
        for (int j = left; j < right; j++) {
            if (array[j] <= pivot) {
                i++;
                int swapTemp = array[i];
                array[i] = array[j];
                array[j] = swapTemp;
            }
        }
        int swapTemp = array[i + 1];
        array[i + 1] = array[right];
        array[right] = swapTemp;
        return i + 1;
    }


    // Quick Sort (Parallel)
    static class ParallelQuickSort extends RecursiveAction {
        private int[] array;
        private int left, right;

        public ParallelQuickSort(int[] array, int left, int right) {
            this.array = array;
            this.left = left;
            this.right = right;
        }

        @Override
        protected void compute() {
            if (left < right) {
                int pivotIndex = partition(array, left, right);
                invokeAll(new ParallelQuickSort(array, left, pivotIndex - 1),
                        new ParallelQuickSort(array, pivotIndex + 1, right));
            }
        }
    }

    // Merge Sort (Serial)
    public static void mergeSort(int[] array, int[] temp, int left, int right) {
        if (left < right) {
            int middle = (left + right) / 2;
            mergeSort(array, temp, left, middle);
            mergeSort(array, temp, middle + 1, right);
            merge(array, temp, left, middle, right);
        }
    }

    private static void merge(int[] array, int[] temp, int left, int middle, int right) {
        System.arraycopy(array, left, temp, left, right - left + 1);
        int i = left, j = middle + 1, k = left;
        while (i <= middle && j <= right) {
            if (temp[i] <= temp[j]) {
                array[k++] = temp[i++];
            } else {
                array[k++] = temp[j++];
            }
        }
        while (i <= middle) {
            array[k++] = temp[i++];
        }
    }

    // Merge Sort (Parallel)
    static class ParallelMergeSort extends RecursiveAction {
        private int[] array, temp;
        private int left, right;

        public ParallelMergeSort(int[] array, int[] temp, int left, int right) {
            this.array = array;
            this.temp = temp;
            this.left = left;
            this.right = right;
        }

        @Override
        protected void compute() {
            if (left < right) {
                int middle = (left + right) / 2;
                invokeAll(new ParallelMergeSort(array, temp, left, middle),
                        new ParallelMergeSort(array, temp, middle + 1, right));
                merge(array, temp, left, middle, right);
            }
        }
    }

    public static void main(String[] args) {
        int size = 500;
        int[] original = generateRandomArray(size);

        // Quick Sort Serial
        int[] quickArray = Arrays.copyOf(original, original.length);
        long startTime = System.currentTimeMillis();
        quickSort(quickArray, 0, quickArray.length - 1);
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken for serial Quick Sort: " + (endTime - startTime) + " ms");

        // Quick Sort Parallel
        ForkJoinPool pool = new ForkJoinPool();
        quickArray = Arrays.copyOf(original, original.length);
        startTime = System.currentTimeMillis();
        pool.invoke(new ParallelQuickSort(quickArray, 0, quickArray.length - 1));
        endTime = System.currentTimeMillis();
        System.out.println("Time taken for parallel Quick Sort: " + (endTime - startTime) + " ms");

        // Merge Sort Serial
        int[] mergeArray = Arrays.copyOf(original, original.length);
        int[] temp = new int[mergeArray.length];
        startTime = System.currentTimeMillis();
        mergeSort(mergeArray, temp, 0, mergeArray.length - 1);
        endTime = System.currentTimeMillis();
        System.out.println("Time taken for serial Merge Sort: " + (endTime - startTime) + " ms");

        // Merge Sort Parallel
        mergeArray = Arrays.copyOf(original, original.length);
        temp = new int[mergeArray.length];
        startTime = System.currentTimeMillis();
        pool.invoke(new ParallelMergeSort(mergeArray, temp, 0, mergeArray.length - 1));
        endTime = System.currentTimeMillis();
        System.out.println("Time taken for parallel Merge Sort: " + (endTime - startTime) + " ms");

        pool.shutdown();
    }
}
