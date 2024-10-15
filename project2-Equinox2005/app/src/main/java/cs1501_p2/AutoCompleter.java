package cs1501_p2;

import java.util.ArrayList;
import java.io.*;

public class AutoCompleter implements AutoComplete_Inter {

    private DLB dictionary;
    private UserHistory userHistory;
    private String currentSearch;


    public AutoCompleter(String dictFileName) {
        this.dictionary = new DLB();
        this.userHistory = new UserHistory();
        this.currentSearch = "";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(dictFileName));
            String word;
            while ((word = reader.readLine()) != null) {
                word = word.trim();
                if (!word.isEmpty()) {
                    this.dictionary.add(word);
            }
            }
            reader.close();
        } catch (IOException e) {
            this.dictionary = new DLB();
        }
    }

    public AutoCompleter(String dictFileName, String userHistoryFileName) {
        this.dictionary = new DLB();
        this.userHistory = new UserHistory();
        this.currentSearch = "";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(dictFileName));
            String word;
            while ((word = reader.readLine()) != null) {
                word = word.trim();
                if (!word.isEmpty()) {
                    this.dictionary.add(word);
                }
            }
            reader.close();
        } catch (IOException e) {
            this.dictionary = new DLB();
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(userHistoryFileName));
            String line;
            while ((line = reader.readLine()) != null) {
                // each line is expected to be in the format: word count
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 2) {
                    String userWord = parts[0];
                    int count = Integer.parseInt(parts[1]);
                    // Add the word to UserHistory 'count' times to represent its frequency
                    for (int i = 0; i < count; i++) {
                        userHistory.add(userWord);
                    }
                } else if (parts.length == 1) {
                    // If count is not available, add the word once
                    String userWord = parts[0];
                    userHistory.add(userWord);
                }
            }
            reader.close();
        } catch (IOException e) {
            this.userHistory = new UserHistory();
        }
    }

    /**
     * Produce up to 5 suggestions based on the current word the user has entered.
     * Suggestions are pulled first from the user history (ordered by frequency),
     * then from the dictionary (ordered lexicographically).
     *
     * @param next The next character the user just entered.
     * @return ArrayList<String> List of up to 5 words prefixed by currentSearch.
     */
    public ArrayList<String> nextChar(char next) {
        this.currentSearch += next;

        // update searches in UserHistory and DLB
        userHistory.searchByChar(next);
        dictionary.searchByChar(next);

        ArrayList<String> uhSuggestions = userHistory.suggest();
        ArrayList<String> dictSuggestions = dictionary.suggest();

        // combine suggestions, ensuring no duplicates and preserving order
        ArrayList<String> combinedSuggestions = new ArrayList<String>();

        // Add UserHistory suggestions first
        for (String s : uhSuggestions) {
            if (!combinedSuggestions.contains(s)) {
                combinedSuggestions.add(s);
            }
            if (combinedSuggestions.size() >= 5) {
                break;
            }
        }

        // add dictionary suggestions
        if (combinedSuggestions.size() < 5) {
            for (String s : dictSuggestions) {
                if (!combinedSuggestions.contains(s)) {
                    combinedSuggestions.add(s);
                }
                if (combinedSuggestions.size() >= 5) {
                    break;
                }
            }
        }

        return combinedSuggestions;
    }

    /**
     * Process the user having selected the current word.
     * Resets the state of any searches and updates the UserHistory.
     *
     * @param cur String representing the word the user has selected.
     */
    public void finishWord(String cur) {
        // Reset the state of any searches
        this.currentSearch = "";
        userHistory.resetByChar();
        dictionary.resetByChar();

        // Update UserHistory with the selected word
        userHistory.add(cur);
    }

    /**
     * Save the state of the UserHistory to a human-readable text file.
     *
     * @param fname String filename to write the history state to.
     */
    public void saveUserHistory(String fname) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fname));

            Object wordCountsObj = userHistory.wordCounts();


            if (wordCountsObj != null) {
                // convert the object to a string and then parse it
                String wordCountsStr = wordCountsObj.toString();
                // example format: {word1=3, word2=5, word3=2}
                // we need to parse this string to get words and counts
                wordCountsStr = wordCountsStr.substring(1, wordCountsStr.length() - 1); // removing curly braces
                String[] entries = wordCountsStr.split(", ");
                for (String entry : entries) {
                    String[] keyValue = entry.split("=");
                    if (keyValue.length == 2) {
                        String word = keyValue[0];
                        String countStr = keyValue[1];
                        int count = Integer.parseInt(countStr);
                        writer.write(word + " " + count);
                        writer.newLine();
                    }
                }
            }

            writer.close();
        } catch (IOException e) {
        }
    }
}
