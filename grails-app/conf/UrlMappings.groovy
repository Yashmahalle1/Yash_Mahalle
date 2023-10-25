class UrlMappings {

	static mappings = {
		
		"/api/users/register"(controller: "user") {action=[POST:"register"]}
		
		"/api/users"(controller: "user") {action=[POST:"login",GET:"listUsers"]}
		
		"/api/users/$userId"(controller: "user") {action=[GET:"showUser",PUT:"updateUser",DELETE:"deleteUser"]}
		
		"/api/parkingSlots"(controller: "parkingSlot") {action=[GET:"listParkingSlots",POST:"create"]}
		
		"/api/parkingSlots/$id"(controller: "parkingSlot") {action=[GET:"showParkingSlot",DELETE:"deleteParkingSlot",PUT:"updateParkingSlot"]}

		//"/api/bookings"(controller: "booking") {action=[POST:"step1",POST:"step2",POST:"extendEndTime",GET:"bookingList"]}
		
		"/api/bookings/$id?"(controller: "booking") {action=[GET:"showBooking",DELETE:"deleteBooking"]}
		
		
		"/api/booking/step1"(controller: "booking", action: "step1", method: "POST") {
		}

		"/api/booking/step2"(controller: "booking", action: "step2", method: "POST") {
		}

		"/api/booking/extendEndTime"(controller: "booking", action: "extendEndTime", method: "POST") {
		}

		"/api/booking"(controller: "booking", action: "bookingList", method: "GET") {
		}

		


		"/$controller/$action?/$id?" {
			constraints {
				// apply constraints here
			}
		}

		"/"(view: "/index")
		"500"(view: '/error')
	}
		
}

