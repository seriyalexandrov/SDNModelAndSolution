public class Utils {

    public static void printMatrix(double[][] m, int i1Len, int i2Len) {

        int size = (i1Len+1) * (i2Len+1);
        System.out.print("        ");
        for(int i1 = 0; i1 <= i1Len; i1++) {
            for(int i2 = 0; i2 <= i2Len; i2++) {
                System.out.print("(" + i1 + ", " + i2 + ") ");
            }
        }
        System.out.println();
        for(int i1 = 0; i1 <= i1Len; i1++) {
            for(int i2 = 0; i2 <= i2Len; i2++) {
                System.out.print("(" + i1 + ", " + i2 + ")   ");
                for (int col = 0; col < size+1; col++) {
                    System.out.printf("%3.2f   ", m[i1*(i2Len+1)+i2][col]);
                }
                System.out.println();
            }
        }
    }

    private static int interationCounter = 0;

    public static void checkCycle() {
        if (interationCounter++ > 10000) throw new IllegalStateException("Infinite cycle!");
    }
}
