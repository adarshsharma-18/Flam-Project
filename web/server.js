import express from "express";
import fs from "fs";
import path from "path";
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const port = 3001; // Different port from http-server

// Enable CORS
app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
  next();
});

// Serve static files
app.use(express.static(__dirname));

// Mock frame endpoint
app.get("/api/frame", (req, res) => {
  try {
    const framePath = path.join(__dirname, 'processed_frame.jpg');
    
    if (fs.existsSync(framePath)) {
      const stats = fs.statSync(framePath);
      const frameData = fs.readFileSync(framePath);
      const base64Frame = `data:image/jpeg;base64,${frameData.toString('base64')}`;
      
      res.json({
        frame: base64Frame,
        fps: Math.floor(Math.random() * 10) + 15, // Mock FPS 15-25
        resolution: "1280x720",
        timestamp: new Date().toISOString(),
        fileSize: stats.size,
        lastModified: stats.mtime.toISOString(),
        effects: ["Normal", "Invert", "Grayscale", "Sepia"],
        status: "success"
      });
    } else {
      res.json({
        frame: null,
        fps: 0,
        resolution: "0x0",
        timestamp: new Date().toISOString(),
        fileSize: 0,
        lastModified: null,
        effects: [],
        status: "no_frame",
        message: "No processed frame available. Run Android app and save a frame first."
      });
    }
  } catch (error) {
    res.status(500).json({
      frame: null,
      fps: 0,
      resolution: "0x0",
      timestamp: new Date().toISOString(),
      fileSize: 0,
      lastModified: null,
      effects: [],
      status: "error",
      message: error.message
    });
  }
});

// Health check endpoint
app.get("/api/health", (req, res) => {
  res.json({
    status: "healthy",
    timestamp: new Date().toISOString(),
    server: "Edge Detection Mock API",
    version: "1.0.0"
  });
});

// Stats endpoint
app.get("/api/stats", (req, res) => {
  const framePath = path.join(__dirname, 'processed_frame.jpg');
  const hasFrame = fs.existsSync(framePath);
  
  res.json({
    frameAvailable: hasFrame,
    serverUptime: process.uptime(),
    timestamp: new Date().toISOString(),
    endpoints: ["/api/frame", "/api/health", "/api/stats"],
    mockData: {
      fps: Math.floor(Math.random() * 10) + 15,
      processingTime: Math.floor(Math.random() * 50) + 10,
      memoryUsage: process.memoryUsage()
    }
  });
});

app.listen(port, () => {
  console.log(`🚀 Edge Detection Mock API running on http://localhost:${port}`);
  console.log(`📊 Endpoints:`);
  console.log(`   - GET /api/frame   - Get processed frame data`);
  console.log(`   - GET /api/health  - Health check`);
  console.log(`   - GET /api/stats   - Server statistics`);
  console.log(`📁 Static files served from: ${__dirname}`);
});
