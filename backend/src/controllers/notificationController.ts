import { Response } from "express";
import Notification from "../models/Notification";
import { CustomRequest } from "../types/CustomRequest";

async function getNotifications(req: CustomRequest, res: Response) {
	const userId = req.user?.id;
	const page = parseInt(req.query.page as string) || 1;
	const limit = parseInt(req.query.limit as string) || 10;

	try {
		const notifications = await Notification.find({ userId })
			.skip((page - 1) * limit)
			.limit(limit);

		const total = await Notification.countDocuments({ userId });

		res.json({
			notifications,
			currentPage: page,
			totalPages: Math.ceil(total / limit),
		});
	} catch (err) {
		res.status(500).json({ message: "Error loading notifications" });
	}
}

export { getNotifications };
