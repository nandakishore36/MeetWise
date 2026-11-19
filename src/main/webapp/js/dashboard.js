
const availabilityForm = document.getElementById("availabilityForm");
const roomResults = document.getElementById("roomResults");
const bookingResults = document.getElementById("bookingResults");
const availabilityMessage = document.getElementById("availabilityMessage");
const bookingMessage = document.getElementById("bookingMessage");
const bookBestRoomBtn = document.getElementById("bookBestRoomBtn");
const dateInput = document.getElementById("meetingDate");

let currentUser = null;

// ==========================================
// 1. LOAD LOGGED-IN USER INFORMATION
// ==========================================

async function loadSessionInfo() {
    try {
        const response = await fetch("sessionInfo");

        if (!response.ok) {
            window.location.href = "login.html";
            return false;
        }

        currentUser = await response.json();

        if (currentUser.userRole === "ADMIN") {
            window.location.href = "admin-dashboard.html";
            return false;
        }

        const welcomeElement = document.getElementById("userWelcome");

        if (welcomeElement) {
            welcomeElement.textContent = "Welcome, " + currentUser.userName;
        }

        // Only Team Leads can create bookings.
        bookBestRoomBtn.hidden = true;

        return true;

    } catch (error) {
        bookingMessage.textContent = "Unable to verify your login session.";
        return false;
    }
}

// ==========================================
// 2. DATE VALIDATION
// ==========================================

function formatLocalDate(date) {
    return [
        date.getFullYear(),
        String(date.getMonth() + 1).padStart(2, "0"),
        String(date.getDate()).padStart(2, "0")
    ].join("-");
}

const today = new Date();
dateInput.min = formatLocalDate(today);

const lastDate = new Date(today);
lastDate.setDate(lastDate.getDate() + 7);
dateInput.max = formatLocalDate(lastDate);

// ==========================================
// 3. COLLECT BOOKING DETAILS
// ==========================================

function getBookingFormData() {
    const formData = new URLSearchParams();

    formData.append("meetingDate", dateInput.value);
    formData.append("startTime", document.getElementById("startTime").value);
    formData.append("endTime", document.getElementById("endTime").value);
    formData.append("attendees", document.getElementById("attendees").value);
    formData.append(
        "requiredFacilities",
        document.getElementById("facilities").value.trim()
    );

    return formData;
}

// ==========================================
// 4. SEARCH AVAILABLE ROOMS
// ==========================================

availabilityForm.addEventListener("submit", async function(event) {
    event.preventDefault();

    availabilityMessage.textContent = "Searching for available rooms...";
    bookingMessage.textContent = "";
    roomResults.replaceChildren();
    bookBestRoomBtn.hidden = true;

    try {
        const response = await fetch("availability", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: getBookingFormData()
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || "Unable to search rooms.");
        }

        displayRooms(data.rooms);

    } catch (error) {
        availabilityMessage.textContent = error.message;
    }
});

// ==========================================
// 5. DISPLAY AVAILABLE ROOMS
// ==========================================

function displayRooms(rooms) {
    roomResults.replaceChildren();
    bookBestRoomBtn.hidden = true;

    if (!rooms || rooms.length === 0) {
        availabilityMessage.textContent = "No suitable rooms found.";
        return;
    }

    availabilityMessage.textContent =
        rooms.length + " suitable room(s) found.";

    rooms.forEach(room => {
        const card = document.createElement("div");
        card.className = "room-card";

        const name = document.createElement("h3");
        name.textContent = room.roomName;

        const capacity = document.createElement("p");
        capacity.textContent = "Capacity: " + room.capacity;

        const location = document.createElement("p");
        location.textContent =
            "Location: " + (room.location || "Not specified");

        const facilities = document.createElement("p");
        facilities.textContent =
            "Facilities: " + (room.facilities || "Not specified");

        card.append(name, capacity, location, facilities);
        roomResults.appendChild(card);
    });

    // Employees can search, but only Team Leads can book.
    if (currentUser && currentUser.userRole === "TEAM_LEAD") {
        bookBestRoomBtn.hidden = false;
    }
}

// ==========================================
// 6. CREATE BOOKING - TEAM LEAD ONLY
// ==========================================

