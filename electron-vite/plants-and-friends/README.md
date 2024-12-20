# Plants&Friends

- Small PoC to manage plants.

- [Repository](https://github.com/Descatres/Plants-and-Friends)

- [Website](http://web-dev-grupo07.dei.uc.pt)

- [Arduino Script](https://wokwi.com/projects/385292642174231553) (can be found [here](./SensorScript/sensor.ino) as well)

# Electron App for Plants and Friends

# How to run Locally

Go to

```bash
cd electron-vite/plants-and-friends
```

Run

```bash
npm install
npm run start
```

- Make sure the backend is running on port 5001 (check [below](#backend-for-plants-and-friends) for more information)

---

# Frontend for Plants and Friends

# How to run Locally

- Create a .env file on the root folder of the fronted with the following:
- VITE_API_URL=http://localhost:5001

`npm i`
`npm run dev`

## Features and notes

- Logged out:
  - Beautiful Landing page featuring Glassmorphism, a 3d model of a tree (which persists across pages while logged off)
  - Login
  - Register
- Logged in

  - Create Plant
  - Edit Plant
  - Delete Plant
  - Check real time sensor data
  - Notifications (broken atm)
  - Search by plant name and species
  - Sort by either name or species (alphabetically, or reverse)
  - The history state is automatically handled by react-router and react-router-dom.
  - Lazy loading is being applied to fetch the plants.

  ***

- A page to control specific sensors for rooms was thought of but it was not implemented.
- Some UX improvements could've been made, such as making some buttons disabled (for example, to update the plant, the user must first update a field, only then the button should be enabled); modals to confirm destructive non reversible actions, such as delting a plant; and so on. We know this would be the best approach but, due to lack of time and not using a UI library, like Chakra UI (which is lovely in our opinion), we couldn't implement it in time. But we do know it is something we can improve the app on!

- The images are not being set because, once again, we did not have time.

- The notifications got broken at some point and we couldn't fix it in time. Nonetheless, only the 10 most recent notifications would be shown, as this is supposed to be a web app to control the plants and check some notifications. For more in depth usage, the Android App we have is a much better option. This PoC is just for basic plant management with some integrations to get sensor data.

- The same species plants are not being shown on the plant details page but it can be filtered by the user in the homepage with the sort by and search features! This would have lazy loading as well if we were to implement it.

- Lastly, the MQTT integration is working, which is a ver nice feature. It only works for one sensor (this is supposed to be a PoC), but it can be upscaled to multiple by giving ids to the sensor in the script and associate each id to the plant or room the user wants.

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
