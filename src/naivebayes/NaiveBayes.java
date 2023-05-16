package naivebayes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class NaiveBayes {
    String[] labels;
    String[] classLabels;
    List<List<String>> trainingSet;
    List<List<String>> testSet;
    final static int CLASS_INDEX = 0;
    Map<String, List<String>> featureValueMap;
    public NaiveBayes(String[] args) {
        // process arguments
        if (args.length != 2) {
            System.out.println("USAGE naive-bayes.jar <training-filename> <test-filename>");
            System.exit(0);
        }
        try {
            System.out.println("Loading files...");
            labels = readFirstLine(args[0]);
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
        featureValueMap.put("breast-quad", List.of("left up", "left low", "right up", "right low", "central"));
        featureValueMap.put("irradiat", List.of("yes", "no"));

        classLabels = new String[] {"no-recurrence-events", "recurrence-events"};

        train(trainingSet, labels);
    }

    private void train(List<List<String>> trainingSet, String[] labels) {
        Map<String, Integer> classLabelCount = new HashMap<>();
        Map<CountKeys, Integer> featureCount = new HashMap<>();


        // Initialise counts
        for (String classLabel : classLabels) {
            String classification = classLabel;
            classLabelCount.put(classification, 1);

            for (Map.Entry<String, List<String>> featureEntry : featureValueMap.entrySet()) {
                String feature = featureEntry.getKey();
                for (String value : featureEntry.getValue()) {
                    CountKeys countKeys = new CountKeys(feature, value, classification);
                    featureCount.put(countKeys, 1);
                }
            }
        }

        System.out.println(featureCount);
        System.out.println(featureCount.size());
        // Count the numbers of each classification and the feature values related to it
        for (List<String> instance : trainingSet) {

        }

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

        @Override
        public String toString() {
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
            CountKeys that = (CountKeys) o;
            return feature.equals(that.feature) && value.equals(that.value) && classification.equals(that.classification);
        }

        @Override
        public int hashCode() {
            return Objects.hash(feature, value, classification);
        }
    }
    public static void main(String[] args) {
        new NaiveBayes(args);
    }

}
