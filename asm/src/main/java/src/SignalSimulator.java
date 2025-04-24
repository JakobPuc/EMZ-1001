package src;

public class SignalSimulator extends Thread implements Runnable {
    private int freqrency = 60; // or 50 for 50Hz
    private double sleep = 1000 / freqrency;
    private long milis = (long) (sleep);
    private int nanos = (int) ((sleep - milis) * 1000000);
    private boolean secondsFlag;

    public void run() {
        while (true) {
            try {
                Thread.sleep(milis, nanos);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            secondsFlag = !secondsFlag;
        }
    }

    public void setSignalFreqency(int freqrency) throws NegetiveFrequencyException {
        if (freqrency > 0) {
            this.freqrency = freqrency;
            this.sleep = 1000 / this.freqrency;
            this.milis = (long) (sleep);
            this.nanos = (int) ((sleep - milis) * 1000000);
        } else {
            throw new NegetiveFrequencyException("Frequency cannot be negative or zero");
        }
    }

    public void setSignalFreqency(boolean europian) {
        if (!europian) {
            this.freqrency = 60;
            this.sleep = 1000 / this.freqrency;
            this.milis = (long) (sleep);
            this.nanos = (int) ((sleep - milis) * 1000000);
        } else {
            this.freqrency = 60;
            this.sleep = 1000 / this.freqrency;
            this.milis = (long) (sleep);
            this.nanos = (int) ((sleep - milis) * 1000000);
        }
    }

    public int getSignalFreqency() {
        return this.freqrency;
    }

    public double getSignalPeriod() {
        return this.milis;
    }

    public boolean getSecondsFlag() {
        return this.secondsFlag;
    }
}

class NegetiveFrequencyException extends Exception {
    public NegetiveFrequencyException(String message) {
        super(message);
    }
}