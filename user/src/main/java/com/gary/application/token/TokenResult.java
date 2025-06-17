package com.gary.application.token;

import com.gary.application.common.ResultStatus;
import com.gary.domain.model.token.RefreshToken;

public record TokenResult(
        RefreshToken refreshToken,
        ResultStatus status
) {
}
