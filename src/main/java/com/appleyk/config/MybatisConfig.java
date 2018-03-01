package com.appleyk.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.github.pagehelper.PageInterceptor;

@Configuration
@EnableTransactionManagement//开启事务
@EnableConfigurationProperties(DataSourceProperties.class)
//扫描一切和Mapper有关的bean，因此，下面对整个项目进行"全身"扫描
@MapperScan("com.appleyk")
public class MybatisConfig {

	
	@Bean(name = "dataSource")
	//Spring 允许我们通过 @Qualifier注释指定注入 Bean 的名称
    @Qualifier(value = "dataSource")
    @ConfigurationProperties(prefix="spring.datasource")
	@Primary
    public DataSource dataSource()
    {
		return DataSourceBuilder.create().build();
    }

	//创建SqlSessionFactory
	@Bean(name = "sqlSessionFactory")
	public SqlSessionFactory sqlSessionFactoryBean(@Qualifier("dataSource") DataSource dataSource){
		
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
	
		//1.设置数据源
		bean.setDataSource(dataSource);
		//2.给包中的类注册别名，注册后可以直接使用类名，而不用使用全限定的类名（就是不用包含包名）
		bean.setTypeAliasesPackage("com.appleyk.database");
			
		// 设置MyBatis分页插件 【PageHelper 5.0.1设置方法】
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("helperDialect", "mysql");
        properties.setProperty("offsetAsPageNum", "true");
        properties.setProperty("rowBoundsWithCount", "true");
        pageInterceptor.setProperties(properties);
        
        //添加插件
        bean.setPlugins(new Interceptor[]{pageInterceptor});

        //添加XML目录,进行Mapper.xml扫描
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
        	//项目中的xxxMapper.xml位于包com.appleyk.mapepr下面
            bean.setMapperLocations(resolver.getResources("classpath*:com/appleyk/mapepr/*.xml"));        
            return bean.getObject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }       
	} 
	
	//创建SqlSessionTemplate
	@Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
		
	
	@Bean(name = "transactionManager")
    @Primary
    public DataSourceTransactionManager testTransactionManager(@Qualifier("dataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }	
}
