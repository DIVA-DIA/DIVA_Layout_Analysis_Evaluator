/*
 * Copyright (c) 2017 UniFR
 * University of Fribourg, Switzerland.
 */

package ch.unifr.experimenter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * This class contains the functions for page segmentation evaluation at pixel level
 * for the HisDoc Layout Competition of the ICDAR 2017
 *
 * This code has been written in pair programming by:
 * @author Michele Alberti <michele.alberti@unifr.ch>
 * @author Manuel Bouillon <manuel.bouillon@unifr.ch>
 * @date 24.07.2017
 * @brief The layout analysis evaluation algorithm
 *
 */
public class SegmentationAnalysis {

    /**
     * Image containing the ground truth on the blue channel and the boundaries on the red one.
     */
    private final BufferedImage gtImage;
    /**
     * Image containing the predicted classes on the blue channel
     */
    private final BufferedImage predictionImage;
    /**
     * Total number of unique classes in the GT
     */
    private final int nbClasses;
    /**
     * Class-wise frequencies of class distribution. It sums up to 1.
     */
    private double[] frequencies;
    /**
     * Class-wise Jaccard index.
     */
    private double[] jaccard;
    /**
     * Class-wise F1-Score
     */
    private double[] f1;
    /**
     * Class-wise precision
     */
    private double[] precision;
    /**
     * Class-wise recall
     */
    private double[] recall;
    /**
     * Results of the evaluation. See @return of evaluateImages() for details.
     */
    private double[] evaluation;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * This is expected constructor of the class, which takes as parameter the BufferedImages of
     * the ground truth and the predicted values.
     * @param gtImage ground truth (multi-label!) image representation
     * @param predictionImage prediction of the algorithm (multi-label!) image representation
     * @param nbClasses total number of different classes in the GT
     */
    public SegmentationAnalysis(final BufferedImage gtImage,
                                final BufferedImage predictionImage,
                                final int nbClasses) {

        assert (gtImage.getWidth() == predictionImage.getWidth());
        assert (gtImage.getHeight() == predictionImage.getHeight());

        /**
         * This re-drawing process is mandatory as the type of the prediction image might be different from the one
         * expected i.e  prediction image is of type GRAYSCALE.
         */
        this.gtImage = new BufferedImage(gtImage.getWidth(), gtImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        this.gtImage.getGraphics().drawImage(gtImage, 0, 0, null);

        this.predictionImage = new BufferedImage(predictionImage.getWidth(), predictionImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        this.predictionImage.getGraphics().drawImage(predictionImage, 0, 0, null);

        this.nbClasses = nbClasses;
    }

    /**
     * This is just another entry point of this class, shall the images be on file
     * rather than on memory.
     * @param gtImagePath path to the ground truth (multi-label!) image representation
     * @param resultImagePath path to the prediction of the algorithm (multi-label!) image representation
     * @param nbClasses total number of different classes in the GT
     * @throws IOException in case there are issues with opening the files
     */
    public SegmentationAnalysis(String gtImagePath, String resultImagePath, int nbClasses) throws IOException {
        this(ImageIO.read(new File(gtImagePath)),ImageIO.read(new File(resultImagePath)),nbClasses);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Public
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * This method perform the evaluation of the image containing the prediction by comparing it
     * with the ground truth provided. This evaluation is conducted on a multi-class and multi-label
     * level.
     * @return the return value is a double[] filled with different metrics of which we extensively evaluated
     *         the significance. Below are mentioned all of them, preceded by their index in the array.
     *
     *
     * [0]  exact match
     *
     * [1]  hamming score (1 - hamming loss)
     *
     * [2]  mean Jaccard index (IU)
     * [3]  Jaccard index (IU)                  (frequency weighted)
     *
     * [4]  mean F1-score
     * [5]  mean precision
     * [6]  mean recall
     * [7]  F1-score                            (frequency weighted)
     * [8]  precision                           (frequency weighted)
     * [9]  recall                              (frequency weighted)
     */
    public double[] evaluateImages(){

        // Check images same size
        assert(gtImage.getHeight() == predictionImage.getHeight());
        assert(gtImage.getWidth() == predictionImage.getWidth());
        assert(nbClasses >= 0);

        // Computing the total number of pixel in the image
        final int N = gtImage.getHeight() * gtImage.getWidth();

        // Init matrices
        double[][][] cm = new double[2][2][nbClasses];

        // Init Hamming Loss
        double hammingLoss = 0;

        // Init Exact match
        double exactMatch = 0;

        // Iterate over all pixels
        for (int x = 0; x < gtImage.getWidth(); x++) {
            for (int y = 0; y < gtImage.getHeight(); y++) {

                // Fetch values from the images
                boolean[] groundTruth = getLabels(gtImage,x,y,nbClasses);
                boolean[] prediction = getLabels(predictionImage,x,y,nbClasses);

                /* Pre-process the vectors to account for boundaries (if necessary)
                 * In this way, if the prediction contains at least ONE of the GT's
                 * labels (only when is a boundary ofc!) it will get a correct
                 * classification score for all classes in the GT. If there are
                 * extra classes which are not present in the GT, they will be
                 * counted as classification mistakes.
                 */
                if (isBoundary(gtImage,x,y)) {
                    // Set the background of the GT to true
                    groundTruth[0] = true;

                    // Check whether at least one class is correct
                    for (int c = 0; c < nbClasses; c++) {
                        if(groundTruth[c] && prediction[c]){
                            // Paste 'ones' of the GT over prediction
                            for (int i = 0; i < nbClasses; i++) {
                                prediction[i] = groundTruth[i] || prediction[i];
                            }
                            // Exit the loop
                            break;
                        }
                    }
                }

                // Populate the matrices
                for (int i = 0; i < nbClasses; i++) {
                    if(groundTruth[i]){
                        if (prediction[i]){
                            cm[0][0][i]++; // TRUE POSITIVE
                        }else {
                            cm[0][1][i]++; // FALSE NEGATIVE
                        }
                    }else{
                        if (prediction[i]){
                            cm[1][0][i]++; // FALSE POSITIVE
                        }else {
                            cm[1][1][i]++; // TRUE NEGATIVE
                        }
                    }
                }

                // Compute the hamming loss
                double loss = hammingLoss(groundTruth,prediction);

                // Integrate the loss
                hammingLoss += loss;

                // Integrate exact match
                exactMatch += (loss == 0) ? 1: 0;
            }
        }

        // Average over samples
        hammingLoss /= N;
        exactMatch /= N;

        // Get class frequencies
        frequencies = classFrequencies(cm);

        // Init the return value array:
        evaluation = new double[10];

        // Fill the return value vector
        evaluation[0] = exactMatch;
        evaluation[1] = 1.0 - hammingLoss;

        jaccard = computeJaccard(cm);
        evaluation[2] = mean(jaccard);
        evaluation[3] = weightedMean(jaccard,frequencies);

        precision = computePrecision(cm);
        recall = computeRecall(cm);
        f1 = computeF1(precision,recall);

        evaluation[4] = mean(f1);
        evaluation[5] = mean(precision);
        evaluation[6] = mean(recall);

        evaluation[7] = weightedMean(f1,frequencies);
        evaluation[8] = weightedMean(precision,frequencies);
        evaluation[9] = weightedMean(recall,frequencies);

        return evaluation;
    }

    /**
     * This method creates an image colored with 5 colors, which represent the correctness of the prediction.
     * (0x007F00) GREEN:   Foreground predicted correctly
     * (0xFFFF00) YELLOW:  Foreground predicted - but the wrong class (e.g. Text instead of Comment)
     * (0x000000) BLACK:   Background predicted correctly
     * (0xFF0000) RED:     Background mis-predicted as Foreground
     * (0x00FFFF) BLUE:    Foreground mis-predicted as Background
     *
     * @return a BufferedImage colored as mentioned
     */
    public BufferedImage visualiseEvaluation(){
        // Init the new image
        BufferedImage bi = new BufferedImage(gtImage.getWidth(),gtImage.getHeight(), gtImage.getType());

        // Iterate over all pixels
        for (int x = 0; x < gtImage.getWidth(); x++) {
            for (int y = 0; y < gtImage.getHeight(); y++) {

                // Fetch values from the images
                boolean[] groundTruth = getLabels(gtImage, x, y, nbClasses);
                boolean[] prediction = getLabels(predictionImage, x, y, nbClasses);

                /* If Background is predicted - but other classes are present  (e.g. Text instead of Comment)
                 * mark it as white as this is not allowed
                 */
                boolean[] bg = new boolean[nbClasses]; // Dummy input for only BG class
                bg[0] = true;
                if (prediction[0] && (hammingLoss(bg, prediction) != 0)){
                    bi.setRGB(x, y, 0xFFFFFF); // WHITE Background predicted - but other classes are present
                    continue;
                }

                if(groundTruth[0] && prediction[0]) {
                    bi.setRGB(x, y, 0x000000); // BLACK: Background predicted correctly
                    continue;
                }

                if (groundTruth[0] && !prediction[0]){
                    bi.setRGB(x, y, 0xFF0000); // RED: Background mis-predicted as Foreground
                    continue;
                }

                if (!groundTruth[0] && prediction[0]){
                    if (isBoundary(gtImage,x,y)) {
                        bi.setRGB(x, y, 0x000000); // BLACK: Background predicted on boundary
                    }else {
                        bi.setRGB(x, y, 0x00FFFF); // BLUE:  Foreground mis-predicted as Background
                    }
                    continue;
                }

                /* Iterate foreground classes
                 * At this point it's clear that both gt[0]/p[0] are false, therefore we can use hamming loss
                 * to detect exact matches
                 * GREEN:   Foreground predicted correctly
                 * YELLOW:  Foreground predicted - but the wrong class (e.g. Text instead of Comment)
                 */
                bi.setRGB(x, y, hammingLoss(groundTruth,prediction) == 0 ? 0x007F00 : 0xFFFF00 );
            }
        }

        return bi;
    }

    /**
     * This method overlaps the evaluation visualization with the original image to further
     * enable the user to spot and interpret the mistakes in the prediction
     * @param visualization the visualization image generated by this.visualizeEvaluation()
     * @param original the original image as it is in the dataset
     * @return a BufferedImage representing the overlapped images
     */
    public BufferedImage overlapEvaluation(BufferedImage visualization, BufferedImage original){

        assert (visualization.getWidth() == original.getWidth());
        assert (visualization.getHeight() == original.getHeight());

        // Create new image of type ARGB (with alpha channel)
        BufferedImage overlap = new BufferedImage(visualization.getWidth(), visualization.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics g = overlap.getGraphics();

        // Paint original
        g.drawImage(original, 0, 0, null);

        // Set alpha
        ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.57f));

        // Paint visualization
        g.drawImage(visualization, 0, 0, null);

        return overlap;
    }

    /**
     * @return this evaluation object nicely formatted
     */
    @Override
    public String toString(){
        return prettyPrint(evaluation,jaccard,f1,precision,recall,frequencies);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Public-static
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * This method takes many values as parameter and gathers them in a nice and consistent way.
     * It is helpful if a human want to interpret the values visually without the aid of a third
     * party software.
     * It is implemented in a static method and not in the toString() to enable printing averaged
     * values (done externally of this class).
     * @param evaluation see evaluation variable of this class
     * @param jaccard  see jaccard variable of this class
     * @param f1  see f1 variable of this class
     * @param precision  see precision variable of this class
     * @param recall  see recall variable of this class
     * @param frequencies  see frequencies variable of this class
     * @return  a string containing all values passed as parameter nicely formatted
     */
    public static String prettyPrint(double[] evaluation,
                                     double[] jaccard,
                                     double[] f1,
                                     double[] precision,
                                     double[] recall,
                                     double[] frequencies){

        StringBuilder s = new StringBuilder();

        s.append("EM=").append(String.format("%2.2f",evaluation[0]));

        s.append(" HS=").append(String.format("%2.2f",evaluation[1]));

        s.append(" IU=").append(String.format("%2.2f",evaluation[2])).append(",").append(String.format("%2.2f",evaluation[3]));
        s.append("[");
        for (int i = 0; i < jaccard.length; i++) {
            s.append(String.format("%2.2f",jaccard[i])).append((i < jaccard.length-1)? "|" : "");
        }
        s.append("]");

        s.append(" F1=").append(String.format("%2.2f",evaluation[4])).append(",").append(String.format("%2.2f",evaluation[7]));
        s.append("[");
        for (int i = 0; i < f1.length; i++) {
            s.append(String.format("%2.2f",f1[i])).append((i < f1.length-1)? "|" : "");
        }
        s.append("]");

        s.append(" P=").append(String.format("%2.2f",evaluation[5])).append(",").append(String.format("%2.2f",evaluation[6]));
        s.append("[");
        for (int i = 0; i < precision.length; i++) {
            s.append(String.format("%2.2f",precision[i])).append((i < precision.length-1)? "|" : "");
        }
        s.append("]");

        s.append(" R=").append(String.format("%2.2f",evaluation[6])).append(",").append(String.format("%2.2f",evaluation[9]));
        s.append("[");
        for (int i = 0; i < recall.length; i++) {
            s.append(String.format("%2.2f",recall[i])).append((i < recall.length-1)? "|" : "");
        }
        s.append("]");

        s.append("Freq:[");
        for (int i = 0; i < frequencies.length; i++) {
            s.append(String.format("%2.2f",frequencies[i])).append((i < frequencies.length-1)? "|" : "");
        }
        s.append("]");

        return s.toString();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Getters
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public double[] getFrequencies() {
        return frequencies;
    }

    public double[] getEvaluation() {
        return evaluation;
    }

    public double[] getJaccard() {
        return jaccard;
    }

    public double[] getF1() {
        return f1;
    }

    public double[] getPrecision() {
        return precision;
    }

    public double[] getRecall() {
        return recall;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Computes the mean of the values passed as parameter
     * @param values vector of values to compute the mean on
     * @return the mean of the values
     */
    private double mean(final double[] values){
        double[] weights = new double[values.length];
        Arrays.fill(weights,1);
        return weightedMean(values,weights);
    }

    /**
     * Computes the weights mean of the values passed as parameter with
     * their relative weights
     * @param values vector of values to compute the mean on
     * @param weights weights of the vector of values
     * @return the weighted mean of the values
     */
    private double weightedMean(double[] values, double[] weights){
        assert (values.length == weights.length);

        double weightedMean = 0;
        double totalWeight = 0;
        for (int c = 0; c < values.length; c++) {
            if(!Double.isNaN(values[c])) {
                weightedMean += values[c] * weights[c];
                totalWeight += weights[c];
            }
        }
        return weightedMean / totalWeight;
    }

    /**
     * Computes the class frequencies from the class-wise binary confusion matrices
     * @param cm binary confusion matrices for each class in the GT (size must be 2 x 2 x nbClasses)
     * @return class accuracies in an array, where the total sum of frequencies equals 1
     */
    private double[] classFrequencies(double[][][] cm){
        assert (cm.length == 2);
        assert (cm[0].length == 2);

        final int L = cm[0][0].length;

        double[] frequencies = new double[L];

        // Integrate the distribution
        int totalAmountOfLabels = 0;
        for (int c = 0; c < L; c++) {
            frequencies[c] = cm[0][0][c] + cm[0][1][c]; // TP + FN
            totalAmountOfLabels += frequencies[c];
        }
        // Normalise
        for (int c = 0; c < L; c++) {
            frequencies[c] /= totalAmountOfLabels;
        }

        return frequencies;
    }

    /**
     * Computes class-wise Intersection over Union metric, from the class-wise binary confusion matrices
     * @param cm binary confusion matrices for each class in the GT (size must be 2 x 2 x nbClasses)
     * @return class-wise Intersection over Union metric
     */
    private double[] computeJaccard(double[][][] cm){
        assert (cm.length == 2);
        assert (cm[0].length == 2);

        final int L = cm[0][0].length;

        double[] jaccard = new double[L];
        for (int c = 0; c < L; c++) {
            jaccard[c] = cm[0][0][c] / (cm[0][0][c] +  cm[0][1][c] + cm[1][0][c]);
        }
        return jaccard;
    }

    /**
     * Computes class-wise precision from the class-wise binary confusion matrices
     * PRECISION = TP / (TP + FP)
     * @param cm binary confusion matrices for each class in the GT (size must be 2 x 2 x nbClasses)
     * @return class-wise precision
     */
    private double[] computePrecision(double[][][] cm){
        assert (cm.length == 2);
        assert (cm[0].length == 2);

        final int L = cm[0][0].length;

        double[] precision = new double[L];
        for (int c = 0; c < L; c++) {
            precision[c] = cm[0][0][c] / (cm[0][0][c] + cm[1][0][c]);
        }
        return precision;
    }

    /**
     * Computes class-wise recall from the class-wise binary confusion matrices
     * RECALL = TP / (TP + FN)
     * @param cm binary confusion matrices for each class in the GT (size must be 2 x 2 x nbClasses)
     * @return class-wise recall
     */
    private double[] computeRecall(double[][][] cm){
        assert (cm.length == 2);
        assert (cm[0].length == 2);

        final int L = cm[0][0].length;

        double[] recall = new double[L];
        for (int c = 0; c < L; c++) {
            recall[c] = cm[0][0][c] / (cm[0][0][c] + cm[0][1][c]);
        }
        return recall;
    }

    /**
     * Computes the class-wise F1-score given class-wise precision and recall,
     * i.e the length of precision, recall and F1 vectors is the number of classes in the GT
     * F1 = 2*PRECISION*RECALL / (PRECISION+RECALL)
     * @param cm binary confusion matrices for each class in the GT (size must be 2 x 2 x nbClasses)
     * @return class-wise F1-score
     */
    private double[] computeF1(double[][][] cm){
        assert (cm.length == 2);
        assert (cm[0].length == 2);

        final int L = cm[0][0].length;

        double[] F1 = new double[L];
        for (int c = 0; c < L; c++) {
            F1[c] = 2*cm[0][0][c] / (2*cm[0][0][c] +  cm[0][1][c] + cm[1][0][c]);
        }
        return F1;
    }

    /**
     * Computes the hamming loss between two vectors of boolean.
     * @param gt vector of labels of the GT
     * @param p vector of predicted labels
     * @return the value of the hamming loss.
     */
    private double hammingLoss(boolean[] gt, boolean[] p){
        assert (gt.length == p.length);
        final int L = gt.length;

        // Compute the hamming distance between the two vectors
        double loss=0;
        for (int c = 0; c < L; c++) {
            loss += (gt[c] != p[c]) ? 1 : 0;
        }

        return loss / L;
    }

    /**
     * Retrieves the label as an array of boolean from the image passed as parameter
     * @param image the image where to read from
     * @param x x-coordinate of the selected pixel
     * @param y y-coordinate of the selected pixel
     * @param nbClasses total number of different classes in the GT
     * @return an array of boolean with TRUE in correspondence of the present labels at the selected location.
     *         e.g: pixel value of 0x0110 will be converted in new boolean{false,true,true,false};
     */
    private boolean[] getLabels(BufferedImage image, int x, int y, int nbClasses) {
        boolean[] labels= new boolean[nbClasses];
        int rgb = image.getRGB(x, y);
        for (int c = 0; c < nbClasses; c++) {
            labels[c] = ((rgb >> c) & 0x1) == 1;
        }
        return labels;
    }

    /**
     * SegmentationAnalysisTool if the pixel at position (x, y) is on the boundary.
     * @param gtImage the image to look into
     * @param x x coordinate in the image
     * @param y y coordinate in the image
     * @return true when is boundary
     */
    private boolean isBoundary(BufferedImage gtImage, int x, int y) {
        return ((gtImage.getRGB(x, y)>>23) & 0x1) == 1;
    }

}