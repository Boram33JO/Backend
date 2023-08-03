package com.sparta.i_mu.security;

import com.sparta.i_mu.entity.User;
import com.sparta.i_mu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


//사용자 인증과 관련된 서비스를 구현, 사용자 정보를 가져오고 인증 절차를 처리하는 역할
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("해당 이메일을 찾을 수 없습니다."));
        return new UserDetailsImpl(user);
    }
}