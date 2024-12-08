import mongoose from 'mongoose';

const connectDB = async () => {
  try {
		const dbURI = process.env.MONGO_URI || "mongodb://localhost:27017/local";
		await mongoose.connect(dbURI);

		console.log("MongoDB connected successfully");
  } catch (error) {
    console.error('MongoDB connection failed:', error);
    process.exit(1);
  }
};

export default connectDB;