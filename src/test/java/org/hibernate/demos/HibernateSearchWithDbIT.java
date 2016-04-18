/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.demos;

import org.hibernate.demos.hswithes.model.Character;
import org.hibernate.demos.hswithes.model.Publisher;
import org.hibernate.demos.hswithes.model.VideoGame;
import org.hibernate.search.backend.elasticsearch.ElasticsearchQueries;
import org.hibernate.search.backend.elasticsearch.ProjectionConstants;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.GregorianCalendar;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class HibernateSearchWithDbIT extends TestBase {

	private static EntityManagerFactory emf;

	@BeforeClass
	public static void setUpEmf() {
		emf = Persistence.createEntityManagerFactory( "videoGamePu" );
		setUpTestData();
	}

	public static void setUpTestData() {
		EntityManager em = emf.createEntityManager();

		inTransaction( em, tx -> {
			Publisher samuraiGames = new Publisher( "Samurai Games, Inc.", "12 Main Road" );

			Character luigi = new Character( "Luigi", "Plumbing" );
			em.persist( luigi );

			Character frank = new Character( "Frank", "Sleeping" );
			em.persist( frank );

			Character dash = new Character( "Dash", "Running" );
			em.persist( dash );

			// Game 1
			VideoGame game = new VideoGame.Builder()
					.withTitle( "Revenge of the Samurai" )
					.withDescription( "The Samurai is mad and takes revenge" )
					.withRating( 8 )
					.withPublishingDate( new GregorianCalendar( 2005, 11, 5 ).getTime() )
					.withPublisher( samuraiGames )
					.withCharacters( luigi, dash )
					.withTags( "action", "real-time", "anime" )
					.build();

			em.persist( game );

			// Game 2
			game = new VideoGame.Builder()
					.withTitle( "Tanaka's return" )
					.withDescription( "The famous Samurai Tanaka returns" )
					.withRating( 10 )
					.withPublishingDate( new GregorianCalendar( 2011, 2, 13 ).getTime() )
					.withPublisher( samuraiGames )
					.withCharacters( frank, dash, luigi )
					.withTags( "action", "round-based" )
					.build();

			em.persist( game );

			// Game 3
			game = new VideoGame.Builder()
					.withTitle( "Ninja Castle" )
					.withDescription( "7 Ninjas live in a castle" )
					.withRating( 5 )
					.withPublishingDate( new GregorianCalendar( 2007, 3, 7 ).getTime() )
					.withPublisher( samuraiGames )
					.withCharacters( frank )
					.build();

			em.persist( game );

		} );

		em.close();
	}

	@Test
	public void queryOnSingleField() {
		EntityManager em = emf.createEntityManager();
		FullTextEntityManager ftem = Search.getFullTextEntityManager(em);

		inTransaction( em, tx -> {
			QueryBuilder qb = ftem.getSearchFactory().buildQueryBuilder().forEntity( VideoGame.class ).get();
			@SuppressWarnings( "unchecked" )
			List<VideoGame> videoGames = ftem.createFullTextQuery(
					qb.keyword().onField( "title" ).matching( "Revenge of the Samurai" ).createQuery(),
					VideoGame.class
				)
				.getResultList();
			assertThat( videoGames ).onProperty( "title" ).containsExactly( "Revenge of the Samurai" );
		} );

		em.close();
	}

	@Test
	public void queryOnSingleFieldNativeElasticsearch() {
		EntityManager em = emf.createEntityManager();
		FullTextEntityManager ftem = Search.getFullTextEntityManager(em);

		inTransaction( em, tx -> {
			@SuppressWarnings( "unchecked" )
			List<VideoGame> videoGames = ftem.createFullTextQuery(
					ElasticsearchQueries.fromQueryString( "title:samurai" ),
					VideoGame.class
			)
					.getResultList();
			assertThat( videoGames ).onProperty( "title" ).containsExactly( "Revenge of the Samurai" );
		} );

		inTransaction( em, tx -> {
			@SuppressWarnings( "unchecked" )
			List<VideoGame> videoGames = ftem.createFullTextQuery(
					ElasticsearchQueries.fromJson( "{ 'query': { 'match' : { 'title' : 'Revenge of the Samurai' } } }" ),
					VideoGame.class
			)
					.getResultList();
			assertThat( videoGames ).onProperty( "title" ).containsExactly( "Revenge of the Samurai" );
		} );

		em.close();
	}
	@Test
	public void queryOnSingleFieldSingleTerm() {
		EntityManager em = emf.createEntityManager();
		FullTextEntityManager ftem = Search.getFullTextEntityManager(em);

		inTransaction( em, tx -> {
			QueryBuilder qb = ftem.getSearchFactory().buildQueryBuilder().forEntity( VideoGame.class ).get();
			@SuppressWarnings( "unchecked" )
			List<VideoGame> videoGames = ftem.createFullTextQuery(
					qb.keyword().onField( "title" ).matching( "samurai" ).createQuery(),
					VideoGame.class
			)
					.getResultList();
			assertThat( videoGames ).onProperty( "title" ).containsExactly( "Revenge of the Samurai" );
		} );

		em.close();
	}

	@Test
	public void queryByRange() {
		EntityManager em = emf.createEntityManager();
		FullTextEntityManager ftem = Search.getFullTextEntityManager(em);

		inTransaction( em, tx -> {
			QueryBuilder qb = ftem.getSearchFactory().buildQueryBuilder().forEntity( VideoGame.class ).get();
			@SuppressWarnings( "unchecked" )
			List<VideoGame> videoGames = ftem.createFullTextQuery(
					qb.range().onField( "rating" ).from( 8 ).excludeLimit().to( 10 ).createQuery()
					, VideoGame.class
			)
					.getResultList();
			assertThat( videoGames ).onProperty( "title" ).containsExactly( "Tanaka's return" );
		} );

		em.close();
	}

	@Test
	public void queryWithProjection() {
		EntityManager em = emf.createEntityManager();
		FullTextEntityManager ftem = Search.getFullTextEntityManager(em);

		inTransaction( em, tx -> {
			QueryBuilder qb = ftem.getSearchFactory().buildQueryBuilder().forEntity( VideoGame.class ).get();
			FullTextQuery query = ftem.createFullTextQuery(
					qb.range().onField( "rating" ).from( 4 ).excludeLimit().to( 10 ).createQuery()
					, VideoGame.class
			);
			@SuppressWarnings( "unchecked" )
			List<VideoGame> videoGames = query
					.setProjection( "title", ProjectionConstants.SOURCE )
					.setFirstResult( 0 )
					.setMaxResults( 20 )
					.getResultList();
			assertThat( videoGames ).hasSize( 3 );
			assertThat( videoGames.get( 0 ) ).isInstanceOf( Object[].class );

		} );

		em.close();
	}
}
