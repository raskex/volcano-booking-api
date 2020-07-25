# volcano-booking-api

##### REST API service that manages the campsite reservations.

#### Assumptions

- The campsite capacity is configured for max 10 guests.
- Bookings can be edited/cancelled anytime as long as checkin date is not a past day.
- The campsite can be reserved for max 3 days.
- The campsite can be reserved minimum 1 day ahead of arrival and up to 1 month in advance.

### Endpoints

##### Availability

- GET /availability/from=yyyy-mm-dd&to=yyyy-mm-dd

> Return the availability between the requested optional dates "from" and "to".
* if "from" param is missing, then it is completed with 'tomorrow' by default.
* if "to" param is missing, then it is completed with '1 month' by default.


##### Booking

- GET /booking/{bookingId}

> Return the requested booking if exists.

---

- POST /booking

> Creates a booking with the following mandatory information in the body:

	"fromDay": "yyyy-mm-dd",
	"toDay": "yyyy-mm-dd"
	"guests": number > 0,
    "firstName": "text",
    "lastName": "text",
    "email": "email format"

---

- PUT /booking/{bookingId}

> Updates the given bookingId (if exists) with the following mandatory information in the body:

	"fromDay": "yyyy-mm-dd",
	"toDay": "yyyy-mm-dd"
	"guests": number > 0,
    "firstName": "text",
    "lastName": "text",
    "email": "email format"

---

- DELETE /booking/{bookingId}

> Remove the given bookingId if exists.

### Run the application:

mvn spring-boot:run

### Test the application:

mvn test
