const roomForm = document.getElementById("roomForm");
const teamForm = document.getElementById("teamForm");
const userForm = document.getElementById("userForm");

const roomTableBody = document.getElementById("roomTableBody");
const teamTableBody = document.getElementById("teamTableBody");

// Reusable POST request
async function postData(url, values) {

    const formData = new URLSearchParams();

    Object.entries(values).forEach(([key, value]) => {
        formData.append(key, value);
    });

    const response = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: formData
    });

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.error || "Request failed.");
    }

    return data;
}

// Display messages
function showMessage(elementId, message, isError = false) {

    const element = document.getElementById(elementId);

    element.textContent = message;
    element.classList.toggle("error", isError);
}

// Load rooms
async function loadRooms() {

    try {

        const response = await fetch("rooms");
        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || "Unable to load rooms.");
        }

        roomTableBody.replaceChildren();

        if (data.rooms.length === 0) {
            roomTableBody.textContent = "No rooms available.";
            return;
        }

        data.rooms.forEach(room => {

            const row = document.createElement("tr");

            [
                room.roomId,
                room.roomName,
                room.capacity,
                room.location,
                room.facilities,
                room.status
            ].forEach(value => {

                const cell = document.createElement("td");
                cell.textContent = value ?? "";
                row.appendChild(cell);
            });

            const actionCell = document.createElement("td");

            if (room.status === "ACTIVE") {

                const button = document.createElement("button");
                button.textContent = "Deactivate";
                button.className = "action-btn";

                button.addEventListener("click", () => {
                    deactivateRoom(room.roomId);
                });

                actionCell.appendChild(button);
            } else {
                actionCell.textContent = "Inactive";
            }

            row.appendChild(actionCell);
            roomTableBody.appendChild(row);
        });

    } catch (error) {
        roomTableBody.textContent = error.message;
    }
}

// Add room
roomForm.addEventListener("submit", async function(event) {

    event.preventDefault();

    try {

        const data = await postData("rooms", {
            action: "add",
            roomName: document.getElementById("roomName").value.trim(),
            capacity: document.getElementById("capacity").value,
            location: document.getElementById("location").value.trim(),
            facilities: document.getElementById("facilities").value.trim()
        });

        showMessage("roomMessage", data.message);
        roomForm.reset();

        loadRooms();

    } catch (error) {
        showMessage("roomMessage", error.message, true);
    }
});

// Deactivate room
async function deactivateRoom(roomId) {

    if (!confirm("Are you sure you want to deactivate this room?")) {
        return;
    }

    try {

        await postData("rooms", {
            action: "deactivate",
            roomId: roomId
        });

        loadRooms();

    } catch (error) {
        showMessage("roomMessage", error.message, true);
    }
}

// Load teams
async function loadTeams() {

    try {

        const response = await fetch("teams");
        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || "Unable to load teams.");
        }

        teamTableBody.replaceChildren();

        if (data.teams.length === 0) {
            teamTableBody.textContent = "No teams available.";
            return;
        }

        data.teams.forEach(team => {

            const row = document.createElement("tr");

            [team.teamId, team.teamName, team.department]
                .forEach(value => {

                    const cell = document.createElement("td");
                    cell.textContent = value ?? "";
                    row.appendChild(cell);
                });

            teamTableBody.appendChild(row);
        });

    } catch (error) {
        teamTableBody.textContent = error.message;
    }
}

// Add team
teamForm.addEventListener("submit", async function(event) {

    event.preventDefault();

    try {

        const data = await postData("teams", {
            teamName: document.getElementById("teamName").value.trim(),
            department: document.getElementById("department").value.trim()
        });

        showMessage("teamMessage", data.message);
        teamForm.reset();

        loadTeams();

    } catch (error) {
        showMessage("teamMessage", error.message, true);
    }
});

// Register user
userForm.addEventListener("submit", async function(event) {

    event.preventDefault();

    const role = document.getElementById("role").value;
    const teamId = document.getElementById("userTeam").value;

    if (role !== "ADMIN" && (!teamId || Number(teamId) <= 0)) {
        showMessage("userMessage", "Enter a valid Team ID.", true);
        return;
    }

    try {

        const data = await postData("users", {
            name: document.getElementById("name").value.trim(),
            email: document.getElementById("email").value.trim(),
            password: document.getElementById("password").value,
            role: role,
            teamId: teamId
        });

        showMessage("userMessage", data.message);
        userForm.reset();

    } catch (error) {
        showMessage("userMessage", error.message, true);
    }
});

// Logout
document.getElementById("logoutBtn").addEventListener("click", function() {
    window.location.href = "logout";
});

// Initial dashboard loading
loadRooms();
loadTeams();
