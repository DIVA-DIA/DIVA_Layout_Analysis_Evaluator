/*
 * Copyright (c) 2017 UniFR
 * University of Fribourg, Switzerland.
 */

package ch.unifr.experimenter;

import javax.imageio.ImageIO;
import java.io.File;

/**
 * This class provides a runnable main for using the SegmentationAnalysis tool
 * for the HisDoc Layout Competition of the ICDAR 2017
 *
 * @author Michele Alberti <michele.alberti@unifr.ch>
 * @date 28.06.2017
 * @brief The layout analysis evaluator main class
 *
 */
public class SegmentationAnalysisTool {

    public static void main(String[] args) throws Exception {

        // Verify parameters syntax
        if (args.length!=2 && args.length!=3) {
            System.err.println("Syntax:\n java -jar SegmentationAnalysisTool.jar gtImagePath resultImagePath [outputFolder]");
            System.exit(1);
        }
        String truthImagePath = args[0];
        String outputImagePath = args[1];

        // Perform the evaluation
        SegmentationAnalysis segmentationAnalysis = new SegmentationAnalysis(truthImagePath, outputImagePath, 4);
        double[] results = segmentationAnalysis.evaluateImages();

        // Print the metric used to determine winner in competition
        System.out.println("Mean UI (JaccardIndex) = " + results[2]);

        // Print all metrics computed
        System.out.print("Additional scores: ");
        for (double result : results) {
            System.out.print(result + " ");
        }

        // If desired, visualize the evaluation and save the image on file
        if (args.length == 3) {
            String evalPath = args[2];
            if (evalPath.lastIndexOf(File.separator) != evalPath.length()) {
                evalPath += File.separator;
            }
            evalPath += outputImagePath.substring(0, outputImagePath.lastIndexOf('.'));
            evalPath += ".visualization.png";
            ImageIO.write(segmentationAnalysis.visualiseEvaluation(), "png", new File(evalPath));
            System.out.println("Visualization image written in: " + evalPath);
        }
    }

}