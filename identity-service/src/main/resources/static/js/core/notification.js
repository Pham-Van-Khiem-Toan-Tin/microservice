class Notification {
    // Hàm private (nội bộ) để render UI
    static max= 4;                 // tối đa bao nhiêu toast cùng lúc
    static dedupeWindowMs= 1500;   // trong 1.5s nếu trùng message+type thì gộp (tuỳ chọn)
    static lastKeyAt = new Map();
    static #show(message, type, duration) {
        const key = `${type}::${message}`;
        const now = Date.now();

        // ===== DEDUPE =====
        const lastAt = this.lastKeyAt.get(key);
        if (lastAt && (now - lastAt) < this.dedupeWindowMs) {
            const $existing = this.findExistingToast(key);
            if ($existing.length) {
                this.bumpCounter($existing);
                this.resetTimer($existing, duration);
                this.lastKeyAt.set(key, now);
                return;
            }
        }
        this.lastKeyAt.set(key, now);

        // ===== LIMIT MAX =====
        const $container = $('#toast-container');
        const $toasts = $container.children('.custom-notification:not([data-toast-template])');
        if ($toasts.length >= this.max) {
            this.remove($toasts.first());
        }

        // ===== CLONE TEMPLATE =====
        const $tpl = $('#toast-template');
        const $toast = $tpl.clone(true, true)
            .removeAttr('id')
            .removeClass('d-none')
            .removeAttr('data-toast-template')
            .attr('data-key', key)
            .addClass(type);

        $toast.find('.notification-content').text(message);

        // close button
        $toast.find('.notification-close')
            .off('click')
            .on('click', () => this.remove($toast));

        // append + animate
        $container.append($toast);
        setTimeout(() => $toast.addClass('show'), 10);

        // auto hide
        this.setTimer($toast, duration);

    }
    static bumpCounter($toast) {
        let count = parseInt($toast.attr('data-count') || '1', 10) + 1;
        $toast.attr('data-count', count);

        let $badge = $toast.find('.toast-count');
        if (!$badge.length) {
            $badge = $('<span class="toast-count badge bg-light text-dark ms-2"></span>');
            $toast.find('.notification-content').append($badge);
        }
        $badge.text('×' + count);
    }

    static setTimer($toast, duration) {
        const old = $toast.data('timer');
        if (old) clearTimeout(old);

        if (duration > 0) {
            const t = setTimeout(() => this.remove($toast), durationSec * 1000);
            $toast.data('timer', t);
        }
    }

    static resetTimer($toast, duration) {
        this.setTimer($toast, duration);
        // nháy nhẹ để thấy toast được cập nhật
        $toast.removeClass('show');
        setTimeout(() => $toast.addClass('show'), 10);
    }
    static findExistingToast(key) {
        return $('#toast-container')
            .children(`.custom-notification[data-key="${this.escapeSelector(key)}"]`)
            .last();
    }
    static remove($toast) {
        const t = $toast.data('timer');
        if (t) clearTimeout(t);

        $toast.removeClass('show');
        setTimeout(() => $toast.remove(), 200);
    }

    static escapeSelector(str) {
        return String(str).replace(/["\\]/g, '\\$&');
    }
    static success(message, duration) {
        this.#show(message, 'success');
    }

    static warning(message, duration) {
        this.#show(message, 'warning');
    }

    static error(message, duration) {
        this.#show(message, 'error');
    }
}