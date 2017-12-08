import edu.princeton.cs.algs4.*;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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

	private class Synonym {
		int id;
		String[] synset;

		Synonym(int id, String[] synset) {
			this.id = id;
			this.synset = synset;
		}
	}

	private RedBlackBST<String, List<Synonym>> symbolTable = new RedBlackBST<>();
	private Digraph graph;
	private SAP sap;
	private Synonym[] synonyms;

	// constructor takes the name of the two input files
	public WordNet(String synsets, String hypernyms) {
		if ((synsets == null) || (hypernyms == null)) {
			throw new IllegalArgumentException();
		}
		In synsetsIn = new In(synsets);
		while (!synsetsIn.isEmpty()) {
			String line = synsetsIn.readLine();
			String[] values = line.split(",");
			String[] words = values[1].split(" ");
			Synonym synonym = new Synonym(Integer.parseInt(values[0]), words);
			for (String word : words) {
				List<Synonym> synonyms = symbolTable.get(word);
				if (synonyms == null) {
					synonyms = new LinkedList<>();
				}
				synonyms.add(synonym);
				symbolTable.put(word, synonyms);
			}
		}
		graph = new Digraph(symbolTable.size());
		synonyms = new Synonym[symbolTable.size()];
		for (String key : symbolTable.keys()) {
			List<Synonym> symbols = symbolTable.get(key);
			for (Synonym s : symbols) {
				synonyms[s.id] = s;
			}
		}

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
		return symbolTable.get(word).stream().map(s -> s.id).collect(Collectors.toList());
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
		return synonyms[ancestor].synset[0];
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
		List<Synonym> synonyms = net.lookupNoun(word);
		for (Synonym synonym : synonyms) {
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
