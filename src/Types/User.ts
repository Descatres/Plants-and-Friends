import { Plant } from "./Plant";

export interface User {
  id: number;
  name: string;
  email: string;
  password: string;
  isLogged: boolean;
  plants: Plant[];
}
