package com.sparta.i_mu.service;

import com.sparta.i_mu.dto.responseDto.CommentResponseDto;
import com.sparta.i_mu.dto.responseDto.FollowResponseDto;
import com.sparta.i_mu.entity.Follow;
import com.sparta.i_mu.entity.User;
import com.sparta.i_mu.entity.Wishlist;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.repository.FollowReporitory;
import lombok.RequiredArgsConstructor;
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
            return new ResponseResource<>(true, "팔로우 삭제", null);
        }

        User followUser = userRepository.findById(userId).orElseThrow();

        Follow saveFollow = Follow.builder()
                .followUser(followUser)
                .followedUser(user)
                .build();

        followReporitory.save(saveFollow);

        return new ResponseResource<>(true, "팔로우 성공", null);
    }

    public List<FollowResponseDto> findFollow(Long userId) {
        List<FollowResponseDto> followList = followReporitory.findAllByFollowedUserId(userId).stream().map(FollowResponseDto::new).toList();

        return followList;
    }
}
