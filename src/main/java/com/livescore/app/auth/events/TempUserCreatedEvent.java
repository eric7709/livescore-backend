package com.livescore.app.auth.events;

import com.livescore.app.auth.enums.Role;

public record TempUserCreatedEvent(String email, String firstName, String token, Role role) {
}