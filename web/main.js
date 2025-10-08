const frame = document.getElementById("frame");
const stats = document.getElementById("stats");
let frameLoaded = false;
let lastUpdateTime = new Date();
function updateStats() {
    const now = new Date();
    const timeDiff = Math.floor((now.getTime() - lastUpdateTime.getTime()) / 1000);
    if (frameLoaded && frame.naturalWidth > 0) {
        stats.textContent = `Resolution: ${frame.naturalWidth}x${frame.naturalHeight} | Updated: ${now.toLocaleTimeString()} | Age: ${timeDiff}s`;
        stats.className = "success";
    }
    else {
        stats.textContent = `Waiting for processed frame... | Checked: ${now.toLocaleTimeString()}`;
        stats.className = "loading";
    }
}
function checkForNewFrame() {
    // Add timestamp to bypass cache
    const timestamp = new Date().getTime();
    const testImage = new Image();
    testImage.onload = function () {
        if (!frameLoaded || testImage.width !== frame.naturalWidth || testImage.height !== frame.naturalHeight) {
            frame.src = `processed_frame.jpg?t=${timestamp}`;
            frameLoaded = true;
            lastUpdateTime = new Date();
            console.log("New frame detected and loaded!");
        }
    };
    testImage.onerror = function () {
        if (frameLoaded) {
            stats.textContent = "Frame file not found - check Android app";
            stats.className = "error";
        }
    };
    testImage.src = `processed_frame.jpg?t=${timestamp}`;
}
// Initial load
frame.onload = function () {
    frameLoaded = true;
    lastUpdateTime = new Date();
    updateStats();
};
frame.onerror = function () {
    frameLoaded = false;
    stats.textContent = "No frame available - run Android app first";
    stats.className = "error";
};
// Update stats every 2 seconds
setInterval(updateStats, 2000);
// API-based frame fetching
async function fetchFrameFromAPI() {
    try {
        const response = await fetch("http://localhost:3001/api/frame");
        const data = await response.json();
        if (data.status === "success" && data.frame) {
            frame.src = data.frame;
            frameLoaded = true;
            lastUpdateTime = new Date();
            stats.textContent = `FPS: ${data.fps} | Resolution: ${data.resolution} | Size: ${Math.round(data.fileSize / 1024)}KB`;
            stats.className = "success";
            console.log("Frame fetched from API successfully!");
        }
        else {
            stats.textContent = data.message || "No frame available from API";
            stats.className = "error";
        }
    }
    catch (error) {
        console.log("API not available, falling back to file-based method");
        checkForNewFrame(); // Fallback to original method
    }
}
// Try API first, fallback to file-based
async function smartFrameCheck() {
    try {
        await fetchFrameFromAPI();
    }
    catch (error) {
        checkForNewFrame();
    }
}
// Check for new frames every 2 seconds (faster refresh for manual saves)
setInterval(smartFrameCheck, 2000);
// Initial check
setTimeout(smartFrameCheck, 1000);
console.log("Edge Detection Viewer initialized!");
console.log("Trying API at http://localhost:3001/api/frame first, then file-based fallback...");
