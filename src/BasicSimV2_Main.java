import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


public class BasicSimV2_Main {

    private static Path currentRelativePath = Paths.get("");
    private static String baseDir = currentRelativePath.toAbsolutePath().toString()+"\\Data";
    private static String CircType = "c7552";
    private static String testSize = "1k";
    private static boolean isUX = false; // true = 混亂化 : false = 正常
    private static String ipFilePath = baseDir + "/" + CircType +"_" + testSize + "_ip.txt" ;
    private static String opFile = baseDir + "/" + CircType +"_" + testSize + "_op.txt" ;
    private static int coreCount = Runtime.getRuntime().availableProcessors();
    private static ArrayList<ArrayList<String>> threadInputs = new ArrayList<>();
    private static ConcurrentHashMap<Integer,ArrayList<String>> threadOutput = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception{
        long start = System.currentTimeMillis() ;

        createThreadInputs();
        startThreads();
        produceResult();

        System.out.printf( "Total time=%.3f sec(s)\n", (System.currentTimeMillis()-start)/1000.0 ) ;
    }

    public static void createThreadInputs() throws IOException {
        String ipvs = "";
        ArrayList<String> inputPile = new ArrayList<String>() ;
        BufferedReader br = new BufferedReader(new FileReader(ipFilePath));

        while ((ipvs=br.readLine())!=null) {
            ipvs = ipvs.trim();
            inputPile.add(ipvs);
        }
        br.close();
        int threadAmount = inputPile.size() / coreCount;
        for (int i = 0; i <coreCount-1 ; i++)
            threadInputs.add(new ArrayList<>(inputPile.subList(threadAmount * i, threadAmount * (i + 1))));
        threadInputs.add(new ArrayList<>(inputPile.subList(threadAmount*(coreCount-1),inputPile.size())));
    }

    public static void startThreads() throws InterruptedException {
        BasicSimV2[] simV2s = new BasicSimV2[coreCount];
        Thread[] Threads= new Thread[coreCount];

        for (int i = 0; i <coreCount ; i++) {
            int finalI = i;
            Threads[i] = new Thread(()-> {
                simV2s[finalI] = new BasicSimV2();
                try {
                    simV2s[finalI].BasicSim(baseDir, CircType, threadInputs.get(finalI), isUX, testSize);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                threadOutput.put(finalI,new ArrayList<>(simV2s[finalI].createResult()));
            });
            Threads[i].start();
        }
        for (int i = 0; i <coreCount ; i++)
            Threads[i].join();
    }
    
    public static void produceResult() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(opFile));
        ArrayList<String> results;

        for (int i = 0; i < coreCount; i++) {
            results = threadOutput.get(i);
            for (int j = 0; j<results.size(); j++) {
                bw.write(results.get(j));
                bw.newLine();
            }
        }
        bw.close();
    }
}