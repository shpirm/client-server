package lection2;

public class Data {
    private int state = 1;

    public int getState() { return state; }

    public void Tic(){
        System.out.print("Tic-");
        state = 2;
    }
    public void Tak(){
        System.out.print("Tak-");
        state = 3;
    }
    public void Toy(){
        System.out.println("Toy");
        state = 1;
    }
}
