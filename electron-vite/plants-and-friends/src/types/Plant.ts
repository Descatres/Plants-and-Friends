export interface Plant {
  _id?: string;
  name?: string;
  species?: string;
  description?: string;
  minTemperature?: number;
  maxTemperature?: number;
  minHumidity?: number;
  maxHumidity?: number;
  imageUrl?: string;
  lastUpdate: string; // dd/mm/yy, hh:mm
  ownerId?: string;
}
