import jwt from "jsonwebtoken";
import bcrypt from "bcryptjs";
import User from "../models/User";
import { CustomError } from "../utils/CustomError";

const JWT_SECRET = process.env.JWT_SECRET || "your_jwt_secret";

async function loginUser(email: string, password: string): Promise<string> {
	const user = await User.findOne({ email });
	if (!user) {
		throw new CustomError("Invalid email or password", 401);
	}

	const isMatch = user.comparePassword(password);
	if (!isMatch) {
		throw new CustomError("Invalid email or password", 401);
	}

	return jwt.sign({ id: user._id }, JWT_SECRET, { expiresIn: "1h" });
}

export { loginUser }