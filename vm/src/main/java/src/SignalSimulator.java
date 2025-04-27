package src;

public class SignalSimulator extends Thread implements Runnable {
    private int freqrency = 60; // or 50 for 50Hz
    private double sleep = 1000 / freqrency;
    private long milis = (long) (sleep);
    private int nanos = (int) ((sleep - milis) * 1000000);
    private boolean flag;
    private boolean running = true;

    public void run() {
        while (running) {
            try {
                Thread.sleep(milis, nanos);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.flag = !this.flag;
        }
    }

    public void setSignalFreqency(int freqrency) throws NegativeFrequencyException {
        if (freqrency > 0) {
            this.freqrency = freqrency;
            this.sleep = 1000 / this.freqrency;
            this.milis = (long) (sleep);
            this.nanos = (int) ((sleep - milis) * 1000000);
        } else {
            throw new NegativeFrequencyException("Frequency cannot be negative or zero");
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
    public void setFlag(boolean flag){
        this.flag = flag;
    }

    public int getSignalFreqency() {
        return this.freqrency;
    }

    public double getSignalPeriod() {
        return this.milis;
    }

    public boolean getFlag() {
        return this.flag;
    }

    public void kill(){
        this.running = false;
    }
}

class NegativeFrequencyException extends Exception {
    public NegativeFrequencyException(String message) {
        super(message);
    }
}