import mongoose from "mongoose";
import Plant from "../models/Plant";
import { CustomError } from "../utils/CustomError";

async function getAllPlants(userId: string, page: number, limit: number) {
	const skip = (page - 1) * limit;
	const plants = await Plant.find({ ownerId: userId }).skip(skip).limit(limit);

	if (!plants || plants.length === 0) {
		throw new CustomError("No plants found for the user", 404);
	}

	return plants;
}

async function getPlantById(id: string, userId: string, page: number, limit: number) {
	const plant = await Plant.findById(id);

	if (!plant) {
		throw new CustomError(`Plant with ID ${id} not found`, 404);
	}

	if (plant.ownerId.toString() !== userId) {
		throw new CustomError("Unauthorized access to this plant", 403);
	}

	const skip = (page - 1) * limit;

	const aggregation = [
		{
			$match: {
				ownerId: new mongoose.Types.ObjectId(userId),
				species: plant.species,
				_id: { $ne: new mongoose.Types.ObjectId(id) },
			},
		},
		{
			$skip: skip,
		},
		{
			$limit: limit,
		},
		{
			$project: {
				_id: 1,
				name: 1,
				species: 1,
				minTemperature: 1,
				maxTemperature: 1,
				minHumidity: 1,
				maxHumidity: 1,
				createdAt: 1,
			},
		},
	];

	const relatedPlants = await Plant.aggregate(aggregation);

	const totalRelated = await Plant.countDocuments({
		ownerId: userId,
		species: plant.species,
		_id: { $ne: id },
	});

	return {
		plant,
		relatedPlants,
		currentPage: page,
		totalPages: Math.ceil(totalRelated / limit),
	};
}

async function createPlant(plantData: any, userId: string) {
	const plant = new Plant({
		...plantData,
		ownerId: userId,
	});

	return await plant.save();
}

async function updatePlant(id: string, userId: string, updateData: Partial<any>) {
	const plant = await Plant.findById(id);

	if (!plant) {
		throw new CustomError(`Plant with ID ${id} not found`, 404);
	}

	if (plant.ownerId.toString() !== userId) {
		throw new CustomError("Unauthorized access to update this plant", 403);
	}

	Object.assign(plant, updateData);
	plant.lastUpdate = new Date().toISOString();

	return await plant.save();
}

export { getAllPlants, getPlantById, createPlant, updatePlant };
