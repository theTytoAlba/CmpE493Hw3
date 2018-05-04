import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {
	public static void main(String[] args) {
		System.out.println("Reading stories...");
		ArrayList<Story> stories = readStories("Dataset");
		System.out.println("Reading stories DONE.");
		System.out.println("Calculating idf scores...");
		ArrayList<HashMap<String,Double>> tfIdfScores = calculateTfIdf(stories);
		System.out.println("Calculating idf scores DONE.");
	}
	
	/**
	 * Calculates tf-idf scores for each word in story set.
	 */
	private static ArrayList<HashMap<String, Double>> calculateTfIdf(ArrayList<Story> stories) {
		// < Word : # of documents containing the word >.
		HashMap<String, Integer> df = new HashMap<>();
		// Array of < Word : # of times term occurs in document >
		ArrayList<HashMap<String, Integer>> tfScores = new ArrayList<>();
		for (Story story : stories) {
			// < Word : # of times term occurs in document >
			HashMap<String, Integer> tf = new HashMap<>();	
			for (String line : story.sentences) {
				for (String word : normalize(line).split(" ")) {
					// Remove single char words
					if (word.length() < 2) {
						continue;
					}
					if (tf.containsKey(word)) {
						tf.put(word, df.get(word) + 1);
					} else {
						tf.put(word, 1);	
					}
				}
			}
			// Add this document's term frequencies.
			tfScores.add(tf);
			// Update this story's words' df.
			for (String word : tf.keySet()) {
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
		// Calculate tf-idf for all docs.
		ArrayList<HashMap<String, Double>> tfIdf = new ArrayList<>();
		for (int docId = 0; docId < tfScores.size(); docId++) {
			HashMap<String, Double> tfIdfForDoc = new HashMap<>();
			for (String word : tfScores.get(docId).keySet()) {
				double tfScore = tfScores.get(docId).get(word) == 0 ? 0 : 1 + Math.log10(tfScores.get(docId).get(word));
				tfIdfForDoc.put(word, tfScore * idf.get(word));
			}
			tfIdf.add(tfIdfForDoc);
		}
		return tfIdf;
	}

	/**
	 * Gets rid of punctuation.
	 */
	private static String normalize(String text) {
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
