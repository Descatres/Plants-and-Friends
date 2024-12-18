import { Response } from "express";
import Notification from "../models/Notification";
import { CustomRequest } from "../types/CustomRequest";
import Plant from "../models/Plant";
import { latestSensorData } from "./sensorController";
import { generateNotifications } from "../services/notificationService";

async function getNotifications(req: CustomRequest, res: Response) {
	const userId = req.user?.id;
	const page = parseInt(req.query.page as string) || 1;
	const limit = parseInt(req.query.limit as string) || 10;

	try {
		const notifications = await Notification.find({ userId })
			.skip((page - 1) * limit)
			.limit(limit);

		const total = await Notification.countDocuments({ userId });

		res.json({
			notifications,
			currentPage: page,
			totalPages: Math.ceil(total / limit),
		});
	} catch (err) {
		res.status(500).json({ message: "Error loading notifications" });
	}
}

async function createNotifications(_req: Request, res: Response) {
	console.log("[Notification Poll] Checking sensor data...");
	const { temperature, humidity } = latestSensorData;

	if (!temperature || !humidity) {
		console.log("[Notification Poll] No valid sensor data.");
		return res.status(204).json({ message: "No valid sensor data available for notifications." });
	}

	const plants = await Plant.find({});
	console.log(`[Notification Poll] Found ${plants.length} plants.`);

	const notifications = [];

	for (const plant of plants) {
		let notificationMessage = "";

		if ((plant.minTemperature && temperature < plant.minTemperature) || (plant.maxTemperature && temperature > plant.maxTemperature)) {
			notificationMessage = `Temperature out of range for plant '${plant.name}' (Current: ${temperature}°C). `;
			const notification = await generateNotifications(notificationMessage, plant);

			if (notification) {
				notifications.push(notification);
			}
		}

		if ((plant.minHumidity && humidity < plant.minHumidity) || (plant.maxHumidity && humidity > plant.maxHumidity)) {
			notificationMessage = `Humidity out of range for plant '${plant.name}' (Current: ${humidity}%). `;
			const notification = await generateNotifications(notificationMessage, plant);
			
            if (notification) {
				notifications.push(notification);
			}
		}
	}

	res.json({ notifications });
}

async function deleteAllNotifications(req: CustomRequest, res: Response) {
	const userId = req.user?.id;

	try {
		await Notification.deleteMany({ userId });
		res.status(200).json({ message: "All notifications deleted successfully" });
	} catch (err) {
		res.status(500).json({ message: "Error deleting notifications" });
	}
}

export { getNotifications, createNotifications, deleteAllNotifications };
