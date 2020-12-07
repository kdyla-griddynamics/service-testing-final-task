package com.griddynamics.gridu.qa.user.config;

import com.griddynamics.gridu.qa.address.api.AddressApi;
import com.griddynamics.gridu.qa.payment.ApiClient;
import com.griddynamics.gridu.qa.payment.api.PaymentApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestApiClientsConfig {

    @Bean
    public PaymentApi paymentApi(ApiClient paymentApiClient) {
        return new PaymentApi(paymentApiClient);
    }

    @Bean
    public ApiClient paymentApiClient(@Value("${payment.service.url}") String paymentServiceURL) {
        ApiClient client = new ApiClient();
        client.setBasePath(paymentServiceURL);
        return client;
    }

    @Bean
    public AddressApi addressApi(com.griddynamics.gridu.qa.address.ApiClient addressApiClient) {
        return new AddressApi(addressApiClient);
    }

    @Bean
    public com.griddynamics.gridu.qa.address.ApiClient addressApiClient(@Value("${address.service.url}") String addressServiceURL) {
        com.griddynamics.gridu.qa.address.ApiClient client = new com.griddynamics.gridu.qa.address.ApiClient();
        client.setBasePath(addressServiceURL);
        return client;
    }
}
