package com.cyc.xiangwei.auth.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RefreshTokenRequest {
    @NotBlank(message = "刷新凭证(RefreshToken)不能为空")
    private String refreshToken;
//    public String getRefreshToken() { return refreshToken; }
//    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
