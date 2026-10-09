package com.livescore.app.auth.dto;

public record LoginResult(AuthResponseDTO tokens, MeResponseDTO me) {}