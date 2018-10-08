package com.fedup.location

import com.fedup.shared.protocol.location.*
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import javax.servlet.http.*


@RestController
@RequestMapping("location")
class TrackingEndpoint(private val locationService: LocationService) {

    @PostMapping("/user")
    fun recordUserLocation(@RequestBody userLocation: UserLocation) {
        locationService.recordUserLocation(userLocation)
    }

    @ExceptionHandler
    fun handleException(e: Exception, response: HttpServletResponse) {
        val status = when(e) {
            is UserNotFoundException -> HttpStatus.NOT_FOUND
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }.value()

        response.sendError(status, e.message)
    }
}