import {
  type DocumentReference,
  updateDoc,
  onSnapshot,
  deleteDoc,
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
  sendCandidate(candidate: RTCIceCandidate): void;
  watchCandidate(handler: (candidate: RTCIceCandidate) => void): void;
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

function createPeerConnecton(signaler: Signaler, trackHandler?: TrackHandler) {
  const pc = new RTCPeerConnection(iceConfig);

  pc.onicecandidate = ({ candidate }) => {
    if (candidate) {
      signaler.sendCandidate(candidate);
    }
  };

  if (trackHandler) {
    pc.ontrack = trackHandler;
  }

  switch (signaler.type) {
    case "callee":
      signaler.watchOffer((offer) => {
        pc.setRemoteDescription(offer)
          .then(() => pc.createAnswer())
          .then((answer) => pc.setLocalDescription(answer))
          .then(() => {
            if (!pc.localDescription) {
              throw new Error("Callee failed to create an answer");
            }
            const answer = {
              type: pc.localDescription.type,
              sdp: pc.localDescription.sdp,
            };
            signaler.sendAnswer(answer);
          })
          .catch((reason: unknown) => {
            console.error("Failed to setup connection");
            console.error(reason);
          });

        signaler.watchCandidate(
          (candidate) => void pc.addIceCandidate(candidate)
        );
      });

      break;

    case "caller":
      pc.onnegotiationneeded = async () => {
        console.log("caller: negotiation needed");
        signaler.watchCandidate(
          (candidate) => void pc.addIceCandidate(candidate)
        );
        signaler.watchAnswer((answer) => void pc.setRemoteDescription(answer));

        const offer = await pc.createOffer();
        await pc.setLocalDescription(offer);
        signaler.sendOffer(offer);
      };
      break;
  }

  return pc;
}

function createFirebaseIceHandler(
  docRef: DocumentReference<Viewer>,
  field: "callerCandidates" | "calleeCandidates"
): IceHandler {
  const candidates: RTCIceCandidateInit[] = [];
  const sendCandidate = (candidate: RTCIceCandidate) => {
    candidates.push(candidate.toJSON());
    if (candidate.candidate === "") {
      void updateDoc(docRef, { [field]: candidates }).then(() => {
        candidates.length = 0;
      });
    }
  };

  const watchCandidate = (handler: (candidate: RTCIceCandidate) => void) => {
    const unsubscribe = onSnapshot(docRef, (snapshot) => {
      const viewer = snapshot.data();

      const remoteField: typeof field =
        field === "calleeCandidates" ? "callerCandidates" : "calleeCandidates";
      const remoteCandidates = viewer?.[remoteField];
      if (remoteCandidates) {
        remoteCandidates.forEach((candidate) =>
          handler(new RTCIceCandidate(candidate))
        );
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

        // TODO: Figure out a better way to remove viewer info...
        setTimeout(() => void deleteDoc(docRef), 1000);
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
      if (viewer?.offer) {
        handler(viewer.offer);
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

export {
  createPeerConnecton,
  createFirebaseCaller,
  createFirebaseCallee,
  type Viewer,
};
