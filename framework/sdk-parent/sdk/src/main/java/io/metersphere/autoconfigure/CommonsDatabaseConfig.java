package io.metersphere.autoconfigure;

import com.fit2cloud.quartz.anno.QuartzDataSource;
import com.github.pagehelper.PageInterceptor;
import com.zaxxer.hikari.HikariDataSource;
import io.metersphere.base.domain.AuthSource;
import io.metersphere.base.domain.FileContent;
import io.metersphere.base.domain.TestResource;
import io.metersphere.commons.utils.CompressUtils;
import io.metersphere.commons.utils.MybatisInterceptorConfig;
import io.metersphere.interceptor.MybatisInterceptor;
import io.metersphere.interceptor.UserDesensitizationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@MapperScan(basePackages = {"io.metersphere.base.mapper", "io.metersphere.xpack.mapper"}, sqlSessionFactoryRef = "sqlSessionFactory")
@EnableTransactionManagement
public class CommonsDatabaseConfig {

    @Bean
    @ConditionalOnMissingBean
    public PageInterceptor pageInterceptor() {
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("helperDialect", "mysql");
        properties.setProperty("rowBoundsWithCount", "true");
        properties.setProperty("reasonable", "true");
        properties.setProperty("offsetAsPageNum", "true");
        properties.setProperty("pageSizeZero", "true");
        pageInterceptor.setProperties(properties);
        return pageInterceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public MybatisInterceptor dbInterceptor() {
        MybatisInterceptor interceptor = new MybatisInterceptor();
        List<MybatisInterceptorConfig> configList = new ArrayList<>();
        configList.add(new MybatisInterceptorConfig(FileContent.class, "file", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(TestResource.class, "configuration"));
        configList.add(new MybatisInterceptorConfig(AuthSource.class, "configuration"));
        interceptor.setInterceptorConfigList(configList);
        return interceptor;
    }

    @Bean
    public UserDesensitizationInterceptor userDesensitizationInterceptor() {
        return new UserDesensitizationInterceptor();
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource dataSource(@Qualifier("dataSourceProperties") DataSourceProperties dataSourceProperties) {
        return DataSourceBuilder.create(dataSourceProperties.getClassLoader()).type(HikariDataSource.class)
                .driverClassName(dataSourceProperties.determineDriverClassName())
                .url(dataSourceProperties.determineUrl())
                .username(dataSourceProperties.determineUsername())
                .password(dataSourceProperties.determinePassword())
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.quartz.hikari")
    @QuartzDataSource
    @ConditionalOnProperty(prefix = "quartz", value = "enabled", havingValue = "true")
    public DataSource quartzDataSource(@Qualifier("quartzDataSourceProperties") DataSourceProperties quartzDataSourceProperties) {
        return DataSourceBuilder.create(quartzDataSourceProperties.getClassLoader()).type(HikariDataSource.class)
                .driverClassName(quartzDataSourceProperties.determineDriverClassName())
                .url(quartzDataSourceProperties.determineUrl())
                .username(quartzDataSourceProperties.determineUsername())
                .password(quartzDataSourceProperties.determinePassword())
                .build();
    }

    @Bean("dataSourceProperties")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("quartzDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.quartz")
    public DataSourceProperties quartzDataSourceProperties() {
        return new DataSourceProperties();
    }
}