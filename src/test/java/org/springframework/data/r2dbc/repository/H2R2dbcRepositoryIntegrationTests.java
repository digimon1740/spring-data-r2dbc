/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.r2dbc.repository;

import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactory;
import org.springframework.data.r2dbc.testing.H2TestSupport;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests for {@link LegoSetRepository} using {@link R2dbcRepositoryFactory} against H2.
 *
 * @author Mark Paluch
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
public class H2R2dbcRepositoryIntegrationTests extends AbstractR2dbcRepositoryIntegrationTests {

	@Autowired private H2LegoSetRepository repository;

	@Configuration
	@EnableR2dbcRepositories(considerNestedRepositories = true,
			includeFilters = @Filter(classes = H2LegoSetRepository.class, type = FilterType.ASSIGNABLE_TYPE))
	static class IntegrationTestConfiguration extends AbstractR2dbcConfiguration {

		@Bean
		@Override
		public ConnectionFactory connectionFactory() {
			return H2TestSupport.createConnectionFactory();
		}
	}

	@Override
	protected DataSource createDataSource() {
		return H2TestSupport.createDataSource();
	}

	@Override
	protected ConnectionFactory createConnectionFactory() {
		return H2TestSupport.createConnectionFactory();
	}

	@Override
	protected String getCreateTableStatement() {
		return H2TestSupport.CREATE_TABLE_LEGOSET_WITH_ID_GENERATION;
	}

	@Override
	protected Class<? extends LegoSetRepository> getRepositoryInterfaceType() {
		return H2LegoSetRepository.class;
	}

	@Test // gh-235
	public void shouldReturnUpdateCount() {

		shouldInsertNewItems();

		repository.updateManual(42).as(StepVerifier::create).expectNext(2L).verifyComplete();
	}

	@Test // gh-235
	public void shouldReturnUpdateCountAsDouble() {

		shouldInsertNewItems();

		repository.updateManualAndReturnDouble(42).as(StepVerifier::create).expectNext(2.0).verifyComplete();
	}

	@Test // gh-235
	public void shouldReturnUpdateSuccess() {

		shouldInsertNewItems();

		repository.updateManualAndReturnBoolean(42).as(StepVerifier::create).expectNext(true).verifyComplete();
	}

	@Test // gh-235
	public void shouldNotReturnUpdateCount() {

		shouldInsertNewItems();

		repository.updateManualAndReturnNothing(42).as(StepVerifier::create).verifyComplete();
	}

	interface H2LegoSetRepository extends LegoSetRepository {

		@Override
		@Query("SELECT * FROM legoset WHERE name like $1")
		Flux<LegoSet> findByNameContains(String name);

		@Override
		@Query("SELECT name FROM legoset")
		Flux<Named> findAsProjection();

		@Override
		@Query("SELECT * FROM legoset WHERE manual = :manual")
		Mono<LegoSet> findByManual(int manual);

		@Override
		@Query("SELECT id FROM legoset")
		Flux<Integer> findAllIds();

		@Query("UPDATE legoset set manual = :manual")
		@Modifying
		Mono<Long> updateManual(int manual);

		@Query("UPDATE legoset set manual = :manual")
		@Modifying
		Mono<Boolean> updateManualAndReturnBoolean(int manual);

		@Query("UPDATE legoset set manual = :manual")
		@Modifying
		Mono<Void> updateManualAndReturnNothing(int manual);

		@Query("UPDATE legoset set manual = :manual")
		@Modifying
		Mono<Double> updateManualAndReturnDouble(int manual);
	}
}
