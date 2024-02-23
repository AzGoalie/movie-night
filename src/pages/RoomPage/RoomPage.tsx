import { DocumentSnapshot } from "firebase/firestore";
import { useLoaderData } from "react-router-dom";
import { useState } from "react";

import useAuth from "../../hooks/useAuth.ts";
import VideoPlayer from "./VideoPlayer.tsx";

interface Room {
  title: string;
  owner: string;
}

export default function RoomPage() {
  const roomRef = useLoaderData() as DocumentSnapshot;
  const { owner } = roomRef.data() as Room;
  const user = useAuth();

  const [isStreaming, setIsStreaming] = useState(false);
  const [stream, setStream] = useState<MediaStream | null>(null);

  const isBroadcaster = user.uid === owner;

  function handleStartStream() {
    navigator.mediaDevices.ondevicechange = () => console.log("device changed");
    navigator.mediaDevices.getDisplayMedia().then((captureStream) => {
      captureStream.getTracks().forEach((track) => {
        track.onended = () => {
          console.log("stream ended");
          setIsStreaming(false);
        };
      });
      setStream(captureStream);
    });
    setIsStreaming(true);
  }

  return (
    <section className="bg-white dark:bg-gray-900">
      {!isStreaming ? (
        <div className="py-8 px-4 mx-auto max-w-screen-xl text-center lg:py-16 lg:px-12">
          <h1 className="mb-4 text-4xl font-extrabold tracking-tight leading-none text-gray-900 md:text-5xl lg:text-6xl dark:text-white">
            The broadcast is not yet live.
          </h1>

          {isBroadcaster && (
            <div className="flex flex-col mb-8 lg:mb-16 space-y-4 sm:flex-row sm:justify-center sm:space-y-0 sm:space-x-4">
              <button
                onClick={handleStartStream}
                className="inline-flex justify-center items-center py-3 px-5 text-base font-medium text-center text-white rounded-lg bg-primary-700 hover:bg-primary-800 focus:ring-4 focus:ring-primary-300 dark:focus:ring-primary-900"
              >
                <svg
                  className="mr-2 -ml-1 w-5 h-5"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path d="M2 6a2 2 0 012-2h6a2 2 0 012 2v8a2 2 0 01-2 2H4a2 2 0 01-2-2V6zM14.553 7.106A1 1 0 0014 8v4a1 1 0 00.553.894l2 1A1 1 0 0018 13V7a1 1 0 00-1.447-.894l-2 1z"></path>
                </svg>
                Start Streaming!
              </button>
            </div>
          )}
        </div>
      ) : (
        <VideoPlayer
          srcObject={stream}
          playsInline
          autoPlay
          muted={isBroadcaster}
        />
      )}
    </section>
  );
}
