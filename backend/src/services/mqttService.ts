import { mqttClient, MQTT_TOPIC } from "../config/mqtt";

mqttClient.on("message", async (topic, message) => {
	if (topic === MQTT_TOPIC) {
		try {
			const payload = JSON.parse(message.toString());
			console.log("Received MQTT message:", payload);

			//TODO
		} catch (error) {
			console.error("Failed to process MQTT message:", error);
		}
	}
});
