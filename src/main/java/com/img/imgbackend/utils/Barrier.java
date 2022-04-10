package com.img.imgbackend.utils;

public class Barrier {
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
