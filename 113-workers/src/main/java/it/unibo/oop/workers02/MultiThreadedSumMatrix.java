package it.unibo.oop.workers02;

import java.util.stream.IntStream;

/**
 * An implementation of SumMatrix interface.
 */
public final class MultiThreadedSumMatrix implements SumMatrix {

    private final int nthread;

    /**
     * Builds a multithreaded matrix sum using streams.
     *
     * @param nthread
     *            no. of thread performing the sum.
     */
    public MultiThreadedSumMatrix(final int nthread) {
        if (nthread <= 0) {
            throw new IllegalArgumentException("nthread must be > 0");
        }
        this.nthread = nthread;
    }

    @Override
    public double sum(double[][] matrix) {
        final int size = matrix.length % nthread + matrix.length / nthread;

        return IntStream.iterate(0, start -> start + size)
            .limit(nthread)
            .mapToObj(start -> new Worker(matrix, start, size))
            .peek(Thread::start)
            .peek(MultiThreadedSumMatrix::joinUninterruptibly)
            .mapToDouble(Worker::getResult)
            .sum();
    }

    @SuppressWarnings("PMD.AvoidPrintStackTrace")
    private static void joinUninterruptibly(final Thread target) {
        var joined = false;
        while (!joined) {
            try {
                target.join();
                joined = true;
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Worker extends Thread {
        private final double[][] matrix;
        private final int startpos;
        private final int nelem;
        private double res;

        /**
         * Build a new worker.
         *
         * @param matrix
         *            the matrix to sum
         * @param startpos
         *            the initial position for this worker
         * @param nelem
         *            the no. of elems to sum up for this worker
         */
        Worker(final double[][] matrix, final int startpos, final int nelem) {
            super();
            this.matrix = matrix;
            this.startpos = startpos;
            this.nelem = nelem;
        }

        @Override
        @SuppressWarnings("PMD.SystemPrintln")
        public synchronized void run() {
            System.out.println("Working on rows " + startpos + " to " + (startpos + nelem - 1));
            for (int i = startpos; i < matrix.length && i < startpos + nelem; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    this.res += matrix[i][j];
                }
            }
        }

        /**
         * Returns the result of summing up the integers within the matrix.
         *
         * @return the sum of every element in the matrix
         */
        public synchronized double getResult() {
            return this.res;
        }

    }

}
