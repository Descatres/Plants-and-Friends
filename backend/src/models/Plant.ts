import mongoose, { Schema, Document } from 'mongoose';

export interface IPlant extends Document {
	name: string;
	species?: string;
	description?: string;
	minTemperature?: number;
	maxTemperature?: number;
	minHumidity?: number;
	maxHumidity?: number;
	image: string;
	lastUpdate: string; // ISO string for consistency
  	ownerId: string;
}

const PlantSchema: Schema = new Schema({
	name: { type: String, required: true },
	species: { type: String },
	description: { type: String },
	minTemperature: { type: Number },
	maxTemperature: { type: Number },
	minHumidity: { type: Number },
	maxHumidity: { type: Number },
	image: { type: String, required: true },
	lastUpdate: { type: String, required: true }, // Store date as ISO string
	ownerId: { type: Schema.Types.ObjectId, ref: "User" }, // Reference to User
});

PlantSchema.index({ ownerId: 1 });

export default mongoose.model<IPlant>('Plant', PlantSchema, 'plants');