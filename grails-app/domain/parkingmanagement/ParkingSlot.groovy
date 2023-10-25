package parkingmanagement

class ParkingSlot {
	
	String slotName
	String slotCategory
	boolean slotAvailable
	String typeOfSlot
	String slotStatus

	
    static constraints = {
		slotName blank: false, nullable: false
        slotCategory(inList: ["twoWheeler", "fourWheeler"])
        slotAvailable(nullable: false)
        typeOfSlot(inList: ["Ground Floor", "Ramp"])
		slotStatus(inList: ["In Use", "Under Maintenance"])
    }
}
