/*
 * Copyright (c) 2017 UniFR
 * University of Fribourg, Switzerland.
 */

package ch.unifr.experimenter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.PrintWriter;

/**
 * This class provides a runnable main for using the SegmentationAnalysis tool
 * for the HisDoc Layout Competition of the ICDAR 2017
 *
 * @author Michele Alberti <michele.alberti@unifr.ch>
 * @date 28.06.2017
 * @brief The layout analysis evaluator main class
 */
public class SegmentationAnalysisTool {

    public static void main(String[] args) throws Exception {

        // Verify parameters syntax
        if (args.length != 2 && args.length != 3 && args.length != 4) {
            System.err.println("Syntax:\n java -jar SegmentationAnalysisTool.jar gtImagePath resultImagePath [outputFolder] [resultFile]");
            System.exit(1);
        }
        String truthImagePath = args[0];
        String outputImagePath = args[1];

        // Perform the evaluation
        SegmentationAnalysis segmentationAnalysis = new SegmentationAnalysis(truthImagePath, outputImagePath, 4);
        double[] results = segmentationAnalysis.evaluateImages();

        // Print the metric used to determine winner in competition
        System.out.printf("Mean IU (Jaccard index) = %.5f\n", results[2]);

        // Print all metrics computed
        System.out.print("Additional scores: ");
        for (double result : results) {
            System.out.printf("%.5f ", result);
        }
        System.out.print("\n");

        // If desired, visualize the evaluation and save the image on file
        if (args.length >= 3) {
            String evalPath = args[2];
            if (evalPath.lastIndexOf(File.separator) != evalPath.length()) {
                evalPath += File.separator;
            }
            evalPath += outputImagePath.substring(outputImagePath.lastIndexOf(File.separator) + 1, outputImagePath.lastIndexOf('.'));
            evalPath += ".visualization.png";
            ImageIO.write(segmentationAnalysis.visualiseEvaluation(), "png", new File(evalPath));
            System.out.println("Visualization image written in: " + evalPath);
        }

        //if desired write JSON output
        if (args.length == 4) {

            JsonObject result = new JsonObject();
            JsonArray output = new JsonArray();

            //Exact Match
            JsonObject exactMatchContent = new JsonObject();
            exactMatchContent.add("name", new JsonPrimitive("exactMatch"));
            exactMatchContent.add("value", new JsonPrimitive(results[0]));
            exactMatchContent.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject exactMatch = new JsonObject();
            exactMatch.add("number", exactMatchContent);
            output.add(exactMatch);

            //Hamming Score (1 - hamming loss)
            JsonObject hammingScoreContent = new JsonObject();
            hammingScoreContent.add("name", new JsonPrimitive("hammingScore"));
            hammingScoreContent.add("value", new JsonPrimitive(results[1]));
            hammingScoreContent.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject hammingScore = new JsonObject();
            hammingScore.add("number", hammingScoreContent);
            output.add(hammingScore);

            //mean Jaccard index (IU)
            JsonObject meanJaccardIndexContent = new JsonObject();
            meanJaccardIndexContent.add("name", new JsonPrimitive("meanJaccardIndex"));
            meanJaccardIndexContent.add("value", new JsonPrimitive(results[2]));
            meanJaccardIndexContent.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject meanJaccardIndex = new JsonObject();
            meanJaccardIndex.add("number", meanJaccardIndexContent);
            output.add(meanJaccardIndex);

            //Jaccard index (IU)
            JsonObject jaccardIndexContent = new JsonObject();
            jaccardIndexContent.add("name", new JsonPrimitive("jaccardIndex"));
            jaccardIndexContent.add("value", new JsonPrimitive(results[3]));
            jaccardIndexContent.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject jaccardIndex = new JsonObject();
            jaccardIndex.add("number", jaccardIndexContent);
            output.add(jaccardIndex);

            //Pixel Accuracy
            JsonObject accuracyContent = new JsonObject();
            accuracyContent.add("name", new JsonPrimitive("accuracy"));
            accuracyContent.add("value", new JsonPrimitive(results[4]));
            accuracyContent.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject accuracy = new JsonObject();
            accuracy.add("number", accuracyContent);
            output.add(accuracy);

            //mean F1-score
            JsonObject meanf1Content = new JsonObject();
            meanf1Content.add("name", new JsonPrimitive("meanF1Score"));
            meanf1Content.add("value", new JsonPrimitive(results[5]));
            meanf1Content.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject meanF1 = new JsonObject();
            meanF1.add("number", meanf1Content);
            output.add(meanF1);

            //mean precision
            JsonObject meanPrecisionContent = new JsonObject();
            meanPrecisionContent.add("name", new JsonPrimitive("meanPrecision"));
            meanPrecisionContent.add("value", new JsonPrimitive(results[6]));
            meanPrecisionContent.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject meanPrecision = new JsonObject();
            meanPrecision.add("number", meanPrecisionContent);
            output.add(meanPrecision);

            //mean recall
            JsonObject meanRecallContent = new JsonObject();
            meanRecallContent.add("name", new JsonPrimitive("meanRecall"));
            meanRecallContent.add("value", new JsonPrimitive(results[7]));
            meanRecallContent.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject meanRecall = new JsonObject();
            meanRecall.add("number", meanRecallContent);
            output.add(meanRecall);

            //f1-score
            JsonObject f1ScoreContent = new JsonObject();
            f1ScoreContent.add("name", new JsonPrimitive("f1Score"));
            f1ScoreContent.add("value", new JsonPrimitive(results[8]));
            f1ScoreContent.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject f1Score = new JsonObject();
            f1Score.add("number", f1ScoreContent);
            output.add(f1Score);

            //precision
            JsonObject precisionContent = new JsonObject();
            precisionContent.add("name", new JsonPrimitive("precision"));
            precisionContent.add("value", new JsonPrimitive(results[9]));
            precisionContent.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject precision = new JsonObject();
            precision.add("number", precisionContent);
            output.add(precision);

            //recall
            JsonObject recallContent = new JsonObject();
            recallContent.add("name", new JsonPrimitive("recall"));
            recallContent.add("value", new JsonPrimitive(results[10]));
            recallContent.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject recall = new JsonObject();
            recall.add("number", recallContent);
            output.add(recall);

            result.add("output", output);
            PrintWriter writer = new PrintWriter(args[4], "UTF-8");
            writer.print(result);
            writer.close();

        }
    }

}