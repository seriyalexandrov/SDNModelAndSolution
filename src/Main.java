import static java.lang.Math.pow;

public class Main {

    static float k = 1.5f;
    static float lambda = 300;

    public static void main(String[] args) {
        double f1 = moment1();
        double f2 = moment2();
        double f3 = moment3();
        System.out.println("f1 = " + f1);
        System.out.println("f2 = " + f2);
        System.out.println("f3 = " + f3);

        double d = D(f1, f2, f3);
        System.out.println("D = " + d);

        double m1 = m1(f1, f2, f3, d);
        System.out.println("m1 = " + m1);

        double m2 = m2(f1, f2, f3, d);
        System.out.println("m2 = " + m2);

        double y = y(f1, m1, m2);
        System.out.println("y = " + y);
    }

    private static double moment1() {
        return k/(lambda);

    }

    private static double moment2() {
        return k*(k+1)/(2*lambda*lambda);
    }

    private static double moment3() {
        return (k*(k+1)*(k+2))/(6*lambda*lambda*lambda);
    }



    private static double D(double f1, double f2, double f3) {
        return 4*pow(f1, 3)*f3 + 4*pow(f2, 3) + pow(f3, 2) - 3*pow(f1*f2, 2) - 6*f1*f2*f3;
    }

    private static double m1 (double f1, double f2, double f3, double D) {
        return (2*(f2-pow(f1, 2))) / (f3 - f1*f2 + Math.sqrt(D));
    }

    private static double m2 (double f1, double f2, double f3, double D) {
        return (2*(f2-pow(f1, 2))) / (f3 - f1*f2 - Math.sqrt(D));
    }

    private static double y(double f1, double m1, double m2) {
        return (f1 - 1/m1)*m2;
    }
}