package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.web.domain.AiModelConfig;
import com.ruoyi.web.mapper.AiModelConfigMapper;
import com.ruoyi.web.service.IAiModelConfigService;
import com.ruoyi.web.service.VoiceServerConfigService;

@Service
public class AiModelConfigServiceImpl implements IAiModelConfigService
{
    @Autowired
    private AiModelConfigMapper aiModelConfigMapper;

    @Autowired
    private VoiceServerConfigService voiceServerConfigService;

    @Override
    public AiModelConfig selectAiModelConfigById(Long id)
    {
        return aiModelConfigMapper.selectAiModelConfigById(id);
    }

    @Override
    public AiModelConfig selectActiveAiModelConfig()
    {
        return aiModelConfigMapper.selectActiveAiModelConfig();
    }

    @Override
    public List<AiModelConfig> selectAiModelConfigList(AiModelConfig aiModelConfig)
    {
        return aiModelConfigMapper.selectAiModelConfigList(aiModelConfig);
    }

    @Override
    @Transactional
    public int insertAiModelConfig(AiModelConfig aiModelConfig)
    {
        checkNameUnique(aiModelConfig);
        fillDefaults(aiModelConfig);
        int rows = aiModelConfigMapper.insertAiModelConfig(aiModelConfig);
        if (Integer.valueOf(1).equals(aiModelConfig.getActive()))
        {
            activateAiModelConfig(aiModelConfig.getId());
        }
        syncVoiceServerIfActive(aiModelConfig);
        return rows;
    }

    @Override
    @Transactional
    public int updateAiModelConfig(AiModelConfig aiModelConfig)
    {
        checkNameUnique(aiModelConfig);
        fillDefaults(aiModelConfig);
        int rows = aiModelConfigMapper.updateAiModelConfig(aiModelConfig);
        if (Integer.valueOf(1).equals(aiModelConfig.getActive()))
        {
            activateAiModelConfig(aiModelConfig.getId());
        }
        syncVoiceServerIfActive(aiModelConfig);
        return rows;
    }

    @Override
    @Transactional
    public int activateAiModelConfig(Long id)
    {
        AiModelConfig config = aiModelConfigMapper.selectAiModelConfigById(id);
        if (config == null)
        {
            throw new ServiceException("AI模型配置不存在");
        }
        if (!Integer.valueOf(1).equals(config.getEnabled()))
        {
            throw new ServiceException("该AI模型配置未启用，不能切换");
        }
        aiModelConfigMapper.clearActiveAiModelConfig();
        int rows = aiModelConfigMapper.setActiveAiModelConfig(id);
        voiceServerConfigService.syncActiveModel(config);
        return rows;
    }

    @Override
    public int deleteAiModelConfigByIds(Long[] ids)
    {
        return aiModelConfigMapper.deleteAiModelConfigByIds(ids);
    }

    @Override
    public int deleteAiModelConfigById(Long id)
    {
        return aiModelConfigMapper.deleteAiModelConfigById(id);
    }

    private void checkNameUnique(AiModelConfig aiModelConfig)
    {
        AiModelConfig byName = aiModelConfigMapper.selectAiModelConfigByName(aiModelConfig.getName());
        if (byName != null && (aiModelConfig.getId() == null || !byName.getId().equals(aiModelConfig.getId())))
        {
            throw new ServiceException("AI模型配置名称已存在，请更换");
        }
    }

    private void fillDefaults(AiModelConfig aiModelConfig)
    {
        if (aiModelConfig.getApiType() == null || aiModelConfig.getApiType().isEmpty())
        {
            aiModelConfig.setApiType("openai");
        }
        if (aiModelConfig.getEnabled() == null)
        {
            aiModelConfig.setEnabled(1);
        }
        if (aiModelConfig.getActive() == null)
        {
            aiModelConfig.setActive(0);
        }
        if (aiModelConfig.getTemperature() == null)
        {
            aiModelConfig.setTemperature(0.7D);
        }
        if (aiModelConfig.getMaxTokens() == null)
        {
            aiModelConfig.setMaxTokens(1024);
        }
    }

    private void syncVoiceServerIfActive(AiModelConfig aiModelConfig)
    {
        if (Integer.valueOf(1).equals(aiModelConfig.getActive()))
        {
            voiceServerConfigService.syncActiveModel(aiModelConfigMapper.selectAiModelConfigById(aiModelConfig.getId()));
        }
    }
}
