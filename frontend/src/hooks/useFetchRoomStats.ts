import { useCallback, useState } from "react";
import { SENSOR_DATA_URL } from "../utils/routesAndEndpoints/routesAndEndpoints";
import { useApi } from "./useApi";

export function useFetchRoomStats() {
  const { api } = useApi();
  const [temperature, setTemperature] = useState<number | null>(null);
  const [humidity, setHumidity] = useState<number | null>(null);

  const getRoomSensorData = useCallback(() => {
    const eventSource = new EventSource(
      `${api.defaults.baseURL}${SENSOR_DATA_URL}`
    );

    eventSource.onmessage = (event) => {
      const data = JSON.parse(event.data);
      setTemperature(data.temperature);
      setHumidity(data.humidity);
    };

    eventSource.onerror = (error) => {
      console.error("Error with sensor:", error);
      eventSource.close();
    };
  }, []);

  return {
    temperature,
    humidity,
    getRoomSensorData,
  };
}

// import { useState, useCallback } from "react";
// import mqtt from "mqtt";

// export function useFetchRoomStats() {
//   const [temperature, setTemperature] = useState<number | null>(null);
//   const [humidity, setHumidity] = useState<number | null>(null);
//   const [isConnected, setIsConnected] = useState<boolean>(false);
//   const [error, setError] = useState<string | null>(null);

//   const getRoomSensorData = useCallback(() => {
//     const brokerUrl = "ws://broker.hivemq.com:8000/mqtt";
//     const client = mqtt.connect(brokerUrl);

//     client.on("connect", () => {
//       console.log("Connected to MQTT broker");
//       setIsConnected(true);

//       client.subscribe("plants_and_friends_temperature_topic", (err) => {
//         if (err) console.error("Failed to subscribe to temperature topic");
//       });

//       client.subscribe("plants_and_friends_humidity_topic", (err) => {
//         if (err) console.error("Failed to subscribe to humidity topic");
//       });
//     });

//     client.on("message", (topic, message) => {
//       const payload = message.toString();
//       if (topic === "plants_and_friends_temperature_topic") {
//         setTemperature(parseFloat(payload));
//       } else if (topic === "plants_and_friends_humidity_topic") {
//         setHumidity(parseFloat(payload));
//       }
//     });

//     client.on("error", (err) => {
//       console.error("MQTT error:", err);
//       setError("Error connecting to MQTT broker");
//     });

//     client.on("close", () => {
//       console.log("Disconnected from MQTT broker");
//       setIsConnected(false);
//     });

//     return () => {
//       client.end();
//     };
//   }, []);

//   return {
//     temperature,
//     humidity,
//     isConnected,
//     error,
//     getRoomSensorData,
//   };
// }
