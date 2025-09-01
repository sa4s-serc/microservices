const COURSE_API_URL = 'http://localhost:4321'; // Course service port

function loadCourses() {
    fetch(`${COURSE_API_URL}/api/courses`)
    .then(res => res.json())
    .then(data => {
        const container = document.getElementById('main-content');
        container.innerHTML = '<h2>All Courses</h2>';
        data.forEach(course => {
        const div = document.createElement('div');
        div.className = 'card';
        div.innerHTML = `
            <h3>${course.courseName}</h3>
            <button onclick="registerCourse(${course.courseId})">Register</button>
            <button onclick="viewTeams(${course.courseId}, '${course.courseName}')">View Teams</button>
        `;
        container.appendChild(div);
        });
    });
}

function showAddCourseForm() {
    const container = document.getElementById('main-content');
    container.innerHTML = `
      <h2>Add New Course</h2>
      <div class="card">
        <label>Course Name:</label><br/>
        <input type="text" id="course-name" placeholder="Enter course name" /><br/><br/>
        <label>Team Size:</label><br/>
        <input type="number" id="team-size" placeholder="Enter team size" /><br/><br/>
        <button onclick="addCourse()">Add Course</button>
      </div>
    `;
  }
  
function addCourse() {
    const courseName = document.getElementById('course-name').value;
    const teamSize = parseInt(document.getElementById('team-size').value);

    if (!courseName || !teamSize) {
        alert('Please fill out all fields');
        return;
    }

    fetch(`${COURSE_API_URL}/api/courses`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${authToken}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ courseName, teamSize })
    })
    .then(response => {
        if(response.ok){
            alert('Course added successfully!');
            loadCourses();
        } else {
            return response.text().then(errorMessage => {
                alert(`Failed to add course: ${errorMessage}`);
            });
        }
    })
    .catch(err => alert(err.message));
}

function showDeleteCourseForm() {
    const container = document.getElementById('main-content');
    container.innerHTML = `
    <h2>Delete Course</h2>
    <div class="card">
        <label>Course ID:</label><br/>
        <input type="number" id="delete-course-id" placeholder="Enter course ID" /><br/><br/>
        <button onclick="deleteCourse()">Delete Course</button>
    </div>
    `;
}

function deleteCourse() {
    const courseId = parseInt(document.getElementById('delete-course-id').value);

    if (!courseId) {
        alert('Please enter a valid course ID');
        return;
    }

    fetch(`${COURSE_API_URL}/api/courses/${courseId}`, {
    method: 'DELETE',
    headers: {
        'Authorization': `Bearer ${authToken}`
    }
    })
    .then(response => {
        if(response.ok){
            alert('Course deleted successfully!');
            loadCourses();
        } else {
            return response.text().then(errorMessage => {
                alert(`Failed to delete course: ${errorMessage}`);
            }); 
        }
    })

    .catch(err => alert(err.message));
}


function loadYourCourses() {
    fetch(`${COURSE_API_URL}/api/courses/enrolled`,{
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${authToken}`,
            'Content-Type': 'application/json'
        }
    })
    .then(res => res.json())
    .then(data => {
        const container = document.getElementById('main-content');
        container.innerHTML = '<h2>Your Courses</h2>';
        console.log(data);
        data.forEach(course => {
            const div = document.createElement('div');
            div.className = 'card';
            div.innerHTML = `
                <h3>${course.courseName}</h3>
                <button onclick="withdrawCourse(${course.courseId})">Withdraw</button>
                <button onclick="viewTeams(${course.courseId}, '${course.courseName}')">View Teams</button>
                <button onclick="loadCreateTeamForm(${course.courseId}, '${course.courseName}')">Create Team</button>
            `;
            container.appendChild(div);
        });
    });
}

function registerCourse(courseId) {
    fetch(`${COURSE_API_URL}/api/courses/${courseId}/register`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${authToken}`,
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            alert('Successfully registered for course!');
            loadYourCourses();
        } else {
            return response.text().then(errorMessage => {
                alert(`Failed to register for course: ${errorMessage}`);
            });
        }
    });
}


