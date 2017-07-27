/*
 * Copyright (c) 2017 UniFR
 * University of Fribourg, Switzerland.
 */
package ch.unifr.experimenter;

import ch.unifr.experimenter.SegmentationAnalysis;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * This class test whether the class SegmentationAnalysis is correct
 *
 * @author Michele Alberti
 */
public class SegmentationAnalysisTest {

    @Test
    public void testToyExample() throws IOException {

        /*  Assume the following GT and prediction:
         *          1   2   3   4   5   6   7   8   9   10
         *  GT:     B   B   B   B   TD  TD  TD  TD  C'  TD'
         *  P:      B   T   TD  BD  BC  TC  T   TD  B   TC
         *
         *  The expected binary cm are (rows is GT, columns Prediction):
         *
         *      B                   !
         *  B   1,4,9,10            2,3
         *  !   5                   6,7,8
         *
         *      C                   !
         *  C   9
         *  !   5,6,10              1,2,3,4,7,8
         *
         *      D                   !
         *  D   8,10                5,6,7
         *  !   3,4                 1,2,9
         *
         *      T                   !
         *  T   6,7,8,10            5,
         *  !   2,3                 1,4,9
         *
         * From that we compute the following metrics as:
         * ___________
         * exact match
         * |1,8,9| = 3 , N=10 => 3/10 = 0.3
         * ___________
         * hamming score (1 - hamming loss)
         * ( 0+2+3+1+4+2+1+0+0+1)/4 = 3.5 , 3.5/10 = 0.35 , 1-0.35 = 0.65
         * ___________
         * mean Jaccard index (IU)
         * Jaccard index (IU)                  (frequency weighted)
         *
         * Jaccard(B)=4/7
         * Jaccard(C)=1/4
         * Jaccard(D)=2/7
         * Jaccard(T)=4/7
         *
         * mean = 0.41964
         * weighted mean = (6*4/7 + 1/4 + 5*2/7 + 5*4/7 ) / 17 = 0.4684
         * ___________
         * mean F1-score
         * mean precision
         * mean recall
         * F1-score                            (frequency weighted)
         * precision                           (frequency weighted)
         * recall                              (frequency weighted)
         *
         *  precision(B)=4/5
         *  precision(C)=1/4
         *  precision(D)=2/4
         *  precision(T)=4/6
         *
         *  recall(B)=4/6
         *  recall(C)=1/1
         *  recall(D)=2/5
         *  recall(T)=4/5
         *
         *  f1(B)= 2 * 4/5 * 4/6 / (4/5 + 4/6) = 32/30 * 30/44 = 0.7272
         *  f1(C)= 2 * 1/4 * 1/1 / (1/4 + 1/1) = 2/4 * 4/5 = 0.4
         *  f1(D)= 2 * 2/4 * 2/5 / (2/4 + 2/5) = 8/20 * 20/18 = 0.4444
         *  f1(T)= 2 * 4/6 * 4/5 / (4/6 + 4/5) = 32/30 * 30/44 = 0.7272
         *
         *  mean precision = (4/5 + 3/4 + 4/6) / 4 = 0.5541
         *  mean recall = (2/3 + 1 + 6/5)/4 = 0.7166
         *  mean f1 = (0.7272*2 + 0.4 + 0.4444)/4 = 0.5747
         *
         *  weighted mean precision = (6*4/5 + 1/4 + 5*2/4 + 5*4/6)/17 = 0.6401
         *  weighted mean recall = (6*4/6 + 1 + 5*2/5 + 5*4/5)/17 = 0.6470
         *  weighted mean f1 = (6*0.7272 + 0.4 + 5*0.4444 + 5*0.7272)/17 = 0.6248 (use full number from precision!)
         *
         */

        BufferedImage groundTruth = new BufferedImage(1,10, BufferedImage.TYPE_INT_RGB);
        BufferedImage prediction = new BufferedImage(1,10, BufferedImage.TYPE_INT_RGB);
        /*
         * Recall that:
         *
         * RGB=0b00...1000=0x000008: main text body
         * RGB=0b00...0100=0x000004: decoration
         * RGB=0b00...0010=0x000002: comment
         * RGB=0b00...0001=0x000001: background (out of page)
         *
         * main text body+comment : 0b...1000 | 0b...0010 = 0b...1010 = 0x00000A
         * main text body+decoration : 0b...1000 | 0b...0100 = 0b...1100 = 0x00000C
         * comment +decoration : 0b...0010 | 0b...0100 = 0b...0110 = 0x000006
         *
         * RGB=0b10...0000=0x800000: boundary pixel (to be combined with one of the classe, expect background)
         *
         */

        groundTruth.setRGB(0,0,0x000001); // B
        groundTruth.setRGB(0,1,0x000001); // B
        groundTruth.setRGB(0,2,0x000001); // B
        groundTruth.setRGB(0,3,0x000001); // B
        groundTruth.setRGB(0,4,0x00000C); // TD
        groundTruth.setRGB(0,5,0x00000C); // TD
        groundTruth.setRGB(0,6,0x00000C); // TD
        groundTruth.setRGB(0,7,0x00000C); // TD
        groundTruth.setRGB(0,8,0x800002); // C'
        groundTruth.setRGB(0,9,0x80000C); // TD'

        prediction.setRGB(0,0,0x000001); // B
        prediction.setRGB(0,1,0x000008); // T
        prediction.setRGB(0,2,0x00000C); // TD
        prediction.setRGB(0,3,0x000005); // BD
        prediction.setRGB(0,4,0x000003); // BC
        prediction.setRGB(0,5,0x00000A); // TC
        prediction.setRGB(0,6,0x000008); // T
        prediction.setRGB(0,7,0x00000C); // TD
        prediction.setRGB(0,8,0x000001); // B
        prediction.setRGB(0,9,0x00000A); // TC

        SegmentationAnalysis segmentationAnalysis = new SegmentationAnalysis(groundTruth, prediction, 4);

        double[] results = segmentationAnalysis.evaluateImages();

        assert (results[0] == 0.3);
        assert (results[1] == 0.65);
        assert (Math.abs(results[2] - 0.4196) < 0.0001);
        assert (Math.abs(results[3] - 0.4684) < 0.0001);
        assert (Math.abs(results[4] - 0.5747) < 0.0001);
        assert (Math.abs(results[5] - 0.5541) < 0.0001);
        assert (Math.abs(results[6] - 0.7166) < 0.0001);
        assert (Math.abs(results[7] - 0.6248) < 0.0001);
        assert (Math.abs(results[8] - 0.6401) < 0.0001);
        assert (Math.abs(results[9] - 0.6470) < 0.0001);
    }

    @Test
    public void testIdentity() throws IOException {
        SegmentationAnalysis segmentationAnalysis = new SegmentationAnalysis(
                "./test/e-codices_fmb-cb-0055_0098v_max.png",
                "./test/e-codices_fmb-cb-0055_0098v_max.png",
                4);

        double[] results = segmentationAnalysis.evaluateImages();

        assert (results[0] == 1);
        assert (results[1] == 1);
        assert (results[2] == 1);
        assert (results[3] == 1);
        assert (results[4] == 1);
        assert (results[5] == 1);
        assert (results[6] == 1);
        assert (results[7] == 1);
        assert (results[8] == 1);
        assert (results[9] == 1);
    }
}
