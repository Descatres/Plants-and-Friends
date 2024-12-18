import mongoose, { Schema, Document } from "mongoose";

export interface INotification extends Document {
	message: string;
	plantId: string;
	userId: string;
	createdAt: Date;
}

const NotificationSchema: Schema = new Schema({
	message: { type: String, required: true },
	plantId: { type: Schema.Types.ObjectId, ref: "Plant", required: true },
	userId: { type: Schema.Types.ObjectId, ref: "User", required: true },
	createdAt: { type: Date, default: Date.now },
});

NotificationSchema.index({ userId: 1, createdAt: -1 });

export default mongoose.model<INotification>("Notification", NotificationSchema, "notifications");
