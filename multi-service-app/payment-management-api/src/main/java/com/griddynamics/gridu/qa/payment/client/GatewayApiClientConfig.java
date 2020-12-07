package com.griddynamics.gridu.qa.payment.client;

import com.griddynamics.gridu.qa.gateway.ApiClient;
import com.griddynamics.gridu.qa.gateway.api.CardApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayApiClientConfig {

    @Bean
    public CardApi cardApi(ApiClient apiClient) {
        return new CardApi(apiClient);
    }

    @Bean
    public ApiClient apiClient(@Value("${payment.gateway.url}") String paymentGatewayURL) {
        ApiClient client = new ApiClient();
        client.setBasePath(paymentGatewayURL);
        return client;
    }
}
