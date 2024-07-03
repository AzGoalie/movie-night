import { type DocumentReference, doc, setDoc } from "firebase/firestore";
import { createFirebaseCallee, createPeerConnecton } from "./signaling";

const video = document.getElementById("video-player") as HTMLVideoElement;
let pc: RTCPeerConnection;

function setupViewer(roomRef: DocumentReference, uid: string) {
  const viewerRef = doc(roomRef, `viewers/${uid}`);
  void setDoc(viewerRef, {});

  const stream = new MediaStream();
  video.srcObject = stream;

  const signaler = createFirebaseCallee(viewerRef);
  pc = createPeerConnecton(signaler, ({ track }) => stream.addTrack(track));
}

export { setupViewer };
