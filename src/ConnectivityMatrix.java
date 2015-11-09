import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.HashMap;
import java.util.Map;


/**
 * A connectivity matrix for use with ordered keys and symmetric connections, representing using a nested hashmaps.
 * The representation exploits the ordering of the keys to only store the upper right half of the connectivity matrix.
 *
 * Created by Max on 11/4/2015.
 */
public class ConnectivityMatrix {
    // A sparse 2D table, backed by nested hashmaps and designed for row-based access.
    // The value at row R and column R is equal to the strength (or number) of (the) connections(s)
    // between keys R and C.
    // This is a more general form of a traditional connectivity matrix. To get a traditional connectivity matrix,
    // given that t(r,c) is the value of the table at row r and column r and that t(r,c) is
    // 0 when there is no entry in the table at (r,c), simply compute min(1, t(r,c) ).
    private Table<String, String, Integer> t;

    // A map showing the row sums, if the matrix were combined with its transpose
    private Map<String, Integer> marginals;

    public ConnectivityMatrix() {
        t = HashBasedTable.create();
        marginals = new HashMap<>();
    }

    /**
     * Adds/strengthens a connection between Strings s1 and s2 in the table.
     * @param s1 a string alphanumerically before @param s2
     * @param s2 a string alphanumerically after @param s1
     */
    public void addConnection(String s1, String s2) {
        marginals.computeIfPresent(s1, (k, v) -> v + 1);
        marginals.computeIfPresent(s2, (k, v) -> v + 1);
        marginals.putIfAbsent(s1, 1);
        marginals.putIfAbsent(s2, 1);

        if (!t.contains(s1, s2)) {
            t.put(s1, s2, 1);
        } else {
            int oldVal = t.get(s1, s2);
            t.put(s1, s2, oldVal + 1);
        }
    }

    /**
     * Decreases the strength of connection between Strings s1 and s2 and, if the connection strength is reduced to 0,
     * removes the entry from the table.
     * @param s1 a string alphanumerically before @param s2
     * @param s2 a string alphanumerically after @param s1
     */
    public void removeConnection(String s1, String s2) {
        if (t.contains(s1, s2)) {
            if (t.get(s1, s2) <= 1) {
                t.remove(s1, s2);
            } else {
                int oldVal = t.get(s1, s2);
                t.put(s1, s2, oldVal - 1);
            }

            if (marginals.containsKey(s1) && marginals.get(s1) > 1) {
                marginals.put(s1, marginals.get(s1) - 1);
            } else {
                marginals.remove(s1);
            }

            if (marginals.containsKey(s2) && marginals.get(s2) > 1) {
                marginals.put(s2, marginals.get(s2) - 1);
            } else {
                marginals.remove(s2);
            }
//            marginals.computeIfPresent(s1, (k, v) -> v - 1);
//            if (marginals.get(s1) <= 0) {
//                marginals.remove(s1);
//            }
//
//            marginals.computeIfPresent(s2, (k, v) -> v - 1);
//            if (marginals.get(s2) <= 0) {
//                marginals.remove(s2);
//            }
        }
    }

    public double computeAverageDegree() {
        // 2 * # of Edges / # of Nodes
        int numNodes = marginals.size();
        int numEdges = t.size();
        return numNodes == 0 ? 0.0 : (double) numEdges * 2 / (double) numNodes;

    }
}
