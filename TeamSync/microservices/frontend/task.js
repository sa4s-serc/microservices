const TASK_API_URL = "http://localhost:4324/api/tasks";

function viewTasks(teamId) {
    fetch(`${TASK_API_URL}/team/${teamId}`)
    .then(res => res.json())
    .then(tasks => {
        const container = document.getElementById('main-content');
        container.innerHTML = '<h2>Team Tasks</h2>';

        tasks.forEach(task => {
            const isAssigned = task.assignedToId !== null;

            container.innerHTML += `
                <div class="card">
                    <input type="checkbox" ${task.completed ? 'checked' : ''} 
                        onchange="toggleCompletion(${task.taskId}, ${task.completed})" />
                    <strong>${task.title}</strong> - ${task.description}<br/>
                    <span>${isAssigned ? `Assigned to: ${task.assignedToId}` : ''}</span>
                    <input type="number" id="assign-${task.taskId}" placeholder="Assign user ID" />
                    <button onclick="assignUser(${task.taskId}, ${teamId})">Assign</button>
                </div>
            `;
        });
    });
}

function loadYourTasks() {
    fetch(`${TASK_API_URL}/my`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${authToken}`,
            'Content-Type': 'application/json'
        }
    })
    .then(res => res.json())
    .then(tasks => {
        const container = document.getElementById('main-content');
        container.innerHTML = '<h2>Your Tasks</h2>';

        tasks.forEach(task => {
            container.innerHTML += `
                <div class="card">
                    <input type="checkbox" ${task.completed ? 'checked' : ''} 
                        onchange="toggleCompletion(${task.taskId}, ${task.completed})" />
                    <strong>${task.title}</strong> - ${task.description}
                </div>
            `;
        });
    });
}


function toggleCompletion(taskId, currentStatus) {
    fetch(`${TASK_API_URL}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ taskId: taskId, completed: !currentStatus })
    })
    .then(res => {
        if (res.ok){
            alert("Task status updated successfully");
        }
    });
}

function assignUser(taskId,teamId) {
    const input = document.getElementById(`assign-${taskId}`);
    const userId = parseInt(input.value);
    if (!userId) return alert("Enter a valid user ID");

    fetch(`${TASK_API_URL}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ taskId: taskId, assignedToId: userId })
    })
    .then(res => {
        if (res.ok){
            alert("User assigned successfully");
            viewTasks(teamId);
        }
    });
}

function loadCreateTaskForm(teamId) {
    const container = document.getElementById('main-content');
    container.innerHTML = `
        <h2>Create Task</h2>
        <form id="create-task-form">
            <label for="title">Title:</label><br>
            <input type="text" id="task-title" name="title" required><br><br>

            <label for="description">Description:</label><br>
            <textarea id="task-description" name="description" required></textarea><br><br>

            <label for="assignedTo">Assign To (Student ID):</label><br>
            <input type="number" id="assigned-to-id" name="assignedTo"><br><br>

            <button type="button" onclick="createTask(${teamId})">Create Task</button>
        </form>
    `;
}

async function createTask(teamId) {
    const title = document.getElementById('task-title').value.trim();
    const description = document.getElementById('task-description').value.trim();
    const assignedToId = parseInt(document.getElementById('assigned-to-id').value);

    if (!title || !description || isNaN(assignedToId)) {
        alert('Please fill all fields correctly.');
        return;
    }

    const task = {
        teamId,
        title,
        description,
        assignedToId,
        completed: false
    };

    try {
        const res = await fetch(`${TASK_API_URL}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(task)
        });

        if (!res.ok) {
            const errMsg = await res.text();
            throw new Error(errMsg);
        }

        alert("Task created successfully!");
        viewTasks(teamId);
        
    } catch (err) {
        alert(`Error creating task: ${err.message}`);
    }
}


