export interface Plant {
    id: string;
    name: string;
    species?: string;
    description?: string;
    temperature?: number;
    humidity?: number;
    lastUpdate: string; // dd/mm/yy, hh:mm
}
