package parkingmanagement

import java.text.SimpleDateFormat
import java.util.TimeZone


class BookingService {
	
	
	
	def processStep1Booking(Map<String, Object> bookingData) {
    try {
        // Extract the data from the request
        def slotCategory = bookingData.slotCategory
        def startTimeString = bookingData.startTime // Get the startTime as a string
        def endTimeString = bookingData.endTime // Get the endTime as a string
        def vehiclePlateNumber = bookingData.vehiclePlateNumber
        def userId = bookingData.userId

        def sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        sdf.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        Date startTime = sdf.parse(startTimeString)
        Date endTime = sdf.parse(endTimeString)

        // Ensure that startTime is after the current time
        Date currentTime = new Date()
        if (startTime.before(currentTime)) {
            return [status: 'error', message: 'Booking step 1 failed.', errors: 'Start time cannot be before the current time.']
        }
		
		// Ensure that endTime is after the startTime
		if (endTime.before(startTime)) {
			return [status: 'error', message: 'Booking step 1 failed.', errors: 'End time cannot be before the start time.']
		}

        // Determine the vehicle category based on slotCategory
        def vehicleCategory = determineVehicleCategory(slotCategory)

		// Vehicle category is determined, proceed to the next steps
        if (vehicleCategory) {

            // Create a Booking instance with the determined data
            def booking = new Booking(
                user: User.get(userId),
                parkingSlot: null, // will assign the parking slot in the next step
                startTime: startTime,
                endTime: endTime,
                status: 'Pending',
                vehicleCategory: vehicleCategory,
                vehiclePlateNumber: vehiclePlateNumber
            )

            // Save the booking
            if (booking.save(flush: true)) {
                return [status: 'success', message: 'Booking step 1 successful.', data: booking]
            } else {
                return [status: 'error', message: 'Booking step 1 failed.', errors: booking.errors]
            }
        } else {
            // Unable to determine the vehicle category, return an error
            return [status: 'error', "message": 'Unable to determine vehicle category. Booking step 1 failed.']
        }
    } catch (Exception e) {
        System.out.println("exception : " + e.getMessage())
        return [status: 'error', message: 'Booking step 1 failed.', errors: "An error occurred during booking step 1."]
    }
}


	def determineVehicleCategory(String slotCategory) {
		if (slotCategory.equals("twoWheeler")) {
			return "twoWheeler"
		} else if (slotCategory.equals("fourWheeler")) {
			return "fourWheeler"
		}

		return null // If the vehicle category cannot be determined
	}

	def retrieveStep1BookingData(Long bookingId) {
		// Retrieve the most recent booking for the given vehicle plate number
		return Booking.get(bookingId)
	}

	def allocateParkingSlot(Booking step1Booking) {
		// Logic to allocate a parking slot based on step 1 data and availability

		if (step1Booking.vehicleCategory == "twoWheeler") {
			// Logic for two-wheeler slot allocation (e.g., ground floor)
			def slot = allocateTwoWheelerSlot(step1Booking)
		
			log.info("Two-wheeler parking slot allocated for booking ID: ${step1Booking.id}")
			return slot
		} else if (step1Booking.vehicleCategory == "fourWheeler") {
			// Logic for four-wheeler slot allocation (e.g., ramp or ground floor)
			def slot = allocateFourWheelerSlot(step1Booking)
			
            log.info("New Four-wheeler parking slot allocated for booking ID: ${step1Booking.id}")
			
     		return slot
			 
		}
	
		
		log.error("No parking slot available for booking ID: ${step1Booking.id}")
		return null // If no slot available
	}

	def allocateTwoWheelerSlot(Booking step1Booking) {
		// If a two-wheeler slot is available on the ground floor, assign it to the booking
		def availableSlot = ParkingSlot.findBySlotCategoryAndTypeOfSlotAndSlotAvailable("twoWheeler", "Ground Floor", true)
		if (availableSlot && availableSlot.slotAvailable) {
			step1Booking.parkingSlot = availableSlot
			step1Booking.status = "Confirmed"
			availableSlot.slotAvailable = false // Mark the slot as not available
			if (step1Booking.save(flush: true) && availableSlot.save(flush: true)) {
			
				log.info("Two-wheeler parking slot allocated successfully for booking ID: ${step1Booking.id}")
				return availableSlot
			}
		}

		return null // If no slot available
	}

	// Implement slot allocation logic for four-wheelers
	def allocateFourWheelerSlot(Booking step1Booking) {
		// Check if a ramp slot is available
		def rampSlot = ParkingSlot.findBySlotCategoryAndTypeOfSlotAndSlotAvailable("fourWheeler", "Ramp", true)

		if (rampSlot && rampSlot.slotAvailable) {
			log.info "ramp available"
			// If a ramp slot is available, assign it to the booking
			step1Booking.parkingSlot = rampSlot
			step1Booking.status = "Confirmed"
			rampSlot.slotAvailable = false // Mark the slot as not available

			if (step1Booking.save(flush: true) && rampSlot.save(flush: true)) {
				
				log.info("Four-wheeler ramp parking slot allocated successfully for booking ID: ${step1Booking.id}")
				return rampSlot
			}
			else
			{
				log.info("else of ramp")
			}
		} else {
		log.info "ramp not available"
			// If no ramp slot is available, assign a ground floor slot
			def groundFloorSlot = ParkingSlot.findBySlotCategoryAndTypeOfSlotAndSlotAvailable("fourWheeler", "Ground Floor", true)
			//log.info("groundFloorSlot : " + groundFloorSlot.properties)
			if (groundFloorSlot && groundFloorSlot.slotAvailable) {
				step1Booking.parkingSlot = groundFloorSlot
				step1Booking.status = "Confirmed"
				groundFloorSlot.slotAvailable = false // Mark the slot as not available

				if (step1Booking.save(flush: true) && groundFloorSlot.save(flush: true)) {
				
					log.info("Four-wheeler ground floor parking slot allocated successfully for booking ID: ${step1Booking.id}")
					return groundFloorSlot
				}
				else
				{
					log.info "else of ground int"
				}
			}
			else
			{
				log.info "else of ground ext"
			}
		}

		return null // If no slot is available
	}

	
		def canExtendEndTime(Booking booking, Date requestedEndTime) {
    // Calculate the time difference in milliseconds between the requested endTime and the current endTime
    def timeDifferenceMillis = requestedEndTime.time - booking.endTime.time

    // Check if the time difference is less than or equal to 600,000 milliseconds (10 minutes)
    return timeDifferenceMillis <= 600000
}
}


