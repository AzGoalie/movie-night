import { auth, db } from "../firebase";
import { signInAnonymously } from "firebase/auth";
import { doc, getDoc } from "firebase/firestore";

import { setupOwner } from "./owner";
import { setupViewer } from "./viewer";

function setHidden(id: string, hidden: boolean) {
  const element = document.getElementById(id);
  if (element) {
    element.hidden = hidden;
  }
}

async function init() {
  const { user } = await signInAnonymously(auth);

  const searchParams = new URLSearchParams(location.search);
  const roomId = searchParams.get("room-id");
  if (roomId === null || roomId.trim() === "") {
    setHidden("loading", true);
    setHidden("invalid-room", false);
    return;
  }

  const roomRef = doc(db, "rooms", roomId);
  const room = await getDoc(roomRef);
  if (!room.exists()) {
    setHidden("loading", true);
    setHidden("invalid-room", false);
    return;
  }

  setHidden("loading", true);
  setHidden("room-wrapper", false);
  if (user.uid === room.data().owner) {
    setupOwner(roomRef);
  } else {
    setupViewer(roomRef, user.uid);
  }
}

void init();
