package parkingmanagement

class BookingExtension {
	
	Booking booking
	Date extendedEndTime

    static constraints = {
		
		booking unique: 'extendedEndTime'
		
    }
}
