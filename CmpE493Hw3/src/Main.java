import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {
	public static void main(String[] args) {
		// Read stories
		System.out.println("Reading stories...");
		ArrayList<Story> stories = readStories("Dataset");
		System.out.println("Reading stories DONE.");
		// Calculate tf-idf scores
		System.out.println("Calculating tf-idf scores...");
		ArrayList<ArrayList<HashMap<String, Double>>> tfIdfScores = calculateTfIdf(stories);
		System.out.println("Calculating tf-idf scores DONE.");
		// Build the cosine similarity matrix.
		System.out.println("Calculating similarities...");
		ArrayList<ArrayList<ArrayList<Double>>> similarities = buildSimilarityMatrices(stories, tfIdfScores);
		System.out.println("Calculating similarities DONE.");
	}
	
	/**
	 * Builds a similarity matrix for all documents.
	 */
	private static ArrayList<ArrayList<ArrayList<Double>>> buildSimilarityMatrices(ArrayList<Story> stories,
			ArrayList<ArrayList<HashMap<String, Double>>> tfIdfScores) {
		// <i, j, k> => in document i, similarity of sentence j to sentence k.
		ArrayList<ArrayList<ArrayList<Double>>> similarities = new ArrayList<>();
		for (int i = 0; i < stories.size(); i++) {
			similarities.add(buildSimilarityMatrix(stories.get(i), tfIdfScores.get(i)));
		}
		return similarities;
 	}
	
	/**
	 * Builds a similarity matrix for one document.
	 */
	private static ArrayList<ArrayList<Double>> buildSimilarityMatrix(Story story, ArrayList<HashMap<String,Double>> tfIdfForSentences) {
		ArrayList<ArrayList<Double>> similarities = new ArrayList<>();
		for (int i = 0; i < story.sentences.size(); i++) {
			ArrayList<Double> similarityVector = new ArrayList<>();
			for (int j = 0; j < story.sentences.size(); j++) {
				similarityVector.add(cosineSimilarity(tfIdfForSentences.get(i), tfIdfForSentences.get(j)));
			}
			similarities.add(similarityVector);
		}
		return similarities;
	}

	/**
	 * Calculates cosine similarity between two tf-idf vectors. 
	 */
	private static double cosineSimilarity(HashMap<String, Double> tfIdf1, HashMap<String, Double> tfIdf2) {
		tfIdf1 = normalizeVector(tfIdf1);
		tfIdf2 = normalizeVector(tfIdf2);
		
		double similarity = 0;
		for (String word : tfIdf1.keySet()) {
			if (tfIdf2.containsKey(word)) {
				similarity += tfIdf1.get(word) * tfIdf2.get(word);
			}
		}
		return similarity;
	}

	/**
	 * Normalizes given vector
	 */
	private static HashMap<String, Double> normalizeVector(HashMap<String, Double> tfIdf) {
		// Calculate length
		double length = 0;
		for (double value : tfIdf.values()) {
			length += value*value;
		}
		length = Math.sqrt(length);
		// Normalize
		HashMap<String, Double> normalized = new HashMap<>();
		for (String key : tfIdf.keySet()) {
			normalized.put(key, tfIdf.get(key)/length);
		}
		return normalized;
	}

	/**
	 * Calculates tf-idf scores for each word in story set.
	 */
	private static ArrayList<ArrayList<HashMap<String, Double>>> calculateTfIdf(ArrayList<Story> stories) {
		// < Word : # of d containing the word >.
		HashMap<String, Integer> df = new HashMap<>();
		// Array of < Word : # of times term occurs in sentence > for each doc
		ArrayList<ArrayList<HashMap<String, Integer>>> tf = new ArrayList<>();
		for (Story story : stories) {
			// Array of < Word : # of times term occurs in sentence > for this doc
			ArrayList<HashMap<String, Integer>> docTf = new ArrayList<>();
			// Dictionary for the document.
			HashSet<String> docDict = new HashSet<>();
				for (String line : story.sentences) {
				// < Word : # of times term occurs in sentence >
				HashMap<String, Integer> sentenceTf = new HashMap<>();	
				for (String word : normalizeText(line).split(" ")) {
					// Remove single char words
					if (word.length() < 2) {
						continue;
					}
					if (sentenceTf.containsKey(word)) {
						sentenceTf.put(word, sentenceTf.get(word) + 1);
					} else {
						sentenceTf.put(word, 1);	
					}
					docDict.add(word);
				}
				// Add this sentence's term frequencies.
				docTf.add(sentenceTf);
			}
			// Add this document's term frequencies.
			tf.add(docTf);
			// Update this story's words' df.
			for (String word : docDict) {
				if (df.containsKey(word)) {
					df.put(word, df.get(word) + 1);
				} else {
					df.put(word, 1);	
				}
			}
		}
		// Calculate idf from df scores.
		HashMap<String, Double> idf = new HashMap<>();
		for (String word : df.keySet()) {
			idf.put(word, Math.log10(1000.0/df.get(word)));
		}
		// Calculate tf-idf for all sentences.
		ArrayList<ArrayList<HashMap<String, Double>>> tfIdf = new ArrayList<>();
		for (int docId = 0; docId < tf.size(); docId++) {
			ArrayList<HashMap<String, Double>> tfIdfForDoc = new ArrayList<>();
			for (int sentenceId = 0; sentenceId < tf.get(docId).size(); sentenceId++) {
				HashMap<String, Double> tfIdfForSentence = new HashMap<>();
				for (String word : tf.get(docId).get(sentenceId).keySet()) {
					double tfScore = tf.get(docId).get(sentenceId).get(word) == 0 ? 0 : 1 + Math.log10(tf.get(docId).get(sentenceId).get(word));
					tfIdfForSentence.put(word, tfScore * idf.get(word));
				}
				tfIdfForDoc.add(tfIdfForSentence);
			}	
			tfIdf.add(tfIdfForDoc);
		}
		return tfIdf;
	}

	/**
	 * Gets rid of punctuation.
	 */
	private static String normalizeText(String text) {
		// Make text lowercase.
		text = text.toLowerCase();
		// Remove all punctuation marks and new lines.
		text = text.replaceAll("\\.", " ");
		text = text.replaceAll("\\,", " ");
		text = text.replaceAll("\\'", " ");
		text = text.replaceAll("\"", " ");
		text = text.replaceAll("\\/", " ");
		text = text.replaceAll("\\-", " ");
		text = text.replaceAll("\\_", " ");
		text = text.replaceAll("\\*", " ");
		text = text.replaceAll("<", " ");
		text = text.replaceAll(">", " ");
		text = text.replaceAll(Pattern.quote("!"), " ");
		text = text.replaceAll(Pattern.quote("?"), " ");
		text = text.replaceAll(Pattern.quote(";"), " ");
		text = text.replaceAll(Pattern.quote(":"), " ");
		text = text.replaceAll(Pattern.quote("("), " ");
		text = text.replaceAll(Pattern.quote(")"), " ");
		text = text.replaceAll(Pattern.quote("="), " ");
		text = text.replaceAll(Pattern.quote("$"), " ");
		text = text.replaceAll(Pattern.quote("%"), " ");
		text = text.replaceAll(Pattern.quote("#"), " ");
		text = text.replaceAll(Pattern.quote("+"), " ");
		text = text.replaceAll("\n", " ");
		return text;
	}
	
	/**
	 * Returns array of stories.
	 */
	private static ArrayList<Story> readStories(String directoryLocation) {
		ArrayList<Story> stories = new ArrayList<>();
		for (int i = 1; i <= 1000; i++) {
			stories.add(readStory(directoryLocation + "/" + i + ".txt"));
		}
		return stories;
	}

	/**
	 * Returns a single story.
	 */
	private static Story readStory(String fileLocation) {
		Scanner in = null;
		try {
			in = new Scanner(new File(fileLocation));
		} catch (FileNotFoundException e) {
			// File not found
			e.printStackTrace();
		}
		Story story = new Story();
		// Read story
		while (in.hasNextLine()) {
			String line = in.nextLine();
			if (line.equals("")) {
				break;
			}
			story.sentences.add(line);
		}
		// Read summary
		while (in.hasNextLine()) {
			story.sentences.add(in.nextLine());
		}
		in.close();
		return story;
	}
}
