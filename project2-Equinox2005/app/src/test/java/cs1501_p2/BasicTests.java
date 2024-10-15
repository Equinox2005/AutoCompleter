package cs1501_p2;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import static java.time.Duration.ofSeconds;

class BasicTests {
    final int DEFAULT_TIMEOUT = 10;

     @Test
    @DisplayName("Testing DLB")
    void basic_dlb_test() {
        assertTimeoutPreemptively(ofSeconds(DEFAULT_TIMEOUT), () -> {
            String dict_fname = "build/resources/test/dictionary.txt";

            DLB dlb = new DLB();
            assertEquals(0, dlb.count(), "Should be empty");

            try (Scanner s = new Scanner(new File(dict_fname))) {
                while (s.hasNext()) {
                    dlb.add(s.nextLine());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            assertEquals(7, dlb.count(), "Incorrect number of keys");

            String[] checks = new String[] { "dict", "definite", "A" };
            for (String c : checks) {
                assertTrue(dlb.contains(c), "DLB should contain " + c);
            }

            checks = new String[] { "not", "there" };
            for (String c : checks) {
                assertTrue(!dlb.contains(c), "DLB should not contain " + c);
            }

            checks = new String[] { "i", "dict" };
            for (String c : checks) {
                assertTrue(dlb.containsPrefix(c), c + " should be a valid prefix");
            }

            assertEquals(-1, dlb.searchByChar('q'), "q should not be a prefix or key");
            dlb.resetByChar();
            assertEquals(0, dlb.searchByChar('d'), "d should be a valid prefix");
            assertEquals(0, dlb.searchByChar('i'), "di should be a valid prefix");
            assertEquals(0, dlb.searchByChar('c'), "dic should be a valid prefix");
            assertEquals(2, dlb.searchByChar('t'), "dict should be a valid prefix and key");
            dlb.resetByChar();
            assertEquals(0, dlb.searchByChar('i'), "i should be a valid prefix");
            assertTrue(dlb.contains("this"), "Should be able to still run contains");
            assertEquals(1, dlb.searchByChar('s'), "is should be a valid key, even if interrupted by contains");

            dlb.resetByChar();
            dlb.searchByChar('d');
            ArrayList<String> sugs = dlb.suggest();
            String[] expected = new String[] { "definite", "dict", "dictionary" };
            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i], sugs.get(i), "Expected suggestion " + expected[i] + " got " + sugs.get(i));
            }

            ArrayList<String> trav = dlb.traverse();
            expected = new String[] { "A", "a", "definite", "dict", "dictionary", "is", "this" };
            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i], trav.get(i),
                        "Expected traversal item " + expected[i] + " got " + trav.get(i));
            }
        });
    }

    @Test
    @DisplayName("Testing UserHistory")
    void basic_uh_test() {
        assertTimeoutPreemptively(ofSeconds(DEFAULT_TIMEOUT), () -> {
            UserHistory uh = new UserHistory();
            assertEquals(0, uh.count(), "Should be empty");

            uh.add("user");
            uh.add("user");
            uh.add("user");
            uh.add("userland");
            uh.add("userland");
            uh.add("up");
            uh.add("up");
            uh.add("up");
            uh.add("up");
            uh.add("up");
            uh.add("unity");
            uh.add("unity");
            uh.add("usermode");
            uh.add("usermode");
            uh.add("ui");
            uh.add("ux");

            assertEquals(7, uh.count(), "Should have 7 distinct words");

            uh.searchByChar('u');
            ArrayList<String> sugs = uh.suggest();
            assertEquals("up", sugs.get(0), "First suggestion should be up");
            assertEquals("user", sugs.get(1), "Second suggestion should be user");
            String[] others = new String[] { "unity", "userland", "usermode" };
            for (String o : others) {
                assertTrue(sugs.contains(o), "Should suggest " + o);
            }
        });
    }

    @Test
    @DisplayName("Testing AutoCompleter")
    void basic_ac_test() {
        assertTimeoutPreemptively(ofSeconds(DEFAULT_TIMEOUT), () -> {
            String dict_fname = "build/resources/test/dictionary.txt";
            String uhist_state_fname = "build/resources/test/uhist_state.p2";

            AutoCompleter ac = new AutoCompleter(dict_fname);

            ArrayList<String> sugs = ac.nextChar('d');
            String[] expected = new String[] { "definite", "dict", "dictionary" };
            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i], sugs.get(i),
                        "(Initial) Expected suggestion " + expected[i] + " got " + sugs.get(i));
            }

            ac.finishWord("dictionary");
            sugs = ac.nextChar('d');
            expected = new String[] { "dictionary", "definite", "dict" };
            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i], sugs.get(i),
                        "(finish dictionary) Expected suggestion " + expected[i] + " got " + sugs.get(i));
            }

            ac.finishWord("dip");
            ac.finishWord("dip");
            sugs = ac.nextChar('d');
            expected = new String[] { "dip", "dictionary", "definite", "dict" };
            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i], sugs.get(i),
                        "(finish dip x2) Expected suggestion " + expected[i] + " got " + sugs.get(i));
            }

            ac.saveUserHistory(uhist_state_fname);

            ac = new AutoCompleter(dict_fname, uhist_state_fname);
            sugs = ac.nextChar('d');
            expected = new String[] { "dip", "dictionary", "definite", "dict" };
            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i], sugs.get(i),
                        "(reloaded state) Expected suggestion " + expected[i] + " got " + sugs.get(i));
            }
        });
    }

    @Test
    @DisplayName("Testing DLB Contains Prefix")
    void test_grade_dlb_contains_pre_test() {
        DLB dlb = new DLB();
        String[] words = { "apple", "app", "banana", "band", "bandage", "cat" };

        for (String word : words) {
            dlb.add(word);
        }

        assertTrue(dlb.containsPrefix("app"), "'app' should be a valid prefix");
        assertTrue(dlb.containsPrefix("ban"), "'ban' should be a valid prefix");
        assertFalse(dlb.containsPrefix("dog"), "'dog' should not be a valid prefix");
    }

    @Test
    @DisplayName("Testing DLB Contains")
    void test_grade_dlb_contains_test() {
        DLB dlb = new DLB();
        String[] words = { "apple", "app", "banana", "band", "bandage", "cat" };

        for (String word : words) {
            dlb.add(word);
        }

        assertTrue(dlb.contains("apple"), "'apple' should be in DLB");
        assertTrue(dlb.contains("band"), "'band' should be in DLB");
        assertFalse(dlb.contains("dog"), "'dog' should not be in DLB");
    }

    @Test
    @DisplayName("Testing DLB Count")
    void test_grade_dlb_count_test() {
        DLB dlb = new DLB();
        assertEquals(0, dlb.count(), "Initial count should be 0");

        String[] words = { "apple", "app", "banana", "band", "bandage", "cat" };
        for (String word : words) {
            dlb.add(word);
        }

        assertEquals(6, dlb.count(), "DLB should contain 6 words");
    }

    @Test
    @DisplayName("Testing DLB Suggest")
    void test_grade_dlb_suggest_test() {
        DLB dlb = new DLB();
        String[] words = { "apple", "app", "applet", "banana", "band", "bandage", "cat" };

        for (String word : words) {
            dlb.add(word);
        }

        dlb.searchByChar('a');
        ArrayList<String> suggestions = dlb.suggest();
        String[] expected = { "app", "apple", "applet" };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], suggestions.get(i), "Expected suggestion " + expected[i]);
        }
    }

    @Test
    @DisplayName("Testing DLB Traverse")
    void test_grade_dlb_traverse_test() {
        DLB dlb = new DLB();
        String[] words = { "apple", "app", "banana", "band", "bandage", "cat" };

        for (String word : words) {
            dlb.add(word);
        }

        ArrayList<String> traversal = dlb.traverse();
        String[] expected = { "app", "apple", "banana", "band", "bandage", "cat" };

        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], traversal.get(i), "Expected traversal word " + expected[i]);
        }
    }

    @Test
    @DisplayName("Testing AutoCompleter Finish")
    void test_grade_ac_finish() {
        String dict_fname = "build/resources/test/Dictionary.txt"; // Update this path
        AutoCompleter ac = new AutoCompleter(dict_fname);
        ac.finishWord("apple");
        ac.finishWord("applet");

        ArrayList<String> suggestions = ac.nextChar('a');
        String[] expected = { "apple", "applet" };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], suggestions.get(i), "Expected suggestion " + expected[i]);
        }
    }

    @Test
    @DisplayName("Testing UserHistory Contains Prefix")
    void test_grade_uh_contains_pre_test() {
        UserHistory uh = new UserHistory();
        String[] words = { "user", "unity", "universe", "unique", "utopia" };

        for (String word : words) {
            uh.add(word);
        }

        assertTrue(uh.containsPrefix("uni"), "'uni' should be a valid prefix");
        assertFalse(uh.containsPrefix("cat"), "'cat' should not be a valid prefix");
    }

    @Test
    @DisplayName("Testing UserHistory Contains")
    void test_grade_uh_contains_test() {
        UserHistory uh = new UserHistory();
        String[] words = { "user", "unity", "universe", "unique", "utopia" };

        for (String word : words) {
            uh.add(word);
        }

        assertTrue(uh.contains("unity"), "'unity' should be in UserHistory");
        assertFalse(uh.contains("cat"), "'cat' should not be in UserHistory");
    }

    @Test
    @DisplayName("Testing UserHistory Count")
    void test_grade_uh_count_test() {
        UserHistory uh = new UserHistory();
        assertEquals(0, uh.count(), "Initial count should be 0");

        String[] words = { "user", "unity", "universe", "unique", "utopia" };
        for (String word : words) {
            uh.add(word);
        }

        assertEquals(5, uh.count(), "UserHistory should contain 5 words");
    }

    @Test
    @DisplayName("Testing UserHistory Suggest")
    void test_grade_uh_suggest_test() {
        UserHistory uh = new UserHistory();
        String[] words = { "user", "unity", "universe", "unique", "utopia" };

        for (String word : words) {
            uh.add(word);
        }

        uh.searchByChar('u');
        ArrayList<String> suggestions = uh.suggest();
        String[] expected = { "unity", "unique", "utopia" };
        for (int i = 0; i < expected.length; i++) {
            assertTrue(suggestions.contains(expected[i]), "Expected suggestion: " + expected[i]);
        }
    }
}