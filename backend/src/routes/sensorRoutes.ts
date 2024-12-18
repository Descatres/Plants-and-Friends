import { sendSensorData } from "../controllers/sensorController";

const express = require("express");

const router = express.Router();

router.get("/sensor-data", sendSensorData);

export default router;
