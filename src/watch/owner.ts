import {
  collection,
  deleteField,
  onSnapshot,
  updateDoc,
  type DocumentReference,
} from "firebase/firestore";
import { createFileInput, createLabel } from "../utils/forms";
import { createFirebaseCaller, createPeerConnecton } from "./signaling";

declare global {
  interface HTMLVideoElement {
    captureStream?(): MediaStream;
    mozCaptureStream?(): MediaStream;
  }
}

const video = document.getElementById("video-player") as HTMLVideoElement;

const connections: RTCPeerConnection[] = [];

function handleNewViewer(stream: MediaStream, viewer: DocumentReference) {
  console.log("Viewer Joined");

  const signaler = createFirebaseCaller(viewer);
  const pc = createPeerConnecton(signaler);
  connections.push(pc);

  stream.getTracks().forEach((track) => pc.addTrack(track));
}

function applyFirefoxWorkaround(stream: MediaStream) {
  const audio = new Audio();
  audio.autoplay = true;
  audio.srcObject = stream;

  document.body.appendChild(audio);
}

function onFileSelect(roomRef: DocumentReference) {
  return function (this: HTMLInputElement) {
    if (this.files === null || this.files.length === 0) {
      return;
    }

    const videoFile = this.files[0];
    const videoURL = URL.createObjectURL(videoFile);
    video.src = videoURL;
    video.load();

    const status = document.getElementById("status");
    if (status) {
      status.textContent = videoFile.name;
    }

    void updateDoc(roomRef, { title: videoFile.name });
  };
}

function createMediaStream() {
  const stream = video.captureStream?.() ?? video.mozCaptureStream?.();
  if (!stream) {
    throw new Error("Failed to create stream from video");
  }

  if (video.mozCaptureStream) {
    applyFirefoxWorkaround(stream);
  }

  return stream;
}

function setupControls(roomRef: DocumentReference) {
  const roomWrapper = document.getElementById("room-wrapper");

  const controlSection = document.createElement("section");
  controlSection.id = "control-section";
  roomWrapper?.appendChild(controlSection);

  const fileSelect = createFileInput("video/*", onFileSelect(roomRef));
  const fileSelectLabel = createLabel("Select Video File", fileSelect);
  controlSection.appendChild(fileSelectLabel);

  // TODO: Implement screen sharing
  //
  // const captureButton = createButton("Share Screen", true);
  // const captureLable = createLabel(
  //   "Alternatively share your screen",
  //   captureButton
  // );
  // controlSection.appendChild(captureLable);
}

function featureCheck() {
  if (!video.captureStream && !video.mozCaptureStream) {
    alert("Your browser doesn't support streaming :(");
    throw new Error("Unable to create stream");
  }
}

function setupOwner(roomRef: DocumentReference) {
  featureCheck();
  setupControls(roomRef);

  void updateDoc(roomRef, { title: deleteField() });

  const stream = createMediaStream();
  stream.onaddtrack = ({ track }) =>
    connections.forEach((pc) => pc.addTrack(track));

  const viewers = collection(roomRef, "viewers");
  onSnapshot(viewers, (snapshot) => {
    snapshot
      .docChanges()
      .filter((change) => change.type === "added")
      .forEach(({ doc: { ref } }) => handleNewViewer(stream, ref));
  });
}

export { setupOwner };
