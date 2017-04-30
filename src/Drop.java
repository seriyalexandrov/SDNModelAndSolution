public class Drop {

    public int i1Treshold;
    public int i2Treshold;
    public int i1Max;
    public int i2Max;

    public Drop(int i1Max, int i2Max) {
        this.i1Max = i1Max;
        this.i2Max = i2Max;

        i1Treshold = Math.round(0.7f * (i1Max-1));
        i2Treshold = Math.round(0.7f * (i2Max-1));
    }

    public float p1(int i1) {
        float p;
        if (i1 < i1Treshold) {
            p = 0;
        } else if (i1 >= i1Max) {
            p = 1;
        } else {
            p = (i1 - i1Treshold)*1.0f/ (i1Max - i1Treshold);
        }
//        System.out.printf("p= %f, treshold= %d, iMax= %d, i= %d\n", p, i1Treshold, i1Max, i1);
        return p;
    }

    public float p2(int i2) {
        if (i2 < i2Treshold) return 0;
        if (i2 >= i2Max) return 1;
        return (i2 - i2Treshold)*1.0f/(i2Max - i2Treshold);
    }

    public float percent(float[] probabilities) {

        float sum = 0;
        for (int i1 = 0; i1 <= i1Max; i1++) {
            for (int i2 = 0; i2 <= i2Max; i2++) {
                sum += probabilities[i1*(i2Max+1)+i2]*(p1(i1) + p2(i2));
            }
        }
        return sum;
    }

    public float percentD1(float[] probabilities) {

        float sum = 0;
        for (int i1 = 0; i1 <= i1Max; i1++) {
            for (int i2 = 0; i2 <= i2Max; i2++) {
                sum += probabilities[i1*(i2Max+1)+i2]*(p1(i1));
            }
        }
        return sum;
    }

    public float percentD2(float[] probabilities) {

        float sum = 0;
        for (int i1 = 0; i1 <= i1Max; i1++) {
            for (int i2 = 0; i2 <= i2Max; i2++) {
                sum += probabilities[i1*(i2Max+1)+i2]*(p2(i2));
            }
        }
        return sum;
    }
}
