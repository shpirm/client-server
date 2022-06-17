package lection2;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        Data d = new Data();

        Worker w1 = new Worker(1, d);
        Worker w2 = new Worker(2, d);
        Worker w3 = new Worker(3, d);

        w3.join();
        System.out.println("End of main...");
    }
}
