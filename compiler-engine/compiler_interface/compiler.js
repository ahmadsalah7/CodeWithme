document.addEventListener('DOMContentLoaded', initializeApp);
const COMPILER_SERVER_BASE_URL = "http://54.147.137.89:9090";
const LOGIN_SERVER_BASE_URL = "http://54.147.137.89:8081";
let currentProjectId = null;
let currentProjectVersions = [];


function initializeApp() {
    let token = getTokenFromStorageOrUrl();
    if (!token) {
        redirectToLogin();
        return;
    }
    fetchUserData(token);
    updateLineNumbers();
    addEventListeners();
    fetchProjects(token);
}

function getTokenFromStorageOrUrl() {
    let token = localStorage.getItem('token');
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('token')) {
        token = urlParams.get('token');
        localStorage.setItem('token', token);
    }
    return token;
}

function redirectToLogin() {
    console.error("No token found, redirecting to login.");
    window.location.href = `${LOGIN_SERVER_BASE_URL}`;
}

document.addEventListener('DOMContentLoaded', function () {
    const modal = document.getElementById('collaboratorModal');
    const addCollabBtn = document.getElementById('addcollaborators');
    const closeModalBtn = document.querySelector('#collaboratorModal .closeBtn');
    const addCollaboratorBtn = document.getElementById('addCollabBtn');
    const deleteModal = document.getElementById('deleteCollaboratorModal');
    const deleteCollabBtn = document.getElementById('deletecollaborators');
    const closeDeleteModalBtn = document.querySelector('#deleteCollaboratorModal .closeDeleteBtn');
    const deleteCollaboratorBtn = document.getElementById('deleteCollabBtn');

    if (!addCollabBtn || !closeModalBtn || !addCollaboratorBtn || !modal) {
        console.error("Some elements are missing in the DOM.");
        return;
    }

    if (!deleteCollabBtn || !closeDeleteModalBtn || !deleteCollaboratorBtn || !deleteModal) {
        console.error("Some elements for deletion are missing in the DOM.");
        return;
    }

    addCollabBtn.addEventListener('click', function () {
        modal.classList.add('show');
    });

    closeModalBtn.addEventListener('click', function () {
        modal.classList.remove('show');
    });

    window.addEventListener('click', function (e) {
        if (e.target == modal) {
            modal.classList.remove('show');
        }
    });

    addCollaboratorBtn.addEventListener('click', addCollaborator);

    deleteCollabBtn.addEventListener('click', function () {
        deleteModal.classList.add('show');
    });

    closeDeleteModalBtn.addEventListener('click', function () {
        deleteModal.classList.remove('show');
    });

    window.addEventListener('click', function (e) {
        if (e.target == deleteModal) {
            deleteModal.classList.remove('show');
        }
    });

    deleteCollaboratorBtn.addEventListener('click', deleteCollaborator);
});

function addCollaborator() {
    const collaboratorEmail = document.getElementById('collab-email').value.trim();
    const role = document.getElementById('collab-role').value;
    const token = getTokenFromStorageOrUrl();
    const ownerEmail = getEmailFromToken(token);

    if (!collaboratorEmail || !role || !currentProjectId) {
        alert('Please fill all fields.');
        return;
    }

    makeAuthFetch(`${COMPILER_SERVER_BASE_URL}/projects/addcollaborator`, 'POST', token, {
        ownerEmail: ownerEmail,
        collaboratorEmail: collaboratorEmail,
        projectId: currentProjectId,
        role: role.toUpperCase()
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Failed to add collaborator.");
        }
        return response.text();
    })
    .then(data => {
        console.log("Collaborator added successfully:", data);
        alert(data);
        document.getElementById('collaboratorModal').classList.remove('show');
    })
    .catch(error => {
        console.error("Error adding collaborator:", error);
        alert('Failed to add collaborator. Please try again.');
    });
}

