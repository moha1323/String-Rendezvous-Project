package com.abc.handoff;

import com.abc.pp.stringhandoff.*;
import com.programix.thread.*;

public class StringHandoffImpl implements StringHandoff {
    private Node passer;
    private Node receiver;
    private boolean shutdown;

    public StringHandoffImpl() {
        passer = new Node();
        receiver = new Node();
        shutdown = false;
    }

    @Override
    public synchronized void pass(String msg, long msTimeout)
        throws InterruptedException, TimedOutException, ShutdownException, IllegalStateException {
        threadWasInterrupted();
        threadWasShutdown();

        if (passer.isInitialized()) {
            throw new IllegalStateException("ANOTHER PASSER EXIST ALREADY");
        }

        try {
            passer.setInititialized(true);
            passer.setReady(true);
            this.notifyAll(); // PASSER IS READY
            if (receiver.isReady()) {
                passer.setMessage(msg);
                passer.setGoalAchieved(true);
                this.notifyAll(); // PASSER HAS SET THE MSG
                while ( !receiver.isGoalAchieved()) {
                    this.wait();
                    threadWasShutdown();
                }
            } else if (msTimeout == 0L) {
                while ( !receiver.isReady()) {
                    this.wait();
                    threadWasShutdown();
                }
                passer.setMessage(msg);
                passer.setGoalAchieved(true);
                this.notifyAll(); // PASSER HAS SET THE MSG
                while ( !receiver.isGoalAchieved()) {
                    this.wait();
                    threadWasShutdown();
                }
            } else {
                long msEndTime = System.currentTimeMillis() + msTimeout;
                long msRemaining = msTimeout;

                while ( !receiver.isReady() && msRemaining >= 1L) {
                    this.wait(msRemaining);
                    threadWasShutdown();

                    msRemaining = msEndTime - System.currentTimeMillis();
                }
                if (receiver.isReady()) {
                    passer.setMessage(msg);
                    passer.setGoalAchieved(true);
                    this.notifyAll(); // PASSER HAS SET THE MSG
                    while ( !receiver.isGoalAchieved()) {
                        this.wait();
                        threadWasShutdown();
                    }
                } else {
                    throw new TimedOutException("RECEIVER WASN'T REEADY > TIME RAN OUT");
                }
            }
        } finally {
            passer.setInititialized(false);
            passer.setReady(false);
            passer.setMessage(null);
            passer.setGoalAchieved(false);
            receiver.setGoalAchieved(false);
        }
    }

    @Override
    public synchronized void pass(String msg) throws InterruptedException, ShutdownException, IllegalStateException {
        pass(msg, 0L);
    }

    @Override
    public synchronized String receive(long msTimeout)
        throws InterruptedException, TimedOutException, ShutdownException, IllegalStateException {
        threadWasInterrupted();
        threadWasShutdown();

        String message = null;

        if (receiver.isInitialized()) {
            throw new IllegalStateException("ANOTHER RECEIVER EXIST ALREADY");
        }

        try {
            receiver.setInititialized(true);
            receiver.setReady(true);
            ;
            this.notifyAll(); // RECEIVER IS READY
            if (passer.isReady()) {
                while ( !passer.isGoalAchieved()) {
                    this.wait();
                    threadWasShutdown();
                }
                message = passer.getMessage();
                receiver.setGoalAchieved(true);
                this.notifyAll(); // RECEIVER HAS A HOLD OF MSG
                return message;
            } else if (msTimeout == 0L) {
                while ( !passer.isReady()) {
                    this.wait();
                    threadWasShutdown();
                }
                while ( !passer.isGoalAchieved()) {
                    this.wait();
                    threadWasShutdown();
                }
                message = passer.getMessage();
                receiver.setGoalAchieved(true);
                this.notifyAll(); // RECEIVER HAS A HOLD OF MSG
                return message;
            } else {
                long msEndTime = System.currentTimeMillis() + msTimeout;
                long msRemaining = msTimeout;

                while ( !passer.isReady() && msRemaining >= 1L) {
                    this.wait(msRemaining);
                    threadWasShutdown();

                    msRemaining = msEndTime - System.currentTimeMillis();
                }
                if (passer.isReady()) {
                    while ( !passer.isGoalAchieved()) {
                        this.wait();
                        threadWasShutdown();
                    }
                    message = passer.getMessage();
                    receiver.setGoalAchieved(true);
                    this.notifyAll(); // RECEIVER HAS A HOLD OF MSG
                    return message;
                } else {
                    throw new TimedOutException("PASSER WASN'T REEADY > TIME RAN OUT");
                }
            }
        } finally {
            receiver.setInititialized(false);
            receiver.setReady(false);
        }
    }

    @Override
    public synchronized String receive() throws InterruptedException, ShutdownException, IllegalStateException {
        return receive(0L);
    }

    @Override
    public synchronized void shutdown() {
        shutdown = true;
    }

    @Override
    public Object getLockObject() {
        return this;
    }

    private void threadWasInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            this.notifyAll();
            throw new InterruptedException("THREAD WAS INTERRUPTED");
        }
    }

    private void threadWasShutdown() {
        if (shutdown) {
            this.notifyAll();
            throw new ShutdownException("THREAD WAS SHUTDOWN");
        }
    }

    class Node {
        private boolean isInitialized;
        private boolean isReady;
        private String message;
        private boolean isGoalAchieved;

        public Node() {
            isInitialized = false;
            isReady = false;
            message = null;
            isGoalAchieved = false;
        }

        public boolean isInitialized() {
            return isInitialized;
        }

        public void setInititialized(boolean initialized) {
            isInitialized = initialized;
        }

        public boolean isReady() {
            return isReady;
        }

        public void setReady(boolean ready) {
            isReady = ready;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String msg) {
            message = msg;
        }

        public boolean isGoalAchieved() {
            return isGoalAchieved;
        }

        public void setGoalAchieved(boolean goalAchieved) {
            isGoalAchieved = goalAchieved;
        }

    }
}
