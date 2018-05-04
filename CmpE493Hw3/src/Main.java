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
		HashMap<String, Double> idf = calculateIdf(stories);
	}
	
	/**
	 * Calculates idf scores for each word in story set.
	 */
	private static HashMap<String, Double> calculateIdf(ArrayList<Story> stories) {
		// < Word : # of documents containing the word >.
		HashMap<String, Integer> df = new HashMap<>();
		for (Story story : stories) {
			// Word set for this story.
			HashSet<String> wordsOfStory = new HashSet<>();	
			for (String line : story.sentences) {
				for (String word : normalize(line).split(" ")) {
					// Remove single char words
					if (word.length() < 2) {
						continue;
					} else {
						wordsOfStory.add(word);
					}
				}
			}
			// Update this story's words' df.
			for (String word : wordsOfStory) {
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
		return idf;
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
