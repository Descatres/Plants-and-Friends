import plantRoutes from "./routes/plantRoutes";
import authRoutes from "./routes/authRoutes";
import connectDB from "./config/db";
import { errorHandler } from "./middleware/errorMiddleware";
import passport from "./config/passport";
import cookieParser from "cookie-parser";
import { mqttClient } from "./config/mqtt";
import sensorRoutes from "./routes/sensorRoutes";
import notificationRoutes from "./routes/notificationRoutes";
import { Worker } from "worker_threads";

const express = require("express");
const cors = require("cors");
const bodyParser = require("body-parser");
const dotenv = require("dotenv");
const session = require("express-session");
const app = express();

function startWorker() {
	const worker = new Worker("./workers/worker.ts");

	worker.on("message", (message) => {
		console.log("Received from worker:", message);
	});

	worker.on("error", (error) => {
		console.error("Worker error:", error);
	});

	worker.on("exit", (code) => {
		if (code !== 0) {
			console.error(`Worker stopped with exit code ${code}`);
		} else {
			console.log("Worker finished successfully");
		}
	});
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
	app.use("/api/", sensorRoutes);

	app.use(errorHandler);

	connectDB();

	const PORT = process.env.PORT || 5001;
	app.listen(PORT, () => {
		console.log(`Server running on http://localhost:${PORT}`);
	});
}

function main() {
	dotenv.config();

	startWorker();
	startServer();
}

main();
