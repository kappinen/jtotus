package org.jlucrum.realtime.analytics;

/**
 * Created by IntelliJ IDEA.
 * User: Evgeni Kappinen
 * Date: 4/15/11
 * Time: 8:49 PM
 */


public class ErrorEstimators {


    /**
     * MSE ( Mean Squared Error)
     * Uses minimum length of target or output
     *
     * @param target the value to be predicted of the time series
     * @param output model output (prediction)
     * @return error
     */
    public static double mse(double[] target, double[] output) {
        double sum = 0;
        int size;

        if (target.length < output.length) {
            size = target.length;
        } else {
            size = output.length;
        }

        for (int i = 0; i < size; i++) {
            sum += target[i] - output[i];
        }

        return sum / (double) (size - 1);
    }

    /**
     * MAPE (Mean absolute percentage error)
     * Uses minimum length of target or output
     * http://en.wikipedia.org/wiki/Mean_absolute_percentage_error
     *
     * @param target the value to be predicted of the time series
     * @param output model output (prediction)
     * @return error
     */
    public static double mape(double[] target, double[] output) {
        double sum = 0;
        double size;

        if (target.length < output.length) {
            size = target.length;
        } else {
            size = output.length;
        }

        for (int i = 0; i < size; i++) {
            sum += (target[i] - output[i]) / target[i];
        }

        return sum / (double) (size - 1);
    }

    /**
     * Theil Statistics
     *
     * @param target the value to be predicted of the time series
     * @param output model output (prediction)
     * @return error
     */
    public static double theil(double[] target, double[] output) {
        double sum = 0;
        double sum2 = 0;
        double size;

        if (target.length < output.length) {
            size = target.length;
        } else {
            size = output.length;
        }

        if (size < 1) {
            throw new RuntimeException("Array is to small for calculation error");
        }

        for (int i = 1; i < size; i++) {
            sum += Math.pow(target[i - 1] - output[i - 1], 2);
            sum2 += Math.pow(target[i - 1] - target[i], 2);
        }

        return sum / sum2;
    }


    /**
     * POCID (Prediction of Forecast the Alterations of Direction)
     *
     * @param target the value to be predicted of the time series
     * @param output model output (prediction)
     * @return error
     */
    public static double pocid(double[] target, double[] output) {
        double sum = 0;
        double size;

        if (target.length < output.length) {
            size = target.length;
        } else {
            size = output.length;
        }

        if (size < 1) {
            throw new RuntimeException("Array is to small for calculation error");
        }

        for (int i = 1; i < size; i++) {
            double d = (target[i] - target[i - 1]) * (output[i] - output[i - 1]);
            if (d > 0) {
                d = 1;
            } else {
                d = 0;
            }
            sum += d;
        }

        return (100 * sum) / (size - 1);
    }

}
