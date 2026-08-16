package org.xiaoxu.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BizException extends RuntimeException {
    private final String message;
}