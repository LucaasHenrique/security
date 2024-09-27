package com.br.security.entity.dto;

public record LoginResponse(String acessToken, Long expiresIn) {
}
