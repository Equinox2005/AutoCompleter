package cs1501_p2;
import java.util.*;
import java.util.ArrayList;

public class UserHistory implements Dict {
    private int count;
    private String searchString = "";
    private HashMap<String, Integer> wordCounts;
    private ArrayList<String> words = new ArrayList<>();
    private DLBNode root;

    public UserHistory() {
        this.root = null;
        this.wordCounts = new HashMap<>();
        this.searchString = "";
    }

    /**
     * Add a new word to the dictionary
     *
     * @param key New word to be added to the dictionary
     */
    public void add(String key) {
        if (key == null) {
            return;
        }
        char currLet = key.charAt(0);
        if (root == null) {
            root = new DLBNode(key.charAt(0));
        }
        DLBNode curr = root;
        int counter = 0;

        while (counter < key.length()) {
            currLet = key.charAt(counter);
            while (curr != null && curr.getLet() != currLet) {
                if (curr.getRight() == null) {
                    DLBNode currLetNode = new DLBNode(currLet);
                    curr.setRight(currLetNode);
                }
                curr = curr.getRight();
            }
            counter++;
            if (counter == key.length()) {
                break;
            }
            currLet = key.charAt(counter);
            if (curr.getDown() == null) {
                DLBNode currLetNode = new DLBNode(currLet);
                curr.setDown(currLetNode);
            }
            curr = curr.getDown();

        }
        DLBNode end = new DLBNode('^');

        if (curr.getDown() == null) {
            curr.setDown(end);
        }
        else {
            curr = curr.getDown();
            while (curr.getRight() != null) {
                curr = curr.getRight();
            }
            curr.setRight(end);
        }
        
        if (this.wordCounts.containsKey(key)) {
            this.wordCounts.put(key, this.wordCounts.get(key) + 1);
        }
        else {
            this.wordCounts.put(key, 1);
            this.count += 1;
            this.words.add(key);
        }
        

    }

    /**
     * Check if the dictionary contains a word
     *
     * @param key Word to search the dictionary for
     *
     * @return true if key is in the dictionary, false otherwise
     */
    public boolean contains(String key) {
        if (root == null || key == null || key.length() == 0) {
            return false;
        }
        char currLet = key.charAt(0);
        DLBNode curr = root;
        int counter = 0;

        while (counter < key.length()) {
            currLet = key.charAt(counter);
            while (curr != null && curr.getLet() != currLet) {
                if (curr.getRight() == null) {
                    return false;
                }
                curr = curr.getRight();
            }
            counter++;
            if (counter == key.length()) {
                break;
            }
            currLet = key.charAt(counter);
            if (curr.getDown() == null) {
                return false;
            }
            curr = curr.getDown();
        }
        curr = curr.getDown();
        while (curr != null) {
            if (curr.getLet() == '^') {
                return true;
            }
            curr = curr.getRight();
        }
        return false;
    }


    /**
     * Check if a String is a valid prefix to a word in the dictionary
     *
     * @param pre Prefix to search the dictionary for
     *
     * @return true if prefix is valid, false otherwise
     */
    public boolean containsPrefix(String pre) {
        if (root == null || pre == null || pre.length() == 0) {
            return false;
        }
        char currLet = pre.charAt(0);
        DLBNode curr = root;
        int counter = 0;
        while (counter < pre.length()) {
            currLet = pre.charAt(counter);
            while (curr != null && curr.getLet() != currLet) {
                if (curr.getRight() == null) {
                    return false;
                }
                curr = curr.getRight();
            }
            counter++;
            if (counter == pre.length()) {
                break;
            }
            if (curr.getDown() == null) {
                return false;
            }
            curr = curr.getDown();
        }
        return true;
    }

    /**
     * Search for a word one character at a time
     *
     * @param next Next character to search for
     *
     * @return int value indicating result for current by-character search:
     *         -1: not a valid word or prefix
     *         0: valid prefix, but not a valid word
     *         1: valid word, but not a valid prefix to any other words
     *         2: both valid word and a valid prefix to other words
     */
    public int searchByChar(char next) {
        if (root == null) {
            return -1;
        }
        this.searchString += next;
        if (!this.containsPrefix(this.searchString) && !this.contains(this.searchString)) {
            return -1;
        }
        else if (this.containsPrefix(this.searchString) && !this.contains(this.searchString)) {
            return 0;
        }
        // dealing with cases where return value is 1 or 2
        else {
            DLBNode curr = root;
            int counter = 0;
            while (counter < this.searchString.length()) {
                char key = this.searchString.charAt(counter);
                while (curr.getLet() != key) {
                    curr = curr.getRight();
                }
                counter++;
                if (counter == this.searchString.length()) {
                    break;
                }
                curr = curr.getDown();
            }
            curr = curr.getDown();
            // it isnt a prefix to other words
            if (curr.getRight() == null) {
                return 1;
            }
            // it is a prefix to atleast one more word
            else {
                return 2;
            }
            }
        }

    /**
     * Reset the state of the current by-character search
     */
    public void resetByChar() {
        this.searchString = "";

    }

    /**
     * Suggest up to 5 words from the dictionary based on the current
     * by-character search. Ordering should depend on the implementation.
     * 
     * @return ArrayList<String> List of up to 5 words that are prefixed by
     *         the current by-character search
     */
    public ArrayList<String> suggest() {
        ArrayList<String> suggestions = new ArrayList<>();
        if (searchString == null || searchString.isEmpty()) {
            return suggestions;
        }
    
        ArrayList<String> allWords = traverse();
    
        int index = binarySearchPrefix(allWords, searchString);
        if (index < 0) {
            return suggestions;
        }
    
        ArrayList<String> prefixWords = new ArrayList<>();
        for (int i = index; i < allWords.size(); i++) {
            String word = allWords.get(i);
            if (word.startsWith(searchString)) {
                prefixWords.add(word);
            } else {
                break; 
            }
        }
        
        // sort according to the frequency of words (descending)
        prefixWords.sort((s1, s2) -> this.wordCounts.get(s2).compareTo(this.wordCounts.get(s1)));
    
        // add up to 5 words to the suggestions list
        for (int i = 0; i < prefixWords.size() && i < 5; i++) {
            suggestions.add(prefixWords.get(i));
        }
    
        return suggestions;
    }
    
    private int binarySearchPrefix(ArrayList<String> words, String prefix) {
        int low = 0;
        int high = words.size() - 1;
        int result = -1;
        while (low <= high) {
            int mid = (low + high) / 2;
            String word = words.get(mid);
            if (word.startsWith(prefix)) {
                // found the word with the prefix, but continue searching to find the first occurrence
                result = mid;
                high = mid - 1;
            } else if (word.compareTo(prefix) < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return result;
    }

    /**
     * List all of the words currently stored in the dictionary
     * 
     * @return ArrayList<String> List of all valid words in the dictionary
     */
    public ArrayList<String> traverse() {
        ArrayList<String> holder = new ArrayList<>(this.words);
        holder.sort(null);
        return holder;
    }

    /**
     * Count the number of words in the dictionary
     *
     * @return int, the number of (distinct) words in the dictionary
     */
    public int count() {
        return this.count;
    }
    public HashMap<String,Integer> wordCounts() {
        return this.wordCounts;
    }

    
}