package hospital;

public class Patient implements Runnable {
    private final String name;
    private final BoundedBuffer waitingRoom;

    public Patient(String name, BoundedBuffer waitingRoom) {
        this.name = name;
        this.waitingRoom = waitingRoom;
    }

    @Override
    public void run() {
        System.out.println(name + " arrived.");
        waitingRoom.enter(this);
    }

    public String getName() {
        return name;
    }
}
