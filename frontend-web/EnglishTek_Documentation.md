# EnglishTek System Documentation

## 1. System Overview

EnglishTek is a comprehensive English language learning platform consisting of three main components:

1. **Backend API**: RESTful API service built with Node.js/Express
2. **Mobile Application**: Cross-platform mobile app built with React Native/Expo
3. **Web Administration and Learning Panel**: Web interface built with React.js

The system follows a client-server architecture where the backend API serves as the central data repository and business logic handler, while the mobile app and web panel provide user interfaces for different user contexts.

## 2. Core Functionalities

### List of Core Features

| Feature | Description | Justification | Status |
|---------|-------------|---------------|--------|
| **User Authentication** | Secure login, registration, and profile management | Essential for personalized learning experiences and data security | ✅ Completed |
| **Chapter-based Learning Path** | Structured learning journey with progressive chapters | Provides systematic, step-by-step learning approach | ✅ Completed |
| **Interactive Lessons** | Text-based lessons with rich content and pagination | Core educational content delivery mechanism | ✅ Completed |
| **Knowledge Assessment Quizzes** | Multiple-choice and identification quizzes | Essential for testing comprehension and retention | ✅ Completed |
| **Progress Tracking** | Monitoring of completed lessons, quizzes, and overall progress | Critical for learner motivation and adaptive learning | ✅ Completed |
| **Achievement Badges** | Rewards for completing milestones | Gamification element to increase engagement | ✅ Completed |
| **User Feedback System** | Chapter-specific ratings and feedback collection | Essential for platform improvement and user engagement | ✅ Completed |
| **Content Management System** | Admin tools for lessons, quizzes, and badge creation | Required for maintaining and expanding educational content | ✅ Completed |
| **User Management** | Admin tools for managing user accounts | Necessary for platform administration | ✅ Completed |
| **Feedback Management** | Admin tools for viewing and analyzing user feedback | Critical for continuous improvement | ✅ Completed |

### Feature Justifications

#### User Authentication
Authentication is fundamental to providing personalized learning experiences. It enables progress tracking, customized content, and secure access to user data. The system implements JWT-based authentication with secure password handling.

#### Chapter-based Learning Path
The structured chapter approach ensures a pedagogically sound progression from basic to advanced concepts. This is crucial for language learning where concepts build upon each other. Each chapter contains a mix of lessons and quizzes.

#### Interactive Lessons
Lessons are the primary content delivery method, providing educational material in an engaging, paginated format with markdown support. They form the foundation of the learning experience.

#### Knowledge Assessment Quizzes
Quizzes validate comprehension and reinforce learning through active recall. They offer immediate feedback and are essential for measuring progress. The system supports multiple choice and identification questions.

#### Progress Tracking
Progress tracking motivates learners by visualizing their advancement. It enables users to see completed tasks, pending activities, and overall progress percentage. This feature is essential for self-directed learning.

#### Achievement Badges
Badges gamify the learning experience, providing extrinsic motivation and recognition for achieving milestones. They are particularly effective for maintaining user engagement in educational applications.

#### User Feedback System
The feedback system captures user satisfaction and suggestions at chapter completion. This data is invaluable for iterative improvement of the platform and content.

#### Content Management System
The CMS enables administrators to create, update, and manage educational content without technical intervention. This is essential for the platform's scalability and content freshness.

#### User Management
User management tools allow administrators to view, edit, and manage user accounts, essential for platform administration and support functions.

#### Feedback Management
This feature allows administrators to review and analyze user feedback, critical for data-driven platform improvement and content refinement.

## 3. Deployment Documentation

### Hosting Platforms

| Component | Hosting Platform | Description |
|-----------|------------------|-------------|
| Backend API | DigitalOcean | Node.js/Express application deployed on a DigitalOcean Droplet providing reliable cloud hosting with scalability options |
| Mobile Application | Google Drive (APK) | Android application package (APK) distributed via Google Drive for easy access and distribution without app store requirements |
| Web Admin Panel | Cloudflare | React.js application with automated deployment from GitHub using Cloudflare Pages for global CDN and security benefits |
| Database | MySQL | Relational database hosted alongside the backend on DigitalOcean for structured data storage with robust querying capabilities |

### Access and Testing

#### Backend API
- **Production API**: https://api-englishtek.aetherrflare.org
- **Testing**: API documentation and testing via Swagger UI at https://api-englishtek.aetherrflare.org/api-docs

#### Mobile Application
- **Android**: Download APK from Google Drive shared link (requires direct link access)
- **Testing Build**: Install APK directly on Android device with Developer options enabled

#### Web Admin Panel
- **Production URL**: https://englishtek.pages.dev
- **User Access**: Regular users can access learning features
- **Admin Access**: Administrators can access content and user management features

### Deployment Process

1. **Backend API**:
   - Code updates pushed to main branch
   - GitHub Actions workflow builds and tests the application
   - Successful builds deployed to DigitalOcean Droplet via SSH

2. **Mobile Application**:
   - Code updates pushed to main branch
   - Expo builds created for Android (.apk)
   - APK manually uploaded to Google Drive and shared with appropriate access permissions

3. **Web Admin Panel**:
   - Code updates pushed to main branch
   - Cloudflare automatically builds and deploys the application
   - Deployment previews available for pull requests

### Known Issues

1. **API Rate Limiting**: The backend API implements rate limiting that may affect performance during high traffic periods.

2. **Mobile Offline Mode**: Limited offline functionality is currently available. Internet connection is required for most features.

3. **Image Upload Size**: Image uploads (for avatars, badges) are limited to 2MB. Larger files will be rejected.

4. **Quiz Submit Timing**: Occasionally, the quiz submission might experience a slight delay due to server processing.

5. **Session Expiration**: JWT tokens expire after 24 hours, requiring users to log in again.

## 4. Technology Stack

### Backend
- **Runtime**: Node.js
- **Framework**: Express.js
- **Database**: MySQL
- **Authentication**: JWT
- **API Documentation**: Swagger
- **File Storage**: AWS S3

### Mobile Application
- **Framework**: React Native
- **Development Platform**: Expo
- **State Management**: React Context API
- **Navigation**: Expo Router
- **UI Components**: Custom components with Tailwind styling

### Web Admin Panel
- **Framework**: React.js
- **Build Tool**: Vite
- **UI Library**: DaisyUI/Tailwind CSS
- **State Management**: React Context API
- **Routing**: React Router

## 5. Future Development

Planned enhancements for future versions:

1. **Advanced Analytics Dashboard**: Detailed learning analytics for users and administrators
2. **Multimedia Content Support**: Video and audio lessons
3. **Social Learning Features**: Discussion forums and peer interaction
4. **Adaptive Learning Path**: Personalized learning recommendations
5. **Offline Mode Expansion**: Enhanced offline functionality for mobile app
6. **Localization**: Support for multiple languages in the interface
7. **Subscription Tiers**: Premium content and features

---

Document last updated: April 28, 2025
