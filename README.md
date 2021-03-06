# Jim

An inexpensive interdisciplinary platform for affective AI, Jim the robot has the ability to talk and express and interpret basic human emotions.

The Electronics, Firmware, and Master Control Software subprojects are the components required to operate the first two main functionalities of Jim. 

The Kinect Software subproject is required to operate the third main functionality. An AI is used to interpret the data from the Kinect One sensor to allow Jim to interpret basic human emotions from people's facial expressions.

EDDIE is a later embodiment of Jim.

The Documentation folder contains additional information about Jim/EDDIE if you are interested in learning more.

## Requirements for...

### Electronics

- Any PDF reader to view the PDF of the schematic.

- [Autodesk Eagle](https://www.autodesk.com/products/eagle/free-download) >= 9.5 to modify the schematic, board layout, and/or Gerber files.

### Firmware

#### Software

- [MPLAB X IDE](https://www.microchip.com/mplab/mplab-x-ide) >= 5.25 to modify and develop the firmware.

- MPLAB X IPE >= 5.25 to program the PIC microcontrollers.

  - While installing the IDE, you are presented with the choice of also installed the IPE.
  
  - The versions of MPLAB X IDE and MPLAB X IPE should be the same.
  
#### Hardware

- [PICkit 4 In-Circuit Debugger/Programmer](https://www.microchip.com/developmenttools/ProductDetails/PG164140) to flash the PIC microcontrollers with new firmware.

### Master Control Software

#### Software

- Both [OpenJDK](https://adoptopenjdk.net/) and [OpenJFX](https://gluonhq.com/products/javafx/) >= 11 to run and develop the software.
  - The versions of OpenJDK and OpenJFX should be the same.
  
- [Intellij IDEA](https://www.jetbrains.com/idea/download/) >= 2019.2 (recommended, but not necessary).

#### Hardware 

- [USB to MIDI cable](https://www.amazon.com/HDE-Synthesizer-Microphone-Instrument-Converter/dp/B00D3QFHN8/)

### Kinect Software

#### Software

- [Kinect for Windows SDK 2.0](https://www.microsoft.com/en-us/download/details.aspx?id=44561)

- [Microsoft Visual Studio IDE](https://visualstudio.microsoft.com/) >= 2019

Using the Visual Studio Installer that comes with the IDE... 

- Install the following workloads:

  - .NET desktop development
  
  - Desktop development with C++
  
- Install the following individual components if necessary:

  - .NET framework >= 4.8 SDK
  
  - .NET framework >= 4.8 targeting pack

#### Hardware

- [Microsoft XBox Kinect One Sensor Bar](https://www.amazon.com/Microsoft-Xbox-One-Kinect-Sensor-Renewed/dp/B00YT168WA/)

- [XBox One Kinect Adapter](https://www.amazon.com/Adapter-PartsCrop-Windows-Interactive-Development/dp/B07H9TLNV4/) to connect the Kinect One to a Windows PC.
