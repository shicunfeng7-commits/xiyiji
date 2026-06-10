package com.xiyiji.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiyiji.modules.system.entity.ServiceTimeConfig;
import com.xiyiji.modules.system.mapper.ServiceTimeConfigMapper;
import com.xiyiji.modules.system.service.ServiceTimeConfigService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceTimeConfigServiceImpl extends ServiceImpl<ServiceTimeConfigMapper, ServiceTimeConfig> implements ServiceTimeConfigService {

    @Override
    public List<ServiceTimeConfig> getEnabledConfigs() {
        LambdaQueryWrapper<ServiceTimeConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceTimeConfig::getEnabled, true);
        return list(wrapper);
    }

    @Override
    public boolean updateConfig(ServiceTimeConfig config) {
        return updateById(config);
    }

    @Override
    public List<Integer> getAvailableHours() {
        List<ServiceTimeConfig> configs = getEnabledConfigs();
        List<Integer> hours = new ArrayList<>();
        for (ServiceTimeConfig config : configs) {
            int startHour = Integer.parseInt(config.getStartTime().split(":")[0]);
            int endHour = Integer.parseInt(config.getEndTime().split(":")[0]);
            for (int h = startHour; h < endHour; h++) {
                hours.add(h);
            }
        }
        return hours.stream().sorted().toList();
    }
}