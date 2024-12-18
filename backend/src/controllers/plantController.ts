import { Response } from "express";
import { getAllPlants, getPlantById, createPlant } from "../services/plantService";
import { CustomRequest } from "../types/CustomRequest";

async function getPlants(req: CustomRequest, res: Response) {
	const userId = req.user?.id;
	const { page = 1, limit = 10 } = req.query;
	const plants = await getAllPlants(userId!, Number(page), Number(limit));
	res.json(plants);
}

async function getPlant(req: CustomRequest, res: Response) {
	const { id } = req.params;
	const userId = req.user?.id;
	const plant = await getPlantById(id, userId!);
	res.json(plant);
}

async function postPlant(req: CustomRequest, res: Response) {
	const userId = req.user?.id;
	const savedPlant = await createPlant(req.body, userId!);
	res.status(201).json(savedPlant);
}

export { getPlants, getPlant, postPlant };
