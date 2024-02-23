import { useEffect, useRef, VideoHTMLAttributes } from "react";

interface VideoPlayerProps extends VideoHTMLAttributes<HTMLVideoElement> {
  srcObject: MediaStream | null;
}

export default function VideoPlayer({ srcObject, ...props }: VideoPlayerProps) {
  const ref = useRef<HTMLVideoElement>(null);

  useEffect(() => {
    if (ref.current) {
      ref.current.srcObject = srcObject;
    }
  }, [srcObject]);

  return <video ref={ref} {...props}></video>;
}
