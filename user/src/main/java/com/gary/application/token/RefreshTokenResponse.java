package com.gary.application.token;

import com.gary.common.ResultStatus;
import com.gary.domain.model.token.RefreshToken;
import lombok.Builder;

@Builder
public record RefreshTokenResponse(
        RefreshToken refreshToken,
        ResultStatus resultStatus
) {
}
