(() => {
    document.querySelectorAll('.success-notification').forEach(notification => {
        let dismissed = false;
        const dismiss = () => {
            if (dismissed) return;
            dismissed = true;
            notification.remove();

            const endpoint = notification.dataset.dismissUrl;
            if (endpoint) {
                fetch(endpoint, { method: 'POST', credentials: 'same-origin' }).catch(() => {});
            }
        };

        const button = notification.querySelector('.success-notification-dismiss');
        if (button) button.addEventListener('click', dismiss);
        window.setTimeout(dismiss, 10000);
    });
})();
