import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Zheng Xin on 8/23/2016.
 */
public class TFIDF {

    public static void ForNormalText(BufferedReader in, Map<String, Integer> df,
                                     Map<Integer,Map<String, Integer>> tf,
                                     Map<Integer, Integer> docLen,
                                     Map<Integer, String> label) throws IOException {
        String s;
        int line = 0;

        while ((s = in.readLine()) != null) {
            String[] part = s.split("&#&");
            String[] subparts = part[2].trim().split("\\s+");
            docLen.put(line, subparts.length);
            Map<String, Integer> termFreq = new HashMap<String, Integer>();
            for (int i = 0; i < subparts.length; i++) {
                if (termFreq.containsKey(subparts[i])) {
                    int num = termFreq.get(subparts[i]);
                    num++;
                    termFreq.put(subparts[i], num);
                } else {
                    termFreq.put(subparts[i], 1);
                }
            }
            tf.put(line, termFreq);
            for (String key : termFreq.keySet()) {
                if (!df.containsKey(key) && key.trim().length() > 1) {
                    df.put(key, 1);
                } else if (key.trim().length() > 1) {
                    int term_df = df.get(key);
                    term_df++;
                    df.put(key, term_df);
                }
            }
            line++;
        }
        in.close();
        df.remove("rt");
        System.out.println("Word number:\t" + df.size());
    }


    public static Map<Integer, List<Double>> tfidf(String path) throws IOException {

        Map<String, Integer> df = new HashMap<String, Integer>();
        Map<Integer, Map<String, Integer>> tf = new HashMap<Integer, Map<String, Integer>>();
        Map<Integer, Integer> docLen = new HashMap<Integer, Integer>();
        BufferedReader in = new BufferedReader(new FileReader(path));
        Map<Integer, String> label = new HashMap<Integer, String>();
        ForNormalText(in, df, tf, docLen, label);
        System.out.println("Start");


        int docNum = docLen.size();
        Map<Integer, List<Double>> tfidf = new HashMap<Integer, List<Double>>();

        for(int i :tf.keySet()) {
            List<Double> tfidf_v = new ArrayList<Double>();
            Map<String, Integer> termFreq = tf.get(i);
            int length = docLen.get(i);
            for (String key : df.keySet()) {
                if (termFreq.containsKey(key)) {
                    double tf_value = (double) termFreq.get(key) / length;
                    double idf_value = Math.log(1 + docNum / df.get(key));
                    double tfidf_value = tf_value * idf_value;
                    tfidf_v.add(tfidf_value);

                } else {
                    tfidf_v.add(0.0);

                }
            }
            tfidf.put(i, tfidf_v);

        }

        System.out.println("tfidf writing done");
        return tfidf;
    }
}
