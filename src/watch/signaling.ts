import { type DocumentReference, updateDoc } from "firebase/firestore";

export const iceConfig = {
  iceServers: [
    {
      urls: ["stun:stun1.l.google.com:19302", "stun:stun2.l.google.com:19302"],
    },
  ],
  iceCandidatePoolSize: 10,
};

type IceHandler = (candidates: RTCIceCandidateInit[]) => void;
type TrackHandler = (event: RTCTrackEvent) => void;

function createPeerConnecton(
  iceHandler: IceHandler,
  trackHandler: TrackHandler
) {
  const pc = new RTCPeerConnection(iceConfig);
  const iceCandidates: RTCIceCandidateInit[] = [];

  pc.onicecandidate = (event) => {
    if (event.candidate !== null) {
      iceCandidates.push(event.candidate.toJSON());
    } else {
      iceHandler(iceCandidates);
    }
  };
  pc.onnegotiationneeded = () =>
    console.log("PeerConnection: Negotiation needed");
  pc.ontrack = trackHandler;

  return pc;
}

function createFirebaseCaller(docRef: DocumentReference) {
  const iceHandler = (candidates: RTCIceCandidate[]) => {
    void updateDoc(docRef, {
      callerIceCandidates: candidates,
    });
  };

  return { iceHandler };
}

export { createPeerConnecton, createFirebaseCaller };
