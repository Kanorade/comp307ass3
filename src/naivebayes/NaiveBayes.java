package naivebayes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class NaiveBayes {
    String[] featureLabels;
    String[] classLabels;
    List<List<String>> trainingSet;
    List<List<String>> testSet;
    final static int CLASS_INDEX = 0;
    Map<String, List<String>> featureValueMap;
    record Probabilities(HashMap<String, Double> classProb, HashMap<CountKeys, Double> valueProb){}
    public NaiveBayes(String[] args) {
        // process arguments
        if (args.length != 2) {
            System.out.println("USAGE naive-bayes.jar <training-filename> <test-filename>");
            System.exit(0);
        }
        try {
            System.out.println("Loading files...");
            featureLabels = readFirstLine(args[0]);
            trainingSet = readFileIgnoringFirstLine(args[0]);
            testSet = readFileIgnoringFirstLine(args[1]);
            System.out.println("Done!");
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't find both files");
            System.err.println("\nMake sure file is in same directory as the jar file. " +
                    "Otherwise you need to include the entire filepath too.");
            System.exit(0);
        } catch (IOException e) {
            System.out.println("Something went wrong reading the files");
            throw new RuntimeException(e);
        }

        System.out.printf("There are %d training instances and %d test instances.\n",
                trainingSet.size(), testSet.size());

        // Time to hard code the list of all possible values for each feature. It's the simplest way to
        // every value is accounted for. Especially the ones not accounted for in the training file.
        // A better practice would be to use a properties file.
        featureValueMap = new HashMap<>();
        featureValueMap.put("age", List.of("10-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80-89", "90-99"));
        featureValueMap.put("menopause", List.of("lt40", "ge40", "premeno"));
        featureValueMap.put("tumor-size", List.of("0-4", "5-9", "10-14", "15-19", "20-24", "25-29", "30-34", "35-39",
                "40-44", "45-49", "50-54", "55-59"));
        featureValueMap.put("inv-nodes", List.of("0-2", "3-5", "6-8", "9-11", "12-14", "15-17", "18-20", "21-23",
                "24-26", "27-29", "30-32", "33-35", "36-39"));
        featureValueMap.put("node-caps", List.of("yes", "no"));
        featureValueMap.put("deg-malig", List.of("1", "2", "3"));
        featureValueMap.put("breast", List.of("left", "right"));
        featureValueMap.put("breast-quad", List.of("left_up", "left_low", "right_up", "right_low", "central"));
        featureValueMap.put("irradiat", List.of("yes", "no"));

        classLabels = new String[] {"no-recurrence-events", "recurrence-events"};

        // Probability data from training data
        Probabilities prob = train(trainingSet, featureLabels, classLabels, featureValueMap);

        //Display probabilities
        System.out.println("\nClass probabilities: ");
        for (String classification : classLabels) {
            System.out.println(classification + " = " + prob.classProb.get(classification));
        }

        System.out.println("\nConditional Probabilities:");
        for (int i = 1; i < featureLabels.length; i++) {    // Skip i=0 as that was the class
            String feature = featureLabels[i];
            System.out.println(feature + ":");
            for (String value : featureValueMap.get(feature)) {
                System.out.print("\t" + value + ":");
                for (String classification : classLabels) {
                    CountKeys probKey = new CountKeys(feature, value, classification);
                    double valueProb = prob.valueProb.get(probKey);
                    System.out.print(" \t" + classification + " = " + valueProb);
                }
                System.out.println();   // newline
            }
        }

        makePredictions(testSet, classLabels, featureLabels, prob);
    }

    private void makePredictions(List<List<String>> testSet, String[] classLabels, String[] featureLabels, Probabilities prob) {
        System.out.println("\n\nUsing test instances to make predictions...\n");
        int successCount = 0;   // To keep track of successful predictions

        int instanceCount = 0;
        for (List<String> instance : testSet) {
            instanceCount++;
            System.out.println("instance " + instanceCount + ": ");
            String bestPrediction = "";
            double bestScore = 0;
            for (String classification : classLabels) {
                double score = prob.classProb.get(classification);
                for (int i = 1; i < featureLabels.length; i++) {    // Skip i=0 as that was the class
                    String feature = featureLabels[i];
                    String value = instance.get(i);
                    CountKeys probKey = new CountKeys(feature, value, classification);
                    score *= prob.valueProb.get(probKey);
                }
                System.out.println(" - score(Y = " + classification + ") = " + score);
                // Is score the best score?
                if (score > bestScore) {
                    bestScore = score;
                    bestPrediction = classification;
                }
            }
            System.out.println(" - prediction: " + bestPrediction);

            if (bestPrediction.equals(instance.get(CLASS_INDEX))) {
                successCount++;
            }
        }

        // Not part of the report, but for my own curiosity
        double accuracy = (double) successCount / testSet.size();
        System.out.println("\nAccuracy: " + (accuracy*100) + "%");
    }

    private Probabilities train(List<List<String>> trainingSet, String[] featureLabels, String[] classLabels,
                                Map<String, List<String>> featureValueMap) {
        Map<String, Integer> classCount = new HashMap<>();
        Map<CountKeys, Integer> featureCount = new HashMap<>();


        // Initialise counts
        for (String classLabel : classLabels) {
            classCount.put(classLabel, 1);

            for (Map.Entry<String, List<String>> featureEntry : featureValueMap.entrySet()) {
                String feature = featureEntry.getKey();
                for (String value : featureEntry.getValue()) {
                    CountKeys countKeys = new CountKeys(feature, value, classLabel);
                    featureCount.put(countKeys, 1);
                }
            }
        }

        System.out.println("\nInitialise counts...");
        System.out.println("Count size: " + featureCount.size());


        // Count the numbers of each classification and the feature values related to it
        for (List<String> instance : trainingSet) {
            String classification = instance.get(CLASS_INDEX);
            int newClassCount = classCount.get(classification) + 1;
            classCount.put(classification, newClassCount);

            for (int i = 1; i < featureLabels.length; i++) { // Skip i=0 as that was the class
                String feature = featureLabels[i];
                String value = instance.get(i);
                CountKeys keys = new CountKeys(feature, value, classification);
                int newCount = featureCount.get(keys) + 1;
                featureCount.put(keys, newCount);
            }
        }


        // Calculate the totals
        int classTotal = 0;
        Map<CountKeys, Integer> featureTotals = new HashMap<>();    // Could have used a 2d array,
                                                                    // but thought this was easier for some reason.
        for (String classification : classLabels) {
            classTotal += classCount.get(classification);    // adding to the total of all the times this class occurred

            for (int i = 1; i < featureLabels.length; i++) {    // Skip i=0 as that was the class
                String feature = featureLabels[i];
                CountKeys featureKey = new CountKeys(feature, classification);
                featureTotals.put(featureKey, 0);   // Initialise count to zero
                List<String> featureValues = featureValueMap.get(feature);
                for (String value : featureValues)  {
                    CountKeys valueKeys = new CountKeys(feature, value, classification);
                    int newCount = featureTotals.get(featureKey) + featureCount.get(valueKeys);
                    featureTotals.put(featureKey, newCount);
                }
            }
        }


        // Calculate the probabilities from all the counts
        System.out.println("\nGenerating probabilities...");
        Probabilities prob = new Probabilities(new HashMap<>(), new HashMap<>());
        for (String classification : classLabels) {
            double classProb = (double) classCount.get(classification) / classTotal;
            prob.classProb.put(classification, classProb);
            for (int i = 1; i < featureLabels.length; i++) {    // Skip i=0 as that was the class
                String feature = featureLabels[i];
                List<String> featureValues = featureValueMap.get(feature);
                for (String value : featureValues) {
                    CountKeys featureKeys = new CountKeys(feature, value, classification);
                    CountKeys totalKey = new CountKeys(feature, classification);
                    double valueProb = (double) featureCount.get(featureKeys) / featureTotals.get(totalKey);
                    prob.valueProb.put(featureKeys, valueProb);
                }
            }
        }

        return prob;
    }

    private String[] readFirstLine(String fileName) throws IOException {
        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);

        String line;
        line = br.readLine();
        // ignore first comma
        line = line.substring(line.indexOf(',')+1);

        fr.close();
        br.close();
        return line.split(",");
    }

    private List<List<String>> readFileIgnoringFirstLine(String fileName) throws IOException {
        List<List<String>> instances = new ArrayList<>();

        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);

        br.readLine();  // ignore first line
        String line;
        while ((line = br.readLine()) != null) {
            line = line.substring(line.indexOf(',')+1);
            String[] tokens = line.split(",");

            instances.add(Arrays.asList(tokens));
        }
        fr.close();
        br.close();
        return instances;
    }

    static class CountKeys {
        private final String feature;
        private final String value;
        private final String classification;

        public CountKeys(String feature, String value, String classification) {
            this.feature = feature;
            this.value = value;
            this.classification = classification;
        }

        public CountKeys(String feature, String classification) {
            this.feature = feature;
            this.classification = classification;
            value = null;
        }

        @Override
        public String toString() {
            if (value == null) {
                return "{" +
                        "feature='" + feature + '\'' +
                        ", classification='" + classification + '\'' +
                        '}';
            }
            return "{" +
                    "feature='" + feature + '\'' +
                    ", value='" + value + '\'' +
                    ", classification='" + classification + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CountKeys countKeys = (CountKeys) o;
            if (value == null) {
                if (countKeys.value != null) return false;
                return feature.equals(countKeys.feature) && classification.equals(countKeys.classification);
            }
            return feature.equals(countKeys.feature) && value.equals(countKeys.value) && classification.equals(countKeys.classification);
        }

        @Override
        public int hashCode() {
            if (value == null) {
                return Objects.hash(feature, classification);
            }
            return Objects.hash(feature, value, classification);
        }
    }
    public static void main(String[] args) {
        new NaiveBayes(args);
    }

}
