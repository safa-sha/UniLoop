(function () {
    const modal = document.createElement('div');
    modal.className = 'uniloop-confirm-backdrop';
    modal.hidden = true;
    modal.innerHTML = `
        <section class="uniloop-confirm-modal" role="alertdialog" aria-modal="true" aria-labelledby="uniloop-confirm-title" aria-describedby="uniloop-confirm-message" tabindex="-1">
            <p class="uniloop-confirm-eyebrow">UNILOOP</p>
            <h2 id="uniloop-confirm-title">Confirm Action</h2>
            <p id="uniloop-confirm-message"></p>
            <div class="uniloop-confirm-actions">
                <button type="button" class="uniloop-confirm-cancel">Cancel</button>
                <button type="button" class="uniloop-confirm-submit">Confirm</button>
            </div>
        </section>`;
    document.body.appendChild(modal);

    const title = modal.querySelector('#uniloop-confirm-title');
    const message = modal.querySelector('#uniloop-confirm-message');
    const cancelButton = modal.querySelector('.uniloop-confirm-cancel');
    const confirmButton = modal.querySelector('.uniloop-confirm-submit');
    const routeConfirmations = {
        '/profile': {
            title: 'Save profile changes?',
            message: 'Save your profile changes?',
            text: 'Save Changes'
        },
        '/settings/password': {
            title: 'Change your password?',
            message: 'Are you sure you want to change your password?',
            text: 'Change Password',
            validatePasswordMatch: true
        }
    };
    let pendingForm = null;
    let submitter = null;
    let previousFocus = null;
    let closeTimer = null;

    function validatePasswordMatch(form) {
        const newPassword = form.querySelector('[name="newPassword"]');
        const confirmPassword = form.querySelector('[name="confirmPassword"]');
        if (!(newPassword instanceof HTMLInputElement)
                || !(confirmPassword instanceof HTMLInputElement)) return true;

        let error = form.querySelector('#confirmPasswordError');
        if (!error) {
            error = document.createElement('small');
            error.id = 'confirmPasswordError';
            error.className = 'account-field-error';
            error.setAttribute('role', 'alert');
            error.setAttribute('aria-live', 'assertive');
            error.hidden = true;
            confirmPassword.insertAdjacentElement('afterend', error);
        }
        confirmPassword.setAttribute('aria-describedby', error.id);

        const mismatch = newPassword.value !== '' && confirmPassword.value !== ''
            && newPassword.value !== confirmPassword.value;
        const errorMessage = 'The new password and confirmation do not match.';
        confirmPassword.setCustomValidity(mismatch ? errorMessage : '');
        error.textContent = mismatch ? errorMessage : '';
        error.hidden = !mismatch;
        if (mismatch) confirmPassword.setAttribute('aria-invalid', 'true');
        else confirmPassword.removeAttribute('aria-invalid');
        return !mismatch;
    }

    const passwordForm = document.querySelector('form[action="/settings/password"]');
    if (passwordForm) {
        const newPassword = passwordForm.querySelector('[name="newPassword"]');
        const confirmPassword = passwordForm.querySelector('[name="confirmPassword"]');
        if (newPassword instanceof HTMLInputElement && confirmPassword instanceof HTMLInputElement) {
            newPassword.addEventListener('input', function () { validatePasswordMatch(passwordForm); });
            confirmPassword.addEventListener('input', function () { validatePasswordMatch(passwordForm); });
        }
    }

    function hasConfirmation(form) {
        if (form.hasAttribute('data-confirm-message')) return true;
        if (form.method.toLowerCase() !== 'post') return false;

        const confirmation = routeConfirmations[new URL(form.action, window.location.href).pathname];
        if (!confirmation) return false;
        form.dataset.confirmTitle = confirmation.title;
        form.dataset.confirmMessage = confirmation.message;
        form.dataset.confirmText = confirmation.text;
        return true;
    }

    function closeModal(restoreFocus) {
        if (modal.hidden) return;
        modal.classList.remove('is-open');
        window.clearTimeout(closeTimer);
        closeTimer = window.setTimeout(function () {
            modal.hidden = true;
            pendingForm = null;
            submitter = null;
            if (restoreFocus && previousFocus && document.contains(previousFocus)) previousFocus.focus();
        }, 170);
    }

    function openModal(form, button) {
        window.clearTimeout(closeTimer);
        pendingForm = form;
        submitter = button;
        previousFocus = document.activeElement;
        confirmButton.disabled = false;
        title.textContent = form.dataset.confirmTitle || 'Confirm Action';
        message.textContent = form.dataset.confirmMessage || 'Are you sure you want to continue?';
        confirmButton.textContent = form.dataset.confirmText || 'Confirm';
        modal.hidden = false;
        window.requestAnimationFrame(function () {
            modal.classList.add('is-open');
            cancelButton.focus();
        });
    }

    document.addEventListener('submit', function (event) {
        const form = event.target;
        if (!(form instanceof HTMLFormElement)) return;
        if (form.dataset.adminAction === 'true'
                && form.dataset.confirmSubmitting === 'true') {
            event.preventDefault();
            return;
        }
        const confirmation = routeConfirmations[new URL(form.action, window.location.href).pathname];
        if (confirmation && confirmation.validatePasswordMatch && !validatePasswordMatch(form)) {
            event.preventDefault();
            const confirmPassword = form.querySelector('[name="confirmPassword"]');
            if (confirmPassword instanceof HTMLInputElement) confirmPassword.reportValidity();
            return;
        }
        if (!hasConfirmation(form)) return;
        if (form.dataset.confirmApproved === 'true') {
            delete form.dataset.confirmApproved;
            if (form.dataset.adminAction === 'true') {
                if (form.dataset.confirmSubmitting === 'true') {
                    event.preventDefault();
                    return;
                }
                form.dataset.confirmSubmitting = 'true';
                form.querySelectorAll('button[type="submit"], input[type="submit"]')
                    .forEach(function (button) { button.disabled = true; });
            }
            return;
        }
        event.preventDefault();
        openModal(form, event.submitter || null);
    });

    cancelButton.addEventListener('click', function () { closeModal(true); });
    confirmButton.addEventListener('click', function () {
        if (!pendingForm) return;
        const form = pendingForm;
        const button = submitter;
        if (typeof form.reportValidity === 'function' && !form.reportValidity()) return;
        confirmButton.disabled = true;
        closeModal(false);
        form.dataset.confirmApproved = 'true';
        if (typeof form.requestSubmit === 'function') {
            if (button && button.form === form) form.requestSubmit(button);
            else form.requestSubmit();
        } else {
            form.submit();
        }
    });

    modal.addEventListener('click', function (event) {
        if (event.target === modal) closeModal(true);
    });
    document.addEventListener('keydown', function (event) {
        if (modal.hidden) return;
        if (event.key === 'Escape') {
            event.preventDefault();
            closeModal(true);
        } else if (event.key === 'Tab') {
            event.preventDefault();
            (document.activeElement === cancelButton ? confirmButton : cancelButton).focus();
        }
    });
})();
