/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.demos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.hibernate.demos.hswithes.model.Character;
import org.hibernate.demos.hswithes.model.Publisher;
import org.hibernate.demos.hswithes.model.VideoGame;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class HibernateSearchWithDbIT extends TestBase {

	private static EntityManagerFactory emf;
	private static TransportClient client;
	private static ObjectMapper mapper;

	@BeforeClass
	public static void setUpEmf() {
		emf = Persistence.createEntityManagerFactory( "videoGamePu" );
		setUpEsClient();
		setUpTestData();
	}

	private static void setUpEsClient() {
		mapper = new ObjectMapper();
		client = TransportClient.builder().build()
				.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress("127.0.0.1", 9300)));
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

			try {
				client.prepareIndex("videogame", "org.hibernate.demos.hswithes.model.VideoGame", "" + game.id)
						.setSource(mapper.writeValueAsString(game))
						.get();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}


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

			try {
				client.prepareIndex("videogame", "org.hibernate.demos.hswithes.model.VideoGame", "" + game.id)
						.setSource(mapper.writeValueAsString(game))
						.get();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}


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

			try {
				client.prepareIndex("videogame", "org.hibernate.demos.hswithes.model.VideoGame", "" + game.id)
						.setSource(mapper.writeValueAsString(game))
						.get();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

		} );

		client.admin().indices().prepareRefresh("videogame").get();
		em.close();
	}

	@Test
	public void queryOnSingleField() throws IOException {
		SearchResponse response = client.prepareSearch("videogame").setQuery(
				QueryBuilders.matchQuery("title", "Revenge of the Samurai")
		).get();

		List<VideoGame> videoGames = new ArrayList<>();
		for (SearchHit hit : response.getHits().getHits()) {
			videoGames.add(mapper.readValue(hit.getSourceAsString(), VideoGame.class));
		}

		assertThat( videoGames ).onProperty( "title" ).containsExactly( "Revenge of the Samurai" );
	}

	@Test
	public void queryOnSingleFieldSingleTerm() throws IOException {
		SearchResponse response = client.prepareSearch("videogame").setQuery(
				QueryBuilders.matchQuery("title", "samurai")
		).get();

		List<VideoGame> videoGames = new ArrayList<>();
		for (SearchHit hit : response.getHits().getHits()) {
			videoGames.add(mapper.readValue(hit.getSourceAsString(), VideoGame.class));
		}

		assertThat( videoGames ).onProperty( "title" ).containsExactly( "Revenge of the Samurai" );
	}

	@Test
	public void queryOnSingleFieldMultipleTerms() throws IOException {
		SearchResponse response = client.prepareSearch("videogame").setQuery(
				QueryBuilders.matchQuery("title", "revenge samurai")
		).get();

		List<VideoGame> videoGames = new ArrayList<>();
		for (SearchHit hit : response.getHits().getHits()) {
			videoGames.add(mapper.readValue(hit.getSourceAsString(), VideoGame.class));
		}

		assertThat( videoGames ).onProperty( "title" ).containsExactly( "Revenge of the Samurai" );
	}

	@Test
	public void queryOnSingleFieldMultipleTermsInverted() throws IOException {
		SearchResponse response = client.prepareSearch("videogame").setQuery(
				QueryBuilders.matchQuery("title", "samurai revenge")
		).get();

		List<VideoGame> videoGames = new ArrayList<>();
		for (SearchHit hit : response.getHits().getHits()) {
			videoGames.add(mapper.readValue(hit.getSourceAsString(), VideoGame.class));
		}

		assertThat( videoGames ).onProperty( "title" ).containsExactly( "Revenge of the Samurai" );
	}
}
