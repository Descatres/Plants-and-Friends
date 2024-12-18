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
	const { page = 1, limit = 10 } = req.query;
	const userId = req.user?.id;

	const result = await getPlantById(id, userId!, Number(page), Number(limit));
	res.json(result);
}

async function postPlant(req: CustomRequest, res: Response) {
	const userId = req.user?.id;
	const savedPlant = await createPlant(req.body, userId!);
	res.status(201).json(savedPlant);
}

export { getPlants, getPlant, postPlant };
