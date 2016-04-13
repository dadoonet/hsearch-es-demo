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

		inTransaction( em, tx -> {
			List<VideoGame> videoGames = em.createQuery("from VideoGame v where v.title=:title", VideoGame.class)
					.setParameter("title", "Revenge of the Samurai")
					.getResultList();
			assertThat( videoGames ).onProperty( "title" ).containsExactly( "Revenge of the Samurai" );
		} );

		em.close();
	}

	@Test
	public void queryOnSingleFieldSingleTerm() {
		EntityManager em = emf.createEntityManager();

		inTransaction( em, tx -> {
			List<VideoGame> videoGames = em.createQuery("from VideoGame v where v.title like :title", VideoGame.class)
					.setParameter("title", "%Samurai%")
					.getResultList();
			assertThat( videoGames ).onProperty( "title" ).containsExactly( "Revenge of the Samurai" );
		} );

		em.close();
	}

	@Test
	public void queryOnSingleFieldMultipleTerms() {
		EntityManager em = emf.createEntityManager();

		inTransaction( em, tx -> {
			List<VideoGame> videoGames = em.createQuery("from VideoGame v where v.title like :title", VideoGame.class)
					.setParameter("title", "%Revenge%Samurai%")
					.getResultList();
			assertThat( videoGames ).onProperty( "title" ).containsExactly( "Revenge of the Samurai" );
		} );

		em.close();
	}

	@Test
	public void queryOnSingleFieldMultipleTermsInverted() {
		EntityManager em = emf.createEntityManager();

		inTransaction( em, tx -> {
			List<VideoGame> videoGames = em.createQuery("from VideoGame v where v.title like :title", VideoGame.class)
					.setParameter("title", "%Samurai%Revenge%")
					.getResultList();
			assertThat( videoGames ).isEmpty();;
		} );

		em.close();
	}
}
