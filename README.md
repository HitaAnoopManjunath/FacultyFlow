# FacultyFlow - Android Application

A clean, modern, minimalist mobile app for faculty-student interactions built with Android Studio using Kotlin and XML.

## Features

### Faculty App
- **Home Dashboard**: Today's schedule with color-coded availability indicators
- **Timetable Upload**: Drag & drop or camera capture for automatic availability setting
- **Booking Inbox**: Manage student booking requests with reply functionality
- **Profile Editor**: Edit faculty information and office hours

### Student App
- **Faculty Directory**: Search and filter faculty by department
- **Faculty Profile**: View faculty details and available time slots
- **Book a Slot**: Visual time-slot grid for booking appointments
- **My Bookings**: Track booking status and view faculty replies

## Design System

### Colors
- **Green** (#4CAF50): Available states
- **Amber** (#FFC107): Busy/manual states  
- **Grey** (#9E9E9E): Inactive/in-class states
- **Light Blue** (#E3F2FD): Interactive highlights

### Typography
- Clean sans-serif style
- Bold headers, medium subtext, light metadata
- Clear visual hierarchy

### UI Components
- Rounded corners (12-16px)
- Soft shadows and elevation
- Card-based layouts
- Minimalist white base theme

## Technical Stack

- **Language**: Kotlin
- **UI**: XML Layouts
- **Architecture**: MVVM with ViewBinding
- **Components**: Material Design Components
- **Images**: Glide for image loading
- **Circular Images**: CircleImageView library

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/facultyflow/
│   │   ├── faculty/                 # Faculty-specific screens
│   │   ├── student/                 # Student-specific screens
│   │   ├── utils/                   # Shared utilities
│   │   ├── models/                  # Data models
│   │   └── adapters/               # RecyclerView adapters
│   ├── res/
│   │   ├── layout/                  # XML layouts
│   │   ├── values/                  # Strings, colors, styles
│   │   └── drawable/               # Drawables and shapes
│   └── AndroidManifest.xml
```

## Key Screens

### Signup Screen
- Student/Faculty user type selection
- Email validation (@rvu.edu.in format)
- Dynamic form fields based on user type

### Faculty Home
- Timeline view of daily schedule
- Quick busy/available toggle
- Pending bookings counter
- Bottom navigation

### Student Directory
- Search functionality
- Department filter chips
- Live availability indicators
- Faculty cards with avatars

## Getting Started

1. Open the project in Android Studio
2. Sync Gradle dependencies
3. Run on emulator or physical device
4. Start with signup flow

## Requirements

- Android SDK 24 (Android 7.0) or higher
- Android Studio Arctic Fox or later
- Kotlin support enabled

## Dependencies

- Material Design Components
- AndroidX libraries
- Glide for image loading
- CircleImageView for circular images
- Navigation Component

## Notes

- Email validation requires @rvu.edu.in domain
- Time slots displayed as visual grid (not dropdown)
- Color-coded availability indicators throughout app
- Expandable cards for detailed information
- Responsive layouts with proper spacing and hierarchy
