import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static float[][] matrix;

    static float lambda = 500;
    static float[] alphas = new float[]{500, 500};
    static float[] alphaP = new float[]{0}; //вероятность перехода на следующий этап обработки
    static float[] betas = new float[]{450, 450};
    static float[] betaP = new float[]{0};
    static float q = 0.5f; // Вероятность ухода пакета из системы
    static int i1Len = 1; //состояние - длина первой очереди + количество в обработке на коммутаторе. 1 - один на обработке. 2 - 1 в очереди, один в обработке
    static int i2Len = 1; //состояние - длина второй очереди + количество в обработке на контроллере. 1 - один на обработке. 2 - 1 в очереди, один в обработке
    static int alphaLen = 1;
    static int betaLen = 1;
    static float epsilon = 0.00001f; //необходимая точность
    static int size;
    static Drop drop;
    static HashSet<Integer> nullableCols = new HashSet<>();
    static HashSet<Integer> nullableRows = new HashSet<>();

    public static void main(String[] args) {

        System.out.println("Length   dropped  dropped1  dropped2  buffer    buff1    buff2    sojourn");
        for (i1Len = 2; i1Len <= 10; i1Len += 1) {
            i2Len = i1Len;
            size = (i1Len + 1) * (i2Len + 1) * (alphaLen + 1) * (betaLen + 1); //вычисляем размерность матрицы
            matrix = new float[size][size + 1];
            drop = new Drop(i1Len, i2Len);
            createMatrix();
//        Utils.printMatrix(matrix, i1Len, i2Len, alphaLen, betaLen);
//            Utils.printMatrixSimple(matrix, size);
            float[] result = calcGaussZeidel();
//            Utils.printResult(result);
            float dropPercent = drop.percent(result);
            float drop1Percent = drop.percentD1(result);
            float drop2Percent = drop.percentD2(result);
            float bufferLength = Utils.averageBufferLength(result, i1Len, i2Len, alphaLen, betaLen);
            float buffer1Length = Utils.averageBuffer1Length(result, i1Len, i2Len, alphaLen, betaLen);
            float buffer2Length = Utils.averageBuffer2Length(result, i1Len, i2Len, alphaLen, betaLen);
            float sojourn = Utils.averageSojournTime(bufferLength, lambda, dropPercent);

            System.out.printf(Locale.ROOT, "   %d    %3.3f    %3.3f     %3.3f     %3.3f     %3.3f    %3.3f    %3.3f\n",
                    i1Len, dropPercent, drop1Percent, drop2Percent, bufferLength, buffer1Length, buffer2Length, sojourn);
        }
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
//        Utils.printMatrix(matrix, i1Len, i2Len, alphaLen, betaLen);
        Utils.checkMatrixColoumnSumIsZero(matrix, size);
        transformSystem();
    }

    private static void transformSystem() {

        assert nullableCols.size() == nullableRows.size();
        List<Integer> rowsList = nullableRows.stream().collect(Collectors.toList());
        List<Integer> colsList = nullableCols.stream().collect(Collectors.toList());

        float[][] matrixCopy = matrix.clone();

        int rowToRemovePos = 0;
        int colToRemovePos = 0;

        matrix = new float[size - nullableRows.size()][size - nullableCols.size() + 1];

        int tmpX = -1;
        int tmpY;


        //re-populate new matrix by searching through the original copy of matrix, while skipping useless row and column
        // works only for 1 row and 1 column in a 2d array but by changing the conditional statement we can make it work for n number of rows or columns in a 2d array.
        for (int i = 0; i < size; i++) {
            tmpX++;
            if (rowToRemovePos < rowsList.size() && i == rowsList.get(rowToRemovePos)) {
                tmpX--;
                rowToRemovePos++;
            }
            tmpY = -1;
            colToRemovePos = 0;
            for (int j = 0; j < size; j++) {
                tmpY++;
                if (colToRemovePos < colsList.size() && j == colsList.get(colToRemovePos)) {
                    tmpY--;
                    colToRemovePos++;
                }

                if (!nullableRows.contains(i) && !nullableCols.contains(j)) {
                    matrix[tmpX][tmpY] = matrixCopy[i][j];
                }
            }
        }

        size = size - nullableRows.size();
        for (int x = 0; x < size; x++) {
            matrix[x][size] = 0;
        }
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

                        if ((i1state > 0 && alphaState == 0) || (i2state > 0 && betaState == 0) ||
                                (i1state == 0 && alphaState > 0) || (i2state == 0 && betaState > 0)) {
                            nullableRows.add(Utils.i(i1state, i2state, alphaState, betaState));
                            nullableCols.add(Utils.i(i1state, i2state, alphaState, betaState));
                        }
                        if ((i1state > 0 && alphaState == 0) || (i2state > 0 && betaState == 0) ||
                                (i1state == 0 && alphaState > 0) || (i2state == 0 && betaState > 0) ||
                                (i1 > 0 && alpha == 0) || (i2 > 0 && beta == 0) ||
                                (i1 == 0 && alpha > 0) || (i2 == 0 && beta > 0)) { //исключаем невозможные состояния
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] = 0;
                        } else if (i1state == i1 && i2state == i2 && alphaState == alpha && betaState == beta) {
                            float k = lambda * (1 - drop.p1(i1));
                            k += I.i(alpha) * alphas[I.i(alpha) * (alpha - 1)];
                            k += I.i(beta) * betas[I.i(beta) * (beta - 1)];
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] = -k;
                        } else if (i1state == 0 && i1 == 1 && i2state == i2 && alphaState == 0 && alpha == 1 && betaState == beta) {
                            //Поступил внешний пакет и сразу попал на обработку, т.к. очередь пуста
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] = lambda * (1 - drop.p1(i1state));
                        } else if ((i1state == 0 && i1 == 1 && i2state == i2 && alphaState == 0 && alpha == 0 && betaState == beta) ||
                                (i1state == i1 && i2state == 0 && i2 == 1 && alphaState == alpha && betaState == 0 && beta == 0)) {
                            //невозможное состояние
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] = 0;
                        } else if (i1state == i1 - 1 && i2state == i2 && alphaState == alpha && betaState == beta) { // Поступил внешний пакет, причем он не был отброшен, причем есть пакет в обработке
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] = lambda * (1 - drop.p1(i1state));
                        } else if ((i1state == i1 + 1 && i2state == i2 && alpha == 1 && alphaState > 0 && betaState == beta) ||
                                (i1state == 1 && i1 == 0 && i2state == i2 && alpha == 0 && alphaState > 0 && betaState == beta)) { // Прошла обработка на коммутаторе
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] =          //и пакет ушел из системы либо не ушел из системы,
                                    alphas[alphaState - 1] * (1 - alphaP[alphaState - 1]) * q +                                 //но был сброшен на второй очереди
                                            alphas[alphaState - 1] * (1 - alphaP[alphaState - 1]) * (1 - q) * drop.p2(i2state);
                        } else if (i1state == i1 && i1 > 0 && i2state == i2 && alphaState == alpha - 1 && betaState == beta) { // Прошла обработка на коммутаторе и переход на следующую стадию
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] =
                                    alphas[alphaState - 1] * alphaP[alphaState - 1];
                        } else if ((i1state == i1 + 1 && i2state == i2 - 1 && alpha == 1 && alphaState > 0 && betaState == beta) ||
                                (i1state == 1 && i1 == 0 && i2state == i2 - 1 && alpha == 0 && alphaState > 0 && betaState == beta)) { //Прошла обработка на коммутаторе и
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] =                  //пакет ушел на контроллер
                                    alphas[alphaState - 1] * (1 - alphaP[alphaState - 1]) * (1 - q) * (1 - drop.p2(i2state));
                        } else if ((i1state == i1 + 1 && i2state == 0 && i2 == 1 && alpha == 1 && alphaState > 0 && betaState == 0 && beta == 1) ||
                                (i1state == 1 && i1 == 0 && i2state == 0 && i2 == 1 && alpha == 0 && alphaState > 0 && betaState == 0 && beta == 1)) { //Прошла обработка на коммутаторе и пакет ушел на контроллер
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] =              //и сразу же ушел в обработку
                                    alphas[alphaState - 1] * (1 - alphaP[alphaState - 1]) * (1 - q) * (1 - drop.p2(i2state));
                        } else if ((i1state == i1 && i2state == i2 + 1 && i2 > 0 && alphaState == alpha && beta == 1 && betaState > 0) ||
                                (i1state == i1 && i2 == 0 && i2state == 1 && alphaState == alpha && beta == 0 && betaState > 0)) { //Прошла обработка на контроллере
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] =   //и пакет попал на коммутатор, но был сброшен
                                    betas[betaState - 1] * (1 - betaP[betaState - 1]) * drop.p1(i1state);
                        } else if ((i1state == i1 - 1 && i1state > 0 && i2state == i2 + 1 && i2 > 0 && alphaState == alpha && beta == 1 && betaState > 0) ||
                                (i1state == i1 - 1 && i1state > 0 && i2state == 1 && i2 == 0 && alphaState == alpha && beta == 0 && betaState > 0)) { //Прошла обработка на контроллере
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] =                         //и пакет попал на коммутатор
                                    betas[betaState - 1] * (1 - betaP[betaState - 1]) * (1 - drop.p1(i1state));
                        } else if ((i1state == 0 && i1 == 1 && i2state == i2 + 1 && i2 > 0 && alphaState == 0 && alpha == 1 && betaState > 0 && beta == 1) ||
                                (i1state == 0 && i1 == 1 && i2state == 1 && i2 == 0 && alphaState == 0 && alpha == 1 && betaState > 0 && beta == 0)) { //Прошла обработка на контроллере
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] =                      //и пакет попал на коммутатор сразу на обработку
                                    betas[betaState - 1] * (1 - betaP[betaState - 1]) * (1 - drop.p1(i1state));
                        } else if (i1state == i1 && i2state == i2 && i2 > 0 && alphaState == alpha && betaState == beta - 1) {//переход на новый этап на контроллере
                            matrix[Utils.i(i1, i2, alpha, beta)][Utils.i(i1state, i2state, alphaState, betaState)] =
                                    betas[betaState - 1] * betaP[betaState - 1];
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
            Utils.checkCycle();
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

            float error = 0.0f;
            for (int i = 0; i < size; i++) {
                error += Math.abs(currentVariableValues[i] - previousVariableValues[i]);
            }
            if (error < epsilon) {
                break;
            }
//            Utils.printResult(previousVariableValues);
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

        return trasformToFullResult(previousVariableValues);
    }

    private static float[] trasformToFullResult(float[] previousVariableValues) {
        int localSize = (i1Len + 1) * (i2Len + 1) * (alphaLen + 1) * (betaLen + 1);
        float[] result = new float[localSize];
        List<Integer> nullables = nullableRows.stream().collect(Collectors.toList());
        int nullablesCount = 0;
        int nonnullCount = 0;
        for (int x = 0; x < localSize; x++) {
            if (nullablesCount < nullables.size() && nullables.get(nullablesCount) == x) {
                result[x] = 0;
                nullablesCount++;
            } else {
                result[x] = previousVariableValues[nonnullCount];
                nonnullCount++;
            }
        }
        return result;
    }
}