import { NextFunction, Response } from "express";
import { mqttClient, MQTT_TOPIC_TEMPERATURE, MQTT_TOPIC_HUMIDITY } from "../config/mqtt";
import { CustomRequest } from "../types/CustomRequest";

let latestSensorData: { temperature?: number; humidity?: number } = {};

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

function sendSensorData(_req: CustomRequest, res: Response, _next: NextFunction) {
	if (latestSensorData.temperature === undefined || latestSensorData.humidity === undefined) {
		console.log("[Interval] No valid sensor data to send.");
		return res.status(500).json({ error: "No valid sensor data to send." });
	}

	res.json(latestSensorData);
}

export { sendSensorData, latestSensorData };
