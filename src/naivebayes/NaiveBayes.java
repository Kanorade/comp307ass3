package naivebayes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class NaiveBayes {
    String[] labels;
    List<List<String>> trainingSet;
    List<List<String>> testSet;
    final static int CLASS_INDEX = 0;

    public NaiveBayes(String[] args) {
        // process arguments
        if (args.length != 2) {
            System.out.println("USAGE naive-bayes.jar <training-filename> <test-filename>");
            System.exit(0);
        }
        try {
            labels = readFirstLine(args[0]);
            trainingSet = readFileIgnoringFirstLine(args[0]);
            testSet = readFileIgnoringFirstLine(args[1]);
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't find both files");
            System.err.println("\nMake sure file is in same directory as the jar file. " +
                    "Otherwise you need to include the entire filepath too.");
            System.exit(0);
        } catch (IOException e) {
            System.out.println("Something went wrong reading the files");
            throw new RuntimeException(e);
        }

        train(trainingSet, labels);
    }

    private void train(List<List<String>> trainingSet, String[] labels) {
        Map<String, Integer> classLabelCount = new HashMap<>();
        Map<Features, Integer> featureCount = new HashMap<>();

        // Initialise counts
        for (List<String> instance : trainingSet) {
            for (int i = 0; i < labels.length; i++) {
                if (i == CLASS_INDEX) {
                    classLabelCount.put(instance.get(i), 1);
                } else {
                    String feature = labels[i];
                    String value = instance.get(i);
                    String classLabel = instance.get(CLASS_INDEX);
                    featureCount.put(new Features(feature, value, classLabel), 1);
                }
            }
        }
        System.out.println(classLabelCount);
        System.out.println(featureCount);
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

    static class Features {
        private final String feature;
        private final String value;
        private final String classification;

        public Features(String feature, String value, String classification) {
            this.feature = feature;
            this.value = value;
            this.classification = classification;
        }

        @Override
        public String toString() {
            return "Features{" +
                    "feature='" + feature + '\'' +
                    ", value='" + value + '\'' +
                    ", classification='" + classification + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Features that = (Features) o;
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
