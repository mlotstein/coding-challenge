import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * An immutable object representing an ASCIITweet.
 *
 * The internal state is representing by a string, which contains only ASCII characters, and a Date object.
 * The Date type is used so that ASCIITweets can be ordered.
 * Created by Max on 10/30/2015.
 */
public class ASCIITweet implements Comparable<ASCIITweet>{


    // Create a formatter capable of parsing the time stamp.
    // This isn't crucial, but if we want to do any ordering of tweets downstream, it's better to have them as Dates
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("EE MMM dd HH:mm:ss Z yyyy");
    private static final String REGEX_PATTERN = "[^\u0000-\u007F]+";
    private static final String HASH_OPEN = " #";
    private static final String HASH_DIGITS = "#[0-9]+";

    final String text;
    final Date timeStamp;
    /**
     * An alphabetically ordered
     */
    final String[] hashtags;
    public ASCIITweet(String text, String timeStampString) throws ParseException {
        // Fix time zone error that causes times to be shifted by four hours.
        DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT"));
        this.text = scrubNonASCII(text);
        this.hashtags = extractHashTags(this.text.toUpperCase());
        this.timeStamp = DATE_FORMATTER.parse(timeStampString);
    }

    /**
     * Removes Non-ASCII characters from a string.
     * Source: http://stackoverflow.com/questions/8519669/replace-non-ascii-character-from-string
     * @param s
     * @return a String with only ASCII characters (and possibly control characters!)
     */
    private String scrubNonASCII(String s) {
        // Alternately, check if the value is < 128
//        StringBuilder stringBuilder = new StringBuilder();
//        for(char c : s.toCharArray()) {
//            if (c < 128) {
//                stringBuilder.append(c);
//            }
//        }
//        return stringBuilder.toString();
        return s.replaceAll(REGEX_PATTERN, "");
    }

    /**
     * Takes a string and returns an array of strings containing all hashtags where hashtags are delimited according to
     * Twitter spec
     * According to Twitter Spec (http://bit.ly/1RkZ16C) says that
     * (1) Hashtags MUST have a space beforehand
     * (2) Punctuation and whitespace will end a hashtag
     * (3) Can't be only numbers
     * @param s
     * @return an array of Strings s.t. each element is unique (intentionally NOT a set)
     */

    private static String[] extractHashTags(String s) {
        int beginIdx = s.indexOf(HASH_OPEN), endIdx = beginIdx + HASH_OPEN.length() - 1;
        char cur;
        ArrayList<String> hashtags = new ArrayList<>();
        while (beginIdx >= 0 && endIdx < s.length()) {
            cur = s.charAt(endIdx);

            // while the current character is not white space and IS either a letter or a number (and thus is not
            // punctuation)
            while (endIdx < s.length() && !Character.isWhitespace(cur) && (Character.isAlphabetic(cur) || Character.isDigit(cur))) {
                endIdx++;
                cur = s.charAt(endIdx >= s.length() ? 0 : endIdx);
            }
            if (endIdx - beginIdx > 2) {
                hashtags.add(s.substring(beginIdx + HASH_OPEN.length() - 1, endIdx));
            }
            beginIdx = s.indexOf(HASH_OPEN, endIdx);
            endIdx = beginIdx + HASH_OPEN.length();

        }

        hashtags.removeIf(p -> p.matches(HASH_DIGITS));
        // Order the set of hashtags
        Collections.sort(hashtags);
        return hashtags.toArray(new String[hashtags.size()]);
    }

    public String toString() {
        return text + " (timestamp: " + DATE_FORMATTER.format(timeStamp) + ")";
    }

    public String toHashTagString() {
        return String.join(", ", hashtags) + "(timestamp: " + DATE_FORMATTER.format(timeStamp) + ")";
    }

    @Override
    public int compareTo(ASCIITweet t) {
        return this.timeStamp.compareTo(t.timeStamp);
    }

}
