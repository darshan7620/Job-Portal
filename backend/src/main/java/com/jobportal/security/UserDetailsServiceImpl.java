package com.jobportal.security;

import com.jobportal.entity.User;
import com.jobportal.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier.toLowerCase()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new AppUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getRole(),
                Boolean.TRUE.equals(user.getActive()));
    }
}
