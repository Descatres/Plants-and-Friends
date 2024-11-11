export interface Plant {
  id: string;
  name: string;
  species?: string;
  description?: string;
  temperature?: number;
  humidity?: number;
  image: string;
  lastUpdate: string; // dd/mm/yy, hh:mm
}
