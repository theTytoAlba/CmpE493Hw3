import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		System.out.println("Reading stories...");
		ArrayList<Story> stories = readStories("Dataset");
		System.out.println("Reading stories DONE.");
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
