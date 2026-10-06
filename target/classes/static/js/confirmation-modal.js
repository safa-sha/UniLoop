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

    const panel = modal.querySelector('.uniloop-confirm-modal');
    const title = modal.querySelector('#uniloop-confirm-title');
    const message = modal.querySelector('#uniloop-confirm-message');
    const cancelButton = modal.querySelector('.uniloop-confirm-cancel');
    const confirmButton = modal.querySelector('.uniloop-confirm-submit');
    let pendingForm = null;
    let submitter = null;
    let previousFocus = null;
    let closeTimer = null;

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
        if (!(form instanceof HTMLFormElement) || !form.hasAttribute('data-confirm-message')) return;
        if (form.dataset.confirmApproved === 'true') {
            delete form.dataset.confirmApproved;
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
