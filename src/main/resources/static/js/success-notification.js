(() => {
    document.querySelectorAll('.success-notification').forEach(notification => {
        let dismissed = false;
        let dismissTimer;
        const dismiss = () => {
            if (dismissed) return;
            dismissed = true;
            window.clearTimeout(dismissTimer);
            notification.remove();

            const endpoint = notification.dataset.dismissUrl;
            if (endpoint) {
                fetch(endpoint, { method: 'POST', credentials: 'same-origin' }).catch(() => {});
            }
        };

        const button = notification.querySelector('.success-notification-dismiss');
        if (button) button.addEventListener('click', dismiss);
        dismissTimer = window.setTimeout(dismiss, 3000);
    });
})();
