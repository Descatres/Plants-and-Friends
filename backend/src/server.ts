import express, { Request, Response, NextFunction } from "express";
import plantRoutes from "./routes/plantRoutes";
import authRoutes from "./routes/authRoutes";
import connectDB from "./config/db";
import { errorHandler } from "./middleware/errorMiddleware";


const cors = require("cors");
const bodyParser = require("body-parser");
const dotenv = require("dotenv");
const app = express();

function main() {
	dotenv.config();

	app.use(cors());
	app.use(bodyParser.json());

	app.use("/", plantRoutes);
  app.use("/", authRoutes);

  app.use(errorHandler);

	connectDB();

	const PORT = process.env.PORT || 5001;
	app.listen(PORT, () => {
		console.log(`Server running on http://localhost:${PORT}`);
	});
}

main();
