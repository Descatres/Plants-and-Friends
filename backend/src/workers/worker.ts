import { parentPort } from "worker_threads";
import mqtt from "mqtt";

const MQTT_BROKER = process.env.MQTT_BROKER || "broker.hivemq.com";
const MQTT_PORT = parseInt(process.env.MQTT_PORT || "1883");
const MQTT_TOPIC_TEMPERATURE = process.env.MQTT_TOPIC_TEMPERATURE || "plants_and_friends_temperature_topic";
const MQTT_TOPIC_HUMIDITY = process.env.MQTT_TOPIC_HUMIDITY || "plants_and_friends_humidity_topic";

const mqttClient = mqtt.connect(`mqtt://${MQTT_BROKER}:${MQTT_PORT}`);

let latestSensorData: { temperature?: number; humidity?: number } = {};

mqttClient.on("connect", () => {
	console.log(`Worker connected to MQTT broker at ${MQTT_BROKER}:${MQTT_PORT}`);
	mqttClient.subscribe([MQTT_TOPIC_TEMPERATURE, MQTT_TOPIC_HUMIDITY], (err) => {
		if (err) {
			console.error("Failed to subscribe to topics:", err.message);
		} else {
			console.log(`Subscribed to topics: ${MQTT_TOPIC_TEMPERATURE}, ${MQTT_TOPIC_HUMIDITY}`);
		}
	});
});

mqttClient.on("message", (topic, message) => {
	const payload = parseFloat(message.toString());

	if (topic === MQTT_TOPIC_TEMPERATURE) {
		latestSensorData.temperature = payload;
	} else if (topic === MQTT_TOPIC_HUMIDITY) {
		latestSensorData.humidity = payload;
	}

	if (parentPort) {
		parentPort.postMessage(latestSensorData);
	}
});

mqttClient.on("error", (error) => {
	console.error("MQTT Error:", error.message);
});

setInterval(() => {
	if (parentPort) {
		parentPort.postMessage(latestSensorData);
	}
}, 30000);

export { mqttClient, MQTT_TOPIC_TEMPERATURE, MQTT_TOPIC_HUMIDITY };
