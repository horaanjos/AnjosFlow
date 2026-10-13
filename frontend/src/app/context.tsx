import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import { useApi } from "../hooks/useApi";
import type { Subject, Task } from "../types/api";
interface Preferences {
  displayName: string;
  focusMinutes: number;
  shortMinutes: number;
  longMinutes: number;
}
const defaults: Preferences = {
  displayName: "",
  focusMinutes: 25,
  shortMinutes: 5,
  longMinutes: 15,
};
export function readPreferences(): Preferences {
  try {
    const value = JSON.parse(
      localStorage.getItem("anjosflow-preferences") || "{}",
    ) as Partial<Preferences>;
    return {
      displayName:
        typeof value.displayName === "string"
          ? value.displayName
          : defaults.displayName,
      focusMinutes:
        Number.isInteger(value.focusMinutes) &&
        value.focusMinutes! > 0 &&
        value.focusMinutes! <= 180
          ? value.focusMinutes!
          : 25,
      shortMinutes:
        Number.isInteger(value.shortMinutes) &&
        value.shortMinutes! > 0 &&
        value.shortMinutes! <= 60
          ? value.shortMinutes!
          : 5,
      longMinutes:
        Number.isInteger(value.longMinutes) &&
        value.longMinutes! > 0 &&
        value.longMinutes! <= 60
          ? value.longMinutes!
          : 15,
    };
  } catch {
    return defaults;
  }
}
interface AppContext {
  subjects: Subject[];
  tasks: Task[];
  catalogError: string;
  refreshCatalog: () => void;
  preferences: Preferences;
  setPreferences: (value: Preferences) => void;
}
const Context = createContext<AppContext | null>(null);
export function AppProvider({ children }: { children: ReactNode }) {
  const subjects = useApi<Subject[]>("/subjects");
  const tasks = useApi<Task[]>("/tasks");
  const [preferences, setPreferences] = useState(readPreferences);
  useEffect(() => {
    localStorage.setItem("anjosflow-preferences", JSON.stringify(preferences));
  }, [preferences]);
  return (
    <Context.Provider
      value={{
        subjects: subjects.data || [],
        tasks: tasks.data || [],
        catalogError: subjects.error || tasks.error,
        refreshCatalog: () => {
          subjects.reload();
          tasks.reload();
        },
        preferences,
        setPreferences,
      }}
    >
      {children}
    </Context.Provider>
  );
}
export function useApp() {
  const context = useContext(Context);
  if (!context) throw new Error("AppProvider ausente");
  return context;
}
