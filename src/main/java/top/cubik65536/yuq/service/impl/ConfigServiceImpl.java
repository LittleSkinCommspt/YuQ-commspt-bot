package top.cubik65536.yuq.service.impl;

import com.icecreamqaq.yudb.jpa.annotation.Transactional;
import top.cubik65536.yuq.dao.ConfigDao;
import top.cubik65536.yuq.entity.ConfigEntity;
import top.cubik65536.yuq.service.ConfigService;

import javax.inject.Inject;
import java.util.List;

public class ConfigServiceImpl implements ConfigService {

    @Inject
    private ConfigDao configDao;

    @Override
    @Transactional
    public List<ConfigEntity> findAll() {
        return configDao.findAll();
    }

    @Override
    @Transactional
    public void save(ConfigEntity configEntity) {
        configDao.saveOrUpdate(configEntity);
    }

    @Override
    @Transactional
    public ConfigEntity findByType(String type) {
        return configDao.findByType(type);
    }
}
