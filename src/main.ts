import { auth, db } from "./firebase";
import { signInAnonymously } from "firebase/auth";
import { collection, doc, setDoc } from "firebase/firestore";

const createRoomButton = document.getElementById(
  "create-room-button"
) as HTMLButtonElement;

function setLoading(element: HTMLElement, loading: boolean, message = "") {
  element.setAttribute("aria-busy", String(loading));
  element.setAttribute("aria-label", message);
}

async function handleCreateRoom() {
  setLoading(createRoomButton, true, "Createing the room...");

  try {
    const { user } = await signInAnonymously(auth);
    const roomRef = doc(collection(db, "rooms"));
    await setDoc(roomRef, { owner: user.uid });

    location.href = `./watch?room-id=${roomRef.id}`;
  } catch (e) {
    console.log(e);
    setLoading(createRoomButton, false);
  }
}

createRoomButton.addEventListener("click", () => void handleCreateRoom());
