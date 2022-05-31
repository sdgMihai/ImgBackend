package com.img.imgbackend.utils;/*
I am aware that some of the private methods in this class do not need to be synchronized because they are only ever called from
synchronized methods. However, adding synchronized doesn't do any harm and I feel it more explicitly states that methods
are thread safe.

My implementation uses a long to keep track of the CyclicBarrier iteration. The iteration is incremented after all threads
arrive at the barrier. When a thread calls await(), the current CyclicBarrier iteration that the thread is waiting on is stored.
Using the long I can tell if the current iteration of the CyclicBarrier is the one on which a thread is waiting or not. If
it's still waiting for the same iteration to finish then it goes back to waiting, otherwise the thread returns.
*/


import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Barrier {

    private int waitingParties; // Number of threads that the barrier is waiting on
    private final int parties; // Number of threads required to trip the barrier
    private final Runnable barrierAction; // Runnable to be executed when barrier breaks if appropriate constructor used
    private Broken broken = new Broken();
    private long iteration;

    /**
     * Constructor that creates a new CyclicBarrier object that will be tripped when the given number of threads are
     * waiting on the barrier.
     * @param parties Number of threads required to trip the barrier.
     */
    public Barrier(int parties) {
        if (parties <= 0) {
            throw new IllegalArgumentException();
        } else {
            this.parties = parties;
            this.waitingParties = parties;
            this.barrierAction = null;
        }
    }

    /**
     * Constructor that creates a new CyclicBarrier object that will be tripped when the given number of threads are
     * waiting on the barrier. Also takes a Runnable to be executed once the barrier is tripped and before the threads
     * are released.
     * @param parties Number of threads required to trip the barrier.
     * @param barrierAction The Runnable to be executed once the barrier has been tripped.
     */
    public Barrier(int parties, Runnable barrierAction) {

        if (parties <= 0) {
            throw new IllegalArgumentException();
        } else {
            this.parties = parties;
            this.waitingParties = parties;
            this.barrierAction = barrierAction;
        }
    }

    /**
     * Inner class used to represent if the barrier for a particular thread is broken. Object needed as each thread will store an instance
     * of this Barrier object when it waits on the thread. Allows for reset to notify waiting threads they need to wake up and return with broken barrier exception
     * whilst still allowing incoming threads to wait on the new Barrier object that gets made.
     */
    private static class Broken {
        boolean broken = false;
    }

    /**
     * Sets the barrier state to broken, resets value of waiting parties and calls notifyAll() which wakes up all waiting threads.
     */
    private synchronized void breakBarrier() {
        this.broken.broken = true;
        this.waitingParties = this.parties;
        notifyAll();
    }

    /**
     * Calls notifyAll() to wake up all waiting threads, resets value of waiting parties and creates a new iteration of
     * the CyclicBarrier.
     */
    private synchronized void nextIteration() {
        this.waitingParties = this.parties;
        this.iteration++;
        broken = new Broken();
        notifyAll();
    }

    /**
     * Method that implements the main functionality of the await() methods. If the thread entering this method is not the last
     * to arrive then it is put into the WAIT state for thread scheduling purposes and lies dormant until one opf the following
     * things happen:
     *
     * The last thread arrives; or
     * Some other thread interrupts the current thread; or
     * Some other thread interrupts one of the other waiting threads; or
     * Some other thread times out while waiting for barrier; or
     * Some other thread invokes reset() on this barrier.
     *
     * @param isTimed Boolean value indicating if there is a time limit the thread should wait for.
     * @param millis milliseconds of time thread should wait for, 0 if isTimed is false.
     * @param nanos nanoseconds of time thread should wait for 0 if isTimed is false.
     * @throws InterruptedException if the thread is interrupted initially or while a threads is in the waiting state.
     * @throws BrokenBarrierException if barrier is broken due to timeout, interruption, reset or failed barrier action.
     * @throws TimeoutException if millis is invalid.
     */
    private synchronized void barrierFunctionality(boolean isTimed, long millis, int nanos) throws InterruptedException, BrokenBarrierException, TimeoutException {
        // Store the iteration on which the party is waiting on.
        long partyIteration = this.iteration;

        // Store the barrier on which the thread was called in a local variable. Allows for the thread to identify the status of the barrier
        // on which it is waiting and not any other barriers that may be made after it arrives (for example if reset is called)
        Broken local_broken = this.broken;

        // Check to see if barrier is already in the broken state when await is called. Throws BrokenBarrierException if it is.
        if (local_broken.broken) {
            throw new BrokenBarrierException();
        }

        // Check to see if the current threads interrupted status has been set. Throws InterruptedException if it is.
        if (Thread.interrupted()) {
            breakBarrier();
            throw new InterruptedException();
        }

        this.waitingParties--;

        // If the current thread is not the last to arrive then it's disabled (put into WAIT state) for thread scheduling purposes.
        if (waitingParties != 0) {

            waitingOnParties(isTimed, millis, nanos, partyIteration, local_broken);
            // Can return since waitingOnParties() method ensures that the current CyclicBarrier iteration is different to the iteration
            // that the thread was waiting on before returning meaning all threads arrived and its barrier was broken.
            return;
        }

        // Last thread has arrived meaning it does not have to wait and the below method can be executed. Below method can't
        // be executed by any thread other than the last to arrive as all other threads return from this method using the above
        // return statement when they wake up meaning they will never reach this point.
        allPartiesArrived();
    }

    /**
     * If the thread that called await isn't the last to arrive then it is put in the WAITING state. When the last thread arrives
     * the thread will wake up and check it wasn't interrupted, check if its iteration of cyclic barrier is over. If the current barrier
     * iteration is different than the iteration the thread was put into the WAITING state on then it returns. Else checks to see if the
     * barrier is broken and goes to sleep again.
     *
     * @param isTimed Boolean value indicating if there is a time limit the thread should wait for.
     * @param millis millis Length of time thread should wait for, 0 if isTimes is false.
     * @param nanos nanoseconds of time thread should wait for 0 if isTimed is false.
     * @param partyIteration iteration of cyclic barrier on which thread is on.
     * @param local_broken Broken object for the thread, gives the status of the barrier on which the current thread is waiting on.
     * @throws InterruptedException if the thread is interrupted initially or while a threads is in the waiting state.
     * @throws BrokenBarrierException if barrier is broken due to timeout, interruption, reset or failed barrier action.
     * @throws TimeoutException if millis is invalid.
     */
    private synchronized void waitingOnParties(boolean isTimed, long millis, int nanos, long partyIteration, Broken local_broken) throws InterruptedException, BrokenBarrierException, TimeoutException {
        while(true) {
            try {
                if (!isTimed) {
                    //If await() called then wait until woken up by a notify() or notifyAll()
                    wait();
                    // If time is less than or equal to 0 then do not wait at all, as stated in java API
                } else if (millis > 0L || nanos > 0) {
                    // If await(timeout) called then wait until either notify() or notifyAll() are called or until timeout.
                    wait(millis, nanos);

                    // If it wakes up and the iteration has not changed and the barrier is not broken then it was not woken because all threads arrived at the
                    // barrier, or because the barrier was broken. This means it must've woken up due to a timeout and we know to throw the TimeoutException.
                    if (partyIteration == this.iteration && !local_broken.broken) {
                        breakBarrier();
                        throw new TimeoutException();
                    }
                } else {
                    // If we reach this point then await(timeout) was called with a value of 0 or less. So we do not wait and instead break the barrier
                    // then throw a TimeoutException as stated in the java API.
                    breakBarrier();
                    throw new TimeoutException();
                }
            } catch (InterruptedException interruptedException) {
                // Catch statement entered if thread was interrupted while waiting.
                // If it was interrupted while waiting then break the barrier, but only if the current iteration is the same as the iteration it is waiting on.
                if (!local_broken.broken && partyIteration == this.iteration) {
                    breakBarrier();
                    throw interruptedException;
                }

                // Set interrupt flag to true
                Thread.currentThread().interrupt();
            }

            // Check if barrier is broken after waking up, throw BrokenBarrierException if it is. Handles the cases where if another
            // thread was interrupted or timed out or reset called(), the current thread returns with a BrokenBarrierException.
            if (local_broken.broken) {
                throw new BrokenBarrierException();
            }

            // Checks if previous iteration is over and a new iteration has begun. If it is no longer the same iteration
            // then the barrier for the iteration on which the thread was waiting on has broken and the thread can return.
            // If the iterations are the same then the while loop executes again and the thread is put into the WAIT state again.
            if (partyIteration != this.iteration) {
                return;
            }
        }
    }

    /**
     * If the thread that calls await is the last to arrive then the barrier action is run before the other threads can continue
     */
    private synchronized void allPartiesArrived() {
        boolean ranBarrierAction = false;

        // Barrier action is run before the other threads can continue.
        try {
            Runnable action = this.barrierAction;
            if (action != null) {
                // run() used instead of start() because start() executes contents of the run method in a new thread but run() executes
                // the content of the run method in the current thread. This ensures that the barrier action is performed by the last thread
                // that enters the barrier as stated in the java api.
                action.run();
            }

            // Check the Runnable ran and no interrupt or exception occurred before it could be run.
            ranBarrierAction = true;

            // Increment the iteration number and wake up the waiting threads.
            nextIteration();

        } finally {
            // If ranBarrierAction not updated to true then barrier is broken and exception is propagated in the current thread.
            if (!ranBarrierAction) {
                this.breakBarrier();
            }
        }
    }


    /**
     * Waits until all parties have invoked await on this barrier.
     * If the current thread is not the last to arrive then it is disabled for thread scheduling purposes and lies
     * dormant until one of the following things happens:
     *
     * The last thread arrives; or
     * Some other thread interrupts the current thread; or
     * Some other thread interrupts one of the other waiting threads; or
     * Some other thread times out while waiting for barrier; or
     * Some other thread invokes reset() on this barrier.
     *
     * @throws InterruptedException thrown by barrierFunctionality
     * @throws BrokenBarrierException thrown by barrierFunctionality
     */
    public synchronized void await() throws InterruptedException, BrokenBarrierException {
        try {
            barrierFunctionality(false, 0L, 0);
        } catch (TimeoutException te) {
            // This error will actually never be thrown due to the implementation of barrierFunctionality. TimeoutException
            // is only thrown when the await method with a timeout is used but to make use of a shared method a Try Catch
            // is needed as it is a checked exception.
            throw new Error(te);
        }
    }

    /**
     * Waits until all parties have invoked await on this barrier, or the specified waiting time elapses.
     * If the current thread is not the last to arrive then it is disabled for thread scheduling purposes and lies
     * dormant until one of the following things happens:
     *
     * The last thread arrives; or
     * The specified timeout elapses; or
     * Some other thread interrupts the current thread; or
     * Some other thread interrupts one of the other waiting threads; or
     * Some other thread times out while waiting for barrier; or
     * Some other thread invokes reset() on this barrier.
     *
     * @param timeout length of time thread should wait before timing out.
     * @param timeUnit Unit of time to be applied to timeout.
     * @throws InterruptedException thrown by barrierFunctionality
     * @throws BrokenBarrierException thrown by barrierFunctionality
     * @throws TimeoutException thrown by barrierFunctionality
     */
    public synchronized void await(long timeout, TimeUnit timeUnit) throws InterruptedException, BrokenBarrierException, TimeoutException {
        // Regardless of time unit given, value passed to barrierFunctionality is in nanoseconds in order to be used with wait(time) method.
        long milliseconds = timeUnit.toMillis(timeout);
        long nanos = timeUnit.toNanos(timeout);
        int nanoseconds = (int) (nanos - milliseconds * 1000000);
        barrierFunctionality(true, milliseconds, nanoseconds);
    }

    /**
     * Method returns the number of parties (threads) that are currently waiting at the barrier.
     */
    public synchronized int getNumberWaiting() {
        return this.parties - this.waitingParties;
    }

    /**
     * Method returns the number of parties (threads) needed to trip the barrier.
     */
    public synchronized int getParties() {
        return this.parties;
    }

    /**
     * Checks to see if the barrier is broken. Returns true if the barrier is broken, false otherwise.
     */
    public synchronized boolean isBroken() {
        return this.broken.broken;
    }

    /**
     * Resets the barrier to its initial state with no parties waiting. Any parties that were waiting before return with
     * BrokenBarrierException
     */
    public synchronized void reset() {
        breakBarrier();
        nextIteration();
    }

}
