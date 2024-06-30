const THEME_KEY = "picoPreferredClorScheme";
const TOGGLE_ID = "theme-toggle";

const preferedTheme = window.matchMedia("(prefers-color-scheme: light)").matches
  ? "light"
  : "dark";

let currentTheme = window.localStorage.getItem(THEME_KEY) ?? preferedTheme;

function applyTheme(theme: string) {
  document.documentElement.setAttribute("data-theme", theme);
  window.localStorage.setItem(THEME_KEY, theme);
}

function toggleTheme() {
  switch (currentTheme) {
    case "dark":
      currentTheme = "light";
      break;
    case "light":
      currentTheme = "dark";
      break;
  }

  applyTheme(currentTheme);
}

if (currentTheme === "light") {
  applyTheme(currentTheme);
}

document.getElementById(TOGGLE_ID)?.addEventListener("click", toggleTheme);

export { toggleTheme };
