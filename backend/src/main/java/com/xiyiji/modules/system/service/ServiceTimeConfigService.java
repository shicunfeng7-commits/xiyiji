package com.xiyiji.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiyiji.modules.system.entity.ServiceTimeConfig;

import java.util.List;

public interface ServiceTimeConfigService extends IService<ServiceTimeConfig> {
    List<ServiceTimeConfig> getEnabledConfigs();
    boolean updateConfig(ServiceTimeConfig config);
    List<Integer> getAvailableHours();
}