# EnglishTek Admin Dashboard

A React-based admin dashboard for managing the EnglishTek learning platform. Built with React, TypeScript, Vite, and TailwindCSS.

## Prerequisites

- Node.js (v16 or higher)
- npm (v7 or higher)
- Access to the EnglishTek backend server

## Setup Instructions

### 1. Install Dependencies

```bash
npm install
```

### 2. Configure Environment Variables

1. Create a `.env` file in the root directory
2. Add the following configuration:

```env
VITE_API_URL=http://YOUR_BACKEND_IP:8080
```

#### Finding Your Backend Server IP (Windows)

1. Open Command Prompt (cmd) as administrator
2. Type `ipconfig` and press Enter
3. Look for "IPv4 Address" under your active network adapter:
   - If using Ethernet: Look under "Ethernet adapter"
   - If using WiFi: Look under "Wireless LAN adapter Wi-Fi"
4. The IP will be in format: `192.168.x.x` or `10.x.x.x`

Example:
```
Wireless LAN adapter Wi-Fi:
   IPv4 Address. . . . . . . . . . . : 192.168.1.100
```
Use this IP in your `.env` file:
```env
VITE_API_URL=http://192.168.1.100:8080
```

### 3. Running the Application

Development mode:
```bash
npm run dev
```
This will start the development server at `http://localhost:5173`

Build for production:
```bash
npm run build
```

Preview production build:
```bash
npm run preview
```

## Common Issues and Solutions

### Backend Connection Issues

1. Ensure your backend server is running
2. Verify the IP address is correct using `ipconfig`
3. Check if the port (8080) matches your backend configuration
4. Try pinging the backend server:
   ```bash
   ping YOUR_BACKEND_IP
   ```

### CORS Issues

If you see CORS errors in the console:
1. Verify your backend CORS configuration allows requests from `http://localhost:5173`
2. Check if the API URL in `.env` includes the correct protocol (http/https)

## Project Structure

```
src/
├── components/    # Reusable UI components
├── pages/         # Main page components
├── services/      # API service functions
├── utils/         # Utility functions
└── App.tsx        # Root component
```

## Available Scripts

- `npm run dev`: Start development server
- `npm run build`: Build for production
- `npm run preview`: Preview production build
- `npm run lint`: Run ESLint
- `npm run format`: Format code with Prettier

## Contributing

1. Ensure your code follows the project's coding standards
2. Test your changes thoroughly
3. Update documentation as needed

## Support

For support or questions, please contact the development team.