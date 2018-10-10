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
class DriverEndpoint(private val shippingService: ShippingService) {

    // FIXME this business with very complicated REST endpoints doesn't work. Just convert everything except /{tracking-id} to JSON and send it as request body
    @PostMapping("/{tracking-id}/driver/{driver-id}/accept")
    fun acceptShipmentRequest(@PathVariable("tracking-id") trackingId: String, @PathVariable("driver-id") driverId: UserId, @RequestBody location: Location) {
        shippingService.acceptShipmentRequest(TrackingId(trackingId), Driver(driverId), location)
    }

    // FIXME same as above
    @PostMapping("/{tracking-id}/driver/{driver-id}/report/pickup/from/shipper/{shipper-id}")
    fun reportPickup(@PathVariable("tracking-id") trackingId: String, @PathVariable("driver-id") driverId: UserId, @PathVariable("shipper-id") shipperId: UserId, @RequestBody location: Location) {
        shippingService.reportPickup(TrackingId(trackingId), Shipper(shipperId), Driver(driverId), location)
    }

    @ExceptionHandler
    fun handleException(e: Exception, response: HttpServletResponse) {
        val status = when(e) {
            is UnknownShipmentException -> HttpStatus.NOT_FOUND
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }.value()

        response.sendError(status, e.message)
    }

}