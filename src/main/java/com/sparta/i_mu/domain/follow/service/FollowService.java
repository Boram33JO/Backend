package com.sparta.i_mu.domain.follow.service;

import com.sparta.i_mu.domain.follow.dto.FollowPopularResponseDto;
import com.sparta.i_mu.domain.follow.entity.Follow;
import com.sparta.i_mu.domain.user.entity.User;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.domain.follow.repository.FollowReporitory;
import com.sparta.i_mu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowReporitory followReporitory;
    private final UserRepository userRepository;

    public ResponseResource<?> createFollow(Long userId, User user) {
        Optional<Follow> follow = followReporitory.findByFollowUserIdAndFollowedUserId(userId, user.getId());

        if (follow.isPresent()) {
            followReporitory.delete(follow.get());
            return ResponseResource.message("팔로우 삭제", HttpStatus.OK);
        }

        User followUser = userRepository.findById(userId).orElseThrow();

        Follow saveFollow = Follow.builder()
                .followUser(followUser)
                .followedUser(user)
                .build();

        followReporitory.save(saveFollow);

        return ResponseResource.message("팔로우 성공", HttpStatus.OK);
    }

    public List<FollowPopularResponseDto> getFollowPopular() {

        List<FollowPopularResponseDto> followPopularList = userRepository.findAllByOrderByFollowCountDesc().stream()
                .map(user -> new FollowPopularResponseDto(user, followReporitory.countByFollowUserId(user.getId())))
                .limit(4)
                .toList();

        return followPopularList;
    }

}
