# Earthquake Detector System

A Java-based earthquake detection system using Arduino hardware to monitor seismic activity in real-time.

## Overview

This project implements an earthquake detection system that uses Arduino sensors to detect ground vibrations and tremors. The system processes sensor data through a Java application that analyzes the readings and alerts when potential seismic activity is detected.

## Components

### Hardware
- Arduino board (Uno/Mega recommended)
- Vibration/accelerometer sensor (e.g., MPU6050, ADXL345, or SW-420)
- USB cable for Arduino-PC connection
- Buzzer or LED indicators (optional, for alerts)

### Software
- `EarthQuickDetector.java` - Main Java application for data processing and earthquake detection
- `Board setup.ino` - Arduino firmware for sensor reading and serial communication

## Features

- Real-time vibration monitoring
- Seismic activity detection based on threshold values
- Serial communication between Arduino and Java application
- Data logging and analysis
- Alert system for detected earthquakes

## Prerequisites

- Java Development Kit (JDK) 8 or higher
- Arduino IDE for uploading firmware
- RXTX library or jSerialComm for Java serial communication
- Arduino board with appropriate sensors

## Installation

### Arduino Setup

1. Open `Board setup.ino` in Arduino IDE
2. Connect your Arduino board to your computer
3. Select the correct board and port from Tools menu
4. Upload the sketch to your Arduino board

### Java Application Setup

1. Clone this repository:
```bash
git clone https://github.com/mobinaghrz/Ardoino-java-based-codes.git
cd Ardoino-java-based-codes
```

2. Ensure you have the required serial communication library (RXTX or jSerialComm)

3. Compile the Java application:
```bash
javac EarthQuickDetector.java
```

4. Run the application:
```bash
java EarthQuickDetector
```

## Usage

1. Connect the Arduino board with sensors to your computer via USB
2. Run the Java application
3. The system will start monitoring for vibrations and seismic activity
4. When abnormal vibrations are detected, the system will trigger alerts

## How It Works

1. **Sensor Reading**: The Arduino continuously reads data from the vibration/accelerometer sensor
2. **Data Transmission**: Sensor data is sent to the computer via serial communication
3. **Analysis**: The Java application processes the incoming data and applies detection algorithms
4. **Alert Generation**: When readings exceed defined thresholds, an earthquake alert is generated

## Configuration

You may need to adjust the following parameters based on your setup:

- Serial port name in the Java application
- Vibration threshold values
- Sensor calibration settings
- Sampling rate

## Troubleshooting

- **Serial port not found**: Check that the Arduino is properly connected and you're using the correct port name
- **No data received**: Verify the baud rate matches between Arduino code and Java application
- **False positives**: Adjust the threshold values to reduce sensitivity
- **Sensor not responding**: Check wiring and sensor power supply

## Future Enhancements

- Web-based dashboard for remote monitoring
- Data visualization with graphs and charts
- Machine learning integration for improved detection accuracy
- Multi-sensor support for better coverage
- Cloud storage for historical data analysis

## Contributing

Contributions are welcome! Feel free to submit issues or pull requests to improve the project.

## License

This project is open source and available for educational and research purposes.

## Acknowledgments

This project was developed as an exploration of IoT applications in seismic monitoring and disaster prevention systems.

## Contact

For questions or suggestions, please open an issue in this repository.

---

**Note**: This is an educational project. For critical earthquake detection applications, please use professional-grade seismological equipment.
