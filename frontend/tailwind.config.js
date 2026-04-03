/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                brand: {
                    dark: "#0f172a",
                    glass: 'rgba(30,41,59,0.7)',
                    accent: '#38bdf8', //Sky Blue
                }
            },
            backdropBlur: {
                xs: '2px',
            }
        },
    },
    plugins: [],
}