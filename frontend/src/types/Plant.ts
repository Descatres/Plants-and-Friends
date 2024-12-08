export interface Plant {
  id: string;
  name: string;
  species?: string;
  description?: string;
  minTemperature?: number;
  maxTemperature?: number;
  minHumidity?: number;
  maxHumidity?: number;
  image: string;
  lastUpdate: string; // dd/mm/yy, hh:mm
  ownerId: string;
}
