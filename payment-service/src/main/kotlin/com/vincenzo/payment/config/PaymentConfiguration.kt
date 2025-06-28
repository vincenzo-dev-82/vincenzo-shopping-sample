package com.vincenzo.payment.config

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan("com.vincenzo.payment.adapter.out.processor")
class PaymentConfiguration
