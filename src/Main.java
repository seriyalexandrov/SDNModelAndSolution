import java.util.Arrays;

public class Main {

    private static float[][] matrix;

    static float lambda = 500;
    static float[] alphas = new float[]{450, 500};
    static float[] betas = new float[]{450, 500};
    static float q = 0.5f; // Вероятность ухода пакета из системы
    static int i1Len = 1; //состояние - длина первой очереди + количество в обработке на коммутаторе. 1 - один на обработке. 2 - 1 в очереди, один в обработке
    static int i2Len = 1; //состояние - длина второй очереди + количество в обработке на контроллере. 1 - один на обработке. 2 - 1 в очереди, один в обработке
    static int alphaLen = 1;
    static int betaLen = 1;
    static float epsilon = 0.00001f; //необходимая точность
    static int size;
    static Drop drop;

    public static void main(String[] args) {

//        System.out.println("Length   dropped  dropped1  dropped2  buffer    buff1    buff2    sojourn");
        int maxSize = 16;
        matrix = new float[maxSize][maxSize + 1];
//        for(i1Len = 10; i1Len <= 150; i1Len+=10) {
        i2Len = i1Len;
        size = (i1Len + 1) * (i2Len + 1); //вычисляем размерность матрицы
        drop = new Drop(i1Len, i2Len);
        createMatrix();
        Utils.printMatrix(matrix, i1Len, i2Len, alphaLen, betaLen);
//        float[] result = calcGaussZeidel();
//        System.out.println("result: " + result);
//            float dropPercent = drop.percent(result);
//            float drop1Percent = drop.percentD1(result);
//            float drop2Percent = drop.percentD2(result);
//            float bufferLength = Utils.averageBufferLength(result, i1Len, i2Len);
//            float buffer1Length = Utils.averageBuffer1Length(result, i1Len, i2Len);
//            float buffer2Length = Utils.averageBuffer2Length(result, i1Len, i2Len);
//            float sojourn = Utils.averageSojournTime(bufferLength, lambda, dropPercent);
//
//            System.out.printf(Locale.ROOT, "   %d    %3.3f    %3.3f     %3.3f     %3.3f     %3.3f    %3.3f    %3.3f\n",
//                    i1Len, dropPercent, drop1Percent, drop2Percent, bufferLength, buffer1Length, buffer2Length, sojourn);
//        }
    }

    private static void createMatrix() {
        for (int i1 = 0; i1 <= i1Len; i1++) {
            for (int i2 = 0; i2 <= i2Len; i2++) {
                for (int alpha = 0; alpha <= alphaLen; alpha++) {
                    for (int beta = 0; beta <= betaLen; beta++) {
//                        fillRightPartCoefficientsForState(i1, i2, alpha, beta);
                        fillLeftPartCoefficientForState(i1, i2, alpha, beta);
                    }
                }
            }
        }
        Utils.checkMatrixColoumnSumIsZero(matrix, size);
    }

    private static void fillLeftPartCoefficientForState(int i1, int i2, int alpha, int beta) {
        //Заполняем значение в столбце левой части
        matrix[Utils.i(i1, i2, alpha, beta)][size] = 1;
    }

    private static void fillRightPartCoefficientsForState(int i1, int i2, int alpha, int beta) {
        for (int i1state = 0; i1state <= i1Len; i1state++) {
            for (int i2state = 0; i2state <= i2Len; i2state++) {
                for (int alphaState = 0; alphaState <= alphaLen; alphaState++) {
                    for (int betaState = 0; betaState <= betaLen; betaState++) {

//                        if (Math.abs(i1 - i1state) > 1 || //Or difference between states is more than 1.
//                                Math.abs(i2 - i2state) > 1 ||
//                                (i1state == i1 - 1 && i2state == i2 - 1) || // Или два события одновременно, что невозможно
//                                (i1state == i1 + 1 && i2state == i2 + 1) ||
//                                (i1state == i1 && i2state == i2 - 1)) { //Пришел новый пакет во вторую очередь, причем в первой не поубавилось - невозможно
//                            matrix[i(i1, i2, alpha, beta)][i(i1state, i2state)] = 0.0f;
//                        } else if (i1state == i1 && i2state == i2) {
//                            matrix[i(i1, i2)][i(i1state, i2state)] = -(lambda * (1 - drop.p1(i1)) + alpha * I.i1(i1) + beta * I.i2(i2));
//                        } else if (i1state == i1 - 1 && i2state == i2) { // Поступил внешний пакет, причем он не был отброшен
//                            matrix[i(i1, i2)][i(i1state, i2state)] = lambda * (1 - drop.p1(i1state));
//                        } else if (i1state == i1 + 1 && i2state == i2) { // Прошла обработка на коммутаторе и пакет ушел из системы либо не ушел из системы, но был сброшен на второй очереди
//                            matrix[i(i1, i2)][i(i1state, i2state)] = alpha * q + alpha * (1 - q) * drop.p2(i2state);
//                        } else if (i1state == i1 && i2state == i2 + 1) { //Прошла обработка на контроллере и пакет попал на коммутатор, но был сброшен
//                            matrix[i(i1, i2)][i(i1state, i2state)] = beta * drop.p1(i1state);
//                        } else if (i1state == i1 - 1 && i2state == i2 + 1) { //Прошла обработка на контроллере и пакет попал на коммутатор
//                            matrix[i(i1, i2)][i(i1state, i2state)] = beta * (1 - drop.p1(i1state));
//                        } else if (i1state == i1 + 1 && i2state == i2 - 1) { //Прошла обработка на коммутаторе и пакет ушел на контроллер
//                            matrix[i(i1, i2)][i(i1state, i2state)] = alpha * (1 - q) * (1 - drop.p2(i2state));
//                        } else {
//                            Utils.printState(i1, i2, i1state, i2state, drop);
//                            throw new IllegalStateException("Unknown state");
//                        }
                    }
                }
            }
        }
    }

    private static float[] calcGaussZeidel() {

        // Введем вектор значений неизвестных на предыдущей итерации,
        // размер которого равен числу строк в матрице, т.е. size,
        // причем согласно методу изначально заполняем его нулями
        float[] previousVariableValues = new float[size];
        Arrays.fill(previousVariableValues, 1f);

        // Введем вектор значений неизвестных на текущем шаге
        float[] currentVariableValues = new float[size];

        // Будем выполнять итерационный процесс до тех пор,
        // пока не будет достигнута необходимая точность
        while (true) {
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
            float error = 0.0f;

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

            System.arraycopy(currentVariableValues, 0, previousVariableValues, 0, currentVariableValues.length);
        }

        float normalize = 0;
        for (float e : previousVariableValues) {
            normalize += e;
        }

        for (int x = 0; x < previousVariableValues.length; x++) {
            previousVariableValues[x] /= normalize;
        }

        Utils.validateResult(previousVariableValues);
        return previousVariableValues;
    }
}