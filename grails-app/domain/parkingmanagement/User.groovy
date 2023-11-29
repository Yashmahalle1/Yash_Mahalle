package parkingmanagement

class User {

	String userName
	String password
	String token
	String phoneCountryCode
	String phoneNumber
	String firstName
	String lastName
    static constraints = {
		userName unique: true, size: 3..255
        password size: 6..255
        phoneCountryCode size: 1..10
        phoneNumber size: 1..20
        firstName size: 2..255
        lastName size: 2..255
    }
	
	boolean isPasswordValid(String rawPassword) {
        return password == rawPassword
    }

}