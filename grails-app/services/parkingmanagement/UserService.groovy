package parkingmanagement


class UserService {
	
	// Register a new user
	def registerUser(Map<String, String> userData) {
		// Check if the 'token' field is null in the userData
		if (userData.token == null) {
			userData.token = '' // Set token to a default value, e.g., an empty string
		}

		// Create a new User instance using the provided user data
		def user = new User(userData)

		if (user.validate()) {
			// If validation succeeds, attempt to save the user to the database with a flush
			if (user.save(flush: true)) {
				// Registration was successful, return a success response
				return [status: 'success', message: 'User registered successfully.', data: user]
			} else {
				// Saving the user to the database failed, return an error response with validation errors
				return [status: 'error', message: 'User registration failed.', errors: user.errors]
			}
		} else {
			// Validation of user data failed, return an error response with validation errors
			return [status: 'error', message: 'User registration failed.', errors: user.errors]
		}
	}

	// Generate a token for the user
	def generateTokenForUser(User user) {
		// Generate a custom token, e.g., a UUID
		def token = UUID.randomUUID().toString()
		user.token = token
		user.save(flush: true)
		return token
	}

	// Authenticate a user based on username and password
	def authenticateUser(String username, String password) {
		def user = User.findByUserName(username)

		if (user && user.isPasswordValid(password)) {
			return user
		}

		return null
	}

	// Update user information
	def updateUser(Long userId, Map<String, String> userData) {
		try {
			def user = User.get(userId)

			if (user) {
				// Update user information based on the request data
				user.properties = userData

				// Save the updated user
				if (user.save(flush: true)) {
					return [status: "success", message: "User updated successfully.", data: user]
				} else {
					def errors = user.errors.allErrors.collect { it.defaultMessage }
					return [status: "error", message: "User update failed.", errors: errors]
				}
			} else {
				return [status: "error", message: "User not found."]
			}
		} catch (Exception e) {
			return [status: "error", message: "User update failed.", errors: ['An error occurred during update.']]
		}
	}

}

