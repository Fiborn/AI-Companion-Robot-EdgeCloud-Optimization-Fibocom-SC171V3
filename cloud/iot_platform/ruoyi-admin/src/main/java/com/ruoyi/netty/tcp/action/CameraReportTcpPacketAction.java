package com.ruoyi.netty.tcp.action;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ruoyi.netty.tcp.CompanionCameraSnapshotService;
import com.ruoyi.netty.tcp.NettyPacketStatusCode;
import com.ruoyi.netty.tcp.NettyPacketType;
import com.ruoyi.netty.tcp.TcpBinaryPacket;
import com.ruoyi.netty.tcp.TcpBinaryPacketCodec;
import com.ruoyi.netty.tcp.TcpChannelAttributes;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

@Component
@HandlesNettyPacketType({ NettyPacketType.CAMERA_REPORT, NettyPacketType.CAMERA_PHOTO_RESP, NettyPacketType.CAMERA_FRAME,
    NettyPacketType.CAMERA_CONFIG_RESP })
public class CameraReportTcpPacketAction implements TcpPacketAction
{
    private static final Logger log = LoggerFactory.getLogger(CameraReportTcpPacketAction.class);

    private final CompanionCameraSnapshotService cameraSnapshotService;

    public CameraReportTcpPacketAction(CompanionCameraSnapshotService cameraSnapshotService)
    {
        this.cameraSnapshotService = cameraSnapshotService;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, TcpBinaryPacket packet)
    {
        String did = ctx.channel().attr(TcpChannelAttributes.DEVICE_DID).get();
        if (did == null || did.trim().isEmpty())
        {
            writeStatus(ctx, NettyPacketType.CAMERA_REPORT_RESP, NettyPacketStatusCode.ERROR_LOGIN);
            return;
        }

        if (packet.getPacketType() == NettyPacketType.CAMERA_PHOTO_RESP)
        {
            handlePhotoResp(did, packet.getBody());
            return;
        }
        if (packet.getPacketType() == NettyPacketType.CAMERA_CONFIG_RESP)
        {
            handleConfigResp(did, packet.getBody());
            return;
        }
        if (packet.getPacketType() == NettyPacketType.CAMERA_FRAME)
        {
            byte status = handleCameraFrame(did, packet.getBody());
            writeStatus(ctx, NettyPacketType.CAMERA_FRAME_RESP, status);
            return;
        }

        byte status = handleCameraReport(did, packet.getBody());
        writeStatus(ctx, NettyPacketType.CAMERA_REPORT_RESP, status);
    }

    private byte handleCameraReport(String did, ByteBuf body)
    {
        if (body.readableBytes() < 14)
        {
            return NettyPacketStatusCode.ERROR_Tag;
        }

        int connected = body.readUnsignedByte();
        String device = readString(body);
        if (device == null || body.readableBytes() < 14)
        {
            return NettyPacketStatusCode.ERROR_Tag;
        }

        int width = body.readUnsignedShort();
        int height = body.readUnsignedShort();
        double fps = body.readFloat();
        long timestamp = body.readUnsignedInt();
        String message = readString(body);
        if (message == null)
        {
            return NettyPacketStatusCode.ERROR_Tag;
        }

        cameraSnapshotService.update(did, new CompanionCameraSnapshotService.CameraSnapshot(
            did, connected == 1, device, width, height, fps, timestamp, message));
        log.debug("摄像头状态上报 did={}, connected={}, device={}", did, connected, device);
        return NettyPacketStatusCode.SUCCESS;
    }

    private void handlePhotoResp(String did, ByteBuf body)
    {
        if (body.readableBytes() < 1)
        {
            cameraSnapshotService.updatePhoto(did, false, "");
            return;
        }
        int status = body.readUnsignedByte();
        String path = readString(body);
        cameraSnapshotService.updatePhoto(did, status == NettyPacketStatusCode.SUCCESS, path == null ? "" : path);
        log.info("摄像头抓拍回执 did={}, status=0x{}, path={}", did, String.format("%02x", status), path);
    }

    private byte handleCameraFrame(String did, ByteBuf body)
    {
        if (body.readableBytes() < 19)
        {
            return NettyPacketStatusCode.ERROR_Tag;
        }

        int totalLength = body.readUnsignedShort();
        body.readUnsignedByte();
        int width = body.readUnsignedShort();
        int height = body.readUnsignedShort();
        double fps = body.readFloat();
        long timestamp = body.readUnsignedInt();
        long jpegLength = body.readUnsignedInt();

        if (totalLength <= 0 || jpegLength <= 0 || jpegLength > body.readableBytes())
        {
            return NettyPacketStatusCode.ERROR_Tag;
        }

        byte[] jpeg = new byte[(int) jpegLength];
        body.readBytes(jpeg);
        cameraSnapshotService.updateFrame(did, new CompanionCameraSnapshotService.CameraFrame(width, height, fps, timestamp, jpeg));
        return NettyPacketStatusCode.SUCCESS;
    }

    private void handleConfigResp(String did, ByteBuf body)
    {
        int status = body.readableBytes() < 1 ? NettyPacketStatusCode.ERROR_Tag : body.readUnsignedByte();
        log.info("camera config resp did={}, status=0x{}", did, String.format("%02x", status));
    }

    private static void writeStatus(ChannelHandlerContext ctx, int packetType, byte status)
    {
        ctx.writeAndFlush(TcpBinaryPacketCodec.encode(ctx.alloc(), packetType, new byte[] { status }));
    }

    private static String readString(ByteBuf body)
    {
        if (body.readableBytes() < 2)
        {
            return null;
        }
        int len = body.readUnsignedShort();
        if (len < 0 || body.readableBytes() < len)
        {
            return null;
        }
        byte[] raw = new byte[len];
        body.readBytes(raw);
        return new String(raw, StandardCharsets.UTF_8);
    }
}
