package com.musinsa.catalog.integration

import com.musinsa.catalog.integration.client.TestClient
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import kotlin.test.assertTrue

@TestConstructor(autowireMode = AutowireMode.ALL)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
abstract class IntegrationTest {

    @LocalServerPort
    var port: Int = 0

    val client: TestClient by lazy { TestClient(WebClient.create("http://localhost:$port")) }

    protected fun assertBadRequest(block: () -> Unit) {
        assertThrows<WebClientResponseException>(block)
            .also { assertTrue(it.statusCode.is4xxClientError) }
    }
}