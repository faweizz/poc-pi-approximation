package de.faweizz.sharing

import java.net.InetAddress
import java.util.*

class Config(
    consumerGroup: String,
    clientSecret: String,
    clientName: String,
    trustStoreLocation: String,
    trustStorePassword: String,
    kafkaAddress: String
) : Properties() {
    init {
        this["client.id"] = InetAddress.getLocalHost().hostName
        this["group.id"] = consumerGroup
        this.setProperty("bootstrap.servers", kafkaAddress)
        this.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        this.setProperty("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer")
        this.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        this.setProperty("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer")
        this["sasl.jaas.config"] =
            "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required oauth.token.endpoint.uri=\"http://keycloak:8080/auth/realms/TestRealm/protocol/openid-connect/token\" oauth.client.id=\"$clientName\" oauth.client.secret=\"$clientSecret\";"
        this["sasl.login.callback.handler.class"] = "io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler"
        this["security.protocol"] = "SASL_SSL"
        this["sasl.mechanism"] = "OAUTHBEARER"
        this["ssl.truststore.location"] = trustStoreLocation
        this["ssl.truststore.password"] = trustStorePassword
        this["ssl.endpoint.identification.algorithm"] = ""
    }
}