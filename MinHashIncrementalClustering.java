import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by Zheng Xin on 1/24/2016.
 */
public class MinHashIncrementalClustering {

    /**
     * Calculate Cosine similarity
     * @param sen is the one needs to calculate cosine similarity with cluster center in ccenter.
     * @throws Exception
     */
    public static double CosineSimilarity(List<Double> sen, List<Double> center){
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < sen.size(); i++) {
            dotProduct += sen.get(i) * center.get(i);
            normA += Math.pow(sen.get(i), 2);
            normB += Math.pow(center.get(i), 2);
        }
        double sim = dotProduct/(Math.sqrt(normA) * Math.sqrt(normB));
        return sim;
    }

    /**
     * Assign Cluster
     * @param sen is the one need to be assigned a cluster
     * @throws Exception
     */
    public static int AssignCluster(List<Double> sen, Map<Integer, List<Double>> ccenter){
        int clu = -1;
        double max = 0;
        for(int i = 0; i < ccenter.size(); i++){
            double sim = CosineSimilarity(sen, ccenter.get(i));
            if(Double.compare(max, sim)<0){
                max = sim;
                clu = i;
            }
        }
        double threshold = 0.35;
        if(Double.compare(max, threshold)<0){
            clu = -1;
        }
        return clu;
    }

    /**
     * Create a new cluster
     * @param cluster need to be update
     * @throws Exception
     */
    public static void CreateCluster(Map<Integer, List<Integer>> cluster, Map<Integer, List<Double>> ccenter,
                                     List<Double> newele, int line){
        int clusterNum = cluster.size();
        ccenter.put(clusterNum, newele);
        List<Integer> cluele = new ArrayList<Integer>();
        cluele.add(line);
        cluster.put(clusterNum,cluele);
    }

    /**
     * Update cluster center
     */
    public static void UpdateCluCenter(Map<Integer, List<Double>> ccenter, Map<Integer, List<Double>> tfidf, List<Integer> cluele, int clu){
        List<Double> newcen = new ArrayList<Double>();
        for(int i = 0; i < ccenter.get(0).size(); i++){
            double sum = 0.0;
            for(int j = 0; j < cluele.size(); j++){
                sum += tfidf.get(cluele.get(j)).get(i);
            }
            double cen = sum/cluele.size();
            newcen.add(cen);
        }
        ccenter.put(clu, newcen);
    }


    /**
     * Update New Cluster UpdateCluster
     * @param cluster need to be update
     * @throws Exception
     */
    public static void UpdateCluster(Map<Integer, List<Integer>> cluster, Map<Integer, List<Double>> ccenter,
                                     Map<Integer, List<Double>> tfidf, int line, int clu){
        List<Integer> cluele = cluster.get(clu);
        cluele.add(line);
        cluster.put(clu, cluele);
        UpdateCluCenter(ccenter, tfidf, cluele, clu);
    }


    public static int getMin(int[] hashcode, int num){
        int min = hashcode[0];
        for(int i = 0; i < (num - 1); i++){
            if( hashcode[i+1] < min){
                min = hashcode[i+1];
            }
        }
        return min;
    }

    public static String concat(String[] words, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++)
            sb.append((i > start ? " " : "") + words[i]);
        return sb.toString();
    }

    public static List<String> ngrams(int n, String str) {
        List<String> ngrams = new ArrayList<String>();
        String[] words = str.split(" ");
        for (int i = 0; i < words.length - n + 1; i++)
            ngrams.add(concat(words, i, i+n));
        return ngrams;
    }



    public static void main(String[] args) throws Exception {
        String path_in = "set/input/data/path";
        String path_out = "set/output/cluster/path";
        if (args.length > 0){
            path_in = args[0];
            path_out = args[1];
        }else{
            System.out.println("Please set input and output file path!");
            return;
        }
        TFIDF tfidf_cal = new TFIDF();
        Map<Integer, List<Double>> tfidf = tfidf_cal.tfidf(path_in);
        System.out.println("Tfidf length:\t" + tfidf.size());


        BufferedReader inB = new BufferedReader(new FileReader(path_in));

        Map<Integer, String> text = new HashMap<Integer, String>();
        Map<Integer, Integer> label = new HashMap<Integer, Integer>();
        int linenum = 0;
        String s;
        while((s = inB.readLine()) != null ){
            text.put(linenum, s);
            String parts[] = s.split("&#&");
            if(parts.length>3 && parts[3].trim().equals("y")){
                label.put(linenum, 1);
            }else{
                label.put(linenum, -1);
            }
            linenum++;
        }
        inB.close();
        System.out.println("Text length:\t" + text.size());

        Map<Integer, List<Integer>> cluster = new HashMap<Integer, List<Integer>>();
        Map<Integer, List<Double>> ccenter = new HashMap<Integer, List<Double>>();
        Map<BigInteger, Integer> hashmap = new HashMap<BigInteger, Integer>();

        BufferedWriter outa = new BufferedWriter(new FileWriter(path_out));

        for(int line = 0; line < text.size(); line++){
            /**
             * Initialize cluster
             */

            s = text.get(line);
            int[] hashvalue = new int[150];
            int minHash1 = 0, minHash2 = 0, minHash3 = 0;

            for (int n = 1; n <= 3; n++) {
                int ngramNum = 0;
                int num = ngrams(n,s).size();
                for (String ngram : ngrams(n, s)){
                    hashvalue[ngramNum] = ngram.hashCode();
                    ngramNum++;
                }
                if( n == 1) {
                    minHash1 = getMin(hashvalue, num);
                }else if(n == 2){
                    minHash2 = getMin(hashvalue, num);
                }else{
                    minHash3 = getMin(hashvalue, num);
                }
            }

            BigInteger key = BigInteger.valueOf(minHash1);
            key = key.add(BigInteger.valueOf(minHash2));
            key = key.add(BigInteger.valueOf(minHash3));

            if(cluster.size() == 0){
                hashmap.put(key, 0);
                List<Integer> cluele = new ArrayList<Integer>();
                cluele.add(line);
                cluster.put(0, cluele);
                ccenter.put(0, tfidf.get(line));
//                inverseClu.put(line, 0);
                outa.write(0 + "\n");
            }else if(hashmap.containsKey(key)){
                int clu = hashmap.get(key);
                UpdateCluster(cluster, ccenter, tfidf, line, clu);
                outa.write(clu + "\n");
            }else{
                int clu = AssignCluster(tfidf.get(line), ccenter);
                if(clu == -1){
                    clu = cluster.size();
                    hashmap.put(key, clu);
                    CreateCluster(cluster, ccenter, tfidf.get(line), line);
                }else{
                    hashmap.put(key, clu);
                    UpdateCluster(cluster, ccenter, tfidf, line, clu);
                }
                outa.write(clu + "\n");
            }
        }
        outa.close();

        System.out.println("Number of clusters is:\t " + cluster.size());

    }
}