function deleteCollaborator() {
    const collaboratorEmail = document.getElementById('delete-collab-email').value.trim();
    const token = getTokenFromStorageOrUrl();
    const ownerEmail = getEmailFromToken(token);

    if (!collaboratorEmail || !currentProjectId) {
        alert('Please fill the email field.');
        return;
    }

    makeAuthFetch(`${COMPILER_SERVER_BASE_URL}/projects/deletecollaborator`, 'POST', token, {
        ownerEmail: ownerEmail,
        collaboratorEmail: collaboratorEmail,
        projectId: currentProjectId
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Failed to delete collaborator.");
        }
        return response.text();
    })
    .then(data => {
        console.log("Collaborator deleted successfully:", data);
        alert(data);
        document.getElementById('deleteCollaboratorModal').classList.remove('show');
    })
    .catch(error => {
        console.error("Error deleting collaborator:", error);
        alert('Failed to delete collaborator. Please try again.');
    });
}

function fetchUserData(token) {
    console.log("Token from localStorage: ", token);
    console.log("Extracted email from token: ", getEmailFromToken(token));

    makeAuthFetch(`${COMPILER_SERVER_BASE_URL}/getuserdata`, 'POST', token, {
        email: getEmailFromToken(token)
    })
    .then(response => {
        if (!response.ok) throw new Error("Failed to fetch user data.");
        return response.json();
    })
    .then(data => {
        document.querySelector('.username').textContent = `${data.username}`;
        console.log("Username: ", data.username);
        console.log("Email: ", data.email);
    })
    .catch(error => {
        console.error("Error fetching user data: ", error);
    });
}

function getEmailFromToken(token) {
    if (!token) return null;
    const payload = JSON.parse(atob(token.split('.')[1]));
    console.log("JWT Payload: ", payload);
    return payload.sub;
}

function addEventListeners() {
    const codeArea = document.getElementById('code-area');
    const newProjectBtn = document.getElementById("newProjectBtn");
    const closeBtn = document.querySelector(".closeBtn");
    const createProjectBtn = document.getElementById("createProjectBtn");
    const saveProjectBtn = document.getElementById('saveProjectBtn');
    const addCommentBtn = document.getElementById('addCommentBtn');
    const pickVersionBtn = document.getElementById('versions'); // Add the "Pick Version" button
    const closeVersionsBtn = document.querySelector('.closeVersionsBtn');
    const loadVersionBtn = document.getElementById('loadVersionBtn');
    const forkBtn = document.getElementById('forkBtn');
    const closeForkBtn = document.querySelector('.closeForkBtn');
    const confirmForkBtn = document.getElementById('confirmForkBtn');

    codeArea.addEventListener('input', updateLineNumbers);
    codeArea.addEventListener('scroll', syncScroll);
    newProjectBtn.addEventListener('click', showNewProjectModal);
    closeBtn.addEventListener('click', closeNewProjectModal);
    window.addEventListener('click', hideNewProjectModal);
    createProjectBtn.addEventListener('click', createNewProject);
    saveProjectBtn.addEventListener('click', saveProject);
    addCommentBtn.addEventListener('click', addComment);
    pickVersionBtn.addEventListener('click', openVersionsModal); // Listener for opening versions modal
    closeVersionsBtn.addEventListener('click', closeVersionsModal); // Close modal button
    loadVersionBtn.addEventListener('click', loadSelectedVersion);
    forkBtn.addEventListener('click', openForkModal);          // Fork button to open modal
    closeForkBtn.addEventListener('click', closeForkModal);    // Close fork modal button
    confirmForkBtn.addEventListener('click', forkProject);
    document.querySelector('.runbtn').addEventListener('click', runCode);
    document.getElementById('deleteProjectBtn').addEventListener('click', deleteProject);
}

function showNewProjectModal() {
    document.getElementById("newProjectModal").style.display = "block";
}

function closeNewProjectModal() {
    document.getElementById("newProjectModal").style.display = "none";
}
function openForkModal() {
    document.getElementById('forkModal').style.display = 'block';
}
function closeForkModal() {
    document.getElementById('forkModal').style.display = 'none';
}

function hideNewProjectModal(event) {
    const newProjectModal = document.getElementById("newProjectModal");
    if (event.target === newProjectModal) {
        newProjectModal.style.display = "none";
    }
}

