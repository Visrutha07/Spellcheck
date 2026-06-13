import java.util.*;

/**
 * Spell Checker using a Trie (prefix tree).
 *
 * Features:
 *  - Fast O(L) word lookup, where L = length of the word
 *  - Suggestions for misspelled words based on edit distance
 *    (insertions, deletions, substitutions) via DFS over the trie
 */
public class Main {

    /** A single node in the trie. */
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isWord = false;
    }

    /** Simple suggestion holder: a word and its edit distance from the query. */
    public static class Suggestion {
        public final String word;
        public final int distance;

        Suggestion(String word, int distance) {
            this.word = word;
            this.distance = distance;
        }

        @Override
        public String toString() {
            return word + " (dist=" + distance + ")";
        }
    }

    private final TrieNode root = new TrieNode();

    public Main() {}

    public Main(Collection<String> words) {
        for (String w : words) {
            addWord(w);
        }
    }

    /** Add a word to the dictionary. */
    public void addWord(String word) {
        TrieNode node = root;
        for (char ch : word.toLowerCase().toCharArray()) {
            node = node.children.computeIfAbsent(ch, c -> new TrieNode());
        }
        node.isWord = true;
    }

    /** Returns true if the word exists exactly in the dictionary. */
    public boolean isCorrect(String word) {
        TrieNode node = findNode(word.toLowerCase());
        return node != null && node.isWord;
    }

    /** Returns true if any word in the dictionary starts with the given prefix. */
    public boolean startsWith(String prefix) {
        return findNode(prefix.toLowerCase()) != null;
    }

    private TrieNode findNode(String prefix) {
        TrieNode node = root;
        for (char ch : prefix.toCharArray()) {
            node = node.children.get(ch);
            if (node == null) {
                return null;
            }
        }
        return node;
    }

    /**
     * Return up to maxResults suggestions for `word`, each within
     * maxDistance edits, sorted by edit distance then alphabetically.
     */
    public List<Suggestion> suggest(String word, int maxDistance, int maxResults) {
        String target = word.toLowerCase();
        List<Suggestion> results = new ArrayList<>();
        dfs(root, "", target, maxDistance, results);

        results.sort((a, b) -> {
            if (a.distance != b.distance) {
                return Integer.compare(a.distance, b.distance);
            }
            return a.word.compareTo(b.word);
        });

        if (results.size() > maxResults) {
            return new ArrayList<>(results.subList(0, maxResults));
        }
        return results;
    }

    private void dfs(TrieNode node, String current, String target, int maxDistance, List<Suggestion> results) {
        // Prune branches that have grown far too long to ever match within maxDistance.
        if (current.length() > target.length() + maxDistance) {
            return;
        }

        if (node.isWord) {
            int dist = editDistance(current, target);
            if (dist <= maxDistance) {
                results.add(new Suggestion(current, dist));
            }
        }

        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            dfs(entry.getValue(), current + entry.getKey(), target, maxDistance, results);
        }
    }

    /** Classic Levenshtein distance via dynamic programming. */
    private static int editDistance(String a, String b) {
        int m = a.length();
        int n = b.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(
                            dp[i - 1][j],       // deletion
                            Math.min(
                                    dp[i][j - 1],     // insertion
                                    dp[i - 1][j - 1]  // substitution
                            )
                    );
                }
            }
        }
        return dp[m][n];
    }

    public static void main(String[] args) {
        // Built-in dictionary (words covering A-Z)
        List<String> dictionary = Arrays.asList(
                "apple", "ant", "arrow", "animal", "area", "answer", "apply", "ago",
                "banana", "bear", "book", "bottle", "brave", "bridge", "bring", "build",
                "cat", "car", "cake", "camera", "candle", "castle", "chair", "climate",
                "dog", "door", "dance", "dream", "desk", "drive", "doctor", "daily",
                "elephant", "egg", "earth", "energy", "engine", "enjoy", "evening", "exam",
                "fish", "fire", "family", "friend", "flower", "forest", "future", "follow",
                "goat", "garden", "glass", "grape", "ground", "guitar", "government", "grow",
                "horse", "house", "happy", "health", "history", "hotel", "human", "hope",
                "ice", "idea", "image", "insect", "island", "internet", "invite", "important",
                "jacket", "juice", "jungle", "journey", "joke", "joy", "judge", "jump",
                "kite", "king", "kitchen", "knowledge", "kind", "kitten", "key", "knife",
                "lion", "lamp", "lake", "language", "leader", "letter", "library", "light",
                "monkey", "mountain", "music", "market", "machine", "memory", "message", "moon",
                "nest", "night", "nature", "network", "news", "number", "nurse", "north",
                "ocean", "orange", "office", "opinion", "open", "order", "owner", "outside",
                "panda", "pencil", "picture", "planet", "police", "power", "problem", "purple",
                "queen", "question", "quiet", "quick", "quality", "quarter", "quote", "quiz",
                "rabbit", "river", "rain", "reason", "record", "result", "road", "robot",
                "snake", "sun", "school", "science", "season", "signal", "society", "sound",
                "tiger", "table", "teacher", "temple", "ticket", "time", "travel", "tree",
                "umbrella", "uncle", "under", "union", "unique", "universe", "until", "update",
                "violin", "village", "valley", "value", "vehicle", "video", "visit", "voice",
                "wolf", "water", "weather", "window", "winter", "wonder", "world", "write",
                "xylophone", "xray", "xenon",
                "yellow", "yard", "year", "yesterday", "yoga", "young", "youth",
                "zebra", "zero", "zone", "zoo", "zigzag"
        );

        Main checker = new Main(dictionary);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a word to check:");
        String w = scanner.nextLine().trim();

        if (checker.isCorrect(w)) {
            System.out.println("'" + w + "' is spelled correctly.");
        } else {
            List<Suggestion> suggestions = checker.suggest(w, 2, 3);
            if (!suggestions.isEmpty()) {
                StringJoiner joiner = new StringJoiner(", ");
                for (Suggestion s : suggestions) {
                    joiner.add(s.toString());
                }
                System.out.println("'" + w + "' is misspelled. Did you mean: " + joiner + "?");
            } else {
                System.out.println("'" + w + "' is misspelled. No suggestions found.");
            }
        }

        scanner.close();
    }
}
