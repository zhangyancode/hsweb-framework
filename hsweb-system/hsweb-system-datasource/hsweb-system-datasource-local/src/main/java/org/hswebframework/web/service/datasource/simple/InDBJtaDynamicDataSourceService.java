package org.hswebframework.web.service.datasource.simple;

import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.datasource.DatabaseType;
import org.hswebframework.web.datasource.DynamicDataSource;
import org.hswebframework.web.datasource.config.DynamicDataSourceConfigRepository;
import org.hswebframework.web.datasource.jta.AtomikosDataSourceConfig;
import org.hswebframework.web.datasource.jta.JtaDynamicDataSourceService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class InDBJtaDynamicDataSourceService extends JtaDynamicDataSourceService {

    static AtomikosDataSourceConfig convert(InDBDynamicDataSourceConfig entity) {
        if (null == entity) {
            return null;
        }
        Map<String, Object> config = entity.getProperties();
        if (config == null) {
            return null;
        }
        Properties xaProperties = new Properties();
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("xaProperties.")) {
                xaProperties.put(key.substring("xaProperties.".length()), entry.getValue());
            }
        }
        AtomikosDataSourceConfig target = FastBeanCopier.copy(config, new AtomikosDataSourceConfig() {
            private static final long serialVersionUID = -2704649332301331803L;

            @Override
            public int hashCode() {
                return entity.hashCode();
            }

            @Override
            public boolean equals(Object o) {
                return o instanceof AtomikosDataSourceConfig && hashCode() == o.hashCode();
            }
        });
        target.setId(entity.getId());
        target.setName(entity.getName());
        target.setDescribe(entity.getDescribe());
        target.setXaProperties(xaProperties);
        target.setDatabaseType(Optional.ofNullable(config.get("databaseType"))
                .map(String::valueOf)
                .map(DatabaseType::valueOf)
                .orElse(null));
        return target;
    }

    public InDBJtaDynamicDataSourceService(DynamicDataSourceConfigRepository<InDBDynamicDataSourceConfig> repository,
                                           DynamicDataSource defaultDataSource) {

        super(new DynamicDataSourceConfigRepository<AtomikosDataSourceConfig>() {
            @Override
            public List<AtomikosDataSourceConfig> findAll() {
                return repository.findAll().stream()
                        .map(InDBJtaDynamicDataSourceService::convert)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }

            @Override
            public AtomikosDataSourceConfig findById(String dataSourceId) {
                return convert(repository.findById(dataSourceId));
            }

            @Override
            public AtomikosDataSourceConfig add(AtomikosDataSourceConfig config) {
                throw new UnsupportedOperationException("不支持添加数据源配置");
            }

            @Override
            public AtomikosDataSourceConfig remove(String dataSourceId) {
                throw new UnsupportedOperationException("不支持删除数据源配置");
            }
        }, defaultDataSource);
    }

}
