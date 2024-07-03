export function createLabel(text: string, children?: Node) {
  const label = document.createElement("label");
  label.textContent = text;

  if (children) {
    label.appendChild(children);
  }

  return label;
}

export function createFileInput(
  accept: string,
  onChange: (this: HTMLInputElement, e: Event) => void
) {
  const input = document.createElement("input");
  input.type = "file";
  input.accept = accept;
  input.addEventListener("change", onChange, false);

  return input;
}

export function createButton(
  text: string,
  block = false,
  onClick?: () => void
) {
  const button = document.createElement("button");
  button.textContent = text;

  if (onClick) {
    button.onclick = onClick;
  }

  if (block) {
    button.style.display = "block";
  }

  return button;
}
