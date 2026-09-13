package com.ruoyi.web.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ruoyi.web.domain.AiModelConfig;

@Service
public class VoiceServerConfigService
{
    private static final Logger log = LoggerFactory.getLogger(VoiceServerConfigService.class);

    /**
     * AI2 语音服务的 .env 路径。<br>
     * 默认值 {@code ../AI2/.env} 以「应用启动目录（iot-platform/iot-platform）」为基准，
     * 指向工程根目录下的 AI2；也可用 application.yml 的 {@code ai2.voice.env-path}
     * 或环境变量 {@code AI2_ENV_PATH} 覆盖为绝对路径。
     */
    @Value("${ai2.voice.env-path:../AI2/.env}")
    private String envPath;

    public void syncActiveModel(AiModelConfig config)
    {
        if (config == null)
        {
            return;
        }
        Path path = Path.of(envPath);
        try
        {
            Map<String, String> updates = new LinkedHashMap<>();
            updates.put("LLM_PROVIDER", "openai_compatible");
            updates.put("OPENAI_BASE_URL", normalizeBaseUrl(config.getBaseUrl()));
            updates.put("OPENAI_MODEL", valueOrDefault(config.getModelName(), "deepseek-chat"));
            updates.put("OPENAI_API_KEY", valueOrDefault(config.getApiKey(), "local"));
            updates.put("LLM_SYSTEM_PROMPT", normalizeLine(config.getSystemPrompt()));
            updates.put("LLM_MAX_TOKENS", String.valueOf(config.getMaxTokens() == null ? 1024 : config.getMaxTokens()));

            writeEnv(path, updates);
            log.info("AI2 voice server env synced: {}", path);
        }
        catch (Exception e)
        {
            log.warn("AI2 voice server env sync failed: {}", e.getMessage(), e);
        }
    }

    private void writeEnv(Path path, Map<String, String> updates) throws IOException
    {
        List<String> lines = Files.exists(path)
            ? Files.readAllLines(path, StandardCharsets.UTF_8)
            : new ArrayList<>();

        Map<String, Integer> indexByKey = new LinkedHashMap<>();
        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("#") || !line.contains("="))
            {
                continue;
            }
            String key = line.substring(0, line.indexOf('=')).trim();
            indexByKey.put(key, i);
        }

        for (Map.Entry<String, String> entry : updates.entrySet())
        {
            String envLine = entry.getKey() + "=" + sanitizeEnvValue(entry.getValue());
            Integer index = indexByKey.get(entry.getKey());
            if (index == null)
            {
                lines.add(envLine);
            }
            else
            {
                lines.set(index, envLine);
            }
        }

        Files.createDirectories(path.getParent());
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    private String normalizeBaseUrl(String baseUrl)
    {
        String value = valueOrDefault(baseUrl, "http://127.0.0.1:18080/v1").trim();
        if (value.endsWith("/chat/completions"))
        {
            value = value.substring(0, value.length() - "/chat/completions".length());
        }
        return value;
    }

    private String normalizeLine(String value)
    {
        return valueOrDefault(value,
            "你是SC171边缘AIoT桌面机器人的中文语音助手，回答要简短、自然、适合语音播报。")
            .replace('\r', ' ')
            .replace('\n', ' ')
            .trim();
    }

    private String valueOrDefault(String value, String defaultValue)
    {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    private String sanitizeEnvValue(String value)
    {
        return value == null ? "" : value.replace("\r", " ").replace("\n", " ").trim();
    }
}
