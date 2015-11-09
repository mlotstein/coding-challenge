import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * This class maintains a list of ASCIITweets that have occurred within a certain number of seconds.
 * Adding a tweet is fairly expensive, as it requires iterating over the entire set of tweets.
 * This could easily be optimized if we can assume that the inputs are ordered, and as a result the tweets themselves
 * are ordered, in which case, we could simply do a binary search over the list to determine which ones to remove.
 * Alternately, we could not bother removing any tweets (or updating the graph) until asked to report the average
 * degree, before which time the list could be allowed to accumulate. Classic time/memory trade-off. This would also
 * make reporting the degree a much more expensive operation, as many entries in the underlying graph would need to
 * be removed.
 * Created by Max on 11/4/2015.
 */
public class TweetBuffer {
    public final int NUM_SECONDS;
    private ConnectivityMatrix graph;
    // An array of tweets that is ORDERED, oldest (0 position) to newest (n-th position).
    private ArrayList<ASCIITweet> tweets;
    private Calendar calendar;

    public TweetBuffer(int numSeconds) {
        NUM_SECONDS = numSeconds;
        tweets = new ArrayList<>();
        graph = new ConnectivityMatrix();
        calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
    }

    public void addLatestTweet(ASCIITweet t) {
        // Check if new tweet is newer than the current newest tweet
        // If so, add as new newest tweet
        if (tweets.size() > 0 && t.compareTo(tweets.get(tweets.size() - 1)) > 0) {
            tweets.add(t);
        } else {
            int idx = findOlderTweetsThan(t.timeStamp);
            tweets.add(idx, t);
//            // Assume t is relatively new, though maybe not the newest
//            // Iterate from newest to oldest until a tweet is found that is older than t
//            for (int i = tweets.size() - 2; i >= 0; i--) {
//                if (t.compareTo(tweets.get(i)) > 0) {
//                    tweets.add(i + 1, t);
//                    // exit the loop
//                    i = -1;
//                }
//            }
        }

        // Add all of t's hashtags to the graph
        for (int i = 0; i < t.hashtags.length - 1; i++ ) {
            for (int j = i + 1; j < t.hashtags.length; j++ ) {
                graph.addConnection(t.hashtags[i], t.hashtags[j]);
            }
        }


        // Determine the earliest possible permissible tweet in the buffer
        calendar.setTime(t.timeStamp);
        calendar.add(GregorianCalendar.SECOND, -1 * NUM_SECONDS);
        Date earliest = calendar.getTime();

        // Find the index of the earliest tweet in tweets
        int idx = findOlderTweetsThan(earliest);
        // Remove those tweets from the graph
        IntStream.range(0, idx).forEach(n -> removeTweetFromGraph(tweets.get(n)));
        // Remove them from the list of tweets
        tweets.subList(0, idx).clear();

//        ASCIITweet curTweet;
//
//        // Determine the buffer's index
//        // Works, not especially performant as it requires iterating over the buffer with every insertion
//        // Iterate over the tweets in buffer and remove if they occur before the hypothetical earliest tweet
//        Iterator<ASCIITweet> it = tweets.iterator();
//        while (it.hasNext()) {
//            curTweet = it.next();
//            if (curTweet.timeStamp.compareTo(earliest) < 0) {
//                // For all unique pairs of hashtags in the tweet remove connection
//                removeTweetFromGraph(curTweet);
//                it.remove();
//            }
//        }
    }

    public void removeTweetFromGraph(ASCIITweet t) {
        String[] hashtags = t.hashtags;
        for (int i = 0; i < hashtags.length - 1; i++ ) {
            for (int j = i + 1; j < hashtags.length; j++ ) {
                graph.removeConnection(hashtags[i], hashtags[j]);
            }
        }
    }

    public double computeAverageDegree() {
        return graph.computeAverageDegree();
    }
    private Predicate<ASCIITweet> olderThan(Date earliest) {
        return p -> p.timeStamp.compareTo(earliest) < 0;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        //stringBuilder.append('\n');
        for (ASCIITweet t : tweets) {
            stringBuilder.append(t.toString());
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }

    /**
     * Finds the index of the newest tweet in tweets that is older than @param d in logarithmic time
     * @param d a Date
     * @return the index in tweets corresponding to the newest tweet that is
     * still older than @param d
     */
    public int findOlderTweetsThan(Date d) {
        int min = 0, max = tweets.size() - 1, mid = -1;
        while (min < max) {
            mid = (int)((max + min)/ 2);

            if (tweets.get(mid).timeStamp.compareTo(d) == 0) {
                return mid;
            } else if (tweets.get(mid).timeStamp.compareTo(d) < 0) {
                min = mid + 1;
            } else {
                max = mid - 1;
            }
        }
        return min;
    }
}
