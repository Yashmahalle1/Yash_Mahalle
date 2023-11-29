package parkingmanagement

import grails.converters.JSON
import parkingmanagement.UserService

class UserController {
	
	UserService userService
	
		// User registration
		def register() {
			try {
				// Parse the incoming JSON data from the HTTP request
				def requestJson = request.JSON
	
				// Call the service method to handle user registration
				def result = userService.registerUser(requestJson)
	
				if (result.status == 'success') {
					// Registration was successful, construct a success response
					def responseBody = [
						status: 'success',
						message: 'User registered successfully.',
						data: [
							 	userId: result.data.id,
								userName: result.data.userName,
								firstName: result.data.firstName,
							    lastName: result.data.lastName,
								phoneCountryCode: result.data.phoneCountryCode,
								phoneNumber: result.data.phoneNumber,
							]
					]
	
					render status: 201, contentType: 'application/json', text: responseBody as JSON
				} else {
					// Registration failed, construct an error response with details
					def responseBody = [
						status: 'error',
						message: 'User registration failed.',
						errors: result.errors
					]
	
					render status: 400, contentType: 'application/json', text: responseBody as JSON
				}
			} catch (Exception e) {
				// Handle any unhandled exceptions that occurred during registration
				render status: 500, contentType: 'application/json',
					text: [
						status: 'error',
						message: 'User registration failed.',
						errors: ['An error occurred during registration.']
					] as JSON
			}
		}
	
		// User login
		def login() {
			try {
				// Parse the request JSON
				def requestJson = request.JSON
				def username = requestJson.userName
				def password = requestJson.password
	
				// Authenticate the user
				def user = userService.authenticateUser(username, password)
	
				if (user) {
					// Generate a token for the authenticated user
					def token = userService.generateTokenForUser(user)
	
					// Prepare the response body
					def responseBody = [
						status: 'success',
						message: 'User authenticated successfully.',
//						data: [
//							userId: user.id,
//							userName: user.userName,
//							token: token
//						]
					]
	
					// Respond with a success status (200) and JSON
					render status: 200, contentType: 'application/json', text: responseBody as JSON
				} else {
					// User authentication failed
					def responseBody = [
						status: 'error',
						message: 'Authentication failed. Invalid username or password.'
					]
	
					// Respond with an error status (401) and JSON
					render status: 401, contentType: 'application/json', text: responseBody as JSON
				}
			} catch (Exception e) {
				// Handle exceptions
				def responseBody = [
					status: 'error',
					message: 'Authentication failed.',
					errors: [e.message]
				]
	
				// Respond with an error status (500) and JSON
				render status: 500, contentType: 'application/json', text: responseBody as JSON
			}
		}
	
		// List all users
		def listUsers() {
			try {
				// Retrieve a list of users from the database (you may need to customize this query)
				def users = User.list()
	
				// Create a list to hold user details
				def userDetails = []
	
				// Iterate through the users and extract their details
				users.each { user ->
					def userDetail = [
//						userId: user.id,
						userName: user.userName,
						firstName: user.firstName,
						lastName: user.lastName,
						phoneCountryCode: user.phoneCountryCode,
						phoneNumber: user.phoneNumber
					]
					userDetails << userDetail
				}
	
				// Construct the success response
				def responseBody = [
					status: 'success',
					message: 'User list retrieved successfully.',
					data: userDetails
				]
	
				render status: 200, contentType: 'application/json', text: responseBody as JSON
			} catch (Exception e) {
				// Handle any exceptions here and return an error response
				render status: 500, contentType: 'application/json',
					text: [
						status: 'error',
						message: 'Failed to retrieve user list.',
						errors: ['An error occurred while fetching user data.']
					] as JSON
			}
		}
	
		// Show a specific user by ID
		def showUser(Long userId) {
			try {
				// Retrieve the user from the database by ID
				def user = User.get(userId)
	
				if (user) {
					// Construct the success response
					def responseBody = [
						status: 'success',
						message: 'User retrieved successfully.',
						data: [
//							userId: user.id,
							userName: user.userName,
							firstName: user.firstName,
							lastName: user.lastName,
							phoneCountryCode: user.phoneCountryCode,
							phoneNumber: user.phoneNumber,
						]
					]
	
					render status: 200, contentType: 'application/json', text: responseBody as JSON
				} else {
					// User not found
					render status: 404, contentType: 'application/json',
						text: [
							status: 'error',
							code: 'user_not_found',
							message: "The user with ID ${userId} was not found.",
							details: "The requested user does not exist in the system."
						] as JSON
				}
			} catch (Exception e) {
				// Handle any exceptions here and return an error response
				render status: 500, contentType: 'application/json',
					text: [
						status: 'error',
						message: 'Failed to retrieve user.',
						errors: ['An error occurred while fetching user data.']
					] as JSON
			}
		}
	
		// Update user information
		def updateUser(Long userId) {
			try {
				// Parse the request JSON
				def requestJson = request.JSON
	
				def result = userService.updateUser(userId, requestJson)
	
				if (result.status == "success") {
					// User updated successfully
					def responseBody = [
						status: 'success',
						message: 'User updated successfully',
						data :[
							userName: result.data.userName,
							firstName: result.data.firstName,
							lastName: result.data.lastName,
							phoneCountryCode: result.data.phoneCountryCode,
							phoneNumber: result.data.phoneNumber,
							]
					]
	
					render status: 200, contentType: 'application/json', text: responseBody as JSON
				} else {
					// User update failed
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
						message: 'User update failed',
						errors: ['An error occurred during update']
					] as JSON
			}
		}
	
		// Delete user by ID
		def deleteUser(Long userId) {
			try {
				// Attempt to retrieve the user with the specified userId
				def user = User.get(userId)
				if (user) {
					// If the user exists, delete it from the database with flushing
					user.delete(flush: true)
	
					// Create a success response body
					def responseBody = [
						status: 'success',
						message: 'User deleted successfully',
						data: [
							userId: user.id,
						   userName: user.userName,
						   ]
					]
					// Respond with a 200 OK status and JSON content type
					render status: 200, contentType: 'application/json', text: responseBody as JSON
				} else {
					// If the user does not exist, create an error response
					def responseBody = [
						status: 'error',
						message: "User with ID $userId not found"
					]
					// Respond with a 404 Not Found status and JSON content type
					render status: 404, contentType: 'application/json', text: responseBody as JSON
				}
			} catch (Exception e) {
				// Catch any exceptions that occur during the process
				render status: 500, contentType: 'application/json',
					text: [
						status: 'error',
						message: 'User deletion failed',
						errors: ['An error occurred during deletion']
					] as JSON
			}
		}
}
