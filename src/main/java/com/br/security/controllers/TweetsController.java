package com.br.security.controllers;

import com.br.security.entity.Role;
import com.br.security.entity.Tweet;
import com.br.security.entity.dto.CreateTweetDto;
import com.br.security.entity.dto.FeedDto;
import com.br.security.entity.dto.FeedItemDto;
import com.br.security.repository.TweetRepository;
import com.br.security.repository.UserRepository;
import org.apache.coyote.Response;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
public class TweetsController {

    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;

    public TweetsController(TweetRepository tweetRepository, UserRepository userRepository) {
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/feed")
    public ResponseEntity<FeedDto> feed(@RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
        var tweets = tweetRepository.findAll(PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimeStamp"))
                .map(tweet -> new FeedItemDto(tweet.getTweetId(),
                        tweet.getContent(),
                        tweet.getUser().getUsername()));

        return ResponseEntity.ok(new FeedDto
                (tweets.getContent(), page, pageSize, tweets.getTotalPages(), tweets.getTotalElements()));
    }


    @PostMapping("/tweets")
    public ResponseEntity<Void> createTweet(@RequestBody CreateTweetDto dto,
                                            JwtAuthenticationToken token) {
        var user = userRepository.findById(UUID.fromString(token.getName()));
        var tweet = new Tweet();
        tweet.setUser(user.get());
        tweet.setContent(dto.content());

        tweetRepository.save(tweet);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tweets/{id}")
    public ResponseEntity<Void> deleteTweet(@PathVariable Long id,
                                            JwtAuthenticationToken token) {
        var user = userRepository.findById(UUID.fromString(token.getName()));

        var isAdmin = user.get().getRoles()
                .stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        var tweet = tweetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (isAdmin || tweet.getUser().getUserId().equals(UUID.fromString(token.getName()))) {
            tweetRepository.deleteById(id);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok().build();
    }
}
