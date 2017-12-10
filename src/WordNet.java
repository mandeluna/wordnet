import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.RedBlackBST;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Topological;

import java.util.LinkedList;
import java.util.List;

public class WordNet {
    /*
     *
     * All methods and the constructor should throw a java.lang.IllegalArgumentException
     * if any argument is null. The constructor should throw a java.lang.IllegalArgumentException
     * if the input does not correspond to a rooted DAG.
     *
     * The distance() and sap() methods should throw a java.lang.IllegalArgumentException
     * unless both of the noun arguments are WordNet nouns.
     *
     */

    private final RedBlackBST<String, List<Synonym>> symbolTable = new RedBlackBST<>();
    private final SAP sap;
    private final Synonym[] synonyms;

    private class Synonym {
        int id;
        String[] synset;

        Synonym(int id, String[] synset) {
            this.id = id;
            this.synset = synset;
        }
    }

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        if ((synsets == null) || (hypernyms == null)) {
            throw new IllegalArgumentException();
        }

        In synsetsIn = new In(synsets);
        int maxId = -1;
        while (!synsetsIn.isEmpty()) {
            String line = synsetsIn.readLine();
            String[] values = line.split(",");
            String[] words = values[1].split(" ");
            maxId = Integer.parseInt(values[0]);
            Synonym syn = new Synonym(maxId, words);
            for (String word : words) {
                List<Synonym> lookup = symbolTable.get(word);
                if (lookup == null) {
                    lookup = new LinkedList<>();
                }
                lookup.add(syn);
                symbolTable.put(word, lookup);
            }
        }

        synonyms = new Synonym[maxId + 1];
        for (String key : symbolTable.keys()) {
            List<Synonym> symbols = symbolTable.get(key);
            for (Synonym s : symbols) {
                synonyms[s.id] = s;
            }
        }

        Digraph graph = new Digraph(maxId + 1);
        In hypernymsIn = new In(hypernyms);
        while (!hypernymsIn.isEmpty()) {
            String line = hypernymsIn.readLine();
            String[] values = line.split(",");
            int n = Integer.parseInt(values[0]);
            for (int i = 1; i < values.length; i++) {
                int v = Integer.parseInt(values[i]);
                graph.addEdge(n, v);
            }
        }
        Topological traversal = new Topological(graph);
        if (!traversal.hasOrder()) {
            throw new IllegalArgumentException("Graph has a cycle");
        }
        int root = -1;
        for (int v = 0; v < graph.V(); v++) {
            if (graph.outdegree(v) == 0) {
                if (root >= 0) {
                    throw new IllegalArgumentException("Graph has multiple roots");
                }
                root = v;
            }
        }
        sap = new SAP(graph);
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return symbolTable.keys();
    }

    private int count() {
        return symbolTable.size();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) {
            throw new IllegalArgumentException();
        }
        return symbolTable.contains(word);
    }

    private List<Integer> matchingKeys(String word) {
        List<Integer> keys = new LinkedList<>();
        for (Synonym s : symbolTable.get(word)) {
            keys.add(s.id);
        }
        return keys;
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if ((nounA == null) || (nounB == null)) {
            throw new IllegalArgumentException();
        }
        if (!isNoun(nounA) || !(isNoun(nounB))) {
            throw new IllegalArgumentException("Arguments must be nouns");
        }

        return sap.length(matchingKeys(nounA), matchingKeys(nounB));
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if ((nounA == null) || (nounB == null)) {
            throw new IllegalArgumentException();
        }
        if (!isNoun(nounA) || !(isNoun(nounB))) {
            throw new IllegalArgumentException("Arguments must be nouns");
        }

        int ancestor = sap.ancestor(matchingKeys(nounA), matchingKeys(nounB));
        if (ancestor < 0) {
            return null;
        }
        return String.join(" ", synonyms[ancestor].synset);
    }

    private List<Synonym> lookupNoun(String word) {
        return symbolTable.get(word);
    }

    private static String readSynonym(WordNet net) {
        String word = StdIn.readString();
        if (word.isEmpty()) {
            return null;
        }
        while (!net.isNoun(word)) {
            StdOut.printf("%s is not a noun\n", word);
            word = StdIn.readString();
        }
        for (Synonym synonym : net.lookupNoun(word)) {
            StdOut.printf("%d - synonyms: %s\n", synonym.id, String.join(", ", synonym.synset));
        }
        return word;
    }

    // do unit testing of this class
    public static void main(String[] args) {
        if ((args == null) || (args.length < 2)) {
            throw new IllegalArgumentException();
        }
        WordNet net = new WordNet(args[0], args[1]);
        StdOut.printf("Found %s nouns\n", net.count());
        while (!StdIn.isEmpty()) {
            String wordA = readSynonym(net);
            if (wordA == null) {
                return;
            }
            String wordB = readSynonym(net);
            if (wordB == null) {
                return;
            }
            int length = net.distance(wordA, wordB);
            String ancestor = net.sap(wordA, wordB);
            StdOut.printf("length = %d, ancestor = %s\n", length, ancestor);
        }
    }
}
