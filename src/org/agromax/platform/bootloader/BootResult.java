package org.agromax.platform.bootloader;

/**
 * @author Anurag Gautam
 */
public class BootResult {
    private float bootTime;
    private boolean success;

    public BootResult(float bootTime, boolean success) {
        this.bootTime = bootTime;
        this.success = success;
    }

    public float getBootTime() {
        return bootTime;
    }

    public boolean isBootSuccessful() {
        return success;
    }
}
