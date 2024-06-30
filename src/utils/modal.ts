const TRANSITION_TIME = 400;

function openModal(modal: HTMLDialogElement) {
  document.documentElement.classList.add("modal-is-opening", "modal-is-open");
  setTimeout(() => {
    document.documentElement.classList.remove("modal-is-opening");
  }, TRANSITION_TIME);

  modal.showModal();
}

function closeModal(modal: HTMLDialogElement) {
  document.documentElement.classList.add("modal-is-closing");
  setTimeout(() => {
    document.documentElement.classList.remove(
      "modal-is-closing",
      "modal-is-open"
    );
    modal.close();
  }, TRANSITION_TIME);
}

function handleClick(modal: HTMLDialogElement) {
  return (event: MouseEvent) => {
    if (event.target instanceof HTMLElement) {
      if (!modal.querySelector("article")?.contains(event.target)) {
        closeModal(modal);
      }
    }
  };
}

function handleEscape(modal: HTMLDialogElement) {
  return (event: KeyboardEvent) => {
    if (event.key === "Escape") {
      event.preventDefault();
      closeModal(modal);
    }
  };
}

function addEventListener(modal: HTMLDialogElement) {
  const clickHandler = handleClick(modal);
  const keyHandler = handleEscape(modal);

  document.addEventListener("click", clickHandler);
  document.addEventListener("keydown", keyHandler);

  return () => {
    document.removeEventListener("click", clickHandler);
    document.removeEventListener("keydown", keyHandler);
  };
}

function toggleModal(event: Event) {
  const currentTarget = event.currentTarget;
  if (currentTarget instanceof HTMLElement && currentTarget.dataset.target) {
    const modal = document.getElementById(currentTarget.dataset.target);
    if (modal instanceof HTMLDialogElement) {
      event.stopPropagation();

      modal.addEventListener("close", addEventListener(modal), { once: true });
      modal.open ? closeModal(modal) : openModal(modal);
    }
  }
}

document
  .querySelectorAll<HTMLButtonElement>("button[data-target]")
  .forEach((element) => (element.onclick = toggleModal));

export { toggleModal };
