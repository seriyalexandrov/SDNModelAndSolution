public class Utils {

    public static void printMatrix(float[][] m, int i1Len, int i2Len, int alphaLen, int betaLen) {

        int size = (i1Len+1) * (i2Len+1) * (alphaLen+1) * (betaLen+1);
        System.out.print("        ");
        for(int i1 = 0; i1 <= i1Len; i1++) {
            for(int i2 = 0; i2 <= i2Len; i2++) {
                for(int alpha = 0; alpha <= alphaLen; alpha++) {
                    for(int beta = 0; beta <= betaLen; beta++) {
                        System.out.print(" (" + i1 + "," + i2 + "," + alpha + "," + beta + ")");
                    }
                }
            }
        }
        System.out.println();
        for(int i1 = 0; i1 <= i1Len; i1++) {
            for(int i2 = 0; i2 <= i2Len; i2++) {
                for(int alpha = 0; alpha <= alphaLen; alpha++) {
                    for (int beta = 0; beta <= betaLen; beta++) {
                        System.out.print("(" + i1 + "," + i2 + "," + alpha + "," + beta + ")   ");
                        for (int col = 0; col < size + 1; col++) {
                            System.out.printf("%5.2f     ", m[i(i1, i2, alpha, beta)][col]);
                        }
                        System.out.println();
                    }
                }
            }
        }
    }

    private static int interationCounter = 0;

    public static void checkCycle() {
        if (interationCounter++ > 100000) throw new IllegalStateException("Infinite cycle!");
    }

    public static void printResult(float[] res) {
        for (float e : res) {
            System.out.printf("%3.8f ", e);
        }
        System.out.println();
    }

    public static void validateResult(float[] res) {
        float s = 0;
        for (float e : res) {
            s += e;
        }
        if (Math.abs(s - 1) > 0.001) throw new IllegalStateException("Result is not consistent: " + s);
    }

    public static void printState(int i1, int i2, int i1state, int i2state, Drop drop) {
        System.out.println("from state (" + i1state + ", " + i2state + ") to state (" + i1 + ", " + i2+ ")");
        System.out.println("pDrop1 = " + drop.p1(i1) + " treshold = " + drop.i1Treshold + " maxLen = " + drop.i1Max);
        System.out.println("pDrop2 = " + drop.p2(i2));
    }

    public static void checkMatrixColoumnSumIsZero(float[][] matrix, int size) {
        for (int col = 0; col < size; col++) {
            float sum = 0;
            for (int row = 0; row < size; row++) {
                sum += matrix[row][col];
            }
            if (Math.abs(sum) > 0.001) {
                throw new IllegalStateException("Column " + col + " has non zero sum");
            }
        }
    }

    public static float averageBufferLength(float[] probabilities, int i1Max, int i2Max) {
        float sum = 0;
        for (int i1 = 0; i1 <= i1Max; i1++) {
            for (int i2 = 0; i2 <= i2Max; i2++) {
                sum += probabilities[i1*(i2Max+1)+i2]*(i1 + i2);
            }
        }
        return sum;
    }

    public static float averageBuffer1Length(float[] probabilities, int i1Max, int i2Max) {
        float sum = 0;
        for (int i1 = 0; i1 <= i1Max; i1++) {
            for (int i2 = 0; i2 <= i2Max; i2++) {
                sum += probabilities[i1*(i2Max+1)+i2]*(i1);
            }
        }
        return sum;
    }

    public static float averageBuffer2Length(float[] probabilities, int i1Max, int i2Max) {
        float sum = 0;
        for (int i1 = 0; i1 <= i1Max; i1++) {
            for (int i2 = 0; i2 <= i2Max; i2++) {
                sum += probabilities[i1*(i2Max+1)+i2]*(i2);
            }
        }
        return sum;
    }

    public static float averageSojournTime(float m, float lambda, float pi) {
        return m/(lambda*(1-pi));
    }

    static int i(int i1, int i2, int alpha, int beta) {
        return
                i1 * (Main.i2Len + 1)*(Main.alphaLen + 1)*(Main.betaLen + 1) +
                i2*(Main.alphaLen + 1)*(Main.betaLen + 1) +
                alpha*(Main.betaLen + 1) +
                beta;
    }
}
