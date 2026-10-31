import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Hospital {

    // Fixed by the assignment
    private static final int TREATMENT_ROOMS = 2;

    // Timing
    private static final int MIN_ARRIVAL_DELAY_MS = 200;
    private static final int MAX_ARRIVAL_DELAY_MS = 700;

    private static final String INVALID_INPUT_MSG =
            "Invalid input. Please enter a positive integer greater than 0.";

    // Inputs
    private int waitingRoomCapacity;
    private int numDoctors;
    private int numPatients;

    // Shared resources (typed — Block 1 decision)
    private WaitingRoom waitingRoom;
    private TreatmentRooms treatmentRooms;

    // Threads
    private final List<Thread> doctors  = new ArrayList<>();
    private final List<Thread> patients = new ArrayList<>();

    private final Random random = new Random();

    // ---------- Logger (one place for all output) ----------
    public static synchronized void log(String message) {
        System.out.println(message);
    }

    // ---------- Entry point ----------
    public static void main(String[] args) {
        Hospital hospital = new Hospital();
        hospital.readInput();
        hospital.createSharedResources();
        hospital.createDoctors();
        hospital.createPatients();
        hospital.startSimulation();
        hospital.waitForCompletion();
        hospital.finish();
    }

    // ---------- Step 4: input ----------
    private void readInput() {
        Scanner sc = new Scanner(System.in);
        waitingRoomCapacity = readPositiveInt(sc, "Waiting room capacity: ");
        numDoctors          = readPositiveInt(sc, "Number of doctors: ");
        numPatients         = readPositiveInt(sc, "Number of patients: ");
        // Scanner intentionally left open; JVM exits after the simulation.
    }

    private int readPositiveInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!sc.hasNextLine()) {                 // EOF: never loop forever
                log("No more input available. Exiting.");
                System.exit(1);
            }
            String line = sc.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value > 0) {
                    return value;
                }
                log(INVALID_INPUT_MSG);
            } catch (NumberFormatException e) {
                log(INVALID_INPUT_MSG);
            }
        }
    }

    // ---------- Step 5: shared resources ----------
    private void createSharedResources() {
        waitingRoom    = new WaitingRoom(waitingRoomCapacity);
        treatmentRooms = new TreatmentRooms(TREATMENT_ROOMS);
    }

    // ---------- Step 6: doctors ----------
    private void createDoctors() {
        for (int i = 1; i <= numDoctors; i++) {
            doctors.add(new Thread(new Doctor(i, waitingRoom, treatmentRooms), "Doctor-" + i));
        }
    }

    // ---------- Step 7: patients ----------
    private void createPatients() {
        StringBuilder arriving = new StringBuilder("Patients arriving:");
        for (int i = 1; i <= numPatients; i++) {
            patients.add(new Thread(new Patient(i, waitingRoom), "Patient-" + i));
            arriving.append(" P").append(i);
        }
        log(arriving.toString());
        log("");
    }

    // ---------- Step 8: start ----------
    private void startSimulation() {
        // Doctors first: they block idle waiting for patients.
        for (Thread doctor : doctors) {
            doctor.start();
        }

        // Patients spaced out with random arrival delays.
        // numPatients > 0 is guaranteed by validation, so size() - 1 >= 0.
        for (int i = 0; i < patients.size(); i++) {
            patients.get(i).start();
            if (i < patients.size() - 1) {
                sleepRandomArrivalDelay();
            }
        }
    }

    private void sleepRandomArrivalDelay() {
        int delay = MIN_ARRIVAL_DELAY_MS
                  + random.nextInt(MAX_ARRIVAL_DELAY_MS - MIN_ARRIVAL_DELAY_MS + 1);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ---------- Step 9: shutdown ----------
    private void waitForCompletion() {
        for (Thread patient : patients) {
            joinQuietly(patient);          // wait for all patients to leave
        }
        waitingRoom.stopDoctors();          // wake doctors blocked in take()
        for (Thread doctor : doctors) {
            joinQuietly(doctor);            // wait for doctors to exit
        }
    }

    private void joinQuietly(Thread t) {
        try {
            t.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ---------- Step 10: finish ----------
    private void finish() {
        log("All patients have been treated.");
        log("Simulation completed.");
    }
}