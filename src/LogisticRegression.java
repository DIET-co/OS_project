import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.Arrays;

public class LogisticRegression {
    private double[] weights; // Logistic regression parameters

    // Constructor to initialize weights
    public LogisticRegression(int n) {
        weights = new double[n]; // Initialize weights for 'n' features
    }

    // Sigmoid function to return the probability estimation
    private double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

    // Predict the probability that the output is 1
    public double predict(double[] x) {
        double linearCombination = 0.0;
        for (int i = 0; i < weights.length; i++) {
            linearCombination += weights[i] * x[i];
        }
        return sigmoid(linearCombination);
    }

    // Update weights using gradient descent (Serial)
    public void updateWeights(double[][] X, double[] y, double lr) {
        double[] gradient = new double[weights.length];
        for (int i = 0; i < X.length; i++) { // for each data point
            double prediction = predict(X[i]);
            for (int j = 0; j < weights.length; j++) { // for each feature
                gradient[j] += (y[i] - prediction) * X[i][j];
            }
        }

        // Update weights
        for (int j = 0; j < weights.length; j++) {
            weights[j] += lr * gradient[j] / X.length; // learning rate and average
        }
    }

    // Train model using serial method
    public void train(double[][] X, double[] y, double lr, int epochs) {
        for (int epoch = 0; epoch < epochs; epoch++) {
            updateWeights(X, y, lr);
        }
    }

    // Parallel task for updating weights
    static class WeightUpdateTask extends RecursiveAction {
        private double[][] X;
        private double[] y;
        private double[] weights;
        private double[] gradient;
        private int start, end;
        private double lr;

        public WeightUpdateTask(double[][] X, double[] y, double[] weights, double[] gradient, int start, int end, double lr) {
            this.X = X;
            this.y = y;
            this.weights = weights;
            this.gradient = gradient;
            this.start = start;
            this.end = end;
            this.lr = lr;
        }

        @Override
        protected void compute() {
            if (end - start <= 50) { // Threshold for splitting task
                for (int i = start; i < end; i++) {
                    double prediction = 0.0;
                    for (int j = 0; j < weights.length; j++) {
                        prediction += weights[j] * X[i][j];
                    }
                    prediction = 1.0 / (1.0 + Math.exp(-prediction)); // Sigmoid

                    for (int j = 0; j < weights.length; j++) {
                        gradient[j] += (y[i] - prediction) * X[i][j];
                    }
                }
            } else {
                int mid = (start + end) / 2;
                WeightUpdateTask left = new WeightUpdateTask(X, y, weights, gradient, start, mid, lr);
                WeightUpdateTask right = new WeightUpdateTask(X, y, weights, gradient, mid, end, lr);
                invokeAll(left, right);
                // Combine gradients from left and right tasks
                for (int i = 0; i < gradient.length; i++) {
                    gradient[i] += right.gradient[i];
                }
            }
        }
    }

    // Train the model using parallel computation
    public void trainParallel(double[][] X, double[] y, double lr, int epochs) {
        ForkJoinPool pool = new ForkJoinPool();
        for (int epoch = 0; epoch < epochs; epoch++) {
            double[] gradient = new double[weights.length];
            pool.invoke(new WeightUpdateTask(X, y, weights, gradient, 0, X.length, lr));
            for (int j = 0; j < weights.length; j++) {
                weights[j] += lr * gradient[j] / X.length; // Apply the average gradient
            }
        }
    }

    public static void main(String[] args) {
        double[][] X = {
                {0.5, 1.5}, {1.5, 1.5}, {0.5, -0.5}, {-1.5, 1.5}, {-0.5, -1.5}, {1.5, -1.5},
                {1.1, 2.5}, {2.5, 2.1}, {1.5, -0.1}, {-1.2, 2.1}, {-1.5, -2.5}, {2.5, -1.0}
        }; //  data points
        double[] y = {0, 0, 1, 0, 1, 1, 0, 0, 1, 0, 1, 1}; // labels

        LogisticRegression model = new LogisticRegression(2); // Initialize logistic regression model with 2 features

        // Serial Training
        System.out.println("Training serially...");
        long startTimeSerial = System.currentTimeMillis();
        model.train(X, y, 0.01, 1000); // Train with learning rate 0.01 and 1000 epochs serially
        long endTimeSerial = System.currentTimeMillis();
        System.out.println("Serial training completed in " + (endTimeSerial - startTimeSerial) + " ms");

        // Parallel Training
        System.out.println("Training in parallel...");
        long startTimeParallel = System.currentTimeMillis();
        model.trainParallel(X, y, 0.01, 1000); // Train with learning rate 0.01 and 1000 epochs in parallel
        long endTimeParallel = System.currentTimeMillis();
        System.out.println("Parallel training completed in " + (endTimeParallel - startTimeParallel) + " ms");

        // Example prediction
        double[] newPoint = {1.0, -1.0};
        double prediction = model.predict(newPoint);
        System.out.println("Predicted probability for new data point " + Arrays.toString(newPoint) + " is: " + prediction);
    }
}
