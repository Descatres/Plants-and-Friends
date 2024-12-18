import Plant, { IPlant } from "../models/Plant";
import Notification from "../models/Notification";
import mongoose from "mongoose";

interface SensorData {
	temperature?: number;
	humidity?: number;
}

const NOTIFICATION_TIME_WINDOW = 60000;

async function generateNotifications(notificationMessage: string, plant: IPlant) {
	if (notificationMessage) {
		console.log(`[Notification Poll] ${notificationMessage}`);

		const existingNotification = await Notification.findOne({
			message: notificationMessage.trim(),
			plantId: plant.id,
			createdAt: { $gte: new Date(Date.now() - 60000) },
		});

		if (existingNotification) {
			console.log(`[Notification Poll] Duplicate notification detected for plant '${plant.name}'.`);
			return;
		}

		const notification = new Notification({
			message: notificationMessage.trim(),
			plantId: plant.id,
			userId: plant.ownerId,
			createdAt: new Date(),
		});
		await notification.save();

        return notification;
	}
}

export { generateNotifications };
