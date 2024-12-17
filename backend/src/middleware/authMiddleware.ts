import { NextFunction, Response } from "express";
import jwt from "jsonwebtoken";
import { CustomError } from "../utils/CustomError"; 
import { CustomRequest } from "../types/CustomRequest";

const JWT_SECRET = process.env.JWT_SECRET || "your_jwt_secret";
const JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET || "your_jwt_refresh_secret";

function authenticate(req: CustomRequest, _res: Response, next: NextFunction) {
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

function refreshToken(req: CustomRequest, res: Response, next: NextFunction) {
	const refreshToken = req.cookies.refreshToken;

	if (!refreshToken) {
		return next(new CustomError("No refresh token provided", 401));
	}

	try {
		const decoded = jwt.verify(refreshToken, JWT_REFRESH_SECRET) as { id: string };

		const accessToken = jwt.sign({ id: decoded.id }, JWT_SECRET, { expiresIn: "1h" });

		res.json({ accessToken, message: "Token refreshed successfully" });
		next();
	} catch (error) {
		return next(new CustomError("Invalid refresh token", 401));
	}
}

export { authenticate, refreshToken }