import java.util.Arrays;

public class Main {

    private static float[][] matrix;

    static float lambda = 500;
    static float[] alphas = new float[]{500, 500};
    static float[] alphaP = new float[]{0.5f, 0}; //вероятность перехода на следующий этап обработки
    static float[] betas = new float[]{450, 450};
    static float[] betaP = new float[]{0.5f, 0};
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
        size = (i1Len + 1) * (i2Len + 1) * (alphaLen + 1) * (betaLen + 1); //вычисляем размерность матрицы
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
                        fillRightPartCoefficientsForState(i1, i2, alpha, beta);
                        fillLeftPartCoefficientForState(i1, i2, alpha, beta);
                    }
                }
            }
        }
//        Utils.checkMatrixColoumnSumIsZero(matrix, size); TODO
    }

    private static void fillLeftPartCoefficientForState(int i1, int i2, int alpha, int beta) {
        //Заполняем значение в столбце левой части
        matrix[Utils.i(i1, i2, alpha, beta)][size] = 0;
    }

    private static void fillRightPartCoefficientsForState(int i1, int i2, int alpha, int beta) {
        for (int i1state = 0; i1state <= i1Len; i1state++) {
            for (int i2state = 0; i2state <= i2Len; i2state++) {
                for (int alphaState = 0; alphaState <= alphaLen; alphaState++) {
                    for (int betaState = 0; betaState <= betaLen; betaState++) {
                        //state - старое состояние
                        if ((i1state > 0 && alphaState == 0) || (i2state > 0 && betaState == 0)) { //исключаем невозможные состояния
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] = 0;
                        } else if (i1state == i1 && i2state == i2 && alphaState == alpha && betaState == beta) {
                            float k = lambda * (1 - drop.p1(i1));
                            k += I.i(alpha) * alphas[I.i(alpha) * (alpha - 1)];
                            k += I.i(beta) * betas[I.i(beta) * (beta - 1)];
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] = -k;
                        } else if (i1state == 0 && i1 == 0 && i2state == i2 && alphaState == 0 && alpha == 1 && betaState == beta) {
                            //Поступил внешний пакет и сразу попал на обработку, т.к. очередь пуста
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] = lambda * (1 - drop.p1(i1state));
                        } else if (i1state == 0 && i1 == 1 && i2state == i2 && alphaState == 0 && alpha == 0 && betaState == beta) {
                            //невозможное состояние
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] = 0;
                        } else if (i1state == i1 - 1 && i2state == i2 && alphaState == alpha && betaState == beta) { // Поступил внешний пакет, причем он не был отброшен, причем есть пакет в обработке
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] = lambda * (1 - drop.p1(i1state));
                        } else if (i1state == i1 + 1 && i2state == i2 && alpha == 1 && alphaState > 0 && betaState == beta) { // Прошла обработка на коммутаторе и пакет ушел из системы либо не ушел из системы, но был сброшен на второй очереди
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] =
                                    alphas[alphaState - 1] * alphaP[alphaState - 1] * q +
                                            alphas[alphaState - 1] * alphaP[alphaState - 1] * (1 - q) * drop.p2(i2state);
                        } else if (i1state == i1 && i2state == i2 && alphaState == alpha-1 && betaState == beta) { // Прошла обработка на коммутаторе и переход на следующую стадию
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] =
                                    alphas[alphaState - 1] * alphaP[alphaState - 1];
                        } else if (i1state == i1 + 1 && i2state == i2 - 1 && alpha == 1 && alphaState > 0 && betaState == beta) { //Прошла обработка на коммутаторе и пакет ушел на контроллер
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] =
                                    alphas[alphaState - 1] * alphaP[alphaState - 1] * (1 - q) * (1 - drop.p2(i2state));
                        } else if (i1state==i1+1 && i2state==0 && i2==0 && alpha == 1 && alphaState > 0 && betaState==0 && beta==1) { //Прошла обработка на коммутаторе и пакет ушел на контроллер
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] =              //и сразу же ушел в обработку
                                    alphas[alphaState - 1] * alphaP[alphaState - 1] * (1 - q) * (1 - drop.p2(i2state));
                        } else if (i1state == i && i2state == i + 1) { //Прошла обработка на контроллере и пакет попал на коммутатор, но был сброшен
                            matrix[Utils.i(i, i)][Utils.i(i1state, i2state)] = beta * drop.p1(i1state);
//                        } else if (i1state == i - 1 && i2state == i + 1) { //Прошла обработка на контроллере и пакет попал на коммутатор
//                            matrix[Utils.i(i, i)][Utils.i(i1state, i2state)] = beta * (1 - drop.p1(i1state));
//                        }
                        } else {
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] = 0.0f;
                        }
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