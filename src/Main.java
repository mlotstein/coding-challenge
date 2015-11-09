import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;

public class Main {

    private static final String INPUT_FILE = "tweet_input" + File.separator +  "tweets.txt";
    private static final String OUTPUT_DIR = "tweet_output";
    private static final String OUTPUT_FEAT1 = "ft1.txt";
    private static final String OUTPUT_FEAT2 = "ft2.txt";
    private static final String TIME_STAMP_KEY = "created_at", TEXT_KEY = "text";

    public static void main(String[] args) {
        runFeature1();
        runFeature2();

    }

    public static void runFeature1() {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(new File(INPUT_FILE)));
            File ft1File = new File(new File(OUTPUT_DIR), OUTPUT_FEAT1);
            ft1File.getParentFile().mkdirs();
            BufferedWriter ft1Writer = new BufferedWriter(new FileWriter(ft1File));
            String line, timeStamp, text;
            JSONObject jsonObject;

            ASCIITweet curTWEET;
            int numWithUnicode = 0;
            while ((line = reader.readLine()) != null)
            {
                try {
                    jsonObject = new JSONObject(line);
                    timeStamp = (String) jsonObject.get(TIME_STAMP_KEY);
                    text = (String) jsonObject.get(TEXT_KEY);
                    curTWEET = new ASCIITweet(text, timeStamp);
                    numWithUnicode += containedUnicode(text, curTWEET.text) ? 1 : 0;
                    ft1Writer.write(curTWEET.toString());
                    ft1Writer.write("\n");
                } catch (JSONException e) {
                   System.err.println(e.toString());
                }
            }
            reader.close();
            ft1Writer.write(numWithUnicode + " tweets contained unicode.");
            ft1Writer.close();
        }
        catch (Exception e)
        {
            System.err.format("Exception while reading '%s'.", INPUT_FILE);
            e.printStackTrace();
        }
    }

    public static void runFeature2() {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(new File(INPUT_FILE)));
            File ft2File = new File(new File(OUTPUT_DIR), OUTPUT_FEAT2);
            ft2File.getParentFile().mkdirs();
            BufferedWriter ft2Writer = new BufferedWriter(new FileWriter(ft2File));
            String line, timeStamp, text;
            JSONObject jsonObject;

            TweetBuffer buffer = new TweetBuffer(60);
            ASCIITweet curTWEET;
            while ((line = reader.readLine()) != null)
            {
                try {

                    jsonObject = new JSONObject(line);
                    timeStamp = (String) jsonObject.get(TIME_STAMP_KEY);
                    text = (String) jsonObject.get(TEXT_KEY);
                    curTWEET = new ASCIITweet(text, timeStamp);
                    buffer.addLatestTweet(curTWEET);
                    //ft2Writer.write(curTWEET.toString());
                    //ft2Writer.write("\n");
                } catch (JSONException e) {
                    System.err.println(e.toString());
                }
            }
            ft2Writer.write(buffer.toString());
            ft2Writer.write("\n");
            ft2Writer.write("Current Average Degree: " + buffer.computeAverageDegree());


            reader.close();
            ft2Writer.close();
        }
        catch (Exception e)
        {
            System.err.format("Exception while reading '%s'.", INPUT_FILE);
            e.printStackTrace();
        }
    }

    public static boolean containedUnicode(String text, String tweetText) {
        return text.length() > tweetText.length();
    }
}
