package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        SLList<Integer> testList = new SLList<>();
        AList<Integer> Ns = new AList<>();
        AList<Integer> opCounts = new AList<>();
        AList<Double> times = new AList<>();

        for (int i = 1; i <= 128000; i++) {
            testList.addLast(i);
            if ((i == 1000) || (i == 2000) || (i == 4000) || (i == 8000) || (i == 16000) || (i == 32000) || (i == 64000) || (i == 128000)) {
                Stopwatch timer = new Stopwatch();
                for (int n = 0; n < 10000; n++) {
                    testList.getLast();
                }
                Ns.addLast(i);
                opCounts.addLast(10000);
                times.addLast(timer.elapsedTime());
            }
        }

        printTimingTable(Ns, times, opCounts);

    }

}
