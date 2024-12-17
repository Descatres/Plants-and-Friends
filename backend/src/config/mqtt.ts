import mqtt from "mqtt";

const MQTT_BROKER_URL = "mqtt://test.mosquitto.org";
const MQTT_TOPIC = "wokwi-sensor-data";

const mqttClient = mqtt.connect(MQTT_BROKER_URL);

mqttClient.on("connect", () => {
	console.log("Connected to MQTT broker");
	mqttClient.subscribe(MQTT_TOPIC, (err) => {
		if (err) {
			console.error("Failed to subscribe to topic:", err);
		} else {
			console.log(`Subscribed to topic: ${MQTT_TOPIC}`);
		}
	});
});

mqttClient.on("error", (err) => {
	console.error("MQTT connection error:", err);
});

export { mqttClient, MQTT_TOPIC };
