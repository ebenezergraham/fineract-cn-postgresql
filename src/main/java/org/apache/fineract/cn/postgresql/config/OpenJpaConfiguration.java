/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.cn.postgresql.config;

import com.jolbox.bonecp.BoneCPDataSource;
import org.apache.fineract.cn.lang.ApplicationName;
import org.apache.fineract.cn.lang.config.EnableApplicationName;
import org.apache.fineract.cn.postgresql.domain.FlywayFactoryBean;
import org.apache.fineract.cn.postgresql.util.JdbcUrlBuilder;
import org.apache.fineract.cn.postgresql.util.PostgreSQLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.OpenJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings("WeakerAccess")
@Configuration
@EnableTransactionManagement
public class OpenJpaConfiguration extends JpaBaseConfiguration {
	
	public OpenJpaConfiguration(DataSource dataSource, JpaProperties properties, ObjectProvider<JtaTransactionManager> jtaTransactionManagerProvider) {
		super(dataSource, properties, jtaTransactionManagerProvider);
	}
	
	@Bean(name = PostgreSQLConstants.LOGGER_NAME)
	public Logger logger() {
		return LoggerFactory.getLogger(PostgreSQLConstants.LOGGER_NAME);
	}
	
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(@SuppressWarnings("SpringJavaAutowiringInspection") final DataSource dataSource) {
		final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setPersistenceUnitName("metaPU");
		em.setDataSource(dataSource);
		em.setPackagesToScan("org.apache.fineract.cn.**.repository");
		
		em.setJpaVendorAdapter(createJpaVendorAdapter());
		em.setJpaPropertyMap(getVendorProperties());
		return em;
	}
	
	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(emf);
		return transactionManager;
	}
	
	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
		return new PersistenceExceptionTranslationPostProcessor();
	}
	
	@Override
	protected AbstractJpaVendorAdapter createJpaVendorAdapter() {
		return new OpenJpaVendorAdapter();
	}
	
	@Override
	protected Map<String, Object> getVendorProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("openjpa.jdbc.DBDictionary", "org.apache.openjpa.jdbc.sql.PostgresDictionary");
		properties.put("openjpa.RuntimeUnenhancedClasses", "supported"); // set to unsupported for production
		properties.put("openjpa.DynamicEnhancementAgent", "true");
		properties.put("openjpa.Log", "DefaultLevel=TRACE, Tool=INFO, SQL=TRACE, Runtime=TRACE"); // set to unsupported for production
		return properties;
	}
}
