package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.dto.auth.UserDetails;
import java.util.Optional;

public interface AccessTokenService {

    String generateAccessToken(UserDetails user);

    Optional<UserDetails> parse(String token);
}
