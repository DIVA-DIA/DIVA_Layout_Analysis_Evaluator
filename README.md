# LayoutAnalysisEvaluator


Layout Analysis Evaluator for:
  * [ICDAR 2019 Historical Document Reading Challenge on Large Structured Chinese Family Records](https://www.ltu.se/research/subjects/Maskininlarning/ICDAR-2019-HDRC-Chinese?l=en "ICDAR 2019 Historical Document Reading Challenge on Large Structured Chinese Family Records Homepage")
  * [ICDAR 2017 competition on Layout Analysis for Challenging Medieval Manuscripts](https://diuf.unifr.ch/main/hisdoc/icdar2017-hisdoc-layout-comp "ICDAR 2017 competition on Layout Analysis for Challenging Medieval Manuscripts Homepage")

Minimal usage: `java -jar LayoutAnalysisEvaluator.jar -gt gt_image.png -p prediction_image.png`

Parameters list: utility-name
```
 -gt,--groundTruth <arg>      Ground Truth image 
 -p,--prediction <arg>        Prediction image 
 -o,--original <arg>          (Optional) Original image, to be overlapped with the results visualization
 -j,--json <arg>              (Optional) Json Path, for the DIVAServices JSON output
 -out,--outputPath <arg>      (Optional) Output path (relative to prediction input path)                            
 -dv,--DisableVisualization   (Optional)(Flag) Vsualizing the evaluation as image is NOT desired
 ```
**Note:** this also outputs a human-friendly visualization of the results next to the
 `prediction_image.png` which can be overlapped to the original image if provided 
 with the parameter `-overlap` to enable deeper analysis. 

## Visualization of the results

Along with the numerical results (such as the Intersection over Union (IU), precision, recall,F1) 
the tool provides a human friendly visualization of the results. 
Additionally, when desired one can provide the original image and it will be overlapped with 
the visualization of the results.
This is particularly helpful to understand why certain artifacts are created. 
The three images below represent the three steps: the original image, the visualization of the result 
and the two overlapped.

![Alt text](examples/original.png?raw=true)
![Alt text](examples/visualization.png?raw=true)
![Alt text](examples/overlap.png?raw=true)

### Interpreting the colors

Pixel colors are assigned as follows:

- GREEN:   Foreground predicted correctly
- YELLOW:  Foreground predicted - but the wrong class (e.g. Text instead of Comment)
- BLACK:   Background predicted correctly
- RED:     Background mis-predicted as Foreground 
- BLUE:    Foreground mis-predicted as Background

### Example of problem hunting

Below there is an example supporting the usefulness of overlapping the prediction quality visualization with the original image.
Focus on the red pixels pointed at by the white arrow: they are background pixels mis-classified as foreground.
In the normal visualization (left) its not possible to know why would an algorithm decide that in that
spot there is something belonging to foreground, as it is clearly far from regular text.
However, when overlapped with the original image (right) one can clearly see that in this area there is an 
ink stain which could explain why the classification algorithm is deceived into thinking these pixel were
foreground. This kind of interpretation is obviously not possible without the information provided by the
original image like in (right).

![Alt text](examples/visualization_error.png?raw=true)
![Alt text](examples/overlap_error.png?raw=true)


## Ground Truth Format

The ground truth information needs to be a pixel-label image where the class information is encoded in the blue
channel. 
Red and green channels should be set to 0 with the exception of the boundaries pixels used in the two competitions mentioned above.

For example, in the DIVA-HisDB dataset there are four different annotated classes which might overlap:
main text body, decorations, comments and background.  

In the pixel-label images the classes are encoded by RGB values as follows:

    Red = 0 everywhere (except boundaries)
    Green = 0 everywhere
    
    Blue = 0b00...1000 = 0x000008: main text body
    Blue = 0b00...0100 = 0x000004: decoration
    Blue = 0b00...0010 = 0x000002: comment
    Blue = 0b00...0001 = 0x000001: background (out of page)

Note that the GT might contain multi-class labeled pixels, for all classes except for the background.
For example:

    Blue = 0b...1000 | 0b...0010 = 0b...1010 = 0x00000A : main text body + comment  
    Blue = 0b...1000 | 0b...0100 = 0b...1100 = 0x00000C : main text body + decoration
    Blue = 0b...0010 | 0b...0100 = 0b...0110 = 0x000006 : comment + decoration


## Citing us

If you use our software, please cite our paper as:

``` latex
@inproceedings{alberti2017evaluation,
    address = {Kyoto, Japan},
    archivePrefix = {arXiv},
    arxivId = {1712.01656},
    author = {Alberti, Michele and Bouillon, Manuel and Ingold, Rolf and Liwicki, Marcus},
    booktitle = {2017 14th IAPR International Conference on Document Analysis and Recognition (ICDAR)},
    doi = {10.1109/ICDAR.2017.311},
    eprint = {1712.01656},
    isbn = {978-1-5386-3586-5},
    month = {nov},
    pages = {43--47},
    title = {{Open Evaluation Tool for Layout Analysis of Document Images}},
    year = {2017}
}
```
