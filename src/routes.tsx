import { doc, getDoc } from "firebase/firestore";
import { createBrowserRouter, Navigate } from "react-router-dom";

import { db } from "./firebase";
import HomePage from "./pages/HomePage.tsx";
import RoomPage from "./pages/RoomPage";

const notFoundResponse = new Response("Not Found", { status: 404 });

export const router = createBrowserRouter([
  {
    path: "/",
    element: <HomePage />,
    errorElement: <Navigate to="/" />,
  },
  {
    path: "rooms/:roomId",
    element: <RoomPage />,
    errorElement: <Navigate to="/" />,
    loader: async ({ params }) => {
      const roomId = params.roomId;
      if (!roomId) {
        throw notFoundResponse;
      }

      const roomRef = await getDoc(doc(db, "rooms", roomId));

      if (!roomRef.exists()) {
        throw notFoundResponse;
      }

      return roomRef;
    },
  },
]);
