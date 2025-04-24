package src;

public class Test {
    public static void main(String[] args) {
        SignalSimulator signalSimulator = new SignalSimulator();
        signalSimulator.start();

        try {
            Thread.sleep(5000); // Let it run for 5 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        try {
            Thread.sleep(5000); // Let it run for another 5 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}