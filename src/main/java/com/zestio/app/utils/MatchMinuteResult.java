package com.zestio.app.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchMinuteResult {
    private final int minute;
    private final int extraMinute;
}