# EnglishTek Backend

Spring Boot backend service for EnglishTek application.

## Prerequisites

- Java JDK 21
- Gradle
- MySQL (XAMPP recommended for Windows)
- IDE (IntelliJ IDEA or Eclipse recommended)

## Database Setup

1. Install XAMPP:
   - Download XAMPP from [https://www.apachefriends.org/](https://www.apachefriends.org/)
   - Install with default settings
   - Start the XAMPP Control Panel

2. Start Required Services:
   - Start Apache service
   - Start MySQL service

3. Create Database:
   - Open browser and go to [http://localhost/phpmyadmin](http://localhost/phpmyadmin)
   - Click "New" on the left sidebar
   - Enter database name: `englishtek`
   - Click "Create"

## Environment Configuration

1. Find Your IP Address:
   - Press `Win + R`
   - Type `cmd` and press Enter
   - Run command: `ipconfig`
   - Look for `IPv4 Address` under your active network adapter
   - Note down the IP (format: `192.168.x.x`)

2. Configure Application Properties:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/englishtek
   spring.datasource.username=root
   spring.datasource.password=
   ```
   Note: These are default XAMPP MySQL credentials. Adjust if you've changed them.

## Running the Backend

1. Open terminal in project root directory
2. Run the application:
   ```bash
   ./gradlew.bat bootRun
   ```
3. Server will start on port 8080

## Frontend Integration

Update the `.env` files in both frontend and admin panel projects:

1. Mobile App (.env):
   ```
   API_URL=http://YOUR_IP_ADDRESS:8080
   ```

2. Admin Panel (.env):
   ```
   API_URL=http://YOUR_IP_ADDRESS:8080
   ```

Replace `YOUR_IP_ADDRESS` with the IP address you found earlier.

## Verification

1. Check if server is running:
   - Open browser
   - Visit `http://YOUR_IP_ADDRESS:8080/swagger-ui/index.html`
   - You should see the Spring Boot welcome page or your API documentation

## Troubleshooting

- If MySQL won't connect:
  - Verify XAMPP MySQL service is running
  - Check if database name matches in application.properties
  - Verify MySQL credentials
  - Ensure port 3306 is not in use by another service

- If server won't start:
  - Check if port 8080 is available
  - Verify all dependencies are downloaded
  - Check application logs for specific errors
  - Ensure you have Java 21 installed and properly configured in your system PATH

## Additional Notes

- Default MySQL port is 3306
- Default MySQL credentials in XAMPP:
  - Username: root
  - Password: (empty)
- The backend must be running for both frontend and admin panel to function properly
- Make sure your Java version is 21 by running:
  ```bash
  java -version
  ```
