package com.img.imgbackend.utils;

public class Barrier {
//    private final Object synchObj = new Object();
//    private int count;
//    private final int NUM_THREADS;
//    private boolean canBeInit = true;
//
//    public Barrier(int noThreads) {
//        synchronized (synchObj) {
//            NUM_THREADS = noThreads;
//        }
//    }
//
//    public void await() {
//        init();
//        countDown();
//        try {
//            awaitZero();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public synchronized void init() {
//        synchronized(synchObj) {
//            if (canBeInit) {
//                count = NUM_THREADS;
//                canBeInit = false;
//            }
//        }
//    }
//
//    public void countDown() {
//        synchronized (synchObj) {
//            if (--count <= 0) {
//                synchObj.notifyAll();
//                canBeInit = true;
//            }
//        }
//    }
//
//    public void awaitZero() throws InterruptedException {
//        synchronized (synchObj) {
//            while (count > 0) {
//                synchObj.wait();
//            }
//        }
//    }
    int initialParties;
    int partiesAwait;

    public Barrier(int parties) {
        initialParties = parties;
        partiesAwait = parties;
    }

    public synchronized void await() {
        //decrements awaiting parties by 1.
        partiesAwait--;

        //If the current thread is not the last to arrive, thread will wait.
        if(partiesAwait>0){
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
       /*If the current thread is last to arrive, notify all waiting threads, and
        launch event*/
        else{
              /* All parties have arrive, make partiesAwait equal to initialParties,
                so that CyclicBarrier could become cyclic. */
            partiesAwait = initialParties;

            notifyAll(); //notify all waiting threads

        }
    }
}
