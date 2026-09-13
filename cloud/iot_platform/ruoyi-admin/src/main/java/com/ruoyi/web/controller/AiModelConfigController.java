package com.ruoyi.web.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.domain.AiModelConfig;
import com.ruoyi.web.service.IAiModelConfigService;

@RestController
@RequestMapping("/ai/model")
public class AiModelConfigController extends BaseController
{
    @Autowired
    private IAiModelConfigService aiModelConfigService;

    @PreAuthorize("@ss.hasPermi('ai:model:list')")
    @GetMapping("/list")
    public TableDataInfo list(AiModelConfig aiModelConfig)
    {
        startPage();
        List<AiModelConfig> list = aiModelConfigService.selectAiModelConfigList(aiModelConfig);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ai:model:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(aiModelConfigService.selectAiModelConfigById(id));
    }

    @PreAuthorize("@ss.hasPermi('ai:model:query')")
    @GetMapping("/active")
    public AjaxResult active()
    {
        return success(aiModelConfigService.selectActiveAiModelConfig());
    }

    @PreAuthorize("@ss.hasPermi('ai:model:add')")
    @Log(title = "AI模型配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AiModelConfig aiModelConfig)
    {
        aiModelConfig.setCreateBy(getUsername());
        return toAjax(aiModelConfigService.insertAiModelConfig(aiModelConfig));
    }

    @PreAuthorize("@ss.hasPermi('ai:model:edit')")
    @Log(title = "AI模型配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AiModelConfig aiModelConfig)
    {
        aiModelConfig.setUpdateBy(getUsername());
        return toAjax(aiModelConfigService.updateAiModelConfig(aiModelConfig));
    }

    @PreAuthorize("@ss.hasPermi('ai:model:edit')")
    @Log(title = "AI模型切换", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/active")
    public AjaxResult activate(@PathVariable Long id)
    {
        return toAjax(aiModelConfigService.activateAiModelConfig(id));
    }

    @PreAuthorize("@ss.hasPermi('ai:model:remove')")
    @Log(title = "AI模型配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(aiModelConfigService.deleteAiModelConfigByIds(ids));
    }
}