function createNewProject() {
    const projectNameInput = document.getElementById("projectName");
    const projectName = projectNameInput.value.trim();
    let token = getTokenFromStorageOrUrl();

    if (projectName) {
        const userEmail = getEmailFromToken(token);

        if (!userEmail) {
            alert("Failed to get user email from token. Please login again.");
            redirectToLogin();
            return;
        }

        makeAuthFetch(`${COMPILER_SERVER_BASE_URL}/projects/create`, 'POST', token, {
            projectName: projectName,
            ownerEmail: userEmail,
            content: ""
        })
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to create the project.");
            }
            return response.json();
        })
        .then(data => {
            console.log("Project created successfully:", data);
            addProjectToList(data.id, projectName);
            closeNewProjectModal();
            projectNameInput.value = "";
        })
        .catch(error => {
            console.error("Error creating project:", error);
            alert("Failed to create project. Please try again.");
        });
    } else {
        alert('Please enter a project name.');
    }
}
function deleteProject() {
    if (!currentProjectId) {
        alert('Please select a project to delete.');
        return;
    }

    const token = getTokenFromStorageOrUrl();
    const ownerEmail = getEmailFromToken(token);

    if (!confirm("Are you sure you want to delete this project?")) {
        return;
    }

    const payload = {
        projectId: currentProjectId, 
        ownerEmail: ownerEmail
    };

    fetch(`${COMPILER_SERVER_BASE_URL}/projects/delete`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(payload)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to delete project.');
        }
        return response.text();
    })
    .then(result => {
        alert(result);
        document.querySelector(`div[data-project-id="${currentProjectId}"]`).remove();
        currentProjectId = null; 
    })
    .catch(error => {
        console.error('Error during project deletion:', error);
        alert('Failed to delete project. Please try again.');
    });
}
function forkProject() {
    const newProjectName = document.getElementById('forkProjectName').value.trim();
    if (!newProjectName) {
        alert("Please enter a name for the new project.");
        return;
    }

    const token = getTokenFromStorageOrUrl();
    const userEmail = getEmailFromToken(token);
    const latestVersion = currentProjectVersions[currentProjectVersions.length - 1];

    if (!latestVersion) {
        alert("No project content found to copy.");
        return;
    }

    console.log("Sending request to copy project...");
    makeAuthFetch(`${COMPILER_SERVER_BASE_URL}/projects/create`, 'POST', token, {
        projectName: newProjectName,
        ownerEmail: userEmail,
        content: latestVersion.content
    })
    .then(response => {
        if (!response.ok) throw new Error("Failed to copy project.");
        return response.json();
    })
    .then(data => {
        console.log("Project copied successfully:", data);
        alert("Project copied successfully!");
        closeForkModal();
        document.getElementById('forkProjectName').value = '';
        addProjectToList(data.id, newProjectName); 
    })
    .catch(error => {
        console.error("Error copying project:", error);
        alert("Failed to copy project. Please try again.");
    });
}

function fetchProjects(token) {
    const userEmail = getEmailFromToken(token);

    makeAuthFetch(`${COMPILER_SERVER_BASE_URL}/projects/getprojects/${userEmail}`, 'GET', token)
    .then(response => {
        if (!response.ok) throw new Error("Failed to fetch projects.");
        return response.json();
    })
    .then(data => {
        data.forEach(project => {
            addProjectToList(project.id, project.projectName);
        });
    })
    .catch(error => {
        console.error("Error fetching projects:", error);
    });
}

function addProjectToList(projectId, projectName) {
    const projectList = document.getElementById('projects');
    const newProjectDiv = document.createElement('div');
    newProjectDiv.classList.add('project');
    newProjectDiv.dataset.projectId = projectId;

    const projectHeader = document.createElement('div');
    projectHeader.classList.add('project-header');
    projectHeader.textContent = projectName;
    projectHeader.onclick = () => openProject(newProjectDiv);

    newProjectDiv.appendChild(projectHeader);
    projectList.appendChild(newProjectDiv);
}

