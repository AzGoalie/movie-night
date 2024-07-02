import {
  type DocumentReference,
  updateDoc,
  onSnapshot,
} from "firebase/firestore";

export const iceConfig = {
  iceServers: [
    {
      urls: ["stun:stun1.l.google.com:19302", "stun:stun2.l.google.com:19302"],
    },
  ],
  iceCandidatePoolSize: 10,
};

type TrackHandler = (event: RTCTrackEvent) => void;

interface IceHandler {
  sendCandidate(candidate: RTCIceCandidateInit | null): void;
  watchCandidate(
    handler: (candidate: RTCIceCandidateInit | null) => void
  ): void;
}

interface Caller {
  type: "caller";
  sendOffer(offer: RTCSessionDescriptionInit): void;
  watchAnswer(handler: (answer: RTCSessionDescriptionInit) => void): void;
}

interface Callee {
  type: "callee";
  sendAnswer(answer: RTCSessionDescriptionInit): void;
  watchOffer(handler: (offer: RTCSessionDescriptionInit) => void): void;
}

type Signaler = IceHandler & (Callee | Caller);

interface Viewer {
  callerCandidates?: RTCIceCandidateInit[];
  calleeCandidates?: RTCIceCandidateInit[];
  offer?: RTCSessionDescriptionInit;
  answer?: RTCSessionDescriptionInit;
}

function handleNegotiationNeeded(pc: RTCPeerConnection, signaler: Signaler) {
  switch (signaler.type) {
    case "callee":
      return async () => {
        const answer = await pc.createAnswer();
        await pc.setLocalDescription(answer);
        signaler.sendAnswer(answer);
      };
    case "caller":
      return async () => {
        const offer = await pc.createOffer();
        await pc.setLocalDescription(offer);
        signaler.sendOffer(offer);
      };
  }
}

function createPeerConnecton(signaler: Signaler, trackHandler: TrackHandler) {
  const pc = new RTCPeerConnection(iceConfig);

  pc.onicecandidate = ({ candidate }) => signaler.sendCandidate(candidate);
  pc.onnegotiationneeded = handleNegotiationNeeded(pc, signaler);
  pc.ontrack = trackHandler;

  return pc;
}

function createFirebaseIceHandler(
  docRef: DocumentReference<Viewer>,
  field: "callerCandidates" | "calleeCandidates"
): IceHandler {
  const candidates: RTCIceCandidateInit[] = [];
  const sendCandidate = (candidate: RTCIceCandidateInit | null) => {
    if (candidate !== null) {
      candidates.push(candidate);
    } else {
      void updateDoc(docRef, { [field]: candidates }).then(() => {
        candidates.length = 0;
      });
    }
  };

  const watchCandidate = (
    handler: (candidate: RTCIceCandidateInit | null) => void
  ) => {
    const unsubscribe = onSnapshot(docRef, (snapshot) => {
      const viewer = snapshot.data();

      const remoteField: typeof field =
        field === "calleeCandidates" ? "callerCandidates" : "calleeCandidates";
      const remoteCandidates = viewer?.[remoteField];
      if (remoteCandidates) {
        [...remoteCandidates, null].forEach(handler);
        unsubscribe();
      }
    });
  };

  return {
    sendCandidate,
    watchCandidate,
  };
}

function createFirebaseCaller(docRef: DocumentReference<Viewer>): Signaler {
  const watchAnswer = (
    handler: (answer: RTCSessionDescriptionInit) => void
  ) => {
    const unsubscribe = onSnapshot(docRef, (snapshot) => {
      const viewer = snapshot.data();
      if (viewer?.answer) {
        handler(viewer.answer);
        unsubscribe();
      }
    });
  };

  return {
    type: "caller",
    ...createFirebaseIceHandler(docRef, "callerCandidates"),
    sendOffer: (offer) => void updateDoc(docRef, { offer }),
    watchAnswer,
  };
}

function createFirebaseCallee(docRef: DocumentReference<Viewer>): Signaler {
  const watchOffer = (handler: (answer: RTCSessionDescriptionInit) => void) => {
    const unsubscribe = onSnapshot(docRef, (snapshot) => {
      const viewer = snapshot.data();
      if (viewer?.answer) {
        handler(viewer.answer);
        unsubscribe();
      }
    });
  };

  return {
    type: "callee",
    ...createFirebaseIceHandler(docRef, "calleeCandidates"),
    sendAnswer: (answer) => void updateDoc(docRef, { answer }),
    watchOffer,
  };
}

export { createPeerConnecton, createFirebaseCaller, createFirebaseCallee };
