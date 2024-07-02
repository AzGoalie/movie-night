import {
  collection,
  onSnapshot,
  type DocumentReference,
} from "firebase/firestore";
import { createFileInput, createLabel } from "../utils/forms";

declare global {
  interface HTMLVideoElement {
    captureStream?(): MediaStream;
    mozCaptureStream?(): MediaStream;
  }
}

const video = document.getElementById("video-player") as HTMLVideoElement;
let currentFileName = "";

function handleNewViewer(stream: MediaStream) {
  console.log("Viewer Joined");
  console.log(stream);
}

function applyFirefoxWorkaround(stream: MediaStream) {
  const audio = new Audio();
  audio.autoplay = true;
  audio.srcObject = stream;

  document.body.appendChild(audio);
}

function onFileSelect(this: HTMLInputElement) {
  if (this.files === null || this.files.length === 0) {
    return;
  }

  const videoFile = this.files[0];
  const videoURL = URL.createObjectURL(videoFile);
  video.src = videoURL;
  video.load();

  currentFileName = videoFile.name;
  const status = document.getElementById("status");
  if (status) {
    status.textContent = currentFileName;
  }
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

function setupControls() {
  const roomWrapper = document.getElementById("room-wrapper");

  const controlSection = document.createElement("section");
  controlSection.style.display = "flex";
  controlSection.style.justifyContent = "space-between";
  controlSection.style.marginTop = "1rem";
  roomWrapper?.appendChild(controlSection);

  const fileSelect = createFileInput("video/*", onFileSelect);
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
  setupControls();

  const stream = createMediaStream();

  const viewers = collection(roomRef, "viewers");
  onSnapshot(viewers, (snapshot) => {
    snapshot
      .docChanges()
      .filter((change) => change.type === "added")
      .forEach(() => handleNewViewer(stream));
  });
}

export { setupOwner };
