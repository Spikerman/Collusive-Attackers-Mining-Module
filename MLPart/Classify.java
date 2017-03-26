package MLPart;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.unsupervised.attribute.Remove;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Spikerman
 * Created Date: 17/2/27
 */
public class Classify {
    public Set<Instance> collusivePairs = new HashSet<>();
    private Instances train;
    private Instances test;

    public Classify() {
        loadTrainingData();
        loadTestingData();
    }

    public static void main(String args[]) {
        Classify c = new Classify();
        c.classifyInstance();
    }

    private void loadTrainingData() {
        try {
            CSVLoader trainDataLoader = new CSVLoader();
            trainDataLoader.setSource(new File("train.csv"));
            train = trainDataLoader.getDataSet();
            train.setClassIndex(train.numAttributes() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTestingData() {
        try {
            CSVLoader testDataLoader = new CSVLoader();
            testDataLoader.setSource(new File("test.csv"));
            test = testDataLoader.getDataSet();
            test.setClassIndex(train.numAttributes() - 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void classifyInstance() {
        try {
            Classifier classifier = new RandomForest();
            int[] indices = new int[]{3, 4};
            Remove rm = new Remove();
            rm.setAttributeIndicesArray(indices);
            FilteredClassifier fc = new FilteredClassifier();
            fc.setFilter(rm);
            fc.setClassifier(classifier);
            fc.buildClassifier(train);
            int nonCollusiveCount = 0;
            for (int i = 0; i < test.numInstances(); i++) {
                double pred = fc.classifyInstance(test.instance(i));
                double dist[] = fc.distributionForInstance(test.instance(i));
                if ((int) pred == 1) {
                    collusivePairs.add(new Instance(test.instance(i).toString(3), test.instance(i).toString(4)));
                } else {
                    nonCollusiveCount++;
                }
                //System.out.println(test.instance(i).toString(3) + " " + test.instance(i).toString(4) + " " + pred);
                System.out.println(test.instance(i).value(0) + "  "
                        + test.instance(i).value(1) + "  "
                        + test.instance(i).value(2) + "  "
                        + pred);
            }
            System.out.println("识别出的可疑目标对总数: " + collusivePairs.size() + "  " + "     普通应用数: " + nonCollusiveCount);
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
