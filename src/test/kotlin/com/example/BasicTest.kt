package com.example

import com.example.auth.TokenService
import kotlin.test.Test
import kotlin.test.assertEquals

class TokenServiceTest {
    @Test
    fun createsVerifiableJwtWithUserClaims() {
        val token = TokenService.create(42, "tester", "USER")
        val decoded = TokenService.verifier().verify(token)

        assertEquals(42, decoded.getClaim("userId").asInt())
        assertEquals("tester", decoded.getClaim("username").asString())
        assertEquals("USER", decoded.getClaim("role").asString())
    }
}
