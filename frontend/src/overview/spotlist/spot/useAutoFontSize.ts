import { RefObject, useLayoutEffect, useRef } from 'react';

export function useAutoFontSize<T extends HTMLElement>(minScale = 0.5): RefObject<T> {
    const ref = useRef<T>(null);

    useLayoutEffect(() => {
        const el = ref.current;
        if (!el) return;

        function fit() {
            if (!el) return;
            el.style.fontSize = '';

            const containerWidth = el.offsetWidth;
            if (containerWidth === 0 || el.scrollWidth <= containerWidth) return;

            const defaultFontSize = parseFloat(getComputedStyle(el).fontSize);
            const minFontSize = defaultFontSize * minScale;

            let low = minFontSize;
            let high = defaultFontSize;

            while (high - low > 0.5) {
                const mid = (low + high) / 2;
                el.style.fontSize = `${mid}px`;
                if (el.scrollWidth <= containerWidth) {
                    low = mid;
                } else {
                    high = mid;
                }
            }

            el.style.fontSize = `${low}px`;
        }

        fit();

        const observer = new ResizeObserver(() => requestAnimationFrame(fit));
        observer.observe(el);
        return () => observer.disconnect();
    }, [minScale]);

    return ref as RefObject<T>;
}
