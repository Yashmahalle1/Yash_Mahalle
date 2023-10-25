package parkingmanagement

import grails.converters.JSON


class ParkingSlotService {

	// Create a new parking slot
	def createParkingSlot(Map parkingSlotData) {
		try {
			// Check if the slot already exists
			if (ParkingSlot.findBySlotName(parkingSlotData.slotName)) {
				return null // Slot with the same name already exists
			}

			// Create a new parking slot and set its properties
			def parkingSlot = new ParkingSlot(parkingSlotData)

			// Validate and save the parking slot
			if (parkingSlot.validate() && parkingSlot.save(flush: true)) {
				return parkingSlot
			}
		} catch (Exception e) {
			// Handle any exceptions here and log them for debugging
			e.printStackTrace()
		}
		return null // Validation or saving failed
	}
	
	// Update parking slot status
	def updateParkingSlot(Long id, Map parkingSlotData) {
		try {
			def parkingSlot = ParkingSlot.get(id)
	
			if (parkingSlot) {
				// Update the slotStatus if it's present in the request data
				if (parkingSlotData.slotStatus) {
					parkingSlot.slotStatus = parkingSlotData.slotStatus
	
					
					// Save the updated parking slot status
					if (parkingSlot.save(flush: true)) {
						return [status: "success", message: "Parking slot status updated successfully.", data: parkingSlot]
					} else {
						def errors = parkingSlot.errors.allErrors.collect { it.defaultMessage }
						return [status: "error", message: "Parking slot status update failed.", errors: errors]
					}
				} else {
					return [status: "error", message: "No slotStatus provided in the request."]
				}
			} else {
				return [status: "error", message: "Parking slot not found."]
			}
		} catch (Exception e) {
			return [status: "error", message: "Parking slot status update failed.", errors: ['An error occurred during update.']]
		}
	}
	
}
