package com.ruoyi.web.controller.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.file.MimeTypeUtils;
import com.ruoyi.system.service.ISysUserService;

@Anonymous
@RestController
public class SysAvatarController
{
    private static final Logger log = LoggerFactory.getLogger(SysAvatarController.class);

    @Autowired
    private ISysUserService userService;

    @PostMapping("/api/set-avatar")
    public AjaxResult setAvatar(@RequestParam("file") MultipartFile file)
    {
        if (file.isEmpty())
        {
            return AjaxResult.error("文件为空");
        }

        try
        {
            String avatarPath = RuoYiConfig.getAvatarPath();
            log.debug("头像保存路径: {}", avatarPath);
            
            String avatar = FileUploadUtils.upload(avatarPath, file, MimeTypeUtils.IMAGE_EXTENSION, true);
            log.debug("头像上传成功，返回路径: {}", avatar);
            
            boolean updateResult = userService.updateUserAvatar(1L, avatar);
            log.debug("更新用户头像结果: {}", updateResult);
            
            if (updateResult)
            {
                String oldAvatar = userService.selectUserById(1L).getAvatar();
                if (StringUtils.isNotEmpty(oldAvatar) && !oldAvatar.equals(avatar))
                {
                    try
                    {
                        FileUtils.deleteFile(RuoYiConfig.getProfile() + FileUtils.stripPrefix(oldAvatar));
                        log.debug("删除旧头像文件成功: {}", oldAvatar);
                    }
                    catch (Exception e)
                    {
                        log.warn("删除旧头像文件失败: {}", oldAvatar);
                    }
                }
                AjaxResult ajax = AjaxResult.success();
                ajax.put("imgUrl", avatar);
                return ajax;
            }
            else
            {
                return AjaxResult.error("更新头像失败");
            }
        }
        catch (Exception e)
        {
            log.error("上传头像失败", e);
            return AjaxResult.error("上传图片异常: " + e.getMessage());
        }
    }
}