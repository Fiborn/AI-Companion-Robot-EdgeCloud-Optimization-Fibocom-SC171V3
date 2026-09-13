package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.AiModelConfig;

public interface IAiModelConfigService
{
    AiModelConfig selectAiModelConfigById(Long id);

    AiModelConfig selectActiveAiModelConfig();

    List<AiModelConfig> selectAiModelConfigList(AiModelConfig aiModelConfig);

    int insertAiModelConfig(AiModelConfig aiModelConfig);

    int updateAiModelConfig(AiModelConfig aiModelConfig);

    int activateAiModelConfig(Long id);

    int deleteAiModelConfigByIds(Long[] ids);

    int deleteAiModelConfigById(Long id);
}
