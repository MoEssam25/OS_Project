import java.util.Random;

public class Doctor implements Runnable {

    private final int doctorId;
    private final BoundedBuffer waitingRoom;
    private final Semaphore treatmentRooms;
    private final Random random = new Random();

    public Doctor(int doctorId, BoundedBuffer waitingRoom,
                  Semaphore treatmentRooms) {
        this.doctorId = doctorId;
        this.waitingRoom = waitingRoom;
        this.treatmentRooms = treatmentRooms;
    }

    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()) {

            Patient patient = waitingRoom.take();

            if (patient == null) {
                break;
            }

            System.out.println(
                "Doctor " + doctorId + " is assigned to "
                + patient.getName() + "."
            );

            System.out.println(
                "Doctor " + doctorId
                + " is trying to acquire a treatment room."
            );

            boolean acquired = treatmentRooms.tryAcquire(3000);

            if (!acquired) {

                System.out.println(
                    "Doctor " + doctorId
                    + " could not acquire a treatment room."
                );

                System.out.println(
                    "Doctor " + doctorId
                    + " is sending " + patient.getName()
                    + " back to the waiting room."
                );

                waitingRoom.enter(patient);
                continue;
            }

            System.out.println(
                "Doctor " + doctorId + " acquired a treatment room."
            );

            try {
                System.out.println(
                    "Doctor " + doctorId + " begins treating "
                    + patient.getName() + "."
                );

                int treatmentTime = 1000 + random.nextInt(4000);
                Thread.sleep(treatmentTime);

                System.out.println(
                    "Doctor " + doctorId + " finished treating "
                    + patient.getName() + "."
                );

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;

            } finally {
                treatmentRooms.release();

                System.out.println(
                    "Doctor " + doctorId
                    + " released the treatment room."
                );
            }

            System.out.println(
                "Doctor " + doctorId + " is now available."
            );

            System.out.println(patient.getName() + " left.");
        }
    }
}
