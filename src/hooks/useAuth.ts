import { useContext } from "react";

import { Authentication } from "../contexts/Authentication.tsx";

export default function useAuth() {
  const context = useContext(Authentication);
  if (!context) {
    throw new Error("useAuth must be used within a AuthProvider");
  }

  return context;
}
