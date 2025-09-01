const NOTIFICATION_API_URL = 'http://localhost:4323'; // Notification service port


function sendJoinRequest(teamId) {
    // Generate a standard request message
    const description = `${currentUser.username} would like to join your team.`;

    fetch(`${NOTIFICATION_API_URL}/api/notifications/request-to-join`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${authToken}`,
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams({
            'teamId': teamId,
            'description': description
        })
    })
        .then(response => {
            if (response.ok) {
                alert('Join request sent successfully!');
                loadCourses();
            } else {
                alert('Failed to send join request');
            }
        })
        .catch(error => {
            console.error('Error sending join request:', error);
            alert('Failed to send join request');
        });
}

function viewNotifications(teamId, teamName) {
    fetch(`${NOTIFICATION_API_URL}/api/notifications/team/${teamId}`, {
        headers: {
            'Authorization': `Bearer ${authToken}`
        }
    })
        .then(res => res.json())
        .then(data => {
            const container = document.getElementById('main-content');
            container.innerHTML = `<h2>Notifications for Team: ${teamName}</h2>`;

            if (data.length === 0) {
                container.innerHTML += '<p>No notifications for this team</p>';
                return;
            }

            data.forEach(notification => {
                const div = document.createElement('div');
                div.className = notification.type === 'REQUEST_TO_JOIN'
                    ? 'notification-card request'
                    : 'notification-card message';

                let notificationContent = `
        <div class="notification-type ${notification.type === 'REQUEST_TO_JOIN' ? 'request' : 'message'}">
            ${notification.type === 'REQUEST_TO_JOIN' ? 'Join Request' : 'Message'}
        </div>
        <p>${notification.description}</p>
        `;

                // Add action buttons for join requests
                if (notification.type === 'REQUEST_TO_JOIN') {
                    notificationContent += `
            <div class="notification-actions">
            <button class="accept-btn" onclick="acceptJoinRequest(${notification.id}, ${teamId}, ${notification.fromStudentId})">Accept</button>
            <button class="reject-btn" onclick="rejectJoinRequest(${notification.id}, ${teamId})">Reject</button>
            </div>
        `;
                }

                div.innerHTML = notificationContent;
                container.appendChild(div);
            });
        })
        .catch(error => {
            console.error('Error fetching notifications:', error);
            const container = document.getElementById('main-content');
            container.innerHTML = `
        <h2>Notifications for Team: ${teamName}</h2>
        <p>Error loading notifications</p>
    `;
        });
}

function acceptJoinRequest(notificationId, teamId, fromStudentId) {
    addToTeam(fromStudentId, teamId)
    fetch(`${NOTIFICATION_API_URL}/api/notifications/${notificationId}/accept`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${authToken}`
        }
    })
        .then(response => {
            if (response.ok) {
                // Reload notifications
                loadYourTeams();
            } else {
                if (response.status === 400) {
                    return response.text().then(text => {
                        throw new Error(text);
                    });
                }
                throw new Error('Failed to accept join request');
            }
        });
        // .then(res => res.json())
        // .then(team => {
        //     viewNotifications(teamId, team.teamName);
        // })
        // .catch(error => {
        //     console.error('Error accepting join request:', error);
        //     alert(error.message || 'Failed to accept join request');
        // });
}

function rejectJoinRequest(notificationId, teamId) {
    fetch(`${NOTIFICATION_API_URL}/api/notifications/${notificationId}/reject`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${authToken}`
        }
    })
        .then(response => {
            if (response.ok) {
                alert('Join request rejected');
                // Reload notifications
                loadYourTeams();
            } else {
                throw new Error('Failed to reject join request');
            }
        })
        .catch(error => {
            console.error('Error rejecting join request:', error);
            alert('Failed to reject join request');
        });
}
