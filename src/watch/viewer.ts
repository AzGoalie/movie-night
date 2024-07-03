import {
  type DocumentReference,
  doc,
  setDoc,
  onSnapshot,
} from "firebase/firestore";
import { createFirebaseCallee, createPeerConnecton } from "./signaling";

interface Room {
  title?: string;
}

const video = document.getElementById("video-player") as HTMLVideoElement;

function setupViewer(roomRef: DocumentReference<Room>, uid: string) {
  const viewerRef = doc(roomRef, `viewers/${uid}`);
  void setDoc(viewerRef, {});

  const stream = new MediaStream();
  video.srcObject = stream;

  const signaler = createFirebaseCallee(viewerRef);
  createPeerConnecton(signaler, ({ track }) => stream.addTrack(track));

  onSnapshot(roomRef, (snapshot) => {
    const room = snapshot.data();
    const status = document.getElementById("status");
    if (status) {
      status.textContent = room?.title ?? "The broadcast hasn't started";
    }
  });
}

export { setupViewer };
