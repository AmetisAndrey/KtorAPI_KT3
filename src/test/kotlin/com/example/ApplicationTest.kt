package com.example
import com.example.plugins.*
import com.example.routes.configureRouting
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {
    private fun configuredTest(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        application {
            configureMonitoring()
            configureSerialization()
            configureStatusPages()
            configureRouting()
        }
        block()
    }

    @Test fun healthEndpointReturnsOk() = configuredTest {
        val response = client.get("/api/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("ok"))
    }

    @Test fun createAndFetchUser() = configuredTest {
        val created = client.post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"Test User","email":"test@example.com"}""")
        }
        assertEquals(HttpStatusCode.Created, created.status)
        assertTrue(created.bodyAsText().contains("Test User"))
        val fetched = client.get("/api/users/1")
        assertEquals(HttpStatusCode.OK, fetched.status)
        assertTrue(fetched.bodyAsText().contains("test@example.com"))
    }

    @Test fun invalidEmailReturnsBadRequest() = configuredTest {
        val response = client.post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"Test User","email":"invalid"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("BAD_REQUEST"))
    }

    @Test fun missingUserReturnsNotFound() = configuredTest {
        val response = client.get("/api/users/999")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.bodyAsText().contains("USER_NOT_FOUND"))
    }

    @Test fun deleteUserReturnsNoContent() = configuredTest {
        client.post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"Delete Me","email":"delete@example.com"}""")
        }
        assertEquals(HttpStatusCode.NoContent, client.delete("/api/users/1").status)
        assertEquals(HttpStatusCode.NotFound, client.get("/api/users/1").status)
    }

    @Test fun swaggerAndOpenApiAreAvailable() = configuredTest {
        assertEquals(HttpStatusCode.OK, client.get("/swagger").status)
        val spec = client.get("/openapi.yaml")
        assertEquals(HttpStatusCode.OK, spec.status)
        assertTrue(spec.bodyAsText().contains("openapi: 3.0.3"))
    }
}
