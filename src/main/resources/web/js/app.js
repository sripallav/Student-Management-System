/**
 * Student Management System - Frontend Controller (Vanilla JavaScript)
 */

document.addEventListener('DOMContentLoaded', () => {
    // API Endpoints
    const API_BASE = '/api';
    const API_STUDENTS = `${API_BASE}/students`;
    const API_STATS = `${API_BASE}/stats`;
    const API_HEALTH = `${API_BASE}/health`;

    // State
    let currentStudents = [];
    let studentToDelete = null;
    let searchTimeout = null;

    // DOM Elements
    const studentsTableBody = document.getElementById('studentsTableBody');
    const recordsCount = document.getElementById('recordsCount');
    const searchInput = document.getElementById('searchInput');
    const clearSearchBtn = document.getElementById('clearSearchBtn');
    const courseFilter = document.getElementById('courseFilter');
    const statusFilter = document.getElementById('statusFilter');
    const refreshBtn = document.getElementById('refreshBtn');
    const exportCsvBtn = document.getElementById('exportCsvBtn');

    // Stats Elements
    const statTotalStudents = document.getElementById('statTotalStudents');
    const statActiveStudents = document.getElementById('statActiveStudents');
    const statInactiveStudents = document.getElementById('statInactiveStudents');
    const statAvgGpa = document.getElementById('statAvgGpa');
    const systemStatus = document.getElementById('systemStatus');

    // Form Modal Elements
    const studentModal = document.getElementById('studentModal');
    const studentForm = document.getElementById('studentForm');
    const modalTitle = document.getElementById('modalTitle');
    const studentIdInput = document.getElementById('studentId');
    const rollNumberInput = document.getElementById('rollNumber');
    const firstNameInput = document.getElementById('firstName');
    const lastNameInput = document.getElementById('lastName');
    const emailInput = document.getElementById('email');
    const phoneInput = document.getElementById('phone');
    const genderInput = document.getElementById('gender');
    const courseInput = document.getElementById('course');
    const yearLevelInput = document.getElementById('yearLevel');
    const gpaInput = document.getElementById('gpa');
    const cityInput = document.getElementById('city');
    const statusInput = document.getElementById('status');
    const openAddModalBtn = document.getElementById('openAddModalBtn');
    const closeModalBtn = document.getElementById('closeModalBtn');
    const cancelModalBtn = document.getElementById('cancelModalBtn');

    // View Modal Elements
    const viewModal = document.getElementById('viewModal');
    const viewModalBody = document.getElementById('viewModalBody');
    const closeViewModalBtn = document.getElementById('closeViewModalBtn');
    const closeViewModalFooterBtn = document.getElementById('closeViewModalFooterBtn');
    const editFromViewBtn = document.getElementById('editFromViewBtn');
    let currentViewingStudentId = null;

    // Delete Modal Elements
    const deleteModal = document.getElementById('deleteModal');
    const deleteStudentPreview = document.getElementById('deleteStudentPreview');
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    const cancelDeleteBtn = document.getElementById('cancelDeleteBtn');
    const closeDeleteModalBtn = document.getElementById('closeDeleteModalBtn');

    // Theme Toggle
    const themeToggleBtn = document.getElementById('themeToggleBtn');
    initTheme();

    // Init App
    checkHealth();
    fetchStats();
    fetchStudents();

    // -------------------------------------------------------------
    // Data Fetching
    // -------------------------------------------------------------

    async function fetchStudents() {
        showLoadingState();
        const q = searchInput.value.trim();
        const course = courseFilter.value;
        const status = statusFilter.value;

        const params = new URLSearchParams();
        if (q) params.append('q', q);
        if (course && course !== 'all') params.append('course', course);
        if (status && status !== 'all') params.append('status', status);

        const url = `${API_STUDENTS}?${params.toString()}`;

        try {
            const res = await fetch(url);
            const result = await res.json();

            if (result.success && Array.isArray(result.data)) {
                currentStudents = result.data;
                renderTable(currentStudents);
            } else {
                showErrorState(result.error || 'Failed to load students');
            }
        } catch (err) {
            console.error('Fetch error:', err);
            showErrorState('Cannot connect to backend server. Ensure Java server is running.');
        }
    }

    async function fetchStats() {
        try {
            const res = await fetch(API_STATS);
            const result = await res.json();
            if (result.success && result.data) {
                const data = result.data;
                animateNumber(statTotalStudents, data.totalStudents || 0);
                animateNumber(statActiveStudents, data.activeStudents || 0);
                animateNumber(statInactiveStudents, data.inactiveStudents || 0);
                statAvgGpa.textContent = (data.averageGpa !== undefined) ? Number(data.averageGpa).toFixed(2) : '0.00';
            }
        } catch (err) {
            console.warn('Stats fetch error:', err);
        }
    }

    async function checkHealth() {
        try {
            const res = await fetch(API_HEALTH);
            const data = await res.json();
            if (data.database === 'CONNECTED') {
                systemStatus.innerHTML = '<span class="status-dot"></span><span class="status-text">MySQL Connected</span>';
                systemStatus.className = 'status-indicator';
            } else {
                systemStatus.innerHTML = '<span class="status-dot" style="background:#ef4444"></span><span class="status-text" style="color:#ef4444">DB Disconnected</span>';
            }
        } catch (e) {
            systemStatus.innerHTML = '<span class="status-dot" style="background:#ef4444"></span><span class="status-text" style="color:#ef4444">Server Offline</span>';
        }
    }

    // -------------------------------------------------------------
    // Table Rendering
    // -------------------------------------------------------------

    function renderTable(students) {
        recordsCount.textContent = `Showing ${students.length} student${students.length === 1 ? '' : 's'}`;

        if (students.length === 0) {
            studentsTableBody.innerHTML = `
                <tr>
                    <td colspan="9" class="empty-state">
                        <i class="fa-regular fa-folder-open"></i>
                        <p>No student records found matching your filter criteria.</p>
                    </td>
                </tr>
            `;
            return;
        }

        studentsTableBody.innerHTML = students.map(s => {
            const initials = getInitials(s.firstName, s.lastName);
            const gpaClass = s.gpa >= 3.7 ? 'gpa-high' : (s.gpa >= 3.0 ? 'gpa-mid' : 'gpa-low');
            const statusClass = s.status && s.status.toLowerCase() === 'active' ? 'active' : 'inactive';

            return `
                <tr data-id="${s.id}">
                    <td><span class="badge-roll">${escapeHtml(s.rollNumber)}</span></td>
                    <td>
                        <div class="student-name-cell">
                            <div class="avatar">${initials}</div>
                            <div>
                                <div class="student-meta-name">${escapeHtml(s.fullName)}</div>
                                <div class="student-meta-sub">${escapeHtml(s.gender || 'Not specified')}</div>
                            </div>
                        </div>
                    </td>
                    <td>
                        <div>${escapeHtml(s.email)}</div>
                        <div class="student-meta-sub">${escapeHtml(s.phone)}</div>
                    </td>
                    <td><strong>${escapeHtml(s.course)}</strong></td>
                    <td>Year ${s.yearLevel}</td>
                    <td><span class="gpa-tag ${gpaClass}">${Number(s.gpa).toFixed(2)}</span></td>
                    <td>${escapeHtml(s.city)}</td>
                    <td>
                        <span class="badge-status ${statusClass}">
                            <i class="fa-solid fa-circle" style="font-size: 7px;"></i> ${escapeHtml(s.status)}
                        </span>
                    </td>
                    <td class="text-right">
                        <div class="action-btns">
                            <button class="action-btn view" onclick="window.viewStudent(${s.id})" title="View Details">
                                <i class="fa-solid fa-eye"></i>
                            </button>
                            <button class="action-btn edit" onclick="window.editStudent(${s.id})" title="Edit Student">
                                <i class="fa-solid fa-pen-to-square"></i>
                            </button>
                            <button class="action-btn delete" onclick="window.promptDeleteStudent(${s.id}, '${escapeHtml(s.fullName)}', '${escapeHtml(s.rollNumber)}')" title="Delete Student">
                                <i class="fa-solid fa-trash-can"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    }

    function showLoadingState() {
        studentsTableBody.innerHTML = `
            <tr>
                <td colspan="9" class="loading-state">
                    <div class="spinner"></div>
                    <p>Fetching records from MySQL database...</p>
                </td>
            </tr>
        `;
    }

    function showErrorState(msg) {
        studentsTableBody.innerHTML = `
            <tr>
                <td colspan="9" class="empty-state">
                    <i class="fa-solid fa-triangle-exclamation" style="color: var(--rose);"></i>
                    <p style="color: var(--rose); font-weight: 600;">${escapeHtml(msg)}</p>
                    <button class="btn btn-outline mt-2" onclick="location.reload()">Retry</button>
                </td>
            </tr>
        `;
    }

    // -------------------------------------------------------------
    // Form Submission (Add / Update)
    // -------------------------------------------------------------

    studentForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const id = studentIdInput.value.trim();
        const isEdit = id.length > 0;

        const studentData = {
            rollNumber: rollNumberInput.value.trim(),
            firstName: firstNameInput.value.trim(),
            lastName: lastNameInput.value.trim(),
            email: emailInput.value.trim(),
            phone: phoneInput.value.trim(),
            gender: genderInput.value,
            course: courseInput.value,
            yearLevel: parseInt(yearLevelInput.value, 10),
            gpa: parseFloat(gpaInput.value),
            city: cityInput.value.trim(),
            status: statusInput.value
        };

        const submitBtn = document.getElementById('saveStudentBtn');
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Saving...';

        try {
            const url = isEdit ? `${API_STUDENTS}/${id}` : API_STUDENTS;
            const method = isEdit ? 'PUT' : 'POST';

            const res = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(studentData)
            });

            const result = await res.json();

            if (result.success) {
                showToast(isEdit ? 'Student updated successfully!' : 'New student registered successfully!', 'success');
                closeStudentModal();
                fetchStudents();
                fetchStats();
            } else {
                showToast(result.error || 'Failed to save student', 'error');
            }
        } catch (err) {
            console.error('Save error:', err);
            showToast('Network error while saving student', 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fa-solid fa-check"></i> Save Student';
        }
    });

    // -------------------------------------------------------------
    // Modals Management
    // -------------------------------------------------------------

    function openAddModal() {
        studentForm.reset();
        studentIdInput.value = '';
        modalTitle.innerHTML = '<i class="fa-solid fa-user-plus"></i> Add New Student';
        // Suggest roll number
        const nextNum = currentStudents.length + 1;
        rollNumberInput.value = `SMS2026-${String(nextNum).padStart(3, '0')}`;
        gpaInput.value = '3.50';
        studentModal.classList.add('open');
    }

    function closeStudentModal() {
        studentModal.classList.remove('open');
    }

    openAddModalBtn.addEventListener('click', openAddModal);
    closeModalBtn.addEventListener('click', closeStudentModal);
    cancelModalBtn.addEventListener('click', closeStudentModal);

    // Global Window Methods for row action clicks
    window.viewStudent = async (id) => {
        try {
            const res = await fetch(`${API_STUDENTS}/${id}`);
            const result = await res.json();
            if (result.success && result.data) {
                const s = result.data;
                currentViewingStudentId = s.id;
                const initials = getInitials(s.firstName, s.lastName);

                viewModalBody.innerHTML = `
                    <div class="profile-card">
                        <div class="profile-header">
                            <div class="profile-avatar">${initials}</div>
                            <div>
                                <h2 style="font-size: 20px; font-weight: 700;">${escapeHtml(s.fullName)}</h2>
                                <p style="color: var(--text-muted); font-size: 13px;">
                                    <span class="badge-roll">${escapeHtml(s.rollNumber)}</span> &bull; 
                                    <span>${escapeHtml(s.status)}</span>
                                </p>
                            </div>
                        </div>
                        <div class="profile-details-grid">
                            <div>
                                <div class="profile-field-label">Email Address</div>
                                <div class="profile-field-value">${escapeHtml(s.email)}</div>
                            </div>
                            <div>
                                <div class="profile-field-label">Phone Number</div>
                                <div class="profile-field-value">${escapeHtml(s.phone)}</div>
                            </div>
                            <div>
                                <div class="profile-field-label">Course / Department</div>
                                <div class="profile-field-value">${escapeHtml(s.course)}</div>
                            </div>
                            <div>
                                <div class="profile-field-label">Academic Year</div>
                                <div class="profile-field-value">Year ${s.yearLevel}</div>
                            </div>
                            <div>
                                <div class="profile-field-label">Current GPA</div>
                                <div class="profile-field-value">${Number(s.gpa).toFixed(2)} / 4.00</div>
                            </div>
                            <div>
                                <div class="profile-field-label">Gender</div>
                                <div class="profile-field-value">${escapeHtml(s.gender)}</div>
                            </div>
                            <div>
                                <div class="profile-field-label">City</div>
                                <div class="profile-field-value">${escapeHtml(s.city)}</div>
                            </div>
                            <div>
                                <div class="profile-field-label">Registration Date</div>
                                <div class="profile-field-value">${s.createdAt ? escapeHtml(s.createdAt.substring(0, 10)) : 'N/A'}</div>
                            </div>
                        </div>
                    </div>
                `;
                viewModal.classList.add('open');
            }
        } catch (e) {
            showToast('Failed to load student details', 'error');
        }
    };

    window.editStudent = async (id) => {
        try {
            const res = await fetch(`${API_STUDENTS}/${id}`);
            const result = await res.json();
            if (result.success && result.data) {
                const s = result.data;
                studentIdInput.value = s.id;
                rollNumberInput.value = s.rollNumber;
                firstNameInput.value = s.firstName;
                lastNameInput.value = s.lastName;
                emailInput.value = s.email;
                phoneInput.value = s.phone;
                genderInput.value = s.gender;
                courseInput.value = s.course;
                yearLevelInput.value = s.yearLevel;
                gpaInput.value = s.gpa;
                cityInput.value = s.city;
                statusInput.value = s.status;

                modalTitle.innerHTML = '<i class="fa-solid fa-user-pen"></i> Edit Student';
                studentModal.classList.add('open');
            }
        } catch (e) {
            showToast('Failed to load student for editing', 'error');
        }
    };

    window.promptDeleteStudent = (id, name, roll) => {
        studentToDelete = id;
        deleteStudentPreview.innerHTML = `
            <strong>${escapeHtml(name)}</strong> (${escapeHtml(roll)})
        `;
        deleteModal.classList.add('open');
    };

    // Close view modal
    closeViewModalBtn.addEventListener('click', () => viewModal.classList.remove('open'));
    closeViewModalFooterBtn.addEventListener('click', () => viewModal.classList.remove('open'));
    editFromViewBtn.addEventListener('click', () => {
        viewModal.classList.remove('open');
        if (currentViewingStudentId) {
            window.editStudent(currentViewingStudentId);
        }
    });

    // Close delete modal
    closeDeleteModalBtn.addEventListener('click', () => deleteModal.classList.remove('open'));
    cancelDeleteBtn.addEventListener('click', () => deleteModal.classList.remove('open'));

    confirmDeleteBtn.addEventListener('click', async () => {
        if (!studentToDelete) return;
        confirmDeleteBtn.disabled = true;
        confirmDeleteBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Deleting...';

        try {
            const res = await fetch(`${API_STUDENTS}/${studentToDelete}`, {
                method: 'DELETE'
            });
            const result = await res.json();
            if (result.success) {
                showToast('Student deleted successfully', 'success');
                deleteModal.classList.remove('open');
                studentToDelete = null;
                fetchStudents();
                fetchStats();
            } else {
                showToast(result.error || 'Failed to delete student', 'error');
            }
        } catch (e) {
            showToast('Error deleting student', 'error');
        } finally {
            confirmDeleteBtn.disabled = false;
            confirmDeleteBtn.innerHTML = '<i class="fa-solid fa-trash-can"></i> Yes, Delete';
        }
    });

    // -------------------------------------------------------------
    // Filters, Search & CSV Export
    // -------------------------------------------------------------

    searchInput.addEventListener('input', () => {
        if (searchInput.value.length > 0) {
            clearSearchBtn.style.display = 'block';
        } else {
            clearSearchBtn.style.display = 'none';
        }
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(fetchStudents, 250);
    });

    clearSearchBtn.addEventListener('click', () => {
        searchInput.value = '';
        clearSearchBtn.style.display = 'none';
        fetchStudents();
    });

    courseFilter.addEventListener('change', fetchStudents);
    statusFilter.addEventListener('change', fetchStudents);
    refreshBtn.addEventListener('click', () => {
        fetchStudents();
        fetchStats();
        checkHealth();
        showToast('Data refreshed', 'info');
    });

    exportCsvBtn.addEventListener('click', () => {
        if (currentStudents.length === 0) {
            showToast('No student records to export', 'error');
            return;
        }

        const headers = ['ID', 'Roll Number', 'First Name', 'Last Name', 'Email', 'Phone', 'Gender', 'Course', 'Year', 'GPA', 'City', 'Status'];
        const csvRows = [headers.join(',')];

        currentStudents.forEach(s => {
            const row = [
                s.id,
                `"${s.rollNumber}"`,
                `"${s.firstName}"`,
                `"${s.lastName}"`,
                `"${s.email}"`,
                `"${s.phone}"`,
                `"${s.gender}"`,
                `"${s.course}"`,
                s.yearLevel,
                s.gpa,
                `"${s.city}"`,
                `"${s.status}"`
            ];
            csvRows.push(row.join(','));
        });

        const csvContent = 'data:text/csv;charset=utf-8,' + encodeURIComponent(csvRows.join('\n'));
        const link = document.createElement('a');
        link.setAttribute('href', csvContent);
        link.setAttribute('download', `students_export_${new Date().toISOString().slice(0, 10)}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        showToast(`Exported ${currentStudents.length} students to CSV!`, 'success');
    });

    // -------------------------------------------------------------
    // UI Helpers (Toast, Theme, Animations)
    // -------------------------------------------------------------

    function showToast(message, type = 'info') {
        const toastContainer = document.getElementById('toastContainer');
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;

        let icon = 'fa-circle-info';
        if (type === 'success') icon = 'fa-circle-check';
        if (type === 'error') icon = 'fa-circle-exclamation';

        toast.innerHTML = `
            <i class="fa-solid ${icon}"></i>
            <span>${escapeHtml(message)}</span>
        `;

        toastContainer.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(10px)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 3500);
    }

    function initTheme() {
        const savedTheme = localStorage.getItem('theme') || 'light';
        document.body.setAttribute('data-theme', savedTheme);
        updateThemeIcon(savedTheme);

        themeToggleBtn.addEventListener('click', () => {
            const current = document.body.getAttribute('data-theme');
            const next = current === 'dark' ? 'light' : 'dark';
            document.body.setAttribute('data-theme', next);
            localStorage.setItem('theme', next);
            updateThemeIcon(next);
        });
    }

    function updateThemeIcon(theme) {
        themeToggleBtn.innerHTML = theme === 'dark'
            ? '<i class="fa-solid fa-sun" style="color:#f59e0b;"></i>'
            : '<i class="fa-solid fa-moon"></i>';
    }

    function getInitials(first, last) {
        const f = first ? first.charAt(0).toUpperCase() : '';
        const l = last ? last.charAt(0).toUpperCase() : '';
        return (f + l) || 'S';
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function animateNumber(element, target) {
        const start = parseInt(element.textContent, 10) || 0;
        if (start === target) {
            element.textContent = target;
            return;
        }
        const duration = 500;
        const stepTime = 25;
        const steps = duration / stepTime;
        const increment = (target - start) / steps;
        let current = start;

        const timer = setInterval(() => {
            current += increment;
            if ((increment > 0 && current >= target) || (increment < 0 && current <= target)) {
                element.textContent = target;
                clearInterval(timer);
            } else {
                element.textContent = Math.round(current);
            }
        }, stepTime);
    }
});
