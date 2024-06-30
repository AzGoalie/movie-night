import {
  collection,
  type DocumentReference,
  onSnapshot,
  QueryDocumentSnapshot,
  updateDoc,
} from "firebase/firestore";
import { iceConfig } from "./signaling";

const video = document.getElementById("video-player") as HTMLVideoElement;
const statusSection = document.getElementById("status-section");

const subscriptions: (() => void)[] = [];

function cleanup() {
  console.log("Stream ended");
  video.srcObject = null;
  subscriptions.forEach((unsubscribe) => unsubscribe());
  subscriptions.length = 0;
}

function handleNewViewer(doc: QueryDocumentSnapshot, stream: MediaStream) {
  // Create new peer connection
  // Send Offer / Recieve Answer
  // Send/Recieve ICE Candidates
  // Add track
  console.log("Viewer Joined");
  console.log(doc.ref.path);

  const pc = new RTCPeerConnection(iceConfig);
  const candidates: RTCIceCandidateInit[] = [];

  pc.onnegotiationneeded = async () => {
    const offer = await pc.createOffer();
    await pc.setLocalDescription(offer);
    await updateDoc(doc.ref, { offer });
  };
  pc.onicecandidate = async (event) => {
    if (event.candidate !== null) {
      candidates.push(event.candidate.toJSON());
    } else {
      await updateDoc(doc.ref, { offerCanidates: candidates });
    }
  };

  stream.getTracks().forEach((track) => pc.addTrack(track));
}

async function captureStream(roomRef: DocumentReference) {
  const stream = await navigator.mediaDevices.getDisplayMedia();

  const viewersRef = collection(roomRef, "viewers");
  const unsubViewers = onSnapshot(viewersRef, (snapshot) =>
    snapshot
      .docChanges()
      .filter(({ type }) => type === "added")
      .forEach(({ doc }) => {
        handleNewViewer(doc, stream);
      })
  );
  subscriptions.push(unsubViewers);

  stream
    .getTracks()
    .forEach((track) => track.addEventListener("ended", cleanup));
  video.srcObject = stream;
}

function createCaptureButton() {
  const button = document.createElement("button");
  button.id = "capture-button";
  button.textContent = "Share your screen";
  button.classList.add("center");

  return button;
}

function setupOwner(roomRef: DocumentReference) {
  const button = createCaptureButton();
  button.onclick = () => captureStream(roomRef);

  statusSection?.appendChild(button);
}

export { setupOwner };
