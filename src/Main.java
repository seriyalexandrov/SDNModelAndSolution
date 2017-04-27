public class Main {

    private static double[][] matrix;

    static double lambda = 500;
    static double alpha = 900;
    static double beta = 400;
    static double q = 0.5; // Вероятность ухода пакета из системы
    static int i1Len = 10; //состояние - длина первой очереди + количество в обработке на коммутаторе. 1 - один на обработке. 2 - 1 в очереди, один в обработке
    static int i2Len = 40; //состояние - длина второй очереди + количество в обработке на контроллере. 1 - один на обработке. 2 - 1 в очереди, один в обработке
    static double epsilon = 0.0000000001; //необходимая точность
    static int size;
    static Drop drop;

    public static void main(String[] args) {

        System.out.println("Length   dropped  dropped1  dropped2  buffer   sojourn");
        for(i1Len = 10; i1Len <= 100; i1Len+=10) {
            i2Len = i1Len;
            size = (i1Len + 1) * (i2Len + 1); //вычисляем размерность матрицы
            drop = new Drop(i1Len, i2Len);
            createMatrix();
//        Utils.printMatrix(matrix, i1Len, i2Len);
            double[] result = calcGaussZeidel();
            double dropPercent = drop.percent(result);
            double drop1Percent = drop.percentD1(result);
            double drop2Percent = drop.percentD2(result);
            double bufferLenght = Utils.averageBufferLength(result, i1Len, i2Len);
            double sojourn = Utils.averageSojournTime(bufferLenght, lambda, dropPercent);

            System.out.printf("   %d    %3.3f    %3.3f     %3.3f     %3.3f    %3.3f\n",
                    i1Len, dropPercent, drop1Percent, drop2Percent, bufferLenght, sojourn);
        }
    }

    private static void createMatrix() {
        matrix = new double[size][size + 1];
        for (int i1 = 0; i1 <= i1Len; i1++) {
            for (int i2 = 0; i2 <= i2Len; i2++) {
                fillRightPartCoefficientsForState(i1, i2);
                fillLeftPartCoefficientForState(i1, i2);
            }
        }
        Utils.checkMatrixColoumnSumIsZero(matrix, size);
    }

    private static void fillLeftPartCoefficientForState(int i1, int i2) {
        //Заполняем значение в столбце левой части
        matrix[index(i1, i2)][size] = 0;
    }

    private static void fillRightPartCoefficientsForState(int i1, int i2) {
        for (int i1state = 0; i1state <= i1Len; i1state++) {
            for (int i2state = 0; i2state <= i2Len; i2state++) {

                if (Math.abs(i1 - i1state) > 1 || //Or difference between states is more than 1.
                        Math.abs(i2 - i2state) > 1 ||
                        (i1state == i1 - 1 && i2state == i2 - 1) || // Или два события одновременно, что невозможно
                        (i1state == i1 + 1 && i2state == i2 + 1) ||
                        (i1state == i1 && i2state == i2 - 1)) { //Пришел новый пакет во вторую очередь, причем в первой не поубавилось - невозможно
                    matrix[index(i1, i2)][index(i1state, i2state)] = 0.0;
                } else if (i1state == i1 && i2state == i2) {
                    matrix[index(i1, i2)][index(i1state, i2state)] = -(lambda * (1 - drop.p1(i1)) + alpha * I.i1(i1) + beta * I.i2(i2));
                } else if (i1state == i1 - 1 && i2state == i2) { // Поступил внешний пакет, причем он не был отброшен
                    matrix[index(i1, i2)][index(i1state, i2state)] = lambda * (1 - drop.p1(i1state));
                } else if (i1state == i1 + 1 && i2state == i2) { // Прошла обработка на коммутаторе и пакет ушел из системы либо не ушел из системы, но был сброшен на второй очереди
                    matrix[index(i1, i2)][index(i1state, i2state)] = alpha * q + alpha * (1 - q) * drop.p2(i2state);
                } else if (i1state == i1 && i2state == i2 + 1) { //Прошла обработка на контроллере и пакет попал на коммутатор, но был сброшен
                    matrix[index(i1, i2)][index(i1state, i2state)] = beta * drop.p1(i1state);
                } else if (i1state == i1 - 1 && i2state == i2 + 1) { //Прошла обработка на контроллере и пакет попал на коммутатор
                    matrix[index(i1, i2)][index(i1state, i2state)] = beta * (1 - drop.p1(i1state));
                } else if (i1state == i1 + 1 && i2state == i2 - 1) { //Прошла обработка на коммутаторе и пакет ушел на контроллер
                    matrix[index(i1, i2)][index(i1state, i2state)] = alpha * (1 - q) * (1 - drop.p2(i2state));
                } else {
                    Utils.printState(i1, i2, i1state, i2state, drop);
                    throw new IllegalStateException("Unknown state");
                }
            }
        }
    }

    private static int index(int i1, int i2) {
        return i1 * (i2Len + 1) + i2;
    }

    private static double[] calcGaussZeidel() {

        // Введем вектор значений неизвестных на предыдущей итерации,
        // размер которого равен числу строк в матрице, т.е. size,
        // причем согласно методу изначально заполняем его нулями
        double[] previousVariableValues = new double[size];
        for (int i = 0; i < size; i++) {
            previousVariableValues[i] = 1.0;
        }

        // Будем выполнять итерационный процесс до тех пор,
        // пока не будет достигнута необходимая точность
        while (true) {
            Utils.checkCycle();
            // Введем вектор значений неизвестных на текущем шаге
            double[] currentVariableValues = new double[size];

            // Посчитаем значения неизвестных на текущей итерации
            // в соответствии с теоретическими формулами
            for (int i = 0; i < size; i++) {
                // Инициализируем i-ую неизвестную значением
                // свободного члена i-ой строки матрицы
                currentVariableValues[i] = matrix[i][size];

                // Вычитаем сумму по всем отличным от i-ой неизвестным
                for (int j = 0; j < size; j++) {
                    // При j < i можем использовать уже посчитанные
                    // на этой итерации значения неизвестных
                    if (j < i) {
                        currentVariableValues[i] -= matrix[i][j] * currentVariableValues[j];
                    }

                    // При j > i используем значения с прошлой итерации
                    if (j > i) {
                        currentVariableValues[i] -= matrix[i][j] * previousVariableValues[j];
                    }
                }

                // Делим на коэффициент при i-ой неизвестной
                currentVariableValues[i] /= matrix[i][i];
            }

            // Посчитаем текущую погрешность относительно предыдущей итерации
            double error = 0.0;

            for (int i = 0; i < size; i++) {
                error += Math.abs(currentVariableValues[i] - previousVariableValues[i]);
            }

            // Если необходимая точность достигнута, то завершаем процесс
            if (error < epsilon) {
                break;
            }

            // Переходим к следующей итерации, так
            // что текущие значения неизвестных
            // становятся значениями на предыдущей итерации
            previousVariableValues = currentVariableValues;
        }

        double normalize = 0;
        for(double e : previousVariableValues) {
            normalize += e;
        }

        for(int x = 0; x < previousVariableValues.length; x++) {
            previousVariableValues[x] /= normalize;
        }

        Utils.validateResult(previousVariableValues);
        return previousVariableValues;
    }

    private static void gauss() {

        double[] results = new double[size];
        for (int i = 0; i < size - 1; i++) {
            double diviver = matrix[i][i];

            for (int j = i + 1; j < size; j++) {
                double multiplyier = matrix[j][i];
                double coefficient = multiplyier / diviver;
                for (int k = i + 1; k < size + 1; k++) {
                    matrix[j][k] -= matrix[i][k] * coefficient;

                }
                matrix[j][i] = 0;
            }
        }

        for (int i = size - 1; i > -1; i--) {
            results[i] = matrix[i][size];
            for (int j = i + 1; j < size; j++) {
                results[i] -= matrix[i][j] * results[j];
            }
            results[i] /= matrix[i][i];
        }

        Utils.printResult(results);
    }
}