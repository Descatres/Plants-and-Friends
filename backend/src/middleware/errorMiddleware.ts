import { Request, Response, NextFunction } from "express";
import { CustomError } from "../utils/CustomError";

export function errorHandler(err: Error | CustomError, req: Request, res: Response, next: NextFunction) {
	const status = err instanceof CustomError ? err.status : 500;
	const message = err.message || "Internal Server Error";

	console.error(`[${status}] ${message}`);
	res.status(status).json({ error: message });
}
