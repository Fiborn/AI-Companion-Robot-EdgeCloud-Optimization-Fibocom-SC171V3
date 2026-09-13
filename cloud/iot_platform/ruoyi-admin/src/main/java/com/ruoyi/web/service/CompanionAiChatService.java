package com.ruoyi.web.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.netty.tcp.CompanionCameraSnapshotService;
import com.ruoyi.netty.tcp.CompanionSensorSnapshotService;
import com.ruoyi.netty.tcp.CompanionSensorSnapshotService.SensorValue;
import com.ruoyi.web.domain.AiModelConfig;

@Service
public class CompanionAiChatService
{
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final IAiModelConfigService aiModelConfigService;

    private final CompanionSensorSnapshotService sensorSnapshotService;

    private final CompanionCameraSnapshotService cameraSnapshotService;

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    public CompanionAiChatService(IAiModelConfigService aiModelConfigService,
        CompanionSensorSnapshotService sensorSnapshotService,
        CompanionCameraSnapshotService cameraSnapshotService)
    {
        this.aiModelConfigService = aiModelConfigService;
        this.sensorSnapshotService = sensorSnapshotService;
        this.cameraSnapshotService = cameraSnapshotService;
    }

    public ChatResult chat(String did, String message)
    {
        String deviceId = did == null || did.trim().isEmpty() ? "000001" : did.trim();
        AiModelConfig config = aiModelConfigService.selectActiveAiModelConfig();
        if (config == null)
        {
            throw new ServiceException("还没有启用AI模型配置，请先在 AI配置 -> 模型配置 中设置当前模型");
        }

        JSONObject body = new JSONObject();
        body.put("model", config.getModelName());
        body.put("temperature", config.getTemperature());
        body.put("max_tokens", config.getMaxTokens());
        body.put("stream", false);
        body.put("messages", buildMessages(config, deviceId, message));

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(config.getBaseUrl()))
            .timeout(REQUEST_TIMEOUT)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()));

        if (config.getApiKey() != null && !config.getApiKey().trim().isEmpty())
        {
            builder.header("Authorization", "Bearer " + config.getApiKey().trim());
        }

        try
        {
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                throw new ServiceException("AI云端调用失败，HTTP " + response.statusCode() + ": " + trim(response.body(), 300));
            }
            String answer = parseOpenAiCompatibleAnswer(response.body());
            if (answer == null || answer.trim().isEmpty())
            {
                throw new ServiceException("AI云端返回为空，请检查模型配置");
            }
            return new ChatResult(answer.trim(), config.getProvider(), config.getModelName());
        }
        catch (IOException e)
        {
            throw new ServiceException("AI云端连接失败：" + e.getMessage());
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("AI云端调用被中断");
        }
    }

    private JSONArray buildMessages(AiModelConfig config, String did, String message)
    {
        JSONArray messages = new JSONArray();
        String systemPrompt = config.getSystemPrompt();
        if (systemPrompt == null || systemPrompt.trim().isEmpty())
        {
            systemPrompt = "你是SC171边缘AIoT桌面机器人的中文助手。回答要简洁、可靠，并优先结合板端实时传感器和摄像头状态。";
        }
        messages.add(message("system", systemPrompt + "\n\n" + buildDeviceContext(did)));
        messages.add(message("user", message));
        return messages;
    }

    private String buildDeviceContext(String did)
    {
        StringBuilder context = new StringBuilder();
        CompanionSensorSnapshotService.DeviceSensorSnapshot sensorSnapshot = sensorSnapshotService.getSnapshot(did);
        context.append("设备ID：").append(did).append('\n');
        context.append("传感器在线：").append(sensorSnapshot.isOnline() ? "是" : "否").append('\n');

        List<SensorValue> sensors = sensorSnapshot.getSensors();
        if (sensors.isEmpty())
        {
            context.append("传感器数据：暂无实时上报\n");
        }
        else
        {
            context.append("传感器数据：");
            List<String> parts = new ArrayList<>();
            for (SensorValue sensor : sensors)
            {
                parts.add(sensor.getName() + "=" + sensor.getValue() + sensor.getUnit());
            }
            context.append(String.join("，", parts)).append('\n');
        }

        CompanionCameraSnapshotService.CameraSnapshot cameraSnapshot = cameraSnapshotService.getSnapshot(did);
        context.append("摄像头在线：").append(cameraSnapshot.isConnected() ? "是" : "否");
        if (cameraSnapshot.isConnected())
        {
            context.append("，分辨率=").append(cameraSnapshot.getWidth()).append("x").append(cameraSnapshot.getHeight())
                .append("，FPS=").append(String.format("%.1f", cameraSnapshot.getFps()));
        }
        context.append('\n');
        return context.toString();
    }

    private JSONObject message(String role, String content)
    {
        JSONObject item = new JSONObject();
        item.put("role", role);
        item.put("content", content);
        return item;
    }

    private String parseOpenAiCompatibleAnswer(String body)
    {
        JSONObject json = JSONObject.parseObject(body);
        JSONArray choices = json.getJSONArray("choices");
        if (choices == null || choices.isEmpty())
        {
            return json.getString("content");
        }
        JSONObject first = choices.getJSONObject(0);
        JSONObject message = first.getJSONObject("message");
        if (message != null)
        {
            return message.getString("content");
        }
        return first.getString("text");
    }

    private String trim(String text, int max)
    {
        if (text == null)
        {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max);
    }

    public static final class ChatResult
    {
        private final String answer;

        private final String provider;

        private final String model;

        public ChatResult(String answer, String provider, String model)
        {
            this.answer = answer;
            this.provider = provider;
            this.model = model;
        }

        public String getAnswer()
        {
            return answer;
        }

        public String getProvider()
        {
            return provider;
        }

        public String getModel()
        {
            return model;
        }
    }
}
