import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Hospital {

    private static final int TREATMENT_ROOMS = 2;      
    private static final int MAX_ARRIVAL_DELAY_MS = 700;

    private int waitingRoomCapacity;
    private int numDoctors;
    private int numPatients;

    private BoundedBuffer waitingRoom;
    private Semaphore treatmentRooms;

    private final List<Thread> doctorThreads = new ArrayList<>();
    private final List<Thread> patientThreads = new ArrayList<>();

    private final Scanner scanner = new Scanner(System.in);
    private final Random random = new Random();

   
    private static synchronized void log(String message) {
        System.out.println(message);
    }

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

    private void readInput() {
        waitingRoomCapacity = readPositiveInt("Waiting room capacity: ");
        numDoctors = readPositiveInt("Number of doctors: ");
        numPatients = readPositiveInt("Number of patients: ");
        scanner.close();
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // fall through to the error message
            }
            System.out.println("Please enter a positive whole number.");
        }
    }

    private void createSharedResources() {
        waitingRoom = new BoundedBuffer(waitingRoomCapacity);
        treatmentRooms = new Semaphore(TREATMENT_ROOMS);
    }

    private void createDoctors() {
        for (int i = 1; i <= numDoctors; i++) {
            doctorThreads.add(new Thread(new Doctor(i, waitingRoom, treatmentRooms)));
        }
    }

    private void createPatients() {
        StringBuilder names = new StringBuilder();
        for (int i = 1; i <= numPatients; i++) {
            String name = "P" + i;
            patientThreads.add(new Thread(new Patient(name, waitingRoom)));
            names.append(name).append(" ");
        }
        log("\nPatients arriving:");
        log(names.toString().trim());
    }

    private void startSimulation() {
        for (Thread doctor : doctorThreads) {
            doctor.start();
        }
        for (Thread patient : patientThreads) {
            patient.start();
            try {
                Thread.sleep(random.nextInt(MAX_ARRIVAL_DELAY_MS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void waitForCompletion() {
        try {
            for (Thread patient : patientThreads) {
                patient.join();
            }
            waitingRoom.shutdown(numDoctors);
            for (Thread doctor : doctorThreads) {
                doctor.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void finish() {
        log("All patients have been treated.");
        log("Simulation completed.");
    }
}
