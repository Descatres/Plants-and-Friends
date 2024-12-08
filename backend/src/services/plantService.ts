import Plant from "../models/Plant";
import { CustomError } from "../utils/CustomError"; 


async function getAllPlants(userId: string) {
	const plants = await Plant.find({ ownerId: userId });

	if (!plants || plants.length === 0) {
		throw new CustomError("No plants found for the user", 404);
	}

	return plants;
}


async function getPlantById(id: string, userId: string) {
	const plant = await Plant.findById(id);

	if (!plant) {
		throw new CustomError(`Plant with ID ${id} not found`, 404);
	}

	if (plant.ownerId.toString() !== userId) {
		throw new CustomError("Unauthorized access to this plant", 403);
	}

	return plant;
}


async function createPlant(plantData: any, userId: string) {
	const plant = new Plant({
		...plantData,
		ownerId: userId,
	});

	try {
		return await plant.save();
	} catch (error) {
		throw new CustomError("Error saving the plant", 500);
	}
}

export { getAllPlants, getPlantById, createPlant }
