package de.faweizz.sharing

import org.apache.avro.SchemaBuilder
import org.apache.avro.generic.GenericRecordBuilder
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.Duration
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

fun main() {
    val kafkaAddress = System.getenv("KAFKA_ADDRESS") ?: throw Exception("Missing variable KAFKA_ADDRESS")
    val clientName = System.getenv("CLIENT_NAME") ?: throw Exception("Missing variable CLIENT_NAME")
    val clientSecret = System.getenv("CLIENT_SECRET") ?: throw Exception("Missing variable CLIENT_SECRET")
    val consumerGroup = System.getenv("CONSUMER_GROUP") ?: throw Exception("Missing variable CONSUMER_GROUP")
    val inputTopic = System.getenv("INPUT_TOPIC") ?: throw Exception("Missing variable INPUT_TOPIC")
    val outputTopic = System.getenv("OUTPUT_TOPIC") ?: throw Exception("Missing variable OUTPUT_TOPIC")
    val trustStoreLocation =
        System.getenv("TRUSTSTORE_LOCATION") ?: throw Exception("Missing variable TRUSTSTORE_LOCATION")
    val trustStorePassword =
        System.getenv("TRUSTSTORE_PASSWORD") ?: throw Exception("Missing variable TRUSTSTORE_PASSWORD")

    val config = Config(
        clientName = clientName,
        clientSecret = clientSecret,
        consumerGroup = consumerGroup,
        trustStoreLocation = trustStoreLocation,
        trustStorePassword = trustStorePassword,
        kafkaAddress = kafkaAddress
    )

    val consumer = KafkaConsumer<String, ByteArray>(config)
    consumer.subscribe(listOf(inputTopic))

    val producer = KafkaProducer<String, ByteArray>(config)

    val inputSchema = SchemaBuilder.builder()
        .record("random_vector")
        .fields()
        .requiredDouble("x")
        .requiredDouble("y")
        .endRecord()

    val outputSchema = SchemaBuilder.builder()
        .record("pi_approximation")
        .fields()
        .requiredLong("totalVectorsProcessed")
        .requiredLong("vectorsInUnitCircle")
        .requiredDouble("piApproximation")
        .requiredDouble("error")
        .endRecord()

    var vectorsChecked = 0
    var vectorsInUnitCircle = 0

    while (true) {
        val newMessages = consumer.poll(Duration.ofMillis(100))
        newMessages.records(inputTopic).forEach {
            val vector = it.value().deserialize(inputSchema)
            println("Pi Service received Vector $vector")

            val vectorMagnitude = sqrt((vector["x"] as Double).pow(2) + (vector["y"] as Double).pow(2))

            if (vectorMagnitude <= 1) vectorsInUnitCircle++
            vectorsChecked++

            val currentPiApproximation = (vectorsInUnitCircle.toDouble() / vectorsChecked.toDouble()) * 4.0
            val error = abs(PI - currentPiApproximation)

            val result = GenericRecordBuilder(outputSchema)
                .set("totalVectorsProcessed", vectorsChecked)
                .set("vectorsInUnitCircle", vectorsInUnitCircle)
                .set("piApproximation", currentPiApproximation)
                .set("error", error)
                .build()

            producer.send(ProducerRecord(outputTopic, result.serialize()))
        }
    }
}