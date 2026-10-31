public class Semaphore {
    
    private int permits;
    
    public Semaphore(int permits) {
        if (permits < 0) {
            throw new IllegalArgumentException("Permits cannot be negative");
        }
        this.permits = permits;
    }
    
    public synchronized void acquire() {
        while (permits <= 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        permits--;
    }
  
    public synchronized void release() {
        permits++;

        notify();
    }
    
    public synchronized boolean tryAcquire(long timeoutMillis) {
        if (permits > 0) {
            permits--;
            return true;
        }
        
        long startTime = System.currentTimeMillis();
        long remainingTime = timeoutMillis;
        
        while (permits == 0 && remainingTime > 0) {
            try {
                wait(remainingTime);
                
                long elapsedTime = System.currentTimeMillis() - startTime;
                remainingTime = timeoutMillis - elapsedTime;
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        if (permits > 0) {
            permits--;
            return true;
        } else {
            return false;
        }
    }
    
    public synchronized int availablePermits() {
        return permits;
    }
}