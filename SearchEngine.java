import java.io.*;
import java.util.*;

public class SearchEngine {
    public static void main(String[] args) {

        File directory = new File("Enter your path here");
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt"));

        if (files == null || files.length == 0) {
            System.err.println("No text files found in the specified directory.");
            return;
        }

        Map<String, Map<String, Integer>> wordDocumentMatrix = new HashMap<>();
        Set<String> documents = new TreeSet<>();

        for (File file : files) {
            try {
                String text = readTextFromFile(file);
                List<String> words = tokenizeAndNormalize(text);

                String documentName = "doc" + (documents.size() + 1);
                documents.add(documentName);
                for (String word : words) {
                    if (!wordDocumentMatrix.containsKey(word)) {
                        wordDocumentMatrix.put(word, new HashMap<>());
                    }
                    wordDocumentMatrix.get(word).put(documentName, wordDocumentMatrix.get(word).getOrDefault(documentName, 0) + 1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        DataStorage dataStorage = new DataStorage(wordDocumentMatrix, documents);
        List<String> words = new ArrayList<>(wordDocumentMatrix.keySet());
        Collections.sort(words);
        int[][] matrix = dataStorage.getMatrix();


        try {
            exportToCSV("Enter your path here", words, matrix, documents);
            System.out.println("CSV file successfully created.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String readTextFromFile(File file) throws IOException {
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append(" ");
            }
        }
        return text.toString();
    }


    public static List<String> tokenizeAndNormalize(String text) {
        String[] words = text.split("\\s+");
        List<String> normalizedWords = new ArrayList<>();
        for (String word : words) {
            word = word.replaceAll("[^a-zA-Z]", "");
            word = word.toLowerCase();
            if (!word.isEmpty()) {
                normalizedWords.add(word);
            }
        }
        return normalizedWords;
    }

    public static void exportToCSV(String filename, List<String> words, int[][] matrix, Set<String> documents) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.append("words,");
            for (String document : documents) {
                writer.append(document).append(",");
            }
            writer.append("\n");

            for (String word : words) {
                writer.append(word).append(",");
                int wordIndex = words.indexOf(word);
                for (int j = 0; j < matrix[wordIndex].length; j++) {
                    writer.append(String.valueOf(matrix[wordIndex][j])).append(",");
                }
                writer.append("\n");
            }
        }
    }
}


class DataStorage {
    private int[][] matrix;

    public DataStorage(Map<String, Map<String, Integer>> wordDocumentMatrix, Set<String> documents) {
        int numWords = wordDocumentMatrix.size();
        int numDocuments = documents.size();
        matrix = new int[numWords][numDocuments];

        List<String> words = new ArrayList<>(wordDocumentMatrix.keySet());
        Collections.sort(words);

        List<String> sortedDocuments = new ArrayList<>(documents);
        Collections.sort(sortedDocuments);

        for (int i = 0; i < numWords; i++) {
            String word = words.get(i);
            Map<String, Integer> wordCounts = wordDocumentMatrix.get(word);
            for (int j = 0; j < numDocuments; j++) {
                String document = sortedDocuments.get(j);
                matrix[i][j] = wordCounts.getOrDefault(document, 0);
            }
        }
    }

    public int[][] getMatrix() {
        return matrix;
    }
}
