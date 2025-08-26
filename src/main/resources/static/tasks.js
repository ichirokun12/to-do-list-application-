// Global variables
    let jsConfetti;
    let sortableInstance;
    let currentTask = null;
    let timerInterval = null;
    let timerMinutes = 25;
    let timerSeconds = 0;
    let timerRunning = false;
    let allTasks = [];
    let backgroundPreviewImage = null;
    let currentBackgroundSettings = {
        image: null,
        opacity: 0.8, // Increased default opacity
        position: 'center center',
        size: 'cover'
    };

    // Initialize everything when DOM is loaded
    document.addEventListener('DOMContentLoaded', function() {
        // Initialize confetti
        jsConfetti = new JSConfetti();

        // Load theme and background
        loadTheme();
        loadBackgroundSettings();

        // Apply task colors and collect tasks
        applyTaskColors();
        collectAllTasks();
        updateStatistics();

        // Initialize drag and drop
        initializeDragAndDrop();

        // Add input animations
        addInputAnimations();

        // Setup keyboard shortcuts
        setupKeyboardShortcuts();

        // Setup search functionality
        setupSearch();

        // Setup filter buttons
        setupFilters();

        // Setup background functionality
        setupBackgroundControls();

        // Play welcome sound
        playSound('welcome');
    });

    // Theme management
    function toggleTheme() {
        const body = document.body;
        const themeIcon = document.getElementById('theme-icon');
        const themeText = document.getElementById('theme-text');

        const isDark = body.getAttribute('data-theme') === 'dark';

        if (isDark) {
            body.setAttribute('data-theme', 'light');
            themeIcon.className = 'fas fa-moon';
            themeText.textContent = 'Dark';
            localStorage.setItem('theme', 'light');
        } else {
            body.setAttribute('data-theme', 'dark');
            themeIcon.className = 'fas fa-sun';
            themeText.textContent = 'Light';
            localStorage.setItem('theme', 'dark');
        }

        // Enhanced animation
        const toggleBtn = document.getElementById('themeToggle');
        toggleBtn.style.transform = 'scale(1.2) rotate(180deg)';
        setTimeout(() => {
            toggleBtn.style.transform = '';
        }, 300);

        showToast('Theme changed!', 'info');
    }

    function loadTheme() {
        const savedTheme = localStorage.getItem('theme') || 'light';
        const body = document.body;
        const themeIcon = document.getElementById('theme-icon');
        const themeText = document.getElementById('theme-text');

        body.setAttribute('data-theme', savedTheme);

        if (savedTheme === 'dark') {
            themeIcon.className = 'fas fa-sun';
            themeText.textContent = 'Light';
        } else {
            themeIcon.className = 'fas fa-moon';
            themeText.textContent = 'Dark';
        }
    }

    // Background Management System
    function setupBackgroundControls() {
        const fileInput = document.getElementById('backgroundFileInput');
        const uploadArea = document.getElementById('uploadArea');
        const opacitySlider = document.getElementById('opacitySlider');
        const opacityValue = document.getElementById('opacityValue');

        // File input handling
        fileInput.addEventListener('change', handleFileSelect);

        // Drag and drop handling
        uploadArea.addEventListener('dragover', handleDragOver);
        uploadArea.addEventListener('dragleave', handleDragLeave);
        uploadArea.addEventListener('drop', handleFileDrop);

        // Opacity slider
        opacitySlider.addEventListener('input', function() {
            const value = parseFloat(this.value);
            currentBackgroundSettings.opacity = value;
            opacityValue.textContent = Math.round(value * 100) + '%';
            updateBackgroundPreview();

            // Update preset buttons
            updateOpacityPresetButtons(value);
        });

        // Position buttons
        document.querySelectorAll('.position-btn[data-position]').forEach(btn => {
            btn.addEventListener('click', function() {
                document.querySelectorAll('.position-btn[data-position]').forEach(b => b.classList.remove('active'));
                this.classList.add('active');
                currentBackgroundSettings.position = this.dataset.position;
                updateBackgroundPreview();
            });
        });

        // Size buttons
        document.querySelectorAll('.position-btn[data-size]').forEach(btn => {
            btn.addEventListener('click', function() {
                document.querySelectorAll('.position-btn[data-size]').forEach(b => b.classList.remove('active'));
                this.classList.add('active');
                currentBackgroundSettings.size = this.dataset.size;
                updateBackgroundPreview();
            });
        });
    }

    // Opacity preset functions
    function setOpacityPreset(value) {
        currentBackgroundSettings.opacity = value;
        document.getElementById('opacitySlider').value = value;
        document.getElementById('opacityValue').textContent = Math.round(value * 100) + '%';
        updateOpacityPresetButtons(value);
        updateBackgroundPreview();
        playSound('click');
    }

    function updateOpacityPresetButtons(currentValue) {
        document.querySelectorAll('.opacity-preset').forEach(btn => {
            btn.classList.remove('active');
            if (Math.abs(parseFloat(btn.dataset.opacity) - currentValue) < 0.05) {
                btn.classList.add('active');
            }
        });
    }

    function handleFileSelect(event) {
        const file = event.target.files[0];
        if (file) {
            processImageFile(file);
        }
    }

    function handleDragOver(event) {
        event.preventDefault();
        event.stopPropagation();
        document.getElementById('uploadArea').classList.add('dragover');
    }

    function handleDragLeave(event) {
        event.preventDefault();
        event.stopPropagation();
        document.getElementById('uploadArea').classList.remove('dragover');
    }

    function handleFileDrop(event) {
        event.preventDefault();
        event.stopPropagation();

        const uploadArea = document.getElementById('uploadArea');
        uploadArea.classList.remove('dragover');

        const files = event.dataTransfer.files;
        if (files.length > 0) {
            const file = files[0];
            processImageFile(file);
        }
    }

    function processImageFile(file) {
        // Validate file type
        if (!file.type.startsWith('image/')) {
            showToast('Please select an image file', 'error');
            return;
        }

        // Validate file size (5MB limit)
        if (file.size > 5 * 1024 * 1024) {
            showToast('File size must be less than 5MB', 'error');
            return;
        }

        const reader = new FileReader();
        reader.onload = function(e) {
            backgroundPreviewImage = e.target.result;
            showBackgroundPreview(e.target.result);
            showToast('Image loaded! Click Apply to set as background', 'success');
        };

        reader.onerror = function() {
            showToast('Error reading file', 'error');
        };

        reader.readAsDataURL(file);
    }

    function showBackgroundPreview(imageData) {
        const preview = document.getElementById('backgroundPreview');
        preview.style.backgroundImage = `url(${imageData})`;
        preview.classList.add('visible');
    }

    function clearPreview() {
        const preview = document.getElementById('backgroundPreview');
        preview.classList.remove('visible');
        preview.style.backgroundImage = '';
        backgroundPreviewImage = null;
        document.getElementById('backgroundFileInput').value = '';
        showToast('Preview cleared', 'info');
    }

    function applyBackground() {
        if (!backgroundPreviewImage) {
            showToast('No image to apply', 'error');
            return;
        }

        currentBackgroundSettings.image = backgroundPreviewImage;
        updateBackgroundDisplay();
        clearPreview();
        showToast('Background applied! Your wallpaper is now clearly visible!', 'success');
        playSound('success');
    }

    function updateBackgroundPreview() {
        if (currentBackgroundSettings.image) {
            updateBackgroundDisplay();
        }
    }

    function updateBackgroundDisplay() {
        const customBg = document.getElementById('customBackground');
        const body = document.body;

        if (currentBackgroundSettings.image) {
            customBg.style.backgroundImage = `url(${currentBackgroundSettings.image})`;
            customBg.style.backgroundPosition = currentBackgroundSettings.position;
            customBg.style.backgroundSize = currentBackgroundSettings.size;
            customBg.style.setProperty('--bg-opacity', currentBackgroundSettings.opacity);
            customBg.classList.add('visible');
            body.classList.add('has-custom-bg');
        } else {
            customBg.classList.remove('visible');
            body.classList.remove('has-custom-bg');
        }
    }

    function resetBackground() {
        currentBackgroundSettings = {
            image: null,
            opacity: 0.8, // Reset to higher default
            position: 'center center',
            size: 'cover'
        };

        // Reset UI controls
        document.getElementById('opacitySlider').value = 0.8;
        document.getElementById('opacityValue').textContent = '80%';
        updateOpacityPresetButtons(0.8);
        document.querySelectorAll('.position-btn[data-position]').forEach(b => b.classList.remove('active'));
        document.querySelector('.position-btn[data-position="center center"]').classList.add('active');
        document.querySelectorAll('.position-btn[data-size]').forEach(b => b.classList.remove('active'));
        document.querySelector('.position-btn[data-size="cover"]').classList.add('active');

        updateBackgroundDisplay();
        clearPreview();
        showToast('Background reset to clear visibility settings', 'info');
    }

    function saveBackground() {
        localStorage.setItem('backgroundSettings', JSON.stringify(currentBackgroundSettings));
        showToast('Background settings saved! Your wallpaper will stay beautifully visible!', 'success');
        playSound('success');
    }

    function loadBackgroundSettings() {
        const saved = localStorage.getItem('backgroundSettings');
        if (saved) {
            try {
                currentBackgroundSettings = JSON.parse(saved);

                // Ensure minimum opacity for better visibility
                if (currentBackgroundSettings.opacity < 0.5) {
                    currentBackgroundSettings.opacity = 0.8;
                }

                // Update UI controls
                document.getElementById('opacitySlider').value = currentBackgroundSettings.opacity;
                document.getElementById('opacityValue').textContent = Math.round(currentBackgroundSettings.opacity * 100) + '%';
                updateOpacityPresetButtons(currentBackgroundSettings.opacity);

                // Update position buttons
                document.querySelectorAll('.position-btn[data-position]').forEach(b => b.classList.remove('active'));
                const posBtn = document.querySelector(`[data-position="${currentBackgroundSettings.position}"]`);
                if (posBtn) posBtn.classList.add('active');

                // Update size buttons
                document.querySelectorAll('.position-btn[data-size]').forEach(b => b.classList.remove('active'));
                const sizeBtn = document.querySelector(`[data-size="${currentBackgroundSettings.size}"]`);
                if (sizeBtn) sizeBtn.classList.add('active');

                updateBackgroundDisplay();
            } catch (e) {
                console.log('Error loading background settings:', e);
            }
        }
    }

    function toggleBackgroundPanel() {
        const panel = document.getElementById('backgroundPanel');
        panel.classList.toggle('visible');
    }

    // Enhanced task color application
    function applyTaskColors() {
        const taskCards = document.querySelectorAll('.task-card');
        taskCards.forEach(function(card) {
            const status = card.dataset.status;
            const priority = card.dataset.priority;

            // Remove existing classes
            card.classList.remove('status-done', 'status-not-done', 'status-partially-done');

            // Apply status classes
            if (status === 'done') {
                card.classList.add('status-done');
            } else if (status === 'not done') {
                card.classList.add('status-not-done');
            } else if (status === 'partially done') {
                card.classList.add('status-partially-done');
            }
        });
    }

    // Collect all tasks for filtering and statistics
    function collectAllTasks() {
        const taskCards = document.querySelectorAll('.task-card');
        allTasks = Array.from(taskCards).map(card => ({
            element: card,
            title: card.querySelector('.task-title').textContent,
            status: card.dataset.status || '',
            priority: card.dataset.priority || '',
            id: card.dataset.taskId
        }));
    }

    // Update task statistics
    function updateStatistics() {
        const total = allTasks.length;
        const completed = allTasks.filter(t => t.status === 'done').length;
        const pending = allTasks.filter(t => t.status === 'not done').length;
        const completionRate = total > 0 ? Math.round((completed / total) * 100) : 0;

        document.getElementById('totalTasks').textContent = total;
        document.getElementById('completedTasks').textContent = completed;
        document.getElementById('pendingTasks').textContent = pending;
        document.getElementById('completionRate').textContent = completionRate + '%';
    }

    // Initialize drag and drop
    function initializeDragAndDrop() {
        const tasksList = document.getElementById('tasksList');
        if (tasksList) {
            sortableInstance = Sortable.create(tasksList, {
                animation: 300,
                ghostClass: 'sortable-ghost',
                chosenClass: 'dragging',
                onStart: function(evt) {
                    evt.item.style.transform = 'rotate(5deg)';
                    playSound('drag');
                },
                onEnd: function(evt) {
                    evt.item.style.transform = '';
                    if (evt.oldIndex !== evt.newIndex) {
                        showToast('Task reordered!', 'success');
                        playSound('drop');
                    }
                }
            });
        }
    }

    // Enhanced search functionality
    function setupSearch() {
        const searchBox = document.getElementById('searchBox');
        const searchResults = document.getElementById('searchResults');

        searchBox.addEventListener('input', function() {
            const query = this.value.toLowerCase().trim();
            let visibleCount = 0;

            allTasks.forEach(task => {
                const matches = task.title.toLowerCase().includes(query) ||
                               task.status.toLowerCase().includes(query) ||
                               task.priority.toLowerCase().includes(query);

                if (matches || query === '') {
                    task.element.style.display = 'block';
                    visibleCount++;
                } else {
                    task.element.style.display = 'none';
                }
            });

            // Update search results info
            if (query) {
                searchResults.textContent = `${visibleCount} found`;
                searchResults.classList.add('visible');
            } else {
                searchResults.classList.remove('visible');
            }

            updateVisibleTaskCount(visibleCount);
            toggleEmptyState(visibleCount === 0 && query !== '');
        });
    }

    // Setup filter buttons
    function setupFilters() {
        const filterButtons = document.querySelectorAll('.filter-btn');

        filterButtons.forEach(btn => {
            btn.addEventListener('click', function() {
                // Update active state
                filterButtons.forEach(b => b.classList.remove('active'));
                this.classList.add('active');

                const filter = this.dataset.filter;
                filterTasks(filter);
            });
        });
    }

    // Filter tasks
    function filterTasks(filter) {
        let visibleCount = 0;

        allTasks.forEach(task => {
            let shouldShow = false;

            switch(filter) {
                case 'all':
                    shouldShow = true;
                    break;
                case 'done':
                    shouldShow = task.status === 'done';
                    break;
                case 'not done':
                    shouldShow = task.status === 'not done';
                    break;
                case 'high':
                    shouldShow = task.priority === 'high';
                    break;
                default:
                    shouldShow = task.status === filter || task.priority === filter;
            }

            if (shouldShow) {
                task.element.style.display = 'block';
                visibleCount++;
            } else {
                task.element.style.display = 'none';
            }
        });

        updateVisibleTaskCount(visibleCount);
        toggleEmptyState(visibleCount === 0);
        showToast(`Showing ${visibleCount} tasks`, 'info');
    }

    // Update visible task count
    function updateVisibleTaskCount(count) {
        document.getElementById('visibleTaskCount').textContent = count + ' tasks';
    }

    // Toggle empty state
    function toggleEmptyState(show) {
        const emptyState = document.getElementById('emptyState');
        if (emptyState) {
            emptyState.style.display = show ? 'block' : 'none';
        }
    }

    // Enhanced toast notifications
    function showToast(message, type = 'info', duration = 3000) {
        const toastContainer = document.getElementById('toastContainer');
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;

        const icon = type === 'success' ? 'fas fa-check-circle' :
                    type === 'error' ? 'fas fa-exclamation-circle' :
                    'fas fa-info-circle';

        toast.innerHTML = `
            <i class="${icon}"></i>
            <span>${message}</span>
        `;

        toastContainer.appendChild(toast);

        // Trigger animation
        setTimeout(() => toast.classList.add('show'), 100);

        // Remove toast
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            }, 400);
        }, duration);
    }

    // Context menu functionality
    function showContextMenu(event, taskCard) {
        event.preventDefault();
        currentTask = taskCard;

        const contextMenu = document.getElementById('contextMenu');
        contextMenu.style.display = 'block';
        contextMenu.style.left = event.pageX + 'px';
        contextMenu.style.top = event.pageY + 'px';

        // Hide context menu when clicking elsewhere
        document.addEventListener('click', hideContextMenu);
    }

    function hideContextMenu() {
        const contextMenu = document.getElementById('contextMenu');
        contextMenu.style.display = 'none';
        document.removeEventListener('click', hideContextMenu);
    }

    // Inline task editing
    function editTaskInline(taskCard = currentTask) {
        if (!taskCard) return;

        const titleElement = taskCard.querySelector('.task-title');
        const originalText = titleElement.textContent;

        titleElement.contentEditable = true;
        titleElement.classList.add('editing');
        titleElement.focus();

        // Select all text
        const range = document.createRange();
        range.selectNodeContents(titleElement);
        const selection = window.getSelection();
        selection.removeAllRanges();
        selection.addRange(range);

        function finishEditing() {
            titleElement.contentEditable = false;
            titleElement.classList.remove('editing');
            const newText = titleElement.textContent.trim();

            if (newText !== originalText && newText !== '') {
                showToast('Task updated!', 'success');
                playSound('success');
            } else {
                titleElement.textContent = originalText;
            }
        }

        titleElement.addEventListener('blur', finishEditing);
        titleElement.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                finishEditing();
            } else if (e.key === 'Escape') {
                titleElement.textContent = originalText;
                finishEditing();
            }
        });

        hideContextMenu();
    }

    // Task duplication
    function duplicateTask() {
        if (!currentTask) return;

        const taskCard = currentTask.cloneNode(true);
        const title = taskCard.querySelector('.task-title');
        title.textContent = '(Copy) ' + title.textContent;

        // Remove task ID to avoid conflicts
        taskCard.removeAttribute('data-task-id');

        // Insert after current task
        currentTask.parentNode.insertBefore(taskCard, currentTask.nextSibling);

        // Update task list
        collectAllTasks();
        updateStatistics();

        showToast('Task duplicated!', 'success');
        playSound('success');
        hideContextMenu();
    }

    // Priority toggle
    function togglePriority() {
        if (!currentTask) return;

        const priorityBadge = currentTask.querySelector('.priority-badge');
        const currentPriority = currentTask.dataset.priority;

        const priorities = ['low', 'medium', 'high'];
        const currentIndex = priorities.indexOf(currentPriority);
        const nextIndex = (currentIndex + 1) % priorities.length;
        const newPriority = priorities[nextIndex];

        currentTask.dataset.priority = newPriority;
        priorityBadge.textContent = newPriority.charAt(0).toUpperCase() + newPriority.slice(1);
        priorityBadge.className = `task-meta-value priority-badge priority-${newPriority}`;

        showToast(`Priority changed to ${newPriority}`, 'info');
        playSound('click');
        hideContextMenu();
    }

    // Timer functionality
    function toggleTimer() {
        const timerWidget = document.getElementById('timerWidget');
        timerWidget.classList.toggle('active');

        if (timerWidget.classList.contains('active')) {
            showToast('Timer ready!', 'info');
        }
    }

    function startTimer() {
        if (timerRunning) return;

        timerRunning = true;
        timerInterval = setInterval(() => {
            if (timerSeconds === 0) {
                if (timerMinutes === 0) {
                    // Timer finished
                    resetTimer();
                    showToast('⏰ Time\'s up! Great work!', 'success');
                    playSound('complete');
                    jsConfetti.addConfetti({
                        emojis: ['⏰', '🎉', '✨', '🔥'],
                        emojiSize: 50,
                        confettiNumber: 30,
                    });
                    return;
                }
                timerMinutes--;
                timerSeconds = 59;
            } else {
                timerSeconds--;
            }
            updateTimerDisplay();
        }, 1000);

        showToast('Timer started!', 'success');
        playSound('start');
    }

    function pauseTimer() {
        if (!timerRunning) return;

        timerRunning = false;
        clearInterval(timerInterval);
        showToast('Timer paused', 'info');
    }

    function resetTimer() {
        timerRunning = false;
        clearInterval(timerInterval);
        timerMinutes = 25;
        timerSeconds = 0;
        updateTimerDisplay();
        showToast('Timer reset', 'info');
    }

    function updateTimerDisplay() {
        const display = document.getElementById('timerDisplay');
        const mins = timerMinutes.toString().padStart(2, '0');
        const secs = timerSeconds.toString().padStart(2, '0');
        display.textContent = `${mins}:${secs}`;
    }

    function startTaskTimer(button) {
        const taskCard = button.closest('.task-card');
        const taskTitle = taskCard.querySelector('.task-title').textContent;

        // Show timer widget
        toggleTimer();

        // Start timer if not running
        if (!timerRunning) {
            startTimer();
        }

        // Add visual indication
        button.classList.add('active');
        showToast(`Timer started for: ${taskTitle}`, 'success');
    }

    // Enhanced form submission with confetti
    function handleFormSubmit(event) {
        const submitBtn = event.target.querySelector('.submit-btn');
        const form = event.target;

        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Adding...';
        submitBtn.disabled = true;
        form.classList.add('loading');

        // Celebrate with confetti
        jsConfetti.addConfetti({
            emojis: ['✨', '🎉', '⭐', '🌟'],
            emojiSize: 40,
            confettiNumber: 50,
        });

        showToast('✨ Task added successfully!', 'success');
        playSound('success');

        return true;
    }

    // Enhanced completion with celebration
    function handleMarkDone(event, form) {
        const button = form.querySelector('.btn-done');
        const taskCard = form.closest('.task-card');
        const taskTitle = taskCard.querySelector('.task-title').textContent;

        button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Marking...';
        button.disabled = true;
        taskCard.classList.add('loading');

        // Massive celebration for task completion!
        jsConfetti.addConfetti({
            emojis: ['🎉', '🥳', '🎊', '✨', '🔥', '💪', '🌟', '⭐'],
            emojiSize: 60,
            confettiNumber: 100,
        });

        // Secondary confetti burst
        setTimeout(() => {
            jsConfetti.addConfetti({
                confettiColors: ['#ff0a54', '#ff477e', '#ff7096', '#ff85a1', '#fbb1bd', '#f9bec7'],
                confettiNumber: 200,
            });
        }, 500);

        showToast(`🎉 Awesome! "${taskTitle}" completed!`, 'success', 4000);
        playSound('complete');

        return true;
    }

    // Enhanced deletion with animation
    function handleDelete(event, form) {
        const taskCard = form.closest('.task-card');
        const taskTitle = taskCard.querySelector('.task-title').textContent;

        const confirmed = confirm(`Are you sure you want to delete "${taskTitle}"?`);

        if (!confirmed) {
            event.preventDefault();
            return false;
        }

        const button = form.querySelector('.btn-delete');
        button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';
        button.disabled = true;
        taskCard.classList.add('loading');

        // Enhanced exit animation
        taskCard.style.transition = 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)';
        taskCard.style.opacity = '0';
        taskCard.style.transform = 'translateX(100px) scale(0.9)';

        showToast(`🗑 "${taskTitle}" deleted`, 'error');
        playSound('delete');

        return true;
    }

    function deleteTaskFromContext() {
        if (!currentTask) return;

        const deleteForm = currentTask.querySelector('form[action*="delete"]');
        if (deleteForm) {
            const event = { preventDefault: () => {} };
            handleDelete(event, deleteForm);

            // Actually submit the form
            setTimeout(() => {
                deleteForm.submit();
            }, 500);
        }

        hideContextMenu();
    }

    // Keyboard shortcuts
    function setupKeyboardShortcuts() {
        document.addEventListener('keydown', function(event) {
            // Don't trigger shortcuts when typing in inputs
            if (event.target.tagName === 'INPUT' || event.target.tagName === 'TEXTAREA' || event.target.contentEditable === 'true') {
                return;
            }

            const key = event.key.toLowerCase();
            const ctrl = event.ctrlKey || event.metaKey;

            if (ctrl && key === 'k') {
                event.preventDefault();
                document.getElementById('searchBox').focus();
                showToast('Search focused', 'info');
            }

            if (ctrl && key === 'n') {
                event.preventDefault();
                document.getElementById('taskInput').focus();
                showToast('New task focused', 'info');
            }

            if (ctrl && key === 't') {
                event.preventDefault();
                toggleTimer();
            }

            if (ctrl && key === 'd') {
                event.preventDefault();
                toggleTheme();
            }

            if (ctrl && key === 'b') {
                event.preventDefault();
                toggleBackgroundPanel();
            }

            if (key === 'escape') {
                hideContextMenu();
                hideShortcuts();
                const bgPanel = document.getElementById('backgroundPanel');
                if (bgPanel.classList.contains('visible')) {
                    toggleBackgroundPanel();
                }
                const searchBox = document.getElementById('searchBox');
                searchBox.value = '';
                searchBox.dispatchEvent(new Event('input'));
            }

            if (key === '?') {
                event.preventDefault();
                showShortcuts();
            }
        });
    }

    // Shortcuts help
    function showShortcuts() {
        document.getElementById('shortcutsHelp').classList.add('visible');
    }

    function hideShortcuts() {
        document.getElementById('shortcutsHelp').classList.remove('visible');
    }

    // Sound effects
    function playSound(type) {
        // Web Audio API sounds (optional - only if sounds are desired)
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();

        const frequencies = {
            success: [523, 659, 784],  // C5, E5, G5
            complete: [440, 554, 659, 831], // A4, C#5, E5, G#5
            click: [800],
            start: [440, 554],
            delete: [200, 150],
            drag: [600],
            drop: [400],
            welcome: [523, 659, 784, 1047] // C5, E5, G5, C6
        };

        const freq = frequencies[type] || [440];

        freq.forEach((f, i) => {
            setTimeout(() => {
                const oscillator = audioContext.createOscillator();
                const gainNode = audioContext.createGain();

                oscillator.connect(gainNode);
                gainNode.connect(audioContext.destination);

                oscillator.frequency.setValueAtTime(f, audioContext.currentTime);
                oscillator.type = 'sine';

                gainNode.gain.setValueAtTime(0.1, audioContext.currentTime);
                gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.3);

                oscillator.start(audioContext.currentTime);
                oscillator.stop(audioContext.currentTime + 0.3);
            }, i * 100);
        });
    }

    // Export tasks functionality
    function exportTasks() {
        const tasks = allTasks.map(task => ({
            title: task.title,
            status: task.status,
            priority: task.priority,
            id: task.id
        }));

        const dataStr = JSON.stringify(tasks, null, 2);
        const dataBlob = new Blob([dataStr], {type: 'application/json'});

        const url = URL.createObjectURL(dataBlob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'tasks-export.json';
        link.click();

        URL.revokeObjectURL(url);
        showToast('Tasks exported!', 'success');
        playSound('success');
    }

    // Enhanced input animations
    function addInputAnimations() {
        const inputs = document.querySelectorAll('.form-group input, .form-group select');

        inputs.forEach(input => {
            input.addEventListener('focus', function() {
                this.parentElement.style.transform = 'translateY(-2px)';
                playSound('click');
            });

            input.addEventListener('blur', function() {
                this.parentElement.style.transform = '';
            });
        });
    }

    // Performance optimization: Debounced resize handler
    let resizeTimeout;
    window.addEventListener('resize', function() {
        clearTimeout(resizeTimeout);
        resizeTimeout = setTimeout(() => {
            collectAllTasks();
            updateStatistics();
        }, 150);
    });

    // Clean up on page unload
    window.addEventListener('beforeunload', function() {
        if (timerInterval) {
            clearInterval(timerInterval);
        }
        saveBackground(); // Auto-save background settings
    });

