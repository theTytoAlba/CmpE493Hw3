import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {
	public static void main(String[] args) throws FileNotFoundException {
		// Read stories
		System.out.println("Reading stories...");
		ArrayList<Story> stories = readStories(args[0]);
		System.out.println("Reading stories DONE.");
		// Calculate tf-idf scores
		System.out.println("Calculating tf-idf scores...");
		ArrayList<ArrayList<HashMap<String, Double>>> tfIdfScores = calculateTfIdf(stories);
		System.out.println("Calculating tf-idf scores DONE.");
		// Build the cosine similarity matrix.
		System.out.println("Calculating similarities...");
		ArrayList<ArrayList<ArrayList<Double>>> similarities = buildSimilarityMatrices(stories, tfIdfScores);
		System.out.println("Calculating similarities DONE.");
		// Build the adjacency matrix
		System.out.println("Building adjacency matrix...");
		ArrayList<ArrayList<ArrayList<Integer>>> adjacencyMatrix = buildAdjacencyMatrix(similarities);
		System.out.println("Building adjacency matrix DONE.");
		// Probabilities with teleportation
		System.out.println("Building transition matrix with teleportation...");
		ArrayList<ArrayList<ArrayList<Double>>> transitionMatrix = buildTransitionMatrix(adjacencyMatrix, 0.15);
		System.out.println("Building transition matrix with teleportation DONE.");
		// Power method
		System.out.println("Applying the power method...");
		ArrayList<ArrayList<Double>> distributions = applyPowerMethod(transitionMatrix);
		System.out.println("Applying the power method DONE.");
		System.out.println(distributions.get(Integer.parseInt(args[1].split("\\.")[0])-1).toString());
		// Create reference files for Rouge.
		/*
		for (int i = 1; i <= stories.size(); i++) {
			String taskName = "news" + i;
			try {
				PrintWriter writer = new PrintWriter(taskName + "_reference1.txt", "UTF-8");
				for (int j = 1; j <= stories.get(i-1).summary.size(); j++) {
					writer.println(stories.get(i-1).summary.get(j-1));
				}
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		*/
		
		// Create system files for Rouge.
		/*
		for (int i = 1; i <= stories.size(); i++) {
			String taskName = "news" + i;
			ArrayList<Double> dist = distributions.get(i-1);
			// Find the highest 3.
			int highest1 = 0, highest2 = 1, highest3 = 2;
			for (int k = 3; k < dist.size(); k++) {
				if (dist.get(k) > dist.get(highest3)) {
					highest3 = k;
				}
				
				if (dist.get(highest3) > dist.get(highest2)) {
					int temp = highest3;
					highest3 = highest2;
					highest2 = temp;
				}
				
				if (dist.get(highest2) > dist.get(highest1)) {
					int temp = highest2;
					highest2 = highest1;
					highest1 = temp;
				}
			}
			try {
				PrintWriter writer = new PrintWriter(taskName + "_system1" + ".txt", "UTF-8");
				writer.println(stories.get(i-1).sentences.get(highest1));
				writer.println(stories.get(i-1).sentences.get(highest2));
				writer.println(stories.get(i-1).sentences.get(highest3));
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
		*/
		// Calculate average precision, recall and f score values.
		/*
		Scanner in = new Scanner(new File("Dataset/results.txt"));
		System.out.println(in.nextLine());
		double Avg_Recall1 = 0, Avg_Precision1=0, Avg_FScore1=0;
		double Avg_Recall2 = 0, Avg_Precision2=0, Avg_FScore2=0;
		double Avg_RecallL = 0, Avg_PrecisionL=0, Avg_FScoreL=0;
		while (in.hasNextLine()) {
			String line = in.nextLine();
			in.nextLine();
			String[] parts = line.split(",");
			if (parts[0].equals("ROUGE-L+StopWordRemoval")) {
				Avg_RecallL += Double.parseDouble(parts[3]);
				Avg_PrecisionL += Double.parseDouble(parts[4]);
				Avg_FScoreL += Double.parseDouble(parts[5]);
			}
			if (parts[0].equals("ROUGE-1+StopWordRemoval")) {
				Avg_Recall1 += Double.parseDouble(parts[3]);
				Avg_Precision1 += Double.parseDouble(parts[4]);
				Avg_FScore1 += Double.parseDouble(parts[5]);
			}
			if (parts[0].equals("ROUGE-2+StopWordRemoval")) {
				Avg_Recall2 += Double.parseDouble(parts[3]);
				Avg_Precision2 += Double.parseDouble(parts[4]);
				Avg_FScore2 += Double.parseDouble(parts[5]);
			}
		}
		System.out.println(Avg_Recall1/1000 + ", " + Avg_Precision1/1000 + ", " + Avg_FScore1/1000);
		System.out.println(Avg_Recall2/1000 + ", " + Avg_Precision2/1000 + ", " + Avg_FScore2/1000);
		System.out.println(Avg_RecallL/1000 + ", " + Avg_PrecisionL/1000 + ", " + Avg_FScoreL/1000);
		*/
	}
	
	/**
	 * Applies the power method to all documents.
	 */
	private static ArrayList<ArrayList<Double>> applyPowerMethod(ArrayList<ArrayList<ArrayList<Double>>> transitionMatrix) {
		ArrayList<ArrayList<Double>> distributions = new ArrayList<>();
		for (int docId = 0; docId < transitionMatrix.size(); docId++) {
			ArrayList<Double> initialDistribution = new ArrayList<>();
			initialDistribution.add(1.0);
			for (int i = 1; i < transitionMatrix.get(docId).size(); i++) {
				initialDistribution.add(0.0);
			}
			distributions.add(powerMethod(initialDistribution, transitionMatrix.get(docId)));
		}
		return distributions;
	}

	/**
	 * Recursively calls itself until the distribution is stabilized.
	 */
	private static ArrayList<Double> powerMethod(ArrayList<Double> distribution,
			ArrayList<ArrayList<Double>> transition) {
		// Iterate
		ArrayList<Double> newDistribution = multiply(distribution, transition);
		// Check if it is stabilized.
		double distance = 0;
		for (int i = 0; i < distribution.size(); i++) {
			distance += (distribution.get(i) - newDistribution.get(i))*(distribution.get(i) - newDistribution.get(i));
		}
		distance = Math.sqrt(distance);
		// Iterate if not stabilized.
		if (distance > 0.00001) {
			return powerMethod(newDistribution, transition);
		} else {
			return newDistribution;
		}
	}

	/**
	 * Standard matrix multiplication.
	 */
	private static ArrayList<Double> multiply(ArrayList<Double> distribution,
			ArrayList<ArrayList<Double>> transition) {
		ArrayList<Double> newDistribution = new ArrayList<>();
		for (int i = 0; i < transition.size(); i++) {
			double value = 0;
			for (int j = 0; j < distribution.size(); j++) {
				value += distribution.get(j) * transition.get(j).get(i);
			}
			newDistribution.add(value);
		}
		return newDistribution;
	}

	/**
	 * Takes in the adjacency matrix and teleportation rate.
	 * Builds the transition matrix.
	 */
	private static ArrayList<ArrayList<ArrayList<Double>>> buildTransitionMatrix(
			ArrayList<ArrayList<ArrayList<Integer>>> adjacencyMatrix, double teleportationRate) {
		ArrayList<ArrayList<ArrayList<Double>>> transitionMatrix = new ArrayList<>();
		for (int docId = 0; docId < adjacencyMatrix.size(); docId++) {
			ArrayList<ArrayList<Double>> transitionMatrixForDocument = new ArrayList<>();
			for (int lineId = 0; lineId < adjacencyMatrix.get(docId).size(); lineId++) {
				// Start with distributing the teleportation rate.
				ArrayList<Double> transitionLine = new ArrayList<>(
						Collections.nCopies(adjacencyMatrix.get(docId).get(lineId).size(), 
								teleportationRate/adjacencyMatrix.get(docId).get(lineId).size()));
				// Count the edges.
				int edgeCount = 0;
				for (int edge : adjacencyMatrix.get(docId).get(lineId)) {
					if (edge == 1) {
						edgeCount++;
					}
				}
				// Distribute remaining possibilities to the edges.
				for (int i = 0; i < transitionLine.size(); i++) {
					if (adjacencyMatrix.get(docId).get(lineId).get(i) == 1) {
						transitionLine.set(i, transitionLine.get(i) + (1-teleportationRate)/edgeCount);
					}
				}
				// Add the line to the document.
				transitionMatrixForDocument.add(transitionLine);
			}
			// Add the document to the matrix.
			transitionMatrix.add(transitionMatrixForDocument);
		}
		return transitionMatrix;
	}

	/**
	 * Adds an edge between sentences if they have a similarity more than 0.10.
	 */
	private static ArrayList<ArrayList<ArrayList<Integer>>> buildAdjacencyMatrix(ArrayList<ArrayList<ArrayList<Double>>> similarities) {
		ArrayList<ArrayList<ArrayList<Integer>>> adjacencyMatrix = new ArrayList<>();
		for (int docId = 0; docId < similarities.size(); docId++) {
			ArrayList<ArrayList<Integer>> adjacencyForDocument = new ArrayList<>();
			for (int sentence1 = 0; sentence1 < similarities.get(docId).size(); sentence1++) {
				ArrayList<Integer> adjacencyForSentence = new ArrayList<>();
				for (int sentence2 = 0; sentence2 < similarities.get(docId).get(sentence1).size(); sentence2++) {
					if (similarities.get(docId).get(sentence1).get(sentence2) > 0.10) {
						adjacencyForSentence.add(1);
					} else {
						adjacencyForSentence.add(0);	
					}
				}
				adjacencyForDocument.add(adjacencyForSentence);
			}
			adjacencyMatrix.add(adjacencyForDocument);
		}
		return adjacencyMatrix;
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
			story.summary.add(in.nextLine());
		}
		in.close();
		return story;
	}
}
