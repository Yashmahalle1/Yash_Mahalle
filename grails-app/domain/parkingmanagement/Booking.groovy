package parkingmanagement

class Booking {
	
	User user
	ParkingSlot parkingSlot
	Date startTime
	Date endTime
	String status
	String vehicleCategory
	String vehiclePlateNumber
	
    static constraints = {
		
		user nullable: false
		parkingSlot nullable: true
		startTime nullable: false
		endTime nullable: false
		status inList: ["Pending", "Confirmed", "Cancelled"]
		vehicleCategory inList: ["twoWheeler", "fourWheeler"]
		vehiclePlateNumber size: 1..20
    }
}
