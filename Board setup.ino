#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_ADXL345_U.h>

Adafruit_ADXL345_Unified accel = Adafruit_ADXL345_Unified(12345);
const int ledPin = 2; // LED connected to D2

void setup() {
  Serial.begin(9600);
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);

  // Initialize accelerometer
  if (!accel.begin()) {
    Serial.println("ADXL345 not detected!");
    while (1); // Halt if sensor fails
  }
  
  // Set sensitivity (2/4/8/16 G)
  accel.setRange(ADXL345_RANGE_16_G);
  
  // Optional: Calibrate sensor (place on flat surface)
  Serial.println("Place sensor flat for calibration...");
  delay(2000);
  Serial.println("Calibration complete. Starting readings.");
}

void loop() {
  // Read sensor data
  sensors_event_t event;
  accel.getEvent(&event);
  
  // Calculate magnitude (g-force)
  float magnitude = sqrt(event.acceleration.x * event.acceleration.x + 
                      event.acceleration.y * event.acceleration.y + 
                      event.acceleration.z * event.acceleration.z) / 9.81;
  
  // Send to Java (only the magnitude value)
  Serial.println(magnitude, 2); // 2 decimal places

  // Check for LED commands from Java
  if (Serial.available() > 0) {
    char command = Serial.read();
    if (command == 'A') {
      digitalWrite(ledPin, HIGH); // Earthquake alert
    } 
    else if (command == 'N') {
      digitalWrite(ledPin, LOW); // Normal state
    }
  }

  delay(50); // ~20Hz sampling rate
}