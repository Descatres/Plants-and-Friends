import Plant from "./models/Plant";
import User from "./models/User";
import connectDB from "./config/db";

const dotenv = require("dotenv");

async function seedDatabase() {
	const usersExist = await User.exists({});
	const plantsExist = await Plant.exists({});

	if (usersExist && plantsExist) {
		console.log("Users and plants already exist, skipping seeding.");
		process.exit(0);
	}

	const user = await createUser();
	await createPlants(user.id);
	console.log("Database seeded successfully!");
	process.exit(0);
}

async function createPlants(userId: any) {
	const plantsData = [
		{ name: "Aloe Vera", species: "Aloe", description: "A succulent plant species.", minTemperature: 22, maxTemperature: 30, minHumidity: 40, maxHumidity: 60, image: "aloe.jpg" },
		{ name: "Basil", species: "Ocimum basilicum", description: "A culinary herb.", minTemperature: 20, maxTemperature: 50, minHumidity: 50, maxHumidity: 90, image: "basil.jpg" },
		{ name: "Cactus", species: "Cactaceae", description: "A desert plant.", minTemperature: 18, maxTemperature: 40, minHumidity: 10, maxHumidity: 30, image: "cactus.jpg" },
		{ name: "Fern", species: "Polypodiopsida", description: "A shade-loving plant.", minTemperature: 15, maxTemperature: 25, minHumidity: 60, maxHumidity: 80, image: "fern.jpg" },
		{ name: "Lavender", species: "Lavandula", description: "A fragrant herb.", minTemperature: 10, maxTemperature: 35, minHumidity: 30, maxHumidity: 50, image: "lavender.jpg" },
		{ name: "Mint", species: "Mentha", description: "A refreshing herb.", minTemperature: 10, maxTemperature: 30, minHumidity: 50, maxHumidity: 70, image: "mint.jpg" },
		{ name: "Rosemary", species: "Salvia rosmarinus", description: "A Mediterranean herb.", minTemperature: 15, maxTemperature: 35, minHumidity: 40, maxHumidity: 60, image: "rosemary.jpg" },
		{ name: "Spider Plant", species: "Chlorophytum comosum", description: "An indoor air-purifying plant.", minTemperature: 18, maxTemperature: 28, minHumidity: 50, maxHumidity: 70, image: "spider_plant.jpg" },
		{ name: "Snake Plant", species: "Sansevieria", description: "A hardy indoor plant.", minTemperature: 10, maxTemperature: 30, minHumidity: 30, maxHumidity: 50, image: "snake_plant.jpg" },
		{ name: "Peace Lily", species: "Spathiphyllum", description: "A popular indoor flowering plant.", minTemperature: 15, maxTemperature: 25, minHumidity: 50, maxHumidity: 80, image: "peace_lily.jpg" },
	];

	const existingPlants = await Plant.find({});
	if (existingPlants.length > 0) {
		console.log("Plants already exist, skipping creation.");
		return;
	}

	const plants = [];
	for (let i = 0; i < 30; i++) {
		const plant = plantsData[i % plantsData.length];
		plants.push({
			name: `${plant.name} ${i + 1}`,
			species: plant.species,
			description: plant.description,
			minTemperature: plant.minTemperature,
			maxTemperature: plant.maxTemperature,
			minHumidity: plant.minHumidity,
			maxHumidity: plant.maxHumidity,
			image: plant.image,
			lastUpdate: new Date().toISOString(),
			ownerId: userId,
		});
	}

	await Plant.insertMany(plants);
	console.log("30 Plants created");
}

async function createUser() {
	const existingUsers = await User.find({});
	if (existingUsers.length > 0) {
		console.log("Users already exist, skipping creation.");
		return existingUsers[0];
	}

	const user = new User({
		email: "user@example.com",
		password: "securepassword",
	});

	await user.save();
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
