import { Request, Response } from "express";
import Notification from "../models/Notification";
import Plant from "../models/Plant";
import { mqttClient, MQTT_TOPIC_TEMPERATURE, MQTT_TOPIC_HUMIDITY } from "../config/mqtt";
import { CustomRequest } from "../types/CustomRequest";

let latestSensorData: { temperature?: number; humidity?: number } = {};
const NOTIFICATION_TIME_WINDOW = 60000;
const SENSOR_TIME_WINDOW = 5000;

mqttClient.on("message", (topic, message) => {
	const payload = parseFloat(message.toString());

	if (topic === MQTT_TOPIC_TEMPERATURE) {
		latestSensorData.temperature = payload;
	} else if (topic === MQTT_TOPIC_HUMIDITY) {
		latestSensorData.humidity = payload;
	} else {
		console.log(`[MQTT Error]: Unexpected message - ${payload}`);
	}
});

async function checkAndCreateNotifications() {
	console.log(`[Sensor Check] Triggered at: ${new Date().toISOString()}`);
	const { temperature, humidity } = latestSensorData;

	console.log(`[Sensor Data] Temperature: ${temperature}, Humidity: ${humidity}`);

	if (!temperature || !humidity) {
		console.log("[Sensor Check] No valid sensor data received.");
		return;
	}

	const plants = await Plant.find({});
	console.log(`[Sensor Check] Fetched ${plants.length} plants from the database.`);

	for (const plant of plants) {
		let notificationMessage = "";

		if ((plant.minTemperature && temperature < plant.minTemperature) || (plant.maxTemperature && temperature > plant.maxTemperature)) {
			notificationMessage += `Temperature out of range for plant '${plant.name}' (Current: ${temperature}°C). `;
		}

		if ((plant.minHumidity && humidity < plant.minHumidity) || (plant.maxHumidity && humidity > plant.maxHumidity)) {
			notificationMessage += `Humidity out of range for plant '${plant.name}' (Current: ${humidity}%). `;
		}

		if (notificationMessage) {
			console.log(`[Notification] ${notificationMessage}`);

            const existingNotification = await Notification.findOne({
				message: notificationMessage.trim(),
				plantId: plant._id,
				createdAt: {
					$gte: new Date(Date.now() - NOTIFICATION_TIME_WINDOW),
				},
			});

			if (existingNotification) {
				console.log(`[Notification] Duplicate detected. Skipping notification for plant '${plant.name}'.`);
				continue;
			}

			const notification = new Notification({
				message: notificationMessage.trim(),
				plantId: plant._id,
				userId: plant.ownerId,
				createdAt: new Date(),
			});

			await notification.save();
			console.log(`[Notification] Saved for plant '${plant.name}' with ID: ${plant._id}`);
		} else {
			console.log(`[Notification] No issues for plant '${plant.name}'.`);
		}
	}
}

function sendSensorData(req: CustomRequest, res: Response) {
	res.setHeader("Content-Type", "text/event-stream");
	res.setHeader("Cache-Control", "no-cache");
	res.setHeader("Connection", "keep-alive");

	console.log("[SSE] Client connected to receive sensor data.");

	const sensorDataInterval = setInterval(() => {
		console.log("[Interval] Sending sensor data to client.");

		if (latestSensorData.temperature === undefined || latestSensorData.humidity === undefined) {
			console.log("[Interval] No valid sensor data to send.");
			return;
		}

		res.write(`data: ${JSON.stringify(latestSensorData)}\n\n`);
	}, SENSOR_TIME_WINDOW);

	const notificationInterval = setInterval(async () => {
		console.log("[Interval] Checking sensor data and generating notifications...");
		await checkAndCreateNotifications();
	}, NOTIFICATION_TIME_WINDOW);

	req.on("close", () => {
		clearInterval(sensorDataInterval);
		clearInterval(notificationInterval);
		console.log("[SSE] Client disconnected.");
	});
}

export { sendSensorData };
