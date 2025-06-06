package intg.com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.MoviesServiceApplication;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {MoviesServiceApplication.class})
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@EnableWireMock({
        @ConfigureWireMock(
            name = "my-mock",
            port = 8084)
      }) // spin up a httpserver in port 8084
@TestPropertySource(
        properties = {
                "restClient.moviesInfoUrl=http://localhost:8084/v1/movieinfos",
                "restClient.reviewsUrl=http://localhost:8084/v1/reviews"
        }
)
public class MoviesControllerIntgTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void retrieveMovieById() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));

        //when
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().size() == 2;
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });

    }

    @Test
    void retrieveMovieById_404() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withStatus(404)
                ));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));

        //when
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(String.class)
                .isEqualTo("There is no MovieInfo Available for the passed in Id : abc");

        WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/movieinfos" + "/" + movieId)));

    }

    @Test
    void retrieveMovieById_reviews_404() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")
                ));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(404)));

        //when
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().size() == 0;
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });;

    }

    @Test
    void retrieveMovieById_5XX() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("MovieInfo Service Unavailable")

                ));

       /* stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));*/

        //when
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server Exception in MoviesInfoService MovieInfo Service Unavailable");

        WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/movieinfos" + "/" + movieId)));

    }

    @Test
    void retrieveMovieById_Reviews_5XX() {
        //given
        var movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")
                ));



        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(500)
                .withBody("Review Service Not Available")));

        //when
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server Exception in ReviewsService Review Service Not Available");

        WireMock.verify(4, getRequestedFor(urlPathMatching("/v1/reviews*")));

    }
}