function withdrawCourse(courseId) {
    fetch(`${COURSE_API_URL}/api/courses/${courseId}/withdraw`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${authToken}`,
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            alert('Successfully withdrawn for course!');
            loadYourCourses();
        } else {
            return response.text().then(errorMessage => {
                alert(`Failed to withdraw for course: ${errorMessage}`);
            });
        }
    });
}

function viewTeams(courseId, courseName) {
    fetch(`${COURSE_API_URL}/api/courses/${courseId}/teams`)
    .then(res => res.json())
    .then(data => {
        const container = document.getElementById('main-content');
        container.innerHTML = `<h2>Teams in Course ${courseName}</h2>`;
        data.forEach(team => {
        const div = document.createElement('div');
        div.className = 'card';
        div.innerHTML = `
            <h3>${team.teamName}</h3>
            <button onclick="sendJoinRequest(${team.teamId})">Request to Join</button>
            <button onclick="viewMembers(${team.teamId})">View Members</button>
        `;
        container.appendChild(div);
        });
    });
}

function loadCreateTeamForm(courseId, courseName) {
    const container = document.getElementById('main-content');
    container.innerHTML = `
        <h2>Create a Team for "${courseName}"</h2>
        <p><strong>Course ID:</strong> ${courseId}</p>
        <label for="team-name">Team Name:</label>
        <input type="text" id="team-name" placeholder="Enter team name" />
        <br><br>
        <button onclick="createTeam(${courseId})">Create and Join Team</button>
    `;
}

function createTeam(courseId) {
    const teamName = document.getElementById('team-name').value;

    if (!teamName) {
        alert('Please enter a team name.');
        return;
    }

    fetch(`${COURSE_API_URL}/api/teams`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${authToken}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ courseId, teamName })
    })
    .then(response => {
        if(response.ok){
            alert("Created and joined new team");
            loadYourTeams();
        }
        else {
            return response.text().then(errorMessage => {
                alert(`Failed to create team: ${errorMessage}`);
            }); 
        }
        return res.json();
    })
}


function loadYourTeams() {
    fetch(`${COURSE_API_URL}/api/teams/my`,{
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${authToken}`,
            'Content-Type': 'application/json'
        }
    })
    .then(res => res.json())
    .then(data => {
        const container = document.getElementById('main-content');
        container.innerHTML = '<h2>Your Teams</h2>';
        data.forEach(team => {
        const div = document.createElement('div');
        div.className = 'card';
        div.innerHTML = `
            <h3>${team.teamName} (Course : ${team.courseName})</h3>
            <button onclick="leaveTeam(${team.teamId})">Leave</button>
            <button onclick="loadCreateTaskForm(${team.teamId})">Create Tasks</button>
            <button onclick="viewTasks(${team.teamId})">View Tasks</button>
            <button onclick="viewMembers(${team.teamId})">View Members</button>
            <button onclick="viewNotifications(${team.teamId}, '${team.teamName}')">View Notifications</button>
        `;
        container.appendChild(div);
        });
    });
}

function addToTeam(userId,teamId) {
    fetch(`${COURSE_API_URL}/api/teams/${teamId}/add?studentId=${userId}`, { method: 'POST' })
    .then(response => {
        if (response.ok) {
            alert('Successfully Joined team!');
            loadYourCourses();
        } else {
            return response.text().then(errorMessage => {
                alert(`Failed to Join team: ${errorMessage}`);
            });
        }
    });
}

function leaveTeam(teamId) {
    fetch(`${COURSE_API_URL}/api/teams/${teamId}/remove`, { 
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${authToken}`,
            'Content-Type': 'application/json'
        } 
    })
    .then(response => {
        if (response.ok) {
            alert('Successfully left team!');
            loadYourCourses();
        } else {
            return response.text().then(errorMessage => {
                alert(`Failed to leave team: ${errorMessage}`);
            });
        }
    });
}

function viewMembers(teamId) {
    fetch(`${COURSE_API_URL}/api/teams/${teamId}/students`)
    .then(res => res.json())
    .then(data => {
        const container = document.getElementById('main-content');
        container.innerHTML = `<h2>Members in Team ${teamId}</h2>`;
        data.forEach(member => {
        const div = document.createElement('div');
        div.className = 'card';
        div.innerHTML = `
            <p>Student ID: ${member.studentId}</p>
        `;
        container.appendChild(div);
        });
    });
}
