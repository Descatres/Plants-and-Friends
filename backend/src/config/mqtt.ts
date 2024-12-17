import mqtt from "mqtt";

const MQTT_BROKER = process.env.MQTT_BROKER || "broker.hivemq.com";
const MQTT_PORT = parseInt(process.env.MQTT_PORT || "1883");
const MQTT_TOPIC_TEMPERATURE = process.env.MQTT_TOPIC_TEMPERATURE || "plants_and_friends_temperature_topic";
const MQTT_TOPIC_HUMIDITY = process.env.MQTT_TOPIC_HUMIDITY || "plants_and_friends_humidity_topic";

const mqttClient = mqtt.connect(`mqtt://${MQTT_BROKER}:${MQTT_PORT}`);

mqttClient.on("connect", () => {
	console.log(`Connected to MQTT broker at ${MQTT_BROKER}:${MQTT_PORT}`);

	mqttClient.subscribe([MQTT_TOPIC_TEMPERATURE, MQTT_TOPIC_HUMIDITY], (err) => {
		if (err) {
			console.error("Failed to subscribe to topics:", err.message);
		} else {
			console.log(`Subscribed to topics: ${MQTT_TOPIC_TEMPERATURE}, ${MQTT_TOPIC_HUMIDITY}`);
		}
	});
});

mqttClient.on("error", (error) => {
	console.error("MQTT Error:", error.message);
});

export { mqttClient, MQTT_TOPIC_TEMPERATURE, MQTT_TOPIC_HUMIDITY }