package com.fedup.location

import com.fedup.shared.protocol.location.*
import org.assertj.core.api.Assertions.*
import org.junit.*
import org.junit.runner.*
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.test.context.*
import org.springframework.test.context.junit4.*

@RunWith(SpringRunner::class)
@SpringBootTest
class UserLocationRepositoryTest {
    @Autowired private lateinit var userLocationRepository: UserLocationRepository

    @Test
    fun `should find stored user location`() {
        val original = UserLocation("driver@drivers.com", Location(37.7534327, -122.4344288))
        userLocationRepository.saveUserLocation(original)

        val retrieved = userLocationRepository.findLocationFor(original.userId)
        assertThat(retrieved).isEqualTo(original)
    }

    @Before
    fun setUp() {
        userLocationRepository.start()
    }

    @After
    fun tearDown() {
        userLocationRepository.stop()
    }
}