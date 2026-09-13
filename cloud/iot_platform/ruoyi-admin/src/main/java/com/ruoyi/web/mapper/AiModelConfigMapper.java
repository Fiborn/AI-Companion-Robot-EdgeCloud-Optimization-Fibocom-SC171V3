package com.ruoyi.web.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.web.domain.AiModelConfig;

public interface AiModelConfigMapper
{
    AiModelConfig selectAiModelConfigById(Long id);

    AiModelConfig selectAiModelConfigByName(@Param("name") String name);

    AiModelConfig selectActiveAiModelConfig();

    List<AiModelConfig> selectAiModelConfigList(AiModelConfig aiModelConfig);

    int insertAiModelConfig(AiModelConfig aiModelConfig);

    int updateAiModelConfig(AiModelConfig aiModelConfig);

    int clearActiveAiModelConfig();

    int setActiveAiModelConfig(@Param("id") Long id);

    int deleteAiModelConfigById(Long id);

    int deleteAiModelConfigByIds(Long[] ids);
}
