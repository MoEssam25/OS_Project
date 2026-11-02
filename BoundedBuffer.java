import java.util.LinkedList;
import java.util.Queue;

public class BoundedBuffer {

    private final Queue<Patient> patients = new LinkedList<>();
    private final Semaphore empty;
    private final Semaphore full;
    private final Semaphore mutex;

    public BoundedBuffer(int capacity) {
        empty = new Semaphore(capacity);
        full = new Semaphore(0);
        mutex = new Semaphore(1);
    }

    public void enter(Patient patient) {
        if (!empty.tryAcquire(0)) {
            System.out.println("The waiting room is full.");
            System.out.println(patient.getName() + " is waiting outside for an available seat.");
            empty.acquire();
        }

        mutex.acquire();
        patients.add(patient);
        System.out.println(patient.getName() + " entered the waiting room.");
        mutex.release();

        full.release();
    }

    public Patient take() {
        full.acquire();

        mutex.acquire();
        Patient patient = patients.poll();
        mutex.release();

        if (patient != null) {
            empty.release();
        }

        return patient;
    }

    public void shutdown(int numberOfDoctors) {
        for (int i = 0; i < numberOfDoctors; i++) {
            full.release();
        }
    }
}
