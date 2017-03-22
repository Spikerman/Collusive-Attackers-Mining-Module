package MLPart;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.File;

/**
 * Author: Spikerman
 * Created Date: 17/2/27
 */
public class Classify {
    private Classifier classifier = new RandomForest();
    private Instances train;

    public Classify() {
        loadTrainingData();
    }

    public static void main(String args[]) {
        Classify c = new Classify();
        c.trainClassifier();
    }

    private void loadTrainingData() {
        try {
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File("result.csv"));
            train = loader.getDataSet();
            if (train.classIndex() == -1) {
                train.setClassIndex(train.numAttributes() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void trainClassifier() {
        try {
            classifier = new RandomForest();
            classifier.buildClassifier(train);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void evaluation(Classifier classifier, Instances data) {
        try {
            Evaluation eval = new Evaluation(data);
            eval.evaluateModel(classifier, data);
            System.out.println(eval.toSummaryString("\nResults\n\n", true));
            System.out.println(eval.toClassDetailsString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}