function openProject(projectElement) {
    const projectId = projectElement.dataset.projectId;
    currentProjectId = projectId;

    if (!projectId) {
        console.error("Project ID is undefined");
        return;
    }

    console.log(`Fetching project with ID: ${projectId}`);

    const allProjects = document.querySelectorAll('.project');
    allProjects.forEach(proj => proj.classList.remove('active'));

    projectElement.classList.add('active');

    let token = getTokenFromStorageOrUrl();
    let email = getEmailFromToken(token);

    makeAuthFetch(`${COMPILER_SERVER_BASE_URL}/projects/${projectId}`, 'GET', token)
    .then(response => {
        if (!response.ok) throw new Error("Failed to fetch project details.");
        return response.json();
    })
    .then(data => {
        const codeArea = document.getElementById('code-area');

        const latestVersion = data.versions[data.versions.length - 1];
        if (latestVersion) {
            codeArea.value = latestVersion.content || "";
        } else {
            codeArea.value = ""; 
        }

        updateLineNumbers();
        fetchCommentsForProject(projectId);
        currentProjectVersions = data.versions;
        return isViewer(token, projectId, email);
    })
    .then(isViewerResult => {
        if (isViewerResult) {
            disableEditorForViewer(); 
        } else {
            enableEditorForEditing(); 
        }
    })
    .catch(error => {
        console.error("Error fetching project details or checking viewer status:", error);
    });
}

function isViewer(projectId, email) {
    const token = getTokenFromStorageOrUrl(); 

    return fetch(`${COMPILER_SERVER_BASE_URL}/projects/isViewer/${projectId}/${email}`, {
        method: 'GET',
        headers: {
            "Authorization": `Bearer ${token}`,  
            "Content-Type": "application/json"
        }
    })
    .then(response => {
        if (response.status === 200) {
            console.log("User is a viewer.");
            return true; 
        } else if (response.status === 403) {
            return false;
        } else {
            throw new Error("Unexpected response from server");
        }
    })
    .catch(error => {
        console.error("Error checking if user is viewer:", error);
        return false; 
    });
}

function disableEditorForViewer() {
    const codeArea = document.getElementById('code-area');
    const runButton = document.querySelector('.runbtn');
    const saveButton = document.getElementById('saveProjectBtn');


    codeArea.readOnly = true;

    if (runButton) runButton.disabled = true;
    if (saveButton) saveButton.disabled = true;

    codeArea.style.backgroundColor = '#f0f0f0';
    codeArea.style.cursor = 'not-allowed';
}
function enableEditorForEditing() {
    const codeArea = document.getElementById('code-area');
    const runButton = document.querySelector('.runbtn');
    const saveButton = document.getElementById('saveProjectBtn');

    codeArea.readOnly = false;

    if (runButton) runButton.disabled = false;
    if (saveButton) saveButton.disabled = false;

    codeArea.style.backgroundColor = '';
    codeArea.style.cursor = '';
}

function openVersionsModal() {
    const versionDropdown = document.getElementById('versionDropdown');
    versionDropdown.innerHTML = '<option value="">Select a version</option>';


    currentProjectVersions.forEach(version => {
        const option = document.createElement('option');
        option.value = version.versionNumber;
        option.textContent = `Version ${version.versionNumber}`;
        versionDropdown.appendChild(option);
    });

    document.getElementById('versionsModal').style.display = 'block'; 
}


function closeVersionsModal() {
    document.getElementById('versionsModal').style.display = 'none';
}


function loadSelectedVersion() {
    const versionDropdown = document.getElementById('versionDropdown');
    const selectedVersionNumber = parseFloat(versionDropdown.value); 

    if (!selectedVersionNumber) {
        alert("Please select a version to load.");
        return;
    }

    const selectedVersion = currentProjectVersions.find(version => version.versionNumber === selectedVersionNumber);
    if (selectedVersion) {
        const codeArea = document.getElementById('code-area');
        codeArea.value = selectedVersion.content || "";
        updateLineNumbers();
    }

    closeVersionsModal();
}


