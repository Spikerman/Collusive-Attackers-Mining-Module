package MLPart;

import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.File;

/**
 * Author: Spikerman < mail4spikerman@gmail.com >
 * Created Date: 17/2/27
 */
public class Classifier {
    public Instances data;

    public Classifier() {
        try {
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File("result.csv"));
            data = loader.getDataSet();
            if (data.classIndex() == -1) {
                data.setClassIndex(data.numAttributes() - 1);
            }

            NaiveBayes nb = new NaiveBayes();
            nb.buildClassifier(data);
            //todo 增加 random part 的处理
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        Classifier c = new Classifier();
    }
}
