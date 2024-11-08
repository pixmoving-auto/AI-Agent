# Arc AI agent 1.0

## Overview
AI agent 1.0 minimized the closed loop for creating virtual characters and their interactions through voice and expressions. Four preset virtual characters and a voice library, along with a visual representation of conversation history functions are provided.  
- **Create avatars**  
  Users can customize and create their own avatars. To help achieve this, our project provides four preset characters with different personalities and speaking styles.
    - ***In-car assistant***: An assistant focused on providing information and solving problems.  
    - ***Emotional companion***: An emotionally supportive presence that engages users in comforting conversations.  
    - ***Guide***: A character that provides insights and suggestions about places of interest during the trip.  
    - ***Language teacher***: An educational character that helps users learn and practice new languages.  
- **UI expression mapping**  
We introduced a set of UI expressions corresponding to the content of the conversation, enhancing visual interaction and providing users with a more expressive interface.  
- **Voice switching**  
A voice library is provided, and users can choose different voice tones for the avatar to enhance the personality and charm of the avatar.  
- **Visual interface for conversation history**  
A user-friendly interface displays conversation records, allowing users to easily review their interactions.  
- **Local music playback**  
Users can play local music files to add entertainment to the overall experience.  

## Installation
### Clone Project
- Open the terminal or command line tool.
- Run the following command to clone the repository.  
  `https://github.com/pixmoving-auto/AI-Agent.git`
- Navigate to the cloned project folder and open it in Android Studio.  
### Import Project
- Open Android Studio.
- Select `File` > `New` > `Import Project`.
- Navigate to the directory containing the project, select the project folder, and click OK.
- Wait for Android Studio to configure and sync the project.
 <img width="800" alt="img2" src="https://github.com/user-attachments/assets/538d6a72-fbc4-4c55-ab88-fc43fe1c5c24">

### Software Environment Requirements
- Android Studio version: 2022.3.1
- Gradle version: 7.4
- API level: 33
- JBR (JetBrains Runtime): 17  

## User Mannual

### Initial Setup
-  **Ensure Internet Connection:** Confirm that your device is connected to the internet.  
-  **Access RoboEV App:** On your smartphone's home screen, find and open the **RoboEV app**.  
### Interface Overview
- Upon opening the app, you’ll see the robot’s buttons and status indicators (see Figure 1-1). Clicking on any blank area hides the buttons; click again to make them reappear.  
![1](https://github.com/user-attachments/assets/0228a61e-eb26-4bcc-a710-608b4b255307)  

### Starting and Stopping the Robot
- **Start the Robot:** Click **[Start Robot]**. If the network is stable, the button changes to **[Stop Robot]** and a "Connected" prompt appears, indicating readiness for voice interaction.
- **Stop the Robot:** Tap **[Stop Robot]** to disconnect. A prompt "Connection disconnected, please retry" appears, and the robot will not respond to voice commands.  
![2](https://github.com/user-attachments/assets/d6a97dd7-d1e4-42b6-a266-06fdff5ab2a9)
![3](https://github.com/user-attachments/assets/cb189d2e-fe15-4888-bf7a-8e9c28e0b9aa)  

### Expression Display Settings
- **Disable Expressions:** Click **[Disable Expressions]** for a text-only interface (see Figure 1-4). Robot responses appear as text, with `resp` for the answer and `emo` indicating emotion. Click **[Show Expressions]** to revert to the original mode.  
- **Exit App:** Click **[Close Software]** to exit.  
![4](https://github.com/user-attachments/assets/6f0cde80-ad90-44bf-8d37-58dd5a5a5a76)

### Changing Robot Roles and Voice
- **Switch Roles:** Tap **[Role]** to select a character profile (see Figure 1-5).  
- **Change Voice:** Click **[Voice]** to browse and select a preferred voice tone (see Figure 1-6).  
![5](https://github.com/user-attachments/assets/7c01516e-fdf8-4cfb-89c7-d37ad17b3ec0)  
![6](https://github.com/user-attachments/assets/177d1566-3227-4ae8-89c4-082cd468fdfd)

### Role Editing
- Click **[Role Edit]** to access character customization options (see Figure 1-7). Users can edit character introductions, names, traits, and hobbies. For additional customization, please contact PIX technical support.  
![7](https://github.com/user-attachments/assets/31d7956f-cdc1-4c0f-98f5-bf619caf0dd9)  

### Music Playback
- Tap **[Music]** to access controls: **Play**, **Pause**, and **Stop**.  
- Browse and select from the playlist, then press **Play** to begin music.  
![8](https://github.com/user-attachments/assets/82f693a0-9378-49b7-b7c5-570824d7287f)  

### Language Settings
- **Select Language:** Tap **[Language Settings]** to choose from Chinese, Japanese, or English (see Figure 1-9 to Figure 1-11).  
![9](https://github.com/user-attachments/assets/a2ef4f29-ccba-49aa-a80a-078869d748b6)  
![10](https://github.com/user-attachments/assets/3925cd3d-80df-4ff6-8620-410681f96adf)  
![11](https://github.com/user-attachments/assets/ff99dec5-3dfe-4838-a9ab-55f8b9a18e17)  

### Robot Status Indicators
- **Thinking Mode:** The robot pauses while processing, indicated by a "thinking" icon (see Figure 1-12).  
- **Listening Mode:** While awaiting input, the robot blinks every 2 seconds if no voice is detected (see Figure 1-13).  
![12](https://github.com/user-attachments/assets/e85dc4c4-3163-4c2c-90fe-426ff1c0bb62)  
![13](https://github.com/user-attachments/assets/67bcc8eb-b081-422e-92b1-d634363af5b3)  

### Emotion Display
The robot displays five emotions:
- `0`: Neutral
- `2`: Excited
- `3`: Surprised
- `6`: Sad
- `9`: Confused

### How to Update the App
1. Open **Firefox** or a bookmarked browser and enter the URL: `https://pixmoving.oss-cn-shenzhen.aliyuncs.com/app/RoboEV.apk`.
2. Download and open the app file. If RoboEV is already installed, select **Update**; otherwise, click **Install** and complete the setup.

### Permissions (First-Time Installation)
Grant necessary permissions upon first launch by following the prompts. Afterward, return to the dialogue screen by clicking the top-left arrow.

### How to Uninstall
1. Locate **RoboEV** on your phone’s home screen.
2. Long-press the app icon and select **App Info**.
3. Click **Uninstall**. Confirm and wait for the uninstallation to complete.
