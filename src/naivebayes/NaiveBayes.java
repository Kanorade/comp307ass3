package naivebayes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NaiveBayes {
    String[] labels;
    List<List<String>> trainingSet;
    List<List<String>> testSet;

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
    public static void main(String[] args) {
        new NaiveBayes(args);
    }
}
