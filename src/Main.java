import static java.lang.Math.pow;

public class Main {

    static float a = 0.01f;
    static float b = 0.02f;

    public static void main(String[] args) {
        double f1 = moment(1);
        double f2 = moment(2);
        double f3 = moment(3);
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

    private static double moment(int num) {

        int factorial = 1;
        for (int i = 1; i <= num; i++) {
            factorial *= i;
        }

        return (pow(b, num+1) - pow(a, num+1)) / (factorial*(b-a)*(num+1));
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