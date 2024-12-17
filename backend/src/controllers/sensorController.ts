import { Request, Response } from "express";
import { mqttClient, MQTT_TOPIC_TEMPERATURE, MQTT_TOPIC_HUMIDITY } from "../config/mqtt";

let latestSensorData: { temperature?: string; humidity?: string } = {};

mqttClient.on("message", (topic, message) => {
	const payload = message.toString();

	if (topic === MQTT_TOPIC_TEMPERATURE) {
		latestSensorData.temperature = payload;
	} else if (topic === MQTT_TOPIC_HUMIDITY) {
		latestSensorData.humidity = payload;
	} else {
        console.log(`[MQTT Error]: ${payload}`);
    }
});

function sendSensorData(req: Request, res: Response) {
	res.setHeader("Content-Type", "text/event-stream");
	res.setHeader("Cache-Control", "no-cache");
	res.setHeader("Connection", "keep-alive");

	const interval = setInterval(() => {
		res.write(`data: ${JSON.stringify(latestSensorData)}\n\n`);
	}, 30000);

	req.on("close", () => {
		clearInterval(interval);
		console.log("Client disconnected from SSE.");
	});
}

export { sendSensorData };
