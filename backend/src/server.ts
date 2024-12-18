import plantRoutes from "./routes/plantRoutes";
import authRoutes from "./routes/authRoutes";
import connectDB from "./config/db";
import { errorHandler } from "./middleware/errorMiddleware";
import passport from "./config/passport";
import cookieParser from "cookie-parser";
import { mqttClient } from "./config/mqtt";
import sensorRoutes from "./routes/sensorRoutes";
import notificationRoutes from "./routes/notificationRoutes";

const express = require("express");
const cors = require("cors");
const bodyParser = require("body-parser");
const dotenv = require("dotenv");
const session = require("express-session");
const app = express();

function main() {
	dotenv.config();

	mqttClient.on("connect", () => {
		console.log("MQTT client is connected and listening for data.");
		app.use("/api/", sensorRoutes);
	});

	mqttClient.on("error", (err: { message: any; }) => {
		console.error("MQTT connection error:", err.message);
	});
	
	startServer();
}

function startServer() {
	app.use(cors());
	app.use(bodyParser.json());

	app.use(
		session({
			secret: process.env.SESSION_SECRET || "your_session_secret",
			resave: false,
			saveUninitialized: false,
			cookie: {
				secure: process.env.NODE_ENV === "production",
				httpOnly: true,
				maxAge: 24 * 60 * 60 * 1000,
				sameSite: "strict",
			},
		})
	);

	app.use(cookieParser());

	app.use(passport.initialize());
	app.use(passport.session());

	app.use("/", plantRoutes);
	app.use("/", authRoutes);
	app.use("/", notificationRoutes);

	app.use(errorHandler);

	connectDB();

	const PORT = process.env.PORT || 5001;
	app.listen(PORT, () => {
		console.log(`Server running on http://localhost:${PORT}`);
	});
}

main();
