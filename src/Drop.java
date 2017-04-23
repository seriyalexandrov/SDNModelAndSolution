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

    //TODO
    public double p1(int i1) {
//        if (i1 < i1Treshold) return 0;
//        if (i1 >= i1Max-1) return 1;
//        return (i1 - i1Treshold)/(i1Max - 1 - i1Treshold);
        return 0;
    }

    public double p2(int i2) {
//        if (i2 < i2Treshold) return 0;
//        if (i2 >= i2Max-1) return 1;
//        return (i2 - i2Treshold)/(i2Max - 1 - i2Treshold);
        return 0;
    }
}
