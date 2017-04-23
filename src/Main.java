import java.io.PrintWriter;

public class Main {

    private static double[][] matrix;

    static double lambda = 5;
    static double alpha = 3;
    static double beta = 2;
    static double q = 0.5; // Вероятность ухода пакета из системы
    static int i1Len = 3; //состояние - длина первой очереди + количество в обработке на коммутаторе. 1 - один на обработке. 2 - 1 в очереди, один в обработке
    static int i2Len = 3; //состояние - длина второй очереди + количество в обработке на контроллере. 1 - один на обработке. 2 - 1 в очереди, один в обработке
    static double epsilon = 0.0001; //необходимая точность
    static int size = (i1Len+1) * (i2Len+1); //вычисляем размерность матрицы
    static Drop drop = new Drop(i1Len, i2Len);

    public static void main(String[] args) {

        PrintWriter printWriter = new PrintWriter(System.out);
        createMatrix();

        Utils.printMatrix(matrix, i1Len, i2Len);

        // Введем вектор значений неизвестных на предыдущей итерации,
        // размер которого равен числу строк в матрице, т.е. size,
        // причем согласно методу изначально заполняем его нулями
        double[] previousVariableValues = new double[size];
        for (int i = 0; i < size; i++) {
            previousVariableValues[i] = 0.0;
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

        // Выводим найденные значения неизвестных
        for (int i = 0; i < size; i++) {
            printWriter.print(previousVariableValues[i] + " ");
        }

        printWriter.close();
    }

    private static void createMatrix() {
        matrix = new double[size][size+1];
        for(int i1 = 0; i1 <= i1Len; i1++) {
            for(int i2 = 0; i2 <= i2Len; i2++) {
                fillRightPartCoefficientsForState(i1, i2);
                fillLeftPartCoefficientForState(i1, i2);
            }
        }
    }

    private static void fillLeftPartCoefficientForState(int i1, int i2) {
        //Заполняем значение в столбце левой части
        matrix[index(i1, i2)][size] = lambda * (1 - drop.p1(i1)) + alpha*I.i1(i1) + beta*I.i2(i2);
    }

    private static void fillRightPartCoefficientsForState(int i1, int i2) {
        for(int i1state = 0; i1state <= i1Len; i1state++) {
            for(int i2state = 0; i2state <= i2Len; i2state++) {

//                if (i1 == 0 &&
//                        i2 == 0 &&
//                        i1state == 1 &&
//                        i2state == 0) {
//                    Utils.printState(i1, i2, i1state, i2state, drop);
//                }
                if (i1state == i1 && i2state == i2 || //Or indexes are the same as the state
                        Math.abs(i1 - i1state) > 1 || //Or difference between states is more than 1.
                        Math.abs(i2 - i2state) > 1 ||
                        (i1state == i1 - 1 && i2state == i2 - 1) || // Или два события одновременно, что невозможно
                        (i1state == i1 + 1 && i2state == i2 + 1) ||
                        (i1state == i1 && i2state == i2 - 1) ) { //Пришел новый пакет во вторую очередь, причем в первой не поубавилось - невозможно
                    matrix[index(i1, i2)][index(i1state, i2state)] = 0.0;
                } else if (i1state == i1 - 1 && i2state == i2) { // Поступил внешний пакет, причем он не был отброшен
                    matrix[index(i1, i2)][index(i1state, i2state)] = lambda*(1-drop.p1(i1));
                } else if (i1state == i1 + 1 && i2state == i2) { // Прошла обработка на коммутаторе и пакет ушел из системы либо не ушел из системы, но был сброшен на второй очереди
                    matrix[index(i1, i2)][index(i1state, i2state)] = alpha*q + alpha*(1-q)*drop.p2(i2);
                } else if (i1state == i1 && i2state == i2 + 1) { //Прошла обработка на контроллере и пакет попал на коммутатор, но был сброшен
                    matrix[index(i1, i2)][index(i1state, i2state)] = beta*drop.p1(i1);
                } else if (i1state == i1 - 1 && i2state == i2 + 1) { //Прошла обработка на контроллере и пакет попал на коммутатор
                    matrix[index(i1, i2)][index(i1state, i2state)] = beta*drop.p1(i1);
                } else if (i1state == i1 + 1 && i2state == i2 - 1) { //Прошла обработка на коммутаторе и пакет ушел на контроллер
                    matrix[index(i1, i2)][index(i1state, i2state)] = alpha*(1-q)*(1-drop.p2(i2));
                } else {
                    Utils.printState(i1, i2, i1state, i2state, drop);
                    throw new IllegalStateException("Unknown state");
                }
            }
        }
    }

    private static int index(int i1, int i2) {
        return i1*(i2Len + 1) + i2;
    }
}