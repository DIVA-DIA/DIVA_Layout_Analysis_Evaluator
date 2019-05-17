/*
 * Copyright (c) 2017 UniFR
 * University of Fribourg, Switzerland.
 */

package ch.unifr;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.cli.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;

/**
 * This class provides a runnable main for using the SegmentationAnalysis tool
 * for the HisDoc Layout Competition of the ICDAR 2017
 *
 * @author Michele Alberti <michele.alberti@unifr.ch>
 * @date 24.07.2017
 * @brief The layout analysis evaluator main class
 */
public class LayoutAnalysisEvaluatorTool {

    public static void main(String[] args) throws Exception {

        ///////////////////////////////////////////////////////////////////////////////////////////////
        // Parse parameters
        ///////////////////////////////////////////////////////////////////////////////////////////////
        Options options = new Options();

        // GT input path
        Option input = new Option("gt", "groundTruth", true, "Ground Truth image file path");
        input.setRequired(true);
        options.addOption(input);

        // Prediction input path
        Option output = new Option("p", "prediction", true, "Prediction image file path");
        output.setRequired(true);
        options.addOption(output);

        // Visualize evaluation as image (optional)
        options.addOption(new Option("dv", "disableVisualization", false, "Flag whether visualizing the evaluation as image is NOT desired"));

        // Overlap original image input path (optional)
        options.addOption(new Option("o", "overlap", true, "Overlap original image file path"));

        // Output path, relative to prediction input path (optional)
        options.addOption(new Option("out", "outputPath", true, "Output path, absolute or relative to prediction input path"));

        // Result file as JSON (optional)
        options.addOption(new Option("j", "json", true, "Json Path, for the DIVAServices JSON output"));

        // Parse arguments
        CommandLine cmd;

        try {
            cmd = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("utility-name", options);
            System.exit(1);
            return;
        }

        // Assign compulsory parameter values
        String groundTruthImagePath = cmd.getOptionValue("groundTruth").replace("/", File.separator);
        String predictionImagePath = cmd.getOptionValue("prediction").replace("/", File.separator);

        // Set the path of the prediction as starting output path
        String outputPath = predictionImagePath.substring(0, predictionImagePath.lastIndexOf(File.separator) + 1);

        // Add any relative path from there (if specified)
        if (cmd.hasOption("outputPath")) {
            File file = new File(cmd.getOptionValue("outputPath"));
            if (file.isAbsolute()) {
                outputPath = cmd.getOptionValue("outputPath").replace("/", File.separator);
            } else {
                outputPath += cmd.getOptionValue("outputPath").replace("/", File.separator);
            }
        }

        // Make sure last char is a file separator
        if (outputPath.lastIndexOf(File.separator) + 1 != outputPath.length()) {
            outputPath += File.separator;
        }

        // Add the prediction file name without extension
        outputPath += predictionImagePath.substring(predictionImagePath.lastIndexOf(File.separator) + 1, predictionImagePath.lastIndexOf('.'));

        ///////////////////////////////////////////////////////////////////////////////////////////////
        // Perform the evaluation
        ///////////////////////////////////////////////////////////////////////////////////////////////

        // Run the evaluation
        LayoutAnalysisEvaluator segmentationAnalysis = new LayoutAnalysisEvaluator(groundTruthImagePath, predictionImagePath);
        double[] results = segmentationAnalysis.evaluateImages();

        // Print the metric used to determine winner in competition
        System.out.printf("Mean IU (Jaccard index) = %.5f\n", results[2]);

        // Print all metrics computed
        System.out.println(segmentationAnalysis);

        // If desired, visualize the evaluation and save the image on file
        if (!cmd.hasOption("disableVisualization")) {
            String visualizationFilePath = outputPath + ".visualization.png";
            BufferedImage visualization = segmentationAnalysis.visualiseEvaluation();
            ImageIO.write(visualization, "png", new File(visualizationFilePath));
            System.out.println("Visualization image written in: " + visualizationFilePath);

            if (cmd.hasOption("overlap")) {
                String overlapFilePath = outputPath + ".overlap.png";
                ImageIO.write(segmentationAnalysis.overlapEvaluation(visualization, ImageIO.read(new File(cmd.getOptionValue("overlap")))), "png", new File(overlapFilePath));
                System.out.println("Overlap image written in: " + overlapFilePath);
            }
        }

        // If desired write JSON output
        if (cmd.hasOption("json")) {
            String jsonPath = cmd.getOptionValue("json").replace("/", File.separator);
            JsonObject jsonResult = new JsonObject();
            JsonArray jsonOutput = new JsonArray();

            //exact match
            JsonObject exactmatchcontent = new JsonObject();
            exactmatchcontent.add("name", new JsonPrimitive("exactmatch"));
            exactmatchcontent.add("value", new JsonPrimitive(results[0]));
            exactmatchcontent.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject exactmatch = new JsonObject();
            exactmatch.add("number", exactmatchcontent);
            jsonOutput.add(exactmatch);

            //hamming score (1 - hamming loss)
            JsonObject hammingscorecontent = new JsonObject();
            hammingscorecontent.add("name", new JsonPrimitive("hammingscore"));
            hammingscorecontent.add("value", new JsonPrimitive(results[1]));
            hammingscorecontent.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject hammingscore = new JsonObject();
            hammingscore.add("number", hammingscorecontent);
            jsonOutput.add(hammingscore);

            //mean jaccard index (iu)
            JsonObject meanjaccardindexcontent = new JsonObject();
            meanjaccardindexcontent.add("name", new JsonPrimitive("meanjaccardindex"));
            meanjaccardindexcontent.add("value", new JsonPrimitive(results[2]));
            meanjaccardindexcontent.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject meanjaccardindex = new JsonObject();
            meanjaccardindex.add("number", meanjaccardindexcontent);
            jsonOutput.add(meanjaccardindex);

            //jaccard index (iu)
            JsonObject jaccardindexcontent = new JsonObject();
            jaccardindexcontent.add("name", new JsonPrimitive("jaccardindex"));
            jaccardindexcontent.add("value", new JsonPrimitive(results[3]));
            jaccardindexcontent.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject jaccardindex = new JsonObject();
            jaccardindex.add("number", jaccardindexcontent);
            jsonOutput.add(jaccardindex);

            //mean f1-score
            JsonObject meanf1content = new JsonObject();
            meanf1content.add("name", new JsonPrimitive("meanf1score"));
            meanf1content.add("value", new JsonPrimitive(results[4]));
            meanf1content.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject meanf1 = new JsonObject();
            meanf1.add("number", meanf1content);
            jsonOutput.add(meanf1);

            //mean precision
            JsonObject meanprecisioncontent = new JsonObject();
            meanprecisioncontent.add("name", new JsonPrimitive("meanprecision"));
            meanprecisioncontent.add("value", new JsonPrimitive(results[5]));
            meanprecisioncontent.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject meanprecision = new JsonObject();
            meanprecision.add("number", meanprecisioncontent);
            jsonOutput.add(meanprecision);

            //mean recall
            JsonObject meanrecallcontent = new JsonObject();
            meanrecallcontent.add("name", new JsonPrimitive("meanrecall"));
            meanrecallcontent.add("value", new JsonPrimitive(results[6]));
            meanrecallcontent.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject meanrecall = new JsonObject();
            meanrecall.add("number", meanrecallcontent);
            jsonOutput.add(meanrecall);

            //f1-score
            JsonObject f1scorecontent = new JsonObject();
            f1scorecontent.add("name", new JsonPrimitive("f1score"));
            f1scorecontent.add("value", new JsonPrimitive(results[7]));
            f1scorecontent.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject f1score = new JsonObject();
            f1score.add("number", f1scorecontent);
            jsonOutput.add(f1score);

            //precision
            JsonObject precisioncontent = new JsonObject();
            precisioncontent.add("name", new JsonPrimitive("precision"));
            precisioncontent.add("value", new JsonPrimitive(results[8]));
            precisioncontent.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject precision = new JsonObject();
            precision.add("number", precisioncontent);
            jsonOutput.add(precision);

            //recall
            JsonObject recallcontent = new JsonObject();
            recallcontent.add("name", new JsonPrimitive("recall"));
            recallcontent.add("value", new JsonPrimitive(results[9]));
            recallcontent.add("mime-type", new JsonPrimitive("text/plain"));

            JsonObject recall = new JsonObject();
            recall.add("number", recallcontent);
            jsonOutput.add(recall);

            PrintWriter writer = new PrintWriter(jsonPath, "utf-8");
            writer.print(jsonOutput);
            writer.close();
        }
    }
}
