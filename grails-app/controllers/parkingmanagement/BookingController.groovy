package parkingmanagement

import grails.converters.JSON
import java.text.SimpleDateFormat

class BookingController {
	
	// Inject the service
	def bookingService

	// Define an action for the 1st step of booking
	def step1() {
		try {
			// Parse the request JSON
			def requestJson = request.JSON

			// Call the service method to handle step 1 of booking
			def result = bookingService.processStep1Booking(requestJson)

			if (result.status == 'success') {
				// Booking step 1 was successful, construct a success response
				def responseBody = [
					status: 'success',
					message: 'Booking step 1 successful.',
					data: result.data
				]

				render status: 200, contentType: 'application/json', text: responseBody as JSON
			} else {
				// Booking step 1 failed, construct an error response with details
				def responseBody = [
					status: 'error',
					message: 'Booking step 1 failed.',
					errors: result.errors
				]

				render status: 400, contentType: 'application/json', text: responseBody as JSON
			}
		} catch (Exception e) {
			// Handle any unhandled exceptions that occurred during booking step 1
			render status: 500, contentType: 'application/json',
				text: [
					status: 'error',
					message: 'Booking step 1 failed.',
					errors: ['An error occurred during booking step 1.']
				] as JSON
		}
	}

	def step2() {
    try {
        // Parse the request JSON
        def requestJson = request.JSON

        // Retrieve step 1 data by bookingId
        def step1Booking = bookingService.retrieveStep1BookingData(requestJson.bookingId)

        if (!step1Booking) {
            log.error("Step 1 booking data not found for the provided booking ID.")
            render status: 400, contentType: 'application/json',
                text: [
                    status: 'error',
                    message: 'Step 1 booking data not found for the provided booking ID.'
                ] as JSON
            return
        }

        log.info("LF Step 1 Booking Data: $step1Booking")

		if (step1Booking.status == "Cancelled") {
			// Check if the booking is already cancelled
			render status: 400, contentType: 'application/json',
				text: [
					status: 'error',
					message: 'This booking has been cancelled and no further action is allowed.'
				] as JSON
			return
		}
		
        if (requestJson.isComing == false) {
            // User has indicated that they are not coming, so cancel the booking
            step1Booking.status = "Cancelled"
            step1Booking.save(flush: true)

            render status: 200, contentType: 'application/json',
                text: [
                    status: 'success',
                    message: 'Booking has been cancelled.'
                ] as JSON
        } else {
            // Calculate the time difference in minutes
            def currentTime = new Date()
            long timeDifferenceMinutes = (step1Booking.startTime.time - currentTime.time) / (60 * 1000)

            if (timeDifferenceMinutes <= 150) {
                // Determine the parking slot allocation based on the user's category
                def allocatedSlot = bookingService.allocateParkingSlot(step1Booking)

                if (allocatedSlot) {
                    render status: 200, contentType: 'application/json',
                        text: [
                            status: 'success',
                            message: 'Booking step 2: Slot allocated.',
                            data: allocatedSlot.slotName
                        ] as JSON
                } else {
                    render status: 500, contentType: 'application/json',
                        text: [
                            status: 'error',
                            message: 'Failed to allocate a parking slot.'
                        ] as JSON
                }
            } else {
                render status: 400, contentType: 'application/json',
                    text: [
                        status: 'error',
                        message: 'You can only confirm coming 15 minutes or less before the start time.'
                    ] as JSON
            }
        }
    } catch (Exception e) {
        log.error("An error occurred during step 2 of the booking process: ${e.message}", e)
        render status: 500, contentType: 'application/json',
            text: [
                status: 'error',
                message: 'An error occurred during step 2 of the booking process.',
                errors: [e.message]
            ] as JSON
    }
}


	
	
	
	def extendEndTime() {
		try {
			def requestJson = request.JSON
	
			// Retrieve the booking by booking id
			def bookingId = requestJson.bookingId
			def booking = Booking.get(bookingId)
	
			if (!booking) {
				render status: 400, contentType: 'application/json',
					text: [
						status: 'error',
						message: 'Booking not found for the provided booking ID.'
					] as JSON
				return
			}
	
			if (booking.status != "Confirmed") {
				render status: 400, contentType: 'application/json',
					text: [
						status: 'error',
						message: 'Booking status is not "Confirmed". Cannot extend the endTime.'
					] as JSON
				return
			}
	
			// Parse the requested endTime String to a Date
			def requestedEndTimeString = requestJson.endTime
			def requestedEndTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(requestedEndTimeString)
	
			// Check if the endTime can be extended (within 10 minutes or less of the current endTime)
			def canExtend = bookingService.canExtendEndTime(booking, requestedEndTime)
	
			if (canExtend) {
				// Check if the booking has already been extended
				def bookingExtension = BookingExtension.findByBooking(booking)
				if (bookingExtension) {
					render status: 400, contentType: 'application/json',
						text: [
							status: 'error',
							message: 'Booking endTime can only be extended once.'
						] as JSON
					return
				}
	
				// Check if the requested endTime is after the current endTime
				if (requestedEndTime.before(booking.endTime)) {
					render status: 400, contentType: 'application/json',
						text: [
							status: 'error',
							message: 'Requested endTime cannot be before the current endTime.'
						] as JSON
					return
				}
	
				// Update the endTime and create a new booking extension entry
				booking.endTime = requestedEndTime
				def extension = new BookingExtension(booking: booking, extendedEndTime: requestedEndTime)
				if (booking.save(flush: true) && extension.save(flush: true)) {
					render status: 200, contentType: 'application/json',
						text: [
							status: 'success',
							message: 'Booking endTime extended successfully.'
						] as JSON
				} else {
					render status: 500, contentType: 'application/json',
						text: [
							status: 'error',
							message: 'Failed to extend the booking endTime.',
							errors: booking.errors
						] as JSON
				}
			} else {
				render status: 400, contentType: 'application/json',
					text: [
						status: 'error',
						message: 'Cannot extend the endTime. Make sure the request is within 10 minutes of the current endTime.'
					] as JSON
			}
		} catch (Exception e) {
			render status: 500, contentType: 'application/json',
				text: [
					status: 'error',
					message: 'An error occurred during the endTime extension process.',
					errors: [e.message]
				] as JSON
		}
	}
	
	

