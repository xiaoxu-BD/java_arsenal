package org.xiaoxu.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCodeMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String phone;

    private String code;

    /**
     * SUCCESS / FAILED
     */
    private String status;

    private String message;

    private LocalDateTime timestamp;
}
