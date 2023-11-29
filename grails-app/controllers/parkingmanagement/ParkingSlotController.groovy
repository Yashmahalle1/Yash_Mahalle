package parkingmanagement

import grails.converters.JSON


class ParkingSlotController {
	
	// Inject the service
	def parkingSlotService

	// Create a new parking slot
	def create() {
		try {
			// Parse the request JSON
			def requestJson = request.JSON

			// Call the service to create the parking slot
			def parkingSlot = parkingSlotService.createParkingSlot(requestJson)

			if (parkingSlot) {
				def responseBody = [
					status: 'success',
					message: 'Parking slot created successfully.',
					data:[
						 id: parkingSlot.id,
						 slotName:parkingSlot.slotName,
						 slotCategory:parkingSlot.slotCategory,
						 typeOfSlot:parkingSlot.typeOfSlot
						 ]
				]
				render status: 201, contentType: 'application/json', text: responseBody as JSON
			} else {
				def responseBody = [
					status: 'error',
					message: 'Parking slot creation failed.',
					errors: 'Invalid data or slot already exists.'
				]
				render status: 400, contentType: 'application/json', text: responseBody as JSON
			}
		} catch (Exception e) {
			def responseBody = [
				status: 'error',
				message: 'Parking slot creation failed.',
				errors: [e.message]
			]
			render status: 500, contentType: 'application/json', text: responseBody as JSON
		}
	}

	// List all parking slots
	def listParkingSlots() {
		try {
			// Retrieve a list of parking slots from the database
			def parkingSlots = ParkingSlot.list()

			// Create a list to hold parking slot details
			def parkingSlotDetails = []

			// Iterate through the parking slots and extract their details
			parkingSlots.each { parkingSlot ->
				def parkingSlotDetail = [
					slotId: parkingSlot.id,
					slotName: parkingSlot.slotName,
					slotCategory: parkingSlot.slotCategory,
					slotAvailable: parkingSlot.slotAvailable,
					typeOfSlot: parkingSlot.typeOfSlot,
					slotStatus: parkingSlot.slotStatus
				]
				parkingSlotDetails << parkingSlotDetail
			}

			// Construct the success response
			def responseBody = [
				status: 'success',
				message: 'Parking slot list retrieved successfully.',
				data: parkingSlotDetails
			]

			render status: 200, contentType: 'application/json', text: responseBody as JSON
		} catch (Exception e) {
			// Handle any exceptions here and return an error response
			render status: 500, contentType: 'application/json',
				text: [
					status: 'error',
					message: 'Failed to retrieve parking slot list.',
					errors: ['An error occurred while fetching parking slot data.']
				] as JSON
		}
	}

	// Show details of a specific parking slot
	def showParkingSlot(Long id) {
		try {
			// Retrieve the parking slot from the database by ID
			def parkingSlot = ParkingSlot.get(id)

			if (parkingSlot) {
				// Construct the success response
				def responseBody = [
					status: 'success',
					message: 'Parking slot retrieved successfully.',
					data: [
						slotId: parkingSlot.id,
						slotName: parkingSlot.slotName,
						slotCategory: parkingSlot.slotCategory,
						slotAvailable: parkingSlot.slotAvailable,
						typeOfSlot: parkingSlot.typeOfSlot,
						slotStatus: parkingSlot.slotStatus,
					]
				]

				render status: 200, contentType: 'application/json', text: responseBody as JSON
			} else {
				// Parking slot not found
				render status: 404, contentType: 'application/json',
					text: [
						status: 'error',
						code: 'parking_slot_not_found',
						message: "The parking slot with ID ${slotId} was not found.",
						details: "The requested parking slot does not exist in the system."
					] as JSON
			}
		} catch (Exception e) {
			// Handle any exceptions here and return an error response
			render status: 500, contentType: 'application.json',
				text: [
					status: 'error',
					message: 'Failed to retrieve parking slot.',
					errors: ['An error occurred while fetching parking slot data.']
				] as JSON
		}
	}

	// Delete a specific parking slot by its ID
	def deleteParkingSlot(Long id) {
		try {
			// Retrieve the parking slot by ID
			def parkingSlot = ParkingSlot.get(id)

			if (parkingSlot) {
				// Delete the parking slot from the database with flushing
				parkingSlot.delete(flush: true)

				// Create a success response body
				def responseBody = [
					status: 'success',
					message: 'Parking slot deleted successfully.',
					data: [
						slotName: parkingSlot.slotName,
						slotCategory: parkingSlot.slotCategory,
						typeOfSlot: parkingSlot.typeOfSlot,
					]
				]

				// Respond with a 200 OK status and JSON content type
				render status: 200, contentType: 'application/json', text: responseBody as JSON
			} else {
				// Parking slot not found
				def responseBody = [
					status: 'error',
					message: "Parking slot with ID $id not found."
				]

				// Respond with a 404 Not Found status and JSON content type
				render status: 404, contentType: 'application/json', text: responseBody as JSON
			}
		} catch (Exception e) {
			// Handle any exceptions here and return an error response
			render status: 500, contentType: 'application/json',
				text: [
					status: 'error',
					message: 'Parking slot deletion failed.',
					errors: ['An error occurred during deletion.']
				] as JSON
		}
	}

	// Update parking slot status
	def updateParkingSlot(Long id) {
		try {
			// Parse the request JSON
			def requestJson = request.JSON

			def result = parkingSlotService.updateParkingSlot(id, requestJson)

			if (result.status == "success") {
				// Parking slot status updated successfully
				def responseBody = [
					status: 'success',
					message: 'Parking slot status updated successfully.',
					data: [
						id: result.data.id,
						slotName:result.data.slotName,
						slotStatus:result.data.slotStatus,
						]
				]

				render status: 200, contentType: 'application/json', text: responseBody as JSON
			} else {
				// Parking slot status update failed
				def responseBody = [
					status: 'error',
					message: result.message,
					errors: result.errors
				]

				render status: 400, contentType: 'application/json', text: responseBody as JSON
			}
		} catch (Exception e) {
			// Handle any exceptions here and return an error response
			render status: 500, contentType: 'application/json',
				text: [
					status: 'error',
					message: 'Parking slot status update failed.',
					errors: ['An error occurred during update.']
				] as JSON
		}
	}
}