bookBestRoomBtn.addEventListener("click", async function() {

    if (!currentUser || currentUser.userRole !== "TEAM_LEAD") {
        bookingMessage.textContent =
            "Only Team Leads can create bookings.";
        return;
    }

    if (!availabilityForm.reportValidity()) {
        return;
    }

    const confirmed = confirm("Confirm your meeting room booking?");

    if (!confirmed) {
        return;
    }

    bookBestRoomBtn.disabled = true;
    bookingMessage.textContent = "Creating your booking...";

    try {
        const response = await fetch("book", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: getBookingFormData()
        });

        const message = await response.text();

        if (!response.ok) {
            throw new Error(message || "Booking failed.");
        }

        bookingMessage.textContent = message;
        bookBestRoomBtn.hidden = true;

        await loadTeamBookings();
        availabilityForm.requestSubmit();

    } catch (error) {
        bookingMessage.textContent = error.message;

    } finally {
        bookBestRoomBtn.disabled = false;
    }
});

// ==========================================
// 7. LOAD TEAM BOOKINGS
// ==========================================

async function loadTeamBookings() {
    try {
        const response = await fetch("teamBookings");
        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || "Unable to load bookings.");
        }

        displayBookings(data.bookings);

    } catch (error) {
        bookingResults.textContent = error.message;
    }
}

// ==========================================
// 8. DISPLAY TEAM BOOKINGS
// ==========================================

function displayBookings(bookings) {
    bookingResults.replaceChildren();

    if (!bookings || bookings.length === 0) {
        bookingResults.textContent =
            "No bookings found for your team.";
        return;
    }

    bookings.forEach(booking => {
        const item = document.createElement("div");
        item.className = "booking-item";

        const details = document.createElement("div");
        details.className = "booking-details";

        const title = document.createElement("h3");
        title.textContent = "Booking #" + booking.bookingId;

        const date = document.createElement("p");
        date.textContent = "Date: " + booking.meetingDate;

        const time = document.createElement("p");
        time.textContent =
            "Time: " + booking.startTime + " - " + booking.endTime;

        const attendees = document.createElement("p");
        attendees.textContent = "Attendees: " + booking.attendees;

        const room = document.createElement("p");
        room.textContent = "Room ID: " + booking.roomId;

        const status = document.createElement("span");
        status.className = "status";
        status.textContent = booking.status;

        if (booking.status === "CANCELLED") {
            status.classList.add("cancelled");
        }

        details.append(title, date, time, attendees, room, status);
        item.appendChild(details);

        // Only the Team Lead who created this booking can see its cancel button.
        const isOwnBooking =
            currentUser &&
            currentUser.userRole === "TEAM_LEAD" &&
            Number(booking.bookedBy) === Number(currentUser.userId);

        if (booking.status === "CONFIRMED" && isOwnBooking) {
            const cancelButton = document.createElement("button");

            cancelButton.type = "button";
            cancelButton.textContent = "Cancel Booking";
            cancelButton.className = "action-btn";

            cancelButton.addEventListener("click", function() {
                cancelBooking(booking.bookingId, cancelButton);
            });

            item.appendChild(cancelButton);
        }

        bookingResults.appendChild(item);
    });
}

// ==========================================
// 9. CANCEL BOOKING - OWN BOOKINGS ONLY
// ==========================================

async function cancelBooking(bookingId, button) {

    if (!currentUser || currentUser.userRole !== "TEAM_LEAD") {
        bookingMessage.textContent =
            "You are not allowed to cancel bookings.";
        return;
    }

    const confirmed = confirm(
        "Are you sure you want to cancel this booking?"
    );

    if (!confirmed) {
        return;
    }

    button.disabled = true;

    try {
        const formData = new URLSearchParams();
        formData.append("bookingId", bookingId);

        const response = await fetch("cancelBooking", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: formData
        });

        const message = await response.text();

        if (!response.ok) {
            throw new Error(message || "Cancellation failed.");
        }

        bookingMessage.textContent = message;

        await loadTeamBookings();

        if (dateInput.value) {
            availabilityForm.requestSubmit();
        }

    } catch (error) {
        bookingMessage.textContent = error.message;
        button.disabled = false;
    }
}

// ==========================================
// 10. LOGOUT
// ==========================================

document.getElementById("logoutBtn").addEventListener("click", function() {
    window.location.href = "logout";
});

// ==========================================
// 11. INITIAL DASHBOARD LOAD
// ==========================================

async function initializeDashboard() {
    const sessionValid = await loadSessionInfo();

    if (!sessionValid) {
        return;
    }

    await loadTeamBookings();
}

initializeDashboard();
