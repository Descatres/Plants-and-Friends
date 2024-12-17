import express from "express";
import { sendSensorData } from "../controllers/sensorController";

const router = express.Router();

router.get("/sensor-data", sendSensorData);

export default router;
