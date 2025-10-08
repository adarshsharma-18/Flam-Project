const frame = document.getElementById("frame") as HTMLImageElement;
const stats = document.getElementById("stats") as HTMLElement;

let frameLoaded = false;
let lastUpdateTime = new Date();

function updateStats() {
  const now = new Date();
  const timeDiff = Math.floor((now.getTime() - lastUpdateTime.getTime()) / 1000);
  
  if (frameLoaded && frame.naturalWidth > 0) {
    stats.textContent = `Resolution: ${frame.naturalWidth}x${frame.naturalHeight} | Updated: ${now.toLocaleTimeString()} | Age: ${timeDiff}s`;
    stats.className = "success";
  } else {
    stats.textContent = `Waiting for processed frame... | Checked: ${now.toLocaleTimeString()}`;
    stats.className = "loading";
  }
}

function checkForNewFrame() {
  // Add timestamp to bypass cache
  const timestamp = new Date().getTime();
  const testImage = new Image();
  
  testImage.onload = function() {
    if (!frameLoaded || testImage.width !== frame.naturalWidth || testImage.height !== frame.naturalHeight) {
      frame.src = `processed_frame.jpg?t=${timestamp}`;
      frameLoaded = true;
      lastUpdateTime = new Date();
      console.log("New frame detected and loaded!");
    }
  };
  
  testImage.onerror = function() {
    if (frameLoaded) {
      stats.textContent = "Frame file not found - check Android app";
      stats.className = "error";
    }
  };
  
  testImage.src = `processed_frame.jpg?t=${timestamp}`;
}

// Initial load
frame.onload = function() {
  frameLoaded = true;
  lastUpdateTime = new Date();
  updateStats();
};

frame.onerror = function() {
  frameLoaded = false;
  stats.textContent = "No frame available - run Android app first";
  stats.className = "error";
};

// Update stats every 2 seconds
setInterval(updateStats, 2000);

// Check for new frames every 2 seconds (faster refresh for manual saves)
setInterval(checkForNewFrame, 2000);

// Initial check
setTimeout(checkForNewFrame, 1000);

console.log("Edge Detection Viewer initialized!");
console.log("Waiting for processed_frame.jpg from Android app...");
