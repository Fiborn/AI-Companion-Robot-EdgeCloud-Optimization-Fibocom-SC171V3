package com.ruoyi.web.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.netty.tcp.CompanionCameraSnapshotService;
import com.ruoyi.netty.tcp.CompanionSensorSnapshotService;
import com.ruoyi.netty.tcp.DeviceTcpSendService;
import com.ruoyi.netty.tcp.NettyPacketType;
import com.ruoyi.web.domain.CompanionChatRequest;
import com.ruoyi.web.service.CompanionAiChatService;

/**
 * AI desktop companion dashboard APIs.
 */
@RestController
@RequestMapping("/companion")
public class CompanionController extends BaseController
{
    private final CompanionSensorSnapshotService sensorSnapshotService;

    private final CompanionCameraSnapshotService cameraSnapshotService;

    private final DeviceTcpSendService deviceTcpSendService;

    private final CompanionAiChatService companionAiChatService;

    public CompanionController(CompanionSensorSnapshotService sensorSnapshotService,
        CompanionCameraSnapshotService cameraSnapshotService,
        DeviceTcpSendService deviceTcpSendService,
        CompanionAiChatService companionAiChatService)
    {
        this.sensorSnapshotService = sensorSnapshotService;
        this.cameraSnapshotService = cameraSnapshotService;
        this.deviceTcpSendService = deviceTcpSendService;
        this.companionAiChatService = companionAiChatService;
    }

    @GetMapping("/sensors")
    public AjaxResult sensors(@RequestParam(defaultValue = "000001") String did)
    {
        return success(sensorSnapshotService.getSnapshot(did));
    }

    @GetMapping("/camera")
    public AjaxResult camera(@RequestParam(defaultValue = "000001") String did)
    {
        deviceTcpSendService.sendByDid(did, NettyPacketType.CAMERA_QUERY, new byte[0]);
        AjaxResult result = success(cameraSnapshotService.getSnapshot(did));
        result.put("photo", cameraSnapshotService.getPhotoResult(did));
        return result;
    }

    @GetMapping("/camera/photo")
    public AjaxResult takePhoto(@RequestParam(defaultValue = "000001") String did)
    {
        CompanionCameraSnapshotService.PhotoResult saved = cameraSnapshotService.saveLatestFrame(did);
        if (saved.isSuccess())
        {
            AjaxResult result = success("抓拍已保存到后台");
            result.put("photo", saved);
            return result;
        }

        if (!deviceTcpSendService.sendByDid(did, NettyPacketType.CAMERA_PHOTO, new byte[0]))
        {
            return error("暂无视频帧且设备离线，无法抓拍");
        }
        return success("暂无实时帧，已下发板端抓拍命令");
    }

    @PostMapping("/camera/config")
    public AjaxResult cameraConfig(@RequestParam(defaultValue = "000001") String did, @RequestBody Map<String, Object> config)
    {
        int width = clamp(intValue(config.get("width"), 480), 160, 3840);
        int height = clamp(intValue(config.get("height"), 360), 120, 2160);
        int fps = clamp(intValue(config.get("fps"), 30), 1, 60);
        int quality = clamp(intValue(config.get("quality"), 45), 20, 95);
        boolean streamEnabled = boolValue(config.get("streamEnabled"), true);

        ByteBuffer body = ByteBuffer.allocate(10);
        body.putShort((short) width);
        body.putShort((short) height);
        body.put((byte) fps);
        body.put((byte) quality);
        body.put((byte) (streamEnabled ? 1 : 0));
        body.put((byte) 0);
        body.putShort((short) 0);

        if (!deviceTcpSendService.sendByDid(did, NettyPacketType.CAMERA_CONFIG, body.array()))
        {
            return error("device offline, camera config not sent");
        }
        AjaxResult result = success("camera config sent");
        result.put("width", width);
        result.put("height", height);
        result.put("fps", fps);
        result.put("quality", quality);
        result.put("streamEnabled", streamEnabled);
        return result;
    }

    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody CompanionChatRequest request)
    {
        CompanionAiChatService.ChatResult chatResult = companionAiChatService.chat(request.getDid(), request.getMessage());
        AjaxResult result = success(chatResult.getAnswer());
        result.put("answer", chatResult.getAnswer());
        result.put("provider", chatResult.getProvider());
        result.put("model", chatResult.getModel());
        return result;
    }

    @Anonymous
    @GetMapping(value = "/camera/stream", produces = "multipart/x-mixed-replace;boundary=frame")
    public StreamingResponseBody cameraStream(@RequestParam(defaultValue = "000001") String did, HttpServletResponse response)
    {
        response.setContentType("multipart/x-mixed-replace; boundary=frame");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        return outputStream -> writeMjpegStream(outputStream, did);
    }

    @Anonymous
    @GetMapping(value = "/camera/frame", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> cameraFrame(@RequestParam(defaultValue = "000001") String did)
    {
        CompanionCameraSnapshotService.CameraFrame frame = cameraSnapshotService.getFrame(did);
        if (frame == null || frame.getJpeg() == null || frame.getJpeg().length == 0)
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .body(frame.getJpeg());
    }

    private void writeMjpegStream(OutputStream outputStream, String did) throws IOException
    {
        long lastFrameTime = -1L;
        while (true)
        {
            CompanionCameraSnapshotService.CameraFrame frame = cameraSnapshotService.getFrame(did);
            if (frame != null && frame.getJpeg() != null && frame.getJpeg().length > 0 && frame.getReceivedAt() != lastFrameTime)
            {
                byte[] jpeg = frame.getJpeg();
                outputStream.write(("--frame\r\nContent-Type: " + MediaType.IMAGE_JPEG_VALUE
                    + "\r\nContent-Length: " + jpeg.length + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                outputStream.write(jpeg);
                outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                lastFrameTime = frame.getReceivedAt();
            }

            try
            {
                Thread.sleep(33L);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private int intValue(Object value, int defaultValue)
    {
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        if (value != null)
        {
            try
            {
                return Integer.parseInt(String.valueOf(value));
            }
            catch (NumberFormatException ignored)
            {
            }
        }
        return defaultValue;
    }

    private boolean boolValue(Object value, boolean defaultValue)
    {
        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }
        if (value != null)
        {
            return Boolean.parseBoolean(String.valueOf(value));
        }
        return defaultValue;
    }

    private int clamp(int value, int min, int max)
    {
        return Math.max(min, Math.min(max, value));
    }
}
