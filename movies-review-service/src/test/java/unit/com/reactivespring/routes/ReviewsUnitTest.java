package unit.com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

import java.util.List;



@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

    @MockBean
    private ReviewReactiveRepository reviewReactiveRepository;

    @Autowired
    private WebTestClient webTestClient;

    static  String REVIEWS_URL = "/v1/reviews";

    @Test
    void addReview() {
        //given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));

        //when

        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedReview = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedReview!=null;
                    assert savedReview.getReviewId()!=null;
                });
    }


    @Test
    void getAllReviews() {
        //given
        var reviews = List.of(new Review(null, 1L, "Awesome Movie", 9.0), new Review(null, 2L, "Bad Movie", 2.0));
        when(reviewReactiveRepository.findAll()).thenReturn(Flux.fromIterable(reviews));

        //when
        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);
    }

    @Test
    void updateReview() {
        //given
        var review = new Review("abc", 1L, "Awesome Movie", 9.0);
        when(reviewReactiveRepository.findById("abc")).thenReturn(Mono.just(review));
        when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));

        //when
        webTestClient
                .put()
                .uri(REVIEWS_URL+"/{id}", "abc")
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var updatedReview = reviewEntityExchangeResult.getResponseBody();
                    assert updatedReview!=null;
                    assert updatedReview.getReviewId()!=null;
                });
    }

    @Test
    void deleteReview() {
        //given
        var review = new Review("abc", 1L, "Awesome Movie", 9.0);
        when(reviewReactiveRepository.findById("abc")).thenReturn(Mono.just(review));
        when(reviewReactiveRepository.deleteById("abc")).thenReturn(Mono.empty());
        //when
        webTestClient
                .delete()
                .uri(REVIEWS_URL+"/{id}", "abc")
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }

    @Test
    void getReviewsByMovieInfoId() {
        //given
        var reviews = List.of(new Review(null, 1L, "Awesome Movie", 9.0), new Review(null, 2L, "Bad Movie", 2.0));
        when(reviewReactiveRepository.findReviewsByMovieInfoId(1L)).thenReturn(Flux.fromIterable(reviews));
        var uri = UriComponentsBuilder.fromUriString(REVIEWS_URL)
                .queryParam("movieInfoId", 1L)
                .buildAndExpand().toUri();

        //when
        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);
    }

}