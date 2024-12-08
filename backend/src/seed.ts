import Plant from './models/Plant';
import User from './models/User';
import connectDB from "./config/db";

const bcrypt = require("bcryptjs");
const dotenv = require("dotenv");

async function seedDatabase() {
	await clearDatabase();
  const user = await createUser();
	await createPlants(user.id);
	console.log("Database seeded successfully!");
	process.exit(0); 
}

async function clearDatabase() {
	await Plant.deleteMany({});
	await User.deleteMany({});
	console.log("Database cleared");
}

async function createPlants(userId: any) {
	const plant1 = await Plant.create({
		name: "Aloe Vera",
		species: "Aloe",
		description: "A succulent plant species.",
		minTemperature: 22,
		maxTemperature: 30,
		minHumidity: 40,
		maxHumidity: 60,
		image: "aloe.jpg",
		lastUpdate: new Date().toISOString(),
    ownerId: userId
	});

	const plant2 = await Plant.create({
		name: "Basil",
		species: "Ocimum basilicum",
		description: "A culinary herb.",
		minTemperature: 20,
		maxTemperature: 50,
		minHumidity: 50,
		maxHumidity: 90,
		image: "basil.jpg",
		lastUpdate: new Date().toISOString(),
    ownerId: userId
	});

	console.log("Plants created");
}

async function createUser() {
  const hashedPassword = await bcrypt.hash("securepassword", 10);

	const user = await User.create({
		name: "Tiago Oliveira",
		email: "toliveir4@example.com",
		password: hashedPassword,
		isLogged: false,
	});

	console.log("User created with plants");
  return user;
}

async function main() {
	try {
		dotenv.config();
		connectDB();

		await seedDatabase();
	} catch (error) {
		console.error("Error during database seeding:", error);
		process.exit(1);
	}
}

main();