function fetchCommentsForProject(projectId) {
    const token = getTokenFromStorageOrUrl();

    fetch(`${COMPILER_SERVER_BASE_URL}/projects/getcomments/${projectId}`, {
        method: 'GET',
        headers: {
            "Authorization": `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) throw new Error("Failed to fetch comments.");
        return response.json();
    })
    .then(comments => {
        const commentsList = document.getElementById('comments-list');
        commentsList.innerHTML = '';

        comments.forEach(comment => {
            const commentDiv = document.createElement('div');
            const formattedDate = new Date(comment.timestamp).toLocaleString();
            commentDiv.classList.add('comment');
            commentDiv.innerHTML = `<strong>${comment.username}</strong> (${formattedDate}): ${comment.content}<hr>`;
            commentsList.appendChild(commentDiv);
        });
    })
    .catch(error => {
        console.error("Error fetching comments:", error);
    });
}


function runCode() {
    // Get the code, input, and language from the page
    const code = document.getElementById('code-area').value;
    const input = document.getElementById('input-box').value;
    const language = document.getElementById('language-select').value.toLowerCase(); // Get selected language

    if (!code) {
        alert("Please enter some code to run.");
        return;
    }

    // Create the payload
    const payload = {
        language: language,
        code: code,
        input: input
    };


    fetch(`${COMPILER_SERVER_BASE_URL}/code/execute`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload) 
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`Failed to execute code. Status: ${response.status}`);
        }
        return response.text();
    })
    .then(result => {
        
        document.getElementById('output-box').value = result;
    })
    .catch(error => {
        console.error('Error during code execution:', error);
        alert('Error during code execution. Please check the console for details.');
    });
}
function saveProject() {
    const codeArea = document.getElementById('code-area');
    const projectCode = codeArea.value;
    const token = getTokenFromStorageOrUrl();
    const userEmail = getEmailFromToken(token);

    if (currentProjectId && projectCode) {
        makeAuthFetch(`${COMPILER_SERVER_BASE_URL}/projects/saveproject`, 'POST', getTokenFromStorageOrUrl(), {
            projectId: currentProjectId ,
            content: projectCode,
            email: userEmail
        })
        .then(response => {
            if (!response.ok) throw new Error("Failed to save project content.");
            alert('Project code saved successfully.');
        })
        .catch(error => {
            console.error("Error saving project content:", error);
            alert("Failed to save project content. Please try again.");
        });
    } else {
        alert('Please open a project to save.');
    }
}

function addComment() {
    const commentArea = document.getElementById('comment-area');
    const commentText = commentArea.value.trim();
    const username = document.querySelector('.username').textContent;
    const token = getTokenFromStorageOrUrl();

    if (commentText && currentProjectId) {
        const date = new Date().toISOString();

        makeAuthFetch(`${COMPILER_SERVER_BASE_URL}/projects/addcomment/${currentProjectId}`, 'POST', token, {
            content: commentText,
            username: username,
            date: date
        })
        .then(response => {
            if (!response.ok) {
                console.error("Error Response:", response);
                throw new Error("Failed to save comment. Status code: " + response.status);
            }
            return response.json();
        })
        .then(data => {
            console.log("Comment saved successfully:", data);

            const commentsList = document.getElementById('comments-list');
            const commentDiv = document.createElement('div');
            commentDiv.classList.add('comment');
            commentDiv.innerHTML = `<strong>${username}</strong> (${date}): ${commentText}<hr>`;
            commentsList.appendChild(commentDiv);
            commentArea.value = '';
        })
        .catch(error => {
            console.error("Error saving comment:", error);
            alert("Failed to save comment. Please try again.");
        });
    } else {
        alert('Please enter a comment or open a project first.');
    }
}

function makeAuthFetch(url, method, token, body = null) {
    const headers = {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
    };

    const options = { method, headers };
    if (body) {
        options.body = JSON.stringify(body);
    }

    return fetch(url, options);
}

function updateLineNumbers() {
    const codeArea = document.getElementById('code-area');
    const lineNumbers = document.getElementById('line-numbers');
    const lines = codeArea.value.split('\n').length;
    lineNumbers.innerHTML = Array.from({ length: lines }, (_, i) => i + 1).join('<br>');
}

function syncScroll() {
    const codeArea = document.getElementById('code-area');
    const lineNumbers = document.getElementById('line-numbers');
    lineNumbers.scrollTop = codeArea.scrollTop;
}
