#  Advanced Edge Detection System
**Real-time Computer Vision with Android + OpenGL + Web Integration**

A comprehensive edge detection system featuring Android camera processing, OpenGL rendering, and web-based visualization with advanced features.

##  Features Overview

| Feature | Status | Description |
|---------|--------|-------------|
| ** Android Camera Feed** | | Real-time camera preview using Camera2 API |
| ** Edge Detection** | | Pure Android Sobel edge detection (no OpenCV dependencies) |
| ** OpenGL Rendering** | | GPU-accelerated rendering with custom shaders |
| ** Visual Effects** | | Normal, Invert, Grayscale, Sepia shader effects |
| ** View Toggle** | | Switch between raw camera and processed view |
| ** FPS Counter** | | Real-time performance monitoring |
| ** Frame Saving** | | Save processed frames to multiple locations |
| ** Web Viewer** | | TypeScript-based web interface |
| ** Mock API** | | Express.js server with RESTful endpoints |
| ** File Management** | | Automated file transfer and organization |

## 📱 Android App Controls

### Main Interface
- **Camera Preview**: Full-screen real-time camera feed
- **FPS Counter**: Top center - shows real-time performance
- **Effect Button**: Top right - cycles through visual effects (🎨 Normal/Invert/Grayscale/Sepia)

### Bottom Controls
- **📸 Save Frame** (Left): Saves current processed frame to device storage
- **🔄 Show Raw/Processed** (Center): Toggles between raw camera and edge-detected view
- **Edge Detection View** (Right): Small OpenGL-rendered processed output

### Visual Effects (OpenGL Shaders)
1. **Normal**: Standard edge detection output
2. **Invert**: Color-inverted edge detection
3. **Grayscale**: Monochrome edge detection
4. **Sepia**: Vintage-style edge detection

## 🌐 Web Viewer Features

### Smart Frame Loading
- **API Integration**: Fetches frames from Express.js server (port 3001)
- **File Fallback**: Falls back to direct file access if API unavailable
- **Auto-refresh**: Checks for new frames every 2 seconds
- **Real-time Stats**: Shows FPS, resolution, file size, and timestamps

### Endpoints
- `GET /api/frame` - Get processed frame with metadata
- `GET /api/health` - Server health check
- `GET /api/stats` - Performance statistics

## 🏗️ Technical Architecture

### Android Components
```
MainActivity.kt          - Main camera and UI logic
MyGLRenderer.kt          - OpenGL ES 2.0 renderer with shaders
activity_main.xml        - UI layout with controls
AndroidManifest.xml      - Permissions and configuration
```

### Web Components
```
index.html              - Main web interface
main.ts                 - TypeScript logic with API integration
style.css               - Modern dark theme styling
server.js               - Express.js mock API server
```

### File Structure
```
FLAM-project/
├── android/            ← Android Studio project
│   ├── app/
│   │   ├── src/main/java/com/example/cameraapp/
│   │   │   ├── MainActivity.kt
│   │   │   └── MyGLRenderer.kt
│   │   └── res/layout/activity_main.xml
│   └── build.gradle.kts
├── web/                ← Web viewer and API
│   ├── index.html
│   ├── main.ts
│   ├── style.css
│   ├── server.js
│   └── package.json
└── README.md
```

## 🚀 Quick Start

### 1. Android App
```bash
cd android
./gradlew assembleDebug
# Install: android/app/build/outputs/apk/debug/app-debug.apk
```

### 2. Web Viewer + API
```bash
cd web
npm install
npm start          # Starts API server on port 3001
npm run dev        # Starts static server on port 3000
```

### 3. Usage Flow
1. **Launch Android app** → Grant camera/storage permissions
2. **Wait for edge detection** → "✓ Edge detection enabled" message
3. **Tap 📸 Save Frame** → Saves to device storage
4. **Copy frame to web folder** → Use ADB, USB, or batch script
5. **View in browser** → http://localhost:3000

## 🎯 Performance Metrics

### Android Performance
- **FPS**: 15-30 FPS real-time processing
- **Resolution**: Up to 1280x720 processing
- **Memory**: Efficient bitmap management with recycling
- **GPU**: OpenGL ES 2.0 hardware acceleration

### Processing Pipeline
```
Camera Frame → Sobel Edge Detection → OpenGL Texture → GPU Render → Display
     ↓
  Save to Storage → Web API → Browser Display
```

## 🔧 Advanced Features

### OpenGL Shader Effects
Custom GLSL fragment shaders provide real-time visual effects:
- **Color Inversion**: `gl_FragColor = vec4(1.0 - color.rgb, color.a)`
- **Grayscale**: `dot(color.rgb, vec3(0.299, 0.587, 0.114))`
- **Sepia**: Custom color matrix transformation

### Edge Detection Algorithm
Pure Android implementation using Sobel operator:
- Converts RGB to grayscale
- Applies 3x3 Sobel kernels (Gx, Gy)
- Calculates gradient magnitude
- Outputs binary edge map

### API Integration
RESTful endpoints with comprehensive metadata:
```json
{
  "frame": "data:image/jpeg;base64,...",
  "fps": 18,
  "resolution": "1280x720",
  "fileSize": 45632,
  "timestamp": "2025-01-08T18:24:00.000Z",
  "effects": ["Normal", "Invert", "Grayscale", "Sepia"],
  "status": "success"
}
```

## 📊 Project Statistics

- **Languages**: Kotlin, TypeScript, GLSL, JavaScript
- **Frameworks**: Android Camera2, OpenGL ES 2.0, Express.js
- **Dependencies**: Minimal (no OpenCV, pure Android)
- **Performance**: GPU-accelerated, real-time processing
- **Architecture**: Native Android + Web API + Modern UI

---

**🎉 Complete computer vision system demonstrating Android development, OpenGL programming, web integration, and real-time image processing!**
