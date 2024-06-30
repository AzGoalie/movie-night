import { type DocumentReference, doc, setDoc } from "firebase/firestore";

function setupViewer(roomRef: DocumentReference, uid: string) {
  console.log("I am a viewer");
  const viewerRef = doc(roomRef, `viewers/${uid}`);
  void setDoc(viewerRef, {});
}

export { setupViewer };
