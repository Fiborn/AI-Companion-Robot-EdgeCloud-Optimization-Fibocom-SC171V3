package com.ruoyi.netty.tcp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class CompanionCameraSnapshotService
{
    private static final long ONLINE_TIMEOUT_MS = 15000L;

    private final Map<String, CameraSnapshot> snapshots = new ConcurrentHashMap<>();

    private final Map<String, PhotoResult> photoResults = new ConcurrentHashMap<>();

    private final Map<String, CameraFrame> frames = new ConcurrentHashMap<>();

    private final Map<String, FrameRateStats> frameRateStats = new ConcurrentHashMap<>();

    public void update(String did, CameraSnapshot snapshot)
    {
        if (did == null || did.trim().isEmpty() || snapshot == null)
        {
            return;
        }
        snapshots.put(did, snapshot);
    }

    public CameraSnapshot getSnapshot(String did)
    {
        CameraSnapshot snapshot = snapshots.get(did);
        return snapshot == null ? CameraSnapshot.empty(did) : snapshot.copy();
    }

    public void updatePhoto(String did, boolean success, String path)
    {
        if (did == null || did.trim().isEmpty())
        {
            return;
        }
        photoResults.put(did, new PhotoResult(success, path, System.currentTimeMillis()));
    }

    public PhotoResult getPhotoResult(String did)
    {
        PhotoResult result = photoResults.get(did);
        return result == null ? new PhotoResult(false, "", 0L) : result;
    }

    public void updateFrame(String did, CameraFrame frame)
    {
        if (did == null || did.trim().isEmpty() || frame == null || frame.getJpeg() == null || frame.getJpeg().length == 0)
        {
            return;
        }
        frames.put(did, frame);
        double measuredFps = frameRateStats.computeIfAbsent(did, key -> new FrameRateStats()).record(frame.getReceivedAt());
        double displayFps = measuredFps > 0.0D ? measuredFps : frame.getFps();
        update(did, new CameraSnapshot(did, true, getSnapshot(did).getDevice(), frame.getWidth(), frame.getHeight(), displayFps, frame.getDeviceTimestamp(), "streaming"));
    }

    public CameraFrame getFrame(String did)
    {
        return frames.get(did);
    }

    public PhotoResult saveLatestFrame(String did)
    {
        CameraFrame frame = getFrame(did);
        if (frame == null || frame.getJpeg() == null || frame.getJpeg().length == 0)
        {
            PhotoResult result = new PhotoResult(false, "", System.currentTimeMillis());
            photoResults.put(did, result);
            return result;
        }

        Path dir = captureDirectory();
        String name = "capture-" + (did == null || did.trim().isEmpty() ? "unknown" : did) + "-"
            + new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date()) + ".jpg";
        Path file = dir.resolve(name);
        try
        {
            Files.createDirectories(dir);
            Files.write(file, frame.getJpeg());
            PhotoResult result = new PhotoResult(true, file.toString(), System.currentTimeMillis());
            photoResults.put(did, result);
            return result;
        }
        catch (IOException e)
        {
            PhotoResult result = new PhotoResult(false, e.getMessage(), System.currentTimeMillis());
            photoResults.put(did, result);
            return result;
        }
    }

    private Path captureDirectory()
    {
        String configured = System.getProperty("ruoyi.camera.captureDir");
        if (configured == null || configured.trim().isEmpty())
        {
            configured = System.getenv("RUOYI_CAMERA_CAPTURE_DIR");
        }
        if (configured != null && !configured.trim().isEmpty())
        {
            return Paths.get(configured.trim());
        }

        Path cwd = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        if (cwd.endsWith(Paths.get("iot-platform", "iot-platform")) && cwd.getParent() != null)
        {
            return cwd.getParent().resolve("camera-captures");
        }
        return cwd.resolve("camera-captures");
    }

    public static final class CameraSnapshot
    {
        private final String did;
        private final boolean connected;
        private final String device;
        private final int width;
        private final int height;
        private final double fps;
        private final long deviceTimestamp;
        private final String message;
        private final long lastSeen;

        public CameraSnapshot(String did, boolean connected, String device, int width, int height, double fps, long deviceTimestamp, String message)
        {
            this(did, connected, device, width, height, fps, deviceTimestamp, message, System.currentTimeMillis());
        }

        private CameraSnapshot(String did, boolean connected, String device, int width, int height, double fps, long deviceTimestamp, String message, long lastSeen)
        {
            this.did = did;
            this.connected = connected;
            this.device = device;
            this.width = width;
            this.height = height;
            this.fps = fps;
            this.deviceTimestamp = deviceTimestamp;
            this.message = message;
            this.lastSeen = lastSeen;
        }

        public static CameraSnapshot empty(String did)
        {
            return new CameraSnapshot(did, false, "", 0, 0, 0.0D, 0L, "waiting for camera report", 0L);
        }

        public CameraSnapshot copy()
        {
            return new CameraSnapshot(did, connected, device, width, height, fps, deviceTimestamp, message, lastSeen);
        }

        public String getDid() { return did; }

        public boolean isConnected() { return connected && isOnline(); }

        public boolean isOnline() { return lastSeen > 0 && System.currentTimeMillis() - lastSeen <= ONLINE_TIMEOUT_MS; }

        public String getDevice() { return device; }

        public int getWidth() { return width; }

        public int getHeight() { return height; }

        public double getFps() { return fps; }

        public long getDeviceTimestamp() { return deviceTimestamp; }

        public String getMessage() { return message; }

        public long getLastSeen() { return lastSeen; }
    }

    private static final class FrameRateStats
    {
        private long windowStart;
        private int frames;
        private double fps;

        synchronized double record(long receivedAt)
        {
            if (windowStart <= 0L)
            {
                windowStart = receivedAt;
            }
            frames++;
            long elapsed = receivedAt - windowStart;
            if (elapsed >= 1000L)
            {
                fps = frames * 1000.0D / elapsed;
                frames = 0;
                windowStart = receivedAt;
            }
            return fps;
        }
    }

    public static final class PhotoResult
    {
        private final boolean success;
        private final String path;
        private final long time;

        public PhotoResult(boolean success, String path, long time)
        {
            this.success = success;
            this.path = path;
            this.time = time;
        }

        public boolean isSuccess() { return success; }

        public String getPath() { return path; }

        public long getTime() { return time; }
    }

    public static final class CameraFrame
    {
        private final int width;
        private final int height;
        private final double fps;
        private final long deviceTimestamp;
        private final long receivedAt;
        private final byte[] jpeg;

        public CameraFrame(int width, int height, double fps, long deviceTimestamp, byte[] jpeg)
        {
            this.width = width;
            this.height = height;
            this.fps = fps;
            this.deviceTimestamp = deviceTimestamp;
            this.jpeg = jpeg;
            this.receivedAt = System.currentTimeMillis();
        }

        public int getWidth() { return width; }

        public int getHeight() { return height; }

        public double getFps() { return fps; }

        public long getDeviceTimestamp() { return deviceTimestamp; }

        public long getReceivedAt() { return receivedAt; }

        public byte[] getJpeg() { return jpeg; }
    }
}
