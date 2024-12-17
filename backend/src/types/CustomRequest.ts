import { Request } from "express";
import session from "express-session";

declare module "express-session" {
	interface SessionData {
		userId?: string;
	}
}

export interface CustomRequest extends Request {
	user?: { id: string };
	session: session.Session & Partial<session.SessionData>;
}