	// List all bookings
	def bookingList() {
		try {
			// Retrieve all bookings
			def bookings = Booking.list()

			// Construct a response with the list of bookings
			def responseBody = [
				status: 'success',
				message: 'List of all bookings',
				data: bookings
			]

			render status: 200, contentType: 'application/json', text: responseBody as JSON
		} catch (Exception e) {
			render status: 500, contentType: 'application/json',
				text: [
					status: 'error',
					message: 'An error occurred while retrieving the list of bookings.',
					errors: [e.message]
				] as JSON
		}
	}

	// Show details of a specific booking
	def showBooking(Long id) {
		try {
			// Retrieve the booking from the database by ID
			def booking = Booking.get(id)

			if (booking) {
				// Construct the success response
				def responseBody = [
					status: 'success',
					message: 'Booking retrieved successfully.',
					data: [
						bookingId: booking.id,
						user: booking.user.userName,
						parkingSlot: booking.parkingSlot,
						startTime: booking.startTime,
						endTime: booking.endTime,
						status: booking.status,
						vehicleCategory: booking.vehicleCategory,
						vehiclePlateNumber: booking.vehiclePlateNumber,
					]
				]

				render status: 200, contentType: 'application/json', text: responseBody as JSON
			} else {
				// Booking not found
				render status: 404, contentType: 'application/json',
					text: [
						status: 'error',
						code: 'booking_not_found',
						message: "The booking with ID ${id} was not found.",
						details: "The requested booking does not exist in the system."
					] as JSON
			}
		} catch (Exception e) {
			// Handle any exceptions here and return an error response
			render status: 500, contentType: 'application/json',
				text: [
					status: 'error',
					message: 'Failed to retrieve booking.',
					errors: ['An error occurred while fetching booking data.']
				] as JSON
		}
	}

	// Delete a specific booking by its ID
	def deleteBooking(Long id) {
		try {
			// Retrieve the booking from the database by ID
			def booking = Booking.get(id)

			if (booking) {
				// Delete the booking
				booking.delete(flush: true)

				// Construct a success response
				def responseBody = [
					status: 'success',
					message: "Booking with ID ${id} has been deleted.",
				]

				render status: 200, contentType: 'application/json', text: responseBody as JSON
			} else {
				// Booking not found
				render status: 404, contentType: 'application/json',
					text: [
						status: 'error',
						code: 'booking_not_found',
						message: "The booking with ID ${id} was not found.",
						details: "The requested booking does not exist in the system."
					] as JSON
			}
		} catch (Exception e) {
			// Handle any exceptions and return an error response
			render status: 500, contentType: 'application/json',
				text: [
					status: 'error',
					message: 'Failed to delete the booking.',
					errors: ['An error occurred while deleting the booking.']
				] as JSON
		}
	}
}
