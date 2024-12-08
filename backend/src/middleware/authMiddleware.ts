import { Response, NextFunction } from "express";
import jwt from "jsonwebtoken";
import { CustomError } from "../utils/CustomError"; 
import { CustomRequest } from "../types/CustomRequest";

const JWT_SECRET = process.env.JWT_SECRET || "your_jwt_secret";

export function authenticate(req: CustomRequest, _res: Response, next: NextFunction) {
	const token = req.header("Authorization")?.replace("Bearer ", "");

	if (!token) {
		return next(new CustomError("Authorization denied, no token provided", 401));
	}

	try {
		const decoded = jwt.verify(token, JWT_SECRET) as { id: string };
		req.user = { id: decoded.id };
		next();
	} catch (error) {
		return next(new CustomError("Token is invalid", 401));
	}
}
