import { Request, Response, NextFunction } from "express";
import { CustomError } from "../utils/CustomError";

export function errorHandler(err: Error | CustomError, _req: Request, res: Response, _next: NextFunction) {
	const status = err instanceof CustomError ? err.status : 500;
	const message = err.message || "Internal Server Error";

	console.error(`[Error Handler] Status: ${status}, Message: ${message}`);
	res.status(status).json({ error: message });
}
