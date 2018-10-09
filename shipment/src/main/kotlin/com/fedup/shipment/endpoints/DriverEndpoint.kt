package com.fedup.shipment.endpoints

import com.fedup.shared.*
import com.fedup.shared.protocol.location.*
import com.fedup.shipment.*
import com.fedup.shipment.model.*
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import javax.servlet.http.*

@RestController
@RequestMapping("shipment")
class DriverEndpoint(private val shipmentFacade: ShipmentFacade) {

    @PostMapping("/{tracking-id}/{user-id}")
    fun acceptShipmentRequest(@PathVariable("tracking-id") trackingId: String, @PathVariable("user-id") driverId: UserId, @RequestBody location: Location) {
        shipmentFacade.acceptShipmentRequest(TrackingId(trackingId), Driver(driverId), location)
    }

    fun reportPickup(trackingId: TrackingId, driver: Driver, at: SpaceTimeCoordinates) {
    }

    fun reportHandOff(trackingId: TrackingId, acceptedBy: Driver, at: SpaceTimeCoordinates) {
    }

    @ExceptionHandler
    fun handleException(e: Exception, response: HttpServletResponse) {
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.message)
    }

}