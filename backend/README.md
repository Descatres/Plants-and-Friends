# Backend for Plants and Friends

This is the backend of the Plants and Friends project, designed to manage plant-related data, authenticate users, and interact with MQTT devices. It features routes for managing plants, user authentication, notifications, and sensor data. The backend connects to a MongoDB database and integrates with MQTT to receive real-time sensor data.

## Features

- **Authentication**: Handles user registration, login via Google, and session management.
- **Plant Management**: API for creating, retrieving, updating, and deleting plant data.
- **Sensor Data**: API to receive data from sensors via MQTT.
- **Notifications**: System for managing notifications related to plants and sensor events.

## Technology Stack

- **Node.js** with **Express.js**
- **TypeScript**
- **MongoDB** for data storage
- **MQTT** for sensor data integration
- **Passport** for authentication (supports Google login)
- **dotenv** for environment variables
- **Cors** for cross-origin resource sharing
- **Body-parser** for parsing incoming request bodies
- **Cookie-parser** for handling cookies
- **Express-session** for session management

## Setup Instructions

### 1. Clone the Repository

Clone the repository to your local machine:

```bash
git clone https://github.com/Descatres/Plants-and-Friends
cd backend
```

### 2. Install Dependencies

Install all required dependencies using npm or yarn:

```bash
npm install
```

### 3. Set Up Environment Variables

Create a `.env` file in the root of the project with the following environment variables:

```env
MONGO_URI=mongodb://localhost:27017/your-database-name
JWT_SECRET=your_jwt_secret
SESSION_SECRET=your_session_secret
MQTT_BROKER=broker.hivemq.com
MQTT_PORT=1883
MQTT_TOPIC_TEMPERATURE=plants_and_friends_temperature_topic
MQTT_TOPIC_HUMIDITY=plants_and_friends_humidity_topic
PORT=5001
```

### 4. Start the Development Server

To run the server in development mode, use the following command:

```bash
npm run dev
```

This command will seed the database (if configured) and start the server using `nodemon` for automatic restarts.

### 5. Build the Project (Optional)

To build the TypeScript code into JavaScript, run:

```bash
npm run build
```

After building, you can start the server with:

```bash
npm run start
```

### 6. Seed Database (Optional)

If you'd like to seed the database with initial data, run:

```bash
npm run seed
```

### 7. API Endpoints

- **Authentication**
  - `POST /auth`: Login
  - `POST /auth/register`: Register a new user
  - `POST /auth/logout`: Logout the user
  - `GET /auth/google`: Google login
  - `POST /refresh`: Refresh the authentication token

- **Plants**
  - `GET /`: Get all plants
  - `GET /plant/:id`: Get a single plant by ID
  - `POST /`: Add a new plant
  - `PATCH /plant/:id`: Update plant data

- **Sensor Data**
  - `GET /sensor-data`: Receive sensor data (temperature, humidity)

- **Notifications**
  - `GET /notifications`: Get all notifications
  - `POST /notifications`: Create a new notification
  - `DELETE /notifications`: Delete all notifications

## Deployment

For deployment, you can configure the server on a remote environment with MongoDB and MQTT connectivity. Make sure to update the `.env` variables with appropriate production